# Diagramas do Investir Mais

Abra o [arquivo editável do Draw.io](diagramas/investir-mais.drawio) e selecione uma das cinco abas. O [README principal](../README.md#diagramas) inclui prévias dos cinco diagramas.

| Diagrama | O que mostra |
| --- | --- |
| [01 · Classes de domínio](diagramas/classes-de-dominio.png) | Entidades JPA, perguntas, perfis, matrículas, progresso e acertos. |
| [02 · Classes da aplicação](diagramas/classes-da-aplicacao.png) | Controllers, serviços, repositórios, mapeadores e envio de e-mails. |
| [03 · Fluxo de navegação](diagramas/fluxo-de-navegacao.png) | Cadastro, login, páginas por perfil e progressão com perguntas. |
| [04 · BPMN de acesso](diagramas/bpmn-login-ott.png) | Uma tentativa de login com senha e token de uso único (OTT). |
| [05 · Diagrama de arquitetura](diagramas/arquitetura.png) | Frontend, API, dados, e-mails e implantação de referência. |

O diagrama de acesso usa BPMN 2.0 para documentar o processo. [Especificação BPMN 2.0.2 da OMG](https://www.omg.org/spec/BPMN/2.0.2/)

## Classes de domínio

Cada curso referencia um usuário proprietário e reúne aulas e perguntas. Cada aula e cada pergunta pertencem a um curso. A exclusão lógica se propaga de usuário para cursos e de curso para aulas e perguntas. A senha é armazenada como hash Argon2 e fica fora da auditoria do Hibernate Envers. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [Course](../src/main/java/com/repositorio/investir_mais/domain/education/model/Course.java), [Lesson](../src/main/java/com/repositorio/investir_mais/domain/education/model/Lesson.java), [Question](../src/main/java/com/repositorio/investir_mais/domain/education/model/Question.java)

Matrículas e progresso pertencem a `User` e são persistidos como coleções de valores embutíveis. A matrícula é indexada por curso e a conclusão por aula. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java)

As aulas possuem `position`. As perguntas são ordenadas por `afterLesson`, `position` e `id`; `afterLesson = 0` indica uma pergunta anterior à primeira aula. As alternativas ficam em `Question.options`, persistidas como ARRAY, e `correctOptionIndex` identifica a alternativa correta. Não existem entidades separadas de alternativa ou tentativa. [Question](../src/main/java/com/repositorio/investir_mais/domain/education/model/Question.java), [Course](../src/main/java/com/repositorio/investir_mais/domain/education/model/Course.java)

Os acertos ficam em `User.passedQuestionIds`, uma coleção de UUIDs na tabela `tb_question_pass`, com unicidade por aluno e pergunta. Essa coleção não é uma associação JPA com `Question`. Uma alteração na pergunta limpa os acertos anteriores. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java)

## Classes da aplicação

O `UserController` atende o painel do professor com apoio de `CourseService`. O `AuthenticationEmailHandler` recebe os eventos de conta, monta as mensagens e as envia por meio do `ResendEmailSender`. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java)

`CourseController`, `LessonController` e `QuestionController` delegam as operações educacionais aos respectivos serviços. `QuestionService` controla a aprovação e os bloqueios de conteúdo, com apoio de `CourseService`, `UserService` e dos repositórios de aulas, perguntas e usuários. `LessonService` consulta essas regras para exibir ou concluir aulas. [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

Os mapeadores `UserMapper`, `CourseMapper`, `LessonMapper` e `QuestionMapper` usam MapStruct. O `QuestionMapper` valida o índice da alternativa correta e a duplicidade das opções antes de criar ou atualizar a entidade. `AccountSecurityService` usa JDBC para tokens de confirmação e recuperação e revoga sessões indexadas no Redis. [QuestionMapper](../src/main/java/com/repositorio/investir_mais/domain/education/mapper/QuestionMapper.java), [AccountSecurityService](../src/main/java/com/repositorio/investir_mais/domain/AccountSecurityService.java)

## Navegação e permissões

O React Router consulta `/auth/me` antes de liberar a área protegida. Em caso de sessão ausente ou incompleta, a interface redireciona para o login com a rota de retorno. Após o OTT, abre o destino interno permitido ou o painel padrão. O cadastro público cria contas de aluno e exige confirmação de e-mail. [Rotas React](../frontend/investirMais/src/routes.jsx), [Tela de login](../frontend/investirMais/src/pages/Login.jsx), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

Quando a pessoa confirma o link original no navegador do cadastro, a aplicação inicia a primeira sessão e abre o painel. Em outro navegador ou com um link reenviado, a confirmação libera o login. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java)

