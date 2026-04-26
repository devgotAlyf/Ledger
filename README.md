# Ledger

Spring Boot transaction auditing service with event sourcing, Kafka-based async processing, Redis idempotency, PostgreSQL event storage, and AI-powered audit decisions.
Run the app in local mode with the in-repo Maven install:

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

These are translated automatically into Spring-compatible settings at startup.
