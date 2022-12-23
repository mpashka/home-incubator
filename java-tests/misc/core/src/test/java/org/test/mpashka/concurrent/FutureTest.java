package org.test.mpashka.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FutureTest {

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
    public void testExecuteAllTimeout() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        List<Callable<String>> tasks = List.of(() -> {
            log.info("Start task...");
            for (int i = 0; i < 3; i++) {
                log.info("Iteration {}", i);
                try {
                    Thread.sleep(10_1000L);
                    return "hello";
                } catch (InterruptedException e) {
                    log.info("Interrupted", e);
//                return "interrupt";
                }
            }
            return "finish";
        });
        List<Future<String>> futures = executorService.invokeAll(tasks, 100, TimeUnit.MILLISECONDS);
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                log.info("Result: {}", result);
            } catch (CancellationException e) {
                log.info("Cancel: {}", future, e);
            } catch (ExecutionException e) {
                log.info("Execution: {}", future, e);
            } catch (Exception e) {
                log.info("Other: {}", future, e);
            }
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
