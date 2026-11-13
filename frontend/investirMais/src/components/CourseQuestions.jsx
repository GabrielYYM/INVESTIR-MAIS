import { useId, useState } from "react";
import { CheckCircle2, CircleHelp, LockKeyhole, Pencil, Plus, Trash2 } from "lucide-react";
import { useFetcher, useLocation } from "react-router";
import { Notice } from "./PageState.jsx";

function StudentQuestion({ question }) {
  const [selected, setSelected] = useState(null);
  const name = useId();
  const fetcher = useFetcher();
  const location = useLocation();
  const busy = fetcher.state !== "idle";
  const passed = question.passed;
  const submit = (event) => {
    event.preventDefault();
    if (selected === null || busy) return;
    fetcher.submit({ intent: "answer-question", id: question.id, payload: { optionIndex: selected } }, {
      method: "post", encType: "application/json", action: location.pathname + location.search,
    });
  };
  return (
    <form onSubmit={submit} className="space-y-3">
      <fieldset disabled={busy || question.locked || passed} className="space-y-2">
        <legend className="sr-only">{question.prompt}</legend>
        {question.options.map((text, index) => (
          <label key={index} className={"flex min-w-0 items-start gap-3 rounded-xl border px-3 py-3 text-sm transition-colors " +
            (selected === index ? "border-brand/50 bg-brand/8" : "border-line bg-canvas") +
            (question.locked || passed ? " text-muted" : " cursor-pointer hover:border-brand/35")}>
            <input type="radio" name={name} value={index} checked={selected === index} onChange={() => setSelected(index)}
              className="mt-0.5 size-4 shrink-0 accent-amber-500" required />
            <span className="min-w-0 break-words whitespace-pre-wrap">{text}</span>
          </label>
        ))}
      </fieldset>
      {passed ? <p role="status" className="flex items-center gap-2 text-sm font-semibold text-positive"><CheckCircle2 size={17} /> Pergunta concluída</p>
        : question.locked ? <p className="flex items-start gap-2 text-xs leading-relaxed text-muted"><LockKeyhole size={15} className="shrink-0" /> Conclua as aulas e acerte as perguntas anteriores para responder.</p>
          : <>
            {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
            {fetcher.data?.passed === false && <Notice error>Ainda não é a alternativa correta. Escolha outra opção e tente novamente.</Notice>}
            <button className="button-primary" disabled={busy || selected === null}>{busy ? "Verificando…" : "Responder pergunta"}</button>
          </>}
    </form>
  );
}

export default function CourseQuestions({ questions, canManage, onEdit, error, loading, onRetry, needsEnrollment, onEnroll }) {
  if (!canManage && !questions.length && !error && !needsEnrollment) return null;
  return (
    <section className="panel min-w-0 space-y-5 p-5 sm:p-6" aria-labelledby="perguntas-titulo" id="perguntas">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="eyebrow">Pratique o que aprendeu</p>
          <h2 id="perguntas-titulo" className="mt-1 text-xl font-bold">Perguntas do curso</h2>
        </div>
        {canManage && <button className="button-primary" onClick={() => onEdit({})}><Plus size={16} /> Nova pergunta</button>}
      </header>
      {error ? <div className="space-y-3"><Notice error>{error}</Notice><button className="button-secondary" disabled={loading} onClick={onRetry}>Tentar novamente</button></div>
        : needsEnrollment ? <div className="space-y-3"><p className="text-sm leading-relaxed text-muted">Entre no curso para responder às perguntas e liberar os próximos conteúdos.</p><button className="button-primary" disabled={loading} onClick={onEnroll}>{loading ? "Entrando…" : "Começar curso"}</button></div>
          : questions.length ? <div className="space-y-4">{questions.map((question, index) => (
            <article key={question.id} className="min-w-0 space-y-4 rounded-xl border border-line bg-raised p-4" aria-labelledby={"question-" + question.id}>
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 space-y-2">
                  <p className="flex flex-wrap items-center gap-2 text-xs text-muted"><CircleHelp size={15} className="text-brand" /> Pergunta {index + 1}<span aria-hidden="true">·</span>{question.afterLesson === 0 ? "Antes da primeira aula" : "Após a aula " + question.afterLesson}</p>
                  <h3 id={"question-" + question.id} className="font-semibold break-words whitespace-pre-wrap">{question.prompt}</h3>
                </div>
                {canManage && <div className="flex shrink-0 gap-1">
                  <button className="rounded-lg p-2 text-muted hover:bg-raised hover:text-ink" aria-label={"Editar pergunta " + question.prompt} onClick={() => onEdit({ item: question })}><Pencil size={16} /></button>
                  <button className="rounded-lg p-2 text-muted hover:bg-negative/10 hover:text-negative" aria-label={"Excluir pergunta " + question.prompt} onClick={() => onEdit({ item: question, remove: true })}><Trash2 size={16} /></button>
                </div>}
              </div>
              {canManage ? <ul className="space-y-2">{question.options.map((text, option) => (
                <li key={option} className={"flex items-start gap-2 rounded-lg border px-3 py-2 text-sm " + (option === question.correctOptionIndex ? "border-emerald-500/25 bg-emerald-500/5 text-positive" : "border-line text-muted")}>
                  {option === question.correctOptionIndex && <CheckCircle2 size={16} className="mt-0.5 shrink-0" />}<span className="min-w-0 break-words">{text}</span>{option === question.correctOptionIndex && <span className="sr-only"> (Correta)</span>}
                </li>))}</ul> : <StudentQuestion key={JSON.stringify([question.prompt, question.options])} question={question} />}
            </article>
          ))}</div> : <p className="text-sm text-muted">Adicione perguntas de múltipla escolha para os alunos praticarem durante o curso.</p>}
    </section>
  );
}
