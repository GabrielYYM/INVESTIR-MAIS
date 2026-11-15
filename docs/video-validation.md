# Validação dos vídeos

Configure `YOUTUBE_API_KEY` no ambiente do backend, com YouTube Data API v3 habilitada. Restrinja a chave a essa API e ao servidor. Ela não é enviada ao frontend. Sem chave, a publicação e a aprovação de novos canais retornam 503; vídeos não verificados não liberam reprodução aos alunos.

No painel Administração, use **Canais de vídeo** na linha de um professor. Informe o ID público do canal, iniciado por `UC` e com 24 caracteres. O administrador deve conferir o vínculo e a autorização de uso antes de aprovar. Aprovar um canal confirma uma decisão administrativa; não comprova a titularidade dos direitos autorais de cada vídeo. A revogação exige confirmação e é verificada novamente nas próximas consultas dos alunos.

O backend aceita apenas URLs reconhecidas do YouTube e consulta `videos.list` para obter o canal, título, descrição, tags, privacidade, permissão de incorporação e restrições. URLs e capas são normalizadas pelo servidor. O canal é comparado com as aprovações do professor responsável pelo curso, inclusive quando um administrador edita a aula.

Vídeos ausentes, privados, não incorporáveis, em processamento, ao vivo/agendados, bloqueados no Brasil ou com restrição etária são recusados. Público e não listado são aceitos se passarem nas demais verificações. A disponibilidade é uma verificação pontual: o YouTube ainda pode impedir a reprodução por alterações, políticas ou reivindicações posteriores.

A triagem de economia, finanças e investimentos consulta termos nos metadados públicos do YouTube, nunca o título/descrição da aula preenchidos na plataforma. Há falsos positivos e negativos; isso não equivale a assistir ao vídeo, analisar áudio/transcrição, avaliar qualidade pedagógica ou comprovar direitos autorais. O administrador deve selecionar canais confiáveis e conferir o material quando necessário.

Na entrega aos alunos, aulas existentes também são verificadas. A lista continua visível, mas URL e capa são omitidas quando as verificações falham. Criação e edição consultam o YouTube novamente, sem reutilizar metadados em cache. Na entrega aos alunos, metadados públicos têm cache local limitado a 1.000 vídeos por até dois minutos; as aprovações de canais são consultadas no banco em cada verificação. Consultas de uma página são agrupadas em lotes de até 50 IDs para limitar o consumo de quota. As rotas de progresso e as regras de perguntas continuam independentes da triagem de vídeos.

O Hibernate adiciona a tabela `tb_teacher_youtube_channel` no próximo início da aplicação. Faça backup do banco oficial antes de aplicar mudanças de esquema, conforme AGENTS.md. Nenhuma aprovação é criada automaticamente para os registros antigos.

Referências: [Videos](https://developers.google.com/youtube/data/reference/rest/v3/videos), [Channels](https://developers.google.com/youtube/data/reference/rest/v3/channels), [uso de chaves de API](https://docs.cloud.google.com/docs/authentication/api-keys-use).
