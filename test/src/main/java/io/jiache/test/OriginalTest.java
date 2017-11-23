package io.jiache.test;

import io.jiache.client.Client;
import io.jiache.common.Address;
import io.jiache.core.MainServer;
import io.jiache.core.Session;

import java.util.ArrayList;
import java.util.List;

public class OriginalTest {
    private final static int benchMarkSize = 1000;
    public static void main(String[] args) throws InterruptedException {
        new Thread(()-> MainServer.main(new String[]{"--host=127.0.0.1", "--port=8081"})).start();

        List<Session> sessions = new ArrayList<>();
        sessions.add(new Session("127.0.0.1", 8081));
        List<Address> serverAddresses = new ArrayList<>();
        serverAddresses.add(new Address("127.0.0.1", 9900));
        serverAddresses.add(new Address("127.0.0.1", 9901));
        serverAddresses.add(new Address("127.0.0.1", 9902));
        serverAddresses.add(new Address("127.0.0.1", 9903));
        serverAddresses.add(new Address("127.0.0.1", 9904));

        List<Address> secretaryAddresses = new ArrayList<>();
        secretaryAddresses.add(new Address("127.0.0.1", 9905));

        Client client = new Client(sessions);

        client.newRaftCluster("raft0", serverAddresses,3,null);
        client.put("raft0", "myKey", "myValue").thenRun(()->System.out.println("put")).join();
        String v = null;
        while (v==null) {
            v = client.get("raft0", "myKey").join();
        }
        System.out.println(v);
    }
}
