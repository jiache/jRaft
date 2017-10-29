package io.jiache.test;

import io.jiache.client.ClientMain;
import io.jiache.core.MainServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MultiTokenTest {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(()-> MainServer.main(new String[]{"--host=127.0.0.1", "--port=8081"}));
        executor.submit(()-> MainServer.main(new String[]{"--host=", "--port=8081"}));
        Future<?> future1 = executor.submit(() -> {
            try {
                ClientMain.main(new String[]{
                        "--connect=127.0.0.1:8081",
                        "--blockSize=256",
                        "--benchmarkSize=1000",
                        "--token=token1",
                        "--create=127.0.0.1:9100,127.0.0.1:9101,127.0.0.1:9102,127.0.0.1:9103,127.0.0.1:9104,127.0.0.1:9105",
                        "--leaderIndex=2",
                        "--secretary=127.0.0.1:8190",
                        "--testStrategy=A"
                });
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        });

        try {
            future1.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Future<?> future2 = executor.submit(() -> {
            try {
                ClientMain.main(new String[]{
                        "--connect=127.0.0.1:8081",
                        "--blockSize=256",
                        "--benchmarkSize=1000",
                        "--token=token0",
                        "--create=127.0.0.1:9000,127.0.0.1:9001,127.0.0.1:9002,127.0.0.1:9003,127.0.0.1:9004,127.0.0.1:9005",
                        "--leaderIndex=2",
//                        "--secretary=127.0.0.1:8190",
                        "--testStrategy=A"
                });
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        });
    }
}
