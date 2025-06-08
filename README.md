<h1 align="center"> Trino Query History Server</h1>

<p align="center">
    <b>Trino is a fast distributed SQL query engine for big data analytics.</b>
</p>

## Overview

The **Trino History Server**, a Spring Boot-based backend service that collects and stores query data from Trino coordinators.

When a Trino coordinator emits a [`QueryCompletedEvent`](https://github.com/trinodb/trino/blob/master/core/trino-spi/src/main/java/io/trino/spi/eventlistener/QueryCompletedEvent.java),
it can be sent to the History Server via the [HTTP Event Listener](https://trino.io/docs/current/admin/event-listeners-http.html) mechanism.

The History Server exposes the following endpoint for this purpose:

```
POST /api/v1/query/{queryId}
```

This endpoint expects:
* A `QueryCompletedEvent` JSON payload
* An `X-Trino-Coordinator-Url` custom HTTP header identifying the source coordinator

Upon receiving the event, the server uses the coordinator URL to fetch both the query JSON representations, 
and persists the document in a configurable storage.
This design decouples data collection from Trino's runtime, enabling long-term query retention and UI history browsing.

## Architecture

### Query Flow 

```text
       ┌────────────────────┐
       │   Trino Coordinator│
       │(via Event Listener)│
       └────────┬───────────┘
                │ POST /api/v1/query/{queryId}
                ▼
      ┌────────────────────────┐
      │  History Server (Spring│
      │       Boot App)        │
      └────────┬───────────────┘
               │
               ├─ Fetch Query JSON
               │  from the coordinator using
               │  `X-Trino-Coordinator-Url` header
               │
               └─ Persist files to configurable storage
```

### User Access Flow

```text
        ┌─────────────┐
        │     User    │
        └─────┬───────┘
              │
              ▼
    ┌────────────────────────┐
    │ Forked Trino Web UI    │
    └────────┬───────────────┘
             │
             │ Requests query details via REST API
             │ to the History Server backend
             ▼
    ┌────────────────────────┐
    │ History Server (Spring │
    │ Boot App)              │
    └────────────────────────┘

```

## Development

This project is developed using Java 23 and the Spring Boot framework, with Maven as the build tool (via the Maven Wrapper).

### Prerequisites

* Java 23+

### Build the Project

```bash
./mvnw clean install
```

### Run Locally

```bash
./mvnw spring-boot:run
```

By default, the History Server starts on port `8080`. Configuration — such as storage settings or Trino user authentication details — 
can be adjusted via `application.yaml` or environment variables.

