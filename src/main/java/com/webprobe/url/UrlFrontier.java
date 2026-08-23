package com.webprobe.url;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UrlFrontier {

    // we are using blocking queue because it is thread safe so multiple workers can work safely.
    // holds url waiting to be crawled
    private final BlockingQueue<UrlTask> queue = new LinkedBlockingQueue<>();

    public void submit(UrlTask task) {

        // add a new crawling task to the queue
        queue.add(task);
    }

    public UrlTask take() throws InterruptedException{

        // wait tilla task is available then remove and retrun the task
        return queue.take();
    }

    public int task() {
        return queue.size();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
