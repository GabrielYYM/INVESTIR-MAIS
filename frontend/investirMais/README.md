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
| `npm run dev` | Gera os clientes da API e inicia o Vite. |
| `npm run build` | Gera os clientes da API e compila o bundle em `dist/`. |
| `npm run preview` | Serve localmente o bundle gerado pelo build. |
| `npm run api:generate` | Regenera clientes, hooks e schemas Zod de conta e educação. |

## Testes e cobertura

Os testes usam Vitest, React Testing Library e `user-event`. O MSW intercepta as chamadas HTTP, permitindo testar as rotas, formulários e serviços reais sem iniciar o backend.

```bash
npm test
npm run test:watch
npm run test:coverage
```

O comando de cobertura mostra um resumo no terminal e gera `reports/coverage/index.html`, ignorado pelo Git. Os mínimos são 85% para linhas, instruções e funções e 80% para ramificações.

O Vitest executa até duas suítes em paralelo para limitar o uso de memória pelo jsdom e pelos componentes Material UI, especialmente durante a cobertura.

Os testes de integração compartilham três helpers:

- `renderApp(rota, { session: usuario })`: monta as rotas reais com o perfil desejado.
- `mockApi(metodo, caminho, resposta, opcoes)`: simula um endpoint e retorna um spy; aceita `status`, `headers` e `once`.
- `fillFields(user, campos, escopo)`: preenche os campos pelos rótulos, usando `screen` como escopo padrão.

Reutilize os dados de `src/test/fixtures.js` e as respostas padrão de `src/test/server.js`. O setup limpa o estado de cada teste e rejeita requisições sem handler, evitando chamadas à API real.

## Rotas e permissões

| Rota | Acesso | Função |
| --- | --- | --- |
| `/cadastro` | Público | Cadastro de aluno com data de nascimento e reenvio da confirmação de e-mail. |
| `/confirmar-email` | Público | Confirma o e-mail; o link original pode iniciar a primeira sessão no navegador do cadastro. |
| `/entrar` | Público | Validação da senha e opção de confirmar o OTT manualmente. |
| `/entrar/link` | Público | Recebe o token do e-mail e conclui o login na sessão correspondente. |
| `/recuperar-senha` | Público | Solicita o código e define uma nova senha. |
| `/` | Sessão completa | Painel com cursos em destaque e métricas conforme o perfil. |
| `/educacao` | Sessão completa | Catálogo paginado de cursos e criação de curso para perfis autorizados. |
| `/educacao/:courseId/:lessonId?` | Sessão completa | Detalhe do curso, lista de aulas e reprodução da aula selecionada. |
| `/aceitar-documentos` | Sessão completa | Leitura e confirmação da versão atual dos Termos e da Política de Privacidade. |
| `/termos`, `/termos-de-uso`, `/politica-de-privacidade` | Público | Textos legais; versões anteriores podem ser abertas pelo histórico do perfil. |
| `/perfil` | Sessão completa | Nome e nascimento com senha atual, gestão de senha, exclusão e histórico de aceites legais. |
| `/admin` | Administrador | Consulta usuários e auditoria e altera papéis autorizados. |

A saída é uma ação em `/sair`, que chama `POST /auth/logout`.

O layout autenticado carrega a identidade por `GET /auth/me`. Se o backend responder `401` ou `403`, o React Router envia o usuário ao login com o caminho de retorno. Alunos consultam o catálogo, matriculam-se e avançam ao concluir aulas e acertar perguntas. Professores gerenciam os próprios cursos, aulas, perguntas e ordem das aulas. Administradores gerenciam todo o catálogo e também podem ordenar as aulas. As permissões são verificadas pelo backend. A promoção a professor exige nascimento informado e idade mínima de 18 anos; o painel não concede nem altera o papel ADMIN.

Antes de abrir os recursos da plataforma, o layout consulta `/api/legal/status`. Uma pendência leva a `/aceitar-documentos`, preservando o retorno interno. O backend também aplica a restrição com `428`; o aceite de uma versão que deixou de ser a atual retorna `409` e exige a leitura da nova versão. Perfil, segurança, exclusão, documentos e saída permanecem disponíveis. Os textos, versões e aceites são consultados na API e persistidos no PostgreSQL. Veja o [guia de publicação](../../docs/documentos-legais.md).

## Sessão e chamadas HTTP

