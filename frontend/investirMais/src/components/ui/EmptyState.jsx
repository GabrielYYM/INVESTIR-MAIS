export default function EmptyState({
  icon: Icon,
  title,
  description,
  action,
  panel = true,
}) {
  return (
    <div
      className={
        panel
          ? "panel flex flex-col items-center gap-2 p-8 text-center"
          : "flex flex-col items-center justify-center gap-3 rounded-xl border border-line bg-canvas p-8 text-center text-muted"
      }
    >
      {Icon && <Icon className={panel ? "mb-2 text-brand" : "text-brand opacity-60"} size={panel ? 25 : 28} />}
      <div>
        <h3 className="font-bold text-zinc-200">{title}</h3>
        {description && <p className="mt-1 text-sm text-muted">{description}</p>}
      </div>
      {action}
    </div>
  );
}
