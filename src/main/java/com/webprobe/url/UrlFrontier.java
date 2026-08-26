package com.webprobe.url;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.webprobe.crawler.CrawlLifecycle;

public class UrlFrontier {

    // this queue is shared by all workers so it has to be thread safe
    private final BlockingQueue<UrlTask> queue =
        new LinkedBlockingQueue<>();
    private final CrawlLifecycle crawlLifecycle;

    public UrlFrontier(CrawlLifecycle crawlLifecycle) {
        this.crawlLifecycle = crawlLifecycle;
    }

    public void submit(UrlTask task) {

        // add the task so one of the workers can pick it up
        queue.add(task);
    }

    public UrlTask take() throws InterruptedException {
        // wait for a little while instead of keeping the thread stuck forever
        UrlTask task = queue.poll(500, TimeUnit.MILLISECONDS);
        if (task != null) {
        crawlLifecycle.taskStarted();
        }
        return task;
    }

    public int size() {

        // useful for checking how much work is still waiting
        return queue.size();
    }

    public boolean isEmpty() {

        // tells us if there is currently no pending work
        return queue.isEmpty();
    }
}