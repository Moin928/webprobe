package com.webprobe.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.webprobe.config.CrawlerConfig;

public class HttpDownloader {

    private final HttpClient httpClient;
    private final CrawlerConfig config;

    public HttpDownloader(CrawlerConfig config) {

        // keep the crawler settings here instead of passing them into every request
        this.config = config;

        // one client is shared by all workers
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(
                Duration.ofMillis(
                    config.getConnectionTimeout()
                )
            )
            .followRedirects(
                HttpClient.Redirect.NORMAL
            )
            .build();
    }

    public DownloadResult download(String url)
            throws IOException, InterruptedException {

        // create the request for the url given by the worker
        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(
                    Duration.ofMillis(
                        config.getRequestTimeout()
                    )
                )
                .header(
                    "User-Agent",
                    config.getUserAgent()
                )
                .GET()
                .build();

        // send the request and wait for the server response
        HttpResponse<String> response =
            httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

        // only successful HTTP responses continue through the crawler
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                "HTTP request failed with status: "
                + response.statusCode()
            );
        }

        // some servers dont send a content type so keep an empty value
        String contentType =
            response.headers()
                .firstValue("Content-Type")
                .orElse("");

        // return the raw response and let CrawlWorker decide what to do with it
        return new DownloadResult(
            response.body(),
            response.statusCode(),
            contentType
        );
    }
}