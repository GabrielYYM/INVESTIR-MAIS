import { Outlet, redirect, useFetcher, useLoaderData, useNavigation } from "react-router";
import { logout } from "../services/authService.js";
import { apiError } from "../services/api.js";
import Sidebar from "./Sidebar.jsx";
import Topbar from "./Topbar.jsx";
import { Notice } from "./PageState.jsx";

export async function signOut() {
  try {
    await logout();
  } catch (error) {
    if (error.response?.status !== 401) {
      return { error: apiError(error, "Não foi possível encerrar a sessão. Tente novamente.") };
    }
  }
  return redirect("/entrar");
}

export default function AppLayout() {
  const user = useLoaderData();
  const signOutForm = useFetcher();
  const navigation = useNavigation();

  return (
    <div className="min-h-dvh lg:grid lg:grid-cols-[15rem_minmax(0,1fr)]">
      <a href="#conteudo" className="sr-only focus:not-sr-only focus:fixed focus:z-30 focus:bg-brand focus:p-3 focus:text-canvas">Pular para o conteúdo</a>
      <Sidebar logout={signOutForm} user={user} />
      <div className="min-w-0">
        <Topbar user={user} />
        <div className={navigation.state === "loading" ? "h-0.5 animate-pulse bg-brand" : "h-0.5"} aria-hidden="true" />
        <main id="conteudo" className="mx-auto max-w-6xl space-y-8 px-4 py-6 sm:p-8">
          {signOutForm.data?.error && <Notice error>{signOutForm.data.error}</Notice>}
          <Outlet />
        </main>
      </div>
    </div>
  );
}
