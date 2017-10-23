package io.jiache.test;

import io.jiache.client.Client;
import io.jiache.common.Address;
import io.jiache.core.MainServer;
import io.jiache.core.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class OffloadTest {
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

        List<Address> secretaryAddresses = new ArrayList<>();
        secretaryAddresses.add(new Address("127.0.0.1", 9905));
        Client client = new Client(sessions);
        client.newRaftCluster("raft0", serverAddresses,3,secretaryAddresses);

        for(int i=0; i<1000; ++i) {
            String key = "myKey"+i;
            String value = "myValue"+i;
            client.put("raft0", key, value);
        }

        List<Future<String>> futureList = new ArrayList<>();
        for(int i=0; i<1000; ++i) {
            String key = "myKey"+i;
            futureList.add(client.get("raft0",key));
        }
        futureList.clear();
        for(int i=0; i<1000; ++i) {
            String key = "myKey"+i;
            for(;;) {
                String value = client.get("raft0", key).join();
                if(value != null) {
                    System.out.println(value);
                    break;
                }
            }
        }

    }
}
