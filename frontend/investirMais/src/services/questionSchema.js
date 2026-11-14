import { z } from "zod";

const integer = (min, max, message) => z.number({ error: message }).int(message).min(min, message).max(max, message);
export const questionSchema = z.object({
  prompt: z.string().trim().min(1, "Informe o enunciado da pergunta.").max(5000, "O enunciado deve ter no máximo 5.000 caracteres."),
  afterLesson: integer(0, 10000, "Informe um número de aula entre 0 e 10.000."),
  position: integer(1, 10001, "Informe uma ordem entre 1 e 10.001."),
  options: z.array(z.object({ text: z.string().trim().min(1, "Preencha todas as alternativas com até 2.000 caracteres.").max(2000, "Preencha todas as alternativas com até 2.000 caracteres.") }))
    .min(2, "Cadastre de 2 a 6 alternativas.").max(6, "Cadastre de 2 a 6 alternativas."),
  correctOptionIndex: integer(0, 5, "Selecione a alternativa correta."),
}).superRefine(({ options, correctOptionIndex }, ctx) => {
  if (new Set(options.map(({ text }) => text.toLocaleLowerCase("pt-BR"))).size !== options.length)
    ctx.addIssue({ code: "custom", path: ["options"], message: "As alternativas precisam ser diferentes." });
  if (correctOptionIndex >= options.length)
    ctx.addIssue({ code: "custom", path: ["correctOptionIndex"], message: "Selecione a alternativa correta." });
});

export function questionDefaults(item, afterLesson = 0, position = 0) {
  return { prompt: item?.prompt ?? "", afterLesson: item?.afterLesson ?? afterLesson,
    position: (item?.position ?? position) + 1, correctOptionIndex: item?.correctOptionIndex ?? 0,
    options: (item?.options ?? ["", ""]).map((text) => ({ text })) };
}

export function questionPayload(value) {
  return { ...value, position: value.position - 1, options: value.options.map(({ text }) => text) };
}

export function firstFormError(errors) {
  if (!errors || typeof errors !== "object") return;
  return errors.message || Object.entries(errors).filter(([key]) => key !== "ref")
    .map(([, value]) => firstFormError(value)).find(Boolean);
}
