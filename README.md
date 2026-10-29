# INVESTIR MAIS

[![Java Version](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.1-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Node.js](https://img.shields.io/badge/Node.js-22-green?style=flat-square&logo=node.js)](https://nodejs.org/)
[![React](https://img.shields.io/badge/React-18-blue?style=flat-square&logo=react)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-5-purple?style=flat-square&logo=vite)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?style=flat-square&logo=tailwindcss)](https://tailwindcss.com/)

[![MapStruct](https://img.shields.io/badge/MapStruct-yellow?style=flat-square)](https://mapstruct.org/)
[![SpringDoc OpenAPI](https://img.shields.io/badge/SpringDoc_OpenAPI-darkgreen?style=flat-square)](https://springdoc.org/)
[![Lombok](https://img.shields.io/badge/Lombok-pink?style=flat-square&logo=lombok)](https://projectlombok.org/)

## Escopo

O **INVESTIR MAIS** é um sistema educacional que conecta docentes, com conhecimentos na área financeira, com crianças e adolescentes, a fim de fomentar a educação financeira infantil, além de oferecer um sistema de gestão patrimonial.

## Tecnologias

| Tecnologia | Versão |
|-----------|--------|
| Java | 21 |
| Spring Boot | 4.1.1 |
| Spring Cloud | 2025.1.3 |
| Spring Modulith | 2.1.1 |
| Node.js | 22.13.1 |
| React | 18.3.1 |
| Vite | 5.4.8 |
| Tailwind CSS | 3.4.13 |
| MapStruct | 1.6.3 |
| SpringDoc OpenAPI | 3.1.1 |
| Lombok | 1.18.38 |
| Resilience4j | gerenciado pelo Spring Cloud |
| Banco de dados | PostgreSQL (runtime e testes de integração) |

## Estrutura do Projeto

```
INVEST_MAIS/
├── .github/
├── .gitignore
├── .env.example
├── pom.xml
├── README.md
├── basedata.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/repositorio/mvp/
│   │   │       ├── MvpApplication.java
│   │   │       ├── domain/
│   │   │       │   ├── education/
│   │   │       │   └── user/
│   │   │       └── infrastructure/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── frontend/
    ├── package.json
    ├── vite.config.js
    ├── src/
    │   ├── App.jsx
    │   ├── components/
    │   ├── pages/
    │   ├── services/
    │   └── styles/
    ├── public/
    ├── index.html
    └── dist/
```

## Como Executar

### Pré-requisitos

Garante que possui as seguintes ferramentas instaladas em sua máquina:
- **Java 21 ou superior** (JDK)
- **Node.js 20 ou superior** e **npm**
- **Git**

---

### 1. Clonar o Repositório

```bash
git clone <URL_DO_REPOSITORIO>
cd INVESTIR-MAIS/investir_mais
```

---

### Redis: sessões e cache

O backend requer Redis em execução. Se o container `redis` já existe, inicie-o com `docker start redis`. Para criar um ambiente local novo:

```powershell
docker run -d --name redis -p 127.0.0.1:6379:6379 -v investir-mais-redis:/data redis:8 redis-server --appendonly yes
docker exec redis redis-cli PING
```

O resultado de `PING` deve ser `PONG`. Configure `REDIS_HOST`, `REDIS_PORT`, `REDIS_USERNAME`, `REDIS_PASSWORD` e `REDIS_SSL_ENABLED` conforme `.env.example`. Para o backend no Windows, o host padrão é `localhost`; em containers na mesma rede, use o nome do serviço Redis. Em produção, use rede restrita, credenciais e TLS conforme o provedor.

`@EnableCaching` fica na classe principal, sem uma classe de configuração adicional. O Spring Cache armazena apenas as respostas de cotação da Brapi, por ticker normalizado, durante `QUOTE_CACHE_TTL` (padrão `1m`). Exceções e respostas vazias não são armazenadas. O cache fica no servidor; a resposta HTTP continua com `Cache-Control: no-store`. Os valores são strings serializadas pelo mecanismo padrão do Spring Data Redis.

As sessões usam o repositório Redis com índice por usuário, permitindo encerrar todas as sessões ao alterar senha ou e-mail. `SESSION_TIMEOUT` controla a inatividade (padrão `30m`). A migração não reaproveita as sessões antigas do PostgreSQL: usuários precisarão entrar novamente. Cadastro, senhas com hash Argon2, códigos de login e tokens de confirmação/recuperação continuam no PostgreSQL; os limites de tentativas usam Redis. Cache e sessões dependem da disponibilidade do Redis, sem fallback em memória.

No RedisInsight, procure `investir-mais:cache:cotacoes::*` para cotações e `investir-mais:session:*` para sessões. A mesma consulta de cotação dentro do prazo reaproveita o resultado; após a expiração, consulta novamente a Brapi. Evite operações globais como `FLUSHDB`, que também removeriam as sessões. As chaves das sessões podem ter expiração técnica superior ao prazo de inatividade para permitir a limpeza de índices; a validade é controlada pelo Spring Session.

O limite de autenticação usa Redis com um script Lua atômico no `SecurityFilter`: `AUTH_RATE_LIMIT_CAPACITY` requisições por IP e endpoint a cada `AUTH_RATE_LIMIT_PERIOD`, com namespace `AUTH_RATE_LIMIT_NAMESPACE` (padrão `investir-mais:rate-limit`). Requisições bloqueadas recebem HTTP 429 e `Retry-After`; o contador expira automaticamente e tentativas bloqueadas não prolongam a janela. Se Redis estiver indisponível, o filtro retorna HTTP 503 sem liberar a tentativa. A tabela antiga `app_auth_rate_limit` deixa de ser usada e não é mais criada; bancos existentes não têm dados ou tabelas removidos automaticamente.

Tokens de confirmação/recuperação mantêm uso único e rollback no PostgreSQL. A emissão para contas existentes é serializada por usuário para que reenvios simultâneos não deixem vários tokens da mesma finalidade ativos. A atualização de perfil exige `currentPassword` no JSON, além de `name`, `email` e `age` opcional. O frontend já envia a senha atual. Excluir um curso também remove suas aulas; URLs de aulas são validadas na entrada HTTP, e descrições e URLs são armazenadas como texto sem o limite anterior de 255 caracteres.

A limpeza de tokens expirados de conta e de login ocorre a cada hora no `AccountSecurityService`, por `@Scheduled` gerenciado pelo Spring. O agendador interno de `JdbcOneTimeTokenService` é desativado para evitar uma thread remanescente no encerramento com a versão atual do Spring Security. A expiração é verificada no consumo, independentemente do horário da limpeza.

### 2. Executar o Backend (Spring Boot)

O backend executa na porta `8080` e usa PostgreSQL e Redis. Os testes de integração iniciam PostgreSQL e Redis temporários com Testcontainers e Docker, sem usar os dados locais.

**No Windows (PowerShell ou Command Prompt):**
```powershell
.\mvnw.cmd spring-boot:run
```

**No Linux / macOS:**
```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Configure as variáveis do backend no ambiente ou em `.env` (arquivo ignorado pelo Git), usando `.env.example` como referência. Para um banco PostgreSQL local, configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.

O backend usa PostgreSQL em execução normal e nos testes de integração específicos desse banco. Esses testes requerem Docker em execução; sem Docker, são ignorados. Os testes isolados do fluxo de login executam os filtros reais do Spring Security e o serviço JDBC nativo com H2 em memória e um cliente Redis simulado, sem Docker. Os testes de cache, rate limit, sessões e ciclo da conta usam Redis real em containers temporários. Os testes de tokens verificam consumo único, emissão concorrente e rollback; os testes completos da conta verificam a integração dessas operações com JPA e PostgreSQL. Testes de controllers carregam apenas a configuração HTTP, sem habilitar persistência ou cache da classe principal. As sessões são armazenadas no Redis; o script `schema-postgresql.sql` continua criando as tabelas de tokens e outras tabelas auxiliares no PostgreSQL.

Autenticação da aplicação web usa Spring Security com MFA nativo e sessão HTTP armazenada pelo Spring Session Redis (cookie `SESSION` HttpOnly). `POST /auth/login` recebe e-mail e senha como formulário (`formLogin`) e, após validar as credenciais, gera um token de uso único pelo serviço JDBC do Spring e envia o e-mail automaticamente. `POST /auth/code` fica disponível para reenvio durante a autenticação parcial. A conta de destino vem exclusivamente da sessão autenticada por senha. `POST /auth/verify-2fa` recebe o parâmetro de formulário `token` (`oneTimeTokenLogin`), válido por cinco minutos. Somente a combinação dos fatores senha e token para o mesmo usuário libera as rotas protegidas, incluindo `GET /auth/me`. Os POSTs de login, reenvio e confirmação retornam 204 quando processados com sucesso; o frontend consulta `/auth/me` após confirmar o token. `POST /auth/logout` é processado pelo filtro nativo do Spring e invalida a sessão. Um novo envio de senha reinicia os fatores, exigindo nova confirmação por e-mail. O CSRF usa `csrf.spa()` do Spring Security. O frontend copia o cookie `XSRF-TOKEN` para o header `X-XSRF-TOKEN` e inicializa o cookie via `GET /auth/me` quando ausente. Frontend e API devem compartilhar o dominio dos cookies, por exemplo via proxy reverso. O backend aceita a origem configurada por `APP_CORS_ALLOWED_ORIGIN` (padrão `http://localhost:5173`). O login exige que o e-mail já tenha sido verificado. As senhas são armazenadas com Argon2. Cadastro, confirmação de e-mail e recuperação de senha continuam como regras da aplicação, com tokens separados dos tokens de login. As alterações de conta e o consumo dos tokens usam uma transação; os e-mails de confirmação e recuperação são enviados somente após o commit. `POST /api/users/resend-verification` recebe JSON com `email` e devolve uma resposta genérica, inclusive para contas inexistentes ou já verificadas. Alterar o e-mail exige nova confirmação e encerra todas as sessões do endereço anterior. Alterar ou recuperar a senha também encerra todas as sessões e invalida os tokens pendentes. Cadastro, recuperação, redefinição e reenvio possuem limite de requisições por IP e endpoint.

O cliente da Brapi é registrado por `@ImportHttpServices` no grupo `brapi`. URL e timeouts usam `spring.http.serviceclient.brapi.*`; o cabeçalho de autorização é aplicado ao grupo, e o circuit breaker continua protegendo as chamadas. Parâmetros HTTP usam Bean Validation, e as respostas de erro seguem `ProblemDetail` pelo tratamento padrão do Spring MVC.

Tokens de confirmação e recuperação são persistidos pelo `AccountSecurityService`, via `JdbcClient` do Spring, na tabela existente `app_one_time_token`. O mesmo serviço concentra a revogação de sessões Redis e a remoção de códigos de login pendentes, sem interfaces intermediárias ou entidade/repositório JPA para tokens. O consumo usa bloqueio pessimista dentro da transação da conta, preservando uso único e rollback. Os tokens de login continuam no serviço JDBC nativo do Spring Security.

Os eventos de e-mail de confirmação e recuperação usam o registro persistente do Spring Modulith (JPA). O registro é gravado na transação da conta e removido após entrega; falhas são reprocessadas pelo próprio `AuthenticationEmailHandler` a cada `ACCOUNT_MAIL_RETRY_DELAY` (padrão `30s`) e publicações pendentes são recuperadas ao reiniciar. Antes de enviar, o listener verifica se o token ainda está ativo: tokens expirados, consumidos ou substituídos são descartados. SMTP pode entregar uma mensagem antes de uma falha no processo ser registrada, portanto um reprocessamento pode produzir uma mensagem duplicada. E-mails de login continuam sendo disparados pelo handler do fluxo de segurança.

As propriedades de limite de requisições, URLs e intervalo de reenvio usam `@ConfigurationProperties` com validação na inicialização. Configurações inválidas interrompem a inicialização em vez de falhar apenas durante uma requisição.

Os tipos `@ConfigurationProperties` são registrados pelo `@ConfigurationPropertiesScan` da aplicação. URL e timeouts da Brapi usam diretamente `spring.http.serviceclient.brapi.*`, com as variáveis `BRAPI_BASE_URL`, `BRAPI_CONNECT_TIMEOUT` e `BRAPI_READ_TIMEOUT`. As restrições TEACHER/ADMIN para criar, alterar e excluir cursos e aulas ficam nos serviços com `@PreAuthorize`; as verificações de propriedade continuam nos serviços. Erros de validação HTTP usam o `ProblemDetail` padrão do Spring MVC, sem selecionar manualmente a primeira mensagem de campo.

Para executar localmente com as opções de desenvolvimento:

```powershell
$env:SPRING_PROFILES_ACTIVE = "dev"
./mvnw.cmd spring-boot:run
```

Em produção, ative o perfil `prod`, configure a URL/credenciais do banco e defina `APP_CORS_ALLOWED_ORIGIN` para a origem real do frontend. O perfil `prod` habilita cookies de sessão Secure, portanto exige HTTPS.

As configurações dos perfis `dev`, `prod` e `test` ficam em documentos separados dentro de `src/main/resources/application.properties`.

---

### 3. Executar o Frontend (React + Vite)

Em um novo terminal, navegue até o diretório do frontend e instale as dependências:

```bash
cd frontend/investirMais
npm install
```

Configure as variáveis de ambiente criando/editando o arquivo `.env` localizado dentro de `frontend/investirMais/`:
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_BRAPI_TOKEN=seu_token_da_brapi_aqui
```

Inicie a aplicação no modo de desenvolvimento:
```bash
npm run dev
```

- **Aplicação Web Frontend:** `http://localhost:5173`

---

## Materiais Complementares
[Documentação técnica e diagrama](./docs/diagrama-de-classes.md) · [Notas de privacidade](./docs/lgpd/README.md)
