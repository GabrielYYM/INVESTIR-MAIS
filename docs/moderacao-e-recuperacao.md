# Denúncias, categorias e lixeira

Na página do curso, os botões **Denunciar vídeo** e **Denunciar professor** permitem selecionar motivo e descrever o problema. O servidor valida o vínculo do alvo com o curso, impede denúncias abertas duplicadas pelo mesmo usuário e limita a dez submissões em 24 horas. A resposta ao denunciante contém somente protocolo e situação.

O admin acessa `/admin/denuncias`. A fila separa denúncias em análise, resolvidas e arquivadas. Exige parecer e confirmação antes de retirar uma aula e suas questões vinculadas. O arquivo externo não é removido por essa ação. A denúncia, seus snapshots e a análise são auditados. O professor não recebe a identificação do denunciante pelas APIs.

Cursos usam uma seleção de categorias: Investimentos, Renda fixa, Renda variável, Fundos imobiliários, Fundos de investimento, Tesouro Direto, Finanças pessoais, Planejamento financeiro, Economia e Geral. Categorias antigas continuam disponíveis ao editar o respectivo curso, sem migração destrutiva. Palavras-chave antigas são preservadas no banco; o formulário não as solicita mais.

O cadastro inicial apresenta links para leitura dos documentos. Há um único aceite persistido após a confirmação do e-mail; novas versões continuam exigindo nova confirmação.

## Lixeira administrativa

`/admin/lixeira` lista somente cursos, aulas e questões excluídos. A restauração exige conta responsável e dependências ativas e recusa conflitos de posição. Restaurar curso não republica automaticamente aulas e questões. Contas excluídas não são recuperáveis; atalhos antigos não oferecem restauração de conta.

## Exclusão de conta e auditoria

A exclusão é apenas lógica (soft delete): marca `deleted_at`, preservando a linha da conta e seus vínculos. Exige senha atual e suprime nome, e-mail, senha e verificação no cadastro imediatamente. O endereço é substituído por um valor aleatório em `deleted.invalid`, liberando o e-mail original sem quebrar o índice único. Um novo cadastro exige nova confirmação, recebe outro UUID e não herda progresso, papel ou histórico.

As linhas de auditoria, revisões, datas, operações, vínculos, progresso, tentativas, denúncias e aceites permanecem. Nas revisões do perfil, nome e e-mail são substituídos; o autor identificado pelo e-mail atual passa a `deleted:<UUID>`. Documentos de candidatura e credenciais pendentes são eliminados. Textos livres históricos podem conter dados pessoais: isso é minimização dos identificadores diretos e **não comprova anonimização irreversível de todo o histórico**.

Em **Administração → Auditoria**, o admin vê quem consultou, quando e quais registros foram retornados, unificados aos demais eventos da plataforma. O servidor registra listagens, detalhes, histórico individual e consultas ao próprio log. A identidade vem da autenticação; o cliente não fornece autor nem horário. Falha ao registrar impede a resposta com os dados. A tabela de visualizações bloqueia UPDATE, DELETE e TRUNCATE, inclusive após exclusão do visualizador. Nome/e-mail exibidos são obtidos do cadastro atual, sem duplicá-los no log.

## Painel de pendências

`/admin` reúne denúncias abertas e candidaturas pendentes de contas ativas. Os campos antigos de recuperação na resposta permanecem zerados para compatibilidade; não há prazos de recuperação nem job de expurgo de contas.

## APIs

| Método | Rota | Acesso |
|---|---|---|
| GET | `/api/admin/pending` | Admin; denúncias e candidaturas |
| POST | `/api/reports` | Usuário autenticado |
| GET | `/api/reports?status=OPEN&page=0&size=20` | Admin |
| POST | `/api/reports/{id}/review` | Admin |
| GET | `/api/trash` | Admin; somente conteúdo |
| POST | `/api/trash/{entity}/{id}/restore` | Admin |
| DELETE | `/api/users/{id}` | Próprio titular com senha; exclusão imediata |
| GET | `/api/audit` | Admin; consulta registrada |
| GET | `/api/audit/{entity}/{entityId}/{revision}` | Admin; detalhe registrado |
| GET | `/api/audit/accesses?viewerId=...&from=...&to=...&page=0&size=20` | Admin; log paginado e consulta registrada |

As rotas de recuperação foram removidas. Exclusões antigas que ainda reservem um e-mail são minimizadas transacionalmente ao tentar cadastrá-lo novamente; não há alteração em massa na inicialização.

## Verificação

`ModerationAndRecoveryIntegrationTest` usa PostgreSQL isolado, com envio de e-mail simulado. Usa Testcontainers por padrão. Opcionalmente, `INVESTIR_TEST_DATABASE_URL` aceita somente PostgreSQL em `127.0.0.1`, com banco cujo nome começa por `investir_test_`, para execução automatizada em uma instância descartável com usuário `postgres` e autenticação local de teste. Nunca usar esse mecanismo para testes manuais nem apontar para `investir_mais`.

Os testes de frontend cobrem denúncia de vídeo/professor, validação e erros, confirmação de moderação, exclusão imediata e registro de visualizações, segregação de perfis, categorias selecionáveis e ausência de aceite duplicado.
