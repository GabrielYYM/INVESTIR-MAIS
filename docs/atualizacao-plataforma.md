# Atualização da plataforma — novembro de 2026

## Cadastro de professores

O interessado cria e confirma sua conta de aluno e acessa **Meu perfil → Quero ser
professor**, em `/cadastro-professor`. Envia um arquivo por tipo exigido. O
administrador analisa os arquivos em **Administração → Candidaturas de professores**
e registra um parecer para aprovar ou reprovar. Uma reprovação permite novo envio;
uma candidatura pendente não aceita substituição dos documentos.

Não existe idade mínima para conceder o perfil de professor. Nome e data de
nascimento continuam no cadastro; a alteração não os remove globalmente. O PATCH
de papel exige candidatura aprovada para promover um aluno. Professores existentes
continuam com seus acessos.

`TEACHER_REQUIRED_DOCUMENTS` configura de um a quatro identificadores separados
por vírgula; padrão: `comprovante-de-qualificacao`. A configuração técnica não
define quais documentos são adequados para uma instituição: configure a lista
adotada antes de abrir as candidaturas.

São aceitos PDF, PNG e JPEG, até 5 MB por arquivo. Os documentos são armazenados
no PostgreSQL e baixados como anexos, mediante sessão, pelo próprio candidato ou
ADMIN. A listagem transmite apenas metadados, sem carregar os binários. Não há
análise automática de autenticidade nem antivírus integrado. A exclusão da conta
remove também a candidatura e seus arquivos.

| Rota | Operação |
| --- | --- |
| `GET /api/teacher-applications/requirements` | Tipos de documentos exigidos |
| `GET /api/teacher-applications/mine` | Candidatura do usuário atual |
| `POST /api/teacher-applications` | Multipart: campos repetidos `types` e `files` |
| `GET /api/teacher-applications` | Listagem paginada para ADMIN |
| `POST /api/teacher-applications/{id}/review` | ADMIN: `approve` e `reason` |
| `GET /api/teacher-applications/{id}/documents/{documentId}` | Download privado |

## Cursos, questões e painéis

Os cursos têm `category`, `keywords` e `createdAt`. Novos cursos sem categoria
informada recebem `Geral`. A pesquisa aceita `query` para nome/palavras-chave,
`category` e `sort=name,asc`, `createdAt,desc` ou `createdAt,asc`, além de paginação.
O catálogo tem os campos correspondentes e preserva os filtros na URL.

O painel separa as matrículas do catálogo. Professores também têm **Cursos que
criei**. Dentro de cada curso, seu responsável e ADMIN consultam alunos, aulas
concluídas, quantidade de respostas e taxas de acerto por aluno e por questão.
O professor não recebe a alternativa marcada pelo aluno e não pode responder às
questões do próprio curso.

Os vídeos podem ser assistidos e marcados como concluídos em qualquer ordem, sem depender de questões ou aulas anteriores. Editar questões não apaga nem oculta o progresso dos vídeos. As questões permanecem dentro da aula correspondente à posição `afterLesson`.
Questões legadas com posição zero aparecem dentro da primeira aula. A interface
orienta criar primeiro o curso e a aula; o campo legado `questions` do cadastro de
curso permanece compatível na API. Posições positivas inexistentes são rejeitadas
na criação e edição de questões individuais.

Cada nova tentativa válida gera `question_attempt`, com aluno, curso, questão,
enunciado apresentado, alternativa marcada, resultado e instante. O próprio aluno
consulta esse histórico em `/api/learning/answers`, com paginação. Tentativas
anteriores à atualização não são reconstruídas. Reenviar uma resposta depois de
já obter aprovação não produz outra tentativa. `tb_question_pass` continua
controlando a progressão; editar a questão invalida a aprovação, preservando as
tentativas históricas. A taxa de acerto considera todas as tentativas registradas,
inclusive anteriores à edição da questão; não é uma nota final.

`GET /api/learning/courses` lista matrículas do usuário atual. As rotas
`GET /api/learning/courses/{courseId}/students` e `/questions` retornam métricas
somente ao professor responsável ou ADMIN. Contas excluídas não entram nas métricas
ativas. O histórico individual permanece vinculado ao identificador interno, sem
copiar nome, e-mail ou nascimento para a tabela de tentativas.

## Auditoria e exclusão

