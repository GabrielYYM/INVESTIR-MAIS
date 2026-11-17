# Diagnóstico técnico de privacidade — 17/11/2026

## Conclusão e método

O projeto **não demonstra implementação completa das obrigações de proteção de dados aplicáveis ao seu funcionamento**. Existem controles úteis, mas há documentos que descrevem comportamentos diferentes do código e faltam controles e evidências de operação. A avaliação considera a declaração do usuário de que o serviço é um projeto acadêmico de pessoas físicas, sem empresa, e permite menores de 18 anos.

Base examinada: `1d1e0546b0717947960ad0059cb797163e927122`. Foram revisados código backend/frontend, modelos de dados, configurações e tarefas, textos legais empacotados, documentos Markdown e PDFs do repositório. O ROPA foi lido por extração de texto e conferência visual. Trata-se de revisão estática de privacidade, não de teste de invasão, certificação LGPD ou parecer jurídico definitivo. Não houve acesso ao banco oficial ou à produção, nem execução de fluxos que enviem e-mails ou apaguem dados.

As classificações distinguem **fato no código**, **documento divergente** e **ausência de evidência operacional**. Não localizar um contrato ou procedimento no Git não demonstra que ele não exista fora do repositório.

## Enquadramento que precisa ser resolvido

O nome de uma plataforma não identifica necessariamente a pessoa ou as pessoas que decidem as finalidades e os meios do tratamento. A identificação pública deve corresponder aos responsáveis reais. Não é necessário inventar CNPJ para pessoas físicas, mas é necessário esclarecer os papéis, inclusive eventual participação da instituição de ensino.

A exceção do art. 4º, II, b, refere-se ao tratamento **exclusivamente acadêmico**, permanecendo aplicáveis as bases dos arts. 7º e 11. Não se presume que cadastro, operação de uma plataforma pública e todos os compartilhamentos sejam abrangidos apenas porque o software foi criado como trabalho acadêmico. Essa conclusão depende da finalidade concreta de cada atividade, conforme o [guia da ANPD para fins acadêmicos](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/web-guia-anpd-tratamento-de-dados-para-fins-academicos.pdf).

A [Resolução nº 2/2022](https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/resolucao-cd-anpd-no-2-de-27-de-janeiro-de-2022) pode alcançar pessoas naturais. Os documentos afirmam enquadramento ATPP, mas não apresentam a avaliação que o sustenta. Menores são critério específico de risco; não bastam, isoladamente, para concluir alto risco ou excluir benefícios: o teste também exige critério geral. Não se confirmou esse enquadramento nesta revisão.

## Superfícies e dados observados

| Atividade | Dados e ativos | Evidência principal |
|---|---|---|
| Cadastro e perfil | Apelido, e-mail, hash de senha, papel, confirmação do e-mail e datas | [UserDTO](../src/main/java/com/repositorio/investir_mais/domain/user/DTO/UserDTO.java), [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java) |
| Login, confirmação e recuperação | Sessão, cookies, códigos temporários, e-mail; publicações duráveis de envio | [SecurityConfig](../src/main/java/com/repositorio/investir_mais/infrastructure/SecurityConfig.java), [AccountSecurityService](../src/main/java/com/repositorio/investir_mais/domain/token/service/AccountSecurityService.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java) |
| Aprendizagem | Matrículas, progresso, alternativa escolhida, acerto/erro, instante e estatísticas por aluno | [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java), [LearningDashboardRepository](../src/main/java/com/repositorio/investir_mais/domain/education/repository/LearningDashboardRepository.java) |
| Candidatura de professor | Arquivos PDF/PNG/JPEG, identificação da conta e parecer | [TeacherApplicationService](../src/main/java/com/repositorio/investir_mais/domain/user/service/TeacherApplicationService.java) |
| Conteúdo e mídia | Vídeos, miniaturas, textos e vínculo com professor/curso; podem conter imagem ou voz | [VideoUploadController](../src/main/java/com/repositorio/investir_mais/domain/education/controller/VideoUploadController.java), [Course](../frontend/investirMais/src/pages/Course.jsx) |
| Denúncias | Denunciante, alvo, motivo, descrição, URL, decisão e responsável pela análise | [ContentReport](../src/main/java/com/repositorio/investir_mais/domain/education/model/ContentReport.java), [ContentReportService](../src/main/java/com/repositorio/investir_mais/domain/education/service/ContentReportService.java) |
| Aceites e auditoria | Versão, instante, snapshots e autor de alterações identificado por e-mail | [LegalDocumentsService](../src/main/java/com/repositorio/investir_mais/domain/legal/service/LegalDocumentsService.java), [AuditRevision](../src/main/java/com/repositorio/investir_mais/domain/AuditRevision.java) |
| Desativação e restauração | Pedido, prazo, itens associados, responsável e justificativa | [AccountDeletionService](../src/main/java/com/repositorio/investir_mais/domain/user/service/AccountDeletionService.java), [ContentTrashService](../src/main/java/com/repositorio/investir_mais/domain/education/service/ContentTrashService.java) |
| Proteção de autenticação | IP e rota em chaves Redis com prazo curto | [SecurityFilter](../src/main/java/com/repositorio/investir_mais/infrastructure/SecurityFilter.java) |

