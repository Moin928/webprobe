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

        this.userAgent = userAgent;

        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public boolean isAllowed(String url)
            throws IOException, InterruptedException, URISyntaxException {

        URI uri = URI.create(url);

        URI robotsUri = new URI(
                uri.getScheme(),
                uri.getAuthority(),
                "/robots.txt",
                null,
                null
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(robotsUri)
                .header("User-Agent", userAgent)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() == 404) {
            return true;
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return false;
        }

        return isPathAllowed(response.body(), uri.getPath());
    }

    private boolean isPathAllowed(String robotsTxt, String path) {

        boolean applies = false;

        for (String line : robotsTxt.split("\\R")) {

            line = line.trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String lower = line.toLowerCase();

            if (lower.startsWith("user-agent:")) {

                String agent = line.substring(
                        line.indexOf(":") + 1
                ).trim();

                applies = agent.equals("*")
                        || agent.equalsIgnoreCase(userAgent);

            } else if (applies && lower.startsWith("disallow:")) {

                String disallowedPath = line.substring(
                        line.indexOf(":") + 1
                ).trim();

                if (!disallowedPath.isEmpty()
                        && path.startsWith(disallowedPath)) {
                    return false;
                }
            }
        }

        return true;
    }
}