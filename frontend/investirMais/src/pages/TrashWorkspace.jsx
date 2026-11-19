import { useState } from "react";
import { useSearchParams } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import { Notice } from "../components/ui/PageState.jsx";
import AppModal from "../components/ui/AppModal.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";

export default function TrashWorkspace() {
  const [search, setSearch] = useSearchParams();
  const kind = search.get("tipo") === "contas" ? "accounts" : "content";
  const [page, setPage] = useState(0), [selected, setSelected] = useState(null), [reason, setReason] = useState(""), [verified, setVerified] = useState(false);
  const client = useQueryClient();
  const accounts = kind === "accounts";
  const result = useQuery({ queryKey: ["trash", kind, page], queryFn: async () => (await apiClient.get(accounts ? "/api/trash/accounts" : "/api/trash", { params: { page, size: 20 } })).data });
  const restore = useMutation({ mutationFn: row => row.isAccount ? apiClient.post(`/api/trash/accounts/${row.id}/restore`, { reason: reason.trim() }) : apiClient.post(`/api/trash/${row.entity}/${row.id}/restore`), onSuccess: () => { setSelected(null); return client.invalidateQueries(); } });
  function select(row) { setSelected({ ...row, isAccount: accounts }); setReason(""); setVerified(false); restore.reset(); }
  return <>
    <label>Itens excluídos<select className="ml-3 rounded border border-line bg-canvas p-2" value={kind} onChange={event => { const next = new URLSearchParams(search); if (event.target.value === "accounts") next.set("tipo", "contas"); else next.delete("tipo"); setSearch(next); setPage(0); setSelected(null); restore.reset(); }}><option value="content">Cursos, aulas e questões</option><option value="accounts">Contas em recuperação</option></select></label>
    <p className="text-sm text-muted">{accounts ? "Contas desativadas são recuperáveis por 30 dias. Restaure somente após verificar a identidade e a solicitação do titular. Contas já eliminadas definitivamente não são recuperáveis." : "Restaure primeiro o curso, depois as aulas e questões desejadas. Restaurar um curso não republica automaticamente os conteúdos excluídos."}</p>
    {result.isPending && <p>Carregando lixeira…</p>}
    {result.error && <Notice error>{apiError(result.error, "Não foi possível carregar a lixeira.")}</Notice>}
    {restore.isSuccess && <Notice>Restauração registrada na auditoria.</Notice>}
    {result.data?.items.length === 0 && <p>Nenhum item na lixeira.</p>}
    {result.data?.items.map(row => <article key={row.entity + row.id} className="panel space-y-2 p-5"><h2 className="font-bold break-words">{accounts ? row.name : row.title}</h2>
      <p className="text-sm text-muted">{accounts ? row.email : ({ courses: "Curso", lessons: "Aula", questions: "Questão" })[row.entity]} · Excluído em {new Date(row.deleted_at).toLocaleString("pt-BR")}</p>
      {accounts && <p>Recuperável até {new Date(row.purge_after).toLocaleString("pt-BR")}</p>}
      <button className="button-secondary" disabled={accounts && new Date(row.purge_after) <= new Date()} onClick={() => select(row)}>Restaurar {accounts ? "conta" : "conteúdo"}</button>
    </article>)}
    <StatePagination page={page} hasNext={Boolean(result.data && (page + 1) * 20 < result.data.totalItems)} onChange={setPage} hideIfSinglePage={false} />
    {selected && <AppModal title={selected.isAccount ? "Restaurar conta" : "Restaurar conteúdo"} onClose={() => setSelected(null)} busy={restore.isPending}><form className="space-y-4" onSubmit={event => { event.preventDefault(); if (!selected.isAccount || (verified && reason.trim())) restore.mutate(selected); }}>
      <p>Restaurar {selected.isAccount ? selected.name : selected.title}?</p>
      {selected.isAccount ? <><p>O acesso, as matrículas e o progresso serão recuperados. Conteúdos do professor ocultados por esta desativação serão restaurados; itens excluídos anteriormente continuarão na lixeira. As sessões antigas permanecerão encerradas.</p><label className="block">Referência da solicitação do titular<textarea required maxLength={2000} className="block w-full rounded border border-line bg-canvas p-2" value={reason} onChange={event => setReason(event.target.value)} /></label><label className="flex gap-2"><input type="checkbox" required checked={verified} onChange={event => setVerified(event.target.checked)} />Verifiquei a identidade e o pedido de recuperação do titular.</label></> : <p>O item voltará a aparecer na plataforma. A restauração exige que a conta responsável, o curso e a aula vinculada estejam ativos, sem conflito de posições.</p>}
      {restore.error && <Notice error>{apiError(restore.error, "Não foi possível restaurar o item.")}</Notice>}
      <button className="button-primary" disabled={restore.isPending || (selected.isAccount && (!verified || !reason.trim()))}>Confirmar restauração</button>
    </form></AppModal>}
  </>;
}
