# Retenção, recuperação e eliminação

Decisão do projeto em 17/11/2026: **30 dias corridos** para a modalidade voluntária de desativação recuperável. Esse prazo não é imposto pela LGPD.

## Regras do produto

1. O titular escolhe entre **desativar por 30 dias** e **solicitar eliminação definitiva agora**, após confirmação com senha. A janela não é imposta a um pedido de eliminação imediata.
2. Na desativação, a conta e seus conteúdos ficam inacessíveis pelo fluxo normal. Sessões e tokens são revogados. Preservam-se dados necessários à recuperação, incluindo matrículas, progresso e documentos enviados.
3. O administrador só restaura dentro da janela, registrando a referência da solicitação e a verificação da identidade do titular. Não são reabertas sessões antigas. Conteúdos excluídos antes da desativação continuam excluídos.
4. O término da janela impede restauração, mesmo que o processamento de eliminação esteja atrasado. A tarefa horária elimina até 100 contas por execução, em transações separadas. Falhas causam rollback e permitem retentativa.
5. A eliminação do banco ativo remove conta, documentos de candidatura, matrículas/progresso, tentativas, conteúdos de autoria do professor e suas dependências, denúncias ligadas ao titular/curso e snapshots relacionados. Remove a identificação do autor das revisões. Não manter dados antigos do perfil apenas porque estavam na auditoria.
6. Aceites permanecem imutáveis durante a existência da conta. A eliminação final usa uma exceção transacional restrita ao titular cuja conta foi fisicamente removida; não modifica versões legais publicadas nem aceites de outras pessoas.
7. Nenhuma conta excluída anteriormente é reconstituída a partir de snapshots antigos. Não existe recuperação de conta já anonimizada pelo comportamento anterior. A tarefa só processa solicitações novas e pendentes.

O tratamento necessário à recuperação solicitada pelo titular deve constar dos documentos e do registro de operações; não classificá-lo como obrigação legal de conservar a conta. Validar juridicamente a finalidade e a base aplicáveis antes de publicar uma nova versão da política. Os documentos legais vigentes não foram alterados nem publicados por esta mudança.

## Matriz por conjunto

| Conjunto | Prazo e marco inicial | Destino / fundamento |
|---|---|---|
| Dados de conta para recuperação voluntária | 30 dias a partir da solicitação registrada pelo servidor | Bloqueados; restauração a pedido do titular. Política operacional, não prazo da LGPD. |
| Dados sem outra finalidade após pedido de eliminação definitiva | Sem esperar a janela de recuperação | Eliminação no banco ativo. Arts. 15, 16 e 18 da LGPD; analisar eventuais exceções específicas, sem conservar o cadastro inteiro. |
| Preferências de marketing, foto/biografia, interesses e histórico de buscas | Não identificados no código examinado | Se forem adicionados com base exclusiva em consentimento e sem dever de guarda, sua eliminação não pode depender dos 30 dias da conta. |
| Registros de acesso à aplicação, se o provedor se enquadrar no art. 15 do Marco Civil | 6 meses a partir de cada acesso, ressalvadas determinações de guarda | Sigilosos e separados do cadastro. A aplicabilidade e a coleta no ambiente de hospedagem ainda precisam ser confirmadas. O código não cria um novo arquivo de IPs. |
| Documentos fiscais | Não há módulo fiscal neste projeto | Prazos decorrem do documento/crédito e de seus marcos legais. CTN arts. 173, 174 e 195, §1º; não aplicar “5 anos” a toda a base. |
| Evidência contratual necessária a processo | Avaliação individual do documento, necessidade, prazo e marco inicial | Art. 7º, VI, com minimização e acesso restrito; prescrição não constitui guarda obrigatória genérica de todos os dados por 5–10 anos. |
| RH / trabalhista | Não identificado no projeto | Não adotar faixa genérica de 2–30 anos sem o tipo documental e norma aplicável. |

O banco atual não contém um arquivo separado de guarda legal nem um mecanismo para registrar ordens judiciais. Antes de aplicar eliminação em ambiente sujeito a dever específico de conservação, segregar os registros mínimos e validar a operação com o responsável pelo tratamento. O fluxo não deve ser apresentado como uma auditoria completa de conformidade LGPD.

## Limites operacionais e eliminação nos operadores

- A remoção no banco não apaga automaticamente mensagens no Resend, vídeos no YouTube/Cloudinary, logs do provedor ou backups. Solicitações aos operadores devem ser registradas e acompanhadas, conforme o art. 18, §6º, quando aplicável. Não prometer eliminação imediata desses arquivos.
- Definir TTL e criptografia dos backups com o provedor. Antes de reabrir um backup restaurado, reconciliar as solicitações de eliminação posteriores ao snapshot. Esse controle externo ainda não está automatizado no repositório.
- Antes de qualquer alteração do esquema no banco oficial, fazer backup e revisar o DDL; as novas tabelas e os gatilhos só foram exercitados em testes isolados nesta implementação.
- Para suspender a tarefa de eliminação em um ambiente durante a preparação operacional, usar `app.privacy.account-erasure-cron=-`. O padrão é a cada hora (`0 0 * * * *`). Suspender a tarefa não amplia a janela de restauração.

## Fontes oficiais consultadas

- [ANPD: perguntas frequentes, especialmente 5.5](https://www.gov.br/anpd/pt-br/acesso-a-informacao/perguntas-frequentes): a LGPD não fixa um prazo geral de tratamento.
- [LGPD, texto atualizado na Câmara](https://www2.camara.leg.br/legin/fed/lei/2018/lei-13709-14-agosto-2018-787077-normaatualizada-pl.html): arts. 7º, 15, 16, 18 e 19. Os 15 dias do art. 19 tratam de declaração completa de acesso, não de lixeira.
- [Marco Civil da Internet, Ministério da Justiça](https://www.gov.br/mj/pt-br/assuntos/sua-protecao/sedigi/LEIN12.965DE23DEABRILDE2014.pdf): arts. 5º, VIII, e 15, incluindo o enquadramento do provedor.
- [CTN, Senado](https://legis.senado.gov.br/norma/547034/publicacao/34620375): marcos de decadência/prescrição e conservação dos documentos fiscais.
