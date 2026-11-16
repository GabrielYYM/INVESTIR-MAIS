# Investir Mais

O Investir Mais é uma plataforma de educação financeira com catálogo de cursos e aulas. A aplicação reúne uma interface React e uma API Spring Boot. A gestão de carteira faz parte do escopo planejado.

[Documentação](docs/README.md) · [Diagramas](#diagramas) · [Execução local](#execução-local) · [API](#api) · [Testes](#testes-e-verificações) · [Apresentação](docs/apresentacao.md)

Veja a [atualização da plataforma](docs/atualizacao-plataforma.md) para candidaturas, novos documentos legais, auditoria com filtros, respostas dos alunos, busca de cursos e upload Cloudinary.

## Funcionalidades

| Área | O que está disponível |
| --- | --- |
| Conta | Cadastro de aluno com data de nascimento, confirmação de e-mail, login em duas etapas e recuperação de senha. O perfil permite alterar nome e nascimento com a senha atual, trocar a senha e excluir a conta. |
| Painel | Entrada comum aos três perfis, com cursos em destaque e métricas para professores e administradores. |
| Educação | Catálogo paginado, matrícula, videoaulas do YouTube, perguntas de múltipla escolha, progressão por aulas concluídas e perguntas aprovadas, e ordenação das aulas. |
| Administração | Gestão de todo o catálogo, consulta de usuários e auditoria, e aprovação de candidaturas de professor com documentos privados, sem idade mínima. |
| Documentos legais | Termos e Política de Privacidade com versões e aceites imutáveis no PostgreSQL. Uma nova versão exige nova confirmação; o perfil permite consultar o histórico. |

| Perfil | Consultar cursos e aulas | Criar cursos | Gerenciar cursos, aulas, perguntas e ordem |
| --- | --- | --- | --- |
| `STUDENT` | Sim | — | — |
| `TEACHER` | Sim | Sim | Cursos próprios e suas aulas |
| `ADMIN` | Sim | Sim | Todo o catálogo |

O cadastro público aceita apenas `STUDENT`. A interface exibe as ações disponíveis e a API valida o perfil e a propriedade do curso em cada operação de escrita. O painel não concede nem altera o papel `ADMIN`.

As aulas aceitam links de qualquer canal do YouTube. A validação é local e verifica o formato da URL; não há filtro temático, aprovação de canais, verificação de disponibilidade ou uso de `YOUTUBE_API_KEY`. Quem publica deve conferir o material e sua reprodução. Veja o [guia dos vídeos](docs/video-validation.md).

O cadastro, a troca e a recuperação de senha exigem de 8 a 100 caracteres, com ao
menos uma letra minúscula, uma maiúscula, um número e um caractere especial. A API
e os formulários aplicam a mesma regra. Senhas existentes continuam aceitas no
login e na confirmação de operações; uma nova senha deve cumprir os requisitos.

## Arquitetura

| Componente | Responsabilidade |
| --- | --- |
| React e React Router | Rotas públicas e protegidas, formulários, painel, catálogo, aula e perfil. |
| Axios e Orval | Axios centraliza cookies, CSRF e tratamento de falhas. Orval gera clientes de conta e educação a partir dos contratos OpenAPI versionados. |
| Spring Boot e Spring Security | Endpoints HTTP, validação, autorização, autenticação com senha e OTT e regras de conta e educação. |
| PostgreSQL | Usuários, cursos, aulas, perguntas e aprovações, progresso, versões e aceites legais, revisões Envers, tokens de uso único e registro de eventos de e-mail. |
| Redis | Sessões indexadas por usuário e contadores de tentativas de autenticação. |
| Resend | Entrega dos e-mails de confirmação, login e recuperação. |
| YouTube | Hospeda os vídeos referenciados pelas aulas. |

| Stack | Versões e referências |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.2, Spring Modulith 2.1.2, MapStruct 1.6.3 e SpringDoc 3.2.0. [Configuração Maven](pom.xml) |
| Frontend | Node.js 24 na CI, React 19.3.1, React Router 7, React-admin 5, Material UI 9, Tailwind CSS 4, Vite 8 e Orval 8.46.0. [Dependências travadas](frontend/investirMais/package-lock.json) |
| Infraestrutura de integração | PostgreSQL 17 e Redis 8 nos testes com Testcontainers. [Configuração da aplicação](src/main/resources/application.properties) |

```text
INVESTIR-MAIS/
├── .github/workflows/        Verificações de backend e frontend
├── docs/                     Diagramas, auditoria e notas de privacidade
├── frontend/investirMais/    SPA React e contratos OpenAPI de conta e educação
├── src/main/java/com/repositorio/investir_mais/
│   ├── domain/               Conta, educação e serviço de documentos legais
│   └── infrastructure/       Segurança, documentos legais e e-mail
├── src/main/resources/       Configuração e SQL de inicialização
├── src/test/                 Testes unitários, HTTP e de integração
├── .env.example              Modelo de configuração local do backend
└── pom.xml                   Build Maven
```

## Diagramas

O [arquivo editável do Draw.io](docs/diagramas/investir-mais.drawio) reúne cinco abas com as classes de domínio, as classes da aplicação, a navegação, o BPMN do login e a arquitetura. Os diagramas incluem os documentos legais versionados, a publicação de links do YouTube e a ordenação das aulas. A [documentação dos diagramas](docs/diagrama-de-classes.md) relaciona cada visão ao código.

### Arquitetura

[![Arquitetura com React, Spring Boot, PostgreSQL, Redis, Resend e YouTube](docs/diagramas/arquitetura.png)](docs/diagramas/arquitetura.png)

### Fluxo de navegação

- O cadastro público cria uma conta de aluno com data de nascimento. Ao confirmar o e-mail original no navegador do cadastro, o usuário inicia a primeira sessão. Em outro navegador, ou com um link reenviado, a página confirma a conta e oferece o login. A recuperação usa um código separado para definir a nova senha.
- Uma rota protegida verifica a sessão com `GET /auth/me`. Sem acesso, o React redireciona ao login e guarda a rota solicitada. Após os dois fatores, o navegador abre um destino interno permitido ou o painel.
- Antes do painel e do catálogo, a aplicação verifica o aceite da versão atual dos Termos e da Política de Privacidade. Uma versão nova exige nova confirmação; o perfil mostra o histórico com o texto de cada versão aceita.
- O professor proprietário e o administrador podem ordenar as aulas, preservando o vínculo das perguntas. A publicação aceita links de qualquer canal do YouTube; a promoção a professor depende de candidatura aprovada, sem idade mínima. A saída encerra a sessão no backend.

[![Fluxo de navegação do cadastro e login à área autenticada](docs/diagramas/fluxo-de-navegacao.png)](docs/diagramas/fluxo-de-navegacao.png)

### BPMN do login com senha e OTT

1. A pessoa informa e-mail e senha. O Spring valida as credenciais e exige e-mail confirmado. Nessa etapa existe uma sessão parcial.
2. O serviço JDBC de OTT do Spring Security gera um token de uso único com validade de cinco minutos. O e-mail inclui o link **Concluir acesso** e a opção de digitar o código.
3. Na rota `/entrar/link`, o React lê e remove o token do fragmento da URL e o envia com CSRF para `POST /auth/verify-2fa`. O código manual chama o mesmo endpoint. O token pertence à sessão que iniciou o login e deve ser usado no mesmo navegador.
4. Após validar e consumir o OTT, o Spring combina os fatores e troca o identificador da sessão. A interface consulta o aceite legal antes de liberar o catálogo. O diagrama também mostra as saídas de recusa por credenciais ou token inválido.

[![BPMN do login com raias para pessoa usuária, React e Spring](docs/diagramas/bpmn-login-ott.png)](docs/diagramas/bpmn-login-ott.png)

### Classes de domínio

- `User` representa a conta e seu perfil `UserRole`. A senha é persistida como hash Argon2id e fica fora das revisões Envers.
- Cada `Course` tem um usuário proprietário e pode ter zero ou mais aulas e perguntas. Cada `Lesson` pertence a exatamente um curso e guarda os endereços de mídia e miniatura.
- `User` mantém matrículas por curso e progresso por aula como coleções de valores embutíveis. Os acertos são UUIDs em `passedQuestionIds`; as alternativas pertencem a `Question`.
- Documentos e aceites ficam nas tabelas SQL `legal_document_release` e `legal_document_acceptance`, acessadas por JDBC e protegidas contra alterações.
- A exclusão é lógica. Excluir um curso propaga a exclusão às aulas e perguntas. Excluir uma conta pelo serviço também exclui logicamente os cursos e aulas vinculados.

[![Entidades de domínio, perfis, matrículas e progresso com atributos e cardinalidades](docs/diagramas/classes-de-dominio.png)](docs/diagramas/classes-de-dominio.png)

### Classes da aplicação

- Na área de conta, `UserController` e `AuthenticationController` expõem operações de usuário e sessão. `UserService` concentra as regras da conta e usa `AccountSecurityService` para tokens de confirmação e recuperação e para revogação de sessões.
- No catálogo, controllers chamam `CourseService`, `LessonService` e `QuestionService`. As perguntas e o conteúdo anterior controlam a progressão do aluno. Os serviços aplicam regras de permissão e propriedade, usam repositórios JPA e mapeiam entidades para DTOs com MapStruct, incluindo `QuestionMapper`.
- `LessonService` usa `LessonVideo` para validar vídeos do YouTube ou Cloudinary e gerar a miniatura. Não há aprovação de canais, filtro de conteúdo nem dependência de chave da API do YouTube.
- `LegalDocumentsService` publica e consulta versões imutáveis e registra os aceites. O interceptor de aceite retorna HTTP 428 quando há pendência para acessar os recursos da plataforma.
- O `AuthenticationEmailHandler` envia os e-mails transacionais pela API do Resend.
- O envio local também usa exclusivamente o Resend; não há transporte SMTP ou Mailpit. Os testes automatizados simulam o provedor.

[![Classes da aplicação para conta, educação e envio de e-mails](docs/diagramas/classes-da-aplicacao.png)](docs/diagramas/classes-da-aplicacao.png)

## Execução local

### Pré-requisitos

- Git, JDK 21 e Node.js 24 com npm. O Maven Wrapper está no repositório.
- PostgreSQL e Redis em execução. Docker é uma opção para iniciar os serviços locais e é usado pelos testes de integração.
- Resend configurado para enviar confirmação de cadastro, link de login e código de recuperação. Use uma caixa de e-mail à qual tenha acesso.

### 1. Baixar o projeto e iniciar os serviços

```bash
git clone https://github.com/GabrielYYM/INVESTIR-MAIS.git
cd INVESTIR-MAIS
```

Se já houver PostgreSQL e Redis disponíveis, configure seus endereços no passo seguinte. Para criar containers locais, com acesso restrito à máquina:

```bash
docker run -d --name investir-mais-postgres -p 127.0.0.1:5432:5432 -e POSTGRES_DB=investir_mais -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=admin -v investir-mais-postgres:/var/lib/postgresql/data postgres:17-alpine
docker run -d --name investir-mais-redis -p 127.0.0.1:6379:6379 -v investir-mais-redis:/data redis:8-alpine redis-server --appendonly yes
docker exec investir-mais-postgres pg_isready -U postgres -d investir_mais
docker exec investir-mais-redis redis-cli PING
```

A senha `admin` acima é exclusiva do exemplo local. Após a criação, use `docker start investir-mais-postgres investir-mais-redis` para iniciar os mesmos containers.

### 2. Configurar e executar o backend

Copie [`.env.example`](.env.example) para `.env` na raiz e substitua os valores de exemplo. Para usar o PostgreSQL dos comandos acima, configure `DB_URL=jdbc:postgresql://localhost:5432/investir_mais`, `DB_USERNAME=postgres` e `DB_PASSWORD=admin`. Variáveis também podem ser fornecidas no ambiente do processo.

| Variáveis | Uso local |
| --- | --- |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Acesso ao PostgreSQL. |
| `REDIS_HOST`, `REDIS_PORT` | Padrão `localhost:6379`. Configure usuário, senha e TLS se o Redis exigir. |
| `RESEND_API_KEY`, `RESEND_FROM` | Chave da API e endereço de remetente de um domínio verificado no Resend. Ambas são obrigatórias no backend. |
| `APP_PUBLIC_BASE_URL` | Origem pública do backend. Padrão `http://localhost:8080`. |
| `APP_CORS_ALLOWED_ORIGIN` | Origem da SPA e base dos links de confirmação e login. Padrão `http://localhost:5173`. |
| `SESSION_TIMEOUT` | Limite de inatividade da sessão. Padrão `30m`. |

No PowerShell:

```powershell
if (-not (Test-Path -LiteralPath .env)) { Copy-Item .env.example .env }
# Edite .env com os dados locais antes de iniciar.
.\mvnw.cmd spring-boot:run
```

No Linux ou macOS:

```bash
test -e .env || cp .env.example .env
# Edite .env com os dados locais antes de iniciar.
bash mvnw spring-boot:run
```

O backend sobe em [localhost:8080](http://localhost:8080) com perfil `dev` por padrão.

### 3. Executar o frontend

Em outro terminal, a partir da raiz:

```bash
cd frontend/investirMais
npm ci
npm run dev
```

Abra [localhost:5173](http://localhost:5173). O proxy do Vite encaminha `/api`, `/auth` e `/csrf` para o backend. Para testar o acesso, cadastre um aluno, confirme o e-mail e entre com a senha. Abra **Concluir acesso** no mesmo navegador ou use o código manual. Veja opções de build, rotas e contrato no [README do frontend](frontend/investirMais/README.md).

## API

No perfil `dev`, o Swagger UI fica em [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) e a descrição OpenAPI em [localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs). Os endpoints de login, OTT e logout pertencem aos filtros de segurança.

| Endpoint ou grupo | Responsabilidade |
| --- | --- |
| `GET /csrf` | Disponibiliza o token CSRF para operações de escrita. |
| `POST /auth/login`, `POST /auth/code`, `POST /auth/verify-2fa` | Início por senha, reenvio na sessão parcial e confirmação do OTT. Login e verificação usam parâmetros de formulário. |
| `GET /auth/me`, `POST /auth/logout` | Identifica o usuário com sessão completa e encerra a sessão. |
| `/api/users` | Cadastro, verificação de e-mail, reenvio, recuperação e operações da própria conta. |
| `/api/courses` | Listagem paginada, detalhe, filtro por professor e gestão de cursos. |
| `/api/lessons` | Aulas por curso, detalhe e operações de gestão. |
| `GET/PUT /api/lessons/course/{courseId}/order` | Consulta e atualização da sequência completa de aulas pelo professor responsável ou ADMIN. |
| `/api/courses/{courseId}/questions` | Gestão de perguntas, consulta sem gabarito para alunos e respostas que liberam a progressão. |
| `/api/legal/current`, `/api/legal/versions/{version}` | Textos públicos da versão atual ou de uma versão específica. |
| `GET /api/legal/status`, `GET/POST /api/legal/acceptances` | Pendência e histórico do usuário autenticado; registro de aceite por POST com CSRF. |

### Sessões, dados e operação

- O cookie `SESSION` é `HttpOnly` e usa `SameSite=Lax`. O cliente consulta `/csrf` para preparar escritas, inclusive quando o token é renovado entre etapas da autenticação.
- O Redis indexa sessões por usuário para revogá-las após alterações de senha ou e-mail e após exclusão de conta.
- O limite de autenticação usa Redis por IP e rota e também por conta em `/auth/login`. O padrão é 10 solicitações por janela de um minuto. O limite retorna `429` com `Retry-After`. Se o Redis estiver indisponível, o filtro retorna `503`.
- Tokens de login usam `one_time_tokens`. Tokens de confirmação e recuperação ficam em `app_one_time_token`, com hash SHA-256 para novas emissões e consumo transacional. O Spring Modulith registra e reprocessa eventos de confirmação e recuperação. O e-mail de login segue o fluxo assíncrono da autenticação.
- A API exige a senha atual para excluir a própria conta. A exclusão lógica preserva vínculos, limpa os dados pessoais do registro principal e libera o e-mail original; os snapshots históricos permanecem na auditoria. As operações do serviço cuidam da cascata e da revogação de acesso.
- Sem aceite da versão legal atual, os recursos protegidos retornam `428`; se a versão mudar durante a leitura, o aceite retorna `409`. Perfil, segurança, exclusão, documentos e saída continuam acessíveis. O aceite registra concordância com os Termos e ciência da Política, não um consentimento genérico para qualquer tratamento de dados.

### Produção

Use o perfil `prod` com PostgreSQL, Redis e HTTPS. Configure `RESEND_API_KEY` e `RESEND_FROM` no ambiente do backend. O remetente deve pertencer a um domínio verificado. [Documentação do Resend](https://resend.com/docs/introduction)

Configure `APP_PUBLIC_BASE_URL` como a origem pública do backend e `APP_CORS_ALLOWED_ORIGIN` como a origem pública da SPA quando forem diferentes. O perfil ativa cookies `Secure`, habilita TLS no Redis por padrão e desativa Swagger e `/v3/api-docs`.

O esquema das entidades usa `ddl-auto=update`. A inicialização também executa o [SQL auxiliar](src/main/resources/schema-postgresql.sql) e a [estrutura imutável dos documentos legais](src/main/resources/legal-history-postgresql.sql). Faça backup antes de mudanças de esquema e preserve os dados existentes. Veja os guias de [documentos legais](docs/documentos-legais.md) e [ordenação e perguntas](docs/questions.md). As opções de publicação da SPA e do proxy estão no [README do frontend](frontend/investirMais/README.md#publicação).

A inicialização não adapta esquemas legados nem realinha sequências antigas. Para reutilizar um banco existente, faça backup e prepare a migração necessária antes de executar esta versão; recrie o banco somente quando seus dados puderem ser descartados.

## Testes e verificações

| Verificação | Comando |
| --- | --- |
| Backend no Windows | `.\mvnw.cmd -B verify` na raiz |
| Backend no Linux ou macOS | `bash mvnw -B verify` na raiz |
| Build do frontend | `npm ci` e `npm run build` em `frontend/investirMais` |
| Testes e cobertura do frontend | `npm run test:coverage` em `frontend/investirMais` |
| Auditoria de dependências npm | `npm audit --audit-level=high` em `frontend/investirMais` |

A suíte inclui testes do fluxo de login com H2 e Redis simulado, além de testes de integração com PostgreSQL e Redis via Docker. Os testes com Testcontainers configurados com `disabledWithoutDocker` são ignorados quando Docker está indisponível. Execute a verificação completa com Docker disponível e mantenha esses testes em bancos isolados: nunca os aponte para o banco oficial local. O [workflow de CI](.github/workflows/checks.yml) executa as verificações em pushes e pull requests.

## Documentação complementar

- [Frontend, rotas e publicação](frontend/investirMais/README.md)
- [Diagramas e referências ao código](docs/diagrama-de-classes.md)
- [Perguntas, progressão e ordem das aulas](docs/questions.md)
- [Vídeos do YouTube e limites da validação](docs/video-validation.md)
- [Publicação e versionamento dos documentos legais](docs/documentos-legais.md)
- [Identificadores das revisões de auditoria](docs/auditoria-revisoes.md)
- [Apresentação e roteiro dos slides](docs/apresentacao.md)
- [Auditoria histórica de frontend e segurança](docs/auditoria-frontend-seguranca.md)
- [Privacidade e dados pessoais](docs/lgpd/README.md)
- [Licença](LICENSE)
