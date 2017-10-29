package io.jiache.offloadServer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.*;
import io.jiache.grpc.offload.*;
import io.jiache.util.Serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Secretary extends ServerServiceGrpc.ServerServiceImplBase implements Server {
    private int term;
    private Log log;
    private ExecutorService executorService;
    private RaftConf raftConf;
    private int thisIndex;
    private Long[] nextIndex;
    private Lock[] raftLocks;
    private List<ServerServiceGrpc.ServerServiceBlockingStub> raftStubList;

    public Secretary(RaftConf raftConf, int thisIndex) {
        this.raftConf = raftConf;
        this.thisIndex = thisIndex;
        term = 0;
        log = new MemoryLog();
        executorService = Executors.newCachedThreadPool();
        nextIndex = new Long[raftConf.getAddressList().size()];
        Arrays.fill(nextIndex, 0L);
        raftLocks = new ReentrantLock[nextIndex.length];
        for(int i=0; i<raftLocks.length; ++i) {
            raftLocks[i] = new ReentrantLock();
        }
        raftStubList = new ArrayList<>();
        executorService.submit(()->{
            try {
                io.grpc.Server rpcServer = ServerBuilder
                        .forPort(raftConf.getSecretaryAddressList().get(thisIndex).getPort())
                        .addService(this)
                        .build()
                        .start();
                Runtime.getRuntime().addShutdownHook(new Thread(rpcServer::shutdown));
                rpcServer.awaitTermination();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        List<Address> raftAddresses = raftConf.getAddressList();
        raftAddresses.forEach(address -> {
            ManagedChannel managedChannel = ManagedChannelBuilder
                    .forAddress(address.getHost(), address.getPort())
                    .usePlaintext(true)
                    .build();
            raftStubList.add(ServerServiceGrpc.newBlockingStub(managedChannel));
        });
    }

    @Override
    public void callBack(CallBackRequest request, StreamObserver<CallBackResponse> responseObserver) {
        System.out.println("Error. Leader callback from secretary.");
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        long term0 = request.getTerm();
        Entry entry0 = Serializer.deSerialize(request.getEntry().getBytes(), Entry.class);
        long prelogIndex0 = request.getPreLogIndex();
        AppendEntriesResponse.Builder responseBuilder = AppendEntriesResponse.newBuilder();
        if(term0 == term && log.match(term0, prelogIndex0)) {
            log.append(entry0);
            AppendEntriesResponse response = responseBuilder
                    .setSuccess(true)
                    .setTerm(term)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else if(term0 > term) { // TODO detect a new leader
            term = (int) term0;
            AppendEntriesResponse response = responseBuilder
                    .setSuccess(false)
                    .setTerm(term)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            AppendEntriesResponse response = responseBuilder
                    .setSuccess(false)
                    .setTerm(term)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private void appendEntriesToFollower(int followerIndex) {
        if(raftLocks[followerIndex].tryLock()) {
            long lastIndex = log.getLastIndex();
            for (long i = nextIndex[followerIndex]; i <= lastIndex; ++i) {
                AppendEntriesRequest request = AppendEntriesRequest.newBuilder()
                        .setTerm(term)
                        .setPreLogIndex((int) (i - 1))
                        .setEntry(new String(Serializer.serialize(log.get(i))))
                        .build();
                AppendEntriesResponse response = raftStubList.get(followerIndex)
                        .appendEntries(request);
                if (!response.getSuccess()) {
//                    nextIndex[followerIndex] = i + 1;
                    raftLocks[followerIndex].unlock();
                    return;
                }
            }
            nextIndex[followerIndex] = lastIndex + 1;
            raftLocks[followerIndex].unlock();
        }
    }

    private void appendEntriesToFollowers() {
        for(;;) {
            for(int i=0; i<raftStubList.size(); ++i) {
                if(i != raftConf.getLeaderIndex()) {
                    int finalI = i;
                    executorService.submit(()->this.appendEntriesToFollower(finalI));
                }
            }
            try {
                Thread.sleep(OFFLOAD_SECRETARY_TO_FOLLOWER_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        executorService.submit(this::appendEntriesToFollowers);
    }
}
