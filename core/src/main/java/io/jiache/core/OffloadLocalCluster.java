package io.jiache.core;

import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.common.Server;
import io.jiache.offloadServer.BaseServer;
import io.jiache.offloadServer.Follower;
import io.jiache.offloadServer.Leader;
import io.jiache.offloadServer.Secretary;
import io.jiache.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OffloadLocalCluster implements LocalCluster {
    private List<Server> cluster;
    private List<Server> secretaries;
    private ExecutorService executorService;
    private Integer leaderIndex;
    private boolean running;

    public OffloadLocalCluster(Address localAddress, RaftConf raftConf) {
        leaderIndex = -1;
        cluster = new ArrayList<>();
        secretaries = new ArrayList<>();
        List<Address> addresses = raftConf.getAddressList();
        for(int i=0; i<addresses.size(); ++i) {
            Address address = addresses.get(i);
            if(address.getHost().equals(localAddress.getHost())) {
                if(raftConf.getLeaderIndex()!=i) {
                    cluster.add(new Follower(raftConf, i));
                } else {
                    cluster.add(new Leader(raftConf, i));
                    leaderIndex = cluster.size()-1;
                }
            }
        }
        addresses = raftConf.getSecretaryAddressList();
        for(int i=0; i<addresses.size(); ++i) {
            Address address = addresses.get(i);
            if(address.getHost().equals(localAddress.getHost())) {
                secretaries.add(new Secretary(raftConf, i));
            }
        }
        executorService = Executors.newCachedThreadPool();
        running = false;
    }

    @Override
    public void start() {
        if(!running) {
            for(Server server : cluster) {
                executorService.submit(server);
            }
            for(Server server : secretaries) {
                executorService.submit(server);
            }
            running = true;
        }
    }

    @Override
    public void shutdown() {
        if(running) {
            executorService.shutdown();
            executorService = Executors.newCachedThreadPool();
            cluster.clear();
            secretaries.clear();
            running = false;
        }
    }

    @Override
    public void put(String key, String value) {
        if(leaderIndex>-1 && running) {
            ((Leader)cluster.get(leaderIndex)).put(key, value);
        }
    }

    @Override
    public String get(String key) {
        if(running) {
            Server server = cluster.get(Random.randomInt(cluster.size()));
            return ((BaseServer) server).get(key);
        }
        return null;
    }
}
