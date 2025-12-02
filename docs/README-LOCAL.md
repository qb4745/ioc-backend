# README - Run locally (ioc-backend)

This file contains minimal steps to build and run the backend locally for development and basic testing.

## Prerequisites
- Java 21 JDK
- Maven 3.8+
- Docker & docker-compose (optional, recommended for local Postgres)
- (Optional) psql cli

## Recommended local stack
You can use the project's `docker-compose.yml` to bring up a local Postgres (or other services) if provided:

```bash
# from repo root
docker-compose up -d postgres
```

## Environment variables (minimum required)
Create a `.env` locally or export variables in your shell. Do NOT commit secrets.

- SUPABASE_DB_PASSWORD
- SUPABASE_SERVICE_ROLE_KEY
- SUPABASE_URL (e.g. https://your-supabase-url)
- METABASE_SECRET_KEY
- SPRING_PROFILES_ACTIVE=local

Example:

```bash
export SUPABASE_DB_PASSWORD=changeme
export SUPABASE_SERVICE_ROLE_KEY=changeme
export SUPABASE_URL=https://local-supabase.test
export METABASE_SECRET_KEY=changeme
export SPRING_PROFILES_ACTIVE=local
```

## Build and run

```bash
# build
mvn clean package -DskipTests

# run the jar (adjust path if necessary)
java -jar target/*-SNAPSHOT.jar --spring.profiles.active=local
```

## Run tests

```bash
# unit + integration (testcontainers may require Docker)
mvn test
```

## Generate OpenAPI (if springdoc configured)

If the project includes springdoc-openapi, run the app and visit http://localhost:8080/v3/api-docs to fetch `openapi.json`.

## Notes
- This README is minimal; for CI/CD secrets use your pipeline's secrets store (GitHub Actions secrets, Vault, AWS Parameter Store, etc.).
- For DB migrations, consider enabling Flyway (see docs/TODO in project-summary).

