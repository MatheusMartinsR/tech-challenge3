# Diagramas da Arquitetura - Tech Challenge 3

** Visualizações de Alto Nível da Arquitetura**

---

## 1. Arquitetura C4 - Nível 1: Contexto do Sistema

```
┌──────────────────────────────────────────────────────────────────┐
│                     Sistema de Agendamento                       │
│             Tech Challenge 3 - Fase 3 Pós-Graduação            │
└──────────────────────────────────────────────────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              [Usuários]         [Notificações]
            (Médicos, Enfer.)   (Email/Sistema)
                    │                   │
                    ▼                   ▼
        ┌──────────────────────────────────────┐
        │  Plataforma de Agendamento de        │
        │  Consultas Médicas                   │
        │  - Autenticação JWT                  │
        │  - Gerenciar Consultas (CRUD)        │
        │  - Notificações Automáticas          │
        └──────────────────────────────────────┘
```

---

## 2. Arquitetura C4 - Nível 2: Containers

```
╔════════════════════════════════════════════════════════════════════╗
║                                                                    ║
║                    TECH CHALLENGE 3 PLATFORM                      ║
║                                                                    ║
║  ┌──────────────────┐        ┌───────────────┐      ┌────────────┐
║  │                  │        │               │      │            │
║  │  AGENDAMENTO     │◄──────►│   RABBITMQ    │◄────►│NOTIFICACAO │
║  │  SERVICE         │  AMQP  │   BROKER      │ AMQP │ SERVICE    │
║  │  (Port 8080)     │        │  (Port 5672)  │      │(Port 8081) │
║  │                  │        │               │      │            │
║  │ ✓ Auth (JWT)     │        │ ✓ Topic       │      │✓Listener   │
║  │ ✓ Consultas      │        │   Exchange    │      │✓Send Notif.│
║  │ ✓ Pub Events     │        │ ✓ Queues      │      │✓Persist    │
║  │                  │        │ ✓ Management  │      │            │
║  └────────┬─────────┘        └───────────────┘      └────┬───────┘
║           │                                               │
║  ┌────────▼──────────────┐                  ┌────────────▼─────┐
║  │                       │                  │                  │
║  │ PostgreSQL 5432       │                  │ PostgreSQL 5433  │
║  │                       │                  │                  │
║  │ ✓ users              │                  │ ✓ notificacoes  │
║  │ ✓ consultas          │                  │                  │
║  │                       │                  │                  │
║  └───────────────────────┘                  └──────────────────┘
║
╚════════════════════════════════════════════════════════════════════╝
```

---

## 3. Arquitetura de Componentes - Serviço Agendamento

```
┌──────────────────────────────────────────────────────────────────┐
│              SERVIÇO DE AGENDAMENTO - ARQUITETURA HEXAGONAL      │
└──────────────────────────────────────────────────────────────────┘

                    ╔════════════════════════════╗
                    ║  Aplicação (Spring Boot)   ║
                    ║  Port: 8080                ║
                    ╚════════════════════════════╝
                              │
         ┌────────────────────┼────────────────────┐
         │                    │                    │
    ┌────▼─────┐         ┌────▼─────┐        ┌────▼─────┐
    │ REST      │         │ REST      │        │ REST      │
    │ Controller│         │ Controller│        │ Controller│
    │           │         │           │        │           │
    │/auth      │         │/consultas │        │/actuator  │
    └────┬──────┘         └────┬──────┘        └───────────┘
         │                     │
    ┌────┴─────────────────────┴────┐
    │                                │
    │  USE CASES (Application)       │
    │                                │
    │ ┌───────────────────┐          │
    │ │ LoginUseCase      │          │
    │ │ RegisterUserUseCase
    │ │ RegistrarConsulta │          │
    │ │ EditarConsulta    │          │
    │ │ ListarConsultas   │          │
    │ │ BuscarConsulta    │          │
    │ └───────────────────┘          │
    │                                │
    └────┬─────────────────────┬────┘
         │                     │
    ┌────▼──────┐         ┌────▼──────┐
    │ DOMAIN     │         │ DOMAIN    │
    │ LAYER      │         │ LAYER     │
    │            │         │           │
    │ • User     │         │ • Consulta│
    │ • Role     │         │ • Status  │
    │ • Exception│         │ • Event   │
    └────┬──────┘         └────┬──────┘
         │                     │
    ┌────┴─────────────────────┴────┐
    │                                │
    │  INFRASTRUCTURE (Adapters)     │
    │                                │
    │ ┌────────────────────────────┐ │
    │ │ • JPA Repositories         │ │
    │ │ • Spring Security + JWT    │ │
    │ │ • RabbitMQ Publisher       │ │
    │ │ • Password Encoder (Bcrypt)│ │
    │ │ • Health Checks            │ │
    │ └────────────────────────────┘ │
    │                                │
    └────┬─────────────────────┬────┘
         │                     │
    ┌────▼──────┐         ┌────▼──────┐
    │ PostgreSQL │         │ RabbitMQ  │
    │ Database   │         │ Broker    │
    │ (Port 5432)│         │ (Port 5672
    └────────────┘         └───────────┘
```

