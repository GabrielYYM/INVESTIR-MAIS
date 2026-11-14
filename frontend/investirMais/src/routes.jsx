import ApplicationProviders from "./providers/ApplicationProviders.jsx";
import { redirect } from "react-router";
import AppLayout, { signOut } from "./components/layout/AppLayout.jsx";
import { PageError, PendingPage } from "./components/ui/PageState.jsx";
import { getCurrentUser } from "./services/authService.js";
import Login, { LoginLink } from "./pages/Login.jsx";
import SignUp from "./pages/SignUp.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import VerifyEmail from "./pages/VerifyEmail.jsx";
import Perfil from "./pages/Perfil.jsx";
import Home, { dashboardLoader } from "./pages/Home.jsx";
import Education, { catalogLoader } from "./pages/Education.jsx";
import Course, { courseLoader } from "./pages/Course.jsx";
import { courseAction } from "./services/educationData.js";

async function requireSession({ request }) {
  try {
    return await getCurrentUser(request.signal);
  } catch (error) {
    if ([401, 403].includes(error.response?.status)) {
      const url = new URL(request.url);
      const next = new URLSearchParams({ retorno: url.pathname + url.search });
      throw redirect("/entrar?" + next);
    }
    throw error;
  }
}

const termsRoute = async () => ({ Component: (await import("./pages/Terms.jsx")).default });

export const appRoutes = [{ Component: ApplicationProviders, children: [
  {
    id: "app",
    Component: AppLayout,
    loader: requireSession,
    shouldRevalidate: ({ currentUrl, nextUrl, defaultShouldRevalidate }) =>
      currentUrl.pathname !== nextUrl.pathname || defaultShouldRevalidate,
    HydrateFallback: PendingPage,
    ErrorBoundary: PageError,
    children: [
      { index: true, Component: Home, loader: dashboardLoader, ErrorBoundary: PageError },
      { path: "educacao", Component: Education, loader: catalogLoader, ErrorBoundary: PageError },
      { path: "educacao/:courseId/:lessonId?", Component: Course, loader: courseLoader, action: courseAction, ErrorBoundary: PageError },
      { path: "perfil", Component: Perfil },
      { path: "admin", lazy: async () => {
        const { default: Component, adminLoader: loader } = await import("./pages/Admin.jsx");
        return { Component, loader };
      }, ErrorBoundary: PageError },
      { path: "sair", action: signOut },
    ],
  },
  { path: "entrar", Component: Login },
  { path: "entrar/link", Component: LoginLink },
  { path: "cadastro", Component: SignUp },
  { path: "confirmar-email", Component: VerifyEmail },
  { path: "recuperar-senha", Component: ForgotPassword },
  { path: "termos", lazy: termsRoute, HydrateFallback: PendingPage },
  { path: "termos-de-uso", lazy: termsRoute, HydrateFallback: PendingPage },
  { path: "politica-de-privacidade", lazy: termsRoute, HydrateFallback: PendingPage },
  { path: "*", Component: PageError },
]}];

