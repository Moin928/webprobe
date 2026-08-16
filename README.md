# WebProbe

WebProbe is a distributed e commerce data collection platform built with Spring Boot microservices.

It discovers product pages, collects product information, tracks price changes, and exposes the collected data through REST APIs and a web dashboard.

## Features

Product discovery across supported sources

Product data extraction

Historical price tracking

Availability tracking

Scheduled crawling

Concurrent crawler workers

URL deduplication

Crawl depth and page limits

Rate limiting

robots.txt support

Retry handling

Asynchronous communication with RabbitMQ

Separate database ownership for each service

REST APIs

Docker based local development

## Architecture

WebProbe uses a microservices architecture.

```text
                         Next.js
                            |
                            v
                     API Gateway
                            |
          +-----------------+-----------------+
          |                 |                 |
          v                 v                 v
    Crawl Service     Product Service    Source Service
          |
          v
       RabbitMQ
          |
     +----+----+
     |         |
     v         v
 Crawler 1  Crawler 2
     |         |
     +----+----+
          |
          v
    External Sources
```

The Crawl Service manages crawl jobs and publishes crawl tasks.

Crawler workers consume those tasks, fetch pages, discover new URLs, and extract product information.

Extracted product data is published through RabbitMQ and consumed by the Product Service.

The Source Service manages supported sources and their crawl configuration.

The API Gateway provides a single entry point for the frontend.

Each service owns its own data.

## Services

### API Gateway

Routes external requests to the appropriate backend service.

### Crawl Service

Creates and manages crawl jobs.

It handles crawl status, limits, scheduling, and crawl task creation.

### Crawler Service

Fetches web pages, follows links, applies crawl restrictions, and extracts product data.

Multiple crawler instances can run at the same time.

### Product Service

Stores products, prices, availability, and price history.

It provides APIs for searching and retrieving product information.

### Source Service

Stores information about supported websites and their crawling configuration.

## Data Flow

A crawl starts when a request reaches the API Gateway.

The gateway forwards the request to the Crawl Service.

The Crawl Service creates a crawl job and publishes crawl tasks to RabbitMQ.

Crawler workers consume the tasks and fetch the corresponding pages.

The crawler extracts links and product information from the pages.

New URLs are added back to the crawl queue.

Extracted product data is published through RabbitMQ.

The Product Service consumes the data and stores it in its database.

The frontend retrieves the processed information through the API Gateway.

## Tech Stack

### Backend

Java

Spring Boot

Spring Cloud Gateway

Spring Data JPA

Hibernate

PostgreSQL

RabbitMQ

Jsoup

Maven

JUnit

Mockito

### Frontend

Next.js

TypeScript

Tailwind CSS

### Infrastructure

Docker

Docker Compose

## Project Structure

```text
webprobe
|
+-- gateway
|
+-- crawl-service
|
+-- crawler-service
|
+-- product-service
|
+-- source-service
|
+-- frontend
|
+-- docker-compose.yml
|
+-- README.md
```

Each backend service is independently deployable and owns its application logic.

## Crawling

WebProbe separates crawling from data extraction.

The crawler is responsible for discovering and fetching pages.

The scraper is responsible for extracting structured product information from the fetched HTML.

This allows the crawling system to remain independent from the structure of individual websites.

Source specific extraction logic is implemented through separate product extractors.

## Concurrency

Crawler workers consume tasks asynchronously from RabbitMQ.

Multiple crawler instances can process different URLs at the same time.

Concurrency is bounded to prevent uncontrolled resource usage and excessive requests to external sources.

## Data Storage

Each service owns its data.

The initial setup uses PostgreSQL with separate databases for the services.

```text
crawl_db
product_db
source_db
```

Services do not directly access another service's database.

Communication between services happens through APIs or asynchronous messages.

## Crawl Safety

WebProbe is designed to crawl responsibly.

The crawler supports robots.txt checks, configurable rate limits, crawl depth limits, page limits, request timeouts, and retry handling.

Only permitted sources should be configured for crawling.

## Running Locally

Clone the repository and start the required services using Docker Compose.

```bash
docker compose up --build
```

The individual Spring Boot services can also be run directly during development.

Environment specific configuration is stored outside the source code.

## Development

The project is being developed incrementally.

The crawler is first implemented as a working single worker pipeline.

Concurrency is introduced after the basic crawling flow is stable.

The system is then expanded with RabbitMQ, multiple crawler workers, product extraction, persistence, scheduling, APIs, and the frontend.

## Future Improvements

Redis based distributed rate limiting

Dead letter queues

Improved crawl scheduling

More source extractors

Product search

Price drop notifications

Authentication

Monitoring and metrics

Cloud deployment

Horizontal scaling of crawler workers

## Goal

The goal of WebProbe is to build a practical distributed system that demonstrates web crawling, data extraction, asynchronous processing, microservices architecture, concurrency, persistence, and scalable backend design.