---

## 4. Arquitetura de Componentes - Serviço Notificação

```
┌──────────────────────────────────────────────────────────────┐
│         SERVIÇO DE NOTIFICAÇÃO - ARQUITETURA HEXAGONAL       │
└──────────────────────────────────────────────────────────────┘

            ╔═════════════════════════════════╗
            ║  Aplicação (Spring Boot)        ║
            ║  Port: 8081                     ║
            ║  (Sem endpoints REST públicos)  ║
            ╚═════════════════════════════════╝
                          │
                          │
         ┌────────────────┴────────────────┐
         │                                 │
    ┌────▼─────────┐         ┌────────────▼────┐
    │ /actuator    │         │ RabbitMQ        │
    │ Health Check │         │ Consumer        │
    └──────────────┘         │                 │
                             │ @RabbitListener │
                             │ NotificacaoListener
                             └────┬────────────┘
                                  │
                    ┌─────────────┴─────────────┐
                    │                           │
                ┌───▼───┐                   ┌───▼───┐
                │ Handler│                   │Handler│
                │Consulta│                   │Consulta
                │Criada  │                   │Editada│
                └───┬───┘                   └───┬───┘
                    │                           │
    ┌───────────────┴──────────────┬────────────┘
    │                              │
    │  USE CASES (Application)     │
    │                              │
    │ ┌──────────────────────────┐ │
    │ │ EnviarLembreteConsulta   │ │
    │ │ UseCase                  │ │
    │ └──────────────────────────┘ │
    │                              │
    └───┬───────────────────────┬──┘
        │                       │
    ┌───▼─────────┐      ┌──────▼───────┐
    │ DOMAIN      │      │ DOMAIN       │
    │ LAYER       │      │ LAYER        │
    │             │      │              │
    │ • Notificacao
    │ • Destinatario
    │ • TipoNotif. │      │ • StatusNotif│
    │ • Exception  │      │              │
    └───┬─────────┘      └──────┬───────┘
        │                       │
    ┌───┴───────────────────────┴───┐
    │                               │
    │  INFRASTRUCTURE (Adapters)    │
    │                               │
    │ ┌──────────────────────────┐  │
    │ │ • JPA Repository         │  │
    │ │ • RabbitMQ Config        │  │
    │ │ • Notification Service   │  │
    │ │   (mock/email)           │  │
    │ │ • Logging                │  │
    │ └──────────────────────────┘  │
    │                               │
    └───┬───────────────────────┬───┘
        │                       │
    ┌───▼───────────┐      ┌────▼────────┐
    │ PostgreSQL    │      │ RabbitMQ    │
    │ Database      │      │ (Consumer)  │
    │ (Port 5433)   │      │ (Port 5672) │
    └───────────────┘      └─────────────┘
```

