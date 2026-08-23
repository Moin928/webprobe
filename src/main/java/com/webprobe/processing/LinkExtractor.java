package com.webprobe.processing;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LinkExtractor {
    public List<String> extract (Document document) {

        List<String> links = new ArrayList<>();

        // select every <a> element that contains an href attribute
        Elements elements = document.select("a[href");

        for (Element element : elements) {

            //absUrl("href") converts relative Urls into absolute Urls
            //USING THE PAGE'S BASE URL
            String url = element.absUrl("href");

            if(!url.isEmpty()) {
                links.add(url);
            }
        }
            return links;
    }
}