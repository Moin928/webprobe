# WebProbe

A multithreaded web crawler built entirely with Java.

WebProbe starts from a set of seed URLs, fetches web pages, extracts links, and continues exploring newly discovered pages. It keeps track of visited URLs to prevent duplicate requests and uses multiple threads to crawl different pages concurrently.

The project is built without Spring Boot or other application frameworks. The goal is to understand how a web crawler actually works by implementing the core components directly with Java.

## Features

Multithreaded crawling

URL discovery and extraction

Duplicate URL detection

Concurrent crawling queue

HTTP page fetching

HTML parsing

Configurable crawl limits

Graceful handling of invalid or unreachable URLs

## Tech Stack

Java

Java HTTP Client

Java Concurrency API

Java Collections Framework

HTML Parser

## How It Works

WebProbe receives one or more starting URLs.

Each URL is placed into the crawling queue.

A worker fetches the page from the web.

The HTML content is parsed to find links.

New links are added to the queue.

The crawler checks whether a URL has already been visited before processing it.

Multiple workers perform these operations simultaneously, allowing WebProbe to explore several pages at once.

## Architecture

WebProbe separates crawling responsibilities into independent components for URL management, page fetching, HTML parsing, crawling logic, and concurrency.

The result is a lightweight exploration engine that gradually builds its own map of the web without relying on a framework to hide the underlying mechanics.

## Running the Project

Clone the repository and open it in your Java IDE.

Configure the seed URLs and crawler settings.

Build the project and run the main application.

WebProbe will begin from the configured seeds and explore discovered pages according to the configured limits.

## Project Status

Currently under development.

The project is being built incrementally with a focus on understanding the underlying concepts while keeping the implementation lightweight and entirely Java based.

## License

MIT
