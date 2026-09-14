# Documentação de Arquitetura - Tech Challenge 3

**Sistema de Agendamento e Notificação de Consultas Médicas**

**Versão:** 1.0.0  
**Data:** 2026-09-12  
**Contexto:** Tech Challenge Fase 3 - Pós-Graduação FIAP

---

## Índice

1. [Visão Geral](#visão-geral)
2. [Contexto de Negócio](#contexto-de-negócio)
3. [Arquitetura C4](#arquitetura-c4)
4. [Arquitetura de Componentes](#arquitetura-de-componentes)
5. [Padrões de Comunicação](#padrões-de-comunicação)
6. [Modelo de Dados](#modelo-de-dados)
7. [Decisões Arquiteturais (ADRs)](#decisões-arquiteturais)
8. [Fluxo de Negócio](#fluxo-de-negócio)
9. [Considerações de Segurança](#considerações-de-segurança)
10. [Escalabilidade e Resiliência](#escalabilidade-e-resiliência)

---

## Visão Geral

O sistema **Tech Challenge 3** é uma plataforma de agendamento e notificação de consultas médicas baseada em **arquitetura de microserviços**. A solução é composta por dois serviços independentes que se comunicam através de **filas de mensagens (RabbitMQ)**, garantindo desacoplamento, escalabilidade e resiliência.

### Características Principais

- **Microserviços Desacoplados**: Dois serviços independentes com responsabilidades bem definidas
- **Comunicação Assíncrona**: RabbitMQ para troca de eventos entre serviços
- **Segurança**: Autenticação JWT e controle de acesso baseado em roles
- **Persistência**: PostgreSQL com schemas isolados por serviço
- **Containerização**: Docker Compose para orquestração local
- **CI/CD**: Pipeline GitHub Actions com testes automatizados

---

## Contexto de Negócio

### Domínio

O sistema gerencia consultas médicas em um ambiente hospitalar/clínico, onde:

- **Médicos** podem agendar, editar e consultar agendamentos
- **Enfermeiros** podem registrar novas consultas
- **Pacientes** podem visualizar seus próprios agendamentos
- **Notificações** são enviadas automaticamente quando consultas são criadas ou editadas

### Atores

| Ator | Responsabilidades |
|------|-------------------|
| **Médico** (ROLE_MEDICO) | Editar consultas, visualizar todas as consultas, atender consultas |
| **Enfermeiro** (ROLE_ENFERMEIRO) | Registrar novas consultas, visualizar todas as consultas |
| **Paciente** (ROLE_PACIENTE) | Visualizar apenas suas próprias consultas, receber notificações |

---

## Arquitetura C4

### Nível 1: Contexto do Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                   Sistema de Consultas                      │
│          (Tech Challenge 3 - Agendamento)                   │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Serviço de Agendamento + Serviço de Notificações   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
              ↑                              ↓
        [Usuários]                    [Notificações]
     (Médicos, Enfermeiros,          (Email/Sistema)
      Pacientes)
```

### Nível 2: Container

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Container Ecosystem                              │
│                                                                          │
│  ┌─────────────────────────┐  ┌──────────────────┐  ┌──────────────────┐│
│  │ Serviço de Agendamento  │  │  Message Broker  │  │ Serviço Notif.   ││
│  │    (Port 8080)          │  │  RabbitMQ        │  │  (Port 8081)     ││
│  │                         │  │  (Port 5672)     │  │                  ││
│  │ • Auth (JWT)            │  │                  │  │ • Listener       ││
│  │ • Consultas (CRUD)      │  │                  │  │ • Lembrete Send  ││
│  │ • Event Publisher       │  │                  │  │ • Event Consumer ││
│  └────────────┬────────────┘  └──────────────────┘  └────────┬─────────┘│
│               │                      ↑↑↓                     │          │
│               │                   Eventos                    │          │
│  ┌────────────▼──────────────┐                  ┌────────────▼─────────┐│
│  │  DB Agendamento           │                  │  DB Notificação      ││
│  │  PostgreSQL 5432          │                  │  PostgreSQL 5433     ││
│  │                           │                  │                      ││
│  │ • users                   │                  │ • notificacoes       ││
│  │ • consultas               │                  │                      ││
│  └───────────────────────────┘                  └──────────────────────┘│
│                                                                          │
└──────────────────────────────────────────────────────────────────────────┘
```

### Nível 3: Componentes (Serviço de Agendamento)

```
┌──────────────────────────────────────────────────────────────┐
│            Serviço de Agendamento (Hexagonal)               │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Web Layer (REST Controllers)              │ │
│  │                                                        │ │
│  │  • AuthController        • ConsultaController         │ │
│  │    - POST /auth/register   - POST /consultas          │ │
│  │    - POST /auth/login      - PUT /consultas/{id}      │ │
│  │                            - GET /consultas           │ │
│  │                            - GET /consultas/{id}      │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↓                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │           Application Layer (Use Cases)                │ │
│  │                                                        │ │
│  │  • LoginUseCase          • RegistrarConsultaUseCase   │ │
│  │  • RegisterUserUseCase   • EditarConsultaUseCase      │ │
│  │                          • ListarConsultasUseCase     │ │
│  │                          • BuscarConsultaUseCase      │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↓                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │            Domain Layer (Business Logic)               │ │
│  │                                                        │ │
│  │  • User (Entity)         • Role (Enum)                │ │
│  │  • Consulta (Entity)     • StatusConsulta (Enum)      │ │
│  │  • Exceptions                                         │ │
│  └────────────────────────────────────────────────────────┘ │
│                          ↓                                   │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         Infrastructure Layer (Adapters)                │ │
│  │                                                        │ │
│  │  • JPA Repositories      • RabbitMQ Publisher         │ │
│  │  • Security (JWT)        • Health Checks              │ │
│  │  • Password Encoder                                   │ │
│  └────────────────────────────────────────────────────────┘ │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

---

## Arquitetura de Componentes

### Serviço de Agendamento

| Componente | Responsabilidade | Tecnologia |
|------------|-----------------|-----------|
| **AuthController** | Endpoints de autenticação | Spring REST |
| **ConsultaController** | Endpoints de gerenciamento de consultas | Spring REST |
| **LoginUseCase** | Lógica de login | Spring Security + JWT |
| **RegisterUserUseCase** | Lógica de registro | Bcrypt Password Encoder |
| **RegistrarConsultaUseCase** | Criar nova consulta | Hexagonal Architecture |
| **EditarConsultaUseCase** | Editar consulta existente | Hexagonal Architecture |
| **ListarConsultasUseCase** | Listar consultas do usuário | Hexagonal Architecture |
| **BuscarConsultaUseCase** | Buscar consulta por ID | Hexagonal Architecture |
| **ConsultaRepository** | Persistência de consultas | Spring Data JPA |
| **UserRepository** | Persistência de usuários | Spring Data JPA |
| **ConsultaEventPublisher** | Publicação de eventos | Spring AMQP |
| **TokenService** | Gerenciamento de tokens JWT | JJWT |

### Serviço de Notificações

| Componente | Responsabilidade | Tecnologia |
|------------|-----------------|-----------|
| **NotificacaoListener** | Consome eventos de consulta | Spring AMQP @RabbitListener |
| **EnviarLembreteConsultaUseCase** | Lógica de envio de notificação | Hexagonal Architecture |
| **NotificacaoRepository** | Persistência de notificações | Spring Data JPA |
| **NotificacaoRabbitConfig** | Configuração de filas/exchanges | Spring AMQP |

---

## Padrões de Comunicação

### Fluxo de Eventos (RabbitMQ)

#### 1. Criação de Consulta

```
┌─────────────────────────────────────────────────────────────┐
│              Fluxo: Criar Nova Consulta                     │
└─────────────────────────────────────────────────────────────┘

1. Cliente                    2. Agendamento Service
   │                                  │
   ├─ POST /consultas                 │
   │  (ConsultaRequest)               │
   └─────────────────────────────────►│
                                      │
                         3. RegistrarConsultaUseCase
                            - Validar dados
                            - Verificar autorização (ENFERMEIRO)
                            - Persistir no banco
                                      │
                         4. Publicar Evento
                            ConsultaCriadaEvent
                                      │
                                      ├─ consultaId
                                      ├─ pacienteId
                                      ├─ pacienteNome
                                      ├─ pacienteEmail
                                      ├─ medicoId
                                      └─ dataHora
                                      │
                                      ▼
                            5. RabbitMQ Exchange
                               "consultas.exchange"
                                      │
                                      ▼
                            6. Rota para Fila
                               "consultas.notificacoes.queue"
                                      │
                         7. Notificacao Service
                            NotificacaoListener
                                      │
                         8. EnviarLembreteConsultaUseCase
                            - Processar evento
                            - Criar notificação
                            - Persistir no banco
                            - Enviar notificação (mock)
```

#### 2. Edição de Consulta

Mesmo fluxo, mas publica `ConsultaEditadaEvent` com status da consulta.

### Contrato de Eventos

#### ConsultaCriadaEvent

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

**Rota**: `consultas.event.created`

#### ConsultaEditadaEvent

```json
{
  "consultaId": 1,
  "pacienteId": 10,
  "pacienteNome": "João Silva",
  "pacienteEmail": "joao@example.com",
  "medicoId": 20,
  "dataHora": "2026-09-25T14:30:00",
  "status": "CONFIRMADA"
}
```

**Rota**: `consultas.event.edited`

### Configuração RabbitMQ

```
Exchange Type: Topic
├─ Exchange Name: "consultas.exchange"
│
├─ Queue 1: "consultas.notificacoes.queue"
│  └─ Routing Key: "consultas.event.*"
│     └─ Consumers: NotificacaoListener (Serviço de Notificações)
```

---

## Modelo de Dados

### Serviço de Agendamento

#### Tabela: `users`

| Campo | Tipo | Constraints | Descrição |
|-------|------|------------|-----------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `nome` | VARCHAR(255) | NOT NULL | Nome completo do usuário |
| `email` | VARCHAR(255) | UNIQUE, NOT NULL | Email único |
| `senha` | VARCHAR(255) | NOT NULL | Senha criptografada (Bcrypt) |
| `role` | VARCHAR(20) | NOT NULL | Função (MEDICO, ENFERMEIRO, PACIENTE) |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Data de criação |
| `updated_at` | TIMESTAMP | DEFAULT NOW() | Data de atualização |

#### Tabela: `consultas`

| Campo | Tipo | Constraints | Descrição |
|-------|------|------------|-----------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `paciente_id` | BIGINT | FK → users.id | Referência ao paciente |
| `medico_id` | BIGINT | FK → users.id | Referência ao médico |
| `data_hora` | TIMESTAMP | NOT NULL | Data e hora da consulta |
| `observacoes` | TEXT | | Observações adicionais |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'AGENDADA' | Status da consulta |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Data de criação |
| `updated_at` | TIMESTAMP | DEFAULT NOW() | Data de atualização |

#### Diagrama ER - Serviço Agendamento

```
┌─────────────────────┐
│      users          │
├─────────────────────┤
│ id (PK) ●───┐       │
│ nome        │       │
│ email       │       │
│ senha       │       │
│ role        │       │
│ created_at  │       │
│ updated_at  │       │
└─────────────────────┘
        ↑
        │
        │ 1:N
        │
    ┌───┴──────────────────┐
    │                      │
    │   ┌─────────────────────────┐
    │   │    consultas            │
    │   ├─────────────────────────┤
    │   │ id (PK)                 │
    │   │ paciente_id (FK) ────────┼──→ users.id
    │   │ medico_id (FK) ──────────┼──→ users.id
    │   │ data_hora               │
    │   │ observacoes             │
    │   │ status                  │
    │   │ created_at              │
    │   │ updated_at              │
    │   └─────────────────────────┘
    │
    └──→ users.id (paciente_id ou medico_id)
```

### Serviço de Notificações

#### Tabela: `notificacoes`

| Campo | Tipo | Constraints | Descrição |
|-------|------|------------|-----------|
| `id` | BIGSERIAL | PRIMARY KEY | Identificador único |
| `consulta_id` | BIGINT | NOT NULL, INDEX | ID da consulta (referência) |
| `paciente_id` | BIGINT | NOT NULL, INDEX | ID do paciente |
| `destinatario` | VARCHAR(255) | NOT NULL | Email do destinatário |
| `tipo` | VARCHAR(30) | NOT NULL | Tipo (CONSULTA_CRIADA, CONSULTA_EDITADA) |
| `mensagem` | TEXT | NOT NULL | Conteúdo da notificação |
| `status` | VARCHAR(20) | NOT NULL, DEFAULT 'PENDENTE' | Status (PENDENTE, ENVIADA, FALHA) |
| `data_envio` | TIMESTAMP | | Data do envio |
| `created_at` | TIMESTAMP | DEFAULT NOW() | Data de criação |
| `updated_at` | TIMESTAMP | DEFAULT NOW() | Data de atualização |

#### Diagrama ER - Serviço Notificação

```
┌──────────────────────────┐
│    notificacoes          │
├──────────────────────────┤
│ id (PK)                  │
│ consulta_id (INDEX)      │
│ paciente_id (INDEX)      │
│ destinatario             │
│ tipo                     │
│ mensagem                 │
│ status                   │
│ data_envio               │
│ created_at               │
│ updated_at               │
└──────────────────────────┘
        ↑
        │
   Eventos RabbitMQ
   (ConsultaCriadaEvent,
    ConsultaEditadaEvent)
```

---

## Decisões Arquiteturais

### ADR-001: Arquitetura de Microserviços

**Status:** ACEITO  
**Data:** 2026-09-12

#### Contexto

O sistema precisa gerenciar consultas médicas e notificações com alta disponibilidade e escalabilidade independente.

#### Decisão

Implementar uma **arquitetura de microserviços** com dois serviços independentes:
- Serviço de Agendamento (responsável por autenticação e gerenciamento de consultas)
- Serviço de Notificações (responsável por processar eventos e enviar notificações)

#### Consequências

**Positivas:**
- ✅ Escalabilidade independente: cada serviço pode escalar conforme sua carga
- ✅ Resiliência: falha em um serviço não afeta o outro
- ✅ Deploy independente: possibilita ciclos de release distintos
- ✅ Tecnologia agnóstica: cada serviço pode evoluir com techs diferentes

**Negativas:**
- ❌ Complexidade aumentada: necessita gerenciamento de distribuição
- ❌ Overhead de comunicação inter-serviços
- ❌ Debugging mais complexo em ambiente distribuído

---

### ADR-002: Comunicação Assíncrona via RabbitMQ

**Status:** ACEITO  
**Data:** 2026-09-12

#### Contexto

Serviços independentes precisam se comunicar sem acoplamento direto, permitindo que o Serviço de Notificações processe eventos de forma desacoplada.

#### Decisão

Usar **RabbitMQ** para comunicação baseada em **eventos** entre serviços:
- Padrão Publish/Subscribe com Topic Exchange
- Cada evento publica dados suficientes para o consumidor (não precisa consultar o outro serviço)

#### Consequências

**Positivas:**
- ✅ Desacoplamento temporal e lógico
- ✅ Escalabilidade: possibilita múltiplos consumidores
- ✅ Confiabilidade: mensagens são persistidas
- ✅ Resiliência: consumidor offline não perde eventos

**Negativas:**
- ❌ Garantia eventual consistency (não imediata)
- ❌ Overhead operacional: requer broker adicional
- ❌ Debugging mais complexo (rastrear eventos)

---

### ADR-003: Banco de Dados Isolado por Serviço

**Status:** ACEITO  
**Data:** 2026-09-12

#### Contexto

Cada microserviço necessita de dados específicos para sua operação sem dependência externa.

#### Decisão

Cada serviço mantém seu **próprio banco PostgreSQL**:
- Serviço Agendamento: `techchallenge` (porta 5432)
- Serviço Notificações: `techchallenge_notificacao` (porta 5433)

#### Consequências

**Positivas:**
- ✅ Autonomia: cada serviço controla sua persistência
- ✅ Escalabilidade: cada banco pode ter suas configurações
- ✅ Segurança: dados isolados com credenciais distintas
- ✅ Evita monolito distribuído

**Negativas:**
- ❌ Complexidade de queries cross-service
- ❌ Gerenciamento de múltiplos bancos
- ❌ Consistência distribuída (sagas necessárias em cenários complexos)

**Mitigação:** Eventos carregam dados suficientes para não necessitar acesso ao banco do outro serviço.

---

### ADR-004: Autenticação JWT + Spring Security

**Status:** ACEITO  
**Data:** 2026-09-12

#### Contexto

Sistema necessita autenticação segura e autorização baseada em roles sem estado (stateless).

#### Decisão

Implementar autenticação via **JWT (JSON Web Tokens)** com **Spring Security**:
- Token gerado no login
- Validado em cada requisição
- Contém claims com role do usuário
- Assinado com chave secreta

#### Consequências

**Positivas:**
- ✅ Stateless: não requer sessão no servidor
- ✅ Escalável: funciona com múltiplas instâncias
- ✅ Seguro: token assinado e validado
- ✅ Suporta autorização granular (roles)

**Negativas:**
- ❌ Token revogação requer cache adicional
- ❌ Expiração fixa (1 hora)
- ❌ Logout não é imediato

---

### ADR-005: Padrão Hexagonal (Ports & Adapters)

**Status:** ACEITO  
**Data:** 2026-09-12

#### Contexto

Código deve ser independente de frameworks, testável e com regra de negócio isolada.

#### Decisão

Implementar **Arquitetura Hexagonal** em ambos os serviços:
- **Core**: Domain (modelos puros, sem annotations)
- **Application Layer**: Use Cases (lógica de negócio)
- **Adapters**: Controllers, Repositories, Publishers (implementações específicas)
- **Ports**: Interfaces de saída (UserRepository, TokenService, etc)

#### Consequências

**Positivas:**
- ✅ Testabilidade: lógica de negócio isolada de frameworks
- ✅ Manutenibilidade: mudanças de framework não afetam core
- ✅ Reusabilidade: use cases podem ser testados isoladamente
- ✅ Clareza: fluxo de dados bem definido

**Negativas:**
- ❌ Mais classes/interfaces inicialmente
- ❌ Overhead de mapeamentos (domain → JPA → DTO)

---

## Fluxo de Negócio

### Fluxo 1: Registro e Login

```
┌─────────────────┐
│  Cliente REST   │
└────────┬────────┘
         │
         ├─ POST /auth/register
         │  { nome, email, senha, role }
         │
         ▼
┌──────────────────────────────────────────────┐
│  AuthController.register()                   │
│  └─ RegisterUserUseCase.execute()            │
│     ├─ Validar entrada                       │
│     ├─ Verificar email único (exception)     │
│     ├─ Hash senha com Bcrypt                 │
│     ├─ Criar User + salvar DB                │
│     └─ Gerar JWT                             │
│                                              │
│  Response: 201 CREATED                       │
│  { token, tokenType: "Bearer" }              │
└──────────────────────────────────────────────┘
         │
         │
         ├─ POST /auth/login
         │  { email, senha }
         │
         ▼
┌──────────────────────────────────────────────┐
│  AuthController.login()                      │
│  └─ LoginUseCase.execute()                   │
│     ├─ Buscar usuário por email              │
│     ├─ Validar senha (exception se inválida) │
│     ├─ Gerar novo JWT                        │
│     └─ Retornar token                        │
│                                              │
│  Response: 200 OK                            │
│  { token, tokenType: "Bearer" }              │
└──────────────────────────────────────────────┘
```

### Fluxo 2: Criar Consulta (Com Notificação)

```
┌─────────────────────────────────────────────┐
│  Cliente REST (Enfermeiro)                  │
│                                             │
│  POST /consultas                            │
│  Authorization: Bearer <token>              │
│  { pacienteId, medicoId, dataHora, obs }   │
└────────────────┬────────────────────────────┘
                 │
                 ▼
         ┌───────────────────────┐
         │ Spring Security       │
         │ └─ Validar JWT        │
         │ └─ Verificar ENFERMEIRO │
         └───────────┬───────────┘
                     │
                     ▼
    ┌────────────────────────────────────────────┐
    │  ConsultaController.registrar()            │
    └────────────┬───────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────────────────────────────┐
    │  RegistrarConsultaUseCase.execute()                        │
    │  ├─ Validar (pacienteId, medicoId existem)                │
    │  ├─ Validar dataHora (não pode ser no passado)            │
    │  ├─ Criar Consulta (status = AGENDADA)                    │
    │  ├─ Persistir no DB: users (paciente_id) x users (medico) │
    │  │                                                         │
    │  ├─ Response: 201 CREATED                                  │
    │  │ { id, paciente, medico, dataHora, status }             │
    │  │                                                         │
    │  └─ [ASYNC] Publicar ConsultaCriadaEvent                  │
    └────────────┬───────────────────────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────────────┐
    │  ConsultaEventPublisher.publish()          │
    │  ├─ Criar payload:                         │
    │  │  {                                      │
    │  │    consultaId: 1,                       │
    │  │    pacienteId: 10,                      │
    │  │    pacienteNome: "João Silva",          │
    │  │    pacienteEmail: "joao@example.com",   │
    │  │    medicoId: 20,                        │
    │  │    dataHora: "2026-09-25T14:30:00"      │
    │  │  }                                      │
    │  │                                         │
    │  └─ Publicar no RabbitMQ exchange          │
    │     com routing key: consultas.event.created
    └────────────┬───────────────────────────────┘
                 │
                 ▼ (Outro Serviço)
    ┌────────────────────────────────────────────┐
    │  Serviço de Notificações                   │
    │  RabbitMQ: Fila recebe evento              │
    │  └─ NotificacaoListener.receberConsultaCriada()
    │     ├─ Extrair dados do evento            │
    │     ├─ Chamar EnviarLembreteConsultaUseCase
    │     │                                     │
    │     ├─ Usar Destinatario(nome, email)    │
    │     ├─ Tipo: CONSULTA_CRIADA              │
    │     │                                     │
    │     └─ Executar Use Case                  │
    └────────────┬───────────────────────────────┘
                 │
                 ▼
    ┌────────────────────────────────────────────┐
    │  EnviarLembreteConsultaUseCase.execute()   │
    │  ├─ Construir mensagem de notificação     │
    │  ├─ Criar Notificacao (status = PENDENTE) │
    │  ├─ Persistir no DB notificacoes          │
    │  │                                         │
    │  ├─ Enviar notificação (mock/email)       │
    │  ├─ Atualizar status: ENVIADA             │
    │  │                                         │
    │  └─ Log: "Notificação enviada com sucesso"
    └────────────────────────────────────────────┘
```

### Fluxo 3: Listar Consultas (Com Autorização)

```
┌──────────────────────────────────────────┐
│  Cliente REST                            │
│                                          │
│  GET /consultas                          │
│  Authorization: Bearer <token_paciente>  │
└────────────┬─────────────────────────────┘
             │
             ▼
     ┌───────────────────────────────┐
     │ Spring Security               │
     │ └─ Validar JWT                │
     │ └─ hasAnyRole(MEDICO, ENFERMEIRO, PACIENTE) │
     │ └─ @AuthenticationPrincipal User
     └───────────┬───────────────────┘
                 │
                 ▼
    ┌──────────────────────────────────────────┐
    │  ConsultaController.listar()             │
    │  (user = User autenticado)               │
    │                                          │
    │  └─ ListarConsultasUseCase.execute(user) │
    └───────────┬──────────────────────────────┘
                │
                ▼
    ┌────────────────────────────────────────────────┐
    │  ListarConsultasUseCase                        │
    │                                                │
    │  if (user.role == PACIENTE)                    │
    │     return consultas where paciente_id = user.id
    │                                                │
    │  if (user.role == MEDICO || ENFERMEIRO)       │
    │     return todas as consultas                  │
    │                                                │
    │  Response: 200 OK                              │
    │  [ { id, paciente, medico, dataHora, status } ]
    └────────────────────────────────────────────────┘
```

---

## Considerações de Segurança

### Autenticação

✅ **Implementado:**
- JWT com assinatura HMAC-SHA256
- Senha criptografada com Bcrypt
- Expiração de token (1 hora)
- Validação de email único

⚠️ **Considerar em Produção:**
- HTTPS/TLS para todas as comunicações
- Refresh tokens (separado de access token)
- Rate limiting em endpoints de login/registro
- Auditoria de logins

### Autorização

✅ **Implementado:**
- Validação de role em @PreAuthorize
- Controle granular por operação:
  - `hasRole('ENFERMEIRO')` para criar consulta
  - `hasRole('MEDICO')` para editar consulta
  - `hasAnyRole('MEDICO', 'ENFERMEIRO', 'PACIENTE')` para listar

⚠️ **Considerar:**
- Validar que paciente só acessa suas próprias consultas
- Validar que médico não edita consulta de outro médico

### Dados em Trânsito

✅ **RabbitMQ:**
- Usa AMQP nativo (port 5672)
- Em produção: implementar TLS/AMQPS

⚠️ **Em Produção:**
- Encriptar payload de eventos se contiver dados sensíveis
- Validar origem de mensagens (autenticação do broker)

### Dados em Repouso

✅ **Banco de Dados:**
- PostgreSQL com credenciais por serviço
- Senhas criptografadas em Bcrypt

⚠️ **Considerar:**
- Encriptação de colunas sensíveis (email em notificações)
- Backups encriptados
- Políticas de retenção de dados

---

## Escalabilidade e Resiliência

### Escalabilidade Horizontal

#### Serviço de Agendamento

```
┌─────────────────────────────────────────┐
│         Load Balancer                   │
│         (Nginx / AWS ALB)               │
└────────────┬────────────────────────────┘
             │
    ┌────────┼────────┬────────────┐
    │        │        │            │
    ▼        ▼        ▼            ▼
┌───────┐┌───────┐┌───────┐    ┌───────┐
│ Agd#1 ││ Agd#2 ││ Agd#3 │... │ Agd#N │
│:8080  ││:8080  ││:8080  │    │:8080  │
└───┬───┘└───┬───┘└───┬───┘    └───┬───┘
    │        │        │            │
    └────────┼────────┼────────────┘
             │
             ▼
        ┌──────────────────┐
        │ PostgreSQL       │
        │ (Conexão Pooled) │
        └──────────────────┘
```

**Benefícios:**
- Cada instância processa requisições independentemente
- Connection pooling no banco
- RabbitMQ distribui eventos entre instâncias

#### Serviço de Notificações

```
┌──────────────────────────────────────────┐
│         RabbitMQ                         │
│         Fila: consultas.notificacoes.q  │
└───┬──────────────────────────────┬───────┘
    │                              │
    ▼                              ▼
┌─────────────┐            ┌─────────────┐
│ Notif Cons#1│            │ Notif Cons#2│
│ Instance:8081│            │ Instance:8081│
└─────────────┘            └─────────────┘

Cada consumidor processa uma mensagem por vez
(Fair Dispatch com QoS = 1)
```

**Benefícios:**
- Múltiplos consumidores paralelos
- Auto-scaling baseado no tamanho da fila
- Resiliência: se um cai, outro processa

### Resiliência

#### Circuit Breaker (Consideração Futura)

```
┌──────────────────────────────────────────┐
│  Serviço A → Serviço B                   │
│                                          │
│  Pico de erros detectado                 │
│  └─ Circuit Breaker ABRE                 │
│     └─ Requisições falharem rápido       │
│     └─ Evita cascata de erros            │
│                                          │
│  Após tempo, tenta novamente (HALF-OPEN) │
└──────────────────────────────────────────┘

Recomendação: Usar Resilience4j + Actuator
```

#### Retry & Dead Letter Queue

```
┌─────────────────────────────────────────┐
│  RabbitMQ                               │
│  ├─ Queue Principal                      │
│  │  └─ Consumer falha 3x                 │
│  │                                       │
│  └─ Dead Letter Exchange (DLX)           │
│     └─ DLQ (Dead Letter Queue)           │
│        └─ Manual inspection + retry      │
└─────────────────────────────────────────┘
```

**Configuração Futura:**
```properties
spring.rabbitmq.listener.simple.retry.enabled=true
spring.rabbitmq.listener.simple.retry.max-attempts=3
spring.rabbitmq.listener.simple.retry.initial-interval=1000
spring.rabbitmq.listener.simple.retry.multiplier=2
```

#### Health Checks

✅ **Implementado:**
```properties
GET /actuator/health
├─ diskSpace: UP
├─ db: UP
└─ rabbit: UP (ou DOWN)
```

**Docker Compose:**
```yaml
healthcheck:
  test: ["CMD-SHELL", "pg_isready -U user -d db"]
  interval: 5s
  retries: 5
```

### Observabilidade

#### Logging Estruturado

**Recomendação:** Implementar ELK Stack (Elasticsearch, Logstash, Kibana)

```properties
logging.level.root=INFO
logging.level.com.fiap.challenge=DEBUG
logging.pattern.console=%d{HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

#### Métricas (Micrometer + Prometheus)

**Endpoints:**
- `GET /actuator/metrics`
- `GET /actuator/metrics/http.server.requests`

**Recomendação:** Integrar com Prometheus + Grafana

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### Distributed Tracing (Jaeger/Zipkin)

**Recomendação:** Usar Spring Cloud Sleuth + Jaeger

```properties
spring.sleuth.sampler.probability=1.0
spring.zipkin.base-url=http://localhost:9411
```

---

## Conclusão

Este documento apresenta uma arquitetura **moderna, escalável e resiliente** baseada em **microserviços**:

- ✅ Separação clara de responsabilidades
- ✅ Comunicação desacoplada via eventos
- ✅ Segurança com JWT + Spring Security
- ✅ Isolamento de dados por serviço
- ✅ Testabilidade via Arquitetura Hexagonal
- ✅ Escalabilidade horizontal
- ✅ Resiliência com RabbitMQ

A arquitetura está pronta para suportar crescimento futuro com adição de novos serviços, consumers e funcionalidades.

---

**Referências:**

1. Domain-Driven Design (Eric Evans)
2. Building Microservices (Sam Newman)
3. Hexagonal Architecture (Alistair Cockburn)
4. Event Sourcing & CQRS (Greg Young)
5. Spring Boot Documentation: https://spring.io/projects/spring-boot
6. RabbitMQ Documentation: https://www.rabbitmq.com/documentation.html
