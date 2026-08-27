package com.webprobe.crawler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

        // ask java to give us the response body as a stream
        HttpResponse<InputStream> response =
            httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofInputStream()
            );

        // dont process unsuccessful http responses as pages
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            response.body().close();

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

        // read the response while making sure it doesnt exceed our limit
        String body;

        try (InputStream input = response.body()) {

            body = readResponse(
                input,
                config.getMaxResponseSize()
            );
        }

        // return the downloaded response to the worker
        return new DownloadResult(
            body,
            response.statusCode(),
            contentType
        );
    }

    private String readResponse(
        InputStream input,
        int maxResponseSize
    ) throws IOException {

        // use a byte counter because the limit is about response bytes not characters
        ByteArrayOutputStream output =
            new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];

        int totalBytes = 0;

        int bytesRead;

        // keep reading until the server finishes or the response becomes too large
        while ((bytesRead = input.read(buffer)) != -1) {

            totalBytes += bytesRead;

            // stop before an oversized response can consume too much memory
            if (totalBytes > maxResponseSize) {

                throw new IOException(
                    "HTTP response exceeds maximum allowed size of "
                    + maxResponseSize
                    + " bytes"
                );
            }

            output.write(
                buffer,
                0,
                bytesRead
            );
        }

        // decode the downloaded bytes as utf 8 for the current crawler pipeline
        return output.toString(
            java.nio.charset.StandardCharsets.UTF_8
        );
    }
}