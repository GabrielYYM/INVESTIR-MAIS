import { Button, IconButton, Radio, Stack } from "@mui/material";
import { Plus, Trash2 } from "lucide-react";
import { ArrayInput, NumberInput, SimpleFormIterator, TextInput, useSimpleFormIterator, useSimpleFormIteratorItem, useSourceContext } from "react-admin";
import { useFormContext, useWatch } from "react-hook-form";
function AddItem({
  source,
  label,
  max,
  defaults
}) {
  const {
    add
  } = useSimpleFormIterator();
  const values = useWatch({
    name: source
  }) ?? [];
  return <Button aria-label={label} startIcon={<Plus size={15} />} disabled={values.length >= max} onClick={() => add(typeof defaults === "function" ? defaults(values.length) : defaults)}>{label}</Button>;
}
function RemoveItem({
  label
}) {
  const {
    index,
    remove
  } = useSimpleFormIteratorItem();
  return label === "Remover alternativa" ? <IconButton color="error" aria-label={`${label} ${index + 1}`} onClick={() => remove()}><Trash2 size={16} /></IconButton> : <Button color="error" startIcon={<Trash2 size={15} />} aria-label={`${label} ${index + 1}`} onClick={() => remove()}>Remover pergunta {index + 1}</Button>;
}
function OptionFields({
  optionsSource
}) {
  const {
    index
  } = useSimpleFormIteratorItem();
  const {
    setValue
  } = useFormContext();
  const options = useWatch({
    name: optionsSource
  }) ?? [];
  return <div className="flex w-full min-w-0 items-start gap-2 rounded-xl border border-line bg-canvas p-3">
    <Radio sx={{ p: 0, mt: 4, "& svg": { fontSize: 16 } }} checked={Boolean(options[index]?.correct)} slotProps={{
      input: {
        "aria-label": `Alternativa correta ${index + 1}`
      }
    }} onChange={() => options.forEach((_, i) => setValue(`${optionsSource}.${i}.correct`, i === index, {
      shouldDirty: true
    }))} />
    <TextInput source="text" label={`Alternativa ${index + 1}`} fullWidth slotProps={{
      htmlInput: {
        maxLength: 2000
      }
    }} />
  </div>;
}
export default function QuestionFields() {
  const {
    getSource
  } = useSourceContext();
  const optionsSource = getSource("options");
  const options = useWatch({
    name: optionsSource
  }) ?? [];
  return <Stack spacing={2} sx={{
    width: "100%"
  }}>
    <TextInput source="prompt" label="Enunciado" multiline rows={3} fullWidth slotProps={{
      htmlInput: {
        maxLength: 5000
      }
    }} />
    <Stack direction={{
      xs: "column",
      sm: "row"
    }} spacing={2}>
      <NumberInput source="afterLesson" label="Após a aula de número" min={0} max={10000} helperText="Use 0 para perguntar antes da primeira aula." />
      <NumberInput source="position" label="Ordem neste ponto" min={1} max={10001} helperText="Define a sequência de perguntas após a mesma aula." />
    </Stack>
    <ArrayInput source="options" label="Alternativas" helperText={false}><SimpleFormIterator fullWidth disableReordering disableRemove={options.length <= 2} sx={{ "& > ul > li": { borderBottom: 0 }, "& > ul > li > .RaSimpleFormIterator-action": { mt: 4.5 } }} addButton={<AddItem source={optionsSource} label="Adicionar alternativa" max={6} defaults={{
        text: "",
        correct: false
      }} />} removeButton={<RemoveItem label="Remover alternativa" />}>
      <OptionFields optionsSource={optionsSource} />
    </SimpleFormIterator></ArrayInput>
  </Stack>;
}
function CourseQuestion() {
  const {
    index
  } = useSimpleFormIteratorItem();
  return <Stack component="fieldset" spacing={2} sx={{
    minWidth: 0,
    width: "100%",
    border: 1,
    borderColor: "divider",
    borderRadius: "12px",
    p: 2
  }}>
    <legend>Pergunta {index + 1}</legend><QuestionFields />
  </Stack>;
}
export function CourseQuestionFields({
  defaults
}) {
  return <section className="w-full space-y-4 border-t border-line pt-4" aria-label="Perguntas do novo curso"><p className="text-xs text-muted">Opcional: adicione agora ou depois de cadastrar as aulas.</p><ArrayInput source="questions" label="Perguntas do curso" helperText={false}><SimpleFormIterator fullWidth disableReordering sx={{ "& > ul > li": { flexDirection: "column", gap: 1, pb: 2, borderBottom: 0 } }} addButton={<AddItem source="questions" label="Adicionar pergunta" max={100} defaults={defaults} />} removeButton={<RemoveItem label="Remover pergunta" />}>
    <CourseQuestion />
  </SimpleFormIterator></ArrayInput></section>;
}
