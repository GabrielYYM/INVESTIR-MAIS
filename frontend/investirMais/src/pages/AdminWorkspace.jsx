import { useState } from "react";
import { Link, Navigate, useRouteLoaderData } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import { Notice } from "../components/ui/PageState.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import { TextField } from "@mui/material";
import ModerationWorkspace from "./ModerationWorkspace.jsx";
import TrashWorkspace from "./TrashWorkspace.jsx";

function AuditWorkspace() {
  const [filters, setFilters] = useState({});
  const [page, setPage] = useState(0);
  const result = useQuery({ queryKey: ["global-audit", filters, page], queryFn: async () => (await apiClient.get("/api/audit", { params: { ...filters, page, size: 20 } })).data });
  return <>
    <p className="text-sm text-muted">Filtros de usuário e papel referem-se ao autor da alteração. Revisões antigas podem não informar o autor.</p>
    <form className="panel flex flex-wrap gap-3 p-5" onSubmit={event => {
      event.preventDefault(); const values = Object.fromEntries(new FormData(event.currentTarget));
      for (const key of ["from", "to"]) if (values[key]) values[key] = new Date(values[key]).toISOString();
      setFilters(Object.fromEntries(Object.entries(values).filter(([, value]) => value))); setPage(0);
    }}>
      <label>Entidade<select name="entity" className="block rounded border border-line bg-canvas p-2"><option value="">Todas</option>{[["users", "Usuários"], ["courses", "Cursos"], ["lessons", "Aulas"], ["questions", "Questões"], ["reports", "Denúncias"], ["restorations", "Restaurações de conteúdo"], ["accounts", "Desativações e recuperações de contas"]].map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <TextField name="entityId" label="ID do registro" size="small" />
      <TextField name="revision" label="Revisão" type="number" slotProps={{ htmlInput: { min: 1 } }} size="small" />
      <TextField name="actor" label="Usuário autor (e-mail)" type="email" size="small" />
      <label>Papel do autor<select name="role" className="block rounded border border-line bg-canvas p-2"><option value="">Todos</option>{["ADMIN", "TEACHER", "STUDENT"].map(role => <option key={role}>{role}</option>)}</select></label>
      <TextField name="from" label="Desde" type="datetime-local" slotProps={{ inputLabel: { shrink: true } }} size="small" />
      <TextField name="to" label="Até" type="datetime-local" slotProps={{ inputLabel: { shrink: true } }} size="small" />
      <button className="button-primary">Filtrar</button>
    </form>
    {result.isPending && <p>Carregando auditoria…</p>}{result.error && <Notice error>{apiError(result.error, "Não foi possível consultar a auditoria.")}</Notice>}
    <p>{result.data?.totalItems ?? 0} registros</p>
    {result.data?.items.map(row => <details key={row.entity + row.id + row.revision} className="panel p-4"><summary className="cursor-pointer">Rev. {row.revision} · {row.entity} · {({ 0: "Criação", 1: "Alteração", 2: "Exclusão" })[row.operation]} · {new Date(Number(row.timestamp)).toLocaleString("pt-BR")} · {row.actor || "Autor não registrado"} ({row.role || "—"})</summary><pre className="mt-3 overflow-auto whitespace-pre-wrap break-words text-xs">{JSON.stringify(JSON.parse(row.data), null, 2)}</pre></details>)}
    <div className="flex gap-3"><button className="button-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button><span>Página {page + 1}</span><button className="button-secondary" disabled={!result.data || (page + 1) * 20 >= result.data.totalItems} onClick={() => setPage(page + 1)}>Próxima</button></div>
  </>;
}

function LegalWorkspace() {
  const client = useQueryClient();
  const current = useQuery({ queryKey: ["legal-editor"], queryFn: async () => (await apiClient.get("/api/legal/current")).data });
  const [draft, setDraft] = useState(null), [preview, setPreview] = useState(null);
  const publish = useMutation({ mutationFn: value => apiClient.post("/api/legal/versions", value), onSuccess: () => { setDraft(null); setPreview(null); return client.invalidateQueries(); } });
  const now = new Date();
  const today = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,"0")}-${String(now.getDate()).padStart(2,"0")}`;
  const value = draft ?? (current.data && { ...current.data, version: current.data.version + 1, publishedOn: today });
  function edit(change) { const next = structuredClone(value); change(next); setDraft(next); setPreview(null); }
  return <>
    <p>A publicação preserva as versões anteriores e exige nova confirmação dos usuários. Mantenha os termos e a política de privacidade e acrescente os novos documentos.</p>
    {current.error && <Notice error>{apiError(current.error, "Não foi possível carregar os documentos.")}</Notice>}
    {value && <form className="panel space-y-5 p-5" onSubmit={event => { event.preventDefault(); setPreview(structuredClone(value)); }}>
      <p className="font-bold">Nova versão: {value.version}</p>
      <TextField label="Data da publicação" type="date" value={value.publishedOn} onChange={event => edit(next => { next.publishedOn = event.target.value; })} required slotProps={{ inputLabel: { shrink: true }, htmlInput: { max: today } }} />
      {value.documents.map((document, index) => <fieldset key={index} className="space-y-3 rounded-xl border border-line p-4"><legend>Documento {index + 1}</legend>
        <TextField label="Identificador" value={document.id} required onChange={event => edit(next => { next.documents[index].id = event.target.value; })} helperText="Use letras minúsculas, números e hífens." slotProps={{ htmlInput: { pattern: "[a-z][a-z0-9-]{0,63}", maxLength: 64 } }} />
        <TextField label="Nome da aba" value={document.label} required onChange={event => edit(next => { next.documents[index].label = event.target.value; })} />
        <TextField label="Título do documento" value={document.title} fullWidth required onChange={event => edit(next => { next.documents[index].title = event.target.value; })} />
        {document.sections.map((section, sectionIndex) => <div key={sectionIndex} className="space-y-3 border-t border-line pt-3">
          <TextField label="Título da seção" value={section.heading} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].heading = event.target.value; })} />
          <TextField label="Texto da seção" value={section.content} multiline minRows={4} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].content = event.target.value; })} />
          {document.sections.length > 1 && <button type="button" className="button-secondary" onClick={() => edit(next => { next.documents[index].sections.splice(sectionIndex, 1); })}>Remover seção</button>}
        </div>)}
        <div className="flex gap-3"><button type="button" className="button-secondary" onClick={() => edit(next => { next.documents[index].sections.push({ heading: "", content: "" }); })}>Adicionar seção</button>
        {!["termos", "privacidade"].includes(document.id) && <button type="button" className="button-danger" onClick={() => edit(next => { next.documents.splice(index, 1); })}>Remover documento</button>}</div>
      </fieldset>)}
      <div className="flex flex-wrap gap-3"><button type="button" className="button-secondary" disabled={value.documents.length >= 20} onClick={() => edit(next => { next.documents.push({ id: "", label: "", title: "", sections: [{ heading: "", content: "" }] }); })}>Adicionar documento</button><button className="button-primary">Revisar publicação</button></div>
    </form>}
    {publish.error && <Notice error>{apiError(publish.error, "Não foi possível publicar.")}</Notice>}
    {publish.isSuccess && <Notice>Nova versão publicada. Confirme os documentos atualizados para continuar.</Notice>}
    {preview && <section className="panel space-y-4 p-5"><h2 className="font-bold">Revisão da versão {preview.version}</h2><p>Data: {preview.publishedOn}</p>
      {preview.documents.map((document, i) => <article key={i}><h3 className="font-semibold">{document.title}</h3>{document.sections.map((section, j) => <div key={j}><h4>{section.heading}</h4><p className="whitespace-pre-wrap text-sm text-muted">{section.content}</p></div>)}</article>)}
      <button className="button-primary" disabled={publish.isPending} onClick={() => publish.mutate(preview)}>{publish.isPending ? "Publicando…" : "Publicar versão e solicitar nova confirmação"}</button>
    </section>}
  </>;
}

function TeacherWorkspace() {
  const [page, setPage] = useState(0);
  const [reasons, setReasons] = useState({});
  const client = useQueryClient();
  const applications = useQuery({ queryKey: ["teacher-applications", page], queryFn: async () => (await apiClient.get("/api/teacher-applications", { params: { page, size: 10 } })).data });
  const review = useMutation({ mutationFn: ({ id, approve }) => apiClient.post(`/api/teacher-applications/${id}/review`, { approve, reason: reasons[id] }), onSuccess: () => client.invalidateQueries({ queryKey: ["teacher-applications"] }) });
  return <>
    {applications.isPending && <p>Carregando candidaturas…</p>}
    {(applications.error || review.error) && <Notice error>{apiError(applications.error || review.error, "Não foi possível analisar a candidatura.")}</Notice>}
    {applications.data?.content.length === 0 && <p>Nenhuma candidatura recebida.</p>}
    {applications.data?.content.map(application => <article key={application.id} className="panel space-y-3 p-5"><h2 className="font-bold">Usuário {application.userId}</h2><p>{application.status} · {new Date(application.submittedAt).toLocaleString("pt-BR")}</p>
      {application.documents.map(document => <a className="block text-brand underline" key={document.id} href={(apiClient.defaults.baseURL || "") + `/api/teacher-applications/${application.id}/documents/${document.id}`}>Baixar {document.type.replaceAll("-", " ")}</a>)}
      {application.status === "PENDING" ? <><TextField label="Parecer da análise" multiline fullWidth value={reasons[application.id] || ""} onChange={event => setReasons(previous => ({ ...previous, [application.id]: event.target.value }))} slotProps={{ htmlInput: { maxLength: 2000 } }} /><div className="flex gap-3">{[true, false].map(approve => <button key={String(approve)} className={approve ? "button-primary" : "button-danger"} disabled={review.isPending || !reasons[application.id]?.trim()} onClick={() => review.mutate({ id: application.id, approve })}>{approve ? "Aprovar professor" : "Reprovar"}</button>)}</div></> : <p>{application.reviewReason}</p>}
    </article>)}
    {applications.data?.page.totalPages > 1 && <div className="flex gap-3"><button className="button-secondary" disabled={page === 0} onClick={() => setPage(page - 1)}>Anterior</button><button className="button-secondary" disabled={page + 1 >= applications.data.page.totalPages} onClick={() => setPage(page + 1)}>Próxima</button></div>}
  </>;
}

export default function AdminWorkspace({ kind }) {
  const user = useRouteLoaderData("app");
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;
  return <><PageHeader title={({ audit: "Auditoria geral", legal: "Documentos legais", teachers: "Candidaturas de professores", reports: "Denúncias", trash: "Lixeira" })[kind]} /><Link className="button-secondary" to="/admin">Voltar à administração</Link>{kind === "audit" ? <AuditWorkspace /> : kind === "legal" ? <LegalWorkspace /> : kind === "reports" ? <ModerationWorkspace /> : kind === "trash" ? <TrashWorkspace /> : <TeacherWorkspace />}</>;
}
