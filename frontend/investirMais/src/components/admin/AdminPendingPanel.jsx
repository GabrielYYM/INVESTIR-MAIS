import { Link } from "react-router";
import { Flag, UserCheck } from "lucide-react";
import { useGetAdminPendingSummary } from "../../generated/account.ts";
import { apiError } from "../../services/api.js";
import { Notice } from "../ui/PageState.jsx";
import StatCard from "../ui/StatCard.jsx";

const dateTime = value => new Date(value).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });

export default function AdminPendingPanel() {
  const pending = useGetAdminPendingSummary();
  const summary = pending.data;
  const empty = summary && summary.openReports === 0 && summary.pendingTeacherApplications === 0;
  return <section className="space-y-4" aria-labelledby="pendencias-titulo">
    <div>
      <h2 id="pendencias-titulo" className="text-xl font-bold">Pendências da administração</h2>
      <p className="text-sm text-muted">Acompanhe as análises que precisam de atenção.</p>
    </div>
    {pending.isPending && <p role="status">Carregando pendências…</p>}
    {pending.error && <Notice error>{apiError(pending.error, "Não foi possível atualizar as pendências. Tente novamente.")}</Notice>}
    {summary && <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-2">
        {[
          ["Denúncias em análise", summary.openReports, Flag, "text-rose-400", "Aguardam parecer do administrador", "/admin/denuncias", "Analisar denúncias"],
          ["Candidaturas pendentes", summary.pendingTeacherApplications, UserCheck, "text-purple-400", "Documentos de candidatos com conta ativa", "/admin/professores", "Analisar candidaturas"],
        ].map(([label, value, Icon, color, caption, to, action]) => <div key={label} className="space-y-2"><StatCard label={label} value={value} icon={Icon} color={color} caption={caption} /><Link className="inline-flex text-sm font-semibold text-brand underline" to={to}>{action}</Link></div>)}
      </div>
      {empty && <p className="text-sm text-muted">Nenhuma pendência no momento.</p>}
      <p className="text-xs text-muted">Contagem consultada em <time dateTime={summary.checkedAt}>{dateTime(summary.checkedAt)}</time>.</p>
    </>}
  </section>;
}