---

## 5. Fluxo de Dados: Criar Consulta

```
┌─────────────────────────────────────────────────────────────────┐
│                 FLUXO: CRIAR NOVA CONSULTA                      │
└─────────────────────────────────────────────────────────────────┘

PASSO 1: Cliente faz requisição
┌──────────────┐
│ Cliente REST │
└──────┬───────┘
       │
       │ POST /consultas
       │ Authorization: Bearer <token>
       │ { pacienteId: 10, medicoId: 20, dataHora, obs }
       │
       ▼
┌────────────────────────────────────────────┐
│ ConsultaController.registrar()             │
└───────────────┬────────────────────────────┘
                │
PASSO 2: Spring Security Valida JWT
                │
                ├─ Extrair token
                ├─ Validar assinatura
                ├─ Verificar expiração
                ├─ Extrair role: ENFERMEIRO ✓
                │
                ▼
┌─────────────────────────────────────────────────┐
│ RegistrarConsultaUseCase.execute()              │
│                                                 │
│ 1. Validar dados de entrada                     │
│    ├─ pacienteId > 0 ✓                         │
│    ├─ medicoId > 0 ✓                           │
│    ├─ dataHora > agora ✓                       │
│                                                 │
│ 2. Buscar usuários no banco                     │
│    ├─ Paciente: ID 10 encontrado ✓             │
│    ├─ Médico: ID 20 encontrado ✓               │
│                                                 │
│ 3. Criar objeto Consulta (domínio)              │
│    └─ status = AGENDADA                         │
│                                                 │
│ 4. Persistir no banco                           │
│    └─ INSERT INTO consultas (...)               │
│       RETURNING id = 1                          │
│                                                 │
└────────────────┬────────────────────────────────┘
                 │
                 ▼
         Response HTTP 201
      {
        id: 1,
        paciente: { id: 10, nome: "João", ... },
        medico: { id: 20, nome: "Dr. Carlos", ... },
        dataHora: "2026-09-25T14:30:00",
        observacoes: "Consulta de rotina",
        status: "AGENDADA"
      }
                 │
                 ├─ Retornar para cliente ✓
                 │
                 ▼
PASSO 3: Publicar evento (ASYNC - não bloqueia)
                 │
        ┌────────▼────────┐
        │ ConsultaCriada  │
        │ Event Publisher │
        └────────┬────────┘
                 │
       ┌─────────▼──────────┐
       │ Serializar evento  │
       │ Montar payload:    │
       │ {                  │
       │   consultaId: 1,   │
       │   pacienteId: 10,  │
       │   pacienteNome: ...,
       │   pacienteEmail: ...,
       │   medicoId: 20,    │
       │   dataHora: ...    │
       │ }                  │
       └─────────┬──────────┘
                 │
                 ▼
       ┌──────────────────────┐
       │ RabbitMQ             │
       │ Exchange:            │
       │ "consultas.exchange" │
       │                      │
       │ Routing Key:         │
       │ "consultas.event.   │
       │  created"           │
       └──────────┬───────────┘
                  │
                  ▼
       ┌────────────────────────────┐
       │ Fila: consultas.           │
       │ notificacoes.queue         │
       └──────────┬─────────────────┘
                  │
                  ▼
PASSO 4: Serviço de Notificação consome evento
       ┌──────────────────────────────┐
       │ NotificacaoListener          │
       │ @RabbitListener              │
       │ receberConsultaCriada()      │
       └────────────┬─────────────────┘
                    │
                    ▼
       ┌──────────────────────────────┐
       │ EnviarLembreteConsultaUseCase│
       │                              │
       │ 1. Montar mensagem           │
       │    "Sua consulta agendada    │
       │     para 25/09 às 14:30"     │
       │                              │
       │ 2. Criar Notificacao (DB)    │
       │    status = PENDENTE         │
       │                              │
       │ 3. Enviar (mock/email)       │
       │    simulate: true            │
       │                              │
       │ 4. Atualizar status          │
       │    status = ENVIADA          │
       │                              │
       │ 5. Log success               │
       └──────────────────────────────┘

RESULTADO FINAL:
✓ Consulta criada com ID 1
✓ Notificação enviada ao paciente
✓ Evento persistido no RabbitMQ
✓ Logs registrados
```

