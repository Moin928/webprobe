package com.webprobe.processing;

import java.util.List;

import com.webprobe.url.SeenUrlRegistry;
import com.webprobe.url.UrlFrontier;
import com.webprobe.url.UrlNormalizer;
import com.webprobe.url.UrlTask;
import com.webprobe.url.UrlValidator;

public class NewUrlDispatcher {
    private final UrlValidator urlValidator;
    private final UrlNormalizer urlNormalizer;
    private final SeenUrlRegistry seenUrlRegistry;
    private final UrlFrontier urlFrontier;

    public NewUrlDispatcher
    (
        UrlValidator urlValidator,
        UrlNormalizer urlNormalizer,
        SeenUrlRegistry seenUrlRegistry,
        UrlFrontier urlFrontier
    ) {
        this.urlValidator = urlValidator;
        this.urlNormalizer = urlNormalizer;
        this.seenUrlRegistry = seenUrlRegistry; 
        this.urlFrontier = urlFrontier;
    }

    public void dispatch (List<String> urls, int depth) {

        for (String url : urls) {
        //ignore urls that webprobe cannot crawl
            if (!urlValidator.isValid(url)) {
                continue;
            }

            //convert the equivalent representation into one onsistent representation
            String normalizedUrl = urlNormalizer.normalize(url);

            //only submit urls that have not already been claimed by another crawler worker
            if(!seenUrlRegistry.markAsSeen(normalizedUrl)) {
                continue;
            }

            //add the new url to the queue for crawling
            urlFrontier.submit(
                new UrlTask(normalizedUrl,depth)
            );
        } 
    }
}
