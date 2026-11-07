# Investir Mais · Frontend

Aplicação React que consome a API Spring do Investir Mais. Reúne o painel de cotações, o catálogo educacional, o perfil e os fluxos de acesso por e-mail. O [README principal](../../README.md) descreve a arquitetura, a execução do backend e os diagramas.

## Desenvolvimento local

Use **Node.js 24** com npm, como na CI. Com o backend rodando em `http://localhost:8080`, execute a partir de `frontend/investirMais`:

```bash
npm ci
npm run dev
```

Abra [localhost:5173](http://localhost:5173). O proxy do Vite encaminha `/api`, `/auth` e `/csrf` ao Spring. PostgreSQL, Redis, SMTP e a integração com a Brapi são configurados no backend.

| Comando | Finalidade |
| --- | --- |
| `npm ci` | Instala as versões do `package-lock.json`. |
| `npm run dev` | Gera o cliente de educação e inicia o Vite. |
| `npm run build` | Gera o cliente de educação e compila o bundle em `dist/`. |
| `npm run preview` | Serve localmente o bundle gerado pelo build. |
| `npm run api:generate` | Regenera apenas o cliente OpenAPI de educação. |
| `npm test` | Executa todos os testes unitários e de integração uma vez. |
| `npm run test:unit` | Executa apenas os testes unitários. |
| `npm run test:integration` | Executa apenas os testes de integração do frontend. |
| `npm run test:watch` | Reexecuta os testes durante a edição dos arquivos. |
| `npm run test:coverage` | Executa a suíte e gera relatórios de cobertura. |

## Testes

Os testes usam Vitest, React Testing Library e JSDOM. Todos os comandos de teste regeneram o cliente OpenAPI antes da execução. Não é necessário iniciar o backend, Docker ou serviços externos.

- `src/test/unit/`: validação de URLs de vídeo, mensagens HTTP, preparação de CSRF, permissões, loaders, ações, paginação e componentes de formulário.
- `src/test/integration/`: formulários de cadastro, login por senha e OTT, link de acesso, recuperação de senha, perfil, sessão, cotações e gestão de cursos e aulas. Os testes usam as mesmas rotas da aplicação, Axios e o cliente Orval; o MSW intercepta apenas a camada HTTP, com dados fictícios.
- `src/test/server.js` e `src/test/fixtures.js`: respostas padrão e dados compartilhados. Cada cenário pode substituir os handlers com `server.use()`. Requisições sem handler causam erro, e handlers, cookies, DOM e roteadores são limpos entre os testes.

Os testes de integração verificam a colaboração entre as partes do frontend com a API simulada. A integração com Spring e infraestrutura real continua coberta pela suíte do backend.

`npm run test:coverage` gera `coverage/index.html` e `coverage/lcov.info`. A CI executa esse comando e exige os mínimos globais de 85% para instruções, funções e linhas e 80% para ramificações. O relatório inclui todo o código da aplicação, exceto o ponto de montagem `main.jsx`, o cliente gerado e os arquivos de teste.

## Rotas e permissões

| Rota | Acesso | Função |
| --- | --- | --- |
| `/cadastro` | Público | Cadastro de aluno e reenvio da confirmação de e-mail. |
| `/entrar` | Público | Validação da senha e opção de confirmar o OTT manualmente. |
| `/entrar/link` | Público | Recebe o token do e-mail e conclui o login na sessão correspondente. |
| `/recuperar-senha` | Público | Solicita o código e define uma nova senha. |
| `/` | Sessão completa | Painel com cursos em destaque e consulta de cotações. |
| `/educacao` | Sessão completa | Catálogo paginado de cursos e criação de curso para perfis autorizados. |
| `/educacao/:courseId/:lessonId?` | Sessão completa | Detalhe do curso, lista de aulas e reprodução da aula selecionada. |
| `/perfil` | Sessão completa | Exibe os dados da conta e permite editar o nome com a senha atual. |

A consulta de cotação usa o loader interno `/cotacao`. A saída é uma ação em `/sair`, que chama `POST /auth/logout`. Essas rotas dão suporte à interface e são acionadas pelo painel e pelo menu.

O layout autenticado carrega a identidade por `GET /auth/me`. Se o backend responder `401` ou `403`, o React Router envia o usuário ao login com o caminho de retorno. Alunos consultam o catálogo. Professores criam cursos e gerenciam os próprios cursos e aulas. Administradores gerenciam todo o catálogo. As permissões são verificadas também pelo backend.

## Sessão e chamadas HTTP

Depois da senha, o e-mail de login oferece **Concluir acesso** e a opção de código manual. O link deve ser aberto no mesmo navegador em que a senha foi informada. O frontend lê o token do fragmento da URL, remove o fragmento do endereço e envia o token ao Spring. Após o sucesso, navega para uma rota interna permitida ou para o painel. O [BPMN de login](../../README.md#bpmn-do-login-com-senha-e-ott) mostra o fluxo completo.

A instância Axios compartilhada envia cookies de sessão. Em escritas, consulta `GET /csrf` para obter o token atual e envia `X-XSRF-TOKEN`. No acesso pela mesma origem, o Axios usa o cookie `XSRF-TOKEN`. Quando a API está em outra origem configurada, o cliente usa o token recebido na resposta de `/csrf`. Respostas `429` podem exibir o tempo de espera informado em `Retry-After`.

## Organização do código

| Arquivo ou diretório | Papel |
| --- | --- |
| [`src/App.jsx`](src/App.jsx) e [`src/routes.jsx`](src/routes.jsx) | Montagem do roteador, rotas, verificação de sessão e tratamento de erros de navegação. |
| [`src/pages/`](src/pages/) | Telas, formulários e ações dos fluxos da aplicação. |
| [`src/components/`](src/components/) | Layout, navegação, estados de página, formulários e componentes de educação. |
| [`src/services/api.js`](src/services/api.js) | Instância Axios, credenciais, CSRF e mensagens de erro. |
| [`src/services/educationData.js`](src/services/educationData.js) | Loaders, paginação, ações e permissões da interface de educação. |
| [`src/services/youtube.js`](src/services/youtube.js) | Validação e transformação de endereços de vídeo usados pela interface. |
| [`education.openapi.json`](education.openapi.json) | Contrato OpenAPI de cursos e aulas usado pelo Orval. |

## Contrato da API de educação

O Orval gera o cliente de cursos e aulas antes de `npm run dev` e `npm run build`. O cliente usa a mesma instância Axios dos demais fluxos. O código gerado fica fora do Git. Quando os endpoints ou DTOs mudarem, gere novamente o contrato a partir da raiz do repositório, com Docker disponível:

```bash
bash mvnw -Dtest=AccountLifecycleIntegrationTest#educationOpenApiIncludesBothResources \
  -Deducation.openapi.output=frontend/investirMais/education.openapi.json test
```

No PowerShell:

```powershell
.\mvnw.cmd '-Dtest=AccountLifecycleIntegrationTest#educationOpenApiIncludesBothResources' '-Deducation.openapi.output=frontend/investirMais/education.openapi.json' test
```

Versione o contrato atualizado junto com a mudança da API. Depois, rode `npm run build` para validar a geração e a compilação.

## Videoaulas

Para cadastrar uma aula, envie o vídeo pelo YouTube Studio e cole o endereço no formulário da aula. O frontend envia ao Spring a URL do vídeo e a URL derivada da miniatura. O arquivo continua hospedado no YouTube.

O player integrado depende das permissões de incorporação do vídeo. Para um vídeo privado, convide os alunos no YouTube Studio. Na aula, eles podem usar **Abrir no YouTube** e entrar com a conta convidada. O acesso ao curso no Investir Mais e a reprodução no YouTube são permissões separadas.

## Publicação

| Variável | Onde definir | Comportamento |
| --- | --- | --- |
| `VITE_DEV_API_TARGET` | Ambiente local do Vite | Destino do proxy de desenvolvimento e preview. Padrão `http://localhost:8080`. |
| `VITE_API_BASE_URL` | Ambiente do build | URL base da API para frontend e backend em origens distintas. Sem valor, o cliente usa caminhos relativos à origem da SPA. |

`npm run build` gera os arquivos estáticos em `dist/`. Em uma hospedagem na mesma origem, encaminhe `/api`, `/auth` e `/csrf` para o Spring. Sirva `index.html` como fallback das rotas da SPA, inclusive quando alguém abre `/entrar/link` ou uma aula diretamente.

Se frontend e API usarem origens distintas dentro do mesmo site, configure `VITE_API_BASE_URL` no build e `APP_CORS_ALLOWED_ORIGIN` no backend com a origem exata da SPA. O navegador aplica as regras de envio do cookie `SameSite=Lax`. Os links de login usam `APP_CORS_ALLOWED_ORIGIN`, e os links de confirmação de e-mail usam `APP_PUBLIC_BASE_URL`.

No proxy ou CDN, use HTTPS e configure HSTS, `X-Content-Type-Options: nosniff` e `Referrer-Policy: strict-origin-when-cross-origin`. Para uma CSP da SPA, uma base é `default-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; frame-src https://www.youtube-nocookie.com`. Inclua em `connect-src` a origem da API quando estiver em outro host. Teste a política em modo de relatório antes de aplicá-la. Limite os corpos de requisições na borda e exclua o parâmetro `token` dos logs de acesso de `/api/users/verify-email`.
