package io.jiache.offloadServer;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.*;
import io.jiache.grpc.offload.*;
import io.jiache.util.Serializer;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.LongStream;

abstract public class BaseServer extends ServerServiceGrpc.ServerServiceImplBase implements Server {
    protected Integer term;
    protected volatile Long lastCommitIndex;
    protected Log log;
    protected StateMachine stateMachine;
    protected ExecutorService executorService;
    protected RaftConf raftConf;
    protected Integer thisIndex;
    protected volatile Long[] nextIndex;
    protected volatile Lock[] raftLocks;

    protected BaseServer(RaftConf raftConf, int thisIndex) {
        try {
            term = 0;
            lastCommitIndex = -1L;
            log = new DefaultLog(raftConf.getToken()+"-"+ UUID.randomUUID().toString());
            stateMachine = new DefaultStateMachine();
            executorService = Executors.newCachedThreadPool();
            executorService.submit(()-> {
                try {
                    io.grpc.Server server = ServerBuilder.forPort(raftConf.getAddressList().get(thisIndex).getPort())
                            .addService(this)
                            .build()
                            .start();
                    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
                    server.awaitTermination();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            this.raftConf = raftConf;
            this.thisIndex = thisIndex;
            nextIndex = new Long[raftConf.getAddressList().size()];
            for(int i=0; i<raftConf.getAddressList().size(); ++i) {
                nextIndex[i] = 0L;
            }
            raftLocks = new ReentrantLock[raftConf.getAddressList().size()];
            for(int i=0; i<raftLocks.length; ++i) {
                raftLocks[i] = new ReentrantLock();
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public synchronized void commit(long toIndex) {
        long[] logIndex = LongStream.range(lastCommitIndex + 1, toIndex + 1).toArray();
        Entry[] entries = log.get(logIndex);
        stateMachine.commit(entries);
        lastCommitIndex = toIndex;
    }


    public String get(String key) {
        return stateMachine.get(key);
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        int term0 = request.getTerm();
        AppendEntriesResponse.Builder responseBuilder = AppendEntriesResponse.newBuilder();
        if(term0<term) {
            responseObserver.onCompleted();
            responseBuilder.setTerm(term)
                    .setSuccess(false);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }

        term = term0;
        int preLogIndex0 = request.getPreLogIndex();
        Entry entry0 = Serializer.deSerialize(request.getEntry().getBytes(), Entry.class);
        if(!log.match(entry0.getTerm(), preLogIndex0)) {
            responseBuilder.setSuccess(false)
                    .setTerm(term);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }
        log.append(entry0);
        responseBuilder.setTerm(term)
                .setSuccess(true);
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void callBack(CallBackRequest request, StreamObserver<CallBackResponse> responseObserver) {
        CallBackResponse.Builder responseBuilder = CallBackResponse.newBuilder();
        long term0 = request.getTerm();
        if(term0 == term && request.getServerIndex()==raftConf.getLeaderIndex()){
            CallBackResponse response = responseBuilder.setTerm(term)
                    .setSuccess(true)
                    .setReplicatedIndex((int) log.getLastIndex())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            // commit
            long lastCommitIndex0 = request.getLatestCommitIndex();
            lastCommitIndex0 = Math.min(lastCommitIndex0, log.getLastIndex());
            if(lastCommitIndex0>lastCommitIndex) {
                commit(lastCommitIndex0);
            }
            return;
        } else if(term0 > term) {  // TODO detective new leader
            term = (int) term0;
            raftConf.setLeaderIndex(request.getServerIndex());
            responseBuilder.setSuccess(true);
            responseBuilder.setTerm(term);
        } else {  // out of date
            responseBuilder.setSuccess(false);
            responseBuilder.setTerm(term);
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
