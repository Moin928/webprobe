package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

public class CrawlLifecycle {

    private final AtomicInteger pendingTasks;

    private final AtomicInteger activeWorkers;

    private final Object completionMonitor;

    public CrawlLifecycle() {

        // counts tasks that are currently waiting in the frontier
        this.pendingTasks = new AtomicInteger();

        // counts tasks that workers are currently processing
        this.activeWorkers = new AtomicInteger();

        // used to wake up the thread that is waiting for the crawl to finish
        this.completionMonitor = new Object();
    }

    public void taskAdded() {

        // a new task is entering the crawler so the pending count goes up
        synchronized (completionMonitor) {
            pendingTasks.incrementAndGet();
        }
    }

    public void taskStarted() {

        // the worker has taken the task so it is no longer waiting
        synchronized (completionMonitor) {
            pendingTasks.decrementAndGet();
            activeWorkers.incrementAndGet();
        }
    }

    public void taskFinished() {

        // the worker has finished processing its current task
        synchronized (completionMonitor) {

            activeWorkers.decrementAndGet();

            // only wake the waiting thread when all crawl work is actually done
            if (pendingTasks.get() == 0
                    && activeWorkers.get() == 0) {

                completionMonitor.notifyAll();
            }
        }
    }

    public boolean isFinished() {

        // both waiting work and active work must be gone
        return pendingTasks.get() == 0
            && activeWorkers.get() == 0;
    }

    public void awaitCompletion()
            throws InterruptedException {

        // wait until there is no pending or active crawl work
        synchronized (completionMonitor) {

            while (!isFinished()) {
                completionMonitor.wait();
            }
        }
    }

    public int getPendingTasks() {

        // useful when checking how much work is still waiting
        return pendingTasks.get();
    }

    public int getActiveWorkers() {

        // useful when checking how many workers are currently processing
        return activeWorkers.get();
    }
}