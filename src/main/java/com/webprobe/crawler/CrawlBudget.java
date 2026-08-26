package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

import lombok.Getter;

@Getter
public class CrawlBudget {

    private final int maxPages;
    private final AtomicInteger pagesStarted;

    public CrawlBudget(int maxPages) {

        // keep the limit in one place so workers dont need to know how it works
        this.maxPages = maxPages;

        // all workers share this counter so the limit stays safe with threads
        this.pagesStarted = new AtomicInteger();
    }

    public boolean tryAcquire() {

        // get the current value before trying to claim a new crawl
        int current = pagesStarted.get();

        while (current < maxPages) {

            // check and update happens as one atomic operation
            // this stops two workers from taking the same last slot
            if (pagesStarted.compareAndSet(current, current + 1)) {
                return true;
            }

            // another worker changed the value so read it again and try
            current = pagesStarted.get();
        }

        // there are no crawl attempts left
        return false;
    }
}