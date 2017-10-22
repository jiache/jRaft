package io.jiache.originalServer;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.*;
import io.jiache.grpc.original.*;
import io.jiache.util.Serializer;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

abstract public class BaseServer extends ServerServiceGrpc.ServerServiceImplBase implements Server{
    protected int term;
    protected long lastCommitIndex;
    protected Log log; // 线程安全
    protected StateMachine stateMachine;
    protected ExecutorService executorService;
    protected RaftConf raftConf;
    protected int thisIndex;
    protected Long[] nextIndex;

    protected BaseServer(RaftConf raftConf, int thisIndex) {
        term = 0;
        lastCommitIndex = -1;
        try {
            log = new DefaultLog(raftConf.getToken()+"-"+UUID.randomUUID().toString());
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
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
        Arrays.fill(nextIndex, 0L);
    }

    public synchronized void commit(long toIndex) {
        long[] logIndex = LongStream.range(lastCommitIndex + 1, toIndex+1).toArray();
        Entry[] entries = log.get(logIndex);
        stateMachine.commit(entries);
        lastCommitIndex = toIndex;

    }

    public String get(String key) {
        return stateMachine.get(key);
    }

    @Override
    public synchronized void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
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
        if(!log.match(entry0.getTerm(),preLogIndex0)) {
            responseBuilder.setSuccess(false)
                    .setTerm(term);
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            return;
        }
        if(entry0.getKey() != null) {
            log.append(entry0);
        }
        int committedIndex0 = request.getCommittedIndex();
        committedIndex0 = (int) Math.min(committedIndex0, log.getLastIndex());
        if(committedIndex0>lastCommitIndex) {
            commit(committedIndex0);
            lastCommitIndex = committedIndex0;
        }
        responseBuilder.setTerm(term)
                .setSuccess(true);
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override  // TODO
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        super.requestVote(request, responseObserver);
    }
}