---

## 6. Fluxo de Dados: Autenticação (Login)

```
┌──────────────────────────────────────────────────────────────┐
│              FLUXO: LOGIN E OBTER JWT TOKEN                  │
└──────────────────────────────────────────────────────────────┘

┌──────────────┐
│ Cliente      │
└──────┬───────┘
       │
       │ POST /auth/login
       │ { email: "joao@example.com", senha: "Senha@123" }
       │
       ▼
┌────────────────────────────────────┐
│ AuthController.login()             │
└────────────┬──────────────────────┘
             │
             ▼
┌────────────────────────────────────────┐
│ LoginUseCase.execute()                 │
│                                        │
│ 1. Buscar usuário por email            │
│    SELECT * FROM users                 │
│    WHERE email = 'joao@example.com'    │
│    Result: User { id: 1, nome: "...",  │
│             email: "...",              │
│             senha: "$2a$10...",        │
│             role: "PACIENTE" }         │
│                                        │
│ 2. Validar senha                       │
│    BCrypt.matches(                     │
│      input: "Senha@123",               │
│      stored: "$2a$10..."               │
│    )                                   │
│    Result: ✓ Match                     │
│                                        │
│ 3. Gerar JWT Token                     │
│    Claims: {                           │
│      sub: "joao@example.com",          │
│      role: "PACIENTE",                 │
│      userId: 1,                        │
│      iat: 1694595000 (now),            │
│      exp: 1694598600 (now + 1h)        │
│    }                                   │
│    Secret: "dev-secret-key"            │
│    Algorithm: HS256                    │
│    Result: eyJhbGc...                  │
│                                        │
│ 4. Construir response                  │
│    {                                   │
│      token: "eyJhbGc...",              │
│      tokenType: "Bearer"               │
│    }                                   │
│                                        │
└────────────┬──────────────────────────┘
             │
             ▼
    Response HTTP 200 OK
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvQGV4YW1wbGUuY29tIiwicm9sZSI6IlBBQ0lFTlRFIiwiaWF0IjoxNjk0NTk1MDAwLCJleHAiOjE2OTQ1OTg2MDB9.abc123...",
      "tokenType": "Bearer"
    }
             │
             ▼
┌──────────────────────────────────────┐
│ Cliente armazena token               │
│                                      │
│ localStorage.setItem(                │
│   "auth_token",                      │
│   "eyJhbGc..."                       │
│ )                                    │
│                                      │
│ Próximas requisições usam:           │
│ Authorization: Bearer eyJhbGc...     │
└──────────────────────────────────────┘
```

---

## 7. Modelo de Dados: Entidade-Relacionamento

```
┌────────────────────────────────────────────────────────────────┐
│              MODELO ER - SERVIÇO AGENDAMENTO                   │
└────────────────────────────────────────────────────────────────┘

                    ┌──────────────────────┐
                    │        USERS         │
                    ├──────────────────────┤
                    │ PK │ id (BIGSERIAL)  │
                    ├────┼──────────────────┤
                    │    │ nome (VARCHAR)   │
                    │    │ email (VARCHAR)  │ ◄──── UNIQUE
                    │    │ senha (VARCHAR)  │
                    │    │ role (VARCHAR)   │
                    │    │ created_at       │
                    │    │ updated_at       │
                    └────┬──────────────────┘
                         │
                    ┌────┴───────────┐
                    │                │
              paciente_id        medico_id
              (FK 1:N)           (FK 1:N)
                    │                │
                    ▼                ▼
            ┌──────────────────────────────┐
            │     CONSULTAS                │
            ├──────────────────────────────┤
            │ PK │ id (BIGSERIAL)          │
            ├────┼──────────────────────────┤
            │ FK │ paciente_id → USERS.id   │
            │ FK │ medico_id → USERS.id     │
            │    │ data_hora (TIMESTAMP)    │
            │    │ observacoes (TEXT)       │
            │    │ status (VARCHAR)         │
            │    │ created_at (TIMESTAMP)   │
            │    │ updated_at (TIMESTAMP)   │
            └──────────────────────────────┘

Cardinalidade:
• Uma USERS pode ter muitas CONSULTAS (1:N)
• Cada CONSULTA precisa de 2 USERS (paciente e médico)
```

