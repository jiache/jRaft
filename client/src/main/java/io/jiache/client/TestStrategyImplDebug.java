package io.jiache.client;

import io.jiache.util.StringGenerator;

/**
 * 计算平均response time和throughput
 */
public class TestStrategyImplDebug implements TestStrategy {
    @Override
    public void runBenchmark(Client client, String token, Integer benchmarkSize) {
        final String value = StringGenerator.randomGenerate(benchmarkSize);
        long begin = System.currentTimeMillis();
        long end = 0;
        for(int i=1; i<=benchmarkSize; ++i) {
            int finalI = i;
            client.put(token, i + "", value).thenRun(()->System.out.println("put "+ finalI)).join();
        }
        end = System.currentTimeMillis();
        double averageResponseTime = (double) (end-begin)/benchmarkSize/1e3;
        double throughput = (double)benchmarkSize*1e3/(end-begin);
        System.out.println("average response time : " + averageResponseTime + " sec");
        System.out.println("throughput : " + throughput + " operators/sec");
    }
}
