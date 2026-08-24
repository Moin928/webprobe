package com.webprobe;

import com.webprobe.config.ConfigLoader;
import com.webprobe.config.CrawlerConfig;
import com.webprobe.crawler.CrawlerEngine;
import com.webprobe.crawler.HttpDownloader;

public class Main {

    public static void main(String[] args) {
        
        //load the crawler config
        ConfigLoader configLoader = new ConfigLoader();
        CrawlerConfig config = configLoader.load();

        // create the http downloader using the config
        HttpDownloader httpDownloader = new HttpDownloader(config);

        // create the crawler engine
        CrawlerEngine crawlerEngine = new CrawlerEngine(
            config.getWorkerCount(),
            config.getMaxPages(),
            config.getUserAgent(),
            config.getDelayMs(),
            config.isRespectRobots(),
            httpDownloader
        );

        //giving the url
        crawlerEngine.submit("https://en.wikipedia.org/wiki/Web_crawler");

        crawlerEngine.start();
    }
}