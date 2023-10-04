package org.mpashka.test.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NioServer {

    private static final String POISON_PILL = "POISON_PILL";
    private int clients = 0;

    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open(StandardProtocolFamily.INET);
        serverSocket.bind(new InetSocketAddress(5454));
        serverSocket.configureBlocking(false);
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(256);
        log.info("Started server on {}", serverSocket.getLocalAddress());

        while (true) {
            int count = selector.select();
//            log.info("Selected: {}", count);
            for (var iter = selector.selectedKeys().iterator(); iter.hasNext();) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    register(selector, serverSocket);
                }

                if (key.isReadable()) {
                    try {
                        answerWithEcho(buffer, key);
                    } catch (IOException e) {
//                        log.info("IO error", e);
                        clients--;
                        if (clients % 100 == 0) {
                            log.info("IO error: {}", clients);
                        }
                    }
                }
            }
        }

    }

    private void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        int read = client.read(buffer);
        if (read == -1) {
            client.close();
            clients--;
            if (clients % 100 == 0) {
                log.info("Channel [{}] was closed: {}", clients, client.getRemoteAddress());
            }
            return;
        }
        String inLine = new String(buffer.array(), 0, buffer.position()).trim();
        log.info("in line: {}", inLine);
        if (inLine.equals(POISON_PILL)) {
            client.close();
            log.info("Not accepting client messages anymore");
        } else {
            buffer.flip();
            client.write(buffer);
            buffer.clear();
        }
    }

    private void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
//        log.info("Client is about to connect");
        SocketChannel client = serverSocket.accept();
//        log.info("Client connected: {}", client.getRemoteAddress());
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        clients++;
        if (clients % 100 == 0) {
            log.info("Client [{}]: {}", clients, client.getRemoteAddress());
        }
    }

    public static void main(String[] args) throws IOException {
        new NioServer().start();
    }
}
