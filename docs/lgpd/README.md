# Privacidade e dados pessoais: estado técnico

Este guia descreve a implementação do repositório em 15/11/2026. Não substitui os textos legais publicados nem uma avaliação jurídica ou da infraestrutura em produção.

## Dados identificados no backend

A entidade [User](../../src/main/java/com/repositorio/investir_mais/domain/user/model/User.java) contém nome, e-mail, data de nascimento, idade legada, hash da senha, perfil, verificação do e-mail e metadados de auditoria. A idade atual é calculada a partir do nascimento; registros antigos podem manter apenas a idade legada. O backend usa Argon2id para senhas e auditoria Envers para o usuário.

O backend armazena cursos, aulas, perguntas, matrículas, conclusão de aulas e IDs das perguntas aprovadas. Não guarda erros, alternativas marcadas, número de tentativas ou notas finais. Veja o [guia de perguntas](../questions.md). O PostgreSQL mantém tokens de uso único e publicações de eventos de e-mail. O Redis mantém sessões e contadores de tentativas. A retenção também depende do armazenamento e dos backups.

`legal_document_release` contém cada versão dos Termos e da Política de Privacidade, data e hash do conteúdo. `legal_document_acceptance` registra usuário, versão e horário do aceite obtidos no servidor. As tabelas usam JDBC e proteção contra atualização, exclusão e truncamento. O perfil oferece histórico com o texto exato de cada versão aceita; uma versão nova exige nova confirmação. O aceite registra concordância com os Termos e ciência da Política, não consentimento genérico para qualquer tratamento. Veja o [guia de documentos legais](../documentos-legais.md).

## Controles técnicos observados

- O Spring Security usa Argon2id para senhas e exclui o hash das revisões de auditoria.
- A API usa sessão com senha, código de e-mail e CSRF para gravações. O cookie de sessão usa `HttpOnly` e `SameSite=Lax`. O perfil `prod` exige `Secure`.
- A tabela de validação armazena o hash dos novos tokens de confirmação e recuperação. O código necessário para envio continua na publicação de e-mail pendente até o processamento.
- O Redis limita as requisições de autenticação por IP e rota e também por conta no login.
- O cadastro exige nascimento. A promoção a professor exige nascimento informado e idade mínima de 18 anos; isso não equivale a verificar documentalmente a idade.
- A exclusão de conta é lógica, exige senha e revoga sessões. Preserva registros; não constitui expurgo dos dados nem implementação de todos os direitos do titular.

## Serviços externos

O Resend recebe os dados necessários aos e-mails transacionais. O navegador carrega o player diretamente do YouTube. O backend não consulta a YouTube Data API nem exige chave dessa API. Não há aprovação de canais, análise de autoria ou filtro de conteúdo. Veja os [limites dos vídeos](../video-validation.md). Não há integração ativa com Brapi ou carteira de investimentos no escopo atual.

## Limites e itens a confirmar

- O modelo contém `birthDate`, mas não `guardianEmail`; não implementa fluxo de consentimento e verificação de responsável por menores.
- Não foi localizada implementação de criptografia AES-256-GCM para campos pessoais em repouso. A proteção em repouso depende também do banco, do armazenamento e da infraestrutura implantada.
- Os registros e finalidades de tratamento, bases legais, compartilhamentos, canal de contato, direitos e prazos de retenção precisam ser definidos pelo controlador e confirmados com a operação real do serviço.
- Os textos legais publicados devem ser revisados conforme o produto implantado e a orientação jurídica adequada. Mudanças exigem nova versão, preservando os textos e aceites anteriores. Os controles descritos aqui não comprovam conformidade integral com a LGPD.
