package com.webprobe.output;

import java.time.Instant;
import java.util.List;

public record CrawlResult(

    // the url that was actually crawled
    String url,

    // how deep this page was found from the seed
    int depth,

    // http response status returned by the server
    int statusCode,

    // tells us what kind of content the server returned
    String contentType,

    // title of the html page
    String title,

    // links found inside this page
    List<String> links,

    // when this page was crawled
    Instant crawledAt

) {
}