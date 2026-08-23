package com.webprobe.url;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SeenUrlRegistry {
    
    // this thread-safe set is required because multiple crawler workers will check and add URLs at the same time
    private final Set<String> seenUrls = ConcurrentHashMap.newKeySet();

    public boolean markAsSeen (String url) {
        return seenUrls.add(url);
    }

    public boolean hasSeen(String url) {
        return seenUrls.contains(url);
    }

    public int size() {
        return seenUrls.size();
    }
}
