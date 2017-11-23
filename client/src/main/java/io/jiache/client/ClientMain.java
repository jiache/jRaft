package io.jiache.client;

import io.jiache.common.Address;
import io.jiache.core.Session;
import io.jiache.util.Assert;
import io.jiache.util.ParaParser;
import io.jiache.util.StringGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class ClientMain {

    private static final String baseStrategy = "io.jiache.client.TestStrategyImpl";

    /**
     * --help
     * --connect=host1:port1,host2:port2,host3:port3
     * --blockSize=256
     * --benchmarkSize=1000
     * --token=token
     * --create=host1:port1,host2:port2,host3:port3
     * --leaderIndex=leaderIndex
     * --secretary=host1:port1
     * --testStrategy=A(B,C)
     */
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(Arrays.stream(args).anyMatch("--help"::equals)) {
            System.out.println(
                    "     --help\n" +
                    "     --connect=host1:port1,host2:port2,host3:port3\n" +
                    "     --blockSize=256\n" +
                    "     --benchmarkSize=1000\n" +
                    "     --token=token\n" +
                    "     --create=host1:port1,host2:port2,host3:port3\n" +
                    "     --leaderIndex=leaderIndex\n" +
                    "     --secretary=host1:port1" +
                    "     --testStrategy=A(B,C)"
            );
            System.exit(0);
        }

        final Map<String, String> params = ParaParser.parse(args);
        final String connect0 = params.get("connect");
        final String blockSize0 = params.get("blockSize");
        final String benchMarkSize0 = params.get("benchmarkSize");
        final String token0 = params.get("token");
        final String create0 = params.get("create");
        final String leaderIndex0 = params.get("leaderIndex");
        final String secretary0 = params.get("secretary");
        final String testStrategy0 = params.get("testStrategy");

        // connect
        Assert.checkNull(connect0, "connect");
        List<Session> sessions = new ArrayList<>();
        Arrays.stream(connect0.split(",")).forEach((s -> {
            String[] ss = s.split(":");
            sessions.add(new Session(ss[0], Integer.parseInt(ss[1])));
        }));
        final Client client = new Client(sessions);

        // create
        if(create0!=null && !"".equals(create0)) {
            Assert.checkNull(token0, "token");
            Assert.checkNull(leaderIndex0, "leaderIndex");
            List<Address> createAddress = new ArrayList<>();
            Arrays.stream(create0.split(",")).forEach(s -> {
                String[] ss = s.split(":");
                createAddress.add(new Address(ss[0], Integer.parseInt(ss[1])));
            });
            List<Address> secretaryAddress = new ArrayList<>();
            if(secretary0!=null && !"".equals(secretary0)) {
                Arrays.stream(secretary0.split(",")).forEach((s -> {
                    String[] ss = s.split(":");
                    secretaryAddress.add(new Address(ss[0], Integer.parseInt(ss[1])));
                }));
                if (secretaryAddress.size() > 1) {
                    System.out.println("warning secretary number is " + secretaryAddress.size() + " not support in this version.");
                }
            }
            client.newRaftCluster(token0, createAddress, Integer.parseInt(leaderIndex0), secretaryAddress);
        }

        // benchmark
        if(testStrategy0!=null && !"".equals(testStrategy0)) {
            Assert.checkNull(benchMarkSize0, "benchmarkSize");
            Assert.checkNull(blockSize0, "blockSize");
            final int benchmarkSize = Integer.parseInt(benchMarkSize0);
            final TestStrategy testStrategy = (TestStrategy) Class.forName(baseStrategy + testStrategy0).newInstance();
            System.out.println("test " + ((secretary0==null||"".equals(secretary0))?"original":"offload"));
            testStrategy.runBenchmark(client, token0, benchmarkSize);
        }
    }
}
