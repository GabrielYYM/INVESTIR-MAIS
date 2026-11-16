import { http, HttpResponse } from "msw/http";
import { setupServer } from "msw/node";
import { course, legalRelease, lesson, paged, student } from "./fixtures.js";

export const origin = "http://localhost:5173";
export const server = setupServer(
  http.get(origin + "/api/learning/teaching/courses", () => HttpResponse.json({items:[],totalItems:0,totalPages:0,page:0})),
  http.get(origin + "/api/learning/courses", () => HttpResponse.json([])),
  http.get(origin + "/api/learning/answers", () => HttpResponse.json(paged([]))),
  http.get(origin + "/api/learning/courses/:id/students", () => HttpResponse.json([])),
  http.get(origin + "/api/learning/courses/:id/questions", () => HttpResponse.json([])),
  http.get(origin + "/api/courses/professor/:id", () => HttpResponse.json(paged([]))),
  http.get(origin + "/csrf", () => HttpResponse.json(
    { token: "csrf-test-token" },
    { headers: { "Set-Cookie": "XSRF-TOKEN=csrf-test-token; Path=/; SameSite=Lax" } },
  )),
  http.get(origin + "/auth/me", () => HttpResponse.json(student)),
  http.get(origin + "/api/legal/current", () => HttpResponse.json(legalRelease)),
  http.get(origin + "/api/legal/versions/:version", () => HttpResponse.json(legalRelease)),
  http.get(origin + "/api/legal/status", () => HttpResponse.json({ currentVersion: 1, acceptanceRequired: false, acceptedAt: "2026-11-14T12:00:00Z" })),
  http.get(origin + "/api/legal/acceptances", () => HttpResponse.json([{ version: 1, acceptedAt: "2026-11-14T12:00:00Z" }])),
  http.get(origin + "/api/teacher/dashboard", () => new HttpResponse(null, { status: 403 })),
  http.get(origin + "/api/courses/:courseId/progress", () => HttpResponse.json({
    enrolled: false, totalLessons: 1, completedLessons: 0, progressPercentage: 0, completedLessonIds: [],
  })),
  http.get(origin + "/api/courses", () => HttpResponse.json(paged([course]))),
  http.get(origin + "/api/courses/:courseId", () => HttpResponse.json(course)),
  http.get(origin + "/api/courses/:courseId/questions/manage", () => HttpResponse.json([])),
  http.get(origin + "/api/courses/:courseId/questions", () => HttpResponse.json([])),
  http.post(origin + "/api/lessons/:courseId/:lessonId/progress", () => HttpResponse.json({
    enrolled: true, totalLessons: 1, completedLessons: 1, progressPercentage: 100, completedLessonIds: [lesson.id],
  })),
  http.get(origin + "/api/lessons/course/:courseId", () => HttpResponse.json(paged([lesson], { size: 10 }))),
  http.get(origin + "/api/lessons/:lessonId", () => HttpResponse.json(lesson)),
);
