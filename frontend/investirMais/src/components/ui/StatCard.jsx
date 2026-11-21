import { ProgressTrack } from "../education/CourseProgressBar.jsx";

export default function StatCard({
  label,
  value,
  icon: Icon,
  color = "text-ink",
  caption,
  progress,
}) {
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
