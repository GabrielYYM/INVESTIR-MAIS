import { UserRound } from "lucide-react";
import { Link } from "react-router";

export const roleNames = { STUDENT: "Aluno", TEACHER: "Professor", ADMIN: "Administrador" };

export default function Topbar({ user }) {
  return (
    <header className="hidden items-center justify-between gap-4 px-8 py-5 lg:flex">
      <Link to="/" className="font-bold tracking-wide">INVESTIR MAIS</Link>
      <Link to="/perfil" className="flex min-w-0 items-center gap-2.5 rounded-lg" aria-label="Abrir meu perfil">
        <span className="hidden min-w-0 text-right sm:block">
          <span className="block max-w-48 truncate text-sm font-semibold">{user.name}</span>
          <span className="block text-xs text-muted">{roleNames[user.role]}</span>
        </span>
        <span className="grid size-9 shrink-0 place-items-center rounded-full border border-brand/25 bg-brand/10 text-brand"><UserRound size={17} /></span>
      </Link>
    </header>
  );
}
