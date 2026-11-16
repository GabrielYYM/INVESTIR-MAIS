# Privacidade e Proteção de Dados (LGPD)

Esta pasta reúne os documentos de conformidade da plataforma **Investir Mais** com a **LGPD (Lei nº 13.709/2018)**, o **Marco Civil da Internet (Lei nº 12.965/2014)** e a **Resolução CD/ANPD nº 2/2022** (Agentes de Tratamento de Pequeno Porte — ATPP).

## Documentos oficiais

| Documento | Versão | Descrição |
| :--- | :---: | :--- |
| [Registro das Operações de Tratamento (ROPA — ATPP)](./Planilha%20sem%20t%C3%ADtulo%20-%20Modelo%20de%20ROPA%20-%20ATPP.pdf) | `15/11/2026` | Inventário de dados pessoais, bases legais, medidas de segurança, retenção e compartilhamento no modelo da ANPD. |
| [Política de Privacidade](./Politica_de_privacidade_InvestirMais.pdf) | `v2.0` | Regras de coleta, finalidades, bases legais, retenção, segurança e direitos dos titulares. |
| [Termos e Condições de Uso](./Termos_e_Condicoes_InvestirMais.pdf) | `v2.0` | Regras de acesso, aviso legal financeiro (*disclaimer*), publicação de conteúdo audiovisual, moderação e responsabilidades. |

- **Controladora:** Investir Mais (ATPP — Resolução CD/ANPD nº 2/2022)
- **Encarregado de Dados (DPO):** Gabriel Yukio Yamaguti Minato
- **Canal de Privacidade, Denúncias e Suporte:** `investirmaisinfo@gmail.com`

## Resumo do tratamento de dados

- **Dados coletados:** nome, e-mail, data de nascimento (verificação de maioridade para perfis `TEACHER` e `ADMIN`), senha com hash irreversível (`Argon2id`), códigos temporários de 2º fator (`OTT`), cookies técnicos (`SESSION` e `XSRF-TOKEN`), registros de acesso (IP e data/hora), progresso nos cursos (matrículas, aulas concluídas e questões aprovadas) e histórico de aceite legal e auditoria cadastral.
- **Minimização:** não há coleta de dados pessoais sensíveis (art. 5º, II, da LGPD), cookies publicitários ou histórico de alternativas incorretas marcadas pelos alunos.
- **Bases legais principais (Arts. 7º e 11 da LGPD):**
  - *Execução de contrato (Art. 7º, V):* cadastro, login, matrículas, progresso nos cursos e envio de e-mails transacionais.
  - *Prevenção à fraude e segurança (Art. 11, II, "g"):* verificação em duas etapas (2FA / OTT) e proteção de sessão.
  - *Obrigação legal, exercício regular de direitos e legítimo interesse (Art. 7º, II, VI e IX c/c Art. 15 do MCI):* guarda de logs de acesso, comprovantes de aceite legal, auditoria cadastral e limitação de tentativas (*rate limiting*).
- **Compartilhamento:** restrito aos operadores essenciais ao funcionamento do sistema — **Resend** (e-mails transacionais), **PostgreSQL** (banco de dados), **Redis** (sessões e *rate limit*) e player incorporado de vídeo (**YouTube**). Os dados jamais são vendidos ou cedidos para fins comerciais.
- **Retenção e exclusão:**
  - *Cadastro e progresso:* mantidos enquanto a conta estiver ativa ou até a exclusão pelo usuário em `/perfil` (que desativa a conta, encerra sessões e invalida códigos pendentes).
  - *Logs de conexão:* mínimo de **6 meses** (Art. 15 do Marco Civil da Internet).
  - *Aceites legais e auditoria de segurança:* até **5 anos** após o encerramento (Art. 16, I e Art. 7º, VI da LGPD).
  - *Sessões e tokens:* sessões expiram em **30 minutos** de inatividade e códigos temporários em **5 minutos**.

## Segurança e direitos do titular

- **Controles técnicos (Art. 46):** senhas com `Argon2id`, autenticação em duas etapas por e-mail (`OTT` com hash `SHA-256`), tráfego sob HTTPS/TLS, cookies `HttpOnly`/`SameSite=Lax`/`Secure`, proteção CSRF, bloqueio de força bruta no Redis e controle de acesso por papéis (`STUDENT`, `TEACHER`, `ADMIN`).
- **Direitos do titular (Art. 18):** consulta de dados, progresso e histórico de versões aceitas, edição de perfil e exclusão de conta diretamente em `/perfil`, ou atendimento pelo e-mail `investirmaisinfo@gmail.com`.

## Guias técnicos relacionados

- [Publicação e versionamento de documentos legais](../documentos-legais.md)
- [Validação de vídeos do YouTube](../video-validation.md)
- [Perguntas e progressão de aulas](../questions.md)
