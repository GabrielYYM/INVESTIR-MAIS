# Investir Mais — frontend

React 19, React Router 8 (Data Mode), Vite 8 e Tailwind CSS 4. Requer Node.js 22.18+; Node 24 é compatível.

## Executar e verificar

```sh
npm ci
npm run dev
npm run check
```

Abra http://localhost:5173 e execute o backend Spring em http://localhost:8080.
O proxy do Vite encaminha `/auth` e `/api`; não é necessário `.env` no desenvolvimento local.
`npm run check` executa Knip, testes e build. O resultado de produção fica em `dist/`.

Em produção, encaminhe os mesmos caminhos ao backend por um proxy reverso. Para outra origem, configure `VITE_API_BASE_URL` no build e os cookies/CORS no servidor. Variáveis `VITE_*` são públicas; o token da Brapi pertence somente ao backend.

## Responsabilidades

- React Router fornece navegação, histórico, carregamento/cancelamento por loaders, envio por actions/Form/fetchers, revalidação após alterações e telas de erro.
- Curso aberto e paginação ficam na URL, como `#/cursos/{id}?page=2`. Voltar/avançar e atualização da página mantêm o endereço.
- Formulários usam validação HTML e o diálogo usa `<dialog>` nativo.
- `services/api.js` adapta o protocolo do Spring: sessão por cookie, CSRF pelo cookie `XSRF-TOKEN` e header `X-XSRF-TOKEN`, inicializado via `/auth/me` quando ausente, `ProblemDetail` e respostas vazias. Um 401 em escrita revalida `/auth/me` para distinguir sessão expirada de senha atual incorreta.
- `src/generated/spring.ts` contém o cliente e os modelos gerados pelo Orval a partir do OpenAPI real dos controllers Spring. Não editar manualmente.
- Os filtros nativos de login/OTT/logout e o endpoint de cotações continuam em adaptadores pequenos, pois não fazem parte do contrato CRUD exportado.

## Atualizar o cliente HTTP

Depois de alterar controllers/DTOs do backend:

```sh
npm run api:generate
npm run check
```

A geração exige o JDK do projeto (21+) e usa o Maven Wrapper para exportar o OpenAPI por MockMvc com os serviços simulados. Não exige PostgreSQL, Redis, SMTP nem backend em execução. O contrato intermediário fica em `target/openapi.json`, ignorado pelo Git. O arquivo gerado no frontend deve ser versionado.

O teste `OpenApiContractTest` também verifica nomes exclusivos dos schemas e a estrutura paginada, evitando colisões entre DTOs de usuário, curso e aula. O Knip exclui apenas o código gerado de seus relatórios, pois ele exporta o contrato inteiro do servidor; o código escrito à mão continua sendo verificado.

## Funcionalidades

- Login por senha e código de e-mail, reenvio de código e logout.
- Cadastro com idade, recuperação/redefinição de senha e reenvio de confirmação de e-mail.
- Cursos/aulas paginados com criação, edição e exclusão conforme perfil e propriedade.
- Cotações pelo endpoint Spring, usando o cache e a resiliência do backend.
- Edição de perfil, e-mail e senha; encerramento da sessão conforme o contrato do servidor.

A confirmação de e-mail usa o link enviado pelo backend (`/api/users/verify-email?token=...`). Carteira e questões, que não possuem APIs no backend atual, foram removidas junto com os componentes e serviços sem uso.

Os testes do frontend simulam respostas HTTP. Testes integrados de PostgreSQL/Redis no backend exigem Docker; não são substituídos por essas simulações.

Em produção, sirva o frontend e a API pelo mesmo domínio via proxy reverso para permitir a leitura do cookie CSRF. `VITE_API_BASE_URL` em outro domínio exige adaptar a entrega do token.
