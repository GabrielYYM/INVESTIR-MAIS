# Investir Mais · Frontend

Aplicação React que consome a API Spring do Investir Mais. Reúne o painel, o catálogo educacional, o perfil e os fluxos de acesso por e-mail. O [README principal](../../README.md) descreve a arquitetura, a execução do backend e os diagramas.

## Desenvolvimento local

Use **Node.js 24** com npm, como na CI. Com o backend rodando em `http://localhost:8080`, execute a partir de `frontend/investirMais`:

```bash
npm ci
npm run dev
```

Abra [localhost:5173](http://localhost:5173). O proxy do Vite encaminha `/api`, `/auth` e `/csrf` ao Spring. PostgreSQL, Redis e o provedor de e-mail são configurados no backend.

| Comando | Finalidade |
| --- | --- |
| `npm ci` | Instala as versões do `package-lock.json`. |
| `npm run dev` | Gera o cliente de educação e inicia o Vite. |
| `npm run build` | Gera o cliente de educação e compila o bundle em `dist/`. |
| `npm run preview` | Serve localmente o bundle gerado pelo build. |
| `npm run api:generate` | Regenera apenas o cliente OpenAPI de educação. |

## Testes e cobertura

Os testes usam Vitest, React Testing Library e `user-event`. O MSW intercepta as chamadas HTTP, permitindo testar as rotas, formulários e serviços reais sem iniciar o backend.

```bash
npm test
npm run test:watch
npm run test:coverage
```

O comando de cobertura mostra um resumo no terminal e gera `reports/coverage/index.html`, ignorado pelo Git. Os mínimos são 85% para linhas, instruções e funções e 80% para ramificações.

Os testes de integração compartilham três helpers:

- `renderApp(rota, { session: usuario })`: monta as rotas reais com o perfil desejado.
- `mockApi(metodo, caminho, resposta, opcoes)`: simula um endpoint e retorna um spy; aceita `status`, `headers` e `once`.
- `fillFields(user, campos, escopo)`: preenche os campos pelos rótulos, usando `screen` como escopo padrão.

Reutilize os dados de `src/test/fixtures.js` e as respostas padrão de `src/test/server.js`. O setup limpa o estado de cada teste e rejeita requisições sem handler, evitando chamadas à API real.

## Rotas e permissões

| Rota | Acesso | Função |
| --- | --- | --- |
| `/cadastro` | Público | Cadastro de aluno e reenvio da confirmação de e-mail. |
| `/entrar` | Público | Validação da senha e opção de confirmar o OTT manualmente. |
| `/entrar/link` | Público | Recebe o token do e-mail e conclui o login na sessão correspondente. |
| `/recuperar-senha` | Público | Solicita o código e define uma nova senha. |
| `/` | Sessão completa | Painel com cursos em destaque e métricas conforme o perfil. |
| `/educacao` | Sessão completa | Catálogo paginado de cursos e criação de curso para perfis autorizados. |
| `/educacao/:courseId/:lessonId?` | Sessão completa | Detalhe do curso, lista de aulas e reprodução da aula selecionada. |
| `/perfil` | Sessão completa | Exibe os dados da conta e permite editar o nome com a senha atual. |

A saída é uma ação em `/sair`, que chama `POST /auth/logout`.

O layout autenticado carrega a identidade por `GET /auth/me`. Se o backend responder `401` ou `403`, o React Router envia o usuário ao login com o caminho de retorno. Alunos consultam o catálogo. Professores criam cursos e gerenciam os próprios cursos e aulas. Administradores gerenciam todo o catálogo. As permissões são verificadas também pelo backend.

## Sessão e chamadas HTTP

Depois da senha, o e-mail de login oferece **Concluir acesso** e a opção de código manual. O link deve ser aberto no mesmo navegador em que a senha foi informada. O frontend lê o token do fragmento da URL, remove o fragmento do endereço e envia o token ao Spring. Após o sucesso, navega para uma rota interna permitida ou para o painel. O [BPMN de login](../../README.md#bpmn-do-login-com-senha-e-ott) mostra o fluxo completo.

A instância Axios compartilhada envia cookies de sessão. Em escritas, consulta `GET /csrf` para obter o token atual e envia `X-XSRF-TOKEN`. No acesso pela mesma origem, o Axios usa o cookie `XSRF-TOKEN`. Quando a API está em outra origem configurada, o cliente usa o token recebido na resposta de `/csrf`. Respostas `429` podem exibir o tempo de espera informado em `Retry-After`.

## Organização do código

| Arquivo ou diretório | Papel |
| --- | --- |
| [`src/App.jsx`](src/App.jsx) | Rotas, verificação de sessão e tratamento de erros de navegação. |
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

Se frontend e API usarem origens distintas dentro do mesmo site, configure `VITE_API_BASE_URL` no build e `APP_CORS_ALLOWED_ORIGIN` no backend com a origem exata da SPA. O navegador aplica as regras de envio do cookie `SameSite=Lax`. Os links de login e confirmação de e-mail usam `APP_CORS_ALLOWED_ORIGIN`.

No proxy ou CDN, use HTTPS e configure HSTS, `X-Content-Type-Options: nosniff` e `Referrer-Policy: strict-origin-when-cross-origin`. Para uma CSP da SPA, uma base é `default-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; frame-src https://www.youtube-nocookie.com`. Inclua em `connect-src` a origem da API quando estiver em outro host. Teste a política em modo de relatório antes de aplicá-la. Limite os corpos de requisições na borda e exclua o parâmetro `token` dos logs de acesso de `/confirmar-email`.
