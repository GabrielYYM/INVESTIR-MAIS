# Vídeos do YouTube e Cloudinary

Professores responsáveis e administradores podem cadastrar e editar aulas com links de vídeos de qualquer canal do YouTube. Não há aprovação de canais, filtro temático nem consulta à YouTube Data API. `YOUTUBE_API_KEY` não é necessária.

O frontend e o backend validam somente o formato do endereço: domínio reconhecido do YouTube, protocolo HTTP/HTTPS e ID do vídeo. O servidor normaliza o link e gera a URL da miniatura a partir desse ID, sem aceitar a capa informada como referência definitiva. Links com credenciais, porta personalizada, domínio falso ou ID inválido são recusados.

A plataforma não verifica autoria, assunto, privacidade, disponibilidade, permissão de incorporação nem restrições de região e idade. O professor deve conferir a adequação do material e sua autorização de uso. A reprodução depende das condições do YouTube e do vídeo no momento do acesso.

As permissões de gestão e a ordem das aulas continuam aplicadas. Os vídeos ficam disponíveis em qualquer ordem, sem bloqueio por questões ou aulas anteriores. As questões permanecem dentro das aulas, com suas regras de resposta e participação no progresso. A remoção dos filtros não permite que alunos editem aulas nem que professores alterem cursos de outros professores.

A tabela antiga `tb_teacher_youtube_channel`, quando já existente, é preservada no banco. O sistema não consulta essas aprovações nem exclui seus registros automaticamente; nenhuma mudança destrutiva de esquema é necessária para esta atualização.

## Upload pelo Cloudinary

O formulário da aula também permite enviar MP4, WebM ou MOV, até 100 MB,
com autenticação do backend no Cloudinary. O usuário salva a aula depois do envio;
o vídeo é reproduzido por um elemento HTML `video`. Sem as três credenciais
`CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` e `CLOUDINARY_API_SECRET`, o envio
retorna 503. O link do YouTube continua disponível. Não há upload automático
para uma conta do YouTube.

Consulte os [limites e a configuração](atualizacao-plataforma.md#vídeos), inclusive
o tratamento de arquivos enviados quando a edição da aula é cancelada.
