package com.webprobe.crawler;

import com.webprobe.processing.HtmlParser;
import com.webprobe.processing.LinkExtractor;
import com.webprobe.processing.NewUrlDispatcher;
import com.webprobe.url.SeenUrlRegistry;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlNormalizer;
import com.webprobe.url.UrlTask;
import com.webprobe.url.UrlValidator;

public class CrawlerEngine {
    
    private final UrlFrontier urlFrontier;
    private final WorkerPool workerPool;
    private final CrawlWorker worker;

    public CrawlerEngine( int workerCount, HttpDownloader httpDownloader) {

        //shared url queue used by all workers
        this.urlFrontier = new UrlFrontier();

        //tracks urls that have already ben claimed
        SeenUrlRegistry seenUrlRegistry = new SeenUrlRegistry();

        //url processing components
        UrlValidator urlValidator = new UrlValidator();
        UrlNormalizer urlNormalizer = new UrlNormalizer();

        //html processing components
        HtmlParser htmlParser = new HtmlParser();
        LinkExtractor linkExtractor = new LinkExtractor();

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
            newUrlDispatcher
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