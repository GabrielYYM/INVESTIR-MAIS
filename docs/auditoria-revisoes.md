# Identificadores das revisões de auditoria

O Hibernate Envers cria `revinfo` e `revinfo_seq` a partir do mapeamento de
`AuditRevision`. Em bancos novos, o gerador mantém os identificadores das revisões
sem ajustes na inicialização.

## Bancos existentes e restauração

A aplicação não realinha automaticamente sequências de bancos legados. Antes de
reutilizar um banco com histórico anterior, faça backup e confira se o gerador e
a sequência são compatíveis com as revisões existentes. Preserve os valores das
sequências ao restaurar o banco; não importe apenas as tabelas.

Uma colisão em `revinfo_pkey` é uma falha interna de auditoria, não uma duplicidade
dos dados informados no perfil. O tratamento HTTP mantém essa distinção. O cálculo
de um eventual ajuste deve considerar a alocação de blocos do gerador Hibernate.

## Regressão automatizada

`AuditRevisionIntegrationTest` cria um PostgreSQL isolado, atualiza o perfil e
atravessa mais de um bloco de 50 identificadores. Verifica as revisões persistidas.
`HttpValidationTest` cobre a resposta HTTP para colisões internas.

## Consulta geral

A tela `/admin/auditoria` e `GET /api/audit` permitem filtrar revisões de usuários,
cursos, aulas e questões por entidade, ID, revisão, datas, autor e papel do autor.
`AuditRevision` registra `actor` e `actor_role` junto a `rev` e `revtstmp`.
Consulte a [descrição dos filtros e snapshots](atualizacao-plataforma.md#auditoria-e-exclusão).
