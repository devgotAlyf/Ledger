# Ledger

Spring Boot transaction auditing service with event sourcing, Kafka-based async processing, Redis idempotency, PostgreSQL event storage, and AI-powered audit decisions.

## Local run

Run the app in local mode with the in-repo Maven install:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\.tools\apache-maven-3.9.15\bin\mvn.cmd spring-boot:run
```

Open:

- `http://localhost:8080/`
- `http://localhost:8080/healthz`
- `http://localhost:8080/swagger-ui.html`

## Local test flow

Create a transaction:

```json
{
  "userId": "user-1",
  "amount": 1200.50,
  "merchant": "Amazon",
  "timestamp": "2026-04-24T10:00:00Z"
}
```

Then test:

- `GET /transactions/user-1/history`
- `GET /transactions/{transactionId}/replay?at=2026-04-24T10:30:00Z`

In the local profile:

- moderate amounts are typically `APPROVED`
- much larger amounts for the same user are typically `FLAGGED`

## Render deploy

This repo now includes:

- `Dockerfile`
- `render.yaml`

Blueprint-managed resources:

- `ledger-api` web service
- `ledger-db` Postgres
- `ledger-cache` Render Key Value

The app accepts Render-style connection URLs:

- `DATABASE_URL=postgresql://...`
- `REDIS_URL=redis://...`

These are translated automatically into Spring-compatible settings at startup.
