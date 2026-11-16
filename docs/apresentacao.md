# Apresentação do Investir Mais

[Abrir a apresentação no Canva](https://www.canva.com/design/DAHaAbqfp74/5FE3iNiXASrw3gszx9830w/edit)

O deck mantém dez slides e o título acadêmico do projeto. O MVP implementa a
plataforma educacional; carteira, cotações e gestão patrimonial são evolução
planejada. Os slides de contexto preservam suas fontes e datas de referência.

| Slide | Assunto | Referência técnica |
| --- | --- | --- |
| 1 | Projeto e equipe | [Escopo atual](../README.md#funcionalidades) |
| 2 | Problema | Fontes indicadas no próprio slide |
| 3 | Contexto educacional | Fontes indicadas no próprio slide |
| 4 | Dores do aluno e do professor | Motivação do projeto |
| 5 | Solução educacional | [Perguntas e progressão](questions.md) e [vídeos](video-validation.md) |
| 6 | Arquitetura | [Diagrama completo](diagramas/arquitetura.png) |
| 7 | Acesso, perfil e aceite legal | [Documentos e aceites](documentos-legais.md) |
| 8 | Permissões e gestão de professores | [Perfis](../README.md#funcionalidades) e [ordem das aulas](questions.md#ordenação-das-aulas) |
| 9 | E-mails transacionais | [BPMN de login](diagramas/bpmn-login-ott.png) |
| 10 | Encerramento | Síntese da proposta |

## Roteiro técnico

### Slide 1 — Escopo

O título reúne a proposta acadêmica de educação financeira e gestão patrimonial.
Nesta versão, demonstramos conta, catálogo de cursos, videoaulas, perguntas e
progressão. A carteira e as cotações ainda não fazem parte da aplicação implementada.

### Slide 5 — Solução

O professor organiza cursos com videoaulas e perguntas de múltipla escolha. O
professor responsável e o administrador podem ajustar a ordem das aulas. As
perguntas acompanham as aulas ao mudar a sequência. O aluno matriculado avança
conforme conclui as aulas anteriores e acerta as perguntas liberadas. O progresso
considera aulas concluídas e perguntas aprovadas, sem nota final ou histórico de
tentativas. Os vídeos ficam no YouTube: a plataforma aceita links de qualquer canal
e valida apenas o formato do endereço. Quem publica confere o conteúdo e as
condições de reprodução; não há análise automática de autoria ou disponibilidade.

### Slide 6 — Arquitetura

O React consome a API Spring Boot. Na implantação descrita pelos arquivos do
projeto, Vite compila a SPA e Caddy serve os arquivos e encaminha as rotas da API.
PostgreSQL guarda usuários, cursos, aulas, perguntas, progresso, auditoria, tokens,
eventos de e-mail e versões e aceites legais. Os documentos legais usam JDBC e
proteção contra alteração dos registros publicados. Redis mantém sessões e limites
de autenticação. Resend entrega confirmação, login e recuperação. O navegador
carrega o player diretamente do YouTube; o backend somente normaliza o link e a
miniatura, sem YouTube Data API ou chave. O diagrama descreve o projeto e não
comprova o estado dos serviços em produção.

### Slide 7 — Acesso e documentos

O cadastro cria uma conta de aluno com data de nascimento e confirmação de e-mail.
O link original no navegador do cadastro pode iniciar a primeira sessão. Os acessos
seguintes usam senha e link ou código de uso único por e-mail. Antes do catálogo,
a pessoa lê e confirma a versão atual dos Termos e da Política de Privacidade.
Versões e aceites ficam no PostgreSQL; uma versão nova exige nova confirmação.
O perfil permite editar nome e nascimento com senha atual, trocar a senha, excluir
a conta e consultar o texto de cada versão aceita. Esse aceite não é um
consentimento genérico para qualquer tratamento de dados.

### Slide 8 — Permissões

O aluno consulta os conteúdos e segue os bloqueios de progressão. O professor
gerencia os próprios cursos, aulas, perguntas e sequência; o ADMIN gerencia
qualquer curso e também pode reordenar suas aulas. O backend verifica perfil e
propriedade. O administrador pode promover um aluno a professor somente com
candidatura e documentos aprovados, sem idade mínima. O painel não concede nem altera o
papel ADMIN. Vídeos de qualquer canal podem ser usados, sem aprovação prévia de
canais, filtro temático ou verificação de disponibilidade pela plataforma.

## Manutenção

Ao mudar uma funcionalidade, revise o texto visível, as notas do apresentador e o
diagrama do slide 6. Use a arquitetura atual do Draw.io; a seta de reprodução do
YouTube parte do navegador, sem consulta externa do backend. As fontes estatísticas
e normativas dos slides de contexto têm revisão própria antes de uma apresentação.
