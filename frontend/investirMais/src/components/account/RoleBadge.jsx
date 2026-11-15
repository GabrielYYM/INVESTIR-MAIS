import { roleNames } from "../../services/dataProvider.js";

const roleBadges = {
  STUDENT: "bg-sky-500/10 text-sky-400 border-sky-500/20",
  TEACHER: "bg-purple-500/10 text-purple-400 border-purple-500/20",
  ADMIN: "bg-amber-500/10 text-amber-400 border-amber-500/20",
};

export default function RoleBadge({ role, className = "" }) {
  const colorClasses = roleBadges[role] ?? "border-line bg-raised text-muted";
  return (
    <span className={`inline-flex rounded border px-2 py-0.5 text-xs font-medium ${colorClasses}${className ? ` ${className}` : ""}`}>
      {roleNames[role] ?? role}
    </span>
  );
}
