package com.webprobe.crawler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.nodes.Document;

import com.webprobe.processing.HtmlParser;
import com.webprobe.processing.LinkExtractor;
import com.webprobe.processing.NewUrlDispatcher;
import com.webprobe.robots.RobotsTxtChecker;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlTask;

public class CrawlWorker implements Runnable{
    
    private final UrlFrontier urlFrontier;
    private final HttpDownloader httpDownloader;
    private final HtmlParser htmlParser;
    private final LinkExtractor linkExtractor;
    private final NewUrlDispatcher newUrlDispatcher;
    private final AtomicInteger pagesCrawled;
    private final Semaphore crawlSlots;
    private final int maxDepth;
    private final int delayMs;
    private final RobotsTxtChecker robotsTxtChecker;

    public CrawlWorker(
        UrlFrontier urlFrontier,
        HttpDownloader httpDownloader,
        HtmlParser htmlParser,
        LinkExtractor linkExtractor,
        NewUrlDispatcher newUrlDispatcher,
        AtomicInteger pagesCrawled,
        Semaphore crawlSlots,
        int maxDepth,
        int delayMs,
        RobotsTxtChecker robotsTxtChecker
    ) {
        this.urlFrontier = urlFrontier;
        this.httpDownloader = httpDownloader;
        this.htmlParser = htmlParser;
        this.linkExtractor = linkExtractor;
        this.newUrlDispatcher = newUrlDispatcher;
        this.pagesCrawled = pagesCrawled;
        this.crawlSlots = crawlSlots;
        this.maxDepth = maxDepth;
        this.delayMs = delayMs;
        this.robotsTxtChecker = robotsTxtChecker;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            
            boolean slotAcquired = false;
            try{
                // wait untill a url is available in the frontier
                UrlTask task = urlFrontier.take();

                if (task == null) {

                    continue;
                }

                if (task.depth() > maxDepth) {
                    continue;
                }
                
                if (!crawlSlots.tryAcquire()) {
                    break;
                }

                slotAcquired = true;

                System.out.println("Attempting crawl: "+ task);

                if (robotsTxtChecker != null && !robotsTxtChecker.isAllowed(task.url())) {
                    System.out.println(
                        "Blocked by robots.txt: " + task.url()
                    );
                    continue;
                }

                //download the page
                DownloadResult result = httpDownloader.download(task.url());

                if (!result.contentType().toLowerCase().startsWith("text/html")) {
                    System.out.println(
                        "Skipping non-HTML content: "
                        + result.contentType()
                        + " " + task.url()
                    );
                    continue;
                }

                String html = result.body();

                int pageNumber = pagesCrawled.incrementAndGet();
                slotAcquired = false;

                System.out.println(
                    "Successfully crawled [" + pageNumber + "] " + task
                );

                // convert html into a jsoup document
                Document document = htmlParser.parse(html,task.url());

                // Extract links from the page
                var links = linkExtractor.extract(document);
                System.out.println("Found: "+ links.size() + " links");

                // sending the discovered urls into the crawling pipeline.
                newUrlDispatcher.dispatch(
                    links,
                    task.depth() + 1
                );

                Thread.sleep(delayMs);

            } catch (InterruptedException e) {

                // restore the inturrept status and stop the worker
                Thread.currentThread().interrupt();

            } catch (Exception e) {

                System.out.println("Failed to crawl page: "+ e.getMessage());
                
            } finally {
                if (slotAcquired) {
                    crawlSlots.release();
                }
            }
        }
    }
}