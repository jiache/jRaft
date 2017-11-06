package io.jiache.core;

import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.common.Server;
import io.jiache.originalServer.BaseServer;
import io.jiache.originalServer.Follower;
import io.jiache.originalServer.Leader;
import io.jiache.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OriginalLocalCluster implements LocalCluster{
    private List<Server> cluster;
    private ExecutorService executorService;
    private Integer leaderIndex;
    private boolean running;

    public OriginalLocalCluster(Address localAddress, RaftConf raftConf) {
        cluster = new ArrayList<>();
        leaderIndex = -1;
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
        executorService = Executors.newCachedThreadPool();
        running = false;
    }

    @Override
    public void start() {
        if(!running) {
            for(Server server : cluster) {
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
        if(running && cluster.size()>0) {
            Server server = cluster.get(Random.randomInt(cluster.size()));
            if(server instanceof BaseServer) {
                return ((BaseServer) server).get(key);
            }
        }
        return null;
    }
}