```
┌────────────────────────────────────────────────────────────────┐
│            MODELO ER - SERVIÇO NOTIFICAÇÃO                     │
└────────────────────────────────────────────────────────────────┘

            ┌──────────────────────────────┐
            │   NOTIFICACOES               │
            ├──────────────────────────────┤
            │ PK │ id (BIGSERIAL)          │
            ├────┼──────────────────────────┤
            │    │ consulta_id (BIGINT)     │ ◄── INDEX
            │    │ paciente_id (BIGINT)     │ ◄── INDEX
            │    │ destinatario (VARCHAR)   │
            │    │ tipo (VARCHAR)           │
            │    │   - CONSULTA_CRIADA      │
            │    │   - CONSULTA_EDITADA     │
            │    │ mensagem (TEXT)          │
            │    │ status (VARCHAR)         │
            │    │   - PENDENTE             │
            │    │   - ENVIADA              │
            │    │   - FALHA                │
            │    │ data_envio (TIMESTAMP)   │
            │    │ created_at (TIMESTAMP)   │
            │    │ updated_at (TIMESTAMP)   │
            └──────────────────────────────┘

Relacionamento:
• Sem FK para USERS (banco isolado)
• Recebe dados via evento RabbitMQ
• Mantém referência lógica via IDs
```

---

## 8. Topologia RabbitMQ

```
┌─────────────────────────────────────────────────────────────────┐
│                      RABBITMQ TOPOLOGY                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  Topic Exchange: "consultas.exchange"                        │
│  Type: TOPIC (pattern matching)                             │
│  Durable: true                                              │
│  Auto-delete: false                                         │
└──────────────────────────────┬───────────────────────────────┘
                               │
              Routing Keys: ┌──┼──┐
                            │  │  │
        ┌─ consultas.event.created
        │
        ├─ consultas.event.edited
        │
        └─ consultas.event.* (all events)
                            │
                            ▼
        ┌───────────────────────────────────────┐
        │  Queue: "consultas.notificacoes.queue"│
        │  Durable: true                        │
        │  Exclusive: false                     │
        │  Auto-delete: false                   │
        │  TTL: (nenhum - persiste)            │
        └───────────────────┬───────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
         Consumer #1              Consumer #2
      (NotificacaoListener)   (NotificacaoListener)
      Instance Port 8081    Instance Port 8081
                │                       │
         ┌──────▼──────┐        ┌──────▼──────┐
         │ Processar   │        │ Processar   │
         │ Evento      │        │ Evento      │
         │ Concurrent  │        │ Concurrent  │
         │ Listeners   │        │ Listeners   │
         └─────────────┘        └─────────────┘

Fluxo de Mensagem:
1. Agendamento Service publica evento
2. RabbitMQ recebe em "consultas.exchange"
3. Faz matching com routing key
4. Entrega à fila "consultas.notificacoes.queue"
5. Qualquer Consumer disponível processa
6. Consumer acknowledges (ACK) após sucesso
7. Mensagem é removida da fila
8. Ou vai para DLQ se falhar 3x (Retry)

Dead Letter Queue (DLQ):
┌──────────────────────────────────┐
│ DLX: "consultas.dlx"             │
│ Queue: "consultas.dlq"           │
│                                  │
│ Mensagens com erro recebem retry:│
│ Initial delay: 1s               │
│ Max attempts: 3                 │
│ Exponential backoff: 2x          │
│                                  │
│ Se falhar 3x → DLQ → Manual      │
└──────────────────────────────────┘
```

