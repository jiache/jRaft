package io.jiache.client;

import io.jiache.core.Session;
import io.jiache.util.ParaParser;

import java.util.Map;

public class OnceClientMain {
    public static Session session;
    public static void main(String[] args) {
        Map<String, String> params = ParaParser.parse(args);
        String remoteAddress = params.get("remoteAddress");
    }
}
