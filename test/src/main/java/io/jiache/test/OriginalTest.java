package io.jiache.test;


import io.jiache.client.Client;
import io.jiache.common.Address;
import io.jiache.core.MainServer;
import io.jiache.core.Session;

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
        client.newRaftCluster("raft0", serverAddresses,0,null);

        for(int i=0; i<1000; ++i) {
            String key = "myKey"+i;
            String value = "myValue"+i;
            client.put("raft0", key, value).join();
        }
        for(int i=0; i<1000; ++i) {
            String key = "myKey"+i;
            String value;
            while(true) {
                value = client.get("raft0", key).join();
                if(value != null) {
                    System.out.println(value);
                    break;
                }
                Thread.interrupted();
            }
        }
    }
}
