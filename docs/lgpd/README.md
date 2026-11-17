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

Idade e data de nascimento não são mais solicitadas, persistidas pelo modelo ou retornadas pela API. O campo técnico `name` representa o apelido e preserva os valores das contas existentes. Colunas legadas de bancos existentes e revisões antigas não são apagadas automaticamente; sua remoção física exige backup e migração específica. Documentos legais já publicados permanecem imutáveis: textos que descrevam a coleta anterior devem ser atualizados em uma nova versão pelo painel de documentos legais.

- **Dados coletados:** apelido, e-mail, senha com hash irreversível (`Argon2id`), códigos temporários de 2º fator (`OTT`), cookies técnicos (`SESSION` e `XSRF-TOKEN`), registros de acesso (IP e data/hora), progresso nos cursos (matrículas, aulas concluídas e questões aprovadas) e histórico de aceite legal e auditoria cadastral.
- **Minimização:** o histórico de respostas passou a registrar alternativa escolhida, resultado e instante para consulta do próprio aluno; os professores recebem métricas agregadas. As candidaturas incluem documentos privados enviados pelo interessado, cujo conteúdo depende dos tipos configurados.
- **Bases legais principais (Arts. 7º e 11 da LGPD):**
  - *Execução de contrato (Art. 7º, V):* cadastro, login, matrículas, progresso nos cursos e envio de e-mails transacionais.
  - *Prevenção à fraude e segurança (Art. 11, II, "g"):* verificação em duas etapas (2FA / OTT) e proteção de sessão.
  - *Obrigação legal, exercício regular de direitos e legítimo interesse (Art. 7º, II, VI e IX c/c Art. 15 do MCI):* guarda de logs de acesso, comprovantes de aceite legal, auditoria cadastral e limitação de tentativas (*rate limiting*).
- **Compartilhamento:** restrito aos operadores essenciais ao funcionamento do sistema — **Resend** (e-mails transacionais), **PostgreSQL** (banco de dados), **Redis** (sessões e *rate limit*) e player incorporado de vídeo (**YouTube**) e hospedagem de vídeos enviados (**Cloudinary**, quando configurado). Os dados jamais são vendidos ou cedidos para fins comerciais.
- **Retenção e exclusão:**
  - *Cadastro e progresso:* mantidos enquanto a conta estiver ativa ou até a exclusão pelo usuário em `/perfil` (que desativa a conta, limpa dados pessoais do registro principal, remove os documentos da candidatura, encerra sessões e invalida códigos pendentes).
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

A [atualização técnica da plataforma](../atualizacao-plataforma.md) descreve os novos
fluxos de documentos, respostas e exclusão. Os textos legais publicados não foram
alterados automaticamente; a instituição deve revisar suas informações antes de
abrir os novos fluxos ao público.

Os prazos descritos acima não são rotinas automáticas de expurgo: a implementação preserva auditoria, aceites e vínculos de progresso até existir uma política operacional de remoção.
