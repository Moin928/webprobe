package com.webprobe.processing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParser {

    public Document parse(String html, String baseUrl) {

        // jsoup handles the ugly html so the crawler doesnt have to
        return Jsoup.parse(html, baseUrl);
    }
}