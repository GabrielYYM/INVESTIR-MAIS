import { useState } from "react";
import { Link } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { AlertTriangle, Archive, Calendar, CheckCircle2, Clock, ExternalLink, Eye, Flag, GraduationCap, Trash2, Video, XCircle } from "lucide-react";
import { Box, Button, IconButton, Paper, Tooltip, Typography } from "@mui/material";
import { DataTable, ListContextProvider, useList } from "react-admin";
import { apiClient, apiError } from "../services/api.js";
import { reportReasons, reportStatuses } from "../services/reportReasons.js";
import { Field } from "../components/ui/FormFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";
import AppModal from "../components/ui/AppModal.jsx";

const statusBadges = {
  OPEN: { label: "Em análise", className: "border-amber-500/30 bg-amber-500/10 text-amber-400", icon: Clock },
  RESOLVED: { label: "Resolvida", className: "border-emerald-500/30 bg-emerald-500/10 text-emerald-400", icon: CheckCircle2 },
  DISMISSED: { label: "Arquivada", className: "border-zinc-500/30 bg-zinc-500/10 text-zinc-400", icon: Archive },
};

function ModerationReportsTable({ list, statusBadges, onSelect }) {
  const listContext = useList({ data: list, perPage: 25 });
  return (
    <Paper variant="outlined" className="panel space-y-4 p-5 sm:p-6">
      <ListContextProvider value={listContext}>
        <Box sx={{ overflowX: "auto" }}>
          <DataTable
            resource="reports"
            bulkActionButtons={false}
            rowClick={false}
            sx={{ width: "100%", minWidth: 760, "& table": { width: "100%" } }}
            empty={<Typography color="text.secondary">Nenhuma denúncia encontrada.</Typography>}
          >
            <DataTable.Col
              source="targetLabel"
              label="Alvo Denunciado"
              render={(report) => {
                const isLesson = report.targetType === "LESSON";
                const TargetIcon = isLesson ? Video : GraduationCap;
                return (
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-canvas border border-line text-brand">
                      <TargetIcon size={18} />
                    </div>
                    <div className="min-w-0">
                      <span className="block font-semibold text-ink truncate">
                        {isLesson ? "Vídeo: " : "Professor: "}{report.targetLabel}
                      </span>
                      <span className="block font-mono text-xs text-muted truncate">
                        Protocolo: {report.id.slice(0, 8)}…
                      </span>
                    </div>
                  </div>
                );
              }}
            />
            <DataTable.Col
              source="reason"
              label="Motivo"
              render={(report) => (
                <span className="rounded-md bg-amber-500/10 px-2 py-0.5 text-xs font-medium text-amber-300">
                  {reportReasons[report.reason] || report.reason}
                </span>
              )}
            />
            <DataTable.Col
              source="status"
              label="Situação"
              render={(report) => {
                const currentBadge = statusBadges[report.status] || statusBadges.OPEN;
                const StatusIcon = currentBadge.icon;
                return (
                  <span className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold ${currentBadge.className}`}>
                    <StatusIcon size={12} /> {currentBadge.label}
                  </span>
                );
              }}
            />
            <DataTable.Col
              source="createdAt"
              label="Data"
              render={(report) => (
                <span className="inline-flex items-center gap-1 text-xs text-muted">
                  <Calendar size={13} /> {new Date(report.createdAt).toLocaleString("pt-BR")}
                </span>
              )}
            />
            <DataTable.Col
              label="Ações"
              align="right"
              render={(report) => (
                <Button
                  size="small"
                  startIcon={<Eye size={15} />}
                  onClick={() => onSelect(report)}
                >
                  Visualizar
                </Button>
              )}
            />
          </DataTable>
        </Box>
      </ListContextProvider>
    </Paper>
  );
}

export default function ModerationWorkspace() {
  const [status, setStatus] = useState("OPEN");
  const [page, setPage] = useState(0);
  const [selectedReport, setSelectedReport] = useState(null);
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
      setSelectedReport(null);
      return client.invalidateQueries();
    },
  });

  const rawList = result.data?.content || [];
  const activeReport = selectedReport
    ? rawList.find((r) => r.id === selectedReport.id) || selectedReport
    : null;

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
                  setSelectedReport(null);
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

      {rawList.length === 0 && !result.isPending && (
        <div className="panel flex flex-col items-center justify-center space-y-2 p-12 text-center text-muted">
          <Flag size={32} className="text-muted/60" />
          <p className="font-medium text-ink">Nenhuma denúncia nesta situação.</p>
          <p className="text-xs">Não há itens classificados como "{reportStatuses[status]}".</p>
        </div>
      )}

      {rawList.length > 0 && (
        <ModerationReportsTable
          list={rawList}
          statusBadges={statusBadges}
          onSelect={setSelectedReport}
        />
      )}

      {/* Modal de Detalhes e Ação da Denúncia */}
      {activeReport && (
        <AppModal
          title={`Denúncia: ${activeReport.targetType === "LESSON" ? "Vídeo" : "Professor"}`}
          width={640}
          onClose={() => {
            setSelectedReport(null);
            setConfirming(null);
          }}
          busy={review.isPending}
        >
          {(() => {
            const isLesson = activeReport.targetType === "LESSON";
            const TargetIcon = isLesson ? Video : GraduationCap;
            const currentBadge = statusBadges[activeReport.status] || statusBadges.OPEN;
            const StatusIcon = currentBadge.icon;
            const noteText = notes[activeReport.id] || "";

            return (
              <div className="space-y-4">
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
                          {reportReasons[activeReport.reason] || activeReport.reason}
                        </span>
                      </div>
                      <h3 className="font-bold text-lg text-ink break-words">
                        {isLesson ? "Vídeo" : "Professor"}: {activeReport.targetLabel}
                      </h3>
                    </div>
                  </div>

                  <div className="text-xs text-muted sm:text-right shrink-0">
                    <p className="flex items-center gap-1 sm:justify-end">
                      <Calendar size={13} /> {new Date(activeReport.createdAt).toLocaleString("pt-BR")}
                    </p>
                  </div>
                </div>

                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 rounded-lg border border-line bg-canvas px-3.5 py-2 text-xs text-muted">
                  <span>Protocolo: <strong className="font-mono text-ink">{activeReport.id}</strong></span>
                  <span>·</span>
                  <span>Autor da denúncia: <strong className="font-mono text-ink">{activeReport.reporterId}</strong></span>
                </div>

                <div className="rounded-xl border border-line bg-canvas/70 p-4 space-y-1.5">
                  <span className="text-xs font-semibold uppercase tracking-wider text-muted">Descrição informada pelo aluno:</span>
                  <p className="whitespace-pre-wrap break-words text-sm text-ink leading-relaxed">{activeReport.description}</p>
                </div>

                <div>
                  <Link
                    className="inline-flex items-center gap-1.5 text-sm font-semibold text-brand underline hover:text-amber-300"
                    to={`/educacao/${activeReport.courseId}${isLesson ? "/" + activeReport.targetId : ""}`}
                  >
                    <ExternalLink size={15} /> Ver conteúdo denunciado
                  </Link>
                </div>

                {activeReport.status === "OPEN" ? (
                  <div className="space-y-3 border-t border-line pt-4">
                    <Field
                      label="Parecer da denúncia"
                      multiline
                      minRows={3}
                      maxLength={2000}
                      placeholder="Justifique o parecer da decisão técnica tomada antes de finalizar..."
                      value={noteText}
                      onChange={(event) => setNotes((prev) => ({ ...prev, [activeReport.id]: event.target.value }))}
                      hint={`${noteText.length}/2000 caracteres`}
                    />
                    <div className="flex flex-wrap items-center gap-2.5">
                      <button
                        type="button"
                        className="button-secondary"
                        disabled={review.isPending || !noteText.trim()}
                        onClick={() => review.mutate({ id: activeReport.id, status: "DISMISSED" })}
                      >
                        <Archive size={15} /> Arquivar denúncia
                      </button>
                      <button
                        type="button"
                        className="button-primary"
                        disabled={review.isPending || !noteText.trim()}
                        onClick={() => review.mutate({ id: activeReport.id, status: "RESOLVED" })}
                      >
                        <CheckCircle2 size={15} /> Marcar como resolvida
                      </button>
                      {isLesson && (
                        <button
                          type="button"
                          className="button-danger"
                          disabled={review.isPending || !noteText.trim()}
                          onClick={() => setConfirming(activeReport.id)}
                        >
                          <Trash2 size={15} /> Remover vídeo e resolver
                        </button>
                      )}
                    </div>

                    {confirming === activeReport.id && (
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
                            onClick={() => review.mutate({ id: activeReport.id, status: "RESOLVED", removeLesson: true })}
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
                    <p className="whitespace-pre-wrap break-words text-ink leading-relaxed font-medium">Parecer: {activeReport.reviewNote}</p>
                    <p className="text-xs text-muted flex items-center gap-1 pt-1">
                      <Clock size={12} /> Analisado em {new Date(activeReport.reviewedAt).toLocaleString("pt-BR")}
                    </p>
                  </div>
                )}
              </div>
            );
          })()}
        </AppModal>
      )}

      <StatePagination page={page} totalPages={result.data?.page.totalPages} onChange={setPage} showPageNumber={false} />
    </div>
  );
}
