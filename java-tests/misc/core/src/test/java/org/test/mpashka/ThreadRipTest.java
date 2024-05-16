package org.test.mpashka;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ThreadRipTest {
    private static final Runnable LONG_TASK = () -> {
        Thread thread = Thread.currentThread();
        log.info("Executing in {} [{}]: {}", thread.getId(), thread.getState(), thread.getName());
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            log.info("Interrupted");
        }
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            log.info("Interrupted2");
        }
        log.info("Execution done");
    };
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final AtomicInteger num = new AtomicInteger();


    @Test
    public void testScheduledExecutorShutdown() throws InterruptedException {
        testExecutorShutdown("Scheduled executor", () -> {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(10, r -> new Thread(r, "scheduled-thread-" + num.getAndIncrement()));
            executor.schedule(() -> log.info("Executed"), 100, TimeUnit.MILLISECONDS);
            executor.schedule(LONG_TASK, 1, TimeUnit.MILLISECONDS);
            return executor;
        });
    }

    @Test
    public void testExecutorShutdown() throws InterruptedException {
        testExecutorShutdown("Executor", () -> {
            ExecutorService executor = Executors.newFixedThreadPool(10, r -> new Thread(r, "thread-" + num.getAndIncrement()));
            executor.execute(LONG_TASK);
            return executor;
        });
    }

    private void testExecutorShutdown(String name, Supplier<ExecutorService> executorServiceSupplier) throws InterruptedException {
        Set<Long> before = Arrays.stream(threadMXBean.getAllThreadIds()).boxed().collect(Collectors.toSet());

        ExecutorService executor = executorServiceSupplier.get();
        Thread.sleep(5);

        printNewThreads(before, "Before shutdown in " + name);

        List<Runnable> runnables = executor.shutdownNow();
        log.info("Is shutdown / terminated: {} / {}", executor.isShutdown(), executor.isTerminated());
        log.info("{} Runnables: {}", name, runnables);
        printNewThreads(before, "After shutdown in " + name);
        executor.awaitTermination(25, TimeUnit.SECONDS);
        log.info("Is shutdown / terminated: {} / {}", executor.isShutdown(), executor.isTerminated());
        printNewThreads(before, "After await in " + name);
    }

    private void printNewThreads(Set<Long> before, String name) {
        long[] newThreads = Arrays.stream(threadMXBean.getAllThreadIds()).filter(i -> !before.contains(i)).toArray();
        if (newThreads.length == 0) {
            log.info("Now new threads on {}", name);
            return;
        }

        log.info("{} new thread on {}", newThreads.length, name);
        for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(newThreads)) {
            Optional.ofNullable(threadInfo).ifPresent(t -> log.info("    {} [{}]: {}", t.getThreadId(), t.getThreadState(), t.getThreadName()));
        }
    }
}
