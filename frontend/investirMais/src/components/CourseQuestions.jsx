import { useState } from "react";
import { Box, Button, Card, Chip, FormControlLabel, Radio, RadioGroup, Stack, Typography } from "@mui/material";
import { useFetcher, useLocation } from "react-router";
import { Notice } from "./PageState.jsx";
function StudentQuestion({
  question
}) {
  const [selected, setSelected] = useState(null);
  const fetcher = useFetcher();
  const location = useLocation();
  const busy = fetcher.state !== "idle";
  const submit = event => {
    event.preventDefault();
    if (selected === null || busy) return;
    fetcher.submit({
      intent: "answer-question",
      id: question.id,
      payload: {
        optionIndex: selected
      }
    }, {
      method: "post",
      encType: "application/json",
      action: location.pathname + location.search
    });
  };
  return <Stack component="form" onSubmit={submit} spacing={2}>
    <Box component="fieldset" disabled={busy || question.locked || question.passed} sx={{
      border: 0,
      p: 0,
      m: 0,
      minWidth: 0
    }}><legend>{question.prompt}</legend>
      <RadioGroup value={selected ?? ""} onChange={(_, value) => setSelected(Number(value))}>{question.options.map((text, index) => <FormControlLabel key={index} value={index} control={<Radio />} label={text} />)}</RadioGroup>
    </Box>
    {question.passed ? <Notice>Pergunta concluída</Notice> : question.locked ? <Typography color="text.secondary">Conclua as aulas e acerte as perguntas anteriores para responder.</Typography> : <>
      {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}{fetcher.data?.passed === false && <Notice error>Ainda não é a alternativa correta. Escolha outra opção e tente novamente.</Notice>}
      <Button type="submit" variant="contained" disabled={busy || selected === null}>{busy ? "Verificando…" : "Responder pergunta"}</Button>
    </>}
  </Stack>;
}
export default function CourseQuestions({
  questions,
  canManage,
  onEdit,
  error,
  loading,
  onRetry,
  needsEnrollment,
  onEnroll
}) {
  if (!canManage && !questions.length && !error && !needsEnrollment) return null;
  return <Card component="section" id="perguntas" aria-labelledby="perguntas-titulo" sx={{
    p: 3
  }}><Stack spacing={3}>
    <Stack direction="row" sx={{
        justifyContent: "space-between",
        alignItems: "center",
        gap: 2
      }}><Typography variant="h2" id="perguntas-titulo">Perguntas do curso</Typography>{canManage && <Button onClick={() => onEdit({})}>Nova pergunta</Button>}</Stack>
    {error ? <><Notice error>{error}</Notice><Button disabled={loading} onClick={onRetry}>Tentar novamente</Button></> : needsEnrollment ? <><Typography>Entre no curso para responder às perguntas e liberar os próximos conteúdos.</Typography><Button variant="contained" disabled={loading} onClick={onEnroll}>Começar curso</Button></> : questions.length ? questions.map((question, index) => <Card component="article" key={question.id} aria-labelledby={`question-${question.id}`} sx={{
        p: 2
      }}><Stack spacing={2}>
        <Typography variant="overline">Pergunta {index + 1} · {question.afterLesson === 0 ? "Antes da primeira aula" : "Após a aula " + question.afterLesson}</Typography>
        <Typography variant="h3" id={`question-${question.id}`}>{question.prompt}</Typography>
        {canManage ? <><Stack component="ul" spacing={1} sx={{
              pl: 2
            }}>{question.options.map((text, option) => <Box component="li" key={option}>{text} {option === question.correctOptionIndex && <Chip label="Correta" color="success" size="small" />}</Box>)}</Stack>
          <Stack direction="row" spacing={1}><Button aria-label={"Editar pergunta " + question.prompt} onClick={() => onEdit({
                item: question
              })}>Editar</Button><Button color="error" aria-label={"Excluir pergunta " + question.prompt} onClick={() => onEdit({
                item: question,
                remove: true
              })}>Excluir</Button></Stack></> : <StudentQuestion question={question} />}
      </Stack></Card>) : <Typography>Adicione perguntas de múltipla escolha para os alunos praticarem durante o curso.</Typography>}
  </Stack></Card>;
}
