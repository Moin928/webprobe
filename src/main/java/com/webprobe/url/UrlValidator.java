package com.webprobe.url;

import java.net.URI;

public class UrlValidator {

    public boolean isValid(String url) {
        try {

            URI uri = URI.create(url);

            return uri.getScheme() != null
                    && (uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https"));

        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}