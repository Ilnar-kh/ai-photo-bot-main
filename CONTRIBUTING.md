# Contributing

Thank you for your interest in contributing to **AI Photo Bot**. This document describes the basic workflow and expectations for contributions.

## Development workflow

1. Fork the repository and create a feature branch from `main`.
2. Ensure your development environment uses Java 21 and Maven 3.9 or newer.
3. Run the full build locally before opening a pull request:
   ```bash
   mvn -Pci -DskipITs=false clean verify
   ```
4. Include tests for any new functionality. Unit tests belong in the `core` module; integration tests should rely on Testcontainers (PostgreSQL or WireMock as appropriate).
5. Follow the existing package structure (`com.aiphoto.bot.<module>`) and avoid introducing framework dependencies into the `core` module.
6. Submit a pull request with a clear description. The CI pipeline must be green before merging.

## Coding standards

- Use constructor injection for Spring components.
- Keep transactional boundaries in the `app` module.
- Prefer immutable domain objects (records) and avoid nulls; use `Optional` when absence is meaningful.
- Log structured JSON (already configured via Logback) and use appropriate log levels.
- Database schema changes must be expressed via Flyway migrations (`adapter-persistence/src/main/resources/db/migration`).

## Commit messages

Use concise, present-tense commit messages (e.g., `Add persistence adapter for uploads`).

## Code of conduct

Be respectful and constructive. The maintainers reserve the right to close issues or PRs that violate these guidelines.

Happy coding!
