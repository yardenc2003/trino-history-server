<h1 align="center"> Trino Query History Server</h1>

<p align="center">
    <b>A backend service for persisting and serving query history from Trino coordinators</b>
</p>

## Overview

The **Trino History Server** is a Spring Boot-based backend service that collects and stores query data from Trino coordinators.

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
This design decouples data collection from Trino's runtime, enabling long-term query retention and historical browsing via the [custom Trino UI frontend](https://github.com/yardenc2003/trino/tree/trino-history-server-475.1).

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

## Configuration

The History Server can be configured via `application.properties` with the following key settings:

```properties
# Server settings
server.port=8080                          # Port on which the History Server runs

# Environment info
global.environment=production             # Server environment name (e.g., test, prod)

# Trino authentication for coordinator requests
trino.auth.username=your-trino-username   # Username used when fetching query data from Trino coordinators
trino.auth.password=your-trino-password   # Password used when fetching query data from Trino coordinators

# Storage backend type (choose one)
storage.type=jdbc                         # Storage backend type: 'jdbc', 'filesystem', or 's3'

# JDBC storage-specific settings (for 'jdbc' backend)
storage.jdbc.dialect=postgresql  # SQL dialect for JDBC (e.g., postgresql, mysql)
storage.jdbc.url=jdbc:postgresql://db.example.com:5432/trino  # JDBC connection URL
storage.jdbc.username=your-db-username   # Database username
storage.jdbc.password=your-db-password   # Database password

# Filesystem storage-specific settings (for 'filesystem' backend)
storage.filesystem.query-dir=/var/data/trino-history/query  # Directory path to store query JSON files

# S3 storage-specific settings (for 's3' backend)
storage.s3.query-dir=query           # Directory (prefix) in the S3 bucket to store query files
storage.s3.storage-class=STANDARD    # S3 storage class (e.g., STANDARD, STANDARD_IA)
storage.s3.bucket=history            # S3 bucket name
storage.s3.region=us-east-1          # S3 bucket region
storage.s3.endpoint=https://s3.example.com   # S3 endpoint
storage.s3.access-key=your-access-key     # S3 access key
storage.s3.secret-key=your-secret-key     # S3 secret key
storage.s3.path-style-access=true         # Use path-style access

# Storage-retry settings (for all storage implementations)
storage.retry.max-retries=3       # Maximum retry attempts for failed storage operations
storage.retry.backoff-millis=500  # Time to wait (in milliseconds) between retry attempts

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

