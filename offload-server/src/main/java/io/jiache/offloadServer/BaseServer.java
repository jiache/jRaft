package io.jiache.offloadServer;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.*;
import io.jiache.grpc.offload.AppendEntriesRequest;
import io.jiache.grpc.offload.AppendEntriesResponse;
import io.jiache.grpc.offload.ServerServiceGrpc;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;

abstract public class BaseServer extends ServerServiceGrpc.ServerServiceImplBase implements Server {
    protected int term;
    protected long lastCommitIndex;
    protected Log log;
    protected StateMachine stateMachine;
    protected ExecutorService executorService;
    protected RaftConf raftConf;
    protected int thisIndex;
    protected Long[] nextIndex;

    protected BaseServer(RaftConf raftConf, int thisIndex) {
        try {
            term = 0;
            lastCommitIndex = -1;
            log = new DefaultLog(raftConf.getToken()+"-"+ UUID.randomUUID().toString());
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
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        super.appendEntries(request, responseObserver);
    }
}
