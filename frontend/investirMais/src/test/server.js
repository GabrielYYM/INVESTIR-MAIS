import { http, HttpResponse } from "msw/http";
import { setupServer } from "msw/node";
import { course, lesson, paged, student } from "./fixtures.js";

export const origin = "http://localhost:5173";
export const server = setupServer(
  http.get(origin + "/csrf", () => HttpResponse.json(
    { token: "csrf-test-token" },
    { headers: { "Set-Cookie": "XSRF-TOKEN=csrf-test-token; Path=/; SameSite=Lax" } },
  )),
  http.get(origin + "/auth/me", () => HttpResponse.json(student)),
<<<<<<< HEAD
  http.get(origin + "/api/teacher/dashboard", () => new HttpResponse(null, { status: 403 })),
  http.get(origin + "/api/courses/:courseId/progress", () => HttpResponse.json({
    enrolled: false, totalLessons: 1, completedLessons: 0, progressPercentage: 0, completedLessonIds: [],
  })),
=======
>>>>>>> origin/dev
  http.get(origin + "/api/courses", () => HttpResponse.json(paged([course]))),
  http.get(origin + "/api/courses/:courseId", () => HttpResponse.json(course)),
  http.get(origin + "/api/lessons/course/:courseId", () => HttpResponse.json(paged([lesson], { size: 10 }))),
  http.get(origin + "/api/lessons/:lessonId", () => HttpResponse.json(lesson)),
);