---

## 9. Fluxo de Segurança: JWT Token

```
┌──────────────────────────────────────────────────────────────┐
│              FLUXO: VALIDAÇÃO DE JWT TOKEN                   │
└──────────────────────────────────────────────────────────────┘

CLIENT REQUEST:
┌──────────────────────────────────────┐
│ GET /consultas                       │
│ Authorization: Bearer eyJhbGc...ABC  │
└──────────────────┬───────────────────┘
                   │
                   ▼
┌──────────────────────────────────────────────┐
│ Spring Security Filter Chain                │
│                                              │
│ 1. Extract token from header                 │
│    "Bearer eyJhbGc...ABC"                    │
│    Remove "Bearer " prefix                   │
│    Token: "eyJhbGc...ABC"                    │
│                                              │
│ 2. Decode JWT (Header.Payload.Signature)     │
│    Header: {                                 │
│      "alg": "HS256",                        │
│      "typ": "JWT"                           │
│    }                                         │
│                                              │
│    Payload: {                                │
│      "sub": "joao@example.com",             │
│      "role": "PACIENTE",                    │
│      "userId": 1,                           │
│      "iat": 1694595000,                     │
│      "exp": 1694598600                      │
│    }                                         │
│                                              │
│    Signature: "abc123..." (HMAC-SHA256)     │
│                                              │
│ 3. Validar signature                         │
│    HMAC-SHA256(                             │
│      message: "Header.Payload",             │
│      key: "dev-secret-key"                  │
│    ) == Signature                           │
│    Result: ✓ VÁLIDO                         │
│                                              │
│ 4. Verificar expiração                       │
│    exp: 1694598600 > agora: 1694595300      │
│    Result: ✓ NÃO EXPIRADO                   │
│                                              │
│ 5. Extrair claims                            │
│    email: "joao@example.com"                │
│    role: "PACIENTE"                         │
│    userId: 1                                │
│                                              │
│ 6. Criar Authentication object               │
│    Principal: User(1, "joao", "PACIENTE")  │
│    Authorities: [ROLE_PACIENTE]             │
│                                              │
│ 7. Armazenar em SecurityContext             │
│    SecurityContextHolder                    │
│      .getContext()                          │
│      .setAuthentication(auth)               │
│                                              │
└────────────────┬───────────────────────────┘
                 │
                 ▼ AUTORIZAÇÃO
┌──────────────────────────────────────────┐
│ @PreAuthorize("hasRole('PACIENTE')")     │
│                                          │
│ ✓ User tem role PACIENTE                 │
│ ✓ ACESSO PERMITIDO                       │
│                                          │
│ Requisição segue para controller         │
│                                          │
└──────────────────────────────────────────┘

FALHA - CENÁRIO 1: Token Inválido
┌───────────────────────────────────────┐
│ Authorization: Bearer invalid_token   │
│                                       │
│ 1. Tentar decodificar                 │
│ 2. JWT Malformado                     │
│ 3. Exceção: JwtException              │
│ 4. Response: 401 UNAUTHORIZED         │
│ {                                     │
│   "status": 401,                      │
│   "message": "JWT inválido",          │
│   "errorCode": "TOKEN_INVALIDO"       │
│ }                                     │
└───────────────────────────────────────┘

FALHA - CENÁRIO 2: Token Expirado
┌───────────────────────────────────────┐
│ Authorization: Bearer <old_token>     │
│ exp: 1694590000 (1h atrás)           │
│                                       │
│ 1. Decodificar: ✓                     │
│ 2. Validar signature: ✓               │
│ 3. Verificar expiração: ✗             │
│ 4. 1694590000 < agora (1694595300)    │
│ 5. Exceção: ExpiredJwtException       │
│ 6. Response: 401 UNAUTHORIZED         │
│ {                                     │
│   "status": 401,                      │
│   "message": "Token expirado",        │
│   "errorCode": "TOKEN_INVALIDO"       │
│ }                                     │
└───────────────────────────────────────┘

FALHA - CENÁRIO 3: Role Insuficiente
┌───────────────────────────────────────┐
│ @PreAuthorize("hasRole('ENFERMEIRO')")│
│ User.role = "PACIENTE"                │
│                                       │
│ 1. Token válido: ✓                    │
│ 2. Extrair role: "PACIENTE"           │
│ 3. Comparar com @PreAuthorize         │
│ 4. "PACIENTE" != "ENFERMEIRO"         │
│ 5. Exceção: AccessDeniedException    │
│ 6. Response: 403 FORBIDDEN            │
│ {                                     │
│   "status": 403,                      │
│   "message": "Acesso negado",         │
│   "errorCode": "ACESSO_NEGADO"        │
│ }                                     │
└───────────────────────────────────────┘
```

