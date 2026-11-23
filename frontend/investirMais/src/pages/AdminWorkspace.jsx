import { useState } from "react";
import { Navigate, useRouteLoaderData } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Calendar, CheckCircle2, Clock, Download, Eye, FileCode, FileText, Upload, UserCheck, Users, XCircle } from "lucide-react";
import { Box, Button, IconButton, Paper, Stack, Tooltip, Typography } from "@mui/material";
import { DataTable, ListContextProvider, useList } from "react-admin";
import { apiClient, apiError } from "../services/api.js";
import { Field } from "../components/ui/FormFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import ModerationWorkspace from "./ModerationWorkspace.jsx";
import AdminNav from "../components/admin/AdminNav.jsx";
import AppModal from "../components/ui/AppModal.jsx";
import { markdownToSections, sectionsToMarkdown } from "../services/legalMarkdown.js";

function LegalWorkspace() {
  const client = useQueryClient();
  const current = useQuery({ queryKey: ["legal-editor"], queryFn: async () => (await apiClient.get("/api/legal/current")).data });
  const [draft, setDraft] = useState(null), [preview, setPreview] = useState(null);
  const [docModes, setDocModes] = useState({}); // documentId or index -> "markdown" | "sections"
  const [markdownTexts, setMarkdownTexts] = useState({}); // documentId or index -> string
  const publish = useMutation({ mutationFn: value => apiClient.post("/api/legal/versions", value), onSuccess: () => { setDraft(null); setPreview(null); return client.invalidateQueries(); } });
  const now = new Date();
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
  const value = draft ?? (current.data && {
    ...current.data,
    version: current.data.version + 1,
    publishedOn: today,
    documents: current.data.documents.filter(d => ["termos", "privacidade"].includes(d.id)),
  });
  function edit(change) { const next = structuredClone(value); change(next); setDraft(next); setPreview(null); }

  function handleImportMd(file, index) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (e) => {
      const content = e.target.result;
      const targetDoc = value.documents[index];
      const h1Match = content.match(/^#\s+(.+)$/m);
      const parsedSections = markdownToSections(content);
      edit((next) => {
        if (h1Match && h1Match[1].trim()) {
          next.documents[index].title = h1Match[1].trim();
        }
        next.documents[index].sections = parsedSections;
      });
      setMarkdownTexts((prev) => ({ ...prev, [targetDoc.id || index]: content }));
    };
    reader.readAsText(file);
  }

  return <>
    <p>A publicação preserva as versões anteriores e exige nova confirmação dos usuários. A plataforma mantém exclusivamente os Termos de Uso e a Política de Privacidade.</p>
    {current.error && <Notice error>{apiError(current.error, "Não foi possível carregar os documentos.")}</Notice>}
    {value && <form className="panel space-y-5 p-5" onSubmit={event => { event.preventDefault(); setPreview(structuredClone(value)); }}>
      <p className="font-bold">Nova versão: {value.version}</p>
      <Field label="Data da publicação" type="date" value={value.publishedOn} onChange={event => edit(next => { next.publishedOn = event.target.value; })} required max={today} />
      {value.documents.map((document, index) => {
        const isMarkdown = docModes[document.id || index] === "markdown";
        const currentMd = markdownTexts[document.id || index] ?? sectionsToMarkdown(document.sections);

        return (
          <fieldset key={index} className="space-y-3 rounded-xl border border-line p-4"><legend>Documento {index + 1}</legend>
            <div className="flex flex-wrap items-center justify-between gap-2 border-b border-line pb-2">
              <span className="text-xs text-muted font-medium">{document.label || document.title}</span>
              <div className="flex items-center gap-2">
                <label className="button-secondary !py-1 !px-2.5 !text-xs cursor-pointer flex items-center gap-1.5" title="Importar arquivo .md para este documento">
                  <Upload size={13} />
                  <span>Importar .md</span>
                  <input
                    type="file"
                    accept=".md,text/markdown"
                    className="hidden"
                    onChange={(e) => {
                      handleImportMd(e.target.files?.[0], index);
                      e.target.value = "";
                    }}
                  />
                </label>
                <button
                  type="button"
                  className={`button-secondary !py-1 !px-2.5 !text-xs flex items-center gap-1.5 ${isMarkdown ? "bg-brand/20 border-brand text-brand" : ""}`}
                  onClick={() => {
                    const nextMode = isMarkdown ? "sections" : "markdown";
                    setDocModes((prev) => ({ ...prev, [document.id || index]: nextMode }));
                    if (!isMarkdown) {
                      setMarkdownTexts((prev) => ({ ...prev, [document.id || index]: sectionsToMarkdown(document.sections) }));
                    }
                  }}
                >
                  <FileCode size={13} />
                  <span>{isMarkdown ? "Editar por Seções" : "Editar via Markdown (.md)"}</span>
                </button>
              </div>
            </div>

            <Field label="Identificador" value={document.id} required disabled hint="Identificador padrão do sistema." />
            <Field label="Nome da aba" value={document.label} required onChange={event => edit(next => { next.documents[index].label = event.target.value; })} />
            <Field label="Título do documento" value={document.title} fullWidth required onChange={event => edit(next => { next.documents[index].title = event.target.value; })} />

            {isMarkdown ? (
              <div className="space-y-2 border-t border-line pt-3">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold uppercase tracking-wider text-muted">Conteúdo do Documento (Markdown .md)</span>
                  <span className="text-xs text-muted">Use ## para títulos de seção</span>
                </div>
                <Field
                  label="Texto em Markdown"
                  value={currentMd}
                  multiline
                  minRows={10}
                  fullWidth
                  required
                  placeholder="## Título da Seção&#10;&#10;Texto descritivo da seção em markdown..."
                  onChange={(event) => {
                    const text = event.target.value;
                    setMarkdownTexts((prev) => ({ ...prev, [document.id || index]: text }));
                    const parsed = markdownToSections(text);
                    edit((next) => {
                      next.documents[index].sections = parsed;
                    });
                  }}
                />
              </div>
            ) : (
              document.sections.map((section, sectionIndex) => (
                <div key={sectionIndex} className="space-y-3 border-t border-line pt-3">
                  <Field label="Título da seção" value={section.heading} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].heading = event.target.value; })} />
                  <Field label="Texto da seção" value={section.content} multiline minRows={4} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].content = event.target.value; })} />
                  {document.sections.length > 1 && <button type="button" className="button-secondary !py-1 !text-xs" onClick={() => edit(next => { next.documents[index].sections.splice(sectionIndex, 1); })}>Remover seção</button>}
                </div>
              ))
            )}

            <div className="flex flex-wrap items-center gap-3 pt-2">
              {!isMarkdown && (
                <button type="button" className="button-secondary" onClick={() => edit(next => { next.documents[index].sections.push({ heading: "", content: "" }); })}>
                  Adicionar seção
                </button>
              )}
            </div>
          </fieldset>
        );
      })}
      <div className="flex flex-wrap items-center gap-3">
        <button className="button-primary">Revisar publicação</button>
      </div>
    </form>}
    {publish.error && <Notice error>{apiError(publish.error, "Não foi possível publicar.")}</Notice>}
    {publish.isSuccess && <Notice>Nova versão publicada. Confirme os documentos atualizados para continuar.</Notice>}
    {preview && <section className="panel space-y-4 p-5"><h2 className="font-bold">Revisão da versão {preview.version}</h2><p>Data: {preview.publishedOn}</p>
      {preview.documents.map((document, i) => <article key={i}><h3 className="font-semibold">{document.title}</h3>{document.sections.map((section, j) => <div key={j}><h4>{section.heading}</h4><p className="whitespace-pre-wrap text-sm text-muted">{section.content}</p></div>)}</article>)}
      <button className="button-primary" disabled={publish.isPending} onClick={() => publish.mutate(preview)}>{publish.isPending ? "Publicando…" : "Publicar versão e solicitar nova confirmação"}</button>
    </section>}
  </>;
}

