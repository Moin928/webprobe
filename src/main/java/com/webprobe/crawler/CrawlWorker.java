package com.webprobe.crawler;

import java.time.Instant;

import org.jsoup.nodes.Document;

import com.webprobe.output.CrawlResult;
import com.webprobe.output.CrawlResultWriter;
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

    private final int maxDepth;

    private final int delayMs;

    private final RobotsTxtChecker robotsTxtChecker;

    private final CrawlBudget crawlBudget;

    private final CrawlLifecycle crawlLifecycle;

    private final CrawlResultWriter crawlResultWriter;

    private final CrawlStats crawlStats;

    public CrawlWorker(

        UrlFrontier urlFrontier,

        HttpDownloader httpDownloader,

        HtmlParser htmlParser,

        LinkExtractor linkExtractor,

        NewUrlDispatcher newUrlDispatcher,

        int maxDepth,

        int delayMs,

        RobotsTxtChecker robotsTxtChecker,

        CrawlLifecycle crawlLifecycle,

        CrawlBudget crawlBudget,

        CrawlResultWriter crawlResultWriter,

        CrawlStats crawlStats

    ) {

        this.urlFrontier = urlFrontier;

        this.httpDownloader = httpDownloader;

        this.htmlParser = htmlParser;

        this.linkExtractor = linkExtractor;

        this.newUrlDispatcher = newUrlDispatcher;

        this.maxDepth = maxDepth;

        this.delayMs = delayMs;

        this.robotsTxtChecker = robotsTxtChecker;

        this.crawlLifecycle = crawlLifecycle;

        this.crawlBudget = crawlBudget;

        this.crawlResultWriter = crawlResultWriter;

        this.crawlStats = crawlStats;
    }

    @Override
    public void run() {

        // keep working untill this worker is interrupted or the crawl is finished
        while (!Thread.currentThread().isInterrupted()) {

            boolean taskStarted = false;

            try {

                // wait for another url to appear in the shared frontier
                UrlTask task = urlFrontier.take();

                if (task == null) {
                    continue;
                }

                taskStarted = true;

                // dont process urls deeper than the configured depth
                if (task.depth() > maxDepth) {
                    continue;
                }

                // check robots before using any crawl budget
                if (robotsTxtChecker != null
                        && !robotsTxtChecker.isAllowed(task.url())) {

                    crawlStats.robotsBlocked();

                    System.out.println(
                        "Blocked by robots.txt: " + task.url()
                    );

                    continue;
                }

                // atomically claim one crawl attempt
                if (!crawlBudget.tryAcquire()) {

                    // tell the lifecycle that no more pages can be crawled
                    crawlLifecycle.crawlLimitReached();

                    break;
                }

                System.out.println(
                    "Attempting crawl: " + task
                );

                // download the page from the target server
                DownloadResult result =
                    httpDownloader.download(task.url());

                // this crawler only processes html pages
                if (!result.contentType()
                        .toLowerCase()
                        .startsWith("text/html")) {

                    crawlStats.nonHtmlSkipped();

                    System.out.println(
                        "Skipping non HTML content: "
                        + result.contentType()
                        + " "
                        + task.url()
                    );

                    continue;
                }

                // turn the downloaded html into a jsoup document
                Document document =
                    htmlParser.parse(
                        result.body(),
                        task.url()
                    );

                // find all links contained in this page
                var links =
                    linkExtractor.extract(document);

                // record how many links were found on this page
                crawlStats.urlsDiscovered(links.size());

                // count the page after it has been processed as html
                crawlStats.pageCrawled();

                System.out.println(
                    "Successfully crawled ["
                    + crawlStats.getPagesCrawled()
                    + "] "
                    + task
                );

                System.out.println(
                    "Found: "
                    + links.size()
                    + " links"
                );

                // collect the useful information from the crawled page
                CrawlResult crawlResult =
                    new CrawlResult(
                        task.url(),
                        task.depth(),
                        result.statusCode(),
                        result.contentType(),
                        document.title(),
                        links,
                        Instant.now()
                    );

                // save the result before moving on to more urls
                crawlResultWriter.write(crawlResult);

                // send discovered urls through validation and deduplication
                newUrlDispatcher.dispatch(
                    links,
                    task.depth() + 1
                );

                // slow the worker down so we dont hammer the server
                Thread.sleep(delayMs);

            } catch (InterruptedException e) {

                // restore the interrupt flag so this worker can stop cleanly
                Thread.currentThread().interrupt();

            } catch (Exception e) {

                // record failures without killing the whole crawler
                crawlStats.pageFailed();

                System.out.println(
                    "Failed to crawl page: "
                    + e.getMessage()
                );

            } finally {

                // tell the lifecycle that this worker is done with its task
                if (taskStarted) {
                    crawlLifecycle.taskFinished();
                }
            }
        }
    }
}