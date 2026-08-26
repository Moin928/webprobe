package com.webprobe;

import com.webprobe.config.ConfigLoader;
import com.webprobe.config.CrawlerConfig;
import com.webprobe.crawler.CrawlerEngine;
import com.webprobe.crawler.HttpDownloader;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        // load all crawler settings from the properties file
        ConfigLoader configLoader = new ConfigLoader();
        CrawlerConfig config = configLoader.load();

        // one downloader is shared by all crawler workers
        HttpDownloader httpDownloader =
            new HttpDownloader(config);

        // build the crawler using the loaded configuration
        CrawlerEngine crawlerEngine =
            new CrawlerEngine(
                config.getWorkerCount(),
                config.getMaxPages(),
                config.getMaxDepth(),
                config.getUserAgent(),
                config.getDelayMs(),
                config.isRespectRobots(),
                httpDownloader
            );

        // give the crawler its starting point
        crawlerEngine.submit(
            "https://en.wikipedia.org/wiki/Web_crawler"
        );

        // start the worker threads and begin crawling
        crawlerEngine.start();
    }
}