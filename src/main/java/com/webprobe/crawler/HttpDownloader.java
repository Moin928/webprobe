package com.webprobe.crawler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.webprobe.config.CrawlerConfig;

public class HttpDownloader {
    
    private final HttpClient httpClient;
    private final CrawlerConfig config;

    public HttpDownloader // java's inbuilt http client
     (CrawlerConfig config) {
        this.config = config;

        this.httpClient = HttpClient.newBuilder()
        .connectTimeout(
            java.time.Duration.ofMillis(
                config.getConnectionTimeout()
            )
        )
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();
    }

    public String download(String url) throws IOException, InterruptedException {
        
        HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .timeout(
            java.time.Duration.ofMillis(
                config.getRequestTimeout()
            )
        )
        .header("User-Agent",config.getUserAgent())
        .GET()
        .build();

        //sending the request and waiting for response

        HttpResponse<String> response = httpClient.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        // only return the page if server reports success
        if (response.statusCode() >= 200 && response.statusCode() < 300) {

            String contentType = response.headers()
            .firstValue("Content-Type")
            .orElse("");

            if (!contentType.toLowerCase().contains("text/html")) {
                throw new IOException(
                    "Skipping non-HTML content: " + contentType
                );
            }

            return response.body();
        }

        throw new IOException(
            "Http Request failed with Status: " + response.statusCode()
        );
    }

}
