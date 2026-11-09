# Diagramas do Investir Mais

Abra o [arquivo editável do Draw.io](diagramas/investir-mais.drawio) e selecione uma das cinco abas. O [README principal](../README.md#diagramas) inclui prévias dos cinco diagramas.

| Diagrama | O que mostra |
| --- | --- |
| [01 · Classes de domínio](diagramas/classes-de-dominio.png) | Entidades JPA, perfis, matrículas, progresso e regras de persistência. |
| [02 · Classes da aplicação](diagramas/classes-da-aplicacao.png) | Dependências de conta, educação e envio de e-mails. |
| [03 · Fluxo de navegação](diagramas/fluxo-de-navegacao.png) | Cadastro, primeiro acesso, login e páginas por perfil. |
| [04 · BPMN de acesso](diagramas/bpmn-login-ott.png) | Uma tentativa de login com senha e token de uso único (OTT). |
| [05 · Diagrama de arquitetura](diagramas/arquitetura.png) | Frontend, API, dados, e-mails e implantação de referência. |

O diagrama de acesso usa BPMN 2.0 para documentar o processo. [Especificação BPMN 2.0.2 da OMG](https://www.omg.org/spec/BPMN/2.0.2/)

## Classes de domínio

Cada curso referencia um usuário proprietário. Cada aula referencia um curso. A exclusão lógica se propaga de usuário para cursos e de curso para aulas. A senha é armazenada como hash Argon2 e fica fora da auditoria do Hibernate Envers. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java), [Course](../src/main/java/com/repositorio/investir_mais/domain/education/model/Course.java), [Lesson](../src/main/java/com/repositorio/investir_mais/domain/education/model/Lesson.java)

Matrículas e progresso pertencem a `User` e são persistidos como coleções de valores embutíveis. A matrícula é indexada por curso e a conclusão por aula. [User](../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java)

## Classes da aplicação

O `UserController` atende o painel do professor com apoio de `CourseService`. O `AuthenticationEmailHandler` recebe os eventos de conta, monta as mensagens e as envia por meio do `ResendEmailSender`. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java)

## Navegação e permissões

O React Router consulta `/auth/me` antes de liberar a área protegida. Em caso de sessão ausente ou incompleta, a interface redireciona para o login com a rota de retorno. Após o OTT, abre o destino interno permitido ou o painel padrão. O cadastro público cria contas de aluno e exige confirmação de e-mail. [Rotas React](../frontend/investirMais/src/App.jsx), [Tela de login](../frontend/investirMais/src/pages/Login.jsx), [UserService](../src/main/java/com/repositorio/investir_mais/domain/user/service/UserService.java)

Quando a pessoa confirma o link original no navegador do cadastro, a aplicação inicia a primeira sessão e abre o painel. Em outro navegador ou com um link reenviado, a confirmação libera o login. [UserController](../src/main/java/com/repositorio/investir_mais/domain/user/controller/UserController.java)

| Perfil | O que pode fazer no catálogo |
| --- | --- |
| Aluno (`STUDENT`) | Consultar cursos, matricular-se e registrar a conclusão de aulas. |
| Professor (`TEACHER`) | Gerenciar os próprios cursos e aulas e acompanhar indicadores. |
| Admin (`ADMIN`) | Gerenciar o catálogo e os usuários. |

Os três perfis usam o mesmo painel. A interface exibe as ações de acordo com o perfil e a propriedade, e o backend aplica as mesmas verificações nas operações de escrita. [Ações do catálogo](../frontend/investirMais/src/services/educationData.js), [CourseService](../src/main/java/com/repositorio/investir_mais/domain/education/service/CourseService.java), [LessonService](../src/main/java/com/repositorio/investir_mais/domain/education/service/LessonService.java)

## Processo de acesso com OTT

O BPMN usa três raias para mostrar as responsabilidades da pessoa usuária, do React e da API Spring. A API valida a senha e o e-mail verificado, registra o desafio na sessão parcial e dispara o envio de um link e um código por e-mail. A pessoa abre o link no navegador em que começou o login ou informa o código na tela. As duas opções chamam `POST /auth/verify-2fa` com proteção CSRF. O Spring consome o token válido, conclui a autenticação e troca o identificador da sessão. [SecurityConfig](../src/main/java/com/repositorio/investir_mais/infrastructure/SecurityConfig.java), [AuthenticationEmailHandler](../src/main/java/com/repositorio/investir_mais/infrastructure/AuthenticationEmailHandler.java), [Login.jsx](../frontend/investirMais/src/pages/Login.jsx)

O recorte termina quando o acesso é concluído ou quando as credenciais ou o OTT são recusados. A etapa de abertura pressupõe que a mensagem chegou. O [fluxo de navegação](../README.md#fluxo-de-navegação) descreve o primeiro acesso após o cadastro.

## Arquitetura

O frontend React chama a API Spring Boot. O PostgreSQL armazena os dados do domínio, os tokens e as publicações de eventos. O Redis mantém sessões e contadores de autenticação. A API envia e-mails via Resend, e o navegador carrega o player das aulas diretamente do YouTube. [Configuração da aplicação](../src/main/resources/application.properties), [Cliente Resend](../src/main/java/com/repositorio/investir_mais/infrastructure/ResendEmailSender.java)

O proxy Caddy no Railway é a implantação de referência descrita na [PR #45](https://github.com/GabrielYYM/INVESTIR-MAIS/pull/45).

Os ícones de tecnologia estão incorporados ao arquivo editável. [Simple Icons](https://github.com/simple-icons/simple-icons), [Devicon](https://github.com/devicons/devicon)
