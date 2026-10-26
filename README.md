# Car Wash Booking API

A simple car-wash booking REST API built using Java 21, Spring Boot, Spring Data JPA, PostgreSQL, Flyway, and Spring Security.

## Features

- List branches and washing services
- Create and retrieve bookings
- Filter bookings by branch, status, and date
- Update booking status
- Prevent double bookings
- API-key authentication
- Swagger documentation
- Automated tests

## Running the project

Requirements: Java 21, Maven, and Docker Desktop.

1. Create a `.env` file based on `.env.example`.
2. Set `API_KEY` and `POSTGRES_PASSWORD`.
3. Run:

```bash
mvn clean verify
docker compose up -d --build
```

The API runs at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

Health check: `http://localhost:8080/actuator/health`

## API endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/branches` | List branches |
| GET | `/api/services` | List services |
| POST | `/api/bookings` | Create booking |
| GET | `/api/bookings/{id}` | Get booking |
| GET | `/api/bookings` | List/filter bookings |
| PATCH | `/api/bookings/{id}/status` | Update status |

All booking endpoints require an `X-API-Key` header. Branch and service listing endpoints are public.

## Example booking request

POST `/api/bookings`

```json
{
  "branchId": 1,
  "serviceId": 1,
  "customerAlias": "demo",
  "startTime": "2030-01-15T10:00:00Z"
}
```

A new booking starts with `PENDING` status.

## Booking statuses

- `PENDING` → `CONFIRMED` or `CANCELLED`
- `CONFIRMED` → `COMPLETED` or `CANCELLED`
- `COMPLETED` and `CANCELLED` cannot be changed

## Double-booking prevention

PostgreSQL uses a unique index to prevent multiple active bookings at the same branch and exact start time.

Only `PENDING` and `CONFIRMED` bookings occupy a slot. Cancelling or completing a booking frees it.

This rule does not check overlapping service durations.

## Database

Flyway runs two SQL migrations:

- `V1__schema.sql` creates the tables and constraints.
- `V2__seed_catalog.sql` inserts sample branches and services.

Hibernate validates the schema instead of creating it.

## Testing

Run:

```bash
mvn clean verify
```

Docker Desktop must be running because integration tests use PostgreSQL through Testcontainers.

## Assumptions

- All timestamps are handled in UTC.
- Service prices are in SAR.
- Only one trusted API key is used; there are no user accounts.
- Sample data is synthetic.
- No opening-hour restrictions or duration-overlap scheduling are implemented.

See `ARCHITECTURE.md` for the technical design and `AI_USAGE.md` for the AI assistance disclosure.

## Sample curl requests

### List branches (public)

```bash
curl http://localhost:8080/api/branches
```

### List services (public)

```bash
curl http://localhost:8080/api/services
```

### Create a booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{"branchId":1,"serviceId":1,"customerAlias":"demo","startTime":"2030-01-15T10:00:00Z"}'
```

Replace `YOUR_API_KEY` with the value configured in your `.env` file.

### Get booking details

```bash
curl http://localhost:8080/api/bookings/1 \
  -H "X-API-Key: YOUR_API_KEY"
```

### Filter bookings by date

```bash
curl "http://localhost:8080/api/bookings?date=2030-01-15" \
  -H "X-API-Key: YOUR_API_KEY"
```

### Stop the application

```bash
docker compose down
```

PostgreSQL data remains stored in the Docker volume.

### Seed data

Flyway automatically inserts two sample branches and three washing services during the initial database setup using `V2__seed_catalog.sql`.