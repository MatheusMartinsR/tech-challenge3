# Hospital Appointment Platform — Tech Challenge Phase 3

Two Spring Boot microservices implement secure hospital appointment scheduling and asynchronous patient notifications. The project follows hexagonal architecture, keeps one PostgreSQL database per service, and exchanges versioned JSON events through RabbitMQ.

## Services and stack

| Module | Responsibility | Port |
|---|---|---:|
| `agendamento-service` | JWT authentication, role authorization, REST scheduling API and GraphQL history queries | 8080 |
| `notificacao-service` | Consumes appointment events, stores delivery attempts, retries failures and routes exhausted messages to a DLQ | 8081 |
| `common` | Shared RabbitMQ event contract | — |

Java 17, Spring Boot, Spring Security, Spring Data JPA, Spring GraphQL, Flyway, PostgreSQL, RabbitMQ, Docker Compose, JUnit 5, Mockito, Testcontainers and JaCoCo are used.

Internal source code is written in English. The Portuguese public contract required by the challenge is intentionally preserved: `/consultas`, GraphQL fields, role values (`MEDICO`, `ENFERMEIRO`, `PACIENTE`), database columns and event names.

## Run the complete stack

Requirements: Docker Desktop with Compose v2. Copy `.env.example` to `.env`, replace the JWT secret outside local development, then run:

```bash
docker compose up --build
```

Health endpoints:

- Scheduling: `http://localhost:8080/actuator/health`
- Notification: `http://localhost:8081/actuator/health`
- RabbitMQ management: `http://localhost:15672`
- GraphiQL: `http://localhost:8080/graphiql`

Flyway creates both schemas. Docker Compose also enables scheduling demonstration data: `medico.demo@hospital.local`, `enfermeiro.demo@hospital.local`, and `paciente.demo@hospital.local`, all with password `senha123`.

Stop the stack with `docker compose down`. Use `docker compose down -v` only when you deliberately want to erase local database volumes.

## Authentication and authorization

Register with `POST /auth/register`, sign in with `POST /auth/login`, and send the returned token as `Authorization: Bearer <token>`. Both responses include the authenticated user ID and profile data, allowing clients to reference the patient or doctor without direct database access.

| Operation | Allowed roles |
|---|---|
| Create appointment | `MEDICO`, `ENFERMEIRO` |
| Edit appointment | `MEDICO`, `ENFERMEIRO` |
| Cancel appointment | `MEDICO`, `ENFERMEIRO` |
| List/read appointments | all authenticated roles; a patient only sees their own |
| GraphQL queries | all authenticated roles; patient ownership is enforced |

Creation and editing reject past dates, invalid patient/doctor roles, cancelled appointments, and a date/time conflict for either participant.

## Tests and quality gate

```bash
./mvnw verify
```

On Windows use `mvnw.cmd verify`. Unit and Spring integration tests run locally; PostgreSQL/RabbitMQ Testcontainers tests run when Docker is available. JaCoCo fails the build when business use cases fall below 60% line coverage. GitHub Actions runs the same command and uploads test and coverage reports.

Run the automated end-to-end Postman scenarios with:

```bash
npx newman run postman-collections/e2e-collection.postman_collection.json \
  -e postman-collections/environment.postman_environment.json
```

## Documentation

- [REST and GraphQL API](docs/API.md)
- [Architecture, diagrams, ADRs and data model](docs/ARCHITECTURE.md)
- [End-to-end test procedure and evidence](docs/E2E-TESTS.md)
- [Issue-by-issue compliance matrix](docs/COMPLIANCE.md)
- Postman collections in `postman-collections/`

## Configuration

Production credentials must be supplied through environment variables. The main variables are `SPRING_DATASOURCE_*`, `SPRING_RABBITMQ_*`, and `JWT_SECRET`. Application defaults exist only for local development; never use the demonstration secret or passwords in production.
