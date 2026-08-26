package com.webprobe.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {

    private final ExecutorService executorService;
    private final int workerCount;

    public WorkerPool(int workerCount) {

        // the executor keeps exactly this many worker threads available
        this.workerCount = workerCount;

        this.executorService =
            Executors.newFixedThreadPool(workerCount);
    }

    public void start(CrawlWorker worker) {

        // all workers use the same crawler components and shared frontier
        for (int i = 0; i < workerCount; i++) {
            executorService.submit(worker);
        }
    }

    public void shutdown() {

        // stop accepting new work and let running workers finish
        executorService.shutdown();
    }
}