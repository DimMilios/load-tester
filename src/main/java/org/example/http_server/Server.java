package org.example.http_server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class Server {
    static Logger logger = Logger.getLogger(Server.class.getName());

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 1024);

        server.createContext("/", new StatusHandler());

        server.start();
        logger.info("Started HTTP server on port 8000");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping HTTP server");
        }));
    }

    private static class StatusHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(200, "OK\n".length());
            logger.info("HTTP GET request. URI: " + exchange.getRequestURI() + ", Status: " + exchange.getResponseCode() );
            OutputStream os = exchange.getResponseBody();
            os.write("OK\n".getBytes());
            os.flush();
            os.close();
            exchange.close();
        }
    }
}
