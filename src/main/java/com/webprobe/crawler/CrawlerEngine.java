package com.webprobe.crawler;

import java.nio.file.Path;

import com.webprobe.output.CrawlResultWriter;
import com.webprobe.output.JsonlCrawlResultWriter;
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

    private final CrawlBudget crawlBudget;

    private final CrawlLifecycle crawlLifecycle;

    private final CrawlResultWriter crawlResultWriter;

    private final CrawlStats crawlStats;

    public CrawlerEngine(

        int workerCount,

        int maxPages,

        int maxDepth,

        String userAgent,

        int delayMs,

        boolean respectRobots,

        HttpDownloader httpDownloader

    ) {

        // this keeps track of the whole crawl from start to finish
        this.crawlLifecycle = new CrawlLifecycle();

        // all workers share the same frontier for pending urls
        this.urlFrontier =
            new UrlFrontier(crawlLifecycle);

        // controls how many crawl attempts are allowed
        this.crawlBudget =
            new CrawlBudget(maxPages);

        // stores statistics shared by every worker
        this.crawlStats =
            new CrawlStats();

        // keeps duplicate urls out of the crawl
        SeenUrlRegistry seenUrlRegistry =
            new SeenUrlRegistry();

        // these handle url validation and normalization
        UrlValidator urlValidator =
            new UrlValidator();

        UrlNormalizer urlNormalizer =
            new UrlNormalizer();

        // these handle the html after it is downloaded
        HtmlParser htmlParser =
            new HtmlParser();

        LinkExtractor linkExtractor =
            new LinkExtractor();

        // robots support can be turned off from the config
        RobotsTxtChecker robotsTxtChecker = null;

        if (respectRobots) {

            robotsTxtChecker =
                new RobotsTxtChecker(userAgent);
        }

        // discovered urls are sent through validation before entering the frontier
        NewUrlDispatcher newUrlDispatcher =
            new NewUrlDispatcher(
                urlValidator,
                urlNormalizer,
                seenUrlRegistry,
                urlFrontier
            );

        // this writer stores each successful crawl result as json
        try {

            this.crawlResultWriter =
                new JsonlCrawlResultWriter(
                    Path.of(
                        "output",
                        "crawl.jsonl"
                    )
                );

        } catch (Exception e) {

            throw new IllegalStateException(
                "Failed to create crawl result writer",
                e
            );
        }

        // all workers share the same crawler components
        this.worker =
            new CrawlWorker(
                urlFrontier,
                httpDownloader,
                htmlParser,
                linkExtractor,
                newUrlDispatcher,
                maxDepth,
                delayMs,
                robotsTxtChecker,
                crawlLifecycle,
                crawlBudget,
                crawlResultWriter,
                crawlStats
            );

        // controls how many crawler threads are running
        this.workerPool =
            new WorkerPool(workerCount);
    }

    public void start() throws InterruptedException {

        // start all crawler workers
        workerPool.start(worker);

        try {

            // wait until there is no more active crawl work
            crawlLifecycle.awaitCompletion();

        } finally {

            // stop the workers after the crawl has finished
            workerPool.shutdown();

            // close the output file after all workers are finished
            try {

                crawlResultWriter.close();

            } catch (Exception e) {

                throw new IllegalStateException(
                    "Failed to close crawl result writer",
                    e
                );
            }
        }

        // print the final results after the crawl is completely finished
        crawlStats.printSummary();
    }

    public void submit(String url) {

        // the first url always starts at depth zero
        urlFrontier.submit(
            new UrlTask(url, 0)
        );
    }

    public void shutdown() {

        // stop the worker pool when the engine is manually shut down
        workerPool.shutdown();

        // close the output writer as well
        try {

            crawlResultWriter.close();

        } catch (Exception e) {

            throw new IllegalStateException(
                "Failed to close crawl result writer",
                e
            );
        }
    }
}