import { Button, FormControlLabel, Radio, Stack } from "@mui/material";
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
  return <Button disabled={values.length >= max} onClick={() => add(typeof defaults === "function" ? defaults(values.length) : defaults)}>{label}</Button>;
}
function RemoveItem({
  label
}) {
  const {
    index,
    remove
  } = useSimpleFormIteratorItem();
  return <Button color="error" aria-label={`${label} ${index + 1}`} onClick={() => remove()}>Remover</Button>;
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
  return <Stack direction="row" spacing={1} sx={{
    alignItems: "center",
    width: "100%"
  }}>
    <FormControlLabel label="Correta" control={<Radio checked={Boolean(options[index]?.correct)} slotProps={{
      input: {
        "aria-label": `Alternativa correta ${index + 1}`
      }
    }} onChange={() => options.forEach((_, i) => setValue(`${optionsSource}.${i}.correct`, i === index, {
      shouldDirty: true
    }))} />} />
    <TextInput source="text" label={`Alternativa ${index + 1}`} fullWidth slotProps={{
      htmlInput: {
        maxLength: 2000
      }
    }} />
  </Stack>;
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
    <ArrayInput source="options" label="Alternativas"><SimpleFormIterator disableReordering disableRemove={options.length <= 2} addButton={<AddItem source={optionsSource} label="Adicionar alternativa" max={6} defaults={{
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
    borderRadius: 2,
    p: 2
  }}>
    <legend>Pergunta {index + 1}</legend><QuestionFields />
  </Stack>;
}
export function CourseQuestionFields({
  defaults
}) {
  return <ArrayInput source="questions" label="Perguntas do curso"><SimpleFormIterator disableReordering addButton={<AddItem source="questions" label="Adicionar pergunta" max={100} defaults={defaults} />} removeButton={<RemoveItem label="Remover pergunta" />}>
    <CourseQuestion />
  </SimpleFormIterator></ArrayInput>;
}
