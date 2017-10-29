package io.jiache.client;

import io.jiache.common.Address;
import io.jiache.common.RaftConf;
import io.jiache.core.Session;
import io.jiache.util.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Client {
    private List<Session> sessionList;

    public Client(List<Session> sessionList) {
        this.sessionList = sessionList;
    }

    public void newRaftCluster(String token, List<Address> nodeAddress, int leaderIndex, List<Address> secretaryAddress) {
        RaftConf raftConf = new RaftConf(token, nodeAddress, leaderIndex, secretaryAddress);
        sessionList.forEach(session -> session.createCluster(raftConf));
    }

    public CompletableFuture<Boolean> put(String token, String key, String value) {
        return CompletableFuture.supplyAsync(()-> {
            List<CompletableFuture<Boolean>> futureList = new ArrayList<>();
            for (int i = 0; i < sessionList.size(); ++i) {
                futureList.add(sessionList.get(i).put(token, key, value));
            }
            return futureList.parallelStream()
                    .map(CompletableFuture::join)
                    .reduce(((r1, r2) -> r1 && r2))
                    .get();
        });
    }

    public CompletableFuture<String> get(String token, String key) {
        Session session = sessionList.get(Random.randomInt(sessionList.size()));
        return session.get(token, key);
    }
}
