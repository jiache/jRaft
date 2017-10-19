package io.jiache.common;

import io.jiache.util.ParaParser;
import org.junit.Test;

import java.util.Map;

public class CommonTest {
    @Test
    public void paraPaserTest() {
        String[] args = {"--", "--std=c++11", "-jdk=1.8", "asdf", "--raftVersion=offload"};
        Map<String, String> map = ParaParser.parse(args);
        map.forEach((key, value)->System.out.println(key+":"+value));
    }
}