Depois da senha, o e-mail de login oferece **Concluir acesso** e a opção de código manual. O link deve ser aberto no mesmo navegador em que a senha foi informada. O frontend lê o token do fragmento da URL, remove o fragmento do endereço e envia o token ao Spring. Após o sucesso, navega para uma rota interna permitida ou para o painel. O [BPMN de login](../../README.md#bpmn-do-login-com-senha-e-ott) mostra o fluxo completo.

A instância Axios compartilhada envia cookies de sessão. Em escritas, consulta `GET /csrf` para obter o token atual e envia `X-XSRF-TOKEN`. No acesso pela mesma origem, o Axios usa o cookie `XSRF-TOKEN`. Quando a API está em outra origem configurada, o cliente usa o token recebido na resposta de `/csrf`. Respostas `429` podem exibir o tempo de espera informado em `Retry-After`.

## Organização do código

| Arquivo ou diretório | Papel |
| --- | --- |
| [`src/routes.jsx`](src/routes.jsx) | Rotas, verificação de sessão e tratamento de erros de navegação. |
| [`src/services/dataProvider.js`](src/services/dataProvider.js) | Adapta o CRUD do React-admin à API Spring e normaliza paginação e erros. |
| [`src/components/ui/CrudDialog.jsx`](src/components/ui/CrudDialog.jsx) | Editor e confirmação de exclusão compartilhados pelos recursos. |
| [`src/pages/`](src/pages/) | Telas, formulários e ações dos fluxos da aplicação. |
| [`src/components/`](src/components/) | Layout, navegação, estados de página, formulários e componentes de educação. |
| [`src/services/api.js`](src/services/api.js) | Instância Axios, credenciais, CSRF e mensagens de erro. |
| [`src/services/educationData.js`](src/services/educationData.js) | Loaders, paginação, ações e permissões da interface de educação. |
| [`src/services/youtube.js`](src/services/youtube.js) | Validação e transformação de endereços de vídeo usados pela interface. |
| [`education.openapi.json`](education.openapi.json) | Contrato OpenAPI de cursos, aulas e perguntas usado pelo Orval. |
| [`account.openapi.json`](account.openapi.json) | Contrato de conta, usuários, auditoria e métricas usado pelo Orval. |
| [`src/services/legalService.js`](src/services/legalService.js) | Consulta de textos, pendência e histórico; registro de aceite da versão atual. |

## Componentes e formulários

A interface preserva o layout anterior à reconstrução: navegação, cartões, espaçamentos, cores e páginas usam os estilos do **Tailwind CSS 4**. Os campos, modais, abas, tabelas e paginação continuam usando **Material UI**, com apresentação ajustada em `UiProvider`. A ordem das camadas CSS é declarada no início de `index.html` para que o reset do Tailwind não sobrescreva os componentes. Ant Design e a integração `react-hook-form-antd` continuam removidos.

O **React-admin** assume os formulários CRUD (`CreateBase`, `EditBase`, `SimpleForm`), as listas dinâmicas de perguntas e alternativas (`ArrayInput`, `SimpleFormIterator`) e as tabelas com filtros, ordenação e paginação (`ListBase`, `DataTable`). `CrudDialog` centraliza envio, validação, falhas e confirmação de exclusão. Os editores, a administração, o painel do professor e os termos são carregados sob demanda.

`dataProvider.js` concentra a adaptação dos recursos `courses`, `lessons`, `questions`, `users` e `audit` aos clientes gerados pelo Orval. Cursos e aulas usam a paginação da API. Usuários e auditoria são paginados e filtrados no cliente porque seus endpoints retornam listas completas. O histórico é produzido pelo Hibernate Envers no backend; o frontend apenas consulta e apresenta os snapshots.

As validações de conta e educação são geradas em `src/generated/*.zod.ts` pelo Orval. Os schemas dos editores acrescentam mensagens em português e regras de apresentação. As alternativas são `options: string[]`, com 2 a 6 textos distintos e um `correctOptionIndex` a partir de zero. A posição da aula começa em 1; `afterLesson: 0` significa antes da primeira aula, e `position` da pergunta ordena perguntas no mesmo ponto a partir de zero. A ordenação envia a lista completa de IDs e preserva o vínculo das perguntas com as aulas. A aprovação e o bloqueio da próxima aula são responsabilidade do backend; as respostas marcadas e o número de tentativas não são persistidos.

Os formulários de conta usam `accountSchema.js`, que reaproveita os schemas Zod gerados pelo Orval e acrescenta normalização, mensagens em português, aceite dos termos e confirmação de senha. Nome, e-mail e código são aparados; senhas mantêm seus espaços. `PasswordFields` compartilha os campos de nova senha e confirmação entre cadastro, recuperação e perfil. Os painéis do perfil mantêm as classes visuais originais, sem restaurar os antigos controladores e validações manuais.

Cadastro, recuperação, alteração de senha e atualização do perfil usam os hooks gerados pelo Orval para envio, carregamento, erros e sucesso. Após editar nome ou nascimento, o perfil revalida a identidade do layout. Uma resposta `401` na atualização só redireciona ao login quando uma nova consulta à sessão confirma sua expiração; senha incorreta e falha de conexão preservam os dados digitados. A exclusão exige confirmação e senha atual, bloqueando fechamento e reenvio durante a operação. O Day.js valida datas reais e o limite de seis meses de calendário do painel.

O nuqs centraliza os parâmetros de período e paginação em `src/services/searchParams.js`, com adaptador do React Router 7 e `shallow: false` para consultar as métricas ao aplicar o período. Os campos em edição não alteram a URL até o envio. A navegação preserva os outros parâmetros e o histórico do navegador.

`ApplicationProviders` configura o React-admin, a tradução brasileira e o cliente TanStack Query em memória. Consultas e mutações sem observadores são descartadas imediatamente, não há tentativas automáticas nem atualização ao focar a janela. Fechar a auditoria cancela a chamada pendente; reabri-la consulta o histórico atualizado. Os loaders do React Router continuam compondo os dados das páginas de aprendizagem, enquanto o CRUD usa o Data Provider.

## Contratos da API

O Orval também gera `src/generated/account.ts` a partir de `account.openapi.json`, cobrindo usuários, sessão atual, auditoria e métricas, com funções Axios e hooks TanStack Query. Os dois clientes usam a mesma instância Axios, preservando cookies e CSRF. Login, confirmação do segundo fator e logout continuam nos endpoints dos filtros do Spring Security.

Para atualizar o contrato de conta/painel com o backend local em execução:

```bash
npm run api:export
npm run api:generate
```

O exportador apenas lê `/v3/api-docs`, remove o endereço do servidor e inclui os schemas utilizados. A origem padrão é `http://localhost:8080`; `API_DOCS_ORIGIN` permite configurar outro ambiente de desenvolvimento. Versione o contrato, e não os clientes gerados.

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

Para cadastrar uma aula, o professor responsável ou ADMIN cola um link de vídeo do YouTube no formulário. São aceitos vídeos de qualquer canal. O arquivo continua hospedado no YouTube; não há upload de arquivos para a plataforma.

O frontend valida o formato do link. O backend valida novamente, normaliza a URL e gera a miniatura pelo ID do vídeo. Não há aprovação de canais, filtro de conteúdo, consulta de disponibilidade nem configuração de `YOUTUBE_API_KEY`. Quem publica deve conferir o conteúdo e a autorização de uso.

O player e a opção **Abrir no YouTube** dependem das condições do vídeo no YouTube, incluindo privacidade, incorporação e restrições de região ou idade. O acesso ao curso no Investir Mais e a reprodução no YouTube são permissões separadas. Veja os [limites da validação](../../docs/video-validation.md) e o [guia de perguntas e ordenação](../../docs/questions.md).

## Publicação

| Variável | Onde definir | Comportamento |
| --- | --- | --- |
| `VITE_DEV_API_TARGET` | Ambiente local do Vite | Destino do proxy de desenvolvimento e preview. Padrão `http://localhost:8080`. |
| `VITE_API_BASE_URL` | Ambiente do build | URL base da API para frontend e backend em origens distintas. Sem valor, o cliente usa caminhos relativos à origem da SPA. |

`npm run build` gera os arquivos estáticos em `dist/`. Em uma hospedagem na mesma origem, encaminhe `/api`, `/auth` e `/csrf` para o Spring. Sirva `index.html` como fallback das rotas da SPA, inclusive quando alguém abre `/entrar/link` ou uma aula diretamente.

Se frontend e API usarem origens distintas dentro do mesmo site, configure `VITE_API_BASE_URL` no build e `APP_CORS_ALLOWED_ORIGIN` no backend com a origem exata da SPA. O navegador aplica as regras de envio do cookie `SameSite=Lax`. Os links de login e confirmação de e-mail usam `APP_CORS_ALLOWED_ORIGIN`.

No proxy ou CDN, use HTTPS e configure HSTS, `X-Content-Type-Options: nosniff` e `Referrer-Policy: strict-origin-when-cross-origin`. Para uma CSP da SPA, uma base é `default-src 'self'; object-src 'none'; base-uri 'none'; frame-ancestors 'none'; frame-src https://www.youtube-nocookie.com`. Inclua em `connect-src` a origem da API quando estiver em outro host. Teste a política em modo de relatório antes de aplicá-la. Limite os corpos de requisições na borda e exclua o parâmetro `token` dos logs de acesso de `/confirmar-email`.
