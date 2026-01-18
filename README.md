# AI Photo Bot

Java 21 / Spring Boot 3.3 project that provides a clean, testable foundation for an AI powered photo generation bot. The codebase follows a hexagonal architecture with a clear separation between the domain (`core`), adapters and the Spring Boot application layer (`app`).

## Modules

- `core` – Domain model, ports and use-cases. No Spring dependencies.
- `adapter-persistence` – PostgreSQL persistence layer using Spring Data JPA and Flyway.
- `adapter-fireworks` – HTTP client for the external AI image generation API.
- `adapter-telegram` – Telegram webhook adapter (stub implementation).
- `adapter-web` – REST API adapters, springdoc OpenAPI configuration and Actuator endpoints.
- `app` – Spring Boot application wiring adapters and exposing transactional facades. Includes Jib container build configuration.

## Getting started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker (for Testcontainers and local Postgres/MinIO services)

### Local services

A minimal `docker compose` snippet to launch the required infrastructure:

```yaml
services:
  postgres:
    image: postgres:16.3-alpine
    environment:
      POSTGRES_DB: aiphoto
      POSTGRES_USER: aiphoto
      POSTGRES_PASSWORD: aiphoto
    ports:
      - "5432:5432"
  minio:
    image: minio/minio:RELEASE.2024-05-10T01-41-38Z
    command: server /data
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio123
    ports:
      - "9000:9000"
      - "9001:9001"
```

Start the services:

```bash
docker compose up -d postgres minio
```

### Build & test

All integration tests run against PostgreSQL via Testcontainers, no H2 is used.

```bash
mvn clean verify
```

For CI parity (explicit profile, enables integration tests):

```bash
mvn -Pci -DskipITs=false clean verify
```

### Run the application

Point the application to your Postgres instance (for example using the `local` profile):

```bash
SPRING_PROFILES_ACTIVE=local \
POSTGRES_HOST=localhost \
POSTGRES_PORT=5432 \
POSTGRES_DB=aiphoto \
POSTGRES_USER=aiphoto \
POSTGRES_PASSWORD=aiphoto \
mvn -pl app spring-boot:run
```

Once started:

- Actuator health: `http://localhost:8080/actuator/health`
- OpenAPI document: `http://localhost:8080/v3/api-docs`

### Container image

The app module is configured with the Jib Maven plugin. To build a local image tarball:

```bash
mvn -pl app jib:buildTar
```

The resulting `app/target/jib-image.tar` can be loaded with `docker load`.

## Testing strategy

- **Unit tests (core)** validate use-case logic using in-memory fakes.
- **Integration tests (adapter-persistence)** run against PostgreSQL via Testcontainers, executing Flyway migrations.
- **Contract tests (adapter-fireworks)** verify the external API client using WireMock.
- **Web tests (adapter-web, adapter-telegram, app)** ensure controllers and Actuator endpoints respond correctly.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidance on project standards and workflow.

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.
