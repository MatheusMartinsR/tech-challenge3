# API reference

Base URL: `http://localhost:8080`. Except for registration and login, send `Authorization: Bearer <token>`.

## Authentication

### `POST /auth/register`

```json
{"nome":"Ana Silva","email":"ana@example.com","senha":"Strong@123","role":"PACIENTE"}
```

Returns `201` with `token`, `tokenType`, `userId`, `name`, `email`, and `role`. Supported roles: `MEDICO`, `ENFERMEIRO`, `PACIENTE`.

### `POST /auth/login`

```json
{"email":"ana@example.com","senha":"Strong@123"}
```

Returns the same authentication response with status `200`.

## Appointment REST API

| Method and path | Role | Result |
|---|---|---|
| `POST /consultas` | `MEDICO`, `ENFERMEIRO` | Creates and publishes an appointment; `201` |
| `PUT /consultas/{id}` | `MEDICO`, `ENFERMEIRO` | Edits and publishes an appointment; `200` |
| `GET /consultas` | all roles | Staff see all; patients see only their own; `200` |
| `GET /consultas/{id}` | all roles | Patient ownership enforced; `200` |
| `DELETE /consultas/{id}` | `MEDICO`, `ENFERMEIRO` | Logical cancellation; `200` |

Create/update body:

```json
{
  "pacienteId": 3,
  "medicoId": 1,
  "dataHora": "2026-12-10T14:30:00",
  "observacoes": "Routine follow-up"
}
```

Successful responses contain `id`, `pacienteId`, `pacienteNome`, `medicoId`, `medicoNome`, `dataHora`, `status`, and `observacoes`. Valid status values are `AGENDADA`, `REALIZADA`, and `CANCELADA`.

## GraphQL

Endpoint: `POST /graphql`. GraphiQL is available at `/graphiql` in the local Docker profile. All queries require JWT authentication and enforce patient ownership.

```graphql
query PatientSchedule($patientId: ID!, $status: StatusConsulta) {
  consultasPorPaciente(pacienteId: $patientId, status: $status) {
    id dataHora status observacoes
    paciente { id nome }
    medico { id nome }
  }
}
```

```graphql
query Upcoming($patientId: ID!) {
  consultasFuturas(pacienteId: $patientId) { id dataHora status }
}
```

```graphql
query History($patientId: ID!) {
  historicoCompleto(pacienteId: $patientId) { id dataHora status observacoes }
}
```

The complete executable schema is in `agendamento-service/src/main/resources/graphql/consulta.graphqls` and defines `Consulta`, `Paciente`, `Medico`, `StatusConsulta`, and the three queries above.

## Errors

REST errors use a consistent body:

```json
{"timestamp":"2026-09-13T18:00:00Z","status":409,"error":"Conflict","message":"The patient or doctor already has an appointment at this time"}
```

| Status | Meaning |
|---:|---|
| `400` | Invalid or missing request field |
| `401` | Missing, expired or invalid JWT; invalid credentials |
| `403` | Role or patient-ownership rule denied access |
| `404` | User or appointment not found |
| `409` | Duplicate email, scheduling conflict, past date, invalid participant role or invalid state transition |

GraphQL validation/execution failures are returned in the standard `errors` array.
