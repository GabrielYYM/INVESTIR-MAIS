import { useState } from "react";
import { Link, Navigate, useRouteLoaderData } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import { Field } from "../components/ui/FormFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import StatePagination from "../components/ui/StatePagination.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import ModerationWorkspace from "./ModerationWorkspace.jsx";
import TrashWorkspace from "./TrashWorkspace.jsx";

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
      <Field label="Data da publicação" type="date" value={value.publishedOn} onChange={event => edit(next => { next.publishedOn = event.target.value; })} required max={today} />
      {value.documents.map((document, index) => <fieldset key={index} className="space-y-3 rounded-xl border border-line p-4"><legend>Documento {index + 1}</legend>
        <Field label="Identificador" value={document.id} required onChange={event => edit(next => { next.documents[index].id = event.target.value; })} hint="Use letras minúsculas, números e hífens." maxLength={64} slotProps={{ htmlInput: { pattern: "[a-z][a-z0-9-]{0,63}", maxLength: 64 } }} />
        <Field label="Nome da aba" value={document.label} required onChange={event => edit(next => { next.documents[index].label = event.target.value; })} />
        <Field label="Título do documento" value={document.title} fullWidth required onChange={event => edit(next => { next.documents[index].title = event.target.value; })} />
        {document.sections.map((section, sectionIndex) => <div key={sectionIndex} className="space-y-3 border-t border-line pt-3">
          <Field label="Título da seção" value={section.heading} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].heading = event.target.value; })} />
          <Field label="Texto da seção" value={section.content} multiline minRows={4} fullWidth required onChange={event => edit(next => { next.documents[index].sections[sectionIndex].content = event.target.value; })} />
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
      {application.status === "PENDING" ? <><Field label="Parecer da análise" multiline fullWidth maxLength={2000} value={reasons[application.id] || ""} onChange={event => setReasons(previous => ({ ...previous, [application.id]: event.target.value }))} /><div className="flex gap-3">{[true, false].map(approve => <button key={String(approve)} className={approve ? "button-primary" : "button-danger"} disabled={review.isPending || !reasons[application.id]?.trim()} onClick={() => review.mutate({ id: application.id, approve })}>{approve ? "Aprovar professor" : "Reprovar"}</button>)}</div></> : <p>{application.reviewReason}</p>}
    </article>)}
    <StatePagination page={page} totalPages={applications.data?.page.totalPages} onChange={setPage} showPageNumber={false} />
  </>;
}

export default function AdminWorkspace({ kind }) {
  const user = useRouteLoaderData("app");
  if (user.role !== "ADMIN") return <Navigate to="/" replace />;
  return <><PageHeader title={({ legal: "Documentos legais", teachers: "Candidaturas de professores", reports: "Denúncias", trash: "Lixeira" })[kind]} /><Link className="button-secondary" to="/admin">Voltar à administração</Link>{kind === "legal" ? <LegalWorkspace /> : kind === "reports" ? <ModerationWorkspace /> : kind === "trash" ? <TrashWorkspace /> : <TeacherWorkspace />}</>;
}
