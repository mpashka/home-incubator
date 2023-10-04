package org.mpashka.test.nio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NioClient {

    private static final String POISON_PILL = "POISON_PILL";

    private Selector selector;

    public void start() throws IOException {

//        fillBufferRandom(buffer);
        selector = Selector.open();
    }

    public void client() throws IOException {
        SocketChannel serverChannel = SocketChannel.open(StandardProtocolFamily.INET);
        serverChannel.configureBlocking(false);

        InetAddress localhost = InetAddress.getByAddress(new byte[]{(byte) 127,
//                (byte) (Math.random()*256), (byte) (Math.random()*256), (byte) (Math.random()*256),
                (byte) 0, (byte) 0, (byte) 1,
        });
        serverChannel.connect(new InetSocketAddress(localhost, 5454));
//        serverChannel.write(buffer);
        SelectionKey key = serverChannel.register(selector, SelectionKey.OP_CONNECT);
        key.attach(new ConnectionInfo(false));
    }

    public void ioCycle() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            int count = selector.select();
/*
            if (count > 0) {
                log.info("Selected: {}", count);
            }
*/
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                log.info("    Ready ops: {}", key.readyOps());
                if (key.isConnectable()) {
                    ConnectionInfo connectionInfo = (ConnectionInfo) key.attachment();
                    boolean firstPing = connectionInfo.isFirstPing();
                    key.interestOps(SelectionKey.OP_READ | (firstPing ? SelectionKey.OP_WRITE : 0));
                    SocketChannel client = (SocketChannel) key.channel();
                    boolean finished = client.finishConnect();
                    log.info("Connection is ready: {}", finished);
                }

                if (key.isWritable()) {
                    ConnectionInfo connectionInfo = (ConnectionInfo) key.attachment();
                    boolean firstPing = connectionInfo.isFirstPing();
                    if (firstPing) {
                        log.info("    First ping");
                        connectionInfo.setFirstPing(false);
                        fillBufferRandom(buffer);
                        SocketChannel channel = (SocketChannel) key.channel();
                        channel.write(buffer);
                        key.interestOps(SelectionKey.OP_READ);
                    }
                }

                if (key.isReadable()) {
                    answerWithEcho(buffer, key);
                }
            }
        }
    }

    private static final String chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static void fillBufferRandom(ByteBuffer buffer) {
        byte[] array = buffer.array();
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) chars.charAt((int) (Math.random() * chars.length()));
        }
        buffer.limit(array.length);
        buffer.position(0);
    }

    private static void answerWithEcho(ByteBuffer buffer, SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        int read = client.read(buffer);
        if (read == -1) {
            log.info("Channel was closed");
            client.close();
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

    private static void register(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        NioClient nioClient = new NioClient();
        nioClient.start();
        Thread ioThread = new Thread(() -> {
            try {
                nioClient.ioCycle();
            } catch (IOException e) {
                log.info("IO error", e);
            }
        });
        ioThread.setDaemon(true);
        ioThread.start();
        try {
            for (int i = 0; i < 100000000; i++) {
                if (i % 10000 == 0) {
                    log.info("Starting client {}", i);
                }
                nioClient.client();
            }
        } catch (IOException e) {
            log.info("IO Error", e);
        }
        log.info("Wait for enter...");
        System.in.read();
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public class ConnectionInfo {
        private boolean firstPing;
    }
}
