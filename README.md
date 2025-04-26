testProject

A Spring Boot WebFlux application that processes GeoTIFF files, stores metadata in an R2DBC-enabled H2 database, and provides RESTful APIs for querying metadata and logs.

Features

Reactive: Built with Spring WebFlux and R2DBC for non-blocking I/O

H2 Database: In-memory or file-based H2 with auto schema initialization

GeoTIFF Support: Extracts and stores metadata (dimensions, CRS, pixel size, etc.)

Logging Table: Tracks raw file uploads and checksums

REST API: Endpoints to retrieve metadata and raw upload logs

Docker Ready: Simple to containerize

Prerequisites

Java 23+

Gradle

Git

(Optional for file-based H2)

H2 CLI or GUI client

Getting Started

Clone the repository

git clone https://github.com/<your-username>/testProject.git
cd testProject

Build the application

./gradlew clean bootJar

Run the application

In development (embedded H2 file):

java -jar build/libs/testProject-0.0.1-SNAPSHOT.jar \
  --spring.r2dbc.url=r2dbc:h2:file:./data/testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE \
  --server.port=8080

In-memory (for tests/demo):

java -jar build/libs/testProject-0.0.1-SNAPSHOT.jar \
  --spring.r2dbc.url=r2dbc:h2:mem:///testdb \
  --server.port=8080

Configuration

Application settings are in src/main/resources/application.yml:

spring:
  r2dbc:
    url: r2dbc:h2:mem:///testdb
  main:
    allow-bean-definition-overriding: true
server:
  port: 8080

Customize profiles with application-dev.yml, application-prod.yml, etc.

API Endpoints

Get all GeoTIFF metadata

GET /metadata
Response: 200 OK
[
  { "id": 1, "filename": "...", /* other fields */ },
  { "id": 2, "filename": "..." }
]

Get all raw upload logs

GET /logs
Response: 200 OK
[
  { "id": 1, "originalFilename": "...", "checksum": "...", /* ... */ }
]

Example using curl

curl -X GET http://localhost:8080/metadata

Database Schema

Initialized automatically by DbInitializer on startup:

CREATE TABLE geotiff_metadata (
  id IDENTITY PRIMARY KEY,
  filename VARCHAR(255),
  width INT,
  height INT,
  coordinate_system VARCHAR(255),
  origin_x DOUBLE,
  origin_y DOUBLE,
  pixel_size_x DOUBLE,
  pixel_size_y DOUBLE,
  band_count INT,
  color_interpretation VARCHAR(255),
  compression VARCHAR(255),
  upload_count INT DEFAULT 1,
  last_upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE raw_geotiff_log (
  id IDENTITY PRIMARY KEY,
  original_filename VARCHAR(255),
  checksum VARCHAR(255),
  upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  file_size BIGINT,
  metadata_id BIGINT
);

Packaging & Deployment

Jar: ./gradlew bootJar → build/libs/*.jar

Docker (optional):

./gradlew bootBuildImage --imageName testproject:latest

Contributing

Fork the repository

Create a feature branch (git checkout -b feature/foo)

Commit your changes (git commit -am 'Add foo')

Push to the branch (git push origin feature/foo)

Open a Pull Request

License

This project is licensed under the MIT License. See LICENSE for details.

Made with ❤️ by Inseong Bang

