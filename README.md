# Tech Challenge 3 - Sistema de Agendamento e Notificação de Consultas Médicas

[![CI Pipeline](https://github.com/MatheusMartinsR/tech-challenge3/workflows/CI/badge.svg)](https://github.com/MatheusMartinsR/tech-challenge3/actions)
[![Java Version](https://img.shields.io/badge/Java-17+-ED8B00?logo=java&logoColor=white)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)

## 📋 Índice

1. [Descrição](#descrição)
2. [Contexto do Projeto](#contexto-do-projeto)
3. [Arquitetura](#arquitetura)
4. [Pré-Requisitos](#pré-requisitos)
5. [Instalação e Configuração](#instalação-e-configuração)
6. [Execução](#execução)
7. [Estrutura do Projeto](#estrutura-do-projeto)
8. [Serviços e Portas](#serviços-e-portas)
9. [API Documentation](#api-documentation)
10. [Testes](#testes)
11. [Troubleshooting](#troubleshooting)
12. [Integrantes e Responsabilidades](#integrantes-e-responsabilidades)

---

## 📝 Descrição

**Tech Challenge 3** é uma plataforma de **agendamento de consultas médicas** e **notificação automática** baseada em **arquitetura de microserviços**. O sistema gerencia:

- ✅ Autenticação segura com JWT
- ✅ Gerenciamento de consultas (criar, editar, listar)
- ✅ Notificações automáticas via eventos
- ✅ Controle de acesso baseado em roles (MEDICO, ENFERMEIRO, PACIENTE)
- ✅ Comunicação assíncrona via RabbitMQ
- ✅ Persistência de dados com PostgreSQL
- ✅ Containerização com Docker

### Principais Características

| Feature | Descrição |
|---------|-----------|
| **Microserviços** | Dois serviços independentes e desacoplados |
| **API REST** | Endpoints bem documentados com validações |
| **Segurança** | JWT + Spring Security com autorização por roles |
| **Comunicação Assíncrona** | RabbitMQ Topic Exchange com eventos |
| **Banco de Dados** | PostgreSQL isolado por serviço |
| **Docker Compose** | Stack completa local em um comando |
| **CI/CD** | GitHub Actions com testes automatizados |
| **Code Quality** | JaCoCo para cobertura de testes |

---

## 🎓 Contexto do Projeto

### Fase 3 - Pós-Graduação FIAP

Este projeto é o **desafio técnico de fase 3** de um curso de pós-graduação em **Arquitetura de Software** da **FIAP**. Os objetivos incluem:

1. **Aplicar padrões de design** e arquitetura profissional
2. **Implementar microserviços** desacoplados e escaláveis
3. **Demonstrar comunicação assíncrona** entre serviços
4. **Garantir qualidade de código** com testes e cobertura
5. **Documentar arquitetura** e decisões técnicas
6. **Implementar segurança** em todas as camadas

---

## 🏗️ Arquitetura

### Diagrama de Alto Nível

```
┌─────────────────────────────────────────────────────┐
│                  Cliente REST                       │
│         (Web, Mobile, Postman, etc)                │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────▼────────────┐
         │  API Gateway (Nginx)   │
         │  Load Balancer         │
         └───────────┬────────────┘
                     │
         ┌───────────┴────────────────────────────┐
         │                                        │
    ┌────▼─────────────┐         ┌──────────────▼─────┐
    │ Serviço          │         │ Serviço            │
    │ Agendamento      │         │ Notificações       │
    │ (Port 8080)      │         │ (Port 8081)        │
    │                  │         │                    │
    │ • Auth JWT       │         │ • Event Listener   │
    │ • CRUD Consultas │         │ • Send Reminders   │
    │ • Publish Events │         │ • Persist Notif.   │
    └────┬─────────────┘         └────┬───────────────┘
         │                            │
         │        ┌──────────┐        │
         │        │ RabbitMQ │        │
         └───────►│ Broker   │◄───────┘
                  │ (AMQP)   │
                  └──────┬───┘
                         │
         ┌───────────────┴───────────────┐
         │                               │
    ┌────▼─────────────┐      ┌──────────▼─────┐
    │ PostgreSQL       │      │ PostgreSQL      │
    │ Agendamento      │      │ Notificação     │
    │ (Port 5432)      │      │ (Port 5433)     │
    │                  │      │                 │
    │ • users          │      │ • notificacoes  │
    │ • consultas      │      │                 │
    └──────────────────┘      └─────────────────┘
```

### Padrões Utilizados

- **Hexagonal Architecture**: Separação clara entre camadas
- **Domain-Driven Design**: Modelos de domínio ricos
- **Event Sourcing**: Eventos como source of truth
- **CQRS**: Separação de leitura e escrita (implícita)
- **Repository Pattern**: Abstração de persistência
- **Dependency Injection**: Loose coupling com Spring

---

## 📦 Pré-Requisitos

### Obrigatório

- **Java 17+** ([Download OpenJDK](https://adoptopenjdk.net/))
- **Maven 3.8+** ([Download Maven](https://maven.apache.org/download.cgi)) ou use `./mvnw`
- **Docker & Docker Compose** ([Download Docker Desktop](https://www.docker.com/products/docker-desktop))
- **Git** ([Download Git](https://git-scm.com/downloads))

### Verificar Instalação

```bash
# Java
java -version
# Output: openjdk version "17.0.x" 2024-xx-xx

# Maven
mvn -version
# Output: Apache Maven 3.8.x

# Docker
docker --version
# Output: Docker version 24.0.x

# Docker Compose
docker compose version
# Output: Docker Compose version 2.x.x
```

### Opcional (Recomendado)

- **IDE**: VS Code, IntelliJ IDEA, ou Eclipse
- **Postman** ou **Insomnia** para testar endpoints
- **DBeaver** ou **pgAdmin** para gerenciar PostgreSQL

---

## 🚀 Instalação e Configuração

### 1. Clonar o Repositório

```bash
# Clone o repositório
git clone https://github.com/MatheusMartinsR/tech-challenge3.git

# Navegue para o diretório
cd tech-challenge3

# Checkout para o branch com todas as features
git checkout feature/notifications-and-infrastructure
```

### 2. Estrutura de Diretórios

```
tech-challenge3/
├── agendamento-service/          # Serviço de Agendamento
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fiap/challenge/techChallenge3/agendamento/
│   │   │   │   ├── web/controller/          # Controllers REST
│   │   │   │   ├── application/usecase/     # Use Cases
│   │   │   │   ├── domain/model/            # Entidades de Domínio
│   │   │   │   ├── infrastructure/          # Adapters (JPA, Security, etc)
│   │   │   │   └── AgendamentoApplication.java
│   │   │   └── resources/
│   │   │       └── application-*.properties
│   │   └── test/
│   └── pom.xml
├── notificacao-service/          # Serviço de Notificações
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/fiap/challenge/techChallenge3/notificacao/
│   │   │   │   ├── domain/model/            # Entidades de Domínio
│   │   │   │   ├── application/usecase/     # Use Cases
│   │   │   │   ├── infrastructure/messaging/# Listeners RabbitMQ
│   │   │   │   └── NotificacaoApplication.java
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── common/                       # Contrato de Eventos
│   ├── src/main/java/
│   │   └── com/fiap/challenge/techChallenge3/common/event/
│   │       ├── ConsultaCriadaEvent.java
│   │       ├── ConsultaEditadaEvent.java
│   │       └── StatusConsulta.java
│   └── pom.xml
├── docker-compose.yml            # Stack completo
├── .env                          # Variáveis de ambiente
├── pom.xml                       # POM pai (Maven Aggregator)
└── README.md                     # Este arquivo
```

### 3. Configurar Variáveis de Ambiente

O arquivo `.env` já está configurado para desenvolvimento:

```bash
# Verificar conteúdo (arquivo já versionado)
cat .env
```

**Conteúdo padrão:**

```properties
# PostgreSQL - Agendamento
POSTGRES_AGENDAMENTO_DB=techchallenge
POSTGRES_AGENDAMENTO_USER=techchallenge
POSTGRES_AGENDAMENTO_PASSWORD=techchallenge
POSTGRES_AGENDAMENTO_PORT=5432

# PostgreSQL - Notificação
POSTGRES_NOTIFICACAO_DB=techchallenge_notificacao
POSTGRES_NOTIFICACAO_USER=notificacao
POSTGRES_NOTIFICACAO_PASSWORD=notificacao
POSTGRES_NOTIFICACAO_PORT=5433

# RabbitMQ
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
RABBITMQ_PORT=5672
RABBITMQ_MANAGEMENT_PORT=15672

# Aplicações
AGENDAMENTO_PORT=8080
NOTIFICACAO_PORT=8081

# JWT
JWT_SECRET=dGVjaC1jaGFsbGVuZ2UtMy1kZXYtc2VjcmV0LWtleS1jaGFuZ2UtbWUtaW4tcHJvZHVjdGlvbg==
JWT_EXPIRATION_MS=3600000
```

> ⚠️ **Em Produção:** Mude `JWT_SECRET`, senhas de banco de dados e RabbitMQ!

---

## 🎯 Execução

### Opção 1: Docker Compose (Recomendado)

A maneira mais rápida de rodar tudo (serviços, bancos, RabbitMQ):

```bash
# Build e start de todos os serviços
docker compose up --build

# Output esperado:
# Creating techchallenge-postgres-agendamento ... done
# Creating techchallenge-postgres-notificacao ... done
# Creating techchallenge-rabbitmq ... done
# Creating techchallenge-agendamento ... done
# Creating techchallenge-notificacao ... done
# 
# agendamento-service_1  | Application started in 12.345 seconds
# notificacao-service_1  | Application started in 9.876 seconds
```

**Parar serviços:**

```bash
docker compose down
```

**Ver logs de um serviço específico:**

```bash
docker compose logs -f agendamento-service
docker compose logs -f notificacao-service
docker compose logs -f rabbitmq
```

### Opção 2: Executar Localmente (Sem Docker)

Se preferir rodar sem containers (requer instalação local de PostgreSQL e RabbitMQ):

#### 2a. Instalar Dependências Externas

```bash
# PostgreSQL (Mac via Homebrew)
brew install postgresql@16
brew services start postgresql@16

# PostgreSQL (Ubuntu)
sudo apt-get install postgresql postgresql-contrib
sudo service postgresql start

# RabbitMQ (Mac via Homebrew)
brew install rabbitmq
brew services start rabbitmq-server

# RabbitMQ (Ubuntu/Docker)
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3.13-management-alpine
```

#### 2b. Compilar Projeto

```bash
# Limpar e compilar
./mvnw clean install

# Ou com Maven instalado globalmente
mvn clean install
```

#### 2c. Criar Bancos de Dados

```bash
# Conectar ao PostgreSQL
psql -U postgres

# Criar bancos (no prompt psql)
CREATE DATABASE techchallenge;
CREATE DATABASE techchallenge_notificacao;

# Criar usuários
CREATE USER techchallenge WITH PASSWORD 'techchallenge';
CREATE USER notificacao WITH PASSWORD 'notificacao';

# Conceder permissões
GRANT ALL PRIVILEGES ON DATABASE techchallenge TO techchallenge;
GRANT ALL PRIVILEGES ON DATABASE techchallenge_notificacao TO notificacao;

# Sair
\q
```

#### 2d. Rodar Serviços

```bash
# Terminal 1: Serviço de Agendamento
cd agendamento-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8080"

# Terminal 2: Serviço de Notificações
cd notificacao-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

---

## 🔌 Serviços e Portas

### Serviços Disponíveis

| Serviço | URL | Descrição |
|---------|-----|-----------|
| **Agendamento Service** | http://localhost:8080 | API REST (autenticação, consultas) |
| **Notificação Service** | http://localhost:8081 | Processor de eventos (sem endpoints REST) |
| **RabbitMQ Admin** | http://localhost:15672 | Painel de gerenciamento (guest/guest) |
| **PostgreSQL Agendamento** | localhost:5432 | Banco de dados (techchallenge / techchallenge) |
| **PostgreSQL Notificação** | localhost:5433 | Banco de dados (techchallenge_notificacao / notificacao) |

### Health Checks

```bash
# Verificar saúde dos serviços
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health

# Response esperado:
# {"status":"UP"}
```

---

## 📚 API Documentation

### Endpoints Principais

#### Autenticação

```bash
# Registrar novo usuário
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "email": "joao@example.com",
    "senha": "Senha@123",
    "role": "PACIENTE"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
#   "tokenType": "Bearer"
# }

# Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "joao@example.com",
    "senha": "Senha@123"
  }'
```

#### Consultas

```bash
# Criar consulta (requer role ENFERMEIRO)
curl -X POST http://localhost:8080/consultas \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "medicoId": 2,
    "dataHora": "2026-09-25T14:30:00",
    "observacoes": "Consulta de rotina"
  }'

# Listar consultas
curl -X GET http://localhost:8080/consultas \
  -H "Authorization: Bearer <token>"

# Buscar consulta por ID
curl -X GET http://localhost:8080/consultas/1 \
  -H "Authorization: Bearer <token>"

# Editar consulta (requer role MEDICO)
curl -X PUT http://localhost:8080/consultas/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "pacienteId": 1,
    "medicoId": 2,
    "dataHora": "2026-09-25T15:00:00",
    "observacoes": "Consulta reagendada"
  }'
```

### Postman Collection

Uma coleção Postman está incluída no repositório:

```
postman-collections/
├── tech-challenge3-auth.json
└── tech-challenge3-consultas.json
```

**Importar no Postman:**

1. Abra Postman
2. Clique em `Import`
3. Selecione os arquivos JSON
4. Execute as requisições (variáveis de ambiente são auto-preenchidas)

### Documentação Detalhada

- 📖 **[ARCHITECTURE.md](./ARCHITECTURE.md)** - Arquitetura e decisões técnicas
- 📖 **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - Endpoints REST e eventos

---

## ✅ Testes

### Rodar Testes

```bash
# Rodar todos os testes (requer Docker para Testcontainers)
./mvnw test

# Rodar testes com cobertura JaCoCo
./mvnw verify

# Ver relatório de cobertura
# - Agendamento: agendamento-service/target/site/jacoco/index.html
# - Notificação: notificacao-service/target/site/jacoco/index.html

# Abrir no navegador (Mac)
open agendamento-service/target/site/jacoco/index.html
```

### Cobertura de Testes

- **Target de cobertura:** 80%+
- **Testes incluem:**
  - Unit tests (modelos, use cases)
  - Integration tests (Controllers, Repositories)
  - Event tests (Producers, Listeners)

### Testcontainers

Os testes usam **Testcontainers** para PostgreSQL e RabbitMQ:

```java
@Testcontainers
@SpringBootTest
public class ConsultaControllerTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16-alpine");
    
    // Testes executam com containers reais
}
```

---

## 🐛 Troubleshooting

### Problema: Porta já em uso

```
ERROR: bind: address already in use
```

**Solução:**

```bash
# Listar processo usando a porta (Mac/Linux)
lsof -i :8080
lsof -i :5432
lsof -i :5672

# Matar processo
kill -9 <PID>

# Ou mudar porta no docker-compose.yml
```

### Problema: Docker Compose falha ao iniciar

```
ERROR: Service 'postgres-agendamento' failed to start
```

**Solução:**

```bash
# Limpar volumes e containers
docker compose down -v
docker system prune -a

# Tentar novamente
docker compose up --build
```

### Problema: JWT Token Inválido

```
{"status": 401, "message": "JWT token é inválido"}
```

**Verificar:**
- Token foi gerado em /auth/login ou /auth/register
- Token não expirou (válido por 1 hora)
- Header Authorization: `Bearer <token>` (não esqueceu "Bearer ")
- JWT_SECRET no .env está correto

### Problema: Consulta não recebe notificação

**Verificar:**
1. RabbitMQ está rodando: `http://localhost:15672`
2. Fila recebeu a mensagem (painel RabbitMQ)
3. Serviço de Notificações está rodando: `http://localhost:8081/actuator/health`
4. Logs: `docker compose logs notificacao-service`

### Problema: "Paciente não encontrado"

```
{"status": 400, "message": "Paciente não encontrado"}
```

**Verificar:**
- Criar usuários primeiro (register)
- Usar IDs de usuários existentes
- Consultar DB: `docker compose exec postgres-agendamento psql -U techchallenge -c "SELECT * FROM users;"`

---

## 👥 Integrantes e Responsabilidades

### Equipe - Tech Challenge 3

| Membro | Responsabilidade | GitHub |
|--------|-----------------|--------|
| **Matheus Martins R.** | Tech Lead, Arquitetura de Microserviços, RabbitMQ | [@MatheusMartinsR](https://github.com/MatheusMartinsR) |
| **Colaborador 2** | Serviço de Agendamento, Controllers e Use Cases | - |
| **Colaborador 3** | Serviço de Notificações, Event Listeners | - |
| **Colaborador 4** | Testes, CI/CD Pipeline, Docker | - |

### Responsabilidades por Módulo

#### Serviço de Agendamento (Port 8080)

- ✅ Autenticação JWT
- ✅ Gerenciamento de Consultas (CRUD)
- ✅ Validação de dados
- ✅ Publicação de eventos
- ✅ Controle de acesso por role

**Proprietário:** Matheus Martins R.

#### Serviço de Notificações (Port 8081)

- ✅ Consumo de eventos RabbitMQ
- ✅ Envio de notificações
- ✅ Persistência de logs de notificação
- ✅ Tratamento de erros e retry

**Proprietário:** Matheus Martins R.

#### Infraestrutura e DevOps

- ✅ Docker Compose
- ✅ CI/CD (GitHub Actions)
- ✅ Testes e Cobertura
- ✅ Health Checks

**Proprietário:** Matheus Martins R.

---

## 📖 Documentação Complementar

### Interno do Projeto

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Decisões arquiteturais (ADRs), padrões e diagramas C4/ER
- [API_DOCUMENTATION.md](./API_DOCUMENTATION.md) - Especificação completa de endpoints, modelos e eventos
- [.github/workflows/ci.yml](.github/workflows/ci.yml) - Pipeline CI/CD

### Links Externos

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring AMQP (RabbitMQ)](https://spring.io/projects/spring-amqp)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/getstarted.html)
- [JWT.io](https://jwt.io/)
- [Docker Documentation](https://docs.docker.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

## 📋 Checklist de Setup

Use este checklist para validar sua instalação:

- [ ] Java 17+ instalado (`java -version`)
- [ ] Maven 3.8+ ou `./mvnw` disponível
- [ ] Docker e Docker Compose instalados
- [ ] Git instalado e repositório clonado
- [ ] `.env` presente na raiz do projeto
- [ ] `docker compose up --build` executado com sucesso
- [ ] Todos os serviços estão em "UP" (green)
- [ ] `http://localhost:8080/actuator/health` retorna UP
- [ ] `http://localhost:8081/actuator/health` retorna UP
- [ ] `http://localhost:15672` acessível (RabbitMQ Admin)
- [ ] Testes passam: `./mvnw verify`
- [ ] Criar usuário via `/auth/register` funciona
- [ ] Login via `/auth/login` retorna token
- [ ] Criar consulta via `/consultas` recebe notificação

---

## 🎉 Próximos Passos

Após setup bem-sucedido:

1. **Explore a API:**
   - Registre um novo usuário (MEDICO, ENFERMEIRO, PACIENTE)
   - Faça login para obter token JWT
   - Crie uma consulta e observe a notificação

2. **Estude a Arquitetura:**
   - Leia [ARCHITECTURE.md](./ARCHITECTURE.md)
   - Analise como eventos fluem entre serviços
   - Explore padrão Hexagonal no código

3. **Customize para Produção:**
   - Mude JWT_SECRET e senhas de banco
   - Configure HTTPS/TLS
   - Configure Rate Limiting
   - Setup logging centralizado

4. **Estenda o Sistema:**
   - Adicione novos tipos de notificação (SMS, Push)
   - Implemente busca avançada de consultas
   - Adicione agendamento automático
   - Integre com serviço de email real

---

## 📞 Suporte

Para dúvidas ou problemas:

1. Consulte [TROUBLESHOOTING](#troubleshooting) acima
2. Verifique logs: `docker compose logs -f`
3. Abra uma [Issue no GitHub](https://github.com/MatheusMartinsR/tech-challenge3/issues)

---

## 📜 Licença

Este projeto é fornecido para fins educacionais como parte do desafio técnico da **FIAP Pós-Graduação**.


---

**Versão:** 1.0.0  
**Última atualização:** 2026-09-12  

---

## 🚀 Deploy em Produção (Futuro)

Arquitetura pronta para escalar:

```
┌─────────────────────────────────────────┐
│  Kubernetes Cluster (GKE/EKS/AKS)      │
│                                         │
│  ┌──────────────────────────────────┐  │
│  │ Ingress / Load Balancer          │  │
│  └────────────────┬─────────────────┘  │
│                   │                     │
│     ┌─────────────┼─────────────┐       │
│     │             │             │       │
│  ┌──▼──┐       ┌──▼──┐      ┌──▼──┐   │
│  │ Pod │───┐   │ Pod │      │ Pod │   │
│  │ Agd │   │   │ Ntf │      │ Agd │   │
│  └──────┘   │   └──────┘     └──────┘   │
│             │                           │
│          ┌──▼──────────────┐            │
│          │ Service Mesh    │            │
│          │ (Istio)         │            │
│          └──────┬──────────┘            │
│                 │                       │
│          ┌──────▼──────────┐            │
│          │ PostgreSQL      │            │
│          │ (CloudSQL)      │            │
│          └─────────────────┘            │
│                                         │
│          ┌─────────────────┐            │
│          │ RabbitMQ        │            │
│          │ (Managed)       │            │
│          └─────────────────┘            │
└─────────────────────────────────────────┘
```

Implementação futura com Kubernetes + Helm Charts.
