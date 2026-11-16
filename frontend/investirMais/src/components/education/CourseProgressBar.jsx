export default function CourseProgressBar({ value = 0, label = "Progresso", completed = false }) {
  const percentage = Math.min(100, Math.max(0, Number(value) || 0));
  return <div className="space-y-2">
    <div className="flex items-center justify-between gap-3 text-sm"><span className="text-muted">{label}</span><strong className={completed ? "text-positive" : "text-brand"}>{percentage.toLocaleString("pt-BR")}%{completed ? " · Concluído" : ""}</strong></div>
    <div role="progressbar" aria-label={label} aria-valuemin={0} aria-valuemax={100} aria-valuenow={percentage} className="h-2.5 overflow-hidden rounded-full bg-white/10">
      <div className={"h-full rounded-full transition-all " + (completed ? "bg-emerald-400" : "bg-amber-400")} style={{ width: `${percentage}%` }} />
    </div>
  </div>;
}
