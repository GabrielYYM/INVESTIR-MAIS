import { render, screen, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";
import { course, teacher } from "../fixtures.js";
import { TeacherCourses } from "../../components/education/LearningDashboard.jsx";
import { MemoryRouter } from "react-router";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";

function renderTeacherCourses(userId) {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(<QueryClientProvider client={queryClient}><MemoryRouter><TeacherCourses userId={userId} /></MemoryRouter></QueryClientProvider>);
}

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

  it("mostra inscritos, progresso médio e taxa de conclusão no componente de cursos do professor", async () => {
    mockApi("get", "/api/learning/teaching/courses", { items: [{ ...course, enrolled_students: 3, completed_students: 1, average_progress: 50, completion_rate: 33.3 }], totalPages: 1 });
    renderTeacherCourses(teacher.id);
    const section = within(await screen.findByRole("region", { name: "Cursos que criei" }));
    expect(await section.findByText("Inscritos: 3 · Concluíram: 1")).toBeInTheDocument();
    expect(section.getByRole("progressbar", { name: "Progresso médio em " + course.name })).toHaveAttribute("aria-valuenow", "50");
    expect(section.getByText("33,3%")).toBeInTheDocument();
  });

  it("não exibe 'Meus cursos' nem 'Cursos que criei' na dashboard principal do professor", async () => {
    mockApi("get", "/api/teacher/dashboard", {
      startDate: "2026-05-13", endDate: "2026-11-13", totalCourses: 1, totalLessons: 2,
      totalStudents: 3, totalLessonsWatched: 4, averageCompletionRate: 66.7,
      courses: [{ courseId: course.id, courseName: course.name, totalLessons: 2, enrolledStudents: 3, lessonsWatched: 4, completionRate: 66.7 }],
    });
    renderApp("/", { session: teacher });
    expect(await screen.findByRole("heading", { name: /olá/i })).toBeInTheDocument();
    expect(screen.queryByRole("region", { name: "Cursos que criei" })).not.toBeInTheDocument();
    expect(screen.queryByRole("region", { name: "Meu aprendizado" })).not.toBeInTheDocument();
    expect(screen.queryByText("Meus cursos")).not.toBeInTheDocument();
  });

  it("mostra progresso individual e a proporção de alunos que concluíram", async () => {
    mockApi("get", `/api/learning/courses/${course.id}/students`, [
      { id: "ana", name: "Ana", progress_percentage: 100, completed: true, total_lessons: 1, completed_lessons: 1, total_questions: 1, passed_questions: 1, attempts: 2, accuracy: 50 },
      { id: "bia", name: "Bia", progress_percentage: 50, completed: false, total_lessons: 1, completed_lessons: 1, total_questions: 1, passed_questions: 0, attempts: 1, accuracy: 0 },
    ]);
    const { user } = renderApp("/educacao/" + course.id, { session: teacher });
    const section = within(await screen.findByRole("region", { name: "Desempenho do curso" }));
    expect((await section.findByText("Taxa de conclusão")).parentElement).toHaveTextContent("50%");
    expect(section.getByText("Alunos inscritos").parentElement).toHaveTextContent("2");
    expect(section.getByText("Alunos que concluíram").parentElement).toHaveTextContent("1");

    // Abre o modal de progresso individual
    await user.click(section.getByRole("button", { name: /ver progresso individual dos alunos/i }));
    const dialog = within(await screen.findByRole("dialog", { name: "Progresso individual dos alunos" }));
    expect(await dialog.findByRole("progressbar", { name: "Progresso de Ana" })).toHaveAttribute("aria-valuenow", "100");
    expect(dialog.getByRole("progressbar", { name: "Progresso de Bia" })).toHaveAttribute("aria-valuenow", "50");

    // Testa busca
    await user.type(dialog.getByPlaceholderText(/buscar/i), "Ana");
    expect(dialog.getByRole("progressbar", { name: "Progresso de Ana" })).toBeInTheDocument();
    expect(dialog.queryByRole("progressbar", { name: "Progresso de Bia" })).not.toBeInTheDocument();

    // Testa filtro por status (Em andamento)
    await user.clear(dialog.getByPlaceholderText(/buscar/i));
    await user.click(dialog.getByRole("button", { name: /em andamento/i }));
    expect(dialog.queryByRole("progressbar", { name: "Progresso de Ana" })).not.toBeInTheDocument();
    expect(dialog.getByRole("progressbar", { name: "Progresso de Bia" })).toBeInTheDocument();
  });

  it("mostra a taxa de acerto dentro de cada questão e mantém os dados de alunos no desempenho", async () => {
    mockApi("get", `/api/courses/${course.id}/questions/manage`, [
      { id: "q1", prompt: "Qual é a taxa Selic?", afterLesson: 1, position: 0, options: ["A", "B"], correctOptionIndex: 0 },
    ]);
    mockApi("get", `/api/learning/courses/${course.id}/questions`, [
      { id: "q1", prompt: "Qual é a taxa Selic?", attempts: 4, accuracy: 75 },
    ]);
    mockApi("get", `/api/learning/courses/${course.id}/students`, [
      { id: "ana", name: "Ana", progress_percentage: 100, completed: true, total_lessons: 1, completed_lessons: 1, total_questions: 1, passed_questions: 1, attempts: 2, accuracy: 50 },
    ]);
    renderApp("/educacao/" + course.id, { session: teacher });

    const questionsSection = within(await screen.findByRole("region", { name: "Perguntas da aula" }));
    expect(await questionsSection.findByText("75%")).toBeInTheDocument();
    expect(questionsSection.getByText("Taxa de acerto:")).toBeInTheDocument();
    expect(questionsSection.getByText(/4 respostas/)).toBeInTheDocument();

    const performanceSection = within(screen.getByRole("region", { name: "Desempenho do curso" }));
    expect(performanceSection.getByText("Alunos inscritos").parentElement).toHaveTextContent("1");
    expect(performanceSection.queryByText("Taxa de acerto por questão")).not.toBeInTheDocument();
  });
});