| Perfil | O que pode fazer no catálogo |
| --- | --- |
| Aluno (`STUDENT`) | Consultar cursos, matricular-se, concluir aulas e responder perguntas liberadas. |
| Professor (`TEACHER`) | Gerenciar os próprios cursos, aulas e perguntas e acompanhar indicadores. |
| Admin (`ADMIN`) | Gerenciar o catálogo, os papéis de usuários e o histórico de auditoria. |

Os três perfis usam o mesmo painel. A interface exibe as ações de acordo com o perfil e a propriedade, e o backend aplica as mesmas verificações nas operações de escrita. [Ações do catálogo](../frontend/investirMais/src/services/educationData.js), [CourseService](../src/main/java/com/repositorio/investir_mais/domain/education/service/CourseService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

A página de curso reúne as aulas e as perguntas. Em cursos com perguntas, o aluno deve concluir o conteúdo anterior e acertar as perguntas anteriores para avançar; aulas bloqueadas continuam visíveis no currículo, mas seus detalhes e mídias são ocultados. A listagem do aluno omite o gabarito, e a listagem de gestão exige professor proprietário ou administrador. O percentual do curso considera aulas concluídas e perguntas aprovadas. [QuestionService](../src/main/java/com/repositorio/investir_mais/domain/education/service/QuestionService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java), [CourseService](../src/main/java/com/repositorio/investir_mais/domain/education/service/CourseService.java)

O perfil oferece alteração de nome com senha atual, troca de senha, recuperação e exclusão da conta. O painel `/admin` permite consultar usuários, alterar os papéis permitidos e consultar auditoria; o backend impede conceder ou alterar o papel `ADMIN` pelo painel. Termos e privacidade possuem rotas públicas. [Perfil](../frontend/investirMais/src/pages/Perfil.jsx), [Admin](../frontend/investirMais/src/pages/Admin.jsx), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

## Processo de acesso com OTT

O BPMN usa três raias para mostrar as responsabilidades da pessoa usuária, do React e da API Spring. A API valida a senha e o e-mail verificado, registra o desafio na sessão parcial e dispara o envio de um link e um código por e-mail. A pessoa abre o link no navegador em que começou o login ou informa o código na tela. As duas opções chamam `POST /auth/verify-2fa` com proteção CSRF. O Spring consome o token válido, conclui a autenticação e troca o identificador da sessão. [SecurityConfig](../src/main/java/com/repositorio/investir_mais/infrastructure/SecurityConfig.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java), [Login.jsx](../frontend/investirMais/src/pages/Login.jsx)

O recorte termina quando o acesso é concluído ou quando as credenciais ou o OTT são recusados. A etapa de abertura pressupõe que a mensagem chegou. O [fluxo de navegação](../README.md#fluxo-de-navegação) descreve o primeiro acesso após o cadastro.

## Arquitetura

O frontend React chama a API Spring Boot. O PostgreSQL armazena os dados do domínio, incluindo perguntas e acertos, a auditoria, os tokens e as publicações de eventos. O Redis mantém sessões indexadas por usuário e contadores de autenticação. O limite padrão de inatividade é de 30 minutos; os tokens de confirmação e recuperação duram cinco minutos. Eventos de e-mail de conta são persistidos e reprocessados pelo Spring Modulith; o envio de OTT de login é assíncrono. A API envia e-mails via Resend, e o navegador carrega o player das aulas diretamente do YouTube. [Configuração da aplicação](../src/main/resources/application.properties), [Cliente Resend](../src/main/java/com/repositorio/investir_mais/infrastructure/ResendEmailSender.java)

A arquitetura de implantação reflete os arquivos atuais: o frontend é compilado com Vite e servido pelo Caddy, que encaminha `/api`, `/auth` e `/csrf` para `BACKEND_URL`. O cookie de sessão permanece na origem do frontend. O diagrama não atesta o estado dos serviços em produção. [Dockerfile do frontend](../frontend/investirMais/Dockerfile), [Caddyfile](../frontend/investirMais/Caddyfile)

Os ícones de tecnologia estão incorporados ao arquivo editável. [Simple Icons](https://github.com/simple-icons/simple-icons), [Devicon](https://github.com/devicons/devicon)
