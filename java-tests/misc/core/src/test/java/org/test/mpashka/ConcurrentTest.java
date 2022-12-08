package org.test.mpashka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentTest {

    @Test
    public void testFutureCancel() throws Exception {
        Future<String> f = new CompletableFuture<>();
        new Thread(() -> f.cancel(true)).start();
        try {
            String s = f.get(10, TimeUnit.SECONDS);
            log.info("Result: {}", s);
        } catch (Throwable e) {
            log.info("Error", e);
            // java.util.concurrent.CancellationException
        }
    }

    @Test
    public void testFutureTimeout() throws Exception {
        Future<String> f = new CompletableFuture<>();
        try {
            String s = f.get(1, TimeUnit.MILLISECONDS);
            log.info("Result: {}", s);
        } catch (Throwable e) {
            log.info("Error", e);
            // java.util.concurrent.TimeoutException
        }
    }

    @Test
    public void testFutureException() throws Exception {
        CompletableFuture<String> f = new CompletableFuture<>();
        new Thread(() -> f.completeExceptionally(new RuntimeException("My exception"))).start();
        try {
            String s = f.get(10, TimeUnit.SECONDS);
            log.info("Result: {}", s);
        } catch (Throwable e) {
            log.info("Error", e);
            // java.util.concurrent.ExecutionException
            // Caused by: java.lang.RuntimeException
        }
    }
}
