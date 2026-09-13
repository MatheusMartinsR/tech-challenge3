# Phase 3 issue compliance

| Issue | Requirement | Implementation/evidence | Status |
|---:|---|---|---|
| 1 | JWT authentication | `/auth/register`, `/auth/login`, JWT service/filter, auth unit tests | Complete |
| 2 | Role authorization | Method rules plus integration tests for `MEDICO`, `ENFERMEIRO`, `PACIENTE` and ownership | Complete |
| 3 | RabbitMQ integration | JSON event producer/consumer, shared contract, retry and Testcontainers tests | Complete |
| 4 | Appointment CRUD | REST CRUD, PostgreSQL/Flyway, future/participant/conflict rules, events | Complete |
| 5 | GraphQL | Required types and three queries with role/ownership protection and tests | Complete |
| 6 | Medical history | Terminal states retained, `historicoCompleto`, Flyway schema and demo history seed | Complete |
| 7 | Notifications | Consumer, reminder adapter, persistence, retry/DLQ and unit/integration tests | Complete |
| 8 | Containers | Multi-stage Dockerfiles, Compose services, health checks, variables and isolated network | Complete |
| 9 | Tests and CI | JUnit/Mockito, Testcontainers, security tests, JaCoCo 60% gate, GitHub Actions | Complete |
| 10 | Authentication Postman collection | Register/login, variables, automatic tokens, response examples for all roles | Complete |
| 11 | Scheduling Postman collection | CRUD, GraphQL, Bearer tokens, allowed and denied assertions | Complete |
| 12 | End-to-end flows | Three automated Newman flows and evidence procedure | Ready; evidence run requires Docker |
| 13 | Architecture documentation | Context/components, sequence, ADRs and ER Mermaid diagrams | Complete |
| 14 | API documentation | Endpoints, roles, GraphQL, examples and error catalog | Complete |
| 15 | README | Setup, architecture summary, configuration, security, tests and documentation links | Complete |

The only non-executed acceptance activity is the real Docker/Newman evidence run for Issue #12. The collection is implemented and JSON-validated, but this workstation has no Docker executable. See `E2E-TESTS.md` for the exact command and evidence location.
