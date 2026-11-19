# Plano priorizado de adequação LGPD

**Data:** 17/11/2026. **Base:** `1d1e0546b0717947960ad0059cb797163e927122`.  
Diagnóstico: [discovery.md](discovery.md). Situação consolidada: [STATUS.md](STATUS.md).

P1 significa prioridade para adequação do tratamento real; P2 significa completar evidência, governança ou defesa adicional. As prioridades não são declaração de infração, multa provável ou gravidade de vulnerabilidade explorada. Nenhum incidente ativo foi relatado ou demonstrado.

## Matriz de cobertura

| Dimensão | Resultado | Pendência |
|---|---|---|
| Inventário de dados e cookies | Parcial | LGPD-03, LGPD-08, LGPD-10 |
| Bases legais por atividade | Parcial; documentos existentes precisam de revisão | LGPD-01, LGPD-03, LGPD-07 |
| Controlador, regime e encarregado/canal | Validação organizacional necessária | LGPD-07 |
| Direitos do titular | Consulta/correção/encerramento parciais; atendimento completo não comprovado | LGPD-06 |
| Aceites dos documentos | Implementados no código | Manter; LGPD-02 trata da exatidão dos textos |
| Consentimento específico, se utilizado | Não demonstrado para supervisão/consentimento parental anunciado; não há marketing próprio identificado | LGPD-01; não exigir ledger de consentimento para toda atividade contratual |
| Retenção e bloqueio seletivo | Não implementados por registro | LGPD-04 |
| Desativação por 30 dias | Implementada como modalidade voluntária | Preservar distinção de eliminação; LGPD-04 |
| Eliminação em banco, logs, provedores e backups | Parcial | LGPD-05, LGPD-11 |
| Autenticação, autorização e CSRF | Controles presentes no código | Não foram submetidos a pentest nesta revisão |
| Criptografia e acessos em produção | Configuração parcial; evidências externas necessárias | LGPD-12 |
| Auditoria e responsabilização | Envers e restrição de admin presentes; minimização/retencão incompletas | LGPD-11 |
| Terceiros e transferência internacional | Não comprovados operacionalmente | LGPD-08 |
| Resposta a incidentes | Não comprovada no repositório | LGPD-09 |
| ROPA | Existe em PDF, mas apresenta divergências | LGPD-03 |
| RIPD/avaliação de risco | Não demonstrados; avaliar necessidade conforme escopo/risco, sem presumir obrigação universal | LGPD-01, LGPD-03 |
| ECA Digital | Avaliação e controles proporcionais não demonstrados | LGPD-01 |

## LGPD-01 — P1 — Adequar o produto ao acesso de menores

**Constatação:** o usuário confirmou acesso de menores. O texto empacotado afirma exigir supervisão/consentimento parental; não foram encontrados mecanismo equivalente, avaliação de melhor interesse ou atendimento por representante. A candidatura a professor declara não ter idade mínima, enquanto os PDFs afirmam maioridade.

**Engenharia:** após definir o público e as finalidades, implementar salvaguardas proporcionais, informação apropriada à idade e fluxo de representação/consentimento quando essa for a base escolhida para crianças. Rever publicação de vídeos e arquivos de menores, notificação, moderação e recurso conforme a incidência do ECA Digital. Não acrescentar documentos ou biometria indiscriminadamente.

**Jurídico/produto:** confirmar faixas etárias, papéis permitidos, base por atividade, melhor interesse e incidência das regras digitais. Não presumir que art. 14 exige consentimento parental em toda atividade de todos os menores, nem que qualquer quiz torna o projeto de alto risco.

**Aceitação:** política, ROPA, UX e backend compatíveis; cenários de crianças/adolescentes/responsáveis documentados e testados; sem coleta etária excessiva.

**Evidência:** `legal/v1.json:55`, `TeacherApplication.jsx:21`, `TeacherApplicationService.java`; [ANPD sobre menores](https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-divulga-enunciado-sobre-o-tratamento-de-dados-pessoais-de-criancas-e-adolescentes) e [ECA Digital](https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-publica-guia-que-esclarece-quem-deve-cumprir-e-quais-sao-as-responsabilidades-das-plataformas-na-protecao-de-criancas-e-adolescentes).

