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

import org.junit.jupiter.api.Disabled;
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
    @Disabled
    public void testExecuteAllTimeout() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Callable<String>> tasks = List.of(() -> {
            log.info("Start task...");
            for (int i = 0; i < 3; i++) {
                log.info("Iteration {}", i);
                try {
                    Thread.sleep(10_1000L);
                    return "hello";
                } catch (InterruptedException e) {
                    log.info("Interrupted {}", i, e);
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

        log.info("Stop executor");
        List<Runnable> runnables = executorService.shutdownNow();
        log.info("Executor has {} runnables: {}", runnables.size(), runnables);
        log.info("Executor isShutdown: {}, isTerminated: {}", executorService.isShutdown(), executorService.isTerminated());
        executorService.awaitTermination(11, TimeUnit.SECONDS);
        log.info("Executor has {} runnables: {}", runnables.size(), runnables);
        log.info("Executor isShutdown: {}, isTerminated: {}", executorService.isShutdown(), executorService.isTerminated());
        log.info("Done");
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

    @Test
    public void testAllFutureException() throws Exception {
        CompletableFuture<String>[] f = new CompletableFuture[10];
        for (int i = 0; i < f.length; i++) {
            int idx = i;
            CompletableFuture<String> ff = new CompletableFuture<>();
            f[i] = ff;
            new Thread(() -> {
                log.info("Start thread {}", idx);
                try {
                    Thread.sleep(100 - idx*10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                log.info("Thread exception {}", idx);
                ff.completeExceptionally(new RuntimeException("My exception " + idx));
            }).start();
        }
        CompletableFuture<Void> ff = CompletableFuture.allOf(f);
        Void unused = ff.join();
        log.info("Result: {}", unused);
    }

    @Test
    public void testDone() {
        CompletableFuture<String> f = new CompletableFuture<>();
        log.info("Is done before: {}", f.isDone());
        f.completeExceptionally(new RuntimeException("Hello"));
        log.info("Is done after: {}. Exceptionally: {}. Cancelled: {}", f.isDone(), f.isCompletedExceptionally(), f.isCancelled());
        try {
            f.get();
            log.info("Get done");
        } catch (ExecutionException e) {
            log.info("Get execution exception", e.getCause());
        } catch (Exception e) {
            log.info("Get exception", e);
        }

        CompletableFuture<String> f2 = new CompletableFuture<>();
        log.info("Cancel: {}", f2.cancel(false));
        log.info("Cancel2: {}", f2.cancel(false));
        log.info("Cancel2a: {}", f2.cancel(true));
        log.info("2Is done after: {}. Exceptionally: {}. Cancelled: {}", f2.isDone(), f2.isCompletedExceptionally(), f2.isCancelled());
        try {
            f2.get();
            log.info("2Get done2");
        } catch (CancellationException e) {
            log.info("2Cancel exception", e);
        } catch (Exception e) {
            log.info("2Get2 exception", e);
        }
    }

    @Test
    public void testApply() {
        thenApplyEx("completed normal", false, CompletableFuture.completedFuture("my_val"));
        new Thread(() -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            thenApplyEx("uncompleted normal", false, future);
            future.completeExceptionally(new RuntimeException());
        }).start();

        thenApplyEx("completed exception", true, CompletableFuture.completedFuture("my_val"));
        new Thread(() -> {
            CompletableFuture<String> future = new CompletableFuture<>();
            thenApplyEx("uncompleted exception", true, future);
            future.completeExceptionally(new RuntimeException());
        }).start();
    }

    private void thenApplyEx(String reason, boolean exception, CompletableFuture<String> future) {
        CompletableFuture<String> appliedFuture = future.handle((f, e) -> {
            if (exception) {
                throw new RuntimeException("Apply internal exception");
            }
            return "result for (" + f + ", " + e + "): " + reason;
        });
        try {
            String result = appliedFuture.get();
            log.info("Got result: {}", result);
        } catch (Exception e) {
            log.info("Exception {}", reason, e);
        }
    }

    @Test
    public void testComplete2() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("Result1");
        future.complete("Result2");
        log.info("Result: {}", future.get());
    }

    @Test
    public void testWhenComplete() throws Exception {
        {
            log.info("Test before");
            CompletableFuture<String> futureBefore = new CompletableFuture<>();
            futureBefore.complete("Result1");
            futureBefore.whenComplete((a, e) ->
                    log.info("Completed {}", a, e));
        }

        {
            log.info("Test after");
            CompletableFuture<String> futureBefore = new CompletableFuture<>();
            futureBefore.complete("Result1");
            futureBefore.whenComplete((a, e) ->
                    log.info("Completed {}", a, e));
        }

        {
            log.info("Test before exception");
            CompletableFuture<String> futureBefore = new CompletableFuture<>();
            futureBefore.completeExceptionally(new RuntimeException());
            futureBefore.whenComplete((a, e) ->
                    log.info("Completed {}", a, e));
        }

        {
            log.info("Test after exception");
            CompletableFuture<String> futureBefore = new CompletableFuture<>();
            futureBefore.completeExceptionally(new RuntimeException());
            futureBefore.whenComplete((a, e) ->
                    log.info("Completed {}", a, e));
        }

    }
}
