# Perguntas do curso

O professor responsável ou o administrador cria, consulta, edita e exclui perguntas dentro de cada aula, sem
rascunho ou publicação. Cada `Question` contém o enunciado, as alternativas e o
índice da alternativa correta. Não existem entidades `QuestionOption` ou `QuestionAnswer`.

As alternativas são uma lista de textos armazenada como array na própria pergunta.
Cada pergunta tem de 2 a 6 alternativas distintas. O índice começa em zero.
Exemplo de pergunta (o campo legado `questions` do cadastro de curso permanece compatível na API):

```json
{
  "prompt": "Qual destes é um título público federal?",
  "afterLesson": 1,
  "position": 0,
  "options": ["Tesouro Selic", "CDB"],
  "correctOptionIndex": 0
}
```

`afterLesson: 1` coloca a pergunta depois da aula de posição 1. Questões legadas com zero são exibidas dentro da primeira aula. `position` ordena as perguntas no mesmo ponto.
As aulas recebem posições crescentes ao serem criadas. Ao criar ou editar uma questão individual, posições positivas precisam corresponder a uma aula existente.

O aluno matriculado continua seguindo os pré-requisitos para responder às questões. Os vídeos podem ser assistidos em qualquer ordem, sem depender de respostas ou da conclusão das aulas anteriores. A consulta do aluno não revela o índice correto. Para responder:

```json
{"optionIndex": 0}
```

A resposta retorna `questionId` e `passed`. Toda nova tentativa válida grava em
`question_attempt` o enunciado, alternativa escolhida, resultado, curso, aluno e
instante. O próprio aluno consulta suas respostas no painel. O professor recebe
somente métricas de acerto, sem alternativas individuais, e não pode responder
às questões do próprio curso. Um acerto também grava o par aluno/pergunta em
`tb_question_pass`, que continua controlando a progressão. Reenviar após obter
aprovação não gera outra tentativa. Respostas antigas não são reconstruídas.

Prefixo `/api/courses/{courseId}/questions`, com sessão autenticada e CSRF nas escritas:

| Método / sufixo | Uso |
| --- | --- |
| POST `/` | Criar pergunta (professor responsável ou ADMIN) |
| PUT `/{questionId}` | Editar pergunta (professor responsável ou ADMIN) |
| DELETE `/{questionId}` | Excluir logicamente pergunta (professor responsável ou ADMIN) |
| GET `/manage` | Consultar perguntas com o índice correto (responsável ou ADMIN) |
| GET `/` | Consultar perguntas sem gabarito, bloqueios e aprovações do próprio aluno |
| POST `/{questionId}/answers` | Validar e registrar uma tentativa privada |

O CRUD continua disponível depois que alunos começarem o curso. Editar uma pergunta
invalida as aprovações dela; os alunos precisam acertar o novo conteúdo. Excluir
uma pergunta remove seu bloqueio e sua participação no progresso. Todos os usuários autenticados podem visualizar os vídeos em qualquer ordem.

O backend permite consultar e marcar qualquer videoaula como assistida, independentemente
das questões. A listagem inclui a mídia de todas as aulas disponíveis. O progresso
considera aulas concluídas e perguntas aprovadas, expondo `totalQuestions` e
`passedQuestions`; assistir a todos os vídeos não substitui responder às questões
para completar o curso em 100%.

O frontend mantém as questões dentro da aula correspondente. O professor edita
alternativas, resposta correta, ponto da aula e ordem. O aluno escolhe uma
alternativa e recebe a confirmação de aprovação; erros permitem tentar novamente.
As questões e suas regras de resposta continuam presentes. Editar uma questão
invalida sua aprovação, preservando o registro dos vídeos já assistidos.

As perguntas usam soft delete em cascata com o curso. Aprovações de perguntas ou
contas excluídas ficam preservadas, mas não entram nas métricas ativas. Não há
restauração nem expurgo neste recurso. A exclusão de aula usada como ponto de uma
pergunta exige realocar ou remover a pergunta antes.

## Ordenação das aulas

O professor responsável e o ADMIN podem consultar e alterar a sequência em
`GET/PUT /api/lessons/course/{courseId}/order`. A consulta retorna a lista completa
com `id`, `title` e `position`. Para salvar, envie todos os IDs das aulas ativas,
uma única vez cada, na ordem desejada:

```json
{"lessonIds": ["UUID_DA_SEGUNDA_AULA", "UUID_DA_PRIMEIRA_AULA"]}
```

Substitua os valores ilustrativos por UUIDs reais do curso. As posições persistidas
começam em 1. A API rejeita IDs repetidos e uma lista que não corresponda às aulas
ativas do curso; se a lista mudou durante a edição, retorna `409` para atualização
da página. A alteração usa transação e bloqueios do curso e das aulas. As perguntas
acompanham a aula à qual estavam vinculadas: `afterLesson` é remapeado para a nova
posição. Perguntas com `afterLesson: 0` continuam antes da primeira aula.

## Inicialização de aulas legadas

O Hibernate mantém a estrutura com `ddl-auto=update`. Depois da inicialização do
JPA, `LessonOrderInitialization` executa o script idempotente
[`lesson-order-postgresql.sql`](../src/main/resources/lesson-order-postgresql.sql).
Somente cursos com alguma aula ativa de posição menor ou igual a zero recebem o
preenchimento: a sequência considera posição anterior, título e ID. As aulas já
posicionadas preservam a ordem relativa; os vínculos das perguntas com posições
positivas são ajustados junto com as aulas. Cursos já ordenados não são alterados.

Antes de iniciar uma atualização de esquema, faça backup do banco de destino.
Testes automatizados usam bancos isolados; verificações manuais locais usam o
banco oficial `investir_mais`, preservando os registros existentes.
