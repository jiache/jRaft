package io.jiache.offloadServer;

import io.grpc.stub.StreamObserver;
import io.jiache.common.RaftConf;
import io.jiache.common.Server;
import io.jiache.grpc.offload.*;

public class Leader extends BaseServer {
    protected Leader(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
    }

    @Override
    public void callBack(CallBackRequest request, StreamObserver<CallBackResponse> responseObserver) {
        super.callBack(request, responseObserver);
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
        super.appendEntries(request, responseObserver);
    }

    @Override
    public void run() {

    }
}
