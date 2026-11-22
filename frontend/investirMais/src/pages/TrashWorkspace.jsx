import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import { Notice } from "../components/ui/PageState.jsx";
import AppModal from "../components/ui/AppModal.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";

export default function TrashWorkspace() {
  const [page, setPage] = useState(0), [selected, setSelected] = useState(null);
  const client = useQueryClient();
  const result = useQuery({ queryKey: ["trash", "content", page], queryFn: async () => (await apiClient.get("/api/trash", { params: { page, size: 20 } })).data });
  const restore = useMutation({ mutationFn: row => apiClient.post(`/api/trash/${row.entity}/${row.id}/restore`), onSuccess: () => { setSelected(null); return client.invalidateQueries(); } });
  return <>
    <p className="text-sm text-muted">Restaure primeiro o curso, depois as aulas e questões desejadas. Contas excluídas não podem ser recuperadas.</p>
    {result.isPending && <p>Carregando lixeira…</p>}
    {result.error && <Notice error>{apiError(result.error, "Não foi possível carregar a lixeira.")}</Notice>}
    {restore.isSuccess && <Notice>Restauração registrada na auditoria.</Notice>}
    {result.data?.items.length === 0 && <p>Nenhum item na lixeira.</p>}
    {result.data?.items.map(row => <article key={row.entity + row.id} className="panel space-y-2 p-5"><h2 className="font-bold break-words">{row.title}</h2>
      <p className="text-sm text-muted">{({ courses: "Curso", lessons: "Aula", questions: "Questão" })[row.entity]} · Excluído em {new Date(row.deleted_at).toLocaleString("pt-BR")}</p>
      <button className="button-secondary" onClick={() => { setSelected(row); restore.reset(); }}>Restaurar conteúdo</button>
    </article>)}
    <StatePagination page={page} hasNext={Boolean(result.data && (page + 1) * 20 < result.data.totalItems)} onChange={setPage} hideIfSinglePage={false} />
    {selected && <AppModal title="Restaurar conteúdo" onClose={() => setSelected(null)} busy={restore.isPending}><form className="space-y-4" onSubmit={event => { event.preventDefault(); restore.mutate(selected); }}>
      <p>Restaurar {selected.title}?</p>
      <p>O item voltará a aparecer na plataforma. A restauração exige que a conta responsável, o curso e a aula vinculada estejam ativos, sem conflito de posições.</p>
      {restore.error && <Notice error>{apiError(restore.error, "Não foi possível restaurar o item.")}</Notice>}
      <button className="button-primary" disabled={restore.isPending}>Confirmar restauração</button>
    </form></AppModal>}
  </>;
}
