package com.webprobe.robots;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RobotsTxtChecker {

    private final HttpClient httpClient;
    private final String userAgent;

    public RobotsTxtChecker(String userAgent) {

        // this client is shared so we dont create a new connection setup for every check
        this.userAgent = userAgent;

        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    public boolean isAllowed(String url)
            throws IOException, InterruptedException, URISyntaxException {

        // get the website origin so robots is checked from the correct host
        URI uri = URI.create(url);

        URI robotsUri = new URI(
            uri.getScheme(),
            uri.getAuthority(),
            "/robots.txt",
            null,
            null
        );

        // ask the server for its robots rules using our crawler identity
        HttpRequest request =
            HttpRequest.newBuilder()
                .uri(robotsUri)
                .header("User-Agent", userAgent)
                .GET()
                .build();

        HttpResponse<String> response =
            httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );

        // if there is no robots file then there are no rules to follow
        if (response.statusCode() == 404) {
            return true;
        }

        // dont crawl when we cannot reliably retrieve the robots rules
        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            return false;
        }

        // check the requested path against the rules we received
        return isPathAllowed(
            response.body(),
            uri.getPath()
        );
    }

    private boolean isPathAllowed(
            String robotsTxt,
            String path) {

        boolean applies = false;

        // robots.txt is line based so process each rule one at a time
        for (String line : robotsTxt.split("\\R")) {

            line = line.trim();

            // empty lines and comments dont affect the rules
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String lower = line.toLowerCase();

            // find which user agent the following rules belong to
            if (lower.startsWith("user-agent:")) {

                String agent =
                    line.substring(
                        line.indexOf(":") + 1
                    ).trim();

                applies =
                    agent.equals("*")
                    || agent.equalsIgnoreCase(userAgent);

            // only apply disallow rules belonging to our user agent
            } else if (applies
                    && lower.startsWith("disallow:")) {

                String disallowedPath =
                    line.substring(
                        line.indexOf(":") + 1
                    ).trim();

                // an empty disallow means nothing is blocked
                if (!disallowedPath.isEmpty()
                        && path.startsWith(disallowedPath)) {

                    return false;
                }
            }
        }

        // nothing matched so this path is allowed
        return true;
    }
}