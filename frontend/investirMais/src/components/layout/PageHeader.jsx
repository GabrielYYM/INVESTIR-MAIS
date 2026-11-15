export default function PageHeader({
  eyebrow,
  title,
  description,
  actions,
  backLink,
  children,
}) {
  return (
    <header className="space-y-3">
      {backLink}
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="max-w-2xl min-w-0">
          {eyebrow && <p className="eyebrow">{eyebrow}</p>}
          <h1 className={`${eyebrow ? "mt-2 text-3xl" : "text-2xl sm:text-3xl"} font-bold break-words`}>{title}</h1>
          {description && (
            <p className={`${eyebrow ? "mt-2" : "mt-1"} text-sm leading-relaxed break-words whitespace-pre-wrap text-muted sm:text-base`}>
              {description}
            </p>
          )}
          {children}
        </div>
        {actions}
      </div>
    </header>
  );
}
