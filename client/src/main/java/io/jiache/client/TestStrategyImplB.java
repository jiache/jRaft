package io.jiache.client;

import io.jiache.util.StringGenerator;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 计算平均response time和throughput
 */
public class TestStrategyImplB implements TestStrategy {
    @Override
    public void runBenchmark(Client client, String token, Integer benchmarkSize) {
        final String value = StringGenerator.randomGenerate(benchmarkSize);
        long begin = System.currentTimeMillis();
        long end = 0;
        for(int i=1; i<=benchmarkSize; ++i) {
            client.put(token, i + "", value).join();
        }
        end = System.currentTimeMillis();
        double averageResponseTime = (double) (end-begin)/benchmarkSize/1e3;
        double throughput = (double)benchmarkSize*1e3/(end-begin);
        System.out.println("average response time : " + averageResponseTime + " sec");
        System.out.println("throughput : " + throughput + " operators/sec");
    }
}
