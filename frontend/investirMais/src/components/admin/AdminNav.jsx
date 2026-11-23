import { NavLink } from "react-router";
import { Users, Flag, UserCheck, FileText } from "lucide-react";
import { useGetAdminPendingSummary } from "../../generated/account.ts";

export default function AdminNav() {
  const pending = useGetAdminPendingSummary();
  const summary = pending.data;

  const links = [
    { to: "/admin", label: "Usuários & Auditoria", icon: Users, end: true },
    { to: "/admin/denuncias", label: "Denúncias", icon: Flag, count: summary?.openReports },
    { to: "/admin/professores", label: "Candidaturas", icon: UserCheck, count: summary?.pendingTeacherApplications },
    { to: "/admin/documentos", label: "Documentos legais", icon: FileText },
  ];

  return (
    <nav aria-label="Navegação administrativa" className="flex flex-wrap items-center gap-2 border-b border-line pb-3">
      {links.map(({ to, label, icon: Icon, count, end }) => (
        <NavLink
          key={to}
          to={to}
          end={end}
          className={({ isActive }) =>
            "flex items-center gap-2 rounded-xl px-3.5 py-2 text-sm font-medium transition-colors " +
            (isActive
              ? "bg-brand font-semibold text-zinc-900 shadow-sm"
              : "border border-line bg-canvas text-ink hover:bg-raised")
          }
        >
          <Icon size={16} className="shrink-0" />
          <span>{label}</span>
          {typeof count === "number" && count > 0 && (
            <span
              className="ml-1 rounded-full bg-rose-500/20 px-2 py-0.5 text-xs font-bold text-rose-300"
              title={`${count} pendência(s)`}
            >
              {count}
            </span>
          )}
        </NavLink>
      ))}
    </nav>
  );
}
