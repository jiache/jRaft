package io.jiache.test;


import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.jiache.client.Client;
import io.jiache.common.Address;
import io.jiache.core.MainServer;
import io.jiache.core.Session;
import io.jiache.grpc.SessionServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OriginalTest {
    public static void main(String[] args) throws InterruptedException {
        new Thread(()-> MainServer.main(new String[]{"--host=127.0.0.1", "--port=8081"})).start();
        TimeUnit.SECONDS.sleep(1);

        List<Session> sessions = new ArrayList<>();
        sessions.add(new Session("127.0.0.1", 8081));
        List<Address> serverAddresses = new ArrayList<>();
        serverAddresses.add(new Address("127.0.0.1", 9900));
        serverAddresses.add(new Address("127.0.0.1", 9901));
        serverAddresses.add(new Address("127.0.0.1", 9902));
        serverAddresses.add(new Address("127.0.0.1", 9903));
        serverAddresses.add(new Address("127.0.0.1", 9904));
        Client client = new Client(sessions);
        client.newRaftCluster("raft0", serverAddresses,2,null);

        client.put("raft0", "myKey", "myValue").join();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(client.get("raft0","myKey").join());


    }
}
