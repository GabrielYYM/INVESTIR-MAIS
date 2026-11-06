import { AlertCircle, ArrowLeft, LoaderCircle } from "lucide-react";
import { isRouteErrorResponse, Link, useRevalidator, useRouteError } from "react-router";

export function PendingPage() {
  return <div className="grid min-h-dvh place-items-center text-muted"><LoaderCircle className="animate-spin" aria-label="Carregando" /></div>;
}

export function Notice({ children, error = false }) {
  return (
    <div role={error ? "alert" : "status"} className={error ? "notice notice-error" : "notice"}>
      {error && <AlertCircle className="mt-0.5 size-4 shrink-0" />}
      <span>{children}</span>
    </div>
  );
}

export function PageError() {
  const error = useRouteError();
  const { revalidate, state } = useRevalidator();
  const status = isRouteErrorResponse(error) ? error.status : error?.response?.status;
  const missing = !error || status === 404;

  return (
    <section className="mx-auto max-w-lg px-5 py-16">
      <div className="panel space-y-4 p-7">
        <AlertCircle className="size-7 text-brand" />
        <h1 className="text-2xl font-bold">{missing ? "Página não encontrada" : "Não foi possível carregar esta página"}</h1>
        <p className="text-muted">{missing ? "O endereço pode ter mudado ou o conteúdo foi removido." : "Confira sua conexão e tente novamente."}</p>
        <div className="flex flex-wrap gap-3">
          <Link to="/" className="button-secondary"><ArrowLeft size={16} /> Voltar ao início</Link>
          {!missing && <button className="button-primary" disabled={state === "loading"} onClick={() => revalidate()}>Tentar novamente</button>}
        </div>
      </div>
    </section>
  );
}
