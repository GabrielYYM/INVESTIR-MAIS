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

### 2. Executar o Backend (Spring Boot)

O backend executa na porta `8080` e usa PostgreSQL. Os testes de integração iniciam um PostgreSQL temporário com Testcontainers e Docker.

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

O backend usa PostgreSQL em execução normal e nos testes de integração específicos desse banco. Esses testes requerem Docker em execução; sem Docker, são ignorados. Os testes do fluxo de login executam os filtros reais do Spring Security e o serviço JDBC nativo com H2 em memória, sem Docker. O esquema JDBC do Spring Session é criado automaticamente (`spring.session.jdbc.initialize-schema=always`); o script `schema-postgresql.sql` cria as tabelas de tokens e outras tabelas auxiliares.

Autenticação da aplicação web usa Spring Security com MFA nativo e sessão HTTP persistida pelo Spring Session JDBC (cookie `SESSION` HttpOnly). `POST /auth/login` recebe e-mail e senha como formulário (`formLogin`) e, após validar as credenciais, gera um token de uso único pelo serviço JDBC do Spring e envia o e-mail automaticamente. `POST /auth/code` fica disponível para reenvio durante a autenticação parcial. A conta de destino vem exclusivamente da sessão autenticada por senha. `POST /auth/verify-2fa` recebe o parâmetro de formulário `token` (`oneTimeTokenLogin`), válido por cinco minutos. Somente a combinação dos fatores senha e token para o mesmo usuário libera as rotas protegidas, incluindo `GET /auth/me`. Os POSTs de login, reenvio e confirmação retornam 204 quando processados com sucesso; o frontend consulta `/auth/me` após confirmar o token. `POST /auth/logout` é processado pelo filtro nativo do Spring e invalida a sessão. Um novo envio de senha reinicia os fatores, exigindo nova confirmação por e-mail. As chamadas de escrita consultam `/csrf` para obter um token atualizado, inclusive após login e logout. O backend aceita a origem configurada por `APP_CORS_ALLOWED_ORIGIN` (padrão `http://localhost:5173`). O login exige que o e-mail já tenha sido verificado. As senhas são armazenadas com Argon2. Cadastro, confirmação de e-mail e recuperação de senha continuam como regras da aplicação, com tokens separados dos tokens de login. As alterações de conta e o consumo dos tokens usam uma transação; os e-mails de confirmação e recuperação são enviados somente após o commit. `POST /api/users/resend-verification` recebe JSON com `email` e devolve uma resposta genérica, inclusive para contas inexistentes ou já verificadas. Alterar o e-mail exige nova confirmação e encerra todas as sessões do endereço anterior. Alterar ou recuperar a senha também encerra todas as sessões e invalida os tokens pendentes. Cadastro, recuperação, redefinição e reenvio possuem limite de requisições por IP e endpoint.

O cliente da Brapi é registrado por `@ImportHttpServices` no grupo `brapi`. URL e timeouts usam `spring.http.serviceclient.brapi.*`; o cabeçalho de autorização é aplicado ao grupo, e o circuit breaker continua protegendo as chamadas. Parâmetros HTTP usam Bean Validation, e as respostas de erro seguem `ProblemDetail` pelo tratamento padrão do Spring MVC.

Tokens de confirmação e recuperação são persistidos pelo Spring Data JPA na tabela existente `app_one_time_token`. O consumo usa bloqueio pessimista dentro da transação da conta, preservando uso único e rollback. Os tokens de login continuam no serviço JDBC nativo do Spring Security.

Os eventos de e-mail de confirmação e recuperação usam o registro persistente do Spring Modulith (JPA). O registro é gravado na transação da conta e removido após entrega; falhas são reprocessadas a cada `ACCOUNT_MAIL_RETRY_DELAY` (padrão `30s`) e publicações pendentes são recuperadas ao reiniciar. Antes de enviar, o listener verifica se o token ainda está ativo: tokens expirados, consumidos ou substituídos são descartados. SMTP pode entregar uma mensagem antes de uma falha no processo ser registrada, portanto um reprocessamento pode produzir uma mensagem duplicada. E-mails de login continuam sendo disparados pelo handler do fluxo de segurança.

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
