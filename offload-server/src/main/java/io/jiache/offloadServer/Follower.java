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

    protected Follower(RaftConf raftConf, int thisIndex) {
        super(raftConf, thisIndex);
        Address leaderAddress = raftConf.getAddressList().get(raftConf.getLeaderIndex());
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(leaderAddress.getHost(), leaderAddress.getPort())
                .usePlaintext(true)
                .build();
        leaderBlockingStub = ServerServiceGrpc.newBlockingStub(managedChannel);
    }

    @Override
    public void callBack(CallBackRequest request, StreamObserver<CallBackResponse> responseObserver) {
        super.callBack(request, responseObserver);
    }

    @Override
    public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesResponse> responseObserver) {
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
        term = term0;
        commit(committedIndex0);
        AppendEntriesResponse response = AppendEntriesResponse.newBuilder()
                .setTerm(this.term)
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void run() {

    }
}
