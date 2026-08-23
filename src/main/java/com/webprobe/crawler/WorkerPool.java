package com.webprobe.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {
    
    private final ExecutorService executorService;
    private final int workerCount;

    public WorkerPool(int workerCount) {

        this.workerCount = workerCount;
        this.executorService = Executors.newFixedThreadPool(workerCount);

    }

    public void start (CrawlWorker worker) {
        for (int i = 0 ; i < workerCount ; i++) {
        executorService.submit(worker);
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}