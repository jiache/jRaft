package io.jiache.originalServer;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.*;
import io.jiache.grpc.*;
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
        try {
            lastCommitIndex = -1;
            log = new DefaultLog(raftConf.getToken()+"-"+UUID.randomUUID().toString());
            stateMachine = new DefaultStateMachine();
            io.grpc.Server server = ServerBuilder.forPort(raftConf.getAddressList().get(thisIndex).getPort())
                    .addService(this)
                    .build()
                    .start();
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
            server.awaitTermination();
            executorService = Executors.newCachedThreadPool();
            this.raftConf = raftConf;
            this.thisIndex = thisIndex;
            nextIndex = new Long[raftConf.getAddressList().size()];
        } catch (IOException | InterruptedException | RocksDBException e) {
            e.printStackTrace();
        }
    }

    public synchronized void commit(long toIndex) {
        int length = (int) (toIndex-lastCommitIndex);
        long[] logIndex = LongStream.range(lastCommitIndex+1, length).toArray();
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
        if(term0<term) {
            responseObserver.onCompleted();
            return;
        }
        int preLogIndex0 = request.getPreLogIndex();
        Entry entry0 = Serializer.deSerialize(request.getEntry().getBytes(), Entry.class);

        if(!log.match(entry0.getTerm(),preLogIndex0)) {
            responseObserver.onCompleted();
            return;
        }
        int committedIndex0 = request.getCommittedIndex();
        log.append(entry0);
        commit(committedIndex0);
    }

    @Override  // TODO
    public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteResponse> responseObserver) {
        super.requestVote(request, responseObserver);
    }
}
