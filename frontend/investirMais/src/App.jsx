import { createBrowserRouter, redirect } from "react-router";
import { RouterProvider } from "react-router/dom";
import AppLayout, { signOut } from "./components/AppLayout.jsx";
import { PageError, PendingPage } from "./components/PageState.jsx";
import { getCurrentUser } from "./services/authService.js";
import Login from "./pages/Login.jsx";
import SignUp from "./pages/SignUp.jsx";
import ForgotPassword from "./pages/ForgotPassword.jsx";
import Perfil from "./pages/Perfil.jsx";
import Home, { dashboardLoader, quoteLoader } from "./pages/Home.jsx";
import Education, { catalogLoader } from "./pages/Education.jsx";
import Course, { courseLoader } from "./pages/Course.jsx";

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

const router = createBrowserRouter([
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
      { path: "cotacao", loader: quoteLoader },
      { path: "educacao", Component: Education, loader: catalogLoader, ErrorBoundary: PageError },
      { path: "educacao/:courseId/:lessonId?", Component: Course, loader: courseLoader, ErrorBoundary: PageError },
      { path: "perfil", Component: Perfil },
      { path: "sair", action: signOut },
    ],
  },
  { path: "entrar", Component: Login },
  { path: "cadastro", Component: SignUp },
  { path: "recuperar-senha", Component: ForgotPassword },
  { path: "*", Component: PageError },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
