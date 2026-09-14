# Documentação de API - Tech Challenge 3

**Sistema de Agendamento e Notificação de Consultas Médicas**

**Versão:** 1.0.0  
**Data:** 2026-09-12  
**Base URL:** `http://localhost:8080` (Agendamento) | `http://localhost:8081` (Notificações)

---

## Índice

1. [Overview](#overview)
2. [Autenticação](#autenticação)
3. [Endpoints REST - Autenticação](#endpoints-rest---autenticação)
4. [Endpoints REST - Consultas](#endpoints-rest---consultas)
5. [GraphQL](#graphql)
6. [Modelos de Dados (DTO)](#modelos-de-dados-dto)
7. [Códigos de Status HTTP](#códigos-de-status-http)
8. [Códigos de Erro Customizados](#códigos-de-erro-customizados)
9. [Exemplos de Erro](#exemplos-de-erro)
10. [Event Stream (RabbitMQ)](#event-stream-rabbitmq)
11. [Rate Limiting & Segurança](#rate-limiting--segurança)

---

## Overview

### Serviços

| Serviço | Port | Descrição |
|---------|------|-----------|
| **Agendamento Service** | 8080 | API REST para autenticação e gerenciamento de consultas |
| **Notificação Service** | 8081 | Processador de eventos (sem endpoints REST públicos) |
| **RabbitMQ** | 5672 | Broker de mensagens para comunicação inter-serviços |
| **PostgreSQL Agendamento** | 5432 | Banco de dados do serviço de agendamento |
| **PostgreSQL Notificação** | 5433 | Banco de dados do serviço de notificações |

### Autenticação Padrão

Todos os endpoints (exceto `/auth/register` e `/auth/login`) requerem autenticação JWT:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Roles (Funções)

| Role | Descrição | Permissões |
|------|-----------|-----------|
| **MEDICO** | Médico | Editar consultas, listar todas as consultas |
| **ENFERMEIRO** | Enfermeiro | Criar consultas, listar todas as consultas |
| **PACIENTE** | Paciente | Listar apenas suas consultas |

---

## Autenticação

### Fluxo de Autenticação

```
1. Cliente → POST /auth/register (nome, email, senha, role)
2. Servidor → 201 CREATED { token, tokenType }
3. Cliente → POST /auth/login (email, senha)
4. Servidor → 200 OK { token, tokenType }
5. Cliente → GET /consultas (com Authorization: Bearer <token>)
6. Servidor → Valida JWT, retorna dados conforme role
```

### JWT Payload Exemplo

```json
{
  "sub": "user@example.com",
  "role": "MEDICO",
  "userId": 5,
  "iat": 1694595000,
  "exp": 1694598600
}
```

**Expiração:** 1 hora (3600000 ms)

---

## Endpoints REST - Autenticação

### 1. Registrar Novo Usuário

**Endpoint:** `POST /auth/register`

**Descrição:** Cria um novo usuário e retorna um JWT para acesso imediato.

**Autenticação:** Não requerida

**Request:**

```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "Senha@123",
  "role": "PACIENTE"
}
```

**Campos de Entrada:**

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `nome` | String | NOT NULL, 1-255 | Nome completo do usuário |
| `email` | String | NOT NULL, Email | Email único no sistema |
| `senha` | String | NOT NULL, 6-255 | Senha (será criptografada com Bcrypt) |
| `role` | Enum | NOT NULL | Uma de: `MEDICO`, `ENFERMEIRO`, `PACIENTE` |

**Response - Sucesso (201 CREATED):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIiwicm9sZSI6IlBBQ0lFTlRFIiwiaWF0IjoxNjk0NTk1MDAwLCJleHAiOjE2OTQ1OTg2MDB9.abc123...",
  "tokenType": "Bearer"
}
```

**Response - Erro (400 BAD REQUEST):**

```json
{
  "status": 400,
  "message": "Email já cadastrado",
  "errorCode": "EMAIL_DUPLICADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/auth/register"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 400 | `EMAIL_DUPLICADO` | Email já registrado no sistema |
| 400 | `CAMPO_INVALIDO` | Validação de entrada falhou |
| 500 | `ERRO_INTERNO` | Erro no servidor |

---

### 2. Login

**Endpoint:** `POST /auth/login`

**Descrição:** Autentica usuário com email e senha, retornando um novo JWT.

**Autenticação:** Não requerida

**Request:**

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "senha": "Senha@123"
}
```

**Campos de Entrada:**

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `email` | String | NOT NULL, Email | Email do usuário |
| `senha` | String | NOT NULL | Senha em texto plano |

**Response - Sucesso (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIiwicm9sZSI6IlBBQ0lFTlRFIiwiaWF0IjoxNjk0NTk1MDAwLCJleHAiOjE2OTQ1OTg2MDB9.abc123...",
  "tokenType": "Bearer"
}
```

**Response - Erro (401 UNAUTHORIZED):**

```json
{
  "status": 401,
  "message": "Credenciais inválidas",
  "errorCode": "CREDENCIAIS_INVALIDAS",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/auth/login"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 401 | `CREDENCIAIS_INVALIDAS` | Email/senha incorretos |
| 400 | `CAMPO_INVALIDO` | Validação de entrada falhou |
| 500 | `ERRO_INTERNO` | Erro no servidor |

---

## Endpoints REST - Consultas

### 1. Criar Nova Consulta

**Endpoint:** `POST /consultas`

**Descrição:** Cria uma nova consulta médica. Requer role `ENFERMEIRO`.

**Autenticação:** Requerida (Bearer Token)

**Autorização:** `hasRole('ENFERMEIRO')`

**Request:**

```http
POST http://localhost:8080/consultas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "pacienteId": 10,
  "medicoId": 20,
  "dataHora": "2026-09-25T14:30:00",
  "observacoes": "Consulta de rotina, verificar pressão e peso"
}
```

**Campos de Entrada:**

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `pacienteId` | Long | NOT NULL, > 0 | ID do paciente (deve existir) |
| `medicoId` | Long | NOT NULL, > 0 | ID do médico (deve existir) |
| `dataHora` | DateTime | NOT NULL, ISO8601 | Data e hora (não pode ser no passado) |
| `observacoes` | String | Optional, 0-1000 | Observações sobre a consulta |

**Response - Sucesso (201 CREATED):**

```json
{
  "id": 1,
  "paciente": {
    "id": 10,
    "nome": "João Silva",
    "email": "joao@example.com",
    "role": "PACIENTE"
  },
  "medico": {
    "id": 20,
    "nome": "Dr. Carlos",
    "email": "carlos@example.com",
    "role": "MEDICO"
  },
  "dataHora": "2026-09-25T14:30:00",
  "observacoes": "Consulta de rotina, verificar pressão e peso",
  "status": "AGENDADA"
}
```

**Response - Erro (400 BAD REQUEST):**

```json
{
  "status": 400,
  "message": "Paciente não encontrado",
  "errorCode": "RECURSO_NAO_ENCONTRADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

**Response - Erro (403 FORBIDDEN):**

```json
{
  "status": 403,
  "message": "Você não tem permissão para acessar este recurso",
  "errorCode": "ACESSO_NEGADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 201 | - | Consulta criada com sucesso |
| 400 | `CAMPO_INVALIDO` | Validação de entrada falhou |
| 400 | `RECURSO_NAO_ENCONTRADO` | Paciente ou médico não existe |
| 400 | `DATA_INVALIDA` | Data/hora no passado ou inválida |
| 401 | `NAO_AUTENTICADO` | JWT ausente ou inválido |
| 403 | `ACESSO_NEGADO` | User não tem role ENFERMEIRO |
| 500 | `ERRO_INTERNO` | Erro no servidor |

**Evento Publicado:**

Ao criar com sucesso, um evento `ConsultaCriadaEvent` é publicado no RabbitMQ:

```json
{
  "consultaId": 1,
  "pacienteId": 10,
  "pacienteNome": "João Silva",
  "pacienteEmail": "joao@example.com",
  "medicoId": 20,
  "dataHora": "2026-09-25T14:30:00"
}
```

---

### 2. Editar Consulta Existente

**Endpoint:** `PUT /consultas/{id}`

**Descrição:** Edita uma consulta existente. Requer role `MEDICO`.

**Autenticação:** Requerida (Bearer Token)

**Autorização:** `hasRole('MEDICO')`

**Parâmetro de Path:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | Long | ID da consulta a editar |

**Request:**

```http
PUT http://localhost:8080/consultas/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "pacienteId": 10,
  "medicoId": 20,
  "dataHora": "2026-09-26T15:00:00",
  "observacoes": "Consulta reagendada - paciente em melhor estado"
}
```

**Campos de Entrada:** (mesmo de criar)

| Campo | Tipo | Validação | Descrição |
|-------|------|-----------|-----------|
| `pacienteId` | Long | NOT NULL, > 0 | ID do paciente |
| `medicoId` | Long | NOT NULL, > 0 | ID do médico |
| `dataHora` | DateTime | NOT NULL, ISO8601 | Data e hora |
| `observacoes` | String | Optional | Observações |

**Response - Sucesso (200 OK):**

```json
{
  "id": 1,
  "paciente": {
    "id": 10,
    "nome": "João Silva",
    "email": "joao@example.com",
    "role": "PACIENTE"
  },
  "medico": {
    "id": 20,
    "nome": "Dr. Carlos",
    "email": "carlos@example.com",
    "role": "MEDICO"
  },
  "dataHora": "2026-09-26T15:00:00",
  "observacoes": "Consulta reagendada - paciente em melhor estado",
  "status": "AGENDADA"
}
```

**Response - Erro (404 NOT FOUND):**

```json
{
  "status": 404,
  "message": "Consulta não encontrada",
  "errorCode": "RECURSO_NAO_ENCONTRADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas/1"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 200 | - | Consulta atualizada com sucesso |
| 400 | `CAMPO_INVALIDO` | Validação de entrada falhou |
| 401 | `NAO_AUTENTICADO` | JWT ausente ou inválido |
| 403 | `ACESSO_NEGADO` | User não tem role MEDICO |
| 404 | `RECURSO_NAO_ENCONTRADO` | Consulta com ID não existe |
| 500 | `ERRO_INTERNO` | Erro no servidor |

**Evento Publicado:**

Ao editar com sucesso, um evento `ConsultaEditadaEvent` é publicado:

```json
{
  "consultaId": 1,
  "pacienteId": 10,
  "pacienteNome": "João Silva",
  "pacienteEmail": "joao@example.com",
  "medicoId": 20,
  "dataHora": "2026-09-26T15:00:00",
  "status": "AGENDADA"
}
```

---

### 3. Listar Consultas

**Endpoint:** `GET /consultas`

**Descrição:** Lista consultas filtradas por role do usuário. Pacientes veem apenas suas consultas, médicos e enfermeiros veem todas.

**Autenticação:** Requerida (Bearer Token)

**Autorização:** `hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')`

**Request:**

```http
GET http://localhost:8080/consultas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Query Parameters:** (nenhum)

**Response - Sucesso (200 OK):**

```json
[
  {
    "id": 1,
    "paciente": {
      "id": 10,
      "nome": "João Silva",
      "email": "joao@example.com",
      "role": "PACIENTE"
    },
    "medico": {
      "id": 20,
      "nome": "Dr. Carlos",
      "email": "carlos@example.com",
      "role": "MEDICO"
    },
    "dataHora": "2026-09-25T14:30:00",
    "observacoes": "Consulta de rotina",
    "status": "AGENDADA"
  },
  {
    "id": 2,
    "paciente": {
      "id": 11,
      "nome": "Maria Santos",
      "email": "maria@example.com",
      "role": "PACIENTE"
    },
    "medico": {
      "id": 21,
      "nome": "Dra. Fernanda",
      "email": "fernanda@example.com",
      "role": "MEDICO"
    },
    "dataHora": "2026-09-26T10:00:00",
    "observacoes": "Consulta pós-cirúrgica",
    "status": "AGENDADA"
  }
]
```

**Response - Vazio (200 OK):**

```json
[]
```

**Response - Erro (401 UNAUTHORIZED):**

```json
{
  "status": 401,
  "message": "JWT token é inválido",
  "errorCode": "TOKEN_INVALIDO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 200 | - | Consultas listadas com sucesso |
| 401 | `NAO_AUTENTICADO` | JWT ausente |
| 401 | `TOKEN_INVALIDO` | JWT inválido ou expirado |
| 403 | `ACESSO_NEGADO` | Role não autorizado |
| 500 | `ERRO_INTERNO` | Erro no servidor |

**Filtros por Role:**

- **PACIENTE**: Retorna apenas suas consultas
  ```json
  GET /consultas
  // Retorna: consultas.paciente_id == user.id
  ```

- **MEDICO / ENFERMEIRO**: Retorna todas as consultas
  ```json
  GET /consultas
  // Retorna: todas as consultas
  ```

---

### 4. Buscar Consulta por ID

**Endpoint:** `GET /consultas/{id}`

**Descrição:** Busca uma consulta específica. Pacientes só podem buscar suas próprias consultas.

**Autenticação:** Requerida (Bearer Token)

**Autorização:** `hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')`

**Parâmetro de Path:**

| Parâmetro | Tipo | Descrição |
|-----------|------|-----------|
| `id` | Long | ID da consulta |

**Request:**

```http
GET http://localhost:8080/consultas/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response - Sucesso (200 OK):**

```json
{
  "id": 1,
  "paciente": {
    "id": 10,
    "nome": "João Silva",
    "email": "joao@example.com",
    "role": "PACIENTE"
  },
  "medico": {
    "id": 20,
    "nome": "Dr. Carlos",
    "email": "carlos@example.com",
    "role": "MEDICO"
  },
  "dataHora": "2026-09-25T14:30:00",
  "observacoes": "Consulta de rotina",
  "status": "AGENDADA"
}
```

**Response - Erro (403 FORBIDDEN - Paciente tentando acessar consulta de outro):**

```json
{
  "status": 403,
  "message": "Você não tem permissão para acessar esta consulta",
  "errorCode": "ACESSO_NEGADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas/1"
}
```

**Response - Erro (404 NOT FOUND):**

```json
{
  "status": 404,
  "message": "Consulta não encontrada",
  "errorCode": "RECURSO_NAO_ENCONTRADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas/1"
}
```

**Possíveis Erros:**

| Código HTTP | Erro Customizado | Descrição |
|------------|-----------------|-----------|
| 200 | - | Consulta recuperada com sucesso |
| 401 | `NAO_AUTENTICADO` | JWT ausente |
| 401 | `TOKEN_INVALIDO` | JWT inválido ou expirado |
| 403 | `ACESSO_NEGADO` | Paciente tentando acessar consulta de outro |
| 404 | `RECURSO_NAO_ENCONTRADO` | Consulta não existe |
| 500 | `ERRO_INTERNO` | Erro no servidor |

---

## GraphQL

Além dos endpoints REST, o Serviço de Agendamento expõe uma API GraphQL (Spring for GraphQL) para consultas flexíveis sobre consultas e histórico médico.

**Endpoint:** `POST /graphql`
**GraphiQL:** disponível em `/graphiql` no perfil local do Docker Compose (`GRAPHQL_GRAPHIQL_ENABLED=true`)
**Autenticação:** requer JWT igual aos endpoints REST
**Autorização:** `@PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')")`, com checagem adicional de propriedade — pacientes só recuperam as próprias consultas

**Schema:** `agendamento-service/src/main/resources/graphql/consulta.graphqls`, define os tipos `Consulta`, `Paciente`, `Medico`, `StatusConsulta` e as três queries abaixo.

### Query: `consultasPorPaciente`

```graphql
query PatientSchedule($patientId: ID!, $status: StatusConsulta) {
  consultasPorPaciente(pacienteId: $patientId, status: $status) {
    id dataHora status observacoes
    paciente { id nome }
    medico { id nome }
  }
}
```

### Query: `consultasFuturas`

```graphql
query Upcoming($patientId: ID!) {
  consultasFuturas(pacienteId: $patientId) { id dataHora status }
}
```

### Query: `historicoCompleto`

```graphql
query History($patientId: ID!) {
  historicoCompleto(pacienteId: $patientId) { id dataHora status observacoes }
}
```

`historicoCompleto` retorna apenas consultas com status `REALIZADA` ou `CANCELADA`, ordenadas da mais recente para a mais antiga.

### Erros GraphQL

Falhas de validação ou execução são retornadas no array padrão `errors` da resposta GraphQL, incluindo violações de autorização (paciente tentando ler histórico de outro paciente).

---

## Modelos de Dados (DTO)

### AuthResponse

**Tipo:** Response DTO

```json
{
  "token": "string (JWT)",
  "tokenType": "Bearer"
}
```

### RegisterRequest

**Tipo:** Request DTO

```json
{
  "nome": "string (1-255)",
  "email": "string (email válido)",
  "senha": "string (6+ caracteres)",
  "role": "MEDICO | ENFERMEIRO | PACIENTE"
}
```

### LoginRequest

**Tipo:** Request DTO

```json
{
  "email": "string (email válido)",
  "senha": "string"
}
```

### ConsultaRequest

**Tipo:** Request DTO

```json
{
  "pacienteId": "long (> 0)",
  "medicoId": "long (> 0)",
  "dataHora": "string (ISO8601: YYYY-MM-DDTHH:mm:ss)",
  "observacoes": "string (optional, max 1000 chars)"
}
```

### ConsultaResponse

**Tipo:** Response DTO

```json
{
  "id": "long",
  "paciente": {
    "id": "long",
    "nome": "string",
    "email": "string",
    "role": "MEDICO | ENFERMEIRO | PACIENTE"
  },
  "medico": {
    "id": "long",
    "nome": "string",
    "email": "string",
    "role": "MEDICO | ENFERMEIRO | PACIENTE"
  },
  "dataHora": "string (ISO8601)",
  "observacoes": "string",
  "status": "AGENDADA | CONFIRMADA | CANCELADA"
}
```

### ErrorResponse

**Tipo:** Response DTO (em caso de erro)

```json
{
  "status": "int (HTTP status code)",
  "message": "string (descrição amigável)",
  "errorCode": "string (código de erro customizado)",
  "timestamp": "string (ISO8601)",
  "path": "string (caminho da requisição)"
}
```

---

## Códigos de Status HTTP

### 2xx - Sucesso

| Código | Descrição | Quando |
|--------|-----------|--------|
| **200** | OK | Requisição bem-sucedida (GET, PUT) |
| **201** | CREATED | Recurso criado com sucesso (POST) |

### 4xx - Erro do Cliente

| Código | Descrição | Quando |
|--------|-----------|--------|
| **400** | BAD REQUEST | Validação falhou, dados inválidos |
| **401** | UNAUTHORIZED | JWT ausente ou inválido, credenciais inválidas |
| **403** | FORBIDDEN | Usuário autenticado mas sem permissão (role insuficiente) |
| **404** | NOT FOUND | Recurso não encontrado (consulta, usuário) |
| **409** | CONFLICT | Recurso duplicado (ex: email já registrado) |

### 5xx - Erro do Servidor

| Código | Descrição | Quando |
|--------|-----------|--------|
| **500** | INTERNAL SERVER ERROR | Erro não previsto no servidor |

---

## Códigos de Erro Customizados

| Código | Descrição | HTTP | Causa |
|--------|-----------|------|-------|
| `EMAIL_DUPLICADO` | Email já cadastrado | 400/409 | Email já existe no banco |
| `CREDENCIAIS_INVALIDAS` | Email/senha incorretos | 401 | Combinação email/senha não existe |
| `CAMPO_INVALIDO` | Validação de entrada falhou | 400 | Email inválido, senha curta, etc |
| `RECURSO_NAO_ENCONTRADO` | Consulta/Usuário não existe | 404 | ID não encontrado no banco |
| `DATA_INVALIDA` | Data/hora inválida | 400 | Data no passado ou formato inválido |
| `ACESSO_NEGADO` | Sem permissão para operação | 403 | Role insuficiente ou dados de outro usuário |
| `NAO_AUTENTICADO` | JWT ausente | 401 | Authorization header faltando |
| `TOKEN_INVALIDO` | JWT inválido/expirado | 401 | Token malformado ou expirado |
| `ERRO_INTERNO` | Erro não previsto | 500 | Exception não mapeada |

---

## Exemplos de Erro

### Exemplo 1: Email Duplicado

**Request:**
```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "nome": "João Silva",
  "email": "joao@example.com",
  "senha": "Senha@123",
  "role": "PACIENTE"
}
```

**Response (400 / 409):**
```json
{
  "status": 409,
  "message": "Email já cadastrado no sistema",
  "errorCode": "EMAIL_DUPLICADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/auth/register"
}
```

### Exemplo 2: Credenciais Inválidas

**Request:**
```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "joao@example.com",
  "senha": "SenhaErrada"
}
```

**Response (401):**
```json
{
  "status": 401,
  "message": "Credenciais inválidas. Verifique email e senha.",
  "errorCode": "CREDENCIAIS_INVALIDAS",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/auth/login"
}
```

### Exemplo 3: Sem Permissão (Role)

**Request:**
```http
POST http://localhost:8080/consultas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "pacienteId": 10,
  "medicoId": 20,
  "dataHora": "2026-09-25T14:30:00",
  "observacoes": "Consulta"
}

// User tem role PACIENTE, mas precisa ENFERMEIRO
```

**Response (403):**
```json
{
  "status": 403,
  "message": "Você não tem permissão para acessar este recurso. Role necessária: ENFERMEIRO",
  "errorCode": "ACESSO_NEGADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

### Exemplo 4: Data Inválida (Passado)

**Request:**
```http
POST http://localhost:8080/consultas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "pacienteId": 10,
  "medicoId": 20,
  "dataHora": "2020-01-01T14:30:00",
  "observacoes": "Consulta"
}
```

**Response (400):**
```json
{
  "status": 400,
  "message": "Data e hora não podem ser no passado",
  "errorCode": "DATA_INVALIDA",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

### Exemplo 5: JWT Expirado

**Request:**
```http
GET http://localhost:8080/consultas
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE2OTQ1OTQ5OTl9...
```

**Response (401):**
```json
{
  "status": 401,
  "message": "JWT token expirado. Faça login novamente.",
  "errorCode": "TOKEN_INVALIDO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas"
}
```

### Exemplo 6: Recurso Não Encontrado

**Request:**
```http
GET http://localhost:8080/consultas/9999
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (404):**
```json
{
  "status": 404,
  "message": "Consulta com ID 9999 não encontrada",
  "errorCode": "RECURSO_NAO_ENCONTRADO",
  "timestamp": "2026-09-12T10:30:00Z",
  "path": "/consultas/9999"
}
```

---

## Event Stream (RabbitMQ)

### Visão Geral

O sistema utiliza **RabbitMQ** para comunicação assíncrona entre serviços através de **eventos**.

### Eventos Publicados

#### 1. ConsultaCriadaEvent

**Publicado quando:** Uma nova consulta é criada via `POST /consultas`

**Publicado por:** Serviço de Agendamento (após persistência bem-sucedida)

**Exchange:** `consultas.exchange`

**Routing Key:** `consultas.event.created`

**Payload:**

```json
{
  "consultaId": 1,
  "pacienteId": 10,
  "pacienteNome": "João Silva",
  "pacienteEmail": "joao@example.com",
  "medicoId": 20,
  "dataHora": "2026-09-25T14:30:00"
}
```

**Consumido por:** Serviço de Notificações → `NotificacaoListener.receberConsultaCriada()`

**Ação:** Envia notificação de confirmação ao paciente

---

#### 2. ConsultaEditadaEvent

**Publicado quando:** Uma consulta é editada via `PUT /consultas/{id}`

**Publicado por:** Serviço de Agendamento (após persistência bem-sucedida)

**Exchange:** `consultas.exchange`

**Routing Key:** `consultas.event.edited`

**Payload:**

```json
{
  "consultaId": 1,
  "pacienteId": 10,
  "pacienteNome": "João Silva",
  "pacienteEmail": "joao@example.com",
  "medicoId": 20,
  "dataHora": "2026-09-26T15:00:00",
  "status": "AGENDADA"
}
```

**Consumido por:** Serviço de Notificações → `NotificacaoListener.receberConsultaEditada()`

**Ação:** Envia notificação de alteração ao paciente

---

### Topologia RabbitMQ

```
┌─────────────────────────────────────────────────────────┐
│                   Topic Exchange                        │
│               "consultas.exchange"                      │
└─────────────────────────────────────────────────────────┘
                          │
                          │ Routing Keys
                    ┌─────┴──────────┐
                    │                │
         consultas.event.*    (qualquer evento de consulta)
                    │
        ┌───────────┴───────────┐
        │                       │
    ┌───▼────────────────┐  ┌──▼──────────────────┐
    │ Queue              │  │ Queue               │
    │ consultas.         │  │ (future queue)      │
    │ notificacoes.queue │  │                     │
    └───┬────────────────┘  └─────────────────────┘
        │
        │ Consumer: NotificacaoListener
        │ (Serviço de Notificações)
        │
        ▼
    ┌──────────────────┐
    │ Processa evento  │
    │ Envia notificação│
    └──────────────────┘
```

### Dead Letter Queue (DLQ)

Se um evento falhar em processar após 3 tentativas, é enviado para:

**Queue:** `consultas.notificacoes.dlq`

**Ação:** Manual inspection + retry após intervenção

```properties
# Configuração (Futura)
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1000
```

---

## Rate Limiting & Segurança

### Recomendações de Produção

#### 1. Rate Limiting

Implementar via Spring Cloud Gateway ou Nginx:

```properties
# Exemplo: máximo 100 requisições por minuto por IP
ratelimit.requests-per-minute=100
ratelimit.window-size-ms=60000
```

#### 2. CORS

Configurar origens permitidas:

```properties
cors.allowed-origins=https://frontend.example.com,https://app.example.com
cors.allowed-methods=GET,POST,PUT,DELETE
cors.allowed-headers=Content-Type,Authorization
```

#### 3. HTTPS/TLS

Forçar HTTPS em produção:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_PASSWORD}
```

#### 4. Content Security Policy

```http
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'
```

#### 5. Autenticação do RabbitMQ

Em produção, usar credenciais seguras:

```yaml
spring:
  rabbitmq:
    host: rabbitmq.production.internal
    port: 5671  # SSL/TLS
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    ssl: true
```

#### 6. Versionamento de API

```http
GET /api/v1/consultas
GET /api/v2/consultas  (futuro)
```

---

## Referências

- [JWT.io](https://jwt.io) - JWT Debugger
- [Spring Security](https://spring.io/projects/spring-security)
- [RabbitMQ Documentation](https://www.rabbitmq.com/documentation.html)
- [REST API Best Practices](https://restfulapi.net)
- [HTTP Status Codes](https://httpwg.org/specs/rfc7231.html#status.codes)

---

**Versão:** 1.0.0  
**Última atualização:** 2026-09-12