package io.jiache.core;


import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.util.Assert;

import java.util.HashMap;
import java.util.Map;

public class ServerManager {
    private Address address;
    private Map<String, LocalCluster> tokenAndLocalCluster;

    public ServerManager(String host, int port) {
        address = new Address(host, port);
        tokenAndLocalCluster = new HashMap<>();
    }

    public synchronized void addAndExecuteTockenLocal(RaftConf raftConf) {
        System.out.println("manager 21");
        LocalCluster localCluster = LocalClusterFactory.createLocalCluster(address, raftConf);
        System.out.println("server manager 22 token "+raftConf.getToken());
        tokenAndLocalCluster.put(raftConf.getToken(), localCluster);
        LocalCluster cluster = tokenAndLocalCluster.get(raftConf.getToken());
        cluster.start();
    }

    public synchronized void removeTockenLocal(String tocken) {
        LocalCluster cluster = tokenAndLocalCluster.get(tocken);
        if(cluster != null) {
            cluster.shutdown();
            tokenAndLocalCluster.remove(tocken);
        }
    }

    public void put(String token, String key, String value) {
        LocalCluster localCluster = tokenAndLocalCluster.get(token);
        Assert.checkNull(localCluster, "token:"+token+" localCluster");
        localCluster.put(key, value);
    }

    public String get(String token, String key) {
        LocalCluster localCluster = tokenAndLocalCluster.get(token);
        Assert.checkNull(localCluster, "token:"+token+" localCluster");
        return localCluster.get(key);
    }

    public Address getAddress() {
        return address;
    }
}
