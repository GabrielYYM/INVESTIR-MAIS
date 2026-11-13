import { useId } from "react";
import { Plus, Trash2 } from "lucide-react";
import { Field } from "./FormFields.jsx";

export function newQuestion(afterLesson = 0, position = 0) {
  return { prompt: "", afterLesson, position, options: ["", ""], correctOptionIndex: 0 };
}

export function questionPayload(value) {
  return { prompt: value.prompt.trim(), afterLesson: value.afterLesson, position: value.position,
    options: value.options.map((text) => text.trim()), correctOptionIndex: value.correctOptionIndex };
}

export function validateQuestion(value) {
  if (!value.prompt.trim()) return "Informe o enunciado da pergunta.";
  if (value.prompt.trim().length > 5000) return "O enunciado deve ter no máximo 5.000 caracteres.";
  if (!Number.isInteger(value.afterLesson) || value.afterLesson < 0 || value.afterLesson > 10000) return "Informe um número de aula entre 0 e 10.000.";
  if (!Number.isInteger(value.position) || value.position < 0 || value.position > 10000) return "Informe uma ordem entre 1 e 10.001.";
  if (value.options.length < 2 || value.options.length > 6) return "Cadastre de 2 a 6 alternativas.";
  if (value.options.some((text) => !text.trim() || text.trim().length > 2000)) return "Preencha todas as alternativas com até 2.000 caracteres.";
  if (new Set(value.options.map((text) => text.trim().toLocaleLowerCase("pt-BR"))).size !== value.options.length) return "As alternativas precisam ser diferentes.";
  if (!Number.isInteger(value.correctOptionIndex) || value.correctOptionIndex < 0 || value.correctOptionIndex >= value.options.length) return "Selecione a alternativa correta.";
  return null;
}

export default function QuestionFields({ value, onChange, error }) {
  const group = useId();
  const set = (field, next) => onChange({ ...value, [field]: next });
  const removeOption = (index) => {
    const correctOptionIndex = value.correctOptionIndex === index ? 0 : value.correctOptionIndex - (value.correctOptionIndex > index ? 1 : 0);
    onChange({ ...value, options: value.options.filter((_, i) => i !== index), correctOptionIndex });
  };
  return (
    <div className="space-y-4">
      {error && <p role="alert" className="text-sm text-negative">{error}</p>}
      <Field label="Enunciado" multiline rows={3} maxLength={5000} value={value.prompt}
        onChange={(event) => set("prompt", event.target.value)} required />
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Após a aula de número" type="number" min={0} max={10000} value={value.afterLesson}
          hint="Use 0 para perguntar antes da primeira aula."
          onChange={(event) => set("afterLesson", event.target.value === "" ? "" : Number(event.target.value))} />
        <Field label="Ordem neste ponto" type="number" min={1} max={10001} value={value.position === "" ? "" : value.position + 1}
          hint="Define a sequência de perguntas após a mesma aula."
          onChange={(event) => set("position", event.target.value === "" ? "" : Number(event.target.value) - 1)} />
      </div>
      <fieldset className="space-y-3">
        <legend className="mb-2 text-sm font-semibold">Alternativas</legend>
        <p className="text-xs text-muted">Preencha as opções e marque a alternativa correta.</p>
        {value.options.map((text, index) => (
          <div key={index} className="flex items-start gap-2 rounded-xl border border-line bg-canvas p-3">
            <label className="mt-8 flex shrink-0 items-center gap-1.5 text-xs text-muted">
              <input type="radio" name={group} checked={value.correctOptionIndex === index}
                onChange={() => set("correctOptionIndex", index)} aria-label={"Alternativa correta " + (index + 1)}
                className="size-4 accent-amber-500" />
              <span className="sr-only">Correta</span>
            </label>
            <div className="min-w-0 flex-1">
              <Field label={"Alternativa " + (index + 1)} value={text} maxLength={2000} required
                onChange={(event) => set("options", value.options.map((old, i) => i === index ? event.target.value : old))} />
            </div>
            {value.options.length > 2 && <button type="button" className="mt-7 rounded-lg p-2 text-muted hover:text-negative"
              aria-label={"Remover alternativa " + (index + 1)} onClick={() => removeOption(index)}><Trash2 size={16} /></button>}
          </div>
        ))}
        <button type="button" className="button-secondary" disabled={value.options.length >= 6}
          onClick={() => set("options", [...value.options, ""])}><Plus size={15} /> Adicionar alternativa</button>
      </fieldset>
    </div>
  );
}