---

## 10. Deploy em Kubernetes (Futuro)

```
┌──────────────────────────────────────────────────────────────┐
│           KUBERNETES ARCHITECTURE (FUTURE)                   │
└──────────────────────────────────────────────────────────────┘

Namespace: tech-challenge-3
┌────────────────────────────────────────────────────────────┐
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Ingress Controller (Nginx)                           │  │
│  │ - External IP: LoadBalancer                          │  │
│  │ - HTTPS/TLS Termination                              │  │
│  │ - Path routing: /api/v1/*                            │  │
│  └─────────────────────┬────────────────────────────────┘  │
│                        │                                   │
│    ┌───────────────────┼───────────────────┐               │
│    │                   │                   │               │
│  ┌─▼──────┐         ┌──▼───┐           ┌──▼───┐           │
│  │Service: │         │Service         │Service            │
│  │Agendame │         │Notif           │RabbitM            │
│  │nto      │         │                │Q                  │
│  │Port:808 │         │Port:8081       │Port:5              │
│  │0        │         │(ClusterIP)     │672                │
│  │         │         │                │                   │
│  └─┬────┬──┘         └──┬──┬──┘        └───┬───┘           │
│    │    │              │  │                │               │
│  ┌─▼──┐┌─▼──┐       ┌──▼┐┌─▼──┐      ┌────▼─────┐        │
│  │Pod1││Pod2│       │Pod│Pod2 │      │Pod (1)   │        │
│  │Agd ││Agd │       │Ntf│Ntf  │      │RabbitMQ  │        │
│  └────┘└────┘       └───┘└────┘      └──────────┘        │
│  └───────────────┬───────┘              │                  │
│                  │                      │                  │
│                  └─────────┬────────────┘                  │
│                            │                              │
│          ┌──────────────────┼──────────────────┐            │
│          │                  │                  │            │
│      ┌───▼───┐          ┌───▼────┐      ┌─────▼─┐         │
│      │StateFul          │Service │      │Service             │
│      │Set: DB.          │SQL     │      │SQL             │
│      │Agd               │Ntf     │      │                │
│      │(postgres:16      │(post.  │      │                │
│      │pvc: 100Gi)       │gres:16 │      │                │
│      │                  │pvc:50Gi│      │                │
│      └──────────────────┘        └──────┘         │
│                                                   │
│  ┌───────────────────────────────────────────┐  │
│  │ ConfigMap + Secrets                       │  │
│  │ - DB Credentials (Secret)                 │  │
│  │ - JWT Secret (Secret)                     │  │
│  │ - App Config (ConfigMap)                  │  │
│  │ - RabbitMQ Config (ConfigMap)             │  │
│  └───────────────────────────────────────────┘  │
│                                                  │
│  ┌───────────────────────────────────────────┐  │
│  │ Monitoring & Logging                      │  │
│  │ - Prometheus (Metrics)                    │  │
│  │ - Grafana (Dashboards)                    │  │
│  │ - ELK Stack (Logs)                        │  │
│  │ - Jaeger (Tracing)                        │  │
│  └───────────────────────────────────────────┘  │
│                                                  │
└──────────────────────────────────────────────────┘

Helm Charts:
- tech-challenge3/Chart.yaml
- tech-challenge3/values.yaml
- tech-challenge3/templates/
  ├── deployment.yaml
  ├── service.yaml
  ├── ingress.yaml
  ├── configmap.yaml
  └── secret.yaml

Deploy:
$ helm install tech-challenge3 ./helm-charts
$ helm upgrade tech-challenge3 ./helm-charts
```

