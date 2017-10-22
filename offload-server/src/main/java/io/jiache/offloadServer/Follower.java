package io.jiache.offloadServer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.Address;
import io.jiache.common.Entry;
import io.jiache.common.RaftConf;
import io.jiache.grpc.offload.*;
import io.jiache.util.Serializer;

public class Follower extends BaseServer{
    private ServerServiceGrpc.ServerServiceBlockingStub leaderBlockingStub;

    public Follower(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
    }

    @Override
    public void run() {

    }
}
