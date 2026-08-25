package com.webprobe.crawler;

import java.util.concurrent.Semaphore;
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

    public CrawlerEngine( 
        int workerCount, 
        int maxPages,
        int maxDepth,
        String userAgent,
        int delayMs,
        boolean respectRobots,
        HttpDownloader httpDownloader) {

        //shared url queue used by all workers
        this.urlFrontier = new UrlFrontier();
        this.pagesCrawled = new AtomicInteger();

        //tracks urls that have already ben claimed
        SeenUrlRegistry seenUrlRegistry = new SeenUrlRegistry();

        //url processing components
        UrlValidator urlValidator = new UrlValidator();
        UrlNormalizer urlNormalizer = new UrlNormalizer();

        //html processing components
        HtmlParser htmlParser = new HtmlParser();
        LinkExtractor linkExtractor = new LinkExtractor();

        Semaphore crawlSlots = new Semaphore(maxPages);
        

        RobotsTxtChecker robotsTxtChecker =  null;
        if (respectRobots) {
            robotsTxtChecker = new RobotsTxtChecker(userAgent);
        }
        

        // sends discovered urls back into the frontier 
        NewUrlDispatcher newUrlDispatcher = new NewUrlDispatcher(
            urlValidator,
            urlNormalizer,
            seenUrlRegistry,
            urlFrontier
        );

        // create one worker definition that shares the same components
        this.worker = new CrawlWorker(
            urlFrontier,
            httpDownloader,
            htmlParser,
            linkExtractor,
            newUrlDispatcher,
            pagesCrawled,
            crawlSlots,
            maxDepth,
            delayMs,
            robotsTxtChecker
        );
        

        this.workerPool = new WorkerPool(workerCount);

    }

    public void start() {
        workerPool.start(worker);
    }


    public void submit(String url) {
        urlFrontier.submit(new UrlTask(url,0));
    }

    public void shutdown() {
        workerPool.shutdown();
    }
}