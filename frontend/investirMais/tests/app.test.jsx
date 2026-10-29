import { afterEach, beforeEach, expect, it, vi } from "vitest";
import { act, cleanup, fireEvent, render, screen, waitFor } from "@testing-library/react";
import { createMemoryRouter } from "react-router";
import { RouterProvider } from "react-router/dom";
import { routes } from "../src/routes";

const USER = { id: "u1", name: "Ana", email: "ana@example.com", role: "STUDENT" };
const COURSE = { id: "c1", name: "Primeiros investimentos", description: "Aprenda os fundamentos.", professorId: "u1" };
const LESSON = { id: "l1", courseId: "c1", title: "Renda fixa", description: "Conceitos", mediaUrl: "https://example.com/lesson", thumbnailUrl: "https://example.com/image.jpg" };
const json = (body, status = 200) => new Response(JSON.stringify(body), { status });
const empty = () => new Response(null, { status: 204 });
const fill = (label, value) => fireEvent.change(screen.getByLabelText(label), { target: { value } });
let routers = [];

function server(options = {}) {
  const state = { user: { ...USER }, profile: { ...USER, age: 25 }, courses: [{ ...COURSE }], lessons: [{ ...LESSON }], ...options };
  const fetcher = vi.fn(async (path, init) => {
    const method = init.method;
    const url = new URL(path, "http://localhost");
    const body = init.body instanceof URLSearchParams ? Object.fromEntries(init.body) : init.body ? JSON.parse(init.body) : {};
    document.cookie = "XSRF-TOKEN=csrf; path=/";
    if (path === "/auth/me") return state.user ? json(state.user) : new Response(null, { status: 401 });
    if (path === "/auth/login") return empty();
    if (path === "/auth/verify-2fa") {
      if (body.token === "wrong") return new Response(null, { status: 401 });
      state.user = { ...USER }; return empty();
    }
    if (path === "/auth/code") return empty();
    if (path === "/auth/logout") { state.user = null; return empty(); }
    if (path === "/api/users/register") return json({ ...body, id: "registered" }, 201);
    if (path === "/api/users/forgot-password" || path === "/api/users/resend-verification") return json({ message: "Confira seu e-mail." });
    if (path === "/api/users/reset-password") return json({ message: "Senha redefinida." });
    if (state.expireWrites && method === "PUT") { state.user = null; return new Response(null, { status: 401 }); }
    if (!state.user) return new Response(null, { status: 401 });
    if (path === "/api/users/update-password") {
      if (body.currentPassword === "wrong") return json({ detail: "Senha atual inválida." }, 401);
      state.user = null; return empty();
    }
    if (path === "/api/users/u1") {
      if (method === "GET") return json(state.profile);
      if (body.currentPassword === "wrong") return json({ detail: "Senha atual inválida." }, 401);
      const oldEmail = state.profile.email;
      state.profile = { ...state.profile, ...body };
      state.user = body.email !== oldEmail ? null : { ...state.user, name: body.name };
      return json(state.profile);
    }
    if (path.startsWith("/api/assets/")) return state.quoteError ? json({ detail: "Cotação indisponível." }, 503) : json({ results: [{ symbol: "PETR4", currency: "BRL", regularMarketPrice: 38.25, regularMarketChangePercent: 1.5 }] });
    const paged = (items) => {
      const page = Number(url.searchParams.get("page") || 0), size = Number(url.searchParams.get("size") || 10);
      return json({ content: items.slice(page * size, (page + 1) * size), page: { number: page, size, totalPages: Math.ceil(items.length / size), totalElements: items.length } });
    };
    if (url.pathname === "/api/courses") {
      if (method === "GET") return state.courseError ? json({ detail: "Catálogo indisponível." }, 503) : paged(state.courses);
      state.courses.push({ ...body, id: "c2", professorId: "u1" }); return json(state.courses.at(-1), 201);
    }
    if (url.pathname.startsWith("/api/courses/")) {
      const course = state.courses.find((item) => item.id === url.pathname.split("/").at(-1));
      if (!course) return json({}, 404);
      if (method === "PUT") Object.assign(course, body);
      if (method === "DELETE") { state.courses = state.courses.filter((item) => item !== course); return empty(); }
      return json(course);
    }
    if (url.pathname.startsWith("/api/lessons/course/")) {
      if (method === "GET") return paged(state.lessons.filter((lesson) => lesson.courseId === url.pathname.split("/").at(-1)));
      state.lessons.push({ ...body, id: "l2", courseId: "c1" }); return json(state.lessons.at(-1), 201);
    }
    if (url.pathname.startsWith("/api/lessons/")) {
      const lesson = state.lessons.find((item) => item.id === url.pathname.split("/").at(-1));
      if (!lesson) return json({}, 404);
      if (method === "PUT") Object.assign(lesson, body);
      if (method === "DELETE") { state.lessons = state.lessons.filter((item) => item !== lesson); return empty(); }
      return json(lesson);
    }
    throw new Error(`Unhandled test request: ${method} ${path}`);
  });
  vi.stubGlobal("fetch", fetcher);
  return { state, fetcher };
}
function app(entry = "/cursos", routeConfig = routes) {
  const router = createMemoryRouter(routeConfig, { initialEntries: [entry] });
  routers.push(router);
  render(<RouterProvider router={router} />);
  return router;
}
beforeEach(() => {
  HTMLDialogElement.prototype.showModal = function () { this.open = true; };
  HTMLDialogElement.prototype.close = function () { this.open = false; };
});
afterEach(() => { cleanup(); routers.forEach((router) => router.dispose()); routers = []; vi.unstubAllGlobals(); });

