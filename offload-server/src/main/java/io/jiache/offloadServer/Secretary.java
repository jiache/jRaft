package io.jiache.offloadServer;

import io.grpc.stub.StreamObserver;
import io.jiache.common.Log;
import io.jiache.common.RaftConf;
import io.jiache.common.Server;
import io.jiache.common.StateMachine;
import io.jiache.grpc.offload.*;

import java.util.concurrent.ExecutorService;

public class Secretary extends ServerServiceGrpc.ServerServiceImplBase implements Server {
    protected int term;
    protected long lastCommitIndex;
    protected Log log;
    protected StateMachine stateMachine;
    protected ExecutorService executorService;
    protected RaftConf raftConf;
    protected int thisIndex;
    protected Long[] nextIndex;


    public Secretary(RaftConf raftConf, int thisIndex) {
    }

    @Override
    public void callBack(CallBackRequest request, StreamObserver<CallBackResponse> responseObserver) {
        System.out.println("Error. Leader callback from secretary.");
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        super.appendEntries(request, responseObserver);
    }

    @Override
    public void run() {

    }
}
