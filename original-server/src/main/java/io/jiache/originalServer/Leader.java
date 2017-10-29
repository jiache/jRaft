package io.jiache.originalServer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jiache.common.*;
import io.jiache.grpc.original.AppendEntriesRequest;
import io.jiache.grpc.original.AppendEntriesResponse;
import io.jiache.grpc.original.ServerServiceGrpc;
import io.jiache.util.Serializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Leader extends BaseServer{
    private List<ServerServiceGrpc.ServerServiceBlockingStub> blockingStubList;

    public Leader(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
        blockingStubList = new ArrayList<>();
        List<Address> addresses = raftConf.getAddressList();
        for(int i=0; i<addresses.size(); ++i) {
            ServerServiceGrpc.ServerServiceBlockingStub blockingStub = null;
            if(i!=thisIndex) {
                ManagedChannel managedChannel = ManagedChannelBuilder
                        .forAddress(addresses.get(i).getHost(), addresses.get(i).getPort())
                        .usePlaintext(true)
                        .build();
                blockingStub = ServerServiceGrpc.newBlockingStub(managedChannel);
            }
            blockingStubList.add(blockingStub);
        }
    }

    // put操作 只有leader有 不需要logMatch
    public void put(String key, String value) {
        put(new Entry(key, value, term));
    }

    public void put(Entry entry) {
        long index = log.append(entry);
        while(lastCommitIndex<index) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void put(Entry[] entries) {
        Arrays.stream(entries).forEach(this::put);
    }

    private void matainCommit() {
        // 扫描nextIndex, 执行commit
        Long[] sortedNextIndex;
        for(;;) {
            sortedNextIndex = Arrays.copyOfRange(nextIndex, 0, nextIndex.length);
            Arrays.sort(sortedNextIndex);
            long newCommitIndex = sortedNextIndex[sortedNextIndex.length/2+1]-1;
            if(newCommitIndex > lastCommitIndex) {
                commit(newCommitIndex);
            }
        }
    }

    private void appendEntriesToFollower(int followerIndex) {
        if(raftLocks[followerIndex].tryLock()) {
            Entry entry;
            long lastIndex = log.getLastIndex();
            if(nextIndex[followerIndex]<=lastIndex) {
                entry = log.get(nextIndex[followerIndex]);
            } else {
                entry = new Entry(null, null, term);
            }
            AppendEntriesRequest request = AppendEntriesRequest.newBuilder()
                    .setCommittedIndex(lastCommitIndex.intValue())
                    .setTerm(term)
                    .setEntry(new String(Serializer.serialize(entry)))
                    .setPreLogIndex(nextIndex[followerIndex].intValue()-1)
                    .build();
            AppendEntriesResponse response = blockingStubList.get(followerIndex).appendEntries(request);
            if(response.getSuccess() && entry.getKey()!=null) {
                ++nextIndex[followerIndex];
            } else if(term < response.getTerm()) {
                term = response.getTerm(); // TODO become follower
            }
            raftLocks[followerIndex].unlock();
        }
    }

    private void appendEntriesToFollowers() {
        for(;;) {
            for (int i = 0; i < raftConf.getAddressList().size(); ++i) {
                if (i != thisIndex) {
                    int finalI = i;
                    executorService.submit(()->appendEntriesToFollower(finalI));
                }
            }
            try {
                Thread.sleep(HEART_BEAT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        executorService.submit(this::appendEntriesToFollowers);
        executorService.submit(this::matainCommit);
    }
}
