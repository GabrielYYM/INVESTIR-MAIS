# Privacidade e dados pessoais: estado técnico

> Este arquivo registra somente comportamentos identificados no código atual. Ele não substitui uma política de privacidade, os Termos de Uso nem uma avaliação jurídica. As descrições anteriores de fluxo de consentimento parental, coleta de data de nascimento e criptografia AES-GCM não correspondiam à implementação e foram removidas para evitar que fossem tratadas como fatos.

## Dados identificados no backend

A entidade `User` (`domain/user/model/User.java`) contém nome, idade, e-mail, senha, perfil, estado de verificação do e-mail e datas de criação/alteração. O backend usa `Argon2PasswordEncoder` para codificar senhas. A entidade tem auditoria Envers e campos temporais de Spring Data JPA Auditing.

O backend também armazena cursos e aulas. O PostgreSQL mantém os registros de tokens de uso único e as publicações pendentes de e-mail. O Redis mantém as sessões e os contadores de tentativas. Os períodos de retenção também dependem da configuração de armazenamento e de backups.

## Controles técnicos observados

- O Spring Security usa Argon2id para senhas e exclui o hash das revisões de auditoria.
- A API usa sessão com senha, código de e-mail e CSRF para gravações. O cookie de sessão usa `HttpOnly` e `SameSite=Lax`. O perfil `prod` exige `Secure`.
- A tabela de validação armazena o hash dos novos tokens de confirmação e recuperação. O código necessário para envio continua na publicação de e-mail pendente até o processamento.
- O backend consulta a Brapi com timeouts e circuit breaker. O Redis limita as requisições de autenticação por IP e rota e também por conta no login.

## Limites e itens a confirmar

- O modelo atual não contém `birthDate` ou `guardianEmail`, nem implementa o fluxo descrito anteriormente de consentimento e verificação dupla para menores.
- Não foi localizada implementação de criptografia AES-256-GCM para campos pessoais em repouso. A proteção em repouso depende também do banco, do armazenamento e da infraestrutura implantada.
- Os registros e finalidades de tratamento, bases legais, compartilhamentos, canal de contato, direitos e prazos de retenção precisam ser definidos pelo controlador e confirmados com a operação real do serviço.
- Antes de uso público, publicar uma política e Termos de Uso revisados de acordo com o produto implantado e com a orientação jurídica adequada.
