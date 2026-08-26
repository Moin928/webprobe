package com.webprobe.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CrawlerConfig {

    // these values describe how the whole crawler should behave
    private final int maxPages;
    private final int maxDepth;
    private final int workerCount;
    private final int connectionTimeout;
    private final int requestTimeout;
    private final String userAgent;
    private final boolean respectRobots;
    private final int delayMs;
}