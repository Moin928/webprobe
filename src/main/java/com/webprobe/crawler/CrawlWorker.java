package com.webprobe.crawler;

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
    private final int maxPages;
    private final int delayMs;
    private final RobotsTxtChecker robotsTxtChecker;

    public CrawlWorker(
        UrlFrontier urlFrontier,
        HttpDownloader httpDownloader,
        HtmlParser htmlParser,
        LinkExtractor linkExtractor,
        NewUrlDispatcher newUrlDispatcher,
        AtomicInteger pagesCrawled,
        int maxPages,
        int delayMs,
        RobotsTxtChecker robotsTxtChecker
    ) {
        this.urlFrontier = urlFrontier;
        this.httpDownloader = httpDownloader;
        this.htmlParser = htmlParser;
        this.linkExtractor = linkExtractor;
        this.newUrlDispatcher = newUrlDispatcher;
        this.pagesCrawled = pagesCrawled;
        this.maxPages = maxPages;
        this.delayMs = delayMs;
        this.robotsTxtChecker = robotsTxtChecker;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            
            try{
                // wait untill a url is available in the frontier
                UrlTask task = urlFrontier.take();

                if (task == null) {
                    
                    if (pagesCrawled.get() >= maxPages) {
                        break;
                    }

                    continue;
                }
                
                int pageNumber = pagesCrawled.incrementAndGet();

                if (pageNumber > maxPages) {
                    break;
                }

                System.out.println(
                    "Crawling[" + pageNumber + "/" + maxPages + "]:" + task
                );

                if (robotsTxtChecker != null && !robotsTxtChecker.isAllowed(task.url())) {
                    System.out.println(
                        "Blocked by robots.txt: " + task.url()
                    );
                    continue;
                }

                //download the page
                String html = httpDownloader.download(task.url());

                System.out.println(
                    "Successfully crawled [" + pageNumber + "/" + maxPages + "] " + task
                );

                // convert html into a jsoup document
                Document document = htmlParser.parse(html, task.url());

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
                
            }
        }
    }
}