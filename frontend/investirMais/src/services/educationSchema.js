import { z } from "zod";
import { questionSchema } from "./questionSchema.js";
import { youtubeVideo } from "./youtube.js";

export function educationSchema(lesson, currentMediaUrl) {
  return z.object({
    title: z.string().trim().min(1, "Informe um título.").min(3, "Informe pelo menos 3 caracteres.").max(100),
    description: z.string().trim().min(1, "Informe uma descrição.").max(10000, "A descrição deve ter no máximo 10.000 caracteres."),
    mediaUrl: lesson ? z.string().trim().min(1, "Informe a URL do vídeo.")
      .refine((value) => !value || youtubeVideo(value) || value === currentMediaUrl, "Informe o link de um vídeo do YouTube.") : z.string(),
    questions: z.array(questionSchema).max(100),
  });
}
