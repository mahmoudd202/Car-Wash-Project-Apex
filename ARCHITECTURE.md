# Architecture

## Overview
- **Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, PostgreSQL 16, Flyway, Spring Security, Swagger.
- **Layers:** Controller (REST API) → Service (transactions & rules) → Repository (Spring Data JPA) → PostgreSQL.

## Data Model
- **Branch:** `id`, `name`, `location`
- **WashingService:** `id`, `name`, `durationMinutes`, `price` (SAR)
- **Booking:** `id`, `branch_id`, `service_id`, `customerAlias`, `startTime` (UTC), `status`, `createdAt`

## Security & Authentication
- Protected by a static API key via `API_KEY` environment variable.
- Requests supply the key in the `X-API-Key` header; validated via constant-time comparison in `ApiKeyFilter`.
- Catalog listings (`/api/branches`, `/api/services`), health check, and Swagger UI are public; all booking operations require authentication.

## Booking Rules & Concurrency
- **Status Transitions:** `PENDING` → `CONFIRMED` / `CANCELLED`; `CONFIRMED` → `COMPLETED` / `CANCELLED`. Completed and cancelled statuses are terminal.
- **Double-Booking Prevention:** PostgreSQL partial unique index on `(branch_id, start_time)` for active (`PENDING`, `CONFIRMED`) bookings. Concurrent conflicting requests return HTTP 409.
- **Race Condition Prevention:** Status updates use a pessimistic write lock (`PESSIMISTIC_WRITE`) on the booking row.

## Database & Testing
- **Schema & Migrations:** Flyway manages schema (`V1`) and seed catalog (`V2`). Hibernate validates the schema.
- **Testing:** Automated integration tests run with JUnit and Testcontainers PostgreSQL.

## Scope Limitations
- Protects exact start-time slots per branch, without duration-overlap scheduling.
- Single trusted client authentication (no customer accounts, JWT, or role management).
- Unpaginated listings and no calendar/opening-hour restrictions.

## Future Improvements

The following improvements are proposed and are not implemented in the current version.

- **Multiple users and signup/login logic:** Replace the shared API key with user signup and login logic instead of the current API key authentication. Add roles such as admin and customer to control access by using JWT Authentication.
- **Observability:** Add centralized application logs, metrics, and monitoring to track errors, API performance, and database health.
- **Backups:** Schedule regular PostgreSQL backups and test restoring the database to prevent data loss.
- **Scaling:** Run multiple API instances behind a load balancer. Keep booking conflict protection in PostgreSQL so concurrent requests cannot reserve the same slot.