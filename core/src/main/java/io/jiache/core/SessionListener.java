package io.jiache.core;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.jiache.common.RaftConf;
import io.jiache.grpc.*;
import io.jiache.util.Serializer;

import java.io.IOException;

public class SessionListener {
    private ServerManager serverManager;
    public SessionListener(ServerManager serverManager) throws IOException {
        this.serverManager = serverManager;
        Server listener = ServerBuilder.forPort(serverManager.getAddress().getPort())
                .addService(new SessionServiceImpl())
                .build()
                .start();

        Runtime.getRuntime().addShutdownHook(new Thread(listener::shutdown));
        try {
            listener.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class SessionServiceImpl extends SessionServiceGrpc.SessionServiceImplBase{
        @Override
        public void createCluster(CreateRequest request, StreamObserver<CreateResponse> responseObserver) {
            RaftConf raftConf = Serializer.deSerialize(request.getRaftConf().getBytes(), RaftConf.class);
            System.out.println("before execute tocken local");
            serverManager.addAndExecuteTockenLocal(raftConf);
            System.out.println("invoke create SessionListener 34 "+Serializer.serialize(raftConf));
            CreateResponse response = CreateResponse.newBuilder().setSuccess(true).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void put(PutRequest request, StreamObserver<PutResponse> responseObserver) {
            String key = request.getKey();
            String value = request.getValue();
            String token = request.getToken();
            serverManager.put(token, key, value);
            System.out.println("invoke put SessionListener 46");
            PutResponse response = PutResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void get(GetRequest request, StreamObserver<GetResponse> responseObserver) {
            String key = request.getKey();
            String token = request.getToken();
            System.out.println("invoke get SessionListener 57");
            String value = serverManager.get(token, key);
            GetResponse response = GetResponse.newBuilder()
                    .setSuccess(true)
                    .setValue(value)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