## LGPD-02 — P1 — Alinhar os textos públicos ao comportamento real

**Constatação:** nome/idade/nascimento, professor maior de 18 anos, ausência de tentativas incorretas, retenção, provedores e contatos divergem entre JSON, PDFs, README e código. A política do banco pode ter sido atualizada; isso não foi consultado.

**Engenharia:** inventariar o conteúdo atualmente servido pela API em cada ambiente; preparar uma nova versão corrigida, preservando versões e aceites antigos; aplicar o mecanismo existente de ciência da atualização. Não editar silenciosamente uma versão publicada.

**Jurídico/produto:** aprovar informações exatas sobre as finalidades, duração, responsáveis, compartilhamentos e direitos. Retirar garantias absolutas sem comprovação, como a declaração de plena conformidade.

**Aceitação:** texto efetivamente exibido ao usuário cobre dados e fluxos reais; registro da revisão e da publicação; versões anteriores permanecem verificáveis. Base de transparência: [art. 9º da LGPD](https://www2.camara.leg.br/legin/fed/lei/2018/lei-13709-14-agosto-2018-787077-normaatualizada-pl.html).

## LGPD-03 — P1 — Corrigir o ROPA e completar bases e inventário

**Constatação:** há ROPA PDF, mas ele exclui menores e inclui pagamentos, assinaturas e NF-e não encontrados no código. Arquivos de candidaturas, denúncias, tentativas individuais e recuperação precisam ser abrangidos. O README usa art. 11 para segurança sem distinguir dados comuns de sensíveis.

**Engenharia:** consolidar atividades, campos, destinatários, sistemas, permissões, retenção e campos legados; registrar inclusive snapshots e logs. Confirmar em banco existente, sem apagar dados, se ainda há colunas antigas de nascimento/idade.

**Jurídico/operações:** escolher e justificar base por atividade. A participação de instituição de ensino, pessoas físicas, operadores ou controladores independentes precisa ser definida. Validar o ROPA simplificado e o enquadramento, sem inventar operações ou obrigações fiscais.

**Aceitação:** inventário corresponde ao produto, inclui o público real, elimina afirmações sem evidência e possui responsável e revisão. Arts. 7º, 11 e 37 da LGPD, conforme enquadramento aplicável; a existência de um modelo preenchido não comprova sua exatidão.

## LGPD-04 — P1 — Decidir retenção antes da eliminação definitiva

**Constatação:** `AccountDeletionService.erase` remove conjuntos e evidências sem consultar uma regra por registro ou uma ordem de preservação. A rotina horária é habilitada por padrão para pedidos vencidos. A política PDF promete conservação segregada de aceites/auditoria, inexistente no fluxo examinado.

**Engenharia:** planejar o destino de cada conjunto — eliminar, anonimizar adequadamente ou conservar apenas o necessário com fundamento, marco inicial, vencimento, acesso restrito e bloqueio de outros usos. Tanto a eliminação imediata quanto a tarefa de vencimento precisam aplicar esse plano. Não resolver a lacuna conservando todo o cadastro por cinco anos.

**Jurídico/operações:** identificar quais obrigações efetivamente existem neste projeto. Os 30 dias são recuperação voluntária; prazos fiscais/contratuais não se tornam deveres gerais do cadastro. Avaliar antes do uso real da eliminação em ambientes com guarda específica. Suspender apenas o cron não protege o endpoint de eliminação imediata.

**Aceitação:** testes de eliminação total quando cabível, conservação mínima quando justificada, vencimento de retenções e impedimento de restaurar conta definitivamente eliminada. Referências: [ANPD sobre duração do tratamento](https://www.gov.br/anpd/pt-br/acesso-a-informacao/perguntas-frequentes) e hipóteses dos arts. 15–16 da LGPD.

## LGPD-05 — P1 — Concluir o ciclo de eliminação fora do banco ativo

**Constatação:** o fluxo local não comprova eliminação de vídeos, mensagens no provedor, logs nem backups. Não há reconciliação de exclusões após restaurar um snapshot.

**Engenharia:** guardar tarefas mínimas de propagação antes de remover os vínculos necessários; acompanhar confirmação/retentativa por operador; evitar remover arquivos compartilhados sem avaliação. Implementar supressão pós-restore ou controle equivalente proporcional para impedir reativação de dados já eliminados.

**Operações:** definir TTL dos backups, regiões, acessos e procedimentos dos fornecedores; distinguir exclusão de arquivo do tratamento efetivo dos dados pessoais nele contidos.

**Aceitação:** entrega comprovada nos sistemas aplicáveis e teste automatizado de restauração de backup com reconciliação antes de abrir o tráfego. Não prometer descarte externo instantâneo sem comprovação. A aplicabilidade da comunicação prevista no art. 18, §6º, deve ser avaliada por compartilhamento.

## LGPD-06 — P1 — Demonstrar atendimento completo aos titulares

**Constatação:** há acesso parcial e endereços de e-mail; não há exportação consolidada ou procedimento demonstrado para solicitação completa, portabilidade, bloqueio/oposição, representantes ou pessoas sem acesso à conta. O e-mail não é editável pela interface; isso requer outro atendimento efetivo.

**Engenharia/operações:** disponibilizar pacote seguro e estruturado quando aplicável, com metadados de tratamento; protocolar e acompanhar pedidos; verificar identidade proporcionalmente; proteger dados de terceiros; permitir atendimento após desativação e por representante legal verificado. Um processo manual bem demonstrado pode atender direitos; não é obrigatório um botão específico para cada um.

**Jurídico:** definir modalidade e SLA conforme regime efetivamente aplicável. Não chamar o aceite dos termos de consentimento revogável para todas as finalidades. Avaliar art. 20 se decisões automatizadas concretas tiverem os efeitos nele previstos; não presumir que todo resultado de exercício exige um processo novo de revisão humana.

**Aceitação:** ensaio completo e documentado de solicitação por titular autenticado, sem acesso e por representante, com entrega, decisão fundamentada e prazo. Referência: [direitos dos titulares na ANPD](https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares).

## LGPD-07 — P1 — Identificar responsáveis e validar regime/canal

**Constatação:** documentos usam a marca como controladora e afirmam ATPP; o usuário informa pessoas físicas sem empresa. O e-mail do perfil/JSON difere do canal institucional dos PDFs e do ROPA. Uma designação formal e a operação dessas caixas não foram verificadas.

**Ação organizacional:** identificar controlador(es) reais e contatos; verificar papel da instituição; avaliar eventual exceção acadêmica e elegibilidade ATPP; formalizar encarregado quando exigível ou canal quando houver dispensa aplicável. Harmonizar as informações sem presumir que uma caixa de e-mail, isoladamente, prova atendimento.

**Aceitação:** identificação e canal acessíveis publicamente, responsabilidades definidas e teste de encaminhamento/atendimento. Fontes: [guia acadêmico](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/web-guia-anpd-tratamento-de-dados-para-fins-academicos.pdf), [Resolução nº 2/2022](https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/resolucao-cd-anpd-no-2-de-27-de-janeiro-de-2022) e [guia do encarregado](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/copy_of_guia_da_atuacao_do_encarregado_anpd.pdf).

## LGPD-08 — P1 — Comprovar regras de fornecedores e transferências

**Constatação:** integrações com Resend, Cloudinary e YouTube e uso de PostgreSQL/Redis são visíveis. Prestadores reais, papéis, contratos, regiões e garantias de transferência não foram comprovados; não se conclui que inexistam fora do Git.

**Ação organizacional/técnica:** inventariar fornecedores e suboperadores, classificar cada papel, verificar contrato/instruções e regiões/acessos efetivos. Selecionar o mecanismo de transferência aplicável; não exigir cláusulas-padrão para toda transferência sem considerar adequação ou outras hipóteses. Explicar ao usuário a conexão de mídia com terceiros e os dados envolvidos.

**Aceitação:** evidência contratual e operacional por fornecedor, registro de finalidade/dados/país/prazo e transparência correspondente. Fonte: [ANPD — transferência internacional](https://www.gov.br/anpd/pt-br/assuntos/assuntos-internacionais/transferencia-internacional-de-dados).

## LGPD-09 — P1 — Comprovar preparação para incidentes

**Constatação:** não foi localizado procedimento de resposta e comunicação, responsáveis e registro operacional de incidentes; pode existir fora do repositório. Não há notícia de incidente ativo nesta avaliação.

**Ação:** definir responsáveis, contenção, preservação proporcional, avaliação de risco/dano relevante, decisão de comunicação e modelos; manter registro conforme a norma, realizar exercício de mesa e documentar a revisão.

**Aceitação:** simulação com horários, evidências e decisões. A regra geral de comunicação para incidentes notificáveis é de três dias úteis; eventual prazo diferenciado exige enquadramento confirmado. A guarda mínima de cinco anos é do registro de incidentes, não de todos os logs. Fonte: [Resolução nº 15/2024](https://bibliotecadigital.mj.gov.br/bitstream/1/12879/2/RES_ANPD_2024_15.html).

## LGPD-10 — P1 — Minimizar e classificar documentos de professores

**Constatação:** há upload de arquivos privados com tipo e tamanho limitados, mas seu conteúdo é aberto e os tipos são configuráveis. Isso não permite garantir que nunca haverá CPF, documentos de terceiros ou dados sensíveis. A política empacotada não descreve adequadamente esse tratamento.

**Engenharia/produto:** definir documentos estritamente necessários, instruções para ocultação de dados excessivos e descarte/retencão após análise; reduzir o acesso administrativo ao necessário. Avaliar necessidade de inspeção antimalware e proteção adicional conforme risco, sem tratá-las como exigências tecnológicas literais da LGPD.

**Aceitação:** tipos e campos mínimos justificados, comunicação antes do envio, armazenamento e acesso comprovados, finalidade/base compatíveis e calendário de descarte para candidaturas pendentes/rejeitadas/aprovadas.

## LGPD-11 — P2 — Minimizar logs e limitar a trilha de auditoria

**Constatação:** logs registram e-mail em ações pedagógicas; snapshots e autor de revisão contêm dados pessoais. Não há calendário comprovado de logs externos, política de leitura administrativa ou proteção de integridade de toda a trilha. Não foi demonstrada exposição pública desses registros.

**Engenharia/operações:** preferir identificadores internos onde o e-mail não é necessário; restringir e revisar consultas; documentar retenção e descarte dos logs e snapshots; definir controles de integridade e acesso proporcionais ao risco. Avaliar proteção adicional dos registros administrativos, inclusive leitura/download de documentos.

**Aceitação:** dados necessários, destinatários e prazo conhecidos; expurgo compatível com LGPD-04/05. Hash encadeado, WORM e criptografia de coluna são opções de engenharia, não obrigações universais de tecnologia.

## LGPD-12 — P2 — Comprovar segurança e registros na infraestrutura real

**Constatação:** código configura medidas de sessão e HTTPS para produção, mas não prova TLS entre serviços, criptografia em repouso, backups, IAM, perfil ativo, políticas de logs ou armazenamento de registros de acesso. O ROPA promete TLS 1.3 e seis meses de logs, sem comprovação técnica correspondente.

**Operações:** produzir evidência dos controles reais, permissões e restauração; revisar segredos e usuários do banco; identificar se e como o Marco Civil se aplica ao responsável e ao serviço. Distinguir registros de aplicação e conexão: o prazo de seis meses do art. 15 não deve ser imposto automaticamente a um projeto de pessoas físicas sem avaliar os pressupostos legais. Não criar coleta excessiva de navegação para cumprir uma obrigação não confirmada.

**Aceitação:** configuração revisada do ambiente, acesso mínimo e ensaio de recuperação, com lacunas explicitadas. Evidências devem ficar em local restrito; não versionar credenciais, backups ou dados pessoais de usuários.

## Sequência proposta para execução posterior

1. Definir responsáveis, enquadramento e público; aprovar finalidades/bases e salvaguardas de menores.
2. Corrigir o inventário e preparar textos coerentes com o produto; submeter a revisão antes de publicar versões novas.
3. Implementar decisão de retenção e ciclo de eliminação; demonstrar o atendimento a titulares e responsáveis.
4. Validar fornecedores, infraestrutura, backups e procedimento de incidentes.
5. Testar os fluxos novos em ambientes isolados e registrar evidências; reavaliar os itens após sua execução.

Esta entrega conclui o diagnóstico L1. O passo seguinte depende da revisão dos achados e do escopo de adequação escolhido; não foram produzidos ROPA/RIPD aprovados nem uma política pronta para publicação.