Não foram identificados módulos próprios de marketing, publicidade comportamental, pagamentos, NF-e ou RH. Não foi identificada uma base de registros de acesso com guarda de seis meses. O IP usado para rate limiting não constitui, por si só, esse arquivo de acesso.

## Controles efetivamente presentes no código

1. Senhas com Argon2id; cadastro público restrito a aluno; verificação do e-mail; login com senha e OTT vinculado à sessão.
2. Proteção CSRF, CORS com origem específica, restrições por papel e verificações de propriedade de recursos. Não foi feito pentest para garantir ausência de todas as falhas de autorização.
3. Sessão com HttpOnly e SameSite; Secure forçado no perfil `prod`; validação de origem HTTPS para produção; timeout de sessão e tokens temporários. A configuração efetivamente ativa na hospedagem não foi observada.
4. Autenticação limitada por Redis; mensagens públicas de conta em formatos genéricos em vários fluxos; sem retorno do token interno de cadastro ao frontend.
5. Versões legais com hash, imutabilidade no banco, aceites associados a versão/data, controle de versão corrente e histórico. O aceite contratual/ciência da política não foi usado como autorização genérica de marketing.
6. Download de documentos de candidatura restrito ao titular ou administrador, com `no-store` e `nosniff`.
7. Retirada de acesso mediante senha, revogação de sessões/tokens, desativação recuperável por 30 dias e opção separada de eliminação definitiva. A sessão da própria requisição é invalidada.
8. Denúncias e restaurações administrativas com restrição de papel, parecer/justificativa e auditoria. Esses controles não substituem procedimentos de privacidade, incidentes ou proteção etária.

