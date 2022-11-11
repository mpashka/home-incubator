package org.mpashka.test.geps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebServer {

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", exchange -> {
            byte[] body = null;
            try (InputStream in = exchange.getRequestBody();
                 OutputStream out = exchange.getResponseBody())
            {
                body = in.readAllBytes();
                log.debug("Request received: [{}] {}\n{}", exchange.getRequestMethod(), exchange.getRequestURI(), new String(body));
                log.debug("    Headers: {}", new HashMap<>(exchange.getRequestHeaders()));

                exchange.sendResponseHeaders(200, 0);
            } catch (Exception e) {
                log.error("Error processing request [{}]: {}\n{}", exchange.getRequestMethod(), exchange.getRequestURI(),
                        body == null ? null : new String(body), e);
            }
        });
        server.start();
        log.info("Server started on {}", server.getAddress().getPort());
    }

    public static void main(String[] args) throws IOException {
        new WebServer().start();
    }
}
