export function ProgressTrack({ value = 0, completed = true, size = "sm", label }) {
  const percentage = Math.min(100, Math.max(0, Number(value) || 0));
  const height = size === "md" ? "h-2.5" : "h-1.5";
  const color = completed ? "bg-emerald-400" : "bg-amber-400";
  return (
    <div
      role={label ? "progressbar" : undefined}
      aria-label={label}
      aria-valuemin={label ? 0 : undefined}
      aria-valuemax={label ? 100 : undefined}
      aria-valuenow={label ? percentage : undefined}
      className={`${height} w-full overflow-hidden rounded-full bg-white/10`}
    >
      <div className={`h-full rounded-full transition-all duration-500 ${color}`} style={{ width: `${percentage}%` }} />
    </div>
  );
}

export default function CourseProgressBar({ value = 0, label = "Progresso", completed = false }) {
  const percentage = Math.min(100, Math.max(0, Number(value) || 0));
  return <div className="space-y-2">
    <div className="flex items-center justify-between gap-3 text-sm"><span className="text-muted">{label}</span><strong className={completed ? "text-positive" : "text-brand"}>{percentage.toLocaleString("pt-BR")}%{completed ? " · Concluído" : ""}</strong></div>
    <ProgressTrack value={percentage} completed={completed} size="md" label={label} />
  </div>;
}