it("registers the backend DTO including age without confirmation fields", async () => {
  const { fetcher } = server({ user: null }); app("/register");
  await screen.findByText("Crie sua conta");
  fill("Nome completo", "Ana Silva"); fill("Idade", "25"); fill("E-mail", "ana@example.com"); fill("Senha", "secret123"); fill("Confirmar senha", "secret123");
  fireEvent.click(screen.getByRole("button", { name: "Criar conta" }));
  await screen.findByText(/Conta criada/);
  const call = fetcher.mock.calls.find(([path]) => path === "/api/users/register");
  expect(JSON.parse(call[1].body)).toEqual({ name: "Ana Silva", age: 25, email: "ana@example.com", password: "secret123" });
});
it("requires both factors and retains the verification step after an incorrect code", async () => {
  server({ user: null }); app("/login");
  await screen.findByText("Bem-vindo de volta"); fill("E-mail", USER.email); fill("Senha", "secret123");
  fireEvent.click(screen.getByRole("button", { name: "Continuar" }));
  await screen.findByText("Verificação em duas etapas");
  expect(screen.queryByRole("navigation", { name: "Menu principal" })).toBeNull();
  fill("Código de verificação", "wrong"); fireEvent.click(screen.getByRole("button", { name: "Confirmar acesso" }));
  await screen.findByRole("alert");
  expect(screen.getByLabelText("Código de verificação")).toBeTruthy();
  fill("Código de verificação", "right"); fireEvent.click(screen.getByRole("button", { name: "Confirmar acesso" }));
  await screen.findByRole("heading", { name: COURSE.name });
  expect(screen.queryByRole("link", { name: "Novo curso" })).toBeNull();
});
it("resends the second factor without requiring the code input", async () => {
  const { fetcher } = server({ user: null }); app("/login");
  await screen.findByText("Bem-vindo de volta"); fill("E-mail", USER.email); fill("Senha", "secret123");
  fireEvent.click(screen.getByRole("button", { name: "Continuar" })); await screen.findByText("Verificação em duas etapas");
  fireEvent.click(screen.getByRole("button", { name: "Reenviar código" }));
  await screen.findByText("Um novo código foi enviado ao seu e-mail.");
  expect(fetcher.mock.calls.some(([path]) => path === "/auth/code")).toBe(true);
});
it("keeps the course and pagination in the URL across reload and back navigation", async () => {
  server(); const router = app("/cursos/c1?page=0");
  await screen.findByRole("link", { name: /Abrir aula/ });
  await act(async () => router.revalidate());
  await screen.findByRole("link", { name: /Abrir aula/ });
  fireEvent.click(screen.getByRole("link", { name: "Todos os cursos" }));
  await screen.findByRole("heading", { name: "Cursos", exact: true });
  await act(async () => router.navigate(-1));
  await screen.findByRole("link", { name: /Abrir aula/ });
  expect(router.state.location.pathname).toBe("/cursos/c1");
});
it("uses URL pagination and preserves the requested page", async () => {
  server({ courses: Array.from({ length: 11 }, (_, index) => ({ ...COURSE, id: `c${index}`, name: `Curso ${index}` })) });
  const router = app(); await screen.findByRole("link", { name: "Próxima" });
  fireEvent.click(screen.getByRole("link", { name: "Próxima" }));
  await screen.findByRole("heading", { name: "Curso 10" });
  expect(router.state.location.search).toBe("?page=1");
});
it("creates courses and revalidates the list without a manual reload callback", async () => {
  const { state } = server({ user: { ...USER, role: "TEACHER" } }); app();
  fireEvent.click(await screen.findByRole("link", { name: "Novo curso" }));
  await screen.findByRole("dialog"); fill("Nome", "Novo investimento"); fill("Descrição", "Descrição do curso.");
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  await screen.findByRole("heading", { name: "Novo investimento" });
  expect(screen.queryByRole("dialog")).toBeNull(); expect(state.courses).toHaveLength(2);
});
it("edits lessons through a nested route and revalidates the course", async () => {
  const { state } = server({ user: { ...USER, role: "TEACHER" } }); const router = app("/cursos/c1/aulas/l1/editar");
  await screen.findByRole("dialog"); fill("Título", "Renda fixa atualizada");
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  await screen.findByRole("heading", { name: "Renda fixa atualizada" });
  await waitFor(() => expect(router.state.location.pathname).toBe("/cursos/c1"));
  expect(state.lessons[0].title).toBe("Renda fixa atualizada");
});
it("deletes courses and automatically revalidates the current list", async () => {
  const { state } = server({ user: { ...USER, role: "TEACHER" } }); vi.spyOn(window, "confirm").mockReturnValue(true); app();
  fireEvent.click(await screen.findByRole("button", { name: `Excluir ${COURSE.name}` }));
  await screen.findByText("Nenhum curso disponível no momento."); expect(state.courses).toHaveLength(0);
});
it("does not show edit controls or allow direct editor navigation for another teacher", async () => {
  server({ user: { ...USER, role: "TEACHER" }, courses: [{ ...COURSE, professorId: "other" }] }); const router = app();
  await screen.findByRole("heading", { name: COURSE.name });
  expect(screen.queryByRole("link", { name: `Editar ${COURSE.name}` })).toBeNull();
  await act(async () => router.navigate("/cursos/c1/editar"));
  expect((await screen.findByRole("alert")).textContent).toContain("permissão");
});
it("redirects an expired write session to login", async () => {
  const { state } = server(); app("/perfil");
  await screen.findByDisplayValue(USER.email); fill("Senha atual para confirmar", "correct"); state.expireWrites = true;
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  await screen.findByText("Bem-vindo de volta"); await screen.findByText("Sua sessão expirou. Entre novamente."); expect(screen.queryByRole("navigation", { name: "Menu principal" })).toBeNull();
});
it("retains a valid session when the current password is incorrect", async () => {
  server(); app("/perfil"); await screen.findByDisplayValue(USER.email); fill("Senha atual para confirmar", "wrong");
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  expect((await screen.findByRole("alert")).textContent).toBe("Senha atual inválida.");
  expect(screen.getByRole("navigation", { name: "Menu principal" })).toBeTruthy();
});
it("refreshes the profile and shared user data after changing the name", async () => {
  server(); app("/perfil"); await screen.findByDisplayValue(USER.email); fill("Nome completo", "Ana Atualizada"); fill("Senha atual para confirmar", "correct");
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  await screen.findByText("Perfil atualizado com sucesso."); await screen.findByText("Ana Atualizada");
});
it("leaves the session after changing the email", async () => {
  server(); app("/perfil"); await screen.findByDisplayValue(USER.email); fill("E-mail", "new@example.com"); fill("Senha atual para confirmar", "correct");
  fireEvent.click(screen.getByRole("button", { name: "Salvar alterações" }));
  await screen.findByText(/Confirme o novo endereço/); expect(screen.queryByRole("navigation", { name: "Menu principal" })).toBeNull();
});
it("leaves the session after changing the password", async () => {
  server(); app("/perfil"); await screen.findByDisplayValue(USER.email); fill("Senha atual", "correct123"); fill("Nova senha", "new12345"); fill("Confirmar nova senha", "new12345");
  fireEvent.click(screen.getByRole("button", { name: "Alterar senha" })); await screen.findByText("Senha alterada. Entre novamente com a nova senha.");
});
it("looks up quotes through Spring and keeps the ticker in the URL", async () => {
  const { fetcher } = server(); const router = app("/cotacoes");
  await screen.findByLabelText("Ticker do ativo"); fill("Ticker do ativo", "petr4"); fireEvent.click(screen.getByRole("button", { name: "Consultar" }));
  await screen.findByText(/38,25/); expect(router.state.location.search).toBe("?ticker=petr4");
  expect(fetcher.mock.calls.some(([path]) => path === "/api/assets/PETR4/quote")).toBe(true);
});
it("shows recoverable errors when loading the catalog fails", async () => {
  server({ courseError: true }); app();
  expect((await screen.findByRole("alert")).textContent).toBe("Catálogo indisponível.");
  expect(screen.getByRole("button", { name: "Tentar novamente" })).toBeTruthy();
});
it("catches a lazy module load failure instead of rendering a blank page", async () => {
  const { RouteError } = await import("../src/components/RouteError");
  vi.spyOn(console, "error").mockImplementation(() => {});
  app("/broken", [{ ErrorBoundary: RouteError, children: [{ path: "broken", lazy: () => Promise.reject(new Error("Loading chunk failed")) }] }]);
  await screen.findByText("Algo deu errado"); expect(screen.getByRole("button", { name: "Tentar novamente" })).toBeTruthy();
});
