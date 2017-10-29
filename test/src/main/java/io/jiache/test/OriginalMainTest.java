package io.jiache.test;

import io.jiache.client.ClientMain;
import io.jiache.core.MainServer;

public class OriginalMainTest {

    public static void main(String[] args) throws InterruptedException {
        new Thread(()-> MainServer.main(new String[]{"--host=127.0.0.1", "--port=8081"})).start();
        new Thread(()->{
            try {
                ClientMain.main(new String[]{
                        "--connect=127.0.0.1:8081",
                        "--blockSize=256",
                        "--benchmarkSize=1000",
                        "--token=token0",
                        "--create=127.0.0.1:9000,127.0.0.1:9001,127.0.0.1:9002,127.0.0.1:9003,127.0.0.1:9004",
                        "--leaderIndex=2",
//                        "--secretary=host1:port1",
                        "--testStrategy=AllRead"
                });
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
