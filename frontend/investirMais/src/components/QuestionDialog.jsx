import { useEffect, useId, useRef, useState } from "react";
import { X } from "lucide-react";
import { useFetcher, useLocation } from "react-router";
import { Notice } from "./PageState.jsx";
import QuestionFields, { newQuestion, questionPayload, validateQuestion } from "./QuestionFields.jsx";

export default function QuestionDialog({ item, remove, afterLesson = 0, position = 0, onClose }) {
  const dialog = useRef(null);
  const titleId = useId();
  const fetcher = useFetcher();
  const location = useLocation();
  const initialLocation = useRef(location.key);
  const [value, setValue] = useState(() => item ? questionPayload(item) : newQuestion(afterLesson, position));
  const [error, setError] = useState(null);
  const busy = fetcher.state !== "idle";
  useEffect(() => {
    const element = dialog.current;
    element.showModal();
    return () => element.close();
  }, []);
  useEffect(() => {
    if (location.key !== initialLocation.current || (!busy && fetcher.data?.ok)) onClose();
  }, [location.key, busy, fetcher.data, onClose]);
  const submit = (event) => {
    event.preventDefault();
    const message = remove ? null : validateQuestion(value);
    setError(message);
    if (message) return;
    fetcher.submit({ intent: `${remove ? "delete" : item ? "update" : "create"}-question`, id: item?.id,
      payload: remove ? undefined : questionPayload(value) }, {
      method: "post", encType: "application/json", action: location.pathname + location.search,
    });
  };
  return (
    <dialog ref={dialog} aria-labelledby={titleId}
      className="panel fixed inset-0 m-auto max-h-[calc(100dvh-2rem)] w-[min(38rem,calc(100%-2rem))] overflow-auto p-0 text-ink backdrop:bg-black/65"
      onCancel={(event) => { event.preventDefault(); if (!busy) onClose(); }}>
      <header className="flex items-center justify-between gap-3 border-b border-line px-5 py-4 sm:px-6">
        <h2 id={titleId} className="text-xl font-bold">{remove ? "Excluir pergunta" : item ? "Editar pergunta" : "Nova pergunta"}</h2>
        <button type="button" onClick={onClose} disabled={busy} aria-label="Fechar" className="rounded-md p-2 text-muted hover:bg-raised hover:text-ink"><X size={18} /></button>
      </header>
      <form onSubmit={submit} className="space-y-4 p-5 sm:p-6" noValidate>
        {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
        <fieldset disabled={busy} className="space-y-4">
          {remove ? <p className="text-sm leading-relaxed text-muted">A pergunta deixará de aparecer no curso e de bloquear o avanço. Deseja excluir <strong className="text-ink break-words">{item.prompt}</strong>?</p>
            : <QuestionFields value={value} onChange={setValue} error={error} />}
          {item && !remove && <p className="rounded-xl border border-amber-500/20 bg-amber-500/5 p-3 text-xs leading-relaxed text-muted">Ao salvar, os alunos precisarão acertar esta pergunta novamente.</p>}
        </fieldset>
        <div className="flex flex-wrap justify-end gap-2 border-t border-line pt-4">
          <button type="button" className="button-secondary" disabled={busy} onClick={onClose}>Cancelar</button>
          <button className={remove ? "button-danger" : "button-primary"} disabled={busy}>{busy ? "Processando…" : remove ? "Confirmar exclusão" : "Salvar pergunta"}</button>
        </div>
      </form>
    </dialog>
  );
}
