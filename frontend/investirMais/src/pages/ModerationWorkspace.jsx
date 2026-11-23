import { useState } from "react";
import { Link } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, Archive, Calendar, CheckCircle2, Clock, ExternalLink, Flag, GraduationCap, Trash2, Video, XCircle } from "lucide-react";
import { apiClient, apiError } from "../services/api.js";
import { reportReasons, reportStatuses } from "../services/reportReasons.js";
import { Field, SelectField } from "../components/ui/FormFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";

const statusBadges = {
  OPEN: { label: "Em análise", className: "border-amber-500/30 bg-amber-500/10 text-amber-400", icon: Clock },
  RESOLVED: { label: "Resolvida", className: "border-emerald-500/30 bg-emerald-500/10 text-emerald-400", icon: CheckCircle2 },
  DISMISSED: { label: "Arquivada", className: "border-zinc-500/30 bg-zinc-500/10 text-zinc-400", icon: Archive },
};

export default function ModerationWorkspace() {
  const [status, setStatus] = useState("OPEN");
  const [page, setPage] = useState(0);
  const [confirming, setConfirming] = useState(null);
  const [notes, setNotes] = useState({});
  const client = useQueryClient();

  const result = useQuery({
    queryKey: ["reports", status, page],
    queryFn: async () => (await apiClient.get("/api/reports", { params: { status, page, size: 20 } })).data,
  });

  const review = useMutation({
    mutationFn: ({ id, status, removeLesson = false }) =>
      apiClient.post(`/api/reports/${id}/review`, { status, note: notes[id]?.trim(), removeLesson }),
    onSuccess: () => {
      setConfirming(null);
      return client.invalidateQueries();
    },
  });

  return (
    <div className="space-y-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap items-center gap-1.5 rounded-xl border border-line bg-canvas p-1">
          {Object.entries(reportStatuses).map(([key, label]) => {
            const active = status === key;
            const Icon = statusBadges[key]?.icon || Flag;
            return (
              <button
                key={key}
                type="button"
                onClick={() => {
                  setStatus(key);
                  setPage(0);
                  setConfirming(null);
                }}
                className={`flex items-center gap-1.5 rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                  active ? "bg-brand font-semibold text-zinc-900 shadow-sm" : "text-ink hover:bg-raised"
                }`}
              >
                <Icon size={14} />
                <span>{label}</span>
              </button>
            );
          })}
        </div>

        <div className="w-full sm:w-56">
          <SelectField
            native
            label="Situação"
            value={status}
            options={Object.entries(reportStatuses)}
            onChange={(event) => {
              setStatus(event.target.value);
              setPage(0);
              setConfirming(null);
            }}
          />
        </div>
      </div>

      {result.isPending && (
        <div className="panel flex items-center justify-center p-12 text-muted">
          <p>Carregando denúncias…</p>
        </div>
      )}

      {(result.error || review.error) && (
        <Notice error>{apiError(result.error || review.error, "Não foi possível consultar ou analisar a denúncia.")}</Notice>
      )}

      {review.isSuccess && <Notice>Análise registrada com sucesso.</Notice>}

      {result.data?.content.length === 0 && (
        <div className="panel flex flex-col items-center justify-center space-y-2 p-12 text-center text-muted">
          <Flag size={32} className="text-muted/60" />
          <p className="font-medium text-ink">Nenhuma denúncia nesta situação.</p>
          <p className="text-xs">Não há itens classificados como "{reportStatuses[status]}".</p>
        </div>
      )}

      {result.data?.content.map((report) => {
        const isLesson = report.targetType === "LESSON";
        const TargetIcon = isLesson ? Video : GraduationCap;
        const currentBadge = statusBadges[report.status] || statusBadges.OPEN;
        const StatusIcon = currentBadge.icon;
        const noteText = notes[report.id] || "";

        return (
          <article className="panel space-y-4 p-5 sm:p-6" key={report.id}>
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex items-start gap-3 min-w-0">
                <div className="rounded-xl border border-line bg-canvas p-2.5 text-brand shrink-0">
                  <TargetIcon size={20} />
                </div>
                <div className="min-w-0 space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="rounded-md border border-line bg-canvas px-2 py-0.5 text-xs font-semibold text-muted">
                      {isLesson ? "Aula / Vídeo" : "Professor"}
                    </span>
                    <span className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold ${currentBadge.className}`}>
                      <StatusIcon size={12} /> {currentBadge.label}
                    </span>
                    <span className="rounded-md bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-300">
                      {reportReasons[report.reason] || report.reason}
                    </span>
                  </div>
                  <h2 className="font-bold text-lg text-ink break-words">
                    {isLesson ? "Vídeo" : "Professor"}: {report.targetLabel}
                  </h2>
                </div>
              </div>

              <div className="text-xs text-muted space-y-0.5 sm:text-right shrink-0">
                <p className="flex items-center gap-1 sm:justify-end">
                  <Calendar size={13} /> {new Date(report.createdAt).toLocaleString("pt-BR")}
                </p>
                <p className="text-[11px] text-muted/70">
                  {reportReasons[report.reason]} · {new Date(report.createdAt).toLocaleString("pt-BR")}
                </p>
              </div>
            </div>

            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-lg border border-line bg-canvas px-3.5 py-2 text-xs text-muted">
              <span>Protocolo: <strong className="font-mono text-ink">{report.id}</strong></span>
              <span>·</span>
              <span>Autor da denúncia: <strong className="font-mono text-ink">{report.reporterId}</strong></span>
            </div>

            <div className="rounded-xl border border-line bg-canvas/70 p-4 space-y-1.5">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted">Descrição informada pelo aluno:</span>
              <p className="whitespace-pre-wrap break-words text-sm text-ink leading-relaxed">{report.description}</p>
            </div>

            <div>
              <Link
                className="inline-flex items-center gap-1.5 text-sm font-semibold text-brand underline hover:text-amber-300"
                to={`/educacao/${report.courseId}${isLesson ? "/" + report.targetId : ""}`}
              >
                <ExternalLink size={15} /> Ver conteúdo denunciado
              </Link>
            </div>

            {report.status === "OPEN" ? (
              <div className="space-y-3 border-t border-line pt-4">
                <Field
                  label="Parecer da denúncia"
                  multiline
                  minRows={3}
                  maxLength={2000}
                  placeholder="Justifique o parecer da decisão técnica tomada antes de finalizar..."
                  value={noteText}
                  onChange={(event) => setNotes((prev) => ({ ...prev, [report.id]: event.target.value }))}
                  hint={`${noteText.length}/2000 caracteres`}
                />
                <div className="flex flex-wrap items-center gap-2.5">
                  <button
                    type="button"
                    className="button-secondary"
                    disabled={review.isPending || !noteText.trim()}
                    onClick={() => review.mutate({ id: report.id, status: "DISMISSED" })}
                  >
                    <Archive size={15} /> Arquivar denúncia
                  </button>
                  <button
                    type="button"
                    className="button-primary"
                    disabled={review.isPending || !noteText.trim()}
                    onClick={() => review.mutate({ id: report.id, status: "RESOLVED" })}
                  >
                    <CheckCircle2 size={15} /> Marcar como resolvida
                  </button>
                  {isLesson && (
                    <button
                      type="button"
                      className="button-danger"
                      disabled={review.isPending || !noteText.trim()}
                      onClick={() => setConfirming(report.id)}
                    >
                      <Trash2 size={15} /> Remover vídeo e resolver
                    </button>
                  )}
                </div>

                {confirming === report.id && (
                  <div className="space-y-3 rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm">
                    <div className="flex items-start gap-2.5 text-rose-300">
                      <AlertTriangle size={18} className="shrink-0 mt-0.5" />
                      <p className="leading-relaxed">
                        A aula e suas questões vinculadas serão retiradas da plataforma. O arquivo no provedor de vídeo permanece disponível por seu endereço.
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2.5 pt-1">
                      <button
                        type="button"
                        className="button-danger"
                        disabled={review.isPending}
                        onClick={() => review.mutate({ id: report.id, status: "RESOLVED", removeLesson: true })}
                      >
                        Confirmar remoção da aula
                      </button>
                      <button
                        type="button"
                        className="button-secondary"
                        onClick={() => setConfirming(null)}
                      >
                        Cancelar
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="rounded-xl border border-line bg-canvas p-4 space-y-1 text-sm border-l-4 border-l-brand">
                <span className="text-xs font-semibold uppercase tracking-wider text-muted">Parecer do moderador:</span>
                <p className="whitespace-pre-wrap break-words text-ink leading-relaxed font-medium">Parecer: {report.reviewNote}</p>
                <p className="text-xs text-muted flex items-center gap-1 pt-1">
                  <Clock size={12} /> Analisado em {new Date(report.reviewedAt).toLocaleString("pt-BR")}
                </p>
              </div>
            )}
          </article>
        );
      })}

      <StatePagination page={page} totalPages={result.data?.page.totalPages} onChange={setPage} hideIfSinglePage={false} />
    </div>
  );
}
