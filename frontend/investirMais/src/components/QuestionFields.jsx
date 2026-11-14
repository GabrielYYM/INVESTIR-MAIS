import { useId } from "react";
import { useFieldArray, useWatch } from "react-hook-form";
import { Plus, Trash2 } from "lucide-react";
import { Field } from "./FormFields.jsx";
import { firstFormError } from "../services/questionSchema.js";

export default function QuestionFields({ form, name = "" }) {
  const group = useId();
  const field = (key) => name ? `${name}.${key}` : key;
  const { fields, append, remove } = useFieldArray({ control: form.control, name: field("options") });
  const correct = useWatch({ control: form.control, name: field("correctOptionIndex") });
  const errors = name ? form.formState.errors.questions?.[Number(name.split(".").at(-1))] : form.formState.errors;
  const error = firstFormError(errors);
  const removeOption = (index) => {
    form.setValue(field("correctOptionIndex"), correct === index ? 0 : correct - Number(correct > index));
    remove(index);
  };
  return (
    <div className="space-y-4">
      {error && <p role="alert" className="text-sm text-negative">{error}</p>}
      <Field label="Enunciado" multiline rows={3} maxLength={5000} required {...form.register(field("prompt"))} />
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Após a aula de número" type="number" min={0} max={10000}
          hint="Use 0 para perguntar antes da primeira aula." {...form.register(field("afterLesson"), { valueAsNumber: true })} />
        <Field label="Ordem neste ponto" type="number" min={1} max={10001}
          hint="Define a sequência de perguntas após a mesma aula." {...form.register(field("position"), { valueAsNumber: true })} />
      </div>
      <fieldset className="space-y-3">
        <legend className="mb-2 text-sm font-semibold">Alternativas</legend>
        <p className="text-xs text-muted">Preencha as opções e marque a alternativa correta.</p>
        {fields.map((option, index) => (
          <div key={option.id} className="flex items-start gap-2 rounded-xl border border-line bg-canvas p-3">
            <label className="mt-8 flex shrink-0 items-center gap-1.5 text-xs text-muted">
              <input type="radio" name={group} checked={correct === index}
                onChange={() => form.setValue(field("correctOptionIndex"), index, { shouldDirty: true })}
                aria-label={"Alternativa correta " + (index + 1)} className="size-4 accent-amber-500" />
              <span className="sr-only">Correta</span>
            </label>
            <div className="min-w-0 flex-1">
              <Field label={"Alternativa " + (index + 1)} maxLength={2000} required {...form.register(field(`options.${index}.text`))} />
            </div>
            {fields.length > 2 && <button type="button" className="mt-7 rounded-lg p-2 text-muted hover:text-negative"
              aria-label={"Remover alternativa " + (index + 1)} onClick={() => removeOption(index)}><Trash2 size={16} /></button>}
          </div>
        ))}
        <button type="button" className="button-secondary" disabled={fields.length >= 6}
          onClick={() => append({ text: "" })}><Plus size={15} /> Adicionar alternativa</button>
      </fieldset>
    </div>
  );
}
