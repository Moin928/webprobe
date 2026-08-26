package com.webprobe.url;

import java.net.URI;

public class UrlValidator {

    public boolean isValid(String url) {

        // first make sure there is actually something to parse
        if (url == null || url.isBlank()) {
            return false;
        }

        try {

            // URI gives us a proper way to check the basic structure
            URI uri = URI.create(url);

            // webprobe only needs normal http and https pages for the MVP
            String scheme = uri.getScheme();

            if (scheme == null
                    || (!scheme.equalsIgnoreCase("http")
                    && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            // a web url needs a host otherwise there isnt really a server to contact
            return uri.getHost() != null;

        } catch (IllegalArgumentException e) {

            // malformed urls should just be ignored instead of killing the crawl
            return false;
        }
    }
}