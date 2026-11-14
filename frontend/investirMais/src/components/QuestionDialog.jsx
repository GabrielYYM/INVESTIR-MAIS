import { useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useFetcher, useLocation } from "react-router";
import { Notice } from "./PageState.jsx";
import QuestionFields from "./QuestionFields.jsx";
import AppModal from "./AppModal.jsx";
import { questionDefaults, questionPayload, questionSchema } from "../services/questionSchema.js";

export default function QuestionDialog({ item, remove, afterLesson = 0, position = 0, onClose }) {
  const fetcher = useFetcher();
  const location = useLocation();
  const initialLocation = useRef(location.key);
  const form = useForm({ defaultValues: questionDefaults(item, afterLesson, position), resolver: zodResolver(questionSchema) });
  const busy = fetcher.state !== "idle";
  useEffect(() => {
    if (location.key !== initialLocation.current || (!busy && fetcher.data?.ok)) onClose();
  }, [location.key, busy, fetcher.data, onClose]);
  const send = (value) => {
    fetcher.submit({ intent: `${remove ? "delete" : item ? "update" : "create"}-question`, id: item?.id,
      payload: remove ? undefined : questionPayload(value) }, {
      method: "post", encType: "application/json", action: location.pathname + location.search,
    });
  };
  const submit = remove ? (event) => { event.preventDefault(); send(); } : form.handleSubmit(send);
  return (
    <AppModal title={remove ? "Excluir pergunta" : item ? "Editar pergunta" : "Nova pergunta"} onClose={onClose} busy={busy} width={608}>
      <form onSubmit={submit} className="space-y-4 pt-4" noValidate>
        {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
        <fieldset disabled={busy} className="space-y-4">
          {remove ? <p className="text-sm leading-relaxed text-muted">A pergunta deixará de aparecer no curso e de bloquear o avanço. Deseja excluir <strong className="text-ink break-words">{item.prompt}</strong>?</p>
            : <QuestionFields form={form} />}
          {item && !remove && <p className="rounded-xl border border-amber-500/20 bg-amber-500/5 p-3 text-xs leading-relaxed text-muted">Ao salvar, os alunos precisarão acertar esta pergunta novamente.</p>}
        </fieldset>
        <div className="flex flex-wrap justify-end gap-2 border-t border-line pt-4">
          <button type="button" className="button-secondary" disabled={busy} onClick={onClose}>Cancelar</button>
          <button className={remove ? "button-danger" : "button-primary"} disabled={busy}>{busy ? "Processando…" : remove ? "Confirmar exclusão" : "Salvar pergunta"}</button>
        </div>
      </form>
    </AppModal>
  );
}
