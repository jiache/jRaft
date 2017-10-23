package io.jiache.test;

import io.jiache.client.Client;
import io.jiache.common.Address;
import io.jiache.core.MainServer;
import io.jiache.core.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OffloadWriteTest {
    private final static int benchMarkSize = 2000;
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

        long begin = System.currentTimeMillis();
        for(int i=0; i<benchMarkSize; ++i) {
            String key = "myKey"+i;
            String value = "myValue"+i;
            client.put("raft0", key, value);
        }
        for(int i=0; i<benchMarkSize-10; ++i) {
            String key = "myKey"+i;
            String value = null;
            while(value == null) {
                value = client.get("raft0", key).join();
//                if(value!=null) {
//                    System.out.println(value);
//                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.printf("offload throughput = %f \n",(benchMarkSize*1e3/(end-begin)));
    }
}
