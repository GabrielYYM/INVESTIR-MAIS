import { Link } from "react-router";
import { AlertTriangle, Clock, Flag, RotateCcw, UserCheck } from "lucide-react";
import { useGetAdminPendingSummary } from "../../generated/account.ts";
import { apiError } from "../../services/api.js";
import { Notice } from "../ui/PageState.jsx";
import StatCard from "../ui/StatCard.jsx";

const dateTime = value => new Date(value).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" });

export default function AdminPendingPanel() {
  const pending = useGetAdminPendingSummary();
  const summary = pending.data;
  const empty = summary && summary.openReports === 0 && summary.pendingTeacherApplications === 0 && summary.recoverableAccounts === 0 && summary.expiredAccounts === 0;
  return <section className="space-y-4" aria-labelledby="pendencias-titulo">
    <div>
      <h2 id="pendencias-titulo" className="text-xl font-bold">Pendências da administração</h2>
      <p className="text-sm text-muted">Acompanhe as análises e os prazos que precisam de atenção.</p>
    </div>
    {pending.isPending && <p role="status">Carregando pendências…</p>}
    {pending.error && <Notice error>{apiError(pending.error, "Não foi possível atualizar as pendências. Tente novamente.")}</Notice>}
    {summary && <>
      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {[
          ["Denúncias em análise", summary.openReports, Flag, "text-rose-400", "Aguardam parecer do administrador", "/admin/denuncias", "Analisar denúncias"],
          ["Candidaturas pendentes", summary.pendingTeacherApplications, UserCheck, "text-purple-400", "Documentos de candidatos com conta ativa", "/admin/professores", "Analisar candidaturas"],
          ["Contas em recuperação", summary.recoverableAccounts, RotateCcw, "text-amber-400", `${summary.expiringAccounts} com prazo nos próximos ${summary.expiringWithinDays} dias`, "/admin/lixeira?tipo=contas", "Ver contas em recuperação"],
          ["Prazos encerrados", summary.expiredAccounts, Clock, "text-sky-400", "Aguardam processamento da eliminação", "/admin/lixeira?tipo=contas", "Ver prazos encerrados"],
        ].map(([label, value, Icon, color, caption, to, action]) => <div key={label} className="space-y-2"><StatCard label={label} value={value} icon={Icon} color={color} caption={caption} /><Link className="inline-flex text-sm font-semibold text-brand underline" to={to}>{action}</Link></div>)}
      </div>
      {summary.expiringAccounts > 0 && <div className="panel flex items-start gap-3 border-amber-500/30 p-4" role="status"><AlertTriangle className="shrink-0 text-amber-400" size={20} /><div><p>{summary.expiringAccounts} {summary.expiringAccounts === 1 ? "conta perde" : "contas perdem"} a possibilidade de recuperação nos próximos {summary.expiringWithinDays} dias.</p><p className="text-sm text-muted">Restaure somente mediante solicitação verificada do titular.</p></div></div>}
      {summary.nextRecoveryDeadline && <p className="text-sm text-muted">Próximo fim de prazo de recuperação: <time dateTime={summary.nextRecoveryDeadline}>{dateTime(summary.nextRecoveryDeadline)}</time>.</p>}
      {empty && <p className="text-sm text-muted">Nenhuma pendência no momento.</p>}
      <p className="text-xs text-muted">Contagem consultada em <time dateTime={summary.checkedAt}>{dateTime(summary.checkedAt)}</time>.</p>
    </>}
  </section>;
}
