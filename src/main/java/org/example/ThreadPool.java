package org.example;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ThreadPool {

    private final List<Worker> workers;
    private final Queue<Runnable> taskQueue;
    private volatile boolean isShutdown = false;

    public ThreadPool(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Значение должно быть больше нуля");
        }

        workers = new LinkedList<>();
        taskQueue = new LinkedList<>();

        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker();
            workers.add(worker);
            worker.start();
        }
    }

    public synchronized void execute(Runnable task) {
        if (isShutdown) throw new IllegalStateException("ThreadPool is shutdown and cannot accept new tasks.");
        synchronized (taskQueue) {
            taskQueue.add(task);
            taskQueue.notifyAll();
        }
    }

    public synchronized void shutdown() {
        isShutdown = true;
        synchronized (taskQueue) {
            taskQueue.notifyAll();
        }
    }

    public void awaitTermination() {
        for (Worker worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private class Worker extends Thread {
        @Override
        public void run() {
            while (true) {
                Runnable task;
                synchronized (taskQueue) {
                    while (taskQueue.isEmpty() && !isShutdown) {
                        try {
                            taskQueue.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    if (isShutdown && taskQueue.isEmpty()) break;
                    task = taskQueue.poll();
                }
                if (task != null) task.run();
            }
        }
    }
}