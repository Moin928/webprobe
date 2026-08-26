package com.webprobe.crawler;

import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Document;

import com.webprobe.processing.HtmlParser;
import com.webprobe.processing.LinkExtractor;
import com.webprobe.processing.NewUrlDispatcher;
import com.webprobe.robots.RobotsTxtChecker;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlTask;

public class CrawlWorker implements Runnable {

    private final UrlFrontier urlFrontier;
    private final HttpDownloader httpDownloader;
    private final HtmlParser htmlParser;
    private final LinkExtractor linkExtractor;
    private final NewUrlDispatcher newUrlDispatcher;
    private final AtomicInteger pagesCrawled;
    private final int maxDepth;
    private final int delayMs;
    private final RobotsTxtChecker robotsTxtChecker;
    private final CrawlBudget crawlBudget;
    private final CrawlLifecycle crawlLifecycle;

    public CrawlWorker(
        UrlFrontier urlFrontier,
        HttpDownloader httpDownloader,
        HtmlParser htmlParser,
        LinkExtractor linkExtractor,
        NewUrlDispatcher newUrlDispatcher,
        AtomicInteger pagesCrawled,
        int maxDepth,
        int delayMs,
        RobotsTxtChecker robotsTxtChecker,
        CrawlLifecycle crawlLifecycle,
        CrawlBudget crawlBudget
    ) {
        this.urlFrontier = urlFrontier;
        this.httpDownloader = httpDownloader;
        this.htmlParser = htmlParser;
        this.linkExtractor = linkExtractor;
        this.newUrlDispatcher = newUrlDispatcher;
        this.pagesCrawled = pagesCrawled;
        this.maxDepth = maxDepth;
        this.delayMs = delayMs;
        this.robotsTxtChecker = robotsTxtChecker;
        this.crawlLifecycle = crawlLifecycle;
        this.crawlBudget = crawlBudget;
    }

    @Override
    public void run() {

        
        // keep taking work until this worker gets interrupted or the budget is gone
        while (!Thread.currentThread().isInterrupted()) {
            boolean taskStarted = false;
            
            try {

                // wait until another task is available in the shared frontier
                UrlTask task = urlFrontier.take();

                // the frontier can return null when there is no work right now
                if (task == null) {
                    continue;
                }

                taskStarted = true;

                // dont follow links deeper than the configured crawl depth
                if (task.depth() > maxDepth) {
                    continue;
                }

                // check robots before spending one of the crawl attempts
                if (robotsTxtChecker != null
                        && !robotsTxtChecker.isAllowed(task.url())) {

                    System.out.println(
                        "Blocked by robots.txt: " + task.url()
                    );

                    continue;
                }

                // atomically claim one crawl attempt
                if (!crawlBudget.tryAcquire()) {
                    break;
                }

                System.out.println(
                    "Attempting crawl: " + task
                );

                // download the page from the target server
                DownloadResult result =
                    httpDownloader.download(task.url());

                // this worker only processes HTML pages
                if (!result.contentType()
                        .toLowerCase()
                        .startsWith("text/html")) {

                    System.out.println(
                        "Skipping non HTML content: "
                        + result.contentType()
                        + " " + task.url()
                    );

                    continue;
                }

                // count the page after we know we actually received HTML
                int pageNumber =
                    pagesCrawled.incrementAndGet();

                System.out.println(
                    "Successfully crawled ["
                    + pageNumber
                    + "] "
                    + task
                );

                // turn the downloaded html into a document we can inspect
                Document document =
                    htmlParser.parse(
                        result.body(),
                        task.url()
                    );

                // find all links that were present on the page
                var links =
                    linkExtractor.extract(document);

                System.out.println(
                    "Found: " + links.size() + " links"
                );

                // send the new links back through validation and deduplication
                newUrlDispatcher.dispatch(
                    links,
                    task.depth() + 1
                );

                // slow the worker down so we dont hammer the server
                Thread.sleep(delayMs);

            } catch (InterruptedException e) {

                // restore the interrupt flag so the worker can stop cleanly
                Thread.currentThread().interrupt();

            } catch (Exception e) {

                // one bad page shouldnt kill the whole crawler
                System.out.println(
                    "Failed to crawl page: " + e.getMessage()
                );
            } finally {
                if (taskStarted) {
                    crawlLifecycle.taskFinished();
                }
            }
        }
    }
}