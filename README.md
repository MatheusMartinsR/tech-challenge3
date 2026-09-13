# Plataforma de Agendamento Hospitalar — Tech Challenge Fase 3

Dois microsserviços Spring Boot implementam o agendamento seguro de consultas hospitalares e notificações assíncronas aos pacientes. O projeto segue uma arquitetura hexagonal, mantém um banco PostgreSQL por serviço e troca eventos JSON versionados por meio do RabbitMQ.

## Serviços e tecnologias

| Módulo | Responsabilidade | Porta |
|---|---|---:|
| `agendamento-service` | Autenticação JWT, autorização por perfil, API REST de agendamento e consultas históricas via GraphQL | 8080 |
| `notificacao-service` | Consome eventos de consultas, armazena tentativas de entrega, repete falhas e encaminha mensagens esgotadas para uma DLQ | 8081 |
| `common` | Contrato compartilhado de eventos do RabbitMQ | — |

São utilizadas as tecnologias Java 17, Spring Boot, Spring Security, Spring Data JPA, Spring GraphQL, Flyway, PostgreSQL, RabbitMQ, Docker Compose, JUnit 5, Mockito, Testcontainers e JaCoCo.

O código-fonte interno é escrito em inglês. O contrato público em português exigido pelo desafio é preservado intencionalmente: `/consultas`, campos GraphQL, valores de perfil (`MEDICO`, `ENFERMEIRO`, `PACIENTE`), colunas do banco e nomes dos eventos.

## Executando a stack completa

Requisitos: Docker Desktop com Compose v2. Copie `.env.example` para `.env`, substitua o segredo JWT fora do ambiente local e execute:

```bash
docker compose up --build
```

Endpoints de saúde:

- Agendamento: `http://localhost:8080/actuator/health`
- Notificação: `http://localhost:8081/actuator/health`
- Gerenciamento do RabbitMQ: `http://localhost:15672`
- GraphiQL: `http://localhost:8080/graphiql`

O Flyway cria os dois schemas. O Docker Compose também habilita dados demonstrativos de agendamento: `medico.demo@hospital.local`, `enfermeiro.demo@hospital.local` e `paciente.demo@hospital.local`, todos com a senha `senha123`.

Pare a stack com `docker compose down`. Use `docker compose down -v` somente quando quiser apagar deliberadamente os volumes locais dos bancos de dados.

## Autenticação e autorização

Cadastre-se com `POST /auth/register`, faça login com `POST /auth/login` e envie o token retornado no cabeçalho `Authorization: Bearer <token>`. Ambas as respostas incluem o ID e os dados do perfil do usuário autenticado, permitindo referenciar o paciente ou médico sem acesso direto ao banco de dados.

| Operação | Perfis permitidos |
|---|---|
| Criar consulta | `MEDICO`, `ENFERMEIRO` |
| Editar consulta | `MEDICO`, `ENFERMEIRO` |
| Cancelar consulta | `MEDICO`, `ENFERMEIRO` |
| Listar/consultar consultas | todos os perfis autenticados; o paciente vê apenas as próprias consultas |
| Consultas GraphQL | todos os perfis autenticados; a propriedade do paciente é respeitada |

A criação e a edição rejeitam datas passadas, perfis inválidos de paciente ou médico, consultas canceladas e conflitos de data/horário para qualquer um dos participantes.

## Testes e critérios de qualidade

```bash
./mvnw verify
```

No Windows, use `mvnw.cmd verify`. Os testes unitários e de integração Spring são executados localmente; os testes com Testcontainers de PostgreSQL/RabbitMQ são executados quando o Docker está disponível. O JaCoCo falha o build quando os casos de uso de negócio ficam abaixo de 60% de cobertura de linhas. O GitHub Actions executa o mesmo comando e publica os relatórios de testes e cobertura.

Execute os cenários automatizados end-to-end do Postman com:

```bash
npx newman run postman-collections/e2e-collection.postman_collection.json \
  -e postman-collections/environment.postman_environment.json
```

## Documentação

- [API REST e GraphQL](docs/API.md)
- [Arquitetura, diagramas, ADRs e modelo de dados](docs/ARCHITECTURE.md)
- [Procedimento e evidências dos testes end-to-end](docs/E2E-TESTS.md)
- [Matriz de conformidade por issue](docs/COMPLIANCE.md)
- Coleções do Postman em `postman-collections/`

## Configuração

As credenciais de produção devem ser fornecidas por variáveis de ambiente. As principais variáveis são `SPRING_DATASOURCE_*`, `SPRING_RABBITMQ_*` e `JWT_SECRET`. Os valores padrão da aplicação existem apenas para desenvolvimento local; nunca use o segredo ou as senhas demonstrativas em produção.
