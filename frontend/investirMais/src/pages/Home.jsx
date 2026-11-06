import { ArrowRight, Award, BookOpen, Search, TrendingDown, TrendingUp } from "lucide-react";
import { Link, redirect, useFetcher, useLoaderData, useRouteLoaderData } from "react-router";
import { CourseCard } from "../components/EducationParts.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { dashboardCourses } from "../services/educationData.js";
import { buscarCotacao } from "../services/quoteService.js";

export async function dashboardLoader({ request }) {
  try {
    return { page: await dashboardCourses(request.signal) };
  } catch (error) {
    if (error.response?.status === 401) throw redirect("/entrar");
    return { page: null };
  }
}

export async function quoteLoader({ request }) {
  const ticker = new URL(request.url).searchParams.get("ticker")?.trim().toUpperCase();
  if (!ticker || !/^[A-Z0-9.-]{1,16}$/.test(ticker)) return { error: "Informe um ticker válido." };

  try {
    const data = await buscarCotacao(ticker, request.signal);
    const quote = data.results?.[0];
    if (!quote || !Number.isFinite(quote.regularMarketPrice)) return { error: "Nenhuma cotação disponível para " + ticker + "." };
    return { quote };
  } catch (error) {
    if (error.response?.status === 401) throw redirect("/entrar");
    return {
      error: error.response?.status === 404
        ? "Ativo não encontrado. Confira o ticker informado."
        : "A cotação está indisponível no momento. Tente novamente.",
    };
  }
}

function MarketQuote() {
  const lookup = useFetcher();
  const loading = lookup.state !== "idle";
  const quote = lookup.data?.quote;
  const change = quote?.regularMarketChangePercent;
  const hasChange = Number.isFinite(change);
  const ChangeIcon = change < 0 ? TrendingDown : TrendingUp;
  const currency = /^[A-Z]{3}$/.test(quote?.currency) ? quote.currency : undefined;
  const priceFormat = new Intl.NumberFormat("pt-BR", currency ? { style: "currency", currency } : { maximumFractionDigits: 2 });

  return (
    <section className="panel space-y-4 p-5 sm:p-6" aria-labelledby="cotacoes-titulo">
      <div>
        <p className="eyebrow">Mercado</p>
        <h2 id="cotacoes-titulo" className="mt-1 text-xl font-bold">Consultar cotação</h2>
        <p className="mt-1 text-sm text-muted">Pesquise pelo código do ativo.</p>
      </div>
      <lookup.Form method="get" action="/cotacao" className="flex flex-wrap gap-2">
        <label htmlFor="ticker" className="sr-only">Ticker</label>
        <input id="ticker" name="ticker" required maxLength={16} autoComplete="off" placeholder="Ex.: PETR4" className="field min-w-32 flex-1 uppercase placeholder:normal-case" />
        <button className="button-primary" disabled={loading}><Search size={16} /> {loading ? "Buscando…" : "Buscar"}</button>
      </lookup.Form>
      {!loading && lookup.data?.error && <Notice error>{lookup.data.error}</Notice>}
      {!loading && quote && (
        <div className="rounded-xl border border-line bg-canvas p-4" aria-live="polite">
          <div className="flex flex-wrap items-start justify-between gap-2">
            <div>
              <p className="font-bold">{quote.symbol}</p>
              <p className="text-xs text-muted">{quote.shortName || quote.longName}</p>
            </div>
            {hasChange && <span className={"inline-flex items-center gap-1 text-xs font-semibold " + (change < 0 ? "text-negative" : "text-positive")}><ChangeIcon size={15} /> {change.toLocaleString("pt-BR", { signDisplay: "exceptZero", maximumFractionDigits: 2 })}%</span>}
          </div>
          <p className="mt-3 text-2xl font-bold tabular-nums">{priceFormat.format(quote.regularMarketPrice)}</p>
        </div>
      )}
      <p className="text-xs text-muted">Dados da Brapi. As cotações podem ter atraso.</p>
    </section>
  );
}

export default function Home() {
  const { page } = useLoaderData();
  const user = useRouteLoaderData("app");
  const firstName = user.name?.trim().split(/\s+/)[0] || "investidor";

  return (
    <>
      <header className="panel border-white/5 p-6 sm:p-8">
        <h1 className="mb-2 text-3xl font-bold break-words">Olá, {firstName}.</h1>
        <p className="max-w-xl text-muted">Bem-vindo ao <span className="font-semibold text-amber-400">Investir+</span>. Explore cursos sobre finanças e consulte cotações do mercado.</p>
        <Link to="/educacao" className="button-primary mt-5">Explorar cursos <ArrowRight size={17} /></Link>
      </header>

      <div className="grid gap-5 sm:grid-cols-2">
        <Link to="/educacao" className="panel group flex min-w-0 items-center gap-4 border-white/5 p-5 hover:border-brand/50 sm:p-6">
          <span className="grid size-12 shrink-0 place-items-center rounded-xl bg-amber-500/20 text-amber-400"><BookOpen size={24} /></span>
          <span className="min-w-0 flex-1"><span className="block text-xs font-semibold tracking-wider text-zinc-500 uppercase">Cursos disponíveis</span><strong className="block truncate text-lg">{page?.totalItems ?? "—"}</strong></span>
          <ArrowRight className="size-4 shrink-0 text-muted group-hover:text-brand" />
        </Link>
        <Link to="/perfil" className="panel group flex min-w-0 items-center gap-4 border-white/5 p-5 hover:border-brand/50 sm:p-6">
          <span className="grid size-12 shrink-0 place-items-center rounded-xl bg-blue-500/20 text-blue-400"><Award size={24} /></span>
          <span className="min-w-0 flex-1"><span className="block text-xs font-semibold tracking-wider text-zinc-500 uppercase">Meu perfil</span><strong className="block text-lg break-words">{roleNames[user.role]}</strong></span>
          <ArrowRight className="size-4 shrink-0 text-muted group-hover:text-brand" />
        </Link>
      </div>

      <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,0.8fr)_minmax(0,1.5fr)]">
        <MarketQuote />
        <section className="space-y-4" aria-labelledby="educacao-titulo">
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div>
              <p className="eyebrow">Educação</p>
              <h2 id="educacao-titulo" className="mt-1 text-2xl font-bold tracking-tight">Comece a aprender</h2>
            </div>
            <Link to="/educacao" className="button-secondary">Ver catálogo <ArrowRight size={16} /></Link>
          </div>
          {page?.items.length ? (
            <div className="grid gap-3 sm:grid-cols-3">{page.items.map((course) => <CourseCard key={course.id} course={course} />)}</div>
          ) : (
            <p className="panel p-5 text-sm text-muted">{page ? "Ainda não há cursos no catálogo." : "O catálogo está indisponível no momento."}</p>
          )}
        </section>
      </div>
    </>
  );
}
