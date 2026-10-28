import { useId, useState } from "react";
import { Eye, EyeOff } from "lucide-react";
import { Link, useSearchParams } from "react-router";

export function Field({ label, multiline, ...props }) {
  const id = useId();
  const Input = multiline ? "textarea" : "input";
  return <label htmlFor={id} className="grid gap-2 text-sm text-zinc-300">{label}<Input id={id} className="field" required {...props} /></label>;
}
export function PasswordField({ label, name, autoComplete = "new-password", minLength = 8 }) {
  const [visible, setVisible] = useState(false);
  return <div className="relative"><Field label={label} name={name} type={visible ? "text" : "password"} minLength={minLength} autoComplete={autoComplete} />
    <button type="button" onClick={() => setVisible(!visible)} aria-label={visible ? "Ocultar senha" : "Exibir senha"} className="absolute top-0 right-0 text-zinc-400">{visible ? <EyeOff size={18} /> : <Eye size={18} />}</button>
  </div>;
}
export function Notice({ error, children }) {
  if (!error && !children) return null;
  return <p role={error ? "alert" : "status"} className={`rounded-xl border px-4 py-3 text-sm ${error ? "border-red-400/25 bg-red-400/10 text-red-200" : "border-emerald-400/25 bg-emerald-400/10 text-emerald-200"}`}>{typeof error === "string" ? error : error?.message || children}</p>;
}
export function Submit({ pending, children = "Salvar alterações" }) {
  return <button className="btn" type="submit" disabled={pending}>{pending ? "Aguarde…" : children}</button>;
}
export function PageHeading({ title, children, action }) {
  return <div className="mb-7 flex flex-wrap items-center justify-between gap-4"><div><h1 className="text-3xl font-bold tracking-tight">{title}</h1>{children && <p className="mt-2 text-sm text-zinc-400">{children}</p>}</div>{action}</div>;
}
export function Pagination({ page }) {
  const [search] = useSearchParams();
  if (!page || page.totalPages < 2) return null;
  const query = (number) => { const next = new URLSearchParams(search); next.set("page", String(number)); return `?${next}`; };
  return <nav aria-label="Paginação" className="mt-6 flex items-center justify-center gap-4 text-sm">
    {page.number > 0 ? <Link className="btn-secondary" to={query(page.number - 1)}>Anterior</Link> : <span className="text-zinc-500">Anterior</span>}
    <span>{page.number + 1} de {page.totalPages}</span>
    {page.number + 1 < page.totalPages ? <Link className="btn-secondary" to={query(page.number + 1)}>Próxima</Link> : <span className="text-zinc-500">Próxima</span>}
  </nav>;
}
