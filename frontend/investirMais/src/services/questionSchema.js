import { z } from "zod";
import { CreateBody, createBodyOptionsItemMax, createBodyOptionsMin, createBodyOptionsMax, createBodyPromptMax, createBodyAfterLessonMax, createBodyPositionMax } from "../generated/education.zod.ts";

export const questionSchema = CreateBody.pick({ prompt: true }).extend({
  prompt: z.string().trim().min(1, "Informe o enunciado da pergunta.").max(createBodyPromptMax, "O enunciado deve ter no máximo 5.000 caracteres."),
  afterLesson: z.number().int("Informe um número de aula entre 0 e 10.000.").min(0).max(createBodyAfterLessonMax),
  position: z.number().int("Informe uma ordem entre 1 e 10.001.").min(1).max(createBodyPositionMax + 1),
  options: z.array(z.object({ text: z.string().trim().min(1, "Preencha todas as alternativas com até 2.000 caracteres.").max(createBodyOptionsItemMax, "Preencha todas as alternativas com até 2.000 caracteres."), correct: z.boolean().default(false) }))
    .min(createBodyOptionsMin, "Cadastre de 2 a 6 alternativas.").max(createBodyOptionsMax, "Cadastre de 2 a 6 alternativas."),
}).superRefine(({ options }, ctx) => {
  if (new Set(options.map(({ text }) => text.toLocaleLowerCase("pt-BR"))).size !== options.length) ctx.addIssue({ code: "custom", path: ["options"], message: "As alternativas precisam ser diferentes." });
  if (options.filter(option => option.correct).length !== 1) ctx.addIssue({ code: "custom", path: ["options"], message: "Selecione a alternativa correta." });
});
export function questionDefaults(item, afterLesson = 0, position = 0) {
  return { prompt: item?.prompt ?? "", afterLesson: item?.afterLesson ?? afterLesson, position: (item?.position ?? position) + 1,
    options: (item?.options ?? ["", ""]).map((text, index) => ({ text, correct: index === (item?.correctOptionIndex ?? 0) })) };
}
export function questionPayload(value) {
  return { prompt: value.prompt, afterLesson: value.afterLesson, position: value.position - 1, options: value.options.map(({ text }) => text), correctOptionIndex: value.options.findIndex(option => option.correct) };
}
