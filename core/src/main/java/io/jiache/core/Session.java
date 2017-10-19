package io.jiache.core;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.grpc.*;
import io.jiache.util.Serializer;

import java.util.concurrent.CompletableFuture;

public class Session {
    Address connectTo;
    private SessionServiceGrpc.SessionServiceBlockingStub sessionServiceBlockingStub;

    public Session(String host, int port) {
        connectTo = new Address(host, port);
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();
        sessionServiceBlockingStub = SessionServiceGrpc.newBlockingStub(channel);
    }

    public boolean createCluster(RaftConf raftConf) {
            String raftConfJson = new String(Serializer.serialize(raftConf));
            CreateRequest request = CreateRequest.newBuilder()
                    .setRaftConf(raftConfJson)
                    .build();
            CreateResponse response = sessionServiceBlockingStub.createCluster(request);
            return response.getSuccess();
    }

    public CompletableFuture<Boolean> put(String token, String key, String value) {
        return CompletableFuture.supplyAsync(()->{
            PutRequest request = PutRequest.newBuilder()
                    .setKey(key)
                    .setValue(value)
                    .setToken(token)
                    .build();
            PutResponse response = sessionServiceBlockingStub.put(request);
            return response.getSuccess();
        });
    }

    public CompletableFuture<String> get(String token, String key) {
        return CompletableFuture.supplyAsync(()->{
            GetRequest request = GetRequest.newBuilder()
                    .setKey(key)
                    .setToken(token)
                    .build();
            GetResponse responce = sessionServiceBlockingStub.get(request);
            return responce.getValue();
        });
    }


}

