package com.webprobe.url;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeenUrlRegistry {

    // many workers can check this set at the same time
    private final Set<String> seenUrls =
        ConcurrentHashMap.newKeySet();

    public boolean markAsSeen(String url) {

        // add returns false when another worker already added the url
        return seenUrls.add(url);
    }

    public boolean hasSeen(String url) {

        // simple check used when we only want to know if it was seen
        return seenUrls.contains(url);
    }

    public int size() {

        // useful for crawler statistics later
        return seenUrls.size();
    }
}