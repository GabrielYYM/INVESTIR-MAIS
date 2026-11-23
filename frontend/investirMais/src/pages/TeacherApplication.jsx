import { useState } from "react";
import { Link, useRouteLoaderData } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { apiClient, apiError } from "../services/api.js";
import PageHeader from "../components/layout/PageHeader.jsx";
import { Notice } from "../components/ui/PageState.jsx";

export default function TeacherApplication() {
  const user = useRouteLoaderData("app");
  const client = useQueryClient();
  const [files, setFiles] = useState({});
  const requirements = useQuery({ queryKey: ["teacher-requirements"], queryFn: async () => (await apiClient.get("/api/teacher-applications/requirements")).data });
  const application = useQuery({ queryKey: ["teacher-application"], queryFn: async () => (await apiClient.get("/api/teacher-applications/mine")).data });
  const submit = useMutation({ mutationFn: async () => {
    const body = new FormData();
    requirements.data.forEach(type => { body.append("types", type); body.append("files", files[type]); });
    return apiClient.post("/api/teacher-applications", body);
  }, onSuccess: () => { setFiles({}); return client.invalidateQueries({ queryKey: ["teacher-application"] }); } });
  const current = application.data;
  return <>
    <PageHeader title="Cadastro de professor" description="Envie os documentos exigidos para que a administração analise sua candidatura." />
    {(requirements.error || application.error || submit.error) && <Notice error>{apiError(requirements.error || application.error || submit.error, "Não foi possível carregar ou enviar a candidatura.")}</Notice>}
    {current && <section className="panel space-y-3 p-5"><h2 className="font-bold">Situação: {({ PENDING: "Em análise", APPROVED: "Aprovada", REJECTED: "Reprovada" })[current.status]}</h2>
      {current.reviewReason && <p>{current.reviewReason}</p>}
      {current.documents.map(document => <a key={document.id} className="block text-brand underline" href={(apiClient.defaults.baseURL || "") + `/api/teacher-applications/${current.id}/documents/${document.id}`}>Baixar {document.type.replaceAll("-", " ")}</a>)}
    </section>}
    {user.role === "STUDENT" && (!current || current.status === "REJECTED") && requirements.data && <form noValidate className="panel space-y-5 p-5" onSubmit={event => { event.preventDefault(); if (requirements.data.every(type => files[type])) submit.mutate(); }}>
      <p className="text-sm text-muted">PDF, PNG ou JPEG, até 5 MB por documento. Os arquivos ficam disponíveis para você e para administradores responsáveis pela análise.</p>
      {requirements.data.map(type => <label className="block space-y-2" key={type}><span className="block">{type.replaceAll("-", " ")}</span><input required type="file" accept="application/pdf,image/png,image/jpeg" onChange={event => setFiles(previous => ({ ...previous, [type]: event.target.files[0] }))} /></label>)}
      <button className="button-primary" disabled={submit.isPending || requirements.data.some(type => !files[type])}>{submit.isPending ? "Enviando…" : "Enviar candidatura"}</button>
    </form>}
    <Link className="button-secondary" to="/">Voltar ao painel</Link>
  </>;
}