---

## 11. CI/CD Pipeline

```
┌────────────────────────────────────────────────────────┐
│              GITHUB ACTIONS CI/CD PIPELINE            │
└────────────────────────────────────────────────────────┘

Trigger Events:
├─ push (qualquer branch)
├─ pull_request (qualquer branch)
└─ schedule (cron - opcional)

┌─────────────────────────────────────────────────────┐
│ Job: Build & Test                                   │
│ Runner: ubuntu-latest (Docker disponível)           │
│ Timeout: 30 minutos                                 │
│                                                     │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 1: Checkout                               │ │
│ │ - actions/checkout@v4                          │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 2: Setup JDK                              │ │
│ │ - Java version: 21                             │ │
│ │ - Distribution: temurin                        │ │
│ │ - Cache: maven (~/.m2)                         │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 3: Chmod mvnw                             │ │
│ │ - chmod +x ./mvnw                              │ │
│ │ (necessário por ser repo Windows)              │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 4: Compile & Test                         │ │
│ │ - ./mvnw -B verify                             │ │
│ │ - Executa:                                     │ │
│ │   ├─ compile                                   │ │
│ │   ├─ test (unit + integration)                 │ │
│ │   ├─ package                                   │ │
│ │   └─ jacoco:report (cobertura)                 │ │
│ │ - Testcontainers para BD/RabbitMQ              │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 5: Upload JaCoCo Reports                  │ │
│ │ - actions/upload-artifact@v4                   │ │
│ │ - Destino: jacoco-reports                      │ │
│ │ - Caminho: */target/site/jacoco/               │ │
│ │ - if: always() (mesmo se falhar)               │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
│ ┌────────────────────────────────────────────────┐ │
│ │ Step 6: Upload Surefire Reports                │ │
│ │ - actions/upload-artifact@v4                   │ │
│ │ - Destino: surefire-reports                    │ │
│ │ - Caminho: */target/surefire-reports/          │ │
│ │ - if: always() (mesmo se falhar)               │ │
│ └────────────────────────────────────────────────┘ │
│                                                    │
└─────────────────────────────────────────────────────┘

Build Status:
┌─────────────────┐
│  ✓ Compile OK   │ Duration: 2-3 min
│  ✓ Tests OK     │ Coverage: 80%+
│  ✓ Package OK   │ Artifacts: JAR + POM
│  ✓ Reports OK   │ Stored: GitHub Artifacts
└─────────────────┘

Deploy (Future):
After successful tests:
├─ Build Docker images
├─ Push to Container Registry
├─ Deploy to Staging
├─ Run E2E tests
└─ Deploy to Production (manual approval)
```

---

## Referências para Visualização

Para melhor visualizar estes diagramas:

1. **Draw.io Online**: https://draw.io
   - Copiar ASCII art
   - Converter para diagrama visual

2. **PlantUML**: https://plantuml.com
   - Renderizar diagramas UML
   - Gerar imagens PNG/SVG

3. **Mermaid**: https://mermaid.live
   - Diagramas em markdown
   - Live editor com preview

4. **Lucidchart**: https://lucidchart.com
   - Criação profissional
   - Sharing e collaboration

---

**Última atualização:** 2026-09-12
