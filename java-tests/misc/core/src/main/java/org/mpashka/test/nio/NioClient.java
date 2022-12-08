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
public class NioClient {

    private static final String POISON_PILL = "POISON_PILL";

    public void start() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);

//        fillBufferRandom(buffer);
        Selector selector = Selector.open();
        SocketChannel serverChannel = SocketChannel.open(StandardProtocolFamily.INET);
        serverChannel.configureBlocking(false);
        serverChannel.connect(new InetSocketAddress("localhost", 5454));
//        serverChannel.write(buffer);
        serverChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

        while (true) {
            int count = selector.select();
            if (count > 0) {
                log.info("Selected: {}", count);
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.info("    {}", key.readyOps());
                if (key.isConnectable()) {
                    log.info("Connection is ready");
                    key.interestOps(SelectionKey.OP_READ);
                }

                if (key.isReadable()) {
                    answerWithEcho(buffer, key);
                }
                iter.remove();
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
        new NioClient().start();
    }
}
