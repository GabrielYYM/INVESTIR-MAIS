# Denúncias, categorias e lixeira

Na página do curso, os botões **Denunciar vídeo** e **Denunciar professor** permitem selecionar motivo e descrever o problema. O servidor valida o vínculo do alvo com o curso, impede denúncias abertas duplicadas pelo mesmo usuário e limita a dez submissões em 24 horas. A resposta ao denunciante contém somente protocolo e situação.

O admin acessa `/admin/denuncias`. A fila separa denúncias em análise, resolvidas e arquivadas. Exige parecer e confirmação antes de retirar uma aula e suas questões vinculadas. O arquivo externo não é removido por essa ação. A denúncia, seus snapshots e a análise são auditados. O professor não recebe a identificação do denunciante pelas APIs.

Cursos usam uma seleção de categorias: Investimentos, Renda fixa, Renda variável, Fundos imobiliários, Fundos de investimento, Tesouro Direto, Finanças pessoais, Planejamento financeiro, Economia e Geral. Categorias antigas continuam disponíveis ao editar o respectivo curso, sem migração destrutiva. Palavras-chave antigas são preservadas no banco; o formulário não as solicita mais.

O cadastro inicial apresenta links para leitura dos documentos. Há um único aceite persistido após a confirmação do e-mail; novas versões continuam exigindo nova confirmação.

## Lixeira administrativa

- `/admin/lixeira` lista conteúdo excluído e, em uma seleção separada, contas com solicitação recuperável.
- Cursos, aulas e questões podem ser restaurados individualmente. Restaurar curso não republica automaticamente aulas e questões. O servidor exige conta responsável e dependências ativas e recusa conflitos de posição.
- Contas desativadas mantêm dados por 30 dias a partir da solicitação. A recuperação exige registro do pedido verificado do titular. Também recupera o progresso e os itens de autoria ocultados pela mesma solicitação, preservando exclusões anteriores.
- A opção de eliminação definitiva exige senha e não cria janela recuperável. A rotina horária processa solicitações vencidas. Veja os limites e a matriz legal em [retenção e eliminação](../.lgpd/retention.md).

## APIs

| Método | Rota | Acesso |
|---|---|---|
| POST | `/api/reports` | Usuário autenticado |
| GET | `/api/reports?status=OPEN&page=0&size=20` | Admin |
| POST | `/api/reports/{id}/review` | Admin |
| GET | `/api/trash`, `/api/trash/accounts` | Admin |
| POST | `/api/trash/{entity}/{id}/restore` | Admin |
| POST | `/api/trash/accounts/{id}/restore` | Admin; justificativa obrigatória |
| DELETE | `/api/users/{id}` | Próprio titular com senha; desativa por 30 dias |
| POST | `/api/users/{id}/erasure` | Próprio titular com senha; eliminação definitiva |

## Verificação

`ModerationAndRecoveryIntegrationTest` usa PostgreSQL isolado, com envio de e-mail simulado. Usa Testcontainers por padrão. Opcionalmente, `INVESTIR_TEST_DATABASE_URL` aceita somente PostgreSQL em `127.0.0.1`, com banco cujo nome começa por `investir_test_`, para execução automatizada em uma instância descartável com usuário `postgres` e autenticação local de teste. Nunca usar esse mecanismo para testes manuais nem apontar para `investir_mais`.

Os testes de frontend cobrem denúncia de vídeo/professor, validação e erros, confirmação de moderação, recuperação administrativa, segregação de perfis, categorias selecionáveis e ausência de aceite duplicado.
