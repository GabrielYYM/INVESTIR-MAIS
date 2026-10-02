import { Filter, ListFilter, ClipboardList, Pencil, Trash2 } from "lucide-react";

function formatarMoeda(valor) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

function formatarPercentual(valor) {
  return `${valor.toFixed(2).replace(".", ",")}%`;
}

function ScoreBadge({ score }) {
  if (score === null || score === undefined) {
    return (
      <span className="inline-flex items-center gap-1 text-xs text-zinc-600 italic">
        — sem avaliação
      </span>
    );
  }

  const color =
    score >= 7
      ? "text-emerald-400 bg-emerald-500/10"
      : score >= 4
      ? "text-amber-400 bg-amber-500/10"
      : "text-red-400 bg-red-500/10";

  const barColor =
    score >= 7 ? "bg-emerald-500" : score >= 4 ? "bg-amber-500" : "bg-red-500";

  return (
    <div className="flex items-center gap-2">
      <span className={`text-sm font-bold px-2 py-0.5 rounded-md ${color}`}>
        {score.toFixed(1)}
      </span>
      <div className="w-16 h-1.5 bg-white/10 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full ${barColor} transition-all`}
          style={{ width: `${score * 10}%` }}
        />
      </div>
    </div>
  );
}

const COLUNAS = ["Tipo", "Ticker", "Valor atual", "Percentual", "Score", "Quantidade", "Ações"];

export default function AssetsTable({ ativos, loading, error, scores, onEvaluate, onEdit, onDelete }) {
  return (
    <div className="bg-[#0e0c14] rounded-2xl overflow-hidden">
      <div className="flex items-center justify-between px-6 py-5">
        <h2 className="text-xl font-bold text-white">Lista de Ativos</h2>
        <div className="flex items-center gap-4 text-zinc-400">
          <Filter size={18} />
          <ListFilter size={18} />
        </div>
      </div>

      <table className="w-full text-sm">
        <thead>
          <tr className="text-zinc-500 text-left">
            {COLUNAS.map((coluna) => (
              <th key={coluna} className="px-6 pb-3 font-normal">
                {coluna}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {loading && (
            <tr>
              <td colSpan={COLUNAS.length} className="px-6 py-6 text-zinc-500">
                Carregando cotações...
              </td>
            </tr>
          )}

          {error && !loading && (
            <tr>
              <td colSpan={COLUNAS.length} className="px-6 py-6 text-red-400">
                Não foi possível carregar os ativos. Tente novamente em instantes.
              </td>
            </tr>
          )}

          {!loading &&
            !error &&
            ativos.map((ativo) => {
              const scoreEntry = scores?.[String(ativo.id ?? ativo.ticker)];
              const score = scoreEntry?.score ?? 0.0;

              return (
                <tr
                  key={ativo.id ?? ativo.ticker}
                  className="border-t border-white/5 text-zinc-200 hover:bg-white/[0.02] transition-colors"
                >
                  <td className="px-6 py-4">{ativo.tipo}</td>
                  <td className="px-6 py-4 font-mono text-amber-400">{ativo.ticker}</td>
                  <td className="px-6 py-4">{formatarMoeda(ativo.valorAtual)}</td>
                  <td className="px-6 py-4">{formatarPercentual(ativo.percentual)}</td>
                  <td className="px-6 py-4">
                    <ScoreBadge score={score} />
                  </td>
                  <td className="px-6 py-4">{ativo.quantidade}</td>
                  <td className="px-6 py-4 text-zinc-500">
                    <div className="flex items-center gap-1">
                      <button
                        id={`btn-avaliar-${ativo.ticker}`}
                        type="button"
                        aria-label={`Avaliar ${ativo.ticker}`}
                        onClick={() => onEvaluate?.(ativo)}
                        title="Avaliar ativo"
                        className="p-1.5 rounded-lg hover:bg-white/10 hover:text-amber-400 transition-colors cursor-pointer"
                      >
                        <ClipboardList size={16} />
                      </button>

                      <button
                        id={`btn-editar-${ativo.ticker}`}
                        type="button"
                        aria-label={`Editar ${ativo.ticker}`}
                        onClick={() => onEdit?.(ativo)}
                        title="Editar ativo"
                        className="p-1.5 rounded-lg hover:bg-white/10 hover:text-blue-400 transition-colors cursor-pointer"
                      >
                        <Pencil size={16} />
                      </button>

                      <button
                        id={`btn-excluir-${ativo.ticker}`}
                        type="button"
                        aria-label={`Excluir ${ativo.ticker}`}
                        onClick={() => onDelete?.(ativo)}
                        title="Excluir ativo"
                        className="p-1.5 rounded-lg hover:bg-white/10 hover:text-red-400 transition-colors cursor-pointer"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}

          {!loading && !error && ativos.length === 0 && (
            <tr>
              <td colSpan={COLUNAS.length + 1} className="px-6 py-6 text-zinc-500">
                Nenhum ativo cadastrado ainda. Clique em &ldquo;Adicionar ativo&rdquo; para
                começar.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
