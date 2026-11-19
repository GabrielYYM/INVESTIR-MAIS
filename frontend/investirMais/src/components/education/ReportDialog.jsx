import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import AppModal from "../ui/AppModal.jsx";
import { Notice } from "../ui/PageState.jsx";
import SubmitButton from "../ui/SubmitButton.jsx";
import { apiClient, apiError } from "../../services/api.js";
import { reportReasons } from "../../services/reportReasons.js";

export default function ReportDialog({ targetType, targetId, courseId, title, onClose }) {
  const [reason, setReason] = useState("INAPPROPRIATE");
  const [description, setDescription] = useState("");
  const report = useMutation({ mutationFn: () => apiClient.post("/api/reports", { targetType, targetId, courseId, reason, description: description.trim() }) });
  return <AppModal title={targetType === "LESSON" ? "Denunciar vídeo" : "Denunciar professor"} onClose={onClose} busy={report.isPending}>
    {report.isSuccess ? <div className="space-y-4"><Notice>Denúncia enviada para análise do administrador.</Notice><p className="text-xs text-muted">Protocolo: {report.data.data.id}</p><button className="button-primary" onClick={onClose}>Concluir</button></div> : <form className="space-y-4" onSubmit={event => { event.preventDefault(); if (description.trim()) report.mutate(); }}>
      <p className="break-words">{title}</p><p className="text-sm text-muted">A denúncia será analisada pelo administrador. Sua identificação não será exibida ao professor.</p>
      <label className="block">Motivo<select className="block w-full rounded border border-line bg-canvas p-2" value={reason} onChange={event => setReason(event.target.value)}>{Object.entries(reportReasons).map(([value, label]) => <option key={value} value={value}>{label}</option>)}</select></label>
      <label className="block">Descreva o problema<textarea className="block w-full rounded border border-line bg-canvas p-2" value={description} onChange={event => setDescription(event.target.value)} required maxLength={2000} rows={4} /></label>
      {report.error && <Notice error>{apiError(report.error, "Não foi possível enviar a denúncia.")}</Notice>}
      <SubmitButton loading={report.isPending} disabled={!description.trim()}>Enviar denúncia</SubmitButton>
    </form>}
  </AppModal>;
}
