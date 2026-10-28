import { redirect } from "react-router";
import App from "./App";
import { RouteError } from "./components/RouteError";
import { logout } from "./services/authService";
import { requireUser, routeAction } from "./services/routing";

const editor = (path, kind) => ({
  path,
  lazy: async () => {
    const module = await import("./pages/Editor");
    return { Component: module.Component, loader: module.loadEditor(kind), action: module.saveEditor(kind) };
  },
});

export const routes = [{
  ErrorBoundary: RouteError,
  HydrateFallback: () => <p role="status" className="p-10 text-center text-zinc-400">Carregando…</p>,
  children: [
    ...["login", "register", "forgot", "reset", "resend"].map((mode) => ({
      path: mode, id: mode,
      lazy: async () => {
        const module = await import("./pages/Auth");
        return { Component: module.Component, action: module.authAction(mode) };
      },
    })),
    { id: "session", loader: requireUser, Component: App, children: [
      { index: true, loader: () => redirect("/cursos") },
      { path: "cursos", lazy: () => import("./pages/Cursos"), children: [editor("novo", "course")] },
      { path: "cursos/:courseId", lazy: () => import("./pages/Aulas"), children: [
        editor("editar", "course"), editor("aulas/nova", "lesson"), editor("aulas/:lessonId/editar", "lesson"),
      ] },
      { path: "cotacoes", lazy: () => import("./pages/Cotacoes") },
      { path: "perfil", lazy: () => import("./pages/Perfil") },
      { path: "logout", action: routeAction(async () => { await logout(); return redirect("/login"); }), loader: () => redirect("/cursos") },
    ] },
    { path: "*", loader: () => { throw new Response("Página não encontrada.", { status: 404 }); } },
  ],
}];
