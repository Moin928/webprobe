package com.webprobe.processing;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class LinkExtractor {

    public List<String> extract(Document document) {

        // keep the links in a simple list so the dispatcher can process them later
        List<String> links = new ArrayList<>();

        // look through every anchor that actually has a href attribute
        for (Element element : document.select("a[href]")) {

            // jsoup resolves relative links using the document base url
            String url = element.absUrl("href");

            // sometimes an anchor still doesnt resolve to a usable url
            if (url.isBlank()) {
                continue;
            }

            // keep the raw discovered url for the validation pipeline
            links.add(url);
        }

        return links;
    }
}