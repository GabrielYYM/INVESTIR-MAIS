# Exclusão de conta, retenção e auditoria

Atualização técnica de 22/11/2026, solicitada pelo responsável: remover a recuperação de 30 dias, suprimir identificadores no soft delete, preservar os registros de ações e permitir o recadastro do e-mail. Não é uma política de privacidade publicada nem uma conclusão de conformidade integral.

## Regras implementadas

1. Senha atual e titularidade verificadas; sessões e tokens revogados antes de alterar o e-mail. As alterações do banco ocorrem em uma transação.
2. Nome, e-mail, senha e verificação removidos do cadastro imediatamente. O substituto aleatório usa `deleted.invalid`, sem hash do endereço original. Nas revisões do perfil, nome/e-mail são suprimidos sem remover linhas, operações, revisões ou datas. O autor identificado pelo e-mail atual é substituído por `deleted:<UUID>`.
3. A linha com `deleted_at` e os vínculos são preservados: histórico de ações, aceites, progresso, matrículas, tentativas, denúncias, conteúdos e restaurações não são apagados. Conteúdo da conta fica oculto e não pode ser restaurado enquanto a conta estiver excluída. Documentos privados da candidatura e filas de e-mail com credenciais são eliminados; metadados da candidatura permanecem, com supressão do parecer livre.
4. O e-mail fica livre para outro cadastro, com novo UUID, STUDENT e confirmação pendente. Nenhum histórico ou privilégio migra. Exclusões antigas são minimizadas sob bloqueio quando seu e-mail é solicitado novamente.
5. Consultas e detalhes da auditoria, histórico individual e consultas ao log de visualizações registram o administrador autenticado, horário UTC do servidor e referências retornadas. O log guarda UUID/papel, sem duplicar nome/e-mail, nem coletar IP, senha, token, conteúdo do snapshot ou filtros livres. Somente ADMIN com autenticação completa acessa essas rotas.
6. A tabela de visualizações não tem API de modificação e bloqueia UPDATE/DELETE/TRUNCATE no PostgreSQL. Não há alegação de inviolabilidade contra proprietário/superusuário, nem cadeia de hashes/ancoragem externa implementada.
7. Só existe soft delete de conta: a linha é mantida com `deleted_at`. A rota de eliminação física foi removida. Não há recuperação, endpoint de restauração de conta ou job que apague histórico após 30 dias.

## Limite da anonimização

Remover identificadores diretos do perfil não torna todo o acervo anônimo. UUIDs, horários, conteúdo autoral e textos livres podem permitir associação ao titular. O histórico continua sujeito à LGPD se houver possibilidade razoável de reidentificação (arts. 5º, XI, e 12). `SOFT_DELETED` indica a exclusão lógica, sem certificar anonimização jurídica do conjunto.

## Matriz e decisões pendentes

| Conjunto | Tratamento técnico | Fundamento e limite |
|---|---|---|
| Identificadores do cadastro e credenciais | Supressão imediata após pedido autenticado | Necessidade, segurança e término do tratamento: arts. 6º, III, 15, 16 e 18. |
| Documentos de candidatura | Eliminação dos binários no banco ativo | Sem finalidade de recuperação. Não presumir obrigação genérica de conservar documentos. |
| Ações, aceites e evidências administrativas | Preservadas, com acesso administrativo restrito e rastreado | Arts. 6º, X, e 46 orientam o controle; **não autorizam guarda irrestrita e eterna**. Validar necessidade, hipótese do art. 16 ou necessidade probatória concreta, base aplicável (ex.: art. 7º, VI, quando cabível), prazo e marco por categoria. |
| Visualizações de auditoria | Preservadas, sem duplicar nome/e-mail e sem IP | Controle técnico recomendado. A LGPD não estabelece prazo universal para esse log. Definir retenção proporcional e descarte autorizado. |
| Acessos sujeitos ao Marco Civil | Não substituídos pelo log de visualizações | Lei 12.965/2014, art. 15: seis meses, se o provedor se enquadrar; validar enquadramento e coleta no provedor. |
| Incidentes de segurança | Sem implementação nova nesta tarefa | Res. CD/ANPD 15/2024, art. 10: mínimo de cinco anos; não aplicar esse prazo a toda auditoria. |

A preservação solicitada impede perda do histórico existente. Não foi configurado expurgo nem inventado prazo legal universal. Aprovar finalidades/bases/prazos por categoria, segregar evidências sob guarda e avaliar acesso mais granular continuam pendentes do responsável pelo tratamento. Dados sem necessidade/base de conservação não devem permanecer identificáveis apenas por estarem no histórico.

## Operadores e backups

A alteração no banco não apaga automaticamente mensagens do Resend, vídeos externos, logs de infraestrutura ou backups. Avaliar comunicação aos agentes com compartilhamento conforme art. 18, §6º. Definir TTL e criptografia dos backups e reconciliar exclusões antes de reabrir tráfego após restore. Esses procedimentos externos não foram automatizados.

O banco oficial exige backup antes de DDL. Testes automatizados usam bancos isolados e Resend simulado; os manuais usam `investir_mais` e leitura do e-mail real no navegador. Documentos legais publicados são imutáveis e não foram substituídos; eventual atualização deve ser revisada e versionada no fluxo legal.

## Fontes oficiais

- [LGPD, texto compilado](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm): arts. 5º, 6º, 7º, 12, 15, 16, 18 e 46.
- [ANPD: perguntas frequentes](https://www.gov.br/anpd/pt-br/acesso-a-informacao/perguntas-frequentes): não há prazo geral e único de tratamento.
- [Marco Civil](https://www.planalto.gov.br/ccivil_03/_ato2011-2014/2014/lei/l12965.htm): art. 15.
