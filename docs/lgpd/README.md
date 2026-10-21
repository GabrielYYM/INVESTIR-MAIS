# Privacidade e dados pessoais: estado técnico

> Este arquivo registra somente comportamentos identificados no código atual. Ele não substitui uma política de privacidade, os Termos de Uso nem uma avaliação jurídica. As descrições anteriores de fluxo de consentimento parental, coleta de data de nascimento e criptografia AES-GCM não correspondiam à implementação e foram removidas para evitar que fossem tratadas como fatos.

## Dados identificados no backend

A entidade `User` (`domain/user/model/User.java`) contém nome, idade, e-mail, senha, perfil, estado de verificação do e-mail e datas de criação/alteração. O backend usa `Argon2PasswordEncoder` para codificar senhas. A entidade tem auditoria Envers e campos temporais de Spring Data JPA Auditing.

O backend também armazena cursos e aulas. Tokens de uso único, autorizações OAuth e sessões são persistidos em tabelas JDBC/PostgreSQL. O armazenamento exato e os períodos de retenção dependem da configuração do banco e dos componentes utilizados.

## Controles técnicos observados

- Senhas: hash por Argon2 via Spring Security; isso não é criptografia reversível de senha.
- Autenticação de API: validação de JWT RSA com Spring Security OAuth2 Resource Server.
- Chaves de assinatura: keystore PKCS#12 configurável; produção desabilita a geração de chave efêmera.
- Sessão: cookie `HttpOnly`, `SameSite=Lax`; o perfil `prod` exige cookie `Secure`.
- Integração de cotação: o backend consulta Brapi com timeouts configurados e Circuit Breaker.
- Rate limit de autenticação: contador persistido no banco via `JdbcTemplate`.

## Limites e itens a confirmar

- O modelo atual não contém `birthDate` ou `guardianEmail`, nem implementa o fluxo descrito anteriormente de consentimento e verificação dupla para menores.
- Não foi localizada implementação de criptografia AES-256-GCM para campos pessoais em repouso. A proteção em repouso depende também do banco, do armazenamento e da infraestrutura implantada.
- Os registros e finalidades de tratamento, bases legais, compartilhamentos, canal de contato, direitos e prazos de retenção precisam ser definidos pelo controlador e confirmados com a operação real do serviço.
- Antes de uso público, publicar uma política e Termos de Uso revisados de acordo com o produto implantado e com a orientação jurídica adequada.
