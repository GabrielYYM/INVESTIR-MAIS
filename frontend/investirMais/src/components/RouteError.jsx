import { Link, isRouteErrorResponse, useRouteError } from "react-router";

export function RouteError() {
  const error = useRouteError();
  const missing = isRouteErrorResponse(error) && error.status === 404;
  const message = error?.name === "ApiError" ? error.message : "Não foi possível carregar esta página. Tente novamente.";
  return <main className="mx-auto max-w-lg px-4 py-16"><section className="panel grid gap-5">
    <h1 className="text-2xl font-bold">{missing ? "Página não encontrada" : "Algo deu errado"}</h1>
    <p role="alert" className="text-zinc-300">{missing ? "Confira o endereço ou volte para os cursos." : message}</p>
    <div className="flex flex-wrap gap-3"><button className="btn" onClick={() => window.location.reload()}>Tentar novamente</button><Link className="btn-secondary" to="/cursos">Voltar aos cursos</Link></div>
  </section></main>;
}
