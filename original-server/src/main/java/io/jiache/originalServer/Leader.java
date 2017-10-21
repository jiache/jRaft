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
        // 扫描nextIndex, 执行commit
        Thread commitThread = new Thread(() -> {
            Long[] sortedNextIndex;
            for(;;) {
                sortedNextIndex = Arrays.copyOfRange(nextIndex, 0, nextIndex.length);
                Arrays.sort(sortedNextIndex);
                long newCommitIndex = sortedNextIndex[sortedNextIndex.length/2+1]-1;
                if(newCommitIndex > lastCommitIndex) {
                    commit(newCommitIndex);
                    lastCommitIndex = newCommitIndex;
                }
            }
        });
        executorService.submit(commitThread);
    }

    // put操作 只有leader有 不需要logMatch
    public void put(String key, String value) {
        put(new Entry(key, value, term));
    }

    public void put(Entry entry) {
        log.append(entry);
    }

    public void put(Entry[] entries) {
        log.append(entries);
    }

    @Override
    public void run() {
        List<Address> addresses = raftConf.getAddressList();
        for(;;) {
            for (int i = 0; i < addresses.size(); ++i) {
                long lastIndex = log.getLastIndex();
                if (i != thisIndex) {
                    ServerServiceGrpc.ServerServiceBlockingStub blockingStub
                            = blockingStubList.get(i);
                    Entry entry;
                    if(nextIndex[i]<=log.getLastIndex()) {
                        entry = log.get(nextIndex[i]);
                    } else {
                        entry = new Entry(null, null, term);
                    }
                    AppendEntriesRequest request = AppendEntriesRequest.newBuilder()
                            .setCommittedIndex((int) lastCommitIndex)
                            .setTerm(term)
                            .setEntry(new String(Serializer.serialize(entry)))
                            .setPreLogIndex((int) (nextIndex[i]-1))
                            .build();
                    int finalI = i;
                    executorService.submit(() -> {
                        AppendEntriesResponse response = blockingStub.appendEntries(request);
                        if(response.getSuccess() && nextIndex[finalI]<=lastIndex) {
                            ++nextIndex[finalI];
                        } else if(term < response.getTerm()) {
                            term = response.getTerm(); // TODO become follower
                        }
                    });
                }
            }
            try {
                Thread.sleep(HEART_BEAT_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