As evidências são os serviços e configurações acima. A existência desses controles atende a partes da segurança e da prestação de contas; não demonstra todas as obrigações da [LGPD](https://www2.camara.leg.br/legin/fed/lei/2018/lei-13709-14-agosto-2018-787077-normaatualizada-pl.html).

## Divergências confirmadas

### Documentos servidos e PDFs não são a mesma fonte

- [TermsContent.jsx](../frontend/investirMais/src/components/account/TermsContent.jsx) consulta a API de documentos legais.
- [LegalDocumentsService.java](../src/main/java/com/repositorio/investir_mais/domain/legal/service/LegalDocumentsService.java) carrega `legal/v*.json` e retorna a maior versão persistida. Só há `v1.json` empacotado no repositório.
- PDFs em `docs/lgpd/` se apresentam como versão 2.0, de 15/11/2026. Sua presença no Git não os publica automaticamente na API. A produção pode ter uma versão posterior cadastrada pelo admin; isso não foi verificado.

### Coleta e menores

- [v1.json:51](../src/main/resources/legal/v1.json#L51) descreve nome completo e idade declarada. O DTO atual descreve apelido e não possui campo de idade/data de nascimento.
- [v1.json:55](../src/main/resources/legal/v1.json#L55) afirma exigir supervisão ou consentimento parental. Não foram localizados cadastro/verificação de responsável, evidência parental, classificação etária ou avaliação de melhor interesse correspondente.
- A política PDF, páginas 2–3, descreve coleta de nascimento e restrição de professor/admin a maiores de 18. [TeacherApplication.jsx:21](../frontend/investirMais/src/pages/TeacherApplication.jsx#L21) afirma que não há idade mínima; o serviço verifica papel, e-mail e arquivos, sem verificação de capacidade etária.
- O ROPA PDF, página 1, deixa a categoria crianças/adolescentes desmarcada e afirma não coletar seus dados. Isso diverge do público confirmado pelo responsável nesta auditoria.

O problema não é a falta isolada de um campo de nascimento. É não ter uma política e salvaguardas verificáveis para o público admitido. O [enunciado da ANPD sobre menores](https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-divulga-enunciado-sobre-o-tratamento-de-dados-pessoais-de-criancas-e-adolescentes) permite bases legais adequadas dos arts. 7º/11 com prevalência do melhor interesse; consentimento parental não é requisito universal para todo tratamento de todos os menores.

### Aprendizagem e minimização

- A política PDF, página 2, e os termos PDF, página 8, dizem que não há histórico de alternativas incorretas.
- [QuestionService.java:94](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java#L94) grava a alternativa, o resultado e o instante de cada tentativa, inclusive incorreta, em `question_attempt`.
- [LearningDashboardRepository.java:55](../src/main/java/com/repositorio/investir_mais/domain/education/repository/LearningDashboardRepository.java#L55) produz métricas individualizadas com ID, nome, tentativas e acurácia para o professor autorizado. A afirmação do README de que professores recebem apenas métricas agregadas precisa ser corrigida.

Esses tratamentos podem ter finalidade pedagógica legítima, mas a transparência deve descrevê-los corretamente e a necessidade/retencão deve ser avaliada, em especial para menores.

### ROPA e enquadramento

Há um ROPA no repositório; não seria correto afirmar que ele inexiste. Entretanto, ele lista gateway de pagamentos, assinaturas e assessoria fiscal/NF-e sem implementação correspondente identificada. Também omite menores e não abrange adequadamente candidaturas com arquivos, denúncias, tentativas detalhadas e recuperação por 30 dias. Ele precisa representar as operações reais e os agentes reais.

O [README de LGPD](../docs/lgpd/README.md) usa art. 11, II, g, para segurança de dados comuns, afirma ATPP e apresenta prazos como obrigações gerais. É necessário validar bases por atividade, sem aplicar o art. 11 indiscriminadamente a dados não sensíveis ou os prazos fiscais a todo cadastro.

### Retenção e eliminação

- [AccountDeletionService.java:57](../src/main/java/com/repositorio/investir_mais/domain/user/service/AccountDeletionService.java#L57) executa eliminação física sem consultar uma decisão de retenção por registro, ordem de preservação ou guarda legal específica.
- O fluxo remove denúncias/snapshots relacionados e aceites do titular; não existe um arquivo segregado de evidências mínimas com prazo e fundamento próprio.
- [AccountErasureJob.java:15](../src/main/java/com/repositorio/investir_mais/infrastructure/AccountErasureJob.java#L15) é habilitado por padrão a cada hora para pedidos vencidos. `app.privacy.account-erasure-cron=-` suspende somente o job, não a opção de eliminação imediata.
- A política PDF, página 6, e o README apresentam conservação de aceites/auditoria até cinco anos. O fluxo atual não implementa essa separação. Isso não torna cinco anos um prazo legal obrigatório universal; exige revisar a justificativa e o comportamento prometido.
- Não foram localizados reconciliação de exclusões após restauração de backup, propagação de eliminação para provedores, confirmação de descarte de mídia ou expurgo específico dos logs de aplicação.

A conservação e o descarte precisam ser decididos por conjunto e finalidade, conforme as hipóteses aplicáveis dos arts. 15–16, e a comunicação a agentes com quem houve compartilhamento deve ser avaliada nos termos do art. 18, §6º. A [ANPD esclarece que não existe um prazo geral de tratamento](https://www.gov.br/anpd/pt-br/acesso-a-informacao/perguntas-frequentes). Os 30 dias adotados são uma escolha de recuperação voluntária.

## Direitos do titular

Há consulta a perfil, respostas/progresso e aceites, atualização de perfil no backend e opções de encerramento. A interface deixa o e-mail somente leitura, embora a API tenha atualização com senha e nova verificação. Isso pode ser atendido por outro canal, mas o procedimento não foi comprovado.

Não foram localizados exportação consolidada de dados do titular, protocolo de solicitação com prazo e decisão, tratamento de representante legal ou atendimento verificável de acesso completo, portabilidade, oposição e bloqueio seletivo. Existem endereços de e-mail publicados; não foi acessada a caixa nem testado seu atendimento. O e-mail, se operacionalizado corretamente, pode atender direitos; um botão de download específico não é exigência literal da lei.

Os direitos devem ser tratados segundo a modalidade e o regime aplicável. Acesso simplificado e declaração completa têm regras diferentes, e benefícios de ATPP não devem ser presumidos. Referências: [direitos dos titulares na ANPD](https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares) e [Resolução nº 2/2022](https://www.gov.br/anpd/pt-br/acesso-a-informacao/institucional/atos-normativos/regulamentacoes_anpd/resolucao-cd-anpd-no-2-de-27-de-janeiro-de-2022).

## Menores e moderação

O cadastro aberto e o público declarado exigem uma avaliação específica. Não há evidência no repositório de avaliação de melhor interesse, configuração por faixa etária ou procedimento de direitos por responsáveis. Também é necessário avaliar a incidência do ECA Digital conforme as funcionalidades e os riscos, sem equiparar automaticamente uma plataforma educacional a uma rede social.

O botão de denúncia exige sessão e é protegido pelo fluxo de aceite; há também e-mail nos documentos. Não foi comprovado um procedimento público acessível a responsáveis sem conta, triagem urgente de violações contra menores, preservação específica de evidências ou contestação/notificação do autor após a remoção. Os controles aplicáveis precisam ser definidos à luz da [orientação atual da ANPD sobre ECA Digital](https://www.gov.br/anpd/pt-br/assuntos/noticias/anpd-publica-guia-que-esclarece-quem-deve-cumprir-e-quais-sao-as-responsabilidades-das-plataformas-na-protecao-de-criancas-e-adolescentes). Não se recomenda coletar RG, selfie ou nascimento exato de todos como solução automática.

## Terceiros, cookies e infraestrutura

- Resend recebe destinatário e mensagem; Cloudinary recebe vídeos enviados; a reprodução de YouTube conecta o navegador ao provedor. PostgreSQL/Redis são tecnologias; é necessário identificar o fornecedor real da hospedagem e seus papéis.
- Não foram encontrados SDKs próprios de publicidade/analytics nem cookies opcionais no código examinado. `SESSION` e `XSRF-TOKEN` têm função técnica; isso, sozinho, não fundamenta exigir um banner de consentimento. O player e as mídias externas ainda demandam análise do fluxo real de dados e comunicação clara.
- Não foram encontrados contratos/DPA ou registros de regiões, suboperadores e mecanismos de transferência. Não é correto concluir que inexistem contratos somente porque não estão no Git. Tampouco é correto classificar todos os serviços externos como operadores sem verificar sua atuação.
- O perfil `prod` usa cookies Secure e Redis com TLS por padrão; os endpoints de provedores usam HTTPS. Não foram verificadas criptografia de volumes/backups, permissões, exposição de rede, chaves, política de acesso aos logs ou aplicação efetiva do perfil em produção. A lei não determina uma tecnologia específica de criptografia de coluna para todos os dados.

As [regras oficiais de transferência internacional](https://www.gov.br/anpd/pt-br/assuntos/assuntos-internacionais/transferencia-internacional-de-dados) exigem avaliar a operação e o mecanismo aplicável. Não é preciso aplicar cláusulas-padrão indistintamente: existem outras hipóteses, incluindo adequação, nas condições da decisão pertinente.

## Logs, auditoria e incidentes

O Envers registra alterações e os endpoints de auditoria exigem admin. Não foram encontrados hash encadeado, ancoragem externa ou proteção contra escrita arbitrária em todas as tabelas de auditoria; esses mecanismos são opções de engenharia, não obrigações universais literais da LGPD.

Há e-mails em logs de matrícula/progresso e no autor de revisão. Eliminar o cadastro no PostgreSQL não elimina automaticamente cópias desses logs no provedor. Não foi demonstrada consulta indevida ou exposição pública deles; a pendência é definir minimização, acesso e retenção.

Não foram localizados plano de resposta, responsáveis acionáveis, triagem de risco/dano relevante, modelos de comunicação e registro de incidentes com guarda adequada. O processo pode existir fora do repositório, mas não foi comprovado. A [Resolução nº 15/2024](https://bibliotecadigital.mj.gov.br/bitstream/1/12879/2/RES_ANPD_2024_15.html) trata especificamente de incidentes, inclusive registro por pelo menos cinco anos; esse prazo não deve ser aplicado como obrigação a todos os logs ou solicitações de titulares.

## Documentos examinados

- `docs/lgpd/Planilha sem título - Modelo de ROPA - ATPP.pdf`, página 1, leitura textual e visual.
- `docs/lgpd/Politica_de_privacidade_InvestirMais.pdf`, nove páginas, leitura textual integral; seções relevantes de coleta, menores, direitos e retenção comparadas com o código.
- `docs/lgpd/Termos_e_Condicoes_InvestirMais.pdf`, nove páginas, leitura textual integral; capacidade etária, moderação, aprendizagem e proteção de dados comparadas com o código.

Os PDFs foram preservados. Seu conteúdo jurídico não foi corrigido ou publicado automaticamente. O plano de adequação está em [gaps.md](gaps.md).
