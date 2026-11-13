# Perguntas do curso

O professor cria, consulta, edita e exclui perguntas diretamente no curso, sem
rascunho ou publicação. Cada `Question` contém o enunciado, as alternativas e o
índice da alternativa correta. Não existem entidades `QuestionOption` ou `QuestionAnswer`.

As alternativas são uma lista de textos armazenada como array na própria pergunta.
Cada pergunta tem de 2 a 6 alternativas distintas. O índice começa em zero.
Exemplo de pergunta, também aceito no campo `questions` do cadastro de curso:

```json
{
  "prompt": "Qual destes é um título público federal?",
  "afterLesson": 1,
  "position": 0,
  "options": ["Tesouro Selic", "CDB"],
  "correctOptionIndex": 0
}
```

`afterLesson: 1` coloca a pergunta depois da aula de posição 1. Zero coloca a
pergunta antes da primeira aula. `position` ordena as perguntas no mesmo ponto.
As aulas recebem posições crescentes ao serem criadas. Uma pergunta pode referenciar
uma posição que o professor ainda vai cadastrar; ficará bloqueada até essa aula existir.

O aluno matriculado precisa concluir as aulas anteriores e acertar cada pergunta
para avançar. A consulta do aluno não revela o índice correto. Para responder:

```json
{"optionIndex": 0}
```

A resposta retorna apenas `questionId` e `passed`. Um erro não grava nada; o aluno
pode tentar novamente. Um acerto grava somente o par aluno/pergunta em
`tb_question_pass`, como uma coleção de IDs do usuário. Repetir um envio após passar
não cria outro registro. Não são armazenados alternativa marcada, erro, tentativas,
datas de resposta, histórico ou nota final. Não há rota `/result`.

Prefixo `/api/courses/{courseId}/questions`, com sessão autenticada e CSRF nas escritas:

| Método / sufixo | Uso |
| --- | --- |
| POST `/` | Criar pergunta (professor responsável ou ADMIN) |
| PUT `/{questionId}` | Editar pergunta (professor responsável ou ADMIN) |
| DELETE `/{questionId}` | Excluir logicamente pergunta (professor responsável ou ADMIN) |
| GET `/manage` | Consultar perguntas com o índice correto (responsável ou ADMIN) |
| GET `/` | Consultar perguntas sem gabarito, bloqueios e aprovações do próprio aluno |
| POST `/{questionId}/answers` | Validar uma alternativa e guardar apenas a aprovação |

O CRUD continua disponível depois que alunos começarem o curso. Editar uma pergunta
invalida as aprovações dela; os alunos precisam acertar o novo conteúdo. Excluir
uma pergunta remove seu bloqueio e sua participação no progresso. Cursos sem
perguntas mantêm a navegação anterior. Professores responsáveis e ADMIN podem
visualizar as aulas sem os bloqueios do aluno.

O backend bloqueia a conclusão e a consulta direta das aulas seguintes. Na listagem,
o conteúdo e a mídia das aulas bloqueadas são omitidos. O progresso considera aulas
concluídas e perguntas aprovadas, expondo `totalQuestions` e `passedQuestions`.
Perguntas após a última aula também precisam ser aprovadas para chegar a 100%.
O frontend permite adicionar perguntas no cadastro do curso e gerenciá-las na página
do curso. O professor edita alternativas, resposta correta, ponto da aula e ordem.
O aluno entra no curso, escolhe uma alternativa e recebe a confirmação de aprovação;
erros permitem tentar novamente. O progresso e as aulas liberadas são atualizados
após a resposta, sem recarregar manualmente. As aulas bloqueadas não oferecem links
de acesso e as perguntas pendentes exibem a orientação para avançar.

As perguntas usam soft delete em cascata com o curso. Aprovações de perguntas ou
contas excluídas ficam preservadas, mas não entram nas métricas ativas. Não há
restauração nem expurgo neste recurso. A exclusão de aula usada como ponto de uma
pergunta exige realocar ou remover a pergunta antes.

Não há arquivo SQL deste recurso. O projeto usa `ddl-auto=update` para a estrutura;
ele não migra nem remove tabelas antigas de implementações anteriores. Aulas antigas
recebem posição padrão zero e precisam de ordenação antes de perguntas intermediárias.
Nenhuma alteração foi aplicada ao banco oficial nesta implementação.
