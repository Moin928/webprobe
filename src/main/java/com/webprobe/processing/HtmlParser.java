package com.webprobe.processing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlParser {
    public Document parse(String html, String baseUrl) {

        System.out.println("HTML length: " + html.length());
        System.out.println("Base URL: " + baseUrl);
        System.out.println("About to parse...");

        Document document = Jsoup.parse(html, baseUrl);

        System.out.println("Parse complete.");

        return document;
    }
}
