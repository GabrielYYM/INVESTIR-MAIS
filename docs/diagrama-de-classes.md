# Diagramas do Investir Mais

Abra o [arquivo editável do Draw.io](diagramas/investir-mais.drawio) e selecione uma das cinco abas. O [README principal](../README.md#diagramas) inclui prévias dos cinco diagramas.

| Diagrama | O que mostra |
| --- | --- |
| [01 · Classes de domínio](diagramas/classes-de-dominio.png) | Entidades JPA e tabelas SQL de documentos e aceites. |
| [02 · Classes da aplicação](diagramas/classes-da-aplicacao.png) | Controllers, serviços, persistência, normalização de links do YouTube, documentos legais e e-mail. |
| [03 · Fluxo de navegação](diagramas/fluxo-de-navegacao.png) | Cadastro, login, aceite legal, permissões, ordenação e promoção a professor. |
| [04 · BPMN de acesso](diagramas/bpmn-login-ott.png) | Login com senha e OTT, seguido da consulta de aceite legal. |
| [05 · Diagrama de arquitetura](diagramas/arquitetura.png) | Frontend, backend, persistência, player do YouTube, e-mails e implantação. |

O diagrama de acesso usa BPMN 2.0 para documentar o processo. [Especificação BPMN 2.0.2 da OMG](https://www.omg.org/spec/BPMN/2.0.2/)

## Classes de domínio

Cada curso referencia um usuário proprietário e reúne aulas e perguntas. Cada aula e cada pergunta pertencem a um curso. A exclusão lógica se propaga de usuário para cursos e de curso para aulas e perguntas. A senha é armazenada como hash Argon2id e fica fora da auditoria do Hibernate Envers. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [Course](../src/main/java/com/repositorio/investir_mais/domain/education/model/Course.java), [Lesson](../src/main/java/com/repositorio/investir_mais/domain/education/model/Lesson.java), [Question](../src/main/java/com/repositorio/investir_mais/domain/education/model/Question.java)

Matrículas e progresso pertencem a `User` e são persistidos como coleções de valores embutíveis. A matrícula é indexada por curso e a conclusão por aula. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java)

As aulas possuem `position`, a partir de 1. O professor proprietário e o administrador podem reordenar a lista completa em uma transação; as perguntas vinculadas às posições acompanham as aulas. As perguntas são ordenadas por `afterLesson`, `position` e `id`; `afterLesson = 0` indica uma pergunta anterior à primeira aula. As alternativas ficam em `Question.options`, persistidas como ARRAY, e `correctOptionIndex` identifica a alternativa correta. Não existem entidades separadas de alternativa ou tentativa. [Question](../src/main/java/com/repositorio/investir_mais/domain/education/model/Question.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

`User.name` representa o apelido. Idade e data de nascimento foram removidas do modelo, dos formulários e do contrato da API. A promoção a professor depende da aprovação da candidatura com documentos. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

As tabelas `legal_document_release` e `legal_document_acceptance` são acessadas por JDBC, sem entidades JPA. A primeira preserva versão, data, conteúdo JSON e hash; a segunda usa a chave composta `(user_id, version)` e registra o horário do aceite. `version` referencia a publicação; `user_id` é um UUID estável, sem chave estrangeira para a conta. Triggers bloqueiam UPDATE, DELETE e TRUNCATE nas duas tabelas. [Schema SQL](../src/main/resources/schema-postgresql.sql), [Proteção do histórico](../src/main/resources/legal-history-postgresql.sql), [Guia dos documentos legais](documentos-legais.md)

Os acertos ficam em `User.passedQuestionIds`, uma coleção de UUIDs na tabela `tb_question_pass`, com unicidade por aluno e pergunta. Essa coleção não é uma associação JPA com `Question`. Uma alteração na pergunta limpa os acertos anteriores. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java)

## Classes da aplicação

O `UserController` atende o painel do professor com apoio de `CourseService`. O `AuthenticationEmailHandler` recebe os eventos de conta, monta as mensagens e as envia por meio do `ResendEmailSender`. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java)

