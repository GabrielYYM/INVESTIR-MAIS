import { useState } from "react";
import { Link } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import { reportReasons, reportStatuses } from "../services/reportReasons.js";
import { Field, SelectField } from "../components/ui/FormFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";

export default function ModerationWorkspace() {
  const [status, setStatus] = useState("OPEN"), [page, setPage] = useState(0), [confirming, setConfirming] = useState(null);
  const [notes, setNotes] = useState({});
  const client = useQueryClient();
  const result = useQuery({ queryKey: ["reports", status, page], queryFn: async () => (await apiClient.get("/api/reports", { params: { status, page, size: 20 } })).data });
  const review = useMutation({ mutationFn: ({ id, status, removeLesson = false }) => apiClient.post(`/api/reports/${id}/review`, { status, note: notes[id]?.trim(), removeLesson }), onSuccess: () => { setConfirming(null); return client.invalidateQueries(); } });
  return <>
    <div className="max-w-xs">
      <SelectField native label="Situação" value={status} options={Object.entries(reportStatuses)} onChange={event => { setStatus(event.target.value); setPage(0); setConfirming(null); }} />
    </div>
    {result.isPending && <p>Carregando denúncias…</p>}
    {(result.error || review.error) && <Notice error>{apiError(result.error || review.error, "Não foi possível consultar ou analisar a denúncia.")}</Notice>}
    {review.isSuccess && <Notice>Análise registrada.</Notice>}
    {result.data?.content.length === 0 && <p>Nenhuma denúncia nesta situação.</p>}
    {result.data?.content.map(report => <article className="panel space-y-3 p-5" key={report.id}>
      <h2 className="font-bold">{report.targetType === "LESSON" ? "Vídeo" : "Professor"}: {report.targetLabel}</h2>
      <p>{reportReasons[report.reason]} · {new Date(report.createdAt).toLocaleString("pt-BR")}</p>
      <p className="text-xs text-muted">Protocolo: {report.id} · Autor da denúncia: {report.reporterId}</p>
      <p className="whitespace-pre-wrap break-words">{report.description}</p>
      <Link className="text-brand underline" to={`/educacao/${report.courseId}${report.targetType === "LESSON" ? "/" + report.targetId : ""}`}>Ver conteúdo denunciado</Link>
      {report.status === "OPEN" ? <>
        <Field label="Parecer da denúncia" multiline minRows={3} maxLength={2000} value={notes[report.id] || ""} onChange={event => setNotes(previous => ({ ...previous, [report.id]: event.target.value }))} />
        <div className="flex flex-wrap gap-3">
          <button className="button-secondary" disabled={review.isPending || !notes[report.id]?.trim()} onClick={() => review.mutate({ id: report.id, status: "DISMISSED" })}>Arquivar denúncia</button>
          <button className="button-primary" disabled={review.isPending || !notes[report.id]?.trim()} onClick={() => review.mutate({ id: report.id, status: "RESOLVED" })}>Marcar como resolvida</button>
          {report.targetType === "LESSON" && <button className="button-danger" disabled={review.isPending || !notes[report.id]?.trim()} onClick={() => setConfirming(report.id)}>Remover vídeo e resolver</button>}
        </div>
        {confirming === report.id && <div className="space-y-3 rounded border border-line p-4"><p>A aula e suas questões vinculadas serão retiradas da plataforma e enviadas para a lixeira. O arquivo no provedor de vídeo permanece disponível por seu endereço.</p><button className="button-danger" disabled={review.isPending} onClick={() => review.mutate({ id: report.id, status: "RESOLVED", removeLesson: true })}>Confirmar remoção da aula</button><button className="button-secondary ml-3" onClick={() => setConfirming(null)}>Cancelar</button></div>}
      </> : <p className="whitespace-pre-wrap break-words">Parecer: {report.reviewNote} · {new Date(report.reviewedAt).toLocaleString("pt-BR")}</p>}
    </article>)}
    <StatePagination page={page} totalPages={result.data?.page.totalPages} onChange={setPage} hideIfSinglePage={false} />
  </>;
}
