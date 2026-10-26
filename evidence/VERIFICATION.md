# Verification Evidence

## Automated Tests

Run tests using:

```bash
mvn clean verify
```

Tests use JUnit 5, Testcontainers, and PostgreSQL 16.

- **18 tests:** 13 integration tests, 4 status tests, and 1 API-key test.
- **Coverage:** Booking creation, validation, missing resources, authentication, filtering, status transitions, duplicate bookings, and concurrency.
- **Results:** See `maven-tests.log` for the actual test results.

## Docker & API Verification

Start the application:

```bash
docker compose up -d --build
```

- **Database:** PostgreSQL 16 with persistent Docker volume.
- **API:** Spring Boot running on Java 21.
- **Health:** `http://localhost:8080/actuator/health`
- **Swagger:** `http://localhost:8080/swagger-ui/index.html`
- **API examples:** See `api-samples.json`.

Docker Compose starts the database before the API but does not configure a database health check.

## Booking Conflict Protection

PostgreSQL prevents duplicate active bookings for the same branch and start time.

Tests verify that conflicting bookings return **HTTP 409**, including concurrent requests.

## Evidence Files

- `maven-tests.log` — Automated test output.
- `api-samples.json` — Example API requests and responses.
- `VERIFICATION.md` — Verification summary.