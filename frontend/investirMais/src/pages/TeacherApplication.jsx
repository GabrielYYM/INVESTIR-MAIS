import { useState } from "react";
import { Link, useRouteLoaderData } from "react-router";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { FileText, X } from "lucide-react";
import { apiClient, apiError } from "../services/api.js";
import PageHeader from "../components/layout/PageHeader.jsx";
import { Notice } from "../components/ui/PageState.jsx";

export default function TeacherApplication() {
  const user = useRouteLoaderData("app");
  const client = useQueryClient();
  const [files, setFiles] = useState({});
  const requirements = useQuery({ queryKey: ["teacher-requirements"], queryFn: async () => (await apiClient.get("/api/teacher-applications/requirements")).data });
  const application = useQuery({ queryKey: ["teacher-application"], queryFn: async () => (await apiClient.get("/api/teacher-applications/mine")).data });

  const reqList = requirements.data || [];

  const handleFileChange = (key, event) => {
    const file = event.target.files?.[0] || null;
    setFiles((prev) => ({ ...prev, [key]: file }));
  };

  const handleClearFile = (key, inputId) => {
    setFiles((prev) => {
      const copy = { ...prev };
      delete copy[key];
      return copy;
    });
    const el = document.getElementById(inputId);
    if (el) el.value = "";
  };

  const submit = useMutation({
    mutationFn: async () => {
      const body = new FormData();
      // Documento(s) obrigatório(s) exigidos pela plataforma
      reqList.forEach((type) => {
        if (files[type]) {
          body.append("types", type);
          body.append("files", files[type]);
        }
      });
      // Documento oficial de identificação (se fornecido)
      if (files["documento-de-identificacao"]) {
        body.append("types", "documento-de-identificacao");
        body.append("files", files["documento-de-identificacao"]);
      }
      // Comprovante adicional (se fornecido)
      if (files["comprovante-adicional"]) {
        body.append("types", "comprovante-adicional");
        body.append("files", files["comprovante-adicional"]);
      }
      return apiClient.post("/api/teacher-applications", body);
    },
    onSuccess: () => {
      setFiles({});
      return client.invalidateQueries({ queryKey: ["teacher-application"] });
    },
  });

  const isFormValid =
    reqList.length > 0 &&
    reqList.every((type) => files[type] != null);

  const current = application.data;
  return (
    <>
      <PageHeader
        title="Cadastro de professor"
        description="Envie um comprovante de qualificação profissional e um documento oficial de identificação para análise da sua candidatura."
      />
      {(requirements.error || application.error || submit.error) && (
        <Notice error>{apiError(requirements.error || application.error || submit.error, "Não foi possível carregar ou enviar a candidatura.")}</Notice>
      )}
      {current && (
        <section className="panel space-y-3 p-5">
          <h2 className="font-bold">
            Situação: {({ PENDING: "Em análise", APPROVED: "Aprovada", REJECTED: "Reprovada" })[current.status]}
          </h2>
          {current.reviewReason && <p>{current.reviewReason}</p>}
          <div className="space-y-2 pt-1">
            <span className="text-xs font-semibold uppercase tracking-wider text-muted">
              Documentos enviados ({current.documents?.length || 0}):
            </span>
            {current.documents?.length === 0 ? (
              <p className="text-xs text-muted">
                {current.status === "APPROVED"
                  ? "Seus documentos comprobatórios foram eliminados com segurança após a aprovação da sua candidatura (LGPD)."
                  : "Nenhum documento anexado."}
              </p>
            ) : (
              <div className="grid gap-2 sm:grid-cols-2">
                {current.documents.map((document) => (
                  <a
                    key={document.id}
                    className="flex items-center gap-2 rounded-lg border border-line bg-canvas p-2.5 text-sm text-brand underline hover:text-amber-300"
                    href={(apiClient.defaults.baseURL || "") + `/api/teacher-applications/${current.id}/documents/${document.id}`}
                    target="_blank"
                    rel="noreferrer"
                  >
                    <FileText size={16} /> Baixar {document.type.replaceAll("-", " ")}
                  </a>
                ))}
              </div>
            )}
          </div>
        </section>
      )}
      {user.role === "STUDENT" && (!current || current.status === "REJECTED") && requirements.data && (
        <form
          noValidate
          className="panel space-y-5 p-5"
          onSubmit={(event) => {
            event.preventDefault();
            if (isFormValid) submit.mutate();
          }}
        >
          <div className="rounded-xl border border-line bg-canvas/70 p-4 space-y-2.5 text-sm">
            <p className="font-semibold text-ink">Orientações para envio dos documentos:</p>
            <ul className="list-disc list-inside space-y-1.5 text-xs text-muted leading-relaxed">
              <li>
                <strong className="text-ink">Comprovante de qualificação:</strong> anexe um documento que comprove seu direito e aptidão para ser professor.
              </li>
              <li>
                <strong className="text-ink">Documento de identificação:</strong> anexe um documento oficial com foto que comprove sua identidade.
              </li>
              <li>
                Formatos aceitos: <strong>PDF, PNG ou JPEG</strong> (até 5 MB por documento, podendo enviar até 3 documentos no total).
              </li>
              <li>
                Seus documentos serão analisados exclusivamente pelos administradores e eliminados com segurança após a aprovação (LGPD).
              </li>
            </ul>
          </div>

          <div className="space-y-4">
            {/* Campo 1: Comprovante de qualificação (obrigatório) */}
            {reqList.map((type) => (
              <label className="block space-y-2" key={type} htmlFor={`file-${type}`}>
                <span className="flex items-center justify-between text-sm font-medium text-ink">
                  <span>{type.replaceAll("-", " ")}</span>
                  <span className="text-xs font-semibold text-amber-400">(Obrigatório)</span>
                </span>
                <input
                  id={`file-${type}`}
                  required
                  type="file"
                  aria-label={type}
                  accept="application/pdf,image/png,image/jpeg"
                  onChange={(event) => handleFileChange(type, event)}
                />
              </label>
            ))}

            {/* Campo 2: Documento oficial de identificação (prova de que ele é ele) */}
            <div className="block space-y-2">
              <label className="flex items-center justify-between text-sm font-medium text-ink" htmlFor="file-doc-id">
                <span>Documento oficial de identificação</span>
                <span className="text-xs font-semibold text-muted">(Opcional)</span>
              </label>
              <div className="flex items-center gap-2">
                <input
                  id="file-doc-id"
                  type="file"
                  aria-label="Documento oficial de identificação"
                  accept="application/pdf,image/png,image/jpeg"
                  className="flex-1"
                  onChange={(event) => handleFileChange("documento-de-identificacao", event)}
                />
                {files["documento-de-identificacao"] && (
                  <button
                    type="button"
                    title="Remover arquivo de identificação"
                    className="flex items-center gap-1 rounded-lg border border-line px-2 py-1 text-xs text-rose-400 hover:bg-rose-500/10"
                    onClick={() => handleClearFile("documento-de-identificacao", "file-doc-id")}
                  >
                    <X size={13} /> Limpar
                  </button>
                )}
              </div>
            </div>

            {/* Campo 3: Comprovante adicional (até 3 documentos no total) */}
            <div className="block space-y-2">
              <label className="flex items-center justify-between text-sm font-medium text-ink" htmlFor="file-doc-extra">
                <span>Comprovante adicional</span>
                <span className="text-xs font-semibold text-muted">(Opcional)</span>
              </label>
              <div className="flex items-center gap-2">
                <input
                  id="file-doc-extra"
                  type="file"
                  aria-label="Comprovante adicional"
                  accept="application/pdf,image/png,image/jpeg"
                  className="flex-1"
                  onChange={(event) => handleFileChange("comprovante-adicional", event)}
                />
                {files["comprovante-adicional"] && (
                  <button
                    type="button"
                    title="Remover comprovante adicional"
                    className="flex items-center gap-1 rounded-lg border border-line px-2 py-1 text-xs text-rose-400 hover:bg-rose-500/10"
                    onClick={() => handleClearFile("comprovante-adicional", "file-doc-extra")}
                  >
                    <X size={13} /> Limpar
                  </button>
                )}
              </div>
            </div>
          </div>

          <div className="pt-2">
            <button
              className="button-primary"
              disabled={submit.isPending || !isFormValid}
            >
              {submit.isPending ? "Enviando…" : "Enviar candidatura"}
            </button>
          </div>
        </form>
      )}
      <Link className="button-secondary" to="/">
        Voltar ao painel
      </Link>
    </>
  );
}
