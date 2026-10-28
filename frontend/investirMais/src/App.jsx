import { useLoaderData, Outlet, useNavigation } from "react-router";
import Sidebar from "./components/Sidebar";
import Topbar from "./components/Topbar";

export default function App() {
  const user = useLoaderData();
  const navigation = useNavigation();
  return <div className="min-h-dvh md:flex">
    <Sidebar />
    <div className="min-w-0 flex-1"><Topbar user={user} />
      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-8" aria-busy={navigation.state !== "idle"}>
        {navigation.state !== "idle" && <p role="status" className="mb-4 text-sm text-amber-400">Carregando…</p>}
        <Outlet />
      </main>
    </div>
  </div>;
}
