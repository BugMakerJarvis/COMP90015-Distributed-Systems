package edu.unimelb.jarvis.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class MyThreadPool {
    private final BlockingQueue<Runnable> taskQueue;
    private final Thread[] threads;

    public MyThreadPool(int poolSize, int taskQueueSize) {
        taskQueue = new ArrayBlockingQueue<>(taskQueueSize);
        threads = new Thread[poolSize];

        for (int i = 0; i < poolSize; i++) {
            threads[i] = new Thread(() -> {
                try {
                    while (true) {
                        Runnable task = taskQueue.take();
                        task.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Reset the interrupted status
                    log.error("Thread interrupted", e);
                }
            });
            threads[i].start();
        }
    }

    public void execute(Runnable task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Reset the interrupted status
            log.error("Task submission interrupted", e);
        }
    }

    public void shutdown() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
    }

    public void shutdownNow() {
        for (Thread thread : threads) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Reset the interrupted status
                log.error("Thread join interrupted", e);
            }
        }
    }
}
