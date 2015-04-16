package org.memnon.server;

import org.httpkit.server.HttpServer;
import org.memnon.util.Configuration;

import java.io.IOException;

/**
 * User: melon
 * Date: 13-12-18
 * Time: 下午5:18
 */
public class Server {

    public static void main(String[] args) throws IOException {
        HttpServer server = new HttpServer(Configuration.getServerHost(), Integer.parseInt(Configuration.getServerPort()), new MultiThreadHandler(4, "worker", 20000), 20480, 2048, 2048);
        server.start();
    }
}
