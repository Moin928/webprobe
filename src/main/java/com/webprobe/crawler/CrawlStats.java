package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

public class CrawlStats {

    private final AtomicInteger pagesCrawled;

    private final AtomicInteger pagesFailed;

    private final AtomicInteger robotsBlocked;

    private final AtomicInteger nonHtmlSkipped;

    private final AtomicInteger urlsDiscovered;

    public CrawlStats() {

        // these counters are shared by all crawler workers
        this.pagesCrawled = new AtomicInteger();

        this.pagesFailed = new AtomicInteger();

        this.robotsBlocked = new AtomicInteger();

        this.nonHtmlSkipped = new AtomicInteger();

        this.urlsDiscovered = new AtomicInteger();
    }

    public void pageCrawled() {

        // one page was downloaded and processed successfully
        pagesCrawled.incrementAndGet();
    }

    public void pageFailed() {

        // something went wrong while processing a page
        pagesFailed.incrementAndGet();
    }

    public void robotsBlocked() {

        // robots.txt prevented this url from being crawled
        robotsBlocked.incrementAndGet();
    }

    public void nonHtmlSkipped() {

        // the response was valid but wasnt an html page
        nonHtmlSkipped.incrementAndGet();
    }

    public void urlsDiscovered(int count) {

        // add all links found on the current page
        urlsDiscovered.addAndGet(count);
    }

    public int getPagesCrawled() {

        // return the current successful page count
        return pagesCrawled.get();
    }

    public int getPagesFailed() {

        // return the number of failed pages
        return pagesFailed.get();
    }

    public int getRobotsBlocked() {

        // return the number of urls blocked by robots.txt
        return robotsBlocked.get();
    }

    public int getNonHtmlSkipped() {

        // return the number of non html responses we ignored
        return nonHtmlSkipped.get();
    }

    public int getUrlsDiscovered() {

        // return the total number of links found
        return urlsDiscovered.get();
    }

    public void printSummary() {

        // print one final summary after the crawl is completely finished
        System.out.println();
        System.out.println("========== Crawl Summary ==========");
        System.out.println();
        System.out.println("Pages crawled       : " + getPagesCrawled());
        System.out.println("Pages failed        : " + getPagesFailed());
        System.out.println("Robots blocked      : " + getRobotsBlocked());
        System.out.println("Non HTML skipped    : " + getNonHtmlSkipped());
        System.out.println("URLs discovered     : " + getUrlsDiscovered());
        System.out.println();
        System.out.println("===================================");
    }
}