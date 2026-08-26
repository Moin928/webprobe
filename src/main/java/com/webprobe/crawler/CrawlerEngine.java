package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

import com.webprobe.processing.HtmlParser;
import com.webprobe.processing.LinkExtractor;
import com.webprobe.processing.NewUrlDispatcher;
import com.webprobe.robots.RobotsTxtChecker;
import com.webprobe.url.SeenUrlRegistry;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlNormalizer;
import com.webprobe.url.UrlTask;
import com.webprobe.url.UrlValidator;

public class CrawlerEngine {

    private final UrlFrontier urlFrontier;
    private final WorkerPool workerPool;
    private final CrawlWorker worker;
    private final AtomicInteger pagesCrawled;
    private final CrawlBudget crawlBudget;
    private final CrawlLifecycle crawlLifecycle;

    public CrawlerEngine(
        int workerCount,
        int maxPages,
        int maxDepth,
        String userAgent,
        int delayMs,
        boolean respectRobots,
        HttpDownloader httpDownloader
    ) {

        // this is the shared queue all crawler workers pull tasks from
        this.crawlLifecycle = new CrawlLifecycle();
        this.urlFrontier = new UrlFrontier(crawlLifecycle);

        // keeps track of pages that were actually crawled successfully
        this.pagesCrawled = new AtomicInteger();

        // controls the total number of crawl attempts
        this.crawlBudget = new CrawlBudget(maxPages);

        // keeps duplicate urls from entering the frontier
        SeenUrlRegistry seenUrlRegistry =
            new SeenUrlRegistry();

        // these handle urls before they reach a worker
        UrlValidator urlValidator =
            new UrlValidator();

        UrlNormalizer urlNormalizer =
            new UrlNormalizer();

        // these handle the html after it has been downloaded
        HtmlParser htmlParser =
            new HtmlParser();

        LinkExtractor linkExtractor =
            new LinkExtractor();

        // robots support can be switched off from the config
        RobotsTxtChecker robotsTxtChecker = null;

        if (respectRobots) {
            robotsTxtChecker =
                new RobotsTxtChecker(userAgent);
        }

        // discovered links come back through this pipeline
        NewUrlDispatcher newUrlDispatcher =
            new NewUrlDispatcher(
                urlValidator,
                urlNormalizer,
                seenUrlRegistry,
                urlFrontier
            );

        // all workers share the same crawler components
        this.worker =
            new CrawlWorker(
                urlFrontier,
                httpDownloader,
                htmlParser,
                linkExtractor,
                newUrlDispatcher,
                pagesCrawled,
                maxDepth,
                delayMs,
                robotsTxtChecker,
                crawlLifecycle,
                crawlBudget
            );

        // this controls how many workers can run at the same time
        this.workerPool =
            new WorkerPool(workerCount);
    }

    public void start() throws InterruptedException {

        // start all crawler workers using the shared worker definition
        workerPool.start(worker);

        // wait until there is no pending or active crawl work
        crawlLifecycle.awaitCompletion();

        // the crawl is finished so stop the worker pool cleanly
        workerPool.shutdown();
    }

    public void submit(String url) {

        // the first url starts at depth zero
        urlFrontier.submit(
            new UrlTask(url, 0)
        );
    }

    public void shutdown() {

        // ask the worker pool to stop accepting more work
        workerPool.shutdown();
    }
}