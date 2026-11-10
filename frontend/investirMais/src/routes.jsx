import { redirect } from "react-router";
import AppLayout, { signOut } from "./components/AppLayout.jsx";
import { PageError, PendingPage } from "./components/PageState.jsx";
import { getCurrentUser } from "./services/authService.js";
import Login, { LoginLink } from "./pages/Login.jsx";
import SignUp from "./pages/SignUp.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import VerifyEmail from "./pages/VerifyEmail.jsx";
import Perfil, { profileAction } from "./pages/Perfil.jsx";
import Home, { dashboardLoader } from "./pages/Home.jsx";
import Education, { catalogLoader } from "./pages/Education.jsx";
import Course, { courseLoader } from "./pages/Course.jsx";
import Admin, { adminLoader } from "./pages/Admin.jsx";
import Terms from "./pages/Terms.jsx";
import { catalogAction, courseAction } from "./services/educationData.js";

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

export const appRoutes = [
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
      { path: "educacao", Component: Education, loader: catalogLoader, action: catalogAction, ErrorBoundary: PageError },
      { path: "educacao/:courseId/:lessonId?", Component: Course, loader: courseLoader, action: courseAction, ErrorBoundary: PageError },
      { path: "perfil", Component: Perfil, action: profileAction },
      { path: "admin", Component: Admin, loader: adminLoader, ErrorBoundary: PageError },
      { path: "sair", action: signOut },
    ],
  },
  { path: "entrar", Component: Login },
  { path: "entrar/link", Component: LoginLink },
  { path: "cadastro", Component: SignUp },
  { path: "confirmar-email", Component: VerifyEmail },
  { path: "recuperar-senha", Component: ForgotPassword },
  { path: "termos", Component: Terms },
  { path: "termos-de-uso", Component: Terms },
  { path: "politica-de-privacidade", Component: Terms },
  { path: "*", Component: PageError },
];

