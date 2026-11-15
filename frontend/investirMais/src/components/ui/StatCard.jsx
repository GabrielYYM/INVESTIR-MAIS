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
        <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-white/10">
          <div
            className="h-full rounded-full bg-emerald-400 transition-all duration-500"
            style={{ width: `${Math.min(100, progress)}%` }}
          />
        </div>
      ) : (
        caption && <span className="text-xs text-muted">{caption}</span>
      )}
    </div>
  );
}
