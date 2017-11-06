package io.jiache.client;

import io.jiache.util.StringGenerator;

/**
 * 计算平均response time和throughput
 */
public class TestStrategyImplAllRead implements TestStrategy {
    @Override
    public void runBenchmark(Client client, String token, Integer benchmarkSize) {
        final String value = StringGenerator.randomGenerate(benchmarkSize);
        long begin = System.currentTimeMillis();
        long end;
        for(int i=1; i<=benchmarkSize; ++i) {
            client.put(token, i + "", value).join();
        }
        String s = null;
        while (s == null) {
            s = client.get(token, benchmarkSize+"").join();
        }
        System.out.println(s);

        end = System.currentTimeMillis();
        double averageResponseTime = (double) (end-begin)/benchmarkSize/1e3;
        double throughput = (double)benchmarkSize*1e3/(end-begin);
        System.out.println("average response time : " + averageResponseTime + " sec");
        System.out.println("throughput : " + throughput + " operators/sec");
    }
}
