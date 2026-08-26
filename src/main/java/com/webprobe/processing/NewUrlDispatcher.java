package com.webprobe.processing;

import java.net.URISyntaxException;
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

    public NewUrlDispatcher(
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

    public void dispatch(List<String> urls, int depth) throws URISyntaxException {

        // go through every link found on the current page
        for (String url : urls) {

            // ignore links that arent something our crawler can handle
            if (!urlValidator.isValid(url)) {
                continue;
            }

            // make different versions of the same url look the same
            String normalizedUrl = urlNormalizer.normalize(url);

            // only the first worker that claims a url gets to submit it
            if (!seenUrlRegistry.markAsSeen(normalizedUrl)) {
                continue;
            }

            // put the new task into the shared queue for a worker
            urlFrontier.submit(
                new UrlTask(normalizedUrl, depth)
            );
        }
    }
}