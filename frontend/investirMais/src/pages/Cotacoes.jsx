import { Search, TrendingDown, TrendingUp } from "lucide-react";
import { Form, useLoaderData, useNavigation, useSearchParams } from "react-router";
import { request } from "../services/api";
import { routeLoader } from "../services/routing";
import { Field, PageHeading, Submit } from "../components/ui";
import { formatarMoeda } from "../utils/format";

export const loader = routeLoader(async ({ request: navigation }) => {
  const ticker = new URL(navigation.url).searchParams.get("ticker")?.trim().toUpperCase();
  if (!ticker) return null;
  return request(`/api/assets/${encodeURIComponent(ticker)}/quote`, { signal: navigation.signal });
});
export function Component() {
  const data = useLoaderData();
  const navigation = useNavigation();
  const [search] = useSearchParams();
  const pending = navigation.state !== "idle";
  const quotes = pending ? [] : data?.results || [];
  return <>
    <PageHeading title="Cotações">Consulte o preço e a variação dos ativos no mercado.</PageHeading>
    <Form className="panel mb-6 flex flex-wrap items-end gap-4">
      <div className="min-w-0 flex-1"><Field label="Ticker do ativo" name="ticker" placeholder="Ex.: PETR4" pattern="[A-Za-z0-9.\-]{1,16}" maxLength={16} autoComplete="off" defaultValue={search.get("ticker") || ""} /></div>
      <Submit pending={pending}><Search size={18} /> Consultar</Submit>
    </Form>
    {!pending && data && !quotes.length && <p className="panel text-zinc-400">Nenhuma cotação encontrada para esse ticker.</p>}
    <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
      {quotes.map((quote) => {
        const change = quote.regularMarketChangePercent;
        const positive = change != null && change >= 0;
        const Icon = positive ? TrendingUp : TrendingDown;
        return <article key={quote.symbol} className="panel">
          <div className="mb-5 flex items-center gap-3"><div className="rounded-xl bg-amber-400/10 p-3 text-amber-400"><TrendingUp size={22} /></div><div><h2 className="text-lg font-bold">{quote.symbol}</h2><p className="text-sm text-zinc-400">{quote.shortName || quote.longName}</p></div></div>
          <p className="text-3xl font-bold">{formatarMoeda(quote.regularMarketPrice, quote.currency || "BRL")}</p>
          {change != null && <p className={`mt-3 flex items-center gap-2 text-sm ${positive ? "text-emerald-400" : "text-red-400"}`}><Icon size={16} />{change.toLocaleString("pt-BR", { maximumFractionDigits: 2, signDisplay: "exceptZero" })}%</p>}
          {quote.regularMarketTime && <p className="mt-5 text-xs text-zinc-500">Atualização: {new Date(typeof quote.regularMarketTime === "number" ? quote.regularMarketTime * 1000 : quote.regularMarketTime).toLocaleString("pt-BR")}</p>}
        </article>;
      })}
    </div>
    {!data && !pending && <div className="panel py-16 text-center text-zinc-400"><Search size={32} className="mx-auto mb-4 text-amber-400" /><p>Pesquise um ativo para acompanhar sua cotação.</p></div>}
  </>;
}