`CourseController`, `LessonController` e `QuestionController` delegam as operações educacionais aos respectivos serviços. `QuestionService` controla a aprovação e os bloqueios de conteúdo, com apoio de `CourseService`, `UserService` e dos repositórios de aulas, perguntas e usuários. `LessonService` consulta essas regras para exibir ou concluir aulas. [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

Os mapeadores `UserMapper`, `CourseMapper`, `LessonMapper` e `QuestionMapper` usam MapStruct. O `QuestionMapper` valida o índice da alternativa correta e a duplicidade das opções antes de criar ou atualizar a entidade. `AccountSecurityService` usa repositórios JPA e `TokenMapper` para tokens de confirmação e recuperação e revoga sessões indexadas no Redis. [QuestionMapper](../src/main/java/com/repositorio/investir_mais/domain/education/mapper/QuestionMapper.java), [AccountSecurityService](../src/main/java/com/repositorio/investir_mais/domain/token/service/AccountSecurityService.java)

`LessonService` usa `YouTubeVideoUrl` para validar o formato do endereço, normalizar o link do YouTube e gerar a URL da miniatura. A publicação aceita vídeos de qualquer canal, sem filtro temático, aprovação administrativa de canais ou consulta a metadados externos. [Normalização do endereço](../src/main/java/com/repositorio/investir_mais/domain/education/service/YouTubeVideoUrl.java), [Guia dos vídeos](video-validation.md)

`LegalDocumentsController` expõe leitura, status, aceite e histórico por meio de `LegalDocumentsService`, que usa repositórios JPA, `LegalDocumentDTO` e `LegalDocumentMapper`. `LegalDocumentsInitialization` registra os arquivos `legal/vN.json` na inicialização; o serviço lê a maior versão persistida no banco. `LegalAcceptanceConfiguration` bloqueia os recursos sujeitos ao aceite enquanto há pendência. [LegalDocumentsService](../src/main/java/com/repositorio/investir_mais/domain/legal/service/LegalDocumentsService.java), [Interceptor de aceite](../src/main/java/com/repositorio/investir_mais/infrastructure/LegalAcceptanceConfiguration.java)

## Navegação e permissões

O React Router consulta `/auth/me` antes de liberar a área protegida. Em caso de sessão ausente ou incompleta, a interface redireciona para o login com a rota de retorno. Após o OTT, abre o destino interno permitido ou o painel padrão. O cadastro público cria contas de aluno e exige confirmação de e-mail. [Rotas React](../frontend/investirMais/src/routes.jsx), [Tela de login](../frontend/investirMais/src/pages/Login.jsx), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

Quando a pessoa confirma o link original no navegador do cadastro, a aplicação inicia a primeira sessão; o acesso ao painel passa pela verificação de aceite legal. Em outro navegador ou com um link reenviado, a confirmação libera o login. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java)

Antes de liberar o catálogo ou o painel, a interface consulta `/api/legal/status`. Havendo pendência, abre `/aceitar-documentos`; a confirmação registra a versão, o usuário autenticado e o horário do servidor. Uma versão nova exige novo aceite. A API retorna HTTP 428 enquanto há pendência e HTTP 409 se a versão mudar durante a leitura. Perfil, segurança, exclusão da conta, documentos e saída permanecem disponíveis. [Rotas React](../frontend/investirMais/src/routes.jsx), [API de documentos](../src/main/java/com/repositorio/investir_mais/domain/legal/controller/LegalDocumentsController.java)

| Perfil | O que pode fazer no catálogo |
| --- | --- |
| Aluno (`STUDENT`) | Consultar cursos, matricular-se, concluir aulas e responder perguntas liberadas. |
| Professor (`TEACHER`) | Gerenciar os próprios cursos, ordenar suas aulas, criar perguntas e publicar links de qualquer canal do YouTube. |
| Admin (`ADMIN`) | Gerenciar todo o catálogo, ordenar as aulas de qualquer curso, alterar os papéis permitidos e consultar auditoria. |

