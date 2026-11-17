import { screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { course, teacher } from "../fixtures.js";

describe("progresso dos cursos", () => {
  it("mostra o mesmo progresso pessoal no painel e no catálogo", async () => {
    const request = mockApi("get", "/api/learning/courses", [{ ...course, progress_percentage: 75, completed: false, total_lessons: 3, completed_lessons: 3, total_questions: 1, passed_questions: 0 }]);
    const { user } = renderApp("/");
    expect(await screen.findByRole("progressbar", { name: "Progresso em " + course.name })).toHaveAttribute("aria-valuenow", "75");
    expect(screen.getByRole("progressbar", { name: "Seu progresso em " + course.name })).toHaveAttribute("aria-valuenow", "75");
    await user.click(screen.getByRole("link", { name: "Ver catálogo" }));
    expect(await screen.findByRole("progressbar", { name: "Seu progresso em " + course.name })).toHaveAttribute("aria-valuenow", "75");
    expect(request.mock.calls.every(([{ request }]) => !new URL(request.url).searchParams.has("studentId"))).toBe(true);
  });

  it("mostra 0% para curso ainda não iniciado", async () => {
    renderApp("/educacao");
    expect(await screen.findByRole("progressbar", { name: "Seu progresso em " + course.name })).toHaveAttribute("aria-valuenow", "0");
  });

  it("mostra inscritos, progresso médio e taxa de conclusão para o professor", async () => {
    mockApi("get", "/api/learning/teaching/courses", { items: [{ ...course, enrolled_students: 3, completed_students: 1, average_progress: 50, completion_rate: 33.3 }], totalPages: 1 });
    renderApp("/", { session: teacher });
    const section = within(await screen.findByRole("region", { name: "Cursos que criei" }));
    expect(await section.findByText("Inscritos: 3 · Concluíram: 1")).toBeInTheDocument();
    expect(section.getByRole("progressbar", { name: "Progresso médio em " + course.name })).toHaveAttribute("aria-valuenow", "50");
    expect(section.getByText("33,3%")).toBeInTheDocument();
  });

  it("mostra progresso individual e a proporção de alunos que concluíram", async () => {
    mockApi("get", `/api/learning/courses/${course.id}/students`, [
      { id: "ana", name: "Ana", progress_percentage: 100, completed: true, total_lessons: 1, completed_lessons: 1, total_questions: 1, passed_questions: 1, attempts: 2, accuracy: 50 },
      { id: "bia", name: "Bia", progress_percentage: 50, completed: false, total_lessons: 1, completed_lessons: 1, total_questions: 1, passed_questions: 0, attempts: 1, accuracy: 0 },
    ]);
    renderApp("/educacao/" + course.id, { session: teacher });
    const section = within(await screen.findByRole("region", { name: "Desempenho do curso" }));
    expect(await section.findByRole("progressbar", { name: "Progresso de Ana" })).toHaveAttribute("aria-valuenow", "100");
    expect(section.getByRole("progressbar", { name: "Progresso de Bia" })).toHaveAttribute("aria-valuenow", "50");
    expect(section.getByText("Taxa de conclusão").parentElement).toHaveTextContent("50%");
    expect(section.getByText("Alunos inscritos").parentElement).toHaveTextContent("2");
    expect(section.getByText("Alunos que concluíram").parentElement).toHaveTextContent("1");
  });
});
