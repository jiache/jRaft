package io.jiache.core;


import io.jiache.util.ParaParser;

import java.io.IOException;
import java.util.Map;

public class MainServer {
    private static SessionListener sessionListener;

    public static void main(String[] args) { // --host=..  --port=..
        Map<String, String> map = ParaParser.parse(args);
        String host = map.get("host");
        String portS = map.get("port");
        if(host==null || portS==null) {
            System.out.println("please input --host= --port=");
            System.exit(-1);
        }
        ServerManager serverManager = new ServerManager(host, Integer.parseInt(portS));
        try {
            sessionListener = new SessionListener(serverManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