**Administração → Auditoria geral** consulta usuários, cursos, aulas e questões.
`GET /api/audit` aceita `entity`, `entityId`, `revision`, `from`, `to`, `actor`,
`role`, `page` e `size` (até 100). `from` e `to` são instantes ISO-8601, inclusivos.
Usuário e papel referem-se ao **autor da alteração**, e não ao titular do registro.
O resultado contém a revisão, operação (0 criação, 1 alteração, 2 exclusão),
instante, autor, papel e o snapshot. Senhas não fazem parte dos snapshots.

`revinfo` preserva os identificadores e a sequência existentes e passa a registrar
autor e papel. Revisões anteriores podem não ter esses campos. A auditoria das
entidades educacionais começa após esta atualização; não reconstrói alterações
anteriores. O histórico antigo de usuários continua disponível.

Excluir uma conta passa a limpar nome, idade, nascimento, senha e confirmação de
e-mail do registro principal e substitui seu e-mail por um identificador técnico
único. A conta permanece excluída logicamente para preservar vínculos. Revisões
de auditoria anteriores retêm os dados históricos e ficam restritas ao ADMIN.
Sessões e tokens são revogados. O endereço de e-mail original pode ser reutilizado
em outro cadastro. Essa regra técnica não estabelece prazo de retenção do histórico.

## Vídeos

A aula aceita links do YouTube ou vídeos enviados ao Cloudinary. Não há upload
automático para uma conta do YouTube: essa opção usa o link de um vídeo já publicado.
O upload Cloudinary usa `POST /api/courses/{courseId}/videos`, multipart `file`,
restrito ao responsável pelo curso e ADMIN. Aceita MP4, WebM e MOV até 100 MB.
As credenciais `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` e
`CLOUDINARY_API_SECRET` ficam apenas no backend. Sem configuração, o upload retorna
503, e os links do YouTube continuam disponíveis.

A integração usa a [API oficial de upload](https://cloudinary.com/documentation/upload_images)
por HTTPS, com autenticação no servidor. O frontend recebe apenas as URLs de vídeo
e miniatura. Depois do envio, é necessário salvar a aula para vinculá-la. Cancelar
a edição depois de enviar pode deixar um arquivo sem vínculo no Cloudinary; não
há rotina automática de remoção desses arquivos. O teste automatizado simula o
provedor e não publica arquivos reais.

## Aplicação e validação

Antes de iniciar esta versão em um banco existente, faça e confira um backup fora
do Git. O projeto ainda usa `ddl-auto=update`: serão adicionadas tabelas de
candidaturas, documentos, tentativas, auditorias educacionais, metadados de curso
e colunas de autor em `revinfo`. Interrompa instâncias antigas antes de atualizar.
Não foi aplicado esquema ao banco oficial nem realizada publicação em produção
durante a implementação.

Os testes de integração usam PostgreSQL/Redis isolados via Testcontainers. Para
atualizar os dois contratos a partir de um OpenAPI exportado pelos testes, execute
`npm run api:export` com `API_DOCS_FILE` apontando para o JSON completo; sem essa
variável, o comando consulta o backend local. `npm run api:generate` regenera os
clientes e validadores.

## Barras de progresso e conclusão

O catálogo e o painel do aluno exibem progresso por curso, considerando aulas
assistidas e questões aprovadas. A fórmula é `(aulas concluídas + questões
aprovadas) / (total de aulas + total de questões) × 100`, arredondada a uma casa
decimal. Um curso sem conteúdo fica em 0% e não é considerado concluído.

O professor vê inscritos ativos, alunos que concluíram, progresso médio da turma
e taxa de conclusão em **Cursos que criei**. Dentro do curso, vê a barra individual,
aulas concluídas, questões aprovadas e taxa de acerto de cada aluno, sem alternativas
marcadas. A taxa de conclusão representa alunos que já concluíram / inscritos
ativos × 100; não é uma previsão. O indicador de conclusão exige todas as aulas e
questões, sem depender do arredondamento do percentual. Contas e conteúdo excluídos
ficam fora dos cálculos.

`GET /api/learning/teaching/courses` retorna cursos do próprio professor, com
paginação e métricas agregadas. Os endpoints de matrículas e de alunos por curso
incluem `progress_percentage`, `completed`, `total_questions` e `passed_questions`.
As consultas do catálogo compartilham o resultado de matrículas; não há uma
requisição de progresso para cada cartão. A navegação livre dos vídeos continua.
