# Investir+ · Frontend

Painel de investimentos e área de educação em React, Vite e Tailwind. O frontend usa o backend Spring para autenticação, cotações, cursos e aulas.

## Desenvolvimento

```bash
npm ci
npm run dev
```

Abra http://localhost:5173 com o backend rodando em http://localhost:8080. O Vite encaminha `/api`, `/auth` e `/csrf` para o Spring. Ajuste `VITE_DEV_API_TARGET` se o backend local estiver em outro endereço. O token da Brapi é configurado somente no backend.

Use `npm run build` para gerar `dist/`. Em produção, encaminhe `/api/*`, `/auth/*` e `/csrf` ao Spring e sirva `index.html` para as rotas da aplicação. Se frontend e API usarem origens distintas dentro do mesmo site, configure `VITE_API_BASE_URL` no build e a origem do frontend no CORS do Spring. A configuração atual usa cookies de sessão `SameSite=Lax`.

## Videoaulas

Professores podem gerenciar seus cursos e aulas. Administradores podem gerenciar todo o catálogo. Para publicar uma aula, envie o vídeo pelo YouTube Studio e cole o endereço do vídeo no formulário. O frontend envia ao Spring a URL do vídeo e a URL derivada da miniatura. O arquivo permanece no YouTube.

Para vídeos privados, convide os alunos pelos respectivos e-mails no YouTube Studio. Na aula, eles podem usar **Abrir no YouTube** e entrar com a conta convidada. O player integrado depende de o YouTube permitir a incorporação do vídeo. A permissão de leitura do curso no Investir+ e a permissão de reprodução no YouTube são independentes.
