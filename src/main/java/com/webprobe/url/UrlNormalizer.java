package com.webprobe.url;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlNormalizer {

    public String normalize(String url) throws URISyntaxException {

        // dont try to normalise something that isnt even a url
        URI uri = URI.create(url);

        // remove the fragment because it points to a section of the same page
        // /page and /page#section should not become two crawl tasks
        URI normalized = new URI(
            uri.getScheme().toLowerCase(),
            uri.getUserInfo(),
            uri.getHost().toLowerCase(),
            uri.getPort(),
            uri.getPath(),
            uri.getQuery(),
            null
        );

        return normalized.toString();
    }
}