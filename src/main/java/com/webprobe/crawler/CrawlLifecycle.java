package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

public class CrawlLifecycle {

    private final AtomicInteger pendingTasks;

    private final AtomicInteger activeWorkers;

    private final Object completionMonitor;

    private boolean crawlLimitReached;

    public CrawlLifecycle() {

        // counts tasks that are currently waiting in the frontier
        this.pendingTasks = new AtomicInteger();

        // counts workers that are currently processing a task
        this.activeWorkers = new AtomicInteger();

        // used to wake the thread waiting for the crawl to finish
        this.completionMonitor = new Object();

        // the crawl has not reached its limit yet
        this.crawlLimitReached = false;
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

            // wake up the waiting thread if the crawl can now finish
            if (isFinished()) {

                completionMonitor.notifyAll();
            }
        }
    }

    public void crawlLimitReached() {

        // no more urls should be processed because the page budget is used up
        synchronized (completionMonitor) {

            crawlLimitReached = true;

            // wake up the waiting thread if no workers are still active
            if (isFinished()) {

                completionMonitor.notifyAll();
            }
        }
    }

    public boolean isFinished() {

        // normal completion means there is no waiting or active work
        boolean noWorkLeft =
            pendingTasks.get() == 0
            && activeWorkers.get() == 0;

        // once the crawl limit is reached queued urls dont need to be processed
        boolean limitReachedWithNoActiveWorkers =
            crawlLimitReached
            && activeWorkers.get() == 0;

        return noWorkLeft || limitReachedWithNoActiveWorkers;
    }

    public void awaitCompletion()
            throws InterruptedException {

        // wait until the crawl has either finished normally or hit its limit
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

    public boolean isCrawlLimitReached() {

        // tells us if the crawler has reached its configured page limit
        synchronized (completionMonitor) {

            return crawlLimitReached;
        }
    }
}