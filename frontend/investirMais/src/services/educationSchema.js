import { z } from "zod";
import { CreateCourseBody, CreateLessonBody, createCourseBodyNameMin, createCourseBodyNameMax, createCourseBodyDescriptionMax, createCourseBodyQuestionsMax } from "../generated/education.zod.ts";
import { questionSchema } from "./questionSchema.js";
import { youtubeVideo, cloudinaryVideo } from "./youtube.js";
export function educationSchema(lesson, currentMediaUrl) {
  return CreateCourseBody.pick({ description: true }).extend({
    title: z.string().trim().min(1, "Informe um título.").min(createCourseBodyNameMin, "Informe pelo menos 3 caracteres.").max(createCourseBodyNameMax),
    description: z.string().trim().min(1, "Informe uma descrição.").max(createCourseBodyDescriptionMax, "A descrição deve ter no máximo 10.000 caracteres."),
    mediaUrl: lesson ? CreateLessonBody.shape.mediaUrl.trim().min(1, "Informe a URL do vídeo.").refine(value => youtubeVideo(value) || cloudinaryVideo(value), "Informe um vídeo do YouTube ou Cloudinary.") : z.string().optional(),
    category: z.string().trim().max(100).default(""),
    keywords: z.string().trim().max(1000).default(""),
    questions: z.array(questionSchema).max(createCourseBodyQuestionsMax).default([]),
  });
}
