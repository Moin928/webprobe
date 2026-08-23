package com.webprobe.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    public CrawlerConfig load() {

        Properties properties = new Properties();

        try (
            InputStream input = getClass()
            .getClassLoader()
            .getResourceAsStream("crawler.properties")
        ){

            if (input == null)
                 throw new IllegalArgumentException("crawler.properties not found");

            properties.load(input);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load crawler configuration", e);
        }

        return new CrawlerConfig(
                Integer.parseInt(properties.getProperty("crawler.max-pages")),
                Integer.parseInt(properties.getProperty("crawler.max-depth")),
                Integer.parseInt(properties.getProperty("crawler.worker-count")),
                Integer.parseInt(properties.getProperty("crawler.connection-timeout")),
                Integer.parseInt(properties.getProperty("crawler.request-timeout")),
                properties.getProperty("crawler.user-agent"),
                Boolean.parseBoolean(properties.getProperty("crawler.respect-robots")),
                Integer.parseInt(properties.getProperty("crawler.delay-ms"))
        );
    }
}
