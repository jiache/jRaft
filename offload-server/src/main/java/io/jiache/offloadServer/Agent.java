package io.jiache.offloadServer;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.grpc.offload.AgentServiceGrpc;
import io.jiache.grpc.offload.GetNewSecretaryRequest;
import io.jiache.grpc.offload.GetNewSecretaryResponse;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Agent extends AgentServiceGrpc.AgentServiceImplBase {
    protected ExecutorService executorService;
    private Queue<Address> secretaries;
    private RaftConf raftConf;

    private Agent() {
        executorService = Executors.newCachedThreadPool();
        executorService.submit(() -> {
            try {
                io.grpc.Server server = ServerBuilder.forPort(raftConf.getAgentAddress().getPort())
                        .addService(this)
                        .build()
                        .start();
                Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
                server.awaitTermination();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Agent(Queue<Address> secretaries, RaftConf raftConf) {
        this();
        this.secretaries = secretaries;
        this.raftConf = raftConf;
    }

    @Override
    public void getNewSecretary(GetNewSecretaryRequest request, StreamObserver<GetNewSecretaryResponse> responseObserver) {
        GetNewSecretaryResponse.Builder builder = GetNewSecretaryResponse.newBuilder();
        if (secretaries.isEmpty()) {
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }
        Address address = secretaries.poll();
        responseObserver.onNext(builder
                .setHost(address.getHost())
                .setPort(address.getPort())
                .build());
        responseObserver.onCompleted();
    }


    public class Builder implements io.jiache.Builder<Agent> {
        private Queue<Address> secretaries = new ConcurrentLinkedDeque<>();
        private RaftConf raftConf;

        public Builder appendSecretary(Address address) {
            secretaries.add(address);
            return this;
        }

        public Builder setRaftConf(RaftConf raftConf) {
            this.raftConf = raftConf;
            return this;
        }

        @Override
        public Agent build() {
            assert  raftConf != null;
            return new Agent(secretaries, raftConf);
        }
    }
}
