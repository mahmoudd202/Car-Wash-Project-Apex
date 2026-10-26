# AI Usage Disclosure

## Tools used

OpenAI Codex, Antigravity and ChatGPT were used to assist with reviewing, testing, and documenting this project.

## How AI was used

Antigravity and ChatGPT were used to review the implementatiom and identify the necessary requirements.

## Testing

Codex previously executed Maven tests using PostgreSQL and Docker. Evidence from those runs is stored in the `evidence/` directory.

## Personal review

- Files reviewed and understood: Controllers, services, repositories, entities (`Booking`, `BookingStatus`, `WashingService`, `Branch`), security (`ApiKeyFilter`, `SecurityConfig`), exception handling, Flyway migrations (`V1`, `V2`), and `BookingIntegrationTest`.
- Commands personally executed: `mvn clean test`, `mvn clean package`, `docker compose up -d --build`, `docker compose ps`, and `curl` health check.
- Test results: All 18 tests passed (`0 failures, 0 errors, BUILD SUCCESS`).
- API endpoints manually tested: `GET /api/branches`, `GET /api/services`, `POST /api/bookings`, `GET /api/bookings`, `PATCH /api/bookings/{id}/status`, `GET /actuator/health`, and Swagger UI.
- Changes personally made: Configured Lombok annotation processor in `pom.xml`, resolved Flyway migration checksum in `V1__schema.sql`, configured `compose.yaml` port mappings, excluded `/actuator` root in `application.yaml`, and simplified documentation.
- Remaining limitations or questions: Capacity rule only guards exact start-time collisions (not duration overlaps); uses a single shared API key rather than customer auth; booking lists are unpaginated.

## Responsibility

I understand that AI-generated code must be reviewed and verified before submission. I will be prepared to explain the application's structure, authentication, database design, booking rules, and concurrency handling during the technical assessment.