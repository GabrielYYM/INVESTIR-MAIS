import { BookOpen, ChartNoAxesCombined, LogOut, User } from "lucide-react";
import { NavLink, useFetcher } from "react-router";
import { Notice } from "./ui";

const PAGES = [
  { to: "/cursos", label: "Cursos", icon: BookOpen },
  { to: "/cotacoes", label: "Cotações", icon: ChartNoAxesCombined },
  { to: "/perfil", label: "Meu perfil", icon: User },
];
export default function Sidebar() {
  const logout = useFetcher();
  return <aside className="border-b border-white/10 p-4 md:sticky md:top-0 md:flex md:h-dvh md:w-60 md:shrink-0 md:flex-col md:border-r md:border-b-0 md:p-6">
    <NavLink to="/cursos" className="mb-6 hidden text-sm font-bold tracking-wide md:block">INVESTIR <span className="text-amber-400">MAIS</span></NavLink>
    <nav aria-label="Menu principal" className="flex flex-wrap gap-2 md:flex-col">
      {PAGES.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} className={({ isActive }) => `flex items-center gap-3 rounded-xl px-3 py-3 text-sm transition ${isActive ? "bg-amber-400 font-semibold text-zinc-950" : "text-zinc-400 hover:bg-white/5 hover:text-white"}`}><Icon size={18} /><span>{label}</span></NavLink>)}
      <logout.Form method="post" action="/logout" className="ml-auto md:mt-8 md:ml-0"><button className="flex items-center gap-2 rounded-xl px-3 py-3 text-sm text-zinc-400 hover:bg-white/5" disabled={logout.state !== "idle"}><LogOut size={18} />{logout.state === "idle" ? "Sair" : "Saindo…"}</button></logout.Form>
    </nav>
    <Notice error={logout.data?.error} />
  </aside>;
}
