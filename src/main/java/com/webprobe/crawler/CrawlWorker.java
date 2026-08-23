package com.webprobe.crawler;

import org.jsoup.nodes.Document;

import com.webprobe.processing.HtmlParser;
import com.webprobe.processing.LinkExtractor;
import com.webprobe.processing.NewUrlDispatcher;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlTask;

public class CrawlWorker implements Runnable{
    
    private final UrlFrontier urlFrontier;
    private final HttpDownloader httpDownloader;
    private final HtmlParser htmlParser;
    private final LinkExtractor linkExtractor;
    private final NewUrlDispatcher newUrlDispatcher;

    public CrawlWorker(
        UrlFrontier urlFrontier,
        HttpDownloader httpDownloader,
        HtmlParser htmlParser,
        LinkExtractor linkExtractor,
        NewUrlDispatcher newUrlDispatcher
    ) {
        this.urlFrontier = urlFrontier;
        this.httpDownloader = httpDownloader;
        this.htmlParser = htmlParser;
        this.linkExtractor = linkExtractor;
        this.newUrlDispatcher = newUrlDispatcher;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            
            try{
                // wait untill a url is available in the frontier
                UrlTask task = urlFrontier.take();
                System.out.println("Crawling: "+ task);
                
                //download the page
                String html = httpDownloader.download(task.url());
                System.out.println("Status: 200");

                // convert html into a jsoup document
                Document document = htmlParser.parse(html);

                // Extract links from the page
                var links = linkExtractor.extract(document);
                System.out.println("Found: "+ links.size() + " links");

                // sending the discovered urls into the crawling pipeline.
                newUrlDispatcher.dispatch(
                    links,
                    task.depth() + 1
                );

            } catch (InterruptedException e) {

                // restore the inturrept status and stop the worker
                Thread.currentThread().interrupt();

            } catch (Exception e) {

                System.out.println("Failed to crawl page: "+ e.getMessage());
                
            }
        }
    }
}