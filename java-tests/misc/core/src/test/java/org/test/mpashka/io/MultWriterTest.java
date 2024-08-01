package org.test.mpashka.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class MultWriterTest {
    @Test
    public void multipleWrites() throws Exception {
        File file = new File("test.txt");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("------------------------------- begin test");
            out.flush();
            Thread[] threads = new Thread[10];
            for (int i = 0; i < threads.length; i++) {
                int ii = i;
                threads[i] = new Thread(() -> saveToFile(file, ii));
                threads[i].start();
            }
            for (Thread thread : threads) {
                thread.join();
            }
            out.println("------------------------------- end test");
        }
    }

    private void saveToFile(File file, int p) {
        log.info("Start {}", p);
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println("--- begin thread " + p);
            for (int i = 0; i < 10; i++) {
                out.println("thread " + p + " : " + i);
                out.flush();
                Thread.sleep(100);
            }
            out.println("--- end thread " + p);
        } catch (Exception e) {
            log.error("Error: {}/{}", file, p, e);
        }
        log.info("Done {}", p);
    }

    @Test
    public void separateWrites() throws Exception {
        File file = new File("test.txt");
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("------------------------------- begin test");
        }

        Thread[] threads = new Thread[10];
        for (int i = 0; i < threads.length; i++) {
            int ii = i;
            threads[i] = new Thread(() -> saveToFile(file, ii));
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        write(file, "------------------------------- end test");
    }

    private void separateSaveToFile(File file, int p) throws InterruptedException {
        write(file, "--- begin thread " + p);
        for (int i = 0; i < 10; i++) {
            write(file, "thread " + p + " : " + i);
            Thread.sleep(100);
        }
        write(file, "--- end thread " + p);
    }

    private void write(File file, String line) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file, true))) {
            out.println(line);
        } catch (Exception e) {
            log.error("Error: {}/{}", file, e);
        }
    }
}
