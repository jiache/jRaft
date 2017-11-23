package io.jiache.offloadServer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jiache.common.Address;
import io.jiache.common.Entry;
import io.jiache.common.RaftConf;
import io.jiache.grpc.offload.*;
import io.jiache.util.Serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Leader extends BaseServer {
    private List<ServerServiceGrpc.ServerServiceBlockingStub> raftStubList;
    private List<ServerServiceGrpc.ServerServiceBlockingStub> secretaryStubList;
    private volatile Long[] secretaryNextIndex;
    private volatile Lock[] secretaryLocks;

    public Leader(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
        raftStubList = new ArrayList<>();
        secretaryStubList = new ArrayList<>();
        List<Address> raftAddresses = raftConf.getAddressList();
        List<Address> secretaryAddresses = raftConf.getSecretaryAddressList();
        secretaryNextIndex = new Long[secretaryAddresses.size()];
        for(int i=0; i<secretaryAddresses.size(); ++i) {
            secretaryNextIndex[i] = 0L;
        }
        secretaryLocks = new ReentrantLock[secretaryAddresses.size()];
        for(int i = 0; i<secretaryLocks.length; ++i) {
            secretaryLocks[i] = new ReentrantLock();
        }
        for(int i=0; i<raftAddresses.size(); ++i) {
            ServerServiceGrpc.ServerServiceBlockingStub blockingStub = null;
            if(i!=thisIndex) {
                ManagedChannel managedChannel = ManagedChannelBuilder
                        .forAddress(raftAddresses.get(i).getHost(), raftAddresses.get(i).getPort())
                        .usePlaintext(true)
                        .build();
                blockingStub = ServerServiceGrpc.newBlockingStub(managedChannel);
            }
            raftStubList.add(blockingStub);
        }
        secretaryAddresses.forEach(address -> {
            ManagedChannel managedChannel = ManagedChannelBuilder
                    .forAddress(address.getHost(), address.getPort())
                    .usePlaintext(true)
                    .build();
            secretaryStubList.add(ServerServiceGrpc.newBlockingStub(managedChannel));
        });

    }

    public void put(String key, String value) {
        put(new Entry(key, value, term));
    }

    public void put(Entry entry) {
        long index = log.append(entry);
    }

    public void put(Entry[] entries) {
        log.append(entries);
    }

    private void matainCommit() {
        // 扫描nextIndex, 执行commit
        Long[] sortedNextIndex = new Long[nextIndex.length];
        for(;;) {
            for(int i=0; i<sortedNextIndex.length; ++i) {
                sortedNextIndex[i] = nextIndex[i];
            }
            Arrays.sort(sortedNextIndex);
            long newCommitIndex = sortedNextIndex[sortedNextIndex.length/2+1]-1;
            if(newCommitIndex > lastCommitIndex) {
                commit(newCommitIndex);
            }
            Thread.interrupted();
        }

    }

    private void appendEntryToSecretary(int secretaryIndex) {
        if(secretaryLocks[secretaryIndex].tryLock()) {
            long lastIndex = log.getLastIndex();
            for (long i = secretaryNextIndex[secretaryIndex]; i <= lastIndex; ++i) {
                AppendEntriesResponse response;
                AppendEntriesRequest request = AppendEntriesRequest.newBuilder()
                        .setTerm(term)
                        .setPreLogIndex((int) (i - 1))
                        .setEntry(new String(Serializer.serialize(log.get(i))))
                        .build();
                response = secretaryStubList.get(secretaryIndex)
                        .appendEntries(request);
                if (!response.getSuccess()) {
                    System.out.println("leader 109 unsuccess");
                    secretaryNextIndex[secretaryIndex] =  i;
                    secretaryLocks[secretaryIndex].unlock();
                    return;
                }
            }
            secretaryNextIndex[secretaryIndex] =  (lastIndex + 1);
            secretaryLocks[secretaryIndex].unlock();
        }
    }

    private void appendEntriesToSecretaries() {
        for(;;) {
            for(int i=0; i<secretaryNextIndex.length; ++i) {
                int finalI = i;
                executorService.submit(()->this.appendEntryToSecretary(finalI));
            }
            try {
                Thread.sleep(OFFLOAD_TO_SECRETARY_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void callBackToFollower(int followerIndex) {
        if(raftLocks[followerIndex].tryLock()) {
            CallBackRequest request = CallBackRequest.newBuilder()
                    .setTerm(term)
                    .setServerIndex(thisIndex)
                    .setLatestCommitIndex(lastCommitIndex.intValue())
                    .build();
            CallBackResponse response = raftStubList.get(followerIndex)
                    .callBack(request);
            if (response.getSuccess()) {
                long replicatedIndex0 = response.getReplicatedIndex();
                if (nextIndex[followerIndex] <= replicatedIndex0) {
                    nextIndex[followerIndex] = replicatedIndex0+1;
                }
            }
            raftLocks[followerIndex].unlock();
        }
    }

    private void callBackToFollowers() {
        for(;;) {
            for(int i=0; i<nextIndex.length; ++i) {
                int finalI = i;
                executorService.submit(()->this.callBackToFollower(finalI));
            }
            try {
                Thread.sleep(OFFLOAD_LEADER_HEARD_BEAT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        // commit entries
        executorService.submit(this::matainCommit);
        // send appendEntry message to secretaries
        executorService.submit(this::appendEntriesToSecretaries);
        // send callBack message to followers
        executorService.submit(this::callBackToFollowers);
    }
}
