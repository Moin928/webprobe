package com.webprobe.crawler;

public record DownloadResult(
    String body,
    int statusCode,
    String contentType
) {
}