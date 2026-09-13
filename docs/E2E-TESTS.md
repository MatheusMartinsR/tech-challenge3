# End-to-end tests

The Postman/Newman collection covers the three flows required by Issue #12 against the complete Docker Compose stack.

## Run and capture evidence

```bash
docker compose up --build -d
npx newman run postman-collections/e2e-collection.postman_collection.json \
  -e postman-collections/environment.postman_environment.json \
  --reporters cli,junit --reporter-junit-export docs/e2e-results/newman.xml
docker compose down
```

Wait until both `/actuator/health` endpoints are `UP` before running Newman. The collection creates unique emails and future dates, captures JWTs and IDs automatically, and must be executed in folder order.

## Covered flows

1. Register patient → login nurse → create appointment → confirm the creation event was acknowledged on `consultas.notification.queue`.
2. Login as two patients → prove each patient only lists their own appointments and receives `403` for another patient's appointment.
3. Login doctor → edit the appointment → confirm the update event was acknowledged.

RabbitMQ's management API is the observable notification boundary because the notification service intentionally has no business REST endpoint. Persistence and listener behavior are independently covered by integration tests.

## Resolved finding

The original E2E automation could not discover a newly registered user's numeric ID. Authentication responses now return `userId`, name, email and role, so the collection can schedule the new patient without querying PostgreSQL. This was fixed together with authentication integration coverage.

## Evidence policy

Do not mark a flow as passed without a real Newman/Collection Runner output. Commit the generated `docs/e2e-results/newman.xml` or attach Runner screenshots to the issue after execution. This environment has no Docker daemon, so the evidence file must be produced on a Docker-enabled workstation or by CI.

| Flow | Automated assertions | Local status |
|---|---|---|
| Register/login/create/notify | HTTP status, captured IDs/token, appointment payload, RabbitMQ ack counter | Ready; Docker execution required |
| Patient data isolation | Empty/owned list and forbidden cross-patient read | Ready; Docker execution required |
| Doctor edit/update event | HTTP status, changed payload, RabbitMQ ack counter | Ready; Docker execution required |
