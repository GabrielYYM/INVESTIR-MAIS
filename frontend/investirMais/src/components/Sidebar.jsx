import { BookOpen, Home, LogOut, User } from "lucide-react";
import { Link, NavLink } from "react-router";

const links = [
  { to: "/", label: "Dashboard", icon: Home },
  { to: "/educacao", label: "Educação", icon: BookOpen },
  { to: "/perfil", label: "Meu perfil", icon: User },
];

export default function Sidebar({ logout }) {
  return (
    <aside className="sticky top-0 z-20 flex flex-col gap-4 border-b border-white/5 bg-canvas px-4 py-3 lg:h-dvh lg:border-r lg:border-b-0 lg:py-6">
      <div className="flex items-center justify-between lg:hidden">
        <Link to="/" className="font-bold tracking-wide">INVESTIR MAIS</Link>
        <logout.Form method="post" action="/sair">
          <button className="button-secondary !min-h-9 !px-3 !py-1.5" disabled={logout.state !== "idle"}><LogOut size={15} /> Sair</button>
        </logout.Form>
      </div>

      <nav aria-label="Menu principal" className="grid grid-cols-3 gap-1 lg:grid-cols-1">
        <p className="mb-2 hidden px-2 text-xs font-medium tracking-wide text-zinc-500 lg:block">MENU</p>
        {links.map(({ to, label, icon: Icon }) => (
          <NavLink key={to} to={to} end={to === "/"} className={({ isActive }) =>
            "flex items-center justify-center gap-2 rounded-xl px-2 py-2.5 text-xs transition-colors sm:text-sm lg:justify-start lg:gap-3 lg:px-3 " +
            (isActive ? "bg-brand font-semibold text-zinc-900" : "text-muted hover:bg-raised hover:text-zinc-200")
          }>
            <Icon size={17} className="shrink-0" /> {label}
          </NavLink>
        ))}
      </nav>

      <logout.Form method="post" action="/sair" className="mt-auto hidden lg:block">
        <button className="flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-muted hover:bg-raised hover:text-negative" disabled={logout.state !== "idle"}>
          <LogOut size={17} /> {logout.state === "idle" ? "Sair da conta" : "Saindo…"}
        </button>
      </logout.Form>
    </aside>
  );
}
