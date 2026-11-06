import { useEffect, useId, useRef } from "react";
import { X } from "lucide-react";
import { useForm } from "react-hook-form";
import { useRevalidator } from "react-router";
import { Field } from "./FormFields.jsx";
import { Notice } from "./PageState.jsx";
import { apiError } from "../services/api.js";
import { youtubeVideo } from "../services/youtube.js";

export default function EducationDialog({ kind, item, remove, onClose, onSave }) {
  const dialog = useRef(null);
  const headingId = useId();
  const { revalidate } = useRevalidator();
  const lesson = kind === "aula";
  const form = useForm({
    defaultValues: {
      title: (lesson ? item?.title : item?.name) ?? "",
      description: item?.description ?? "",
      mediaUrl: item?.mediaUrl ?? "",
    },
  });
  const { errors, isSubmitting } = form.formState;

  useEffect(() => {
    const element = dialog.current;
    element.showModal();
    return () => element.close();
  }, []);

  const submit = form.handleSubmit(async ({ title, description, mediaUrl }) => {
    try {
      if (remove) {
        await onSave();
      } else if (lesson) {
        const video = youtubeVideo(mediaUrl);
        await onSave({
          title,
          description,
          mediaUrl: video?.watchUrl ?? mediaUrl,
          thumbnailUrl: video?.thumbnailUrl ?? item.thumbnailUrl,
        });
      } else {
        await onSave({ name: title, description });
      }
      onClose();
    } catch (error) {
      form.setError("root", { message: apiError(error, "Não foi possível concluir a operação. Tente novamente.") });
      if (error.response?.status === 401) await revalidate();
    }
  });

  return (
    <dialog
      ref={dialog}
      aria-labelledby={headingId}
      className="panel fixed inset-0 m-auto max-h-[calc(100dvh-2rem)] w-[min(32rem,calc(100%-2rem))] overflow-auto p-0 text-ink backdrop:bg-black/65"
      onCancel={(event) => { event.preventDefault(); if (!isSubmitting) onClose(); }}
    >
      <header className="flex items-center justify-between gap-3 border-b border-line px-5 py-4 sm:px-6">
        <h2 id={headingId} className="text-xl font-bold">
          {remove ? "Excluir " + kind : item ? "Editar " + kind : lesson ? "Nova aula" : "Novo curso"}
        </h2>
        <button type="button" onClick={onClose} disabled={isSubmitting} aria-label="Fechar" className="rounded-md p-2 text-muted hover:bg-raised hover:text-ink"><X size={18} /></button>
      </header>
      <form onSubmit={submit} className="space-y-4 p-5 sm:p-6" noValidate>
        {errors.root && <Notice error>{errors.root.message}</Notice>}
        {remove ? (
          <p className="text-sm leading-relaxed text-muted">
            {lesson ? "A aula deixará de aparecer no curso." : "O curso e suas aulas deixarão de aparecer no catálogo."} Deseja excluir <strong className="text-ink">{item.title || item.name}</strong>?
          </p>
        ) : (
          <>
            <Field label={lesson ? "Título da aula" : "Nome do curso"} maxLength={100} error={errors.title?.message}
              {...form.register("title", {
                setValueAs: (value) => value.trim(),
                required: "Informe um título.",
                minLength: { value: 3, message: "Informe pelo menos 3 caracteres." },
              })} />
            <Field label="Descrição" multiline rows={4} error={errors.description?.message}
              {...form.register("description", { setValueAs: (value) => value.trim(), required: "Informe uma descrição." })} />
            {lesson && (
              <Field
                label="URL do vídeo no YouTube"
                type="url"
                placeholder="https://www.youtube.com/watch?v=…"
                hint="Envie o vídeo pelo YouTube Studio e cole o link. Se for privado, convide os alunos pelo YouTube Studio."
                error={errors.mediaUrl?.message}
                {...form.register("mediaUrl", {
                  setValueAs: (value) => value.trim(),
                  required: "Informe a URL do vídeo.",
                  validate: (value) => Boolean(youtubeVideo(value) || value === item?.mediaUrl) || "Informe o link de um vídeo do YouTube.",
                })}
              />
            )}
          </>
        )}
        <div className="flex flex-wrap justify-end gap-2 border-t border-line pt-4">
          <button type="button" className="button-secondary" disabled={isSubmitting} onClick={onClose}>Cancelar</button>
          <button className={remove ? "button-danger" : "button-primary"} disabled={isSubmitting}>
            {isSubmitting ? "Processando…" : remove ? "Confirmar exclusão" : "Salvar " + kind}
          </button>
        </div>
      </form>
    </dialog>
  );
}
