<h1 align="center">Trino Query History Server</h1>
<p align="center">
    <img alt="Trino Logo" src="assets/trino-history.png" /></a>
</p>
<p align="center">
    <b>A backend service for persisting and serving query history from Trino coordinators.</b>
</p>

## Overview

The **Trino History Server** is a Spring Boot-based backend service that collects and stores query data from Trino coordinators.

When a Trino coordinator emits a [`QueryCompletedEvent`](https://trino.io/docs/475/admin/event-listeners-http.html#configuration-properties),
it can be sent to the History Server via the [HTTP Event Listener](https://trino.io/docs/475/admin/event-listeners-http.html) mechanism.

The History Server exposes the following endpoint for this purpose:

```
POST /api/v1/query/{queryId}
```

This endpoint expects:
* A [`QueryCompletedEvent`](https://trino.io/docs/475/admin/event-listeners-http.html#configuration-properties) JSON payload
* An `X-Trino-Coordinator-Url` [custom HTTP header](https://trino.io/docs/475/admin/event-listeners-http.html#custom-http-headers) identifying the source coordinator

Upon receiving the event, the server uses the coordinator URL to fetch the query JSON representation, and persists the document in a configurable storage.
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
              │ queryId
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
# Note: This must be an admin user, as it is used to fetch all query data (across all users) from the coordinators
trino.auth.username=your-trino-username   # Username used when fetching query data from Trino coordinators
trino.auth.password=your-trino-password   # Password used when fetching query data from Trino coordinators

# Storage-retry settings (for all storage implementations)
storage.retry.max-retries=3       # Maximum retry attempts for failed storage operations
storage.retry.backoff-millis=500  # Time to wait (in milliseconds) between retry attempts

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

```

## Development

This project is developed using Java 24 and the Spring Boot framework, with Maven as the build tool (via the Maven Wrapper).

### Prerequisites

* Java 24+

### Build the Project

From the **root directory** of the project, run:

```bash
./mvnw clean install
```

### Run Locally

```bash
./mvnw spring-boot:run
```

By default, the History Server starts on port `8080`. Configuration — such as storage settings or Trino user authentication details — 
can be adjusted via `application.yaml` or environment variables.

## Building the Docker Image

To build a Docker image for the History Server backend, run:

```bash
./docker/build.sh [version] [arch]
```

* `[version]` (optional): The image version tag (e.g. `1.0.1`). Defaults to `latest` if not specified.
* `[arch]` (optional): The target architecture (e.g. `amd64`, `arm64`). Defaults to `amd64`.

Examples:

```bash
./docker/build.sh                # Builds version "latest" for amd64
```

```bash
./docker/build.sh 1.0.0         # Builds version 1.0.0 for amd64
```

```bash
./docker/build.sh 1.0.0 arm64    # Builds version 1.0.0 for arm64
```

This produces a Docker image tagged as:

```text
trino-history-backend:<version>-<arch>
```