function TeacherApplicationsTable({ list, statusBadges, onSelect }) {
  const listContext = useList({ data: list, perPage: 25 });
  return (
    <Paper variant="outlined" className="panel space-y-4 p-5 sm:p-6">
      <ListContextProvider value={listContext}>
        <Box sx={{ overflowX: "auto" }}>
          <DataTable
            resource="teacher-applications"
            bulkActionButtons={false}
            rowClick={false}
            sx={{ width: "100%", minWidth: 720, "& table": { width: "100%" } }}
            empty={<Typography color="text.secondary">Nenhuma candidatura encontrada.</Typography>}
          >
            <DataTable.Col
              source="userName"
              label="Candidato"
              render={(application) => {
                const initial = (application.userName || "U").charAt(0).toUpperCase();
                return (
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-amber-500/10 font-bold text-amber-400 border border-amber-500/30">
                      {initial}
                    </div>
                    <div className="min-w-0">
                      <span className="block font-semibold text-ink truncate">
                        {application.userName || "Apelido indisponível"}
                      </span>
                      <span className="block font-mono text-xs text-muted truncate">
                        ID: {application.id.slice(0, 8)}…
                      </span>
                    </div>
                  </div>
                );
              }}
            />
            <DataTable.Col
              source="status"
              label="Situação"
              render={(application) => {
                const badge = statusBadges[application.status] || {
                  label: application.status,
                  className: "border-line bg-canvas text-muted",
                  icon: Clock,
                };
                const StatusIcon = badge.icon;
                return (
                  <span className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold ${badge.className}`}>
                    <StatusIcon size={12} /> {badge.label}
                  </span>
                );
              }}
            />
            <DataTable.Col
              label="Documentos"
              render={(application) => (
                <span className="inline-flex items-center gap-1.5 text-xs text-muted">
                  <FileText size={14} className="text-brand" />
                  <strong className="text-ink">{application.documents?.length || 0}</strong> anexo(s)
                </span>
              )}
            />
            <DataTable.Col
              source="submittedAt"
              label="Data de Envio"
              render={(application) => (
                <span className="inline-flex items-center gap-1 text-xs text-muted">
                  <Calendar size={13} /> {new Date(application.submittedAt).toLocaleString("pt-BR")}
                </span>
              )}
            />
            <DataTable.Col
              label="Avaliação"
              align="right"
              render={(application) => {
                const isPending = application.status === "PENDING";
                return (
                  <Button
                    size="small"
                    startIcon={isPending ? <UserCheck size={15} /> : <FileText size={15} />}
                    onClick={() => onSelect(application)}
                  >
                    {isPending ? "Avaliar" : "Ver parecer"}
                  </Button>
                );
              }}
            />
          </DataTable>
        </Box>
      </ListContextProvider>
    </Paper>
  );
}

function TeacherWorkspace() {
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState("PENDING");
  const [selectedApplication, setSelectedApplication] = useState(null);
  const [reasons, setReasons] = useState({});
  const client = useQueryClient();

  const applications = useQuery({
    queryKey: ["teacher-applications", page],
    queryFn: async () => (await apiClient.get("/api/teacher-applications", { params: { page, size: 10 } })).data,
  });

  const review = useMutation({
    mutationFn: ({ id, approve }) =>
      apiClient.post(`/api/teacher-applications/${id}/review`, { approve, reason: reasons[id] }),
    onSuccess: () => {
      setSelectedApplication(null);
      return client.invalidateQueries({ queryKey: ["teacher-applications"] });
    },
  });

  const statusBadges = {
    PENDING: { label: "Pendente de análise", className: "border-amber-500/30 bg-amber-500/10 text-amber-400", icon: Clock },
    APPROVED: { label: "Professor aprovado", className: "border-emerald-500/30 bg-emerald-500/10 text-emerald-400", icon: CheckCircle2 },
    REJECTED: { label: "Candidatura reprovada", className: "border-rose-500/30 bg-rose-500/10 text-rose-400", icon: XCircle },
  };

  const rawList = applications.data?.content || [];
  const filteredList = rawList.filter((a) => a.status === statusFilter);

  // Manter o item selecionado atualizado se houver refetch
  const activeItem = selectedApplication
    ? rawList.find((a) => a.id === selectedApplication.id) || selectedApplication
    : null;

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-1.5 rounded-xl border border-line bg-canvas p-1">
          {[
            ["PENDING", "Pendentes", Clock],
            ["APPROVED", "Aprovadas", CheckCircle2],
            ["REJECTED", "Reprovadas", XCircle],
          ].map(([key, label, Icon]) => {
            const active = statusFilter === key;
            return (
              <button
                key={key}
                type="button"
                onClick={() => setStatusFilter(key)}
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

      {applications.isPending && (
        <div className="panel flex items-center justify-center p-12 text-muted">
          <p>Carregando candidaturas…</p>
        </div>
      )}

      {(applications.error || review.error) && (
        <Notice error>{apiError(applications.error || review.error, "Não foi possível analisar a candidatura.")}</Notice>
      )}

      {rawList.length === 0 && (
        <div className="panel flex flex-col items-center justify-center space-y-2 p-12 text-center text-muted">
          <UserCheck size={32} className="text-muted/60" />
          <p className="font-medium text-ink">Nenhuma candidatura recebida.</p>
          <p className="text-xs">Quando novos alunos solicitarem o papel de professor, os dados aparecerão aqui.</p>
        </div>
      )}

      {rawList.length > 0 && filteredList.length === 0 && (
        <div className="panel p-8 text-center text-muted">
          <p>Nenhuma candidatura com a situação selecionada nesta página.</p>
        </div>
      )}

      {filteredList.length > 0 && (
        <TeacherApplicationsTable
          list={filteredList}
          statusBadges={statusBadges}
          onSelect={setSelectedApplication}
        />
      )}

      {/* Modal de Detalhes e Ação da Candidatura */}
      {activeItem && (
        <AppModal
          title={
            activeItem.status === "PENDING"
              ? `Avaliação de Candidatura — ${activeItem.userName || "Professor"}`
              : `Parecer da Candidatura — ${activeItem.userName || "Professor"}`
          }
          width={640}
          onClose={() => setSelectedApplication(null)}
          busy={review.isPending}
        >
          <div className="space-y-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
              <div className="flex items-start gap-3 min-w-0">
                <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-amber-500/10 font-bold text-amber-400 border border-amber-500/30">
                  {(activeItem.userName || "U").charAt(0).toUpperCase()}
                </div>
                <div className="min-w-0 space-y-1">
                  <div className="flex flex-wrap items-center gap-2">
                    {(() => {
                      const badge = statusBadges[activeItem.status] || {
                        label: activeItem.status,
                        className: "border-line bg-canvas text-muted",
                        icon: Clock,
                      };
                      const StatusIcon = badge.icon;
                      return (
                        <span className={`inline-flex items-center gap-1 rounded-full border px-2.5 py-0.5 text-xs font-semibold ${badge.className}`}>
                          <StatusIcon size={12} /> {badge.label}
                        </span>
                      );
                    })()}
                  </div>
                  <h3 className="font-bold text-lg text-ink break-words">
                    {activeItem.userName || "Apelido indisponível"}
                  </h3>
                </div>
              </div>

              <div className="text-xs text-muted sm:text-right shrink-0">
                <p className="flex items-center gap-1 sm:justify-end">
                  <Calendar size={13} /> {new Date(activeItem.submittedAt).toLocaleString("pt-BR")}
                </p>
              </div>
            </div>

            <div className="space-y-2 rounded-xl border border-line bg-canvas p-4">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted">
                Documentos comprobatórios ({activeItem.documents?.length || 0}):
              </span>
              {activeItem.documents?.length === 0 ? (
                <p className="text-xs text-muted">
                  {activeItem.status === "APPROVED"
                    ? "Documentos eliminados com segurança após aprovação do professor (LGPD)."
                    : "Nenhum documento anexado."}
                </p>
              ) : (
                <div className="grid gap-2.5 sm:grid-cols-2">
                  {activeItem.documents.map((document) => (
                    <a
                      key={document.id}
                      className="flex items-center justify-between gap-3 rounded-xl border border-line bg-raised/50 p-3 transition-colors hover:border-brand/40 hover:bg-raised"
                      href={(apiClient.defaults.baseURL || "") + `/api/teacher-applications/${activeItem.id}/documents/${document.id}`}
                      target="_blank"
                      rel="noreferrer"
                    >
                      <div className="flex items-center gap-2.5 min-w-0">
                        <FileText size={18} className="shrink-0 text-brand" />
                        <span className="truncate text-sm font-medium text-ink underline">
                          Baixar {document.type.replaceAll("-", " ")}
                        </span>
                      </div>
                      <Download size={15} className="shrink-0 text-muted" />
                    </a>
                  ))}
                </div>
              )}
            </div>

            {activeItem.status === "PENDING" ? (
              <div className="space-y-3 border-t border-line pt-4">
                <Field
                  label="Parecer da análise"
                  multiline
                  fullWidth
                  minRows={3}
                  maxLength={2000}
                  placeholder="Informe a justificativa da decisão (obrigatório para aprovar ou reprovar)..."
                  value={reasons[activeItem.id] || ""}
                  onChange={(event) => setReasons((prev) => ({ ...prev, [activeItem.id]: event.target.value }))}
                  hint={`${(reasons[activeItem.id] || "").length}/2000 caracteres`}
                />
                <div className="flex flex-wrap gap-2.5 pt-1">
                  <button
                    type="button"
                    className="button-primary"
                    disabled={review.isPending || !(reasons[activeItem.id] || "").trim()}
                    onClick={() => review.mutate({ id: activeItem.id, approve: true })}
                  >
                    <UserCheck size={16} /> Aprovar professor
                  </button>
                  <button
                    type="button"
                    className="button-danger"
                    disabled={review.isPending || !(reasons[activeItem.id] || "").trim()}
                    onClick={() => review.mutate({ id: activeItem.id, approve: false })}
                  >
                    <XCircle size={16} /> Reprovar
                  </button>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-line bg-canvas p-4 space-y-1 text-sm border-l-4 border-l-brand">
                <span className="text-xs font-semibold uppercase tracking-wider text-muted">Parecer registrado:</span>
                <p className="whitespace-pre-wrap break-words text-ink leading-relaxed font-medium">{activeItem.reviewReason}</p>
                {activeItem.reviewedAt && (
                  <p className="text-xs text-muted flex items-center gap-1 pt-1">
                    <Clock size={12} /> Analisado em {new Date(activeItem.reviewedAt).toLocaleString("pt-BR")}
                  </p>
                )}
              </div>
            )}
          </div>
        </AppModal>
      )}

      <StatePagination page={page} totalPages={applications.data?.page.totalPages} onChange={setPage} showPageNumber={false} />
    </div>
  );
}

export default function AdminWorkspace({ kind }) {
  const user = useRouteLoaderData("app");
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;
  return (
    <div className="w-full space-y-5">
      <AdminNav />
      <PageHeader
        title={({ legal: "Documentos legais", teachers: "Candidaturas de professores", reports: "Denúncias" })[kind]}
        description={
          kind === "reports"
            ? "Analise denúncias de videoaulas e professores realizadas pelos alunos."
            : kind === "teachers"
            ? "Avalie os documentos e requisitos legais de candidatos ao papel de professor."
            : "Gerencie as versões e publicação de termos de uso e políticas de privacidade."
        }
      />
      {kind === "legal" ? <LegalWorkspace /> : kind === "reports" ? <ModerationWorkspace /> : <TeacherWorkspace />}
    </div>
  );
}
