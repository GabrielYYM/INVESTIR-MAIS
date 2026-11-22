import { ProgressTrack } from "../education/CourseProgressBar.jsx";
import IconBadge from "./IconBadge.jsx";

export default function StatCard({
  label,
  value,
  icon: Icon,
  color = "text-ink",
  badgeClass,
  caption,
  progress,
}) {
  const isCompact = !caption && progress === undefined;

  if (isCompact) {
    return (
      <div className="panel flex items-center gap-3.5 px-4 py-3.5">
        {Icon && <IconBadge icon={Icon} className={badgeClass || `bg-white/5 ${color}`} />}
        <div className="min-w-0">
          <p className="truncate text-xs font-medium text-muted">{label}</p>
          <p className={`mt-0.5 text-xl font-bold leading-tight ${color}`}>{value}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="panel p-5">
      <div className="flex items-center justify-between text-muted">
        <span className="text-xs font-medium">{label}</span>
        {Icon && <Icon size={18} className={color} />}
      </div>
      <p className={`mt-2 text-2xl font-bold ${color}`}>{value}</p>
      {progress !== undefined ? (
        <div className="mt-2">
          <ProgressTrack value={progress} />
        </div>
      ) : (
        caption && <span className="text-xs text-muted">{caption}</span>
      )}
    </div>
  );
}
