import { UserRound } from "lucide-react";
import { Link } from "react-router";

const ROLES = { STUDENT: "Aluno", TEACHER: "Professor", ADMIN: "Administrador" };
export default function Topbar({ user }) {
  return <header className="flex items-center justify-end gap-3 border-b border-white/5 px-4 py-5 sm:px-8">
    <div className="text-right"><p className="text-sm font-medium">{user.name}</p><p className="text-xs text-zinc-400">{ROLES[user.role] || user.role}</p></div>
    <Link to="/perfil" aria-label="Abrir meu perfil" className="rounded-full bg-amber-400/10 p-2.5 text-amber-400"><UserRound size={20} /></Link>
  </header>;
}