Os três perfis usam o mesmo painel. A interface exibe as ações de acordo com o perfil e a propriedade, e o backend aplica as mesmas verificações nas operações de escrita. [Ações do catálogo](../frontend/investirMais/src/services/educationData.js), [CourseService](../src/main/java/com/repositorio/investir_mais/domain/education/service/CourseService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

A página de curso reúne as aulas e as perguntas. Em cursos com perguntas, o aluno deve concluir o conteúdo anterior e acertar as perguntas anteriores para avançar; aulas bloqueadas continuam visíveis no currículo, mas seus detalhes e mídias são ocultados. A listagem do aluno omite o gabarito, e a listagem de gestão exige professor proprietário ou administrador. O percentual do curso considera aulas concluídas e perguntas aprovadas. [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java), [CourseService](../src/main/java/com/repositorio/investir_mais/domain/education/service/CourseService.java)

O perfil oferece alteração de apelido com senha atual, troca de senha, recuperação, exclusão da conta e histórico dos aceites legais com links para o texto exato de cada versão. O painel `/admin` permite consultar usuários, alterar os papéis permitidos e consultar auditoria. O backend impede conceder ou alterar o papel `ADMIN` pelo painel e exige candidatura com documentos aprovada para promover um aluno a professor. Termos e privacidade possuem rotas públicas. [Perfil](../frontend/investirMais/src/pages/Perfil.jsx), [Admin](../frontend/investirMais/src/pages/Admin.jsx), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

## Processo de acesso com OTT

O BPMN usa três raias para mostrar as responsabilidades da pessoa usuária, do React e da API Spring. A API valida a senha e o e-mail verificado, registra o desafio na sessão parcial e dispara o envio de um link e um código por e-mail. A pessoa abre o link no navegador em que começou o login ou informa o código na tela. As duas opções chamam `POST /auth/verify-2fa` com proteção CSRF. O Spring consome o token válido, conclui a autenticação e troca o identificador da sessão. [SecurityConfig](../src/main/java/com/repositorio/investir_mais/infrastructure/SecurityConfig.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java), [Login.jsx](../frontend/investirMais/src/pages/Login.jsx)

O recorte termina quando a autenticação é concluída e a interface consulta o aceite legal, ou quando as credenciais ou o OTT são recusados. A etapa de abertura pressupõe que a mensagem chegou. O [fluxo de navegação](../README.md#fluxo-de-navegação) detalha o primeiro acesso após o cadastro e a confirmação dos documentos antes de liberar o catálogo.

## Arquitetura

O frontend React chama a API Spring Boot. O PostgreSQL armazena os dados do domínio, incluindo perguntas e acertos, versões e aceites legais, auditoria, tokens e publicações de eventos. O Redis mantém sessões indexadas por usuário e contadores de autenticação. O limite padrão de inatividade é de 30 minutos; os tokens de confirmação e recuperação duram cinco minutos. Eventos de e-mail de conta são persistidos e reprocessados pelo Spring Modulith; o envio de OTT de login é assíncrono. A API envia e-mails via Resend, e o navegador carrega o player das aulas diretamente do YouTube. [Configuração da aplicação](../src/main/resources/application.properties), [Cliente Resend](../src/main/java/com/repositorio/investir_mais/infrastructure/ResendEmailSender.java)

O navegador carrega o player diretamente do YouTube. O backend não consulta a YouTube Data API e não precisa de `YOUTUBE_API_KEY`. Assunto, autoria, disponibilidade e restrições do vídeo não são verificados pela plataforma; professor e administrador conferem o material antes de publicar. [Guia dos vídeos](video-validation.md)

A arquitetura de implantação reflete os arquivos atuais: o frontend é compilado com Vite e servido pelo Caddy, que encaminha `/api`, `/auth` e `/csrf` para `BACKEND_URL`. O cookie de sessão permanece na origem do frontend. O diagrama não atesta o estado dos serviços em produção. [Dockerfile do frontend](../frontend/investirMais/Dockerfile), [Caddyfile](../frontend/investirMais/Caddyfile)

Os ícones de tecnologia estão incorporados ao arquivo editável. [Simple Icons](https://github.com/simple-icons/simple-icons), [Devicon](https://github.com/devicons/devicon)

## Atualização dos arquivos

Revisão técnica: 15/11/2026. As cinco visões refletem a validação local de links, os aceites no PostgreSQL e as permissões de ordenação para professor responsável e ADMIN. A atribuição e alteração de posições estão descritas no [guia de perguntas e ordenação](questions.md). O [roteiro dos slides](apresentacao.md) usa a mesma arquitetura.

O arquivo `diagramas/investir-mais.drawio` é a fonte editável das cinco imagens e possui uma exceção no `.gitignore` para poder ser versionado. Ao editar uma aba, exporte novamente seu PNG com o mesmo nome, incluindo uma cópia do diagrama na imagem. As prévias atuais foram exportadas com escala 1,5 e borda de 24 pixels; cada PNG também pode ser aberto no Draw.io para edição.


Com o Draw.io Desktop instalado, exporte cada aba usando o índice de 1 a 5. Exemplo no PowerShell para a arquitetura (ajuste o caminho do executável instalado):

```powershell
& 'C:\Program Files\draw.io\draw.io.exe' --export --page-index 5 --scale 1.5 --border 24 --embed-diagram --output docs/diagramas/arquitetura.png docs/diagramas/investir-mais.drawio
```

Confira o PNG exportado e se o diagrama incorporado corresponde à aba editável antes de versionar ambos.
