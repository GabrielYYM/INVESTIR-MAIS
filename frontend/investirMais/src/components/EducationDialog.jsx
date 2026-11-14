import { useEffect, useRef } from "react";
import { Plus, Trash2 } from "lucide-react";
import { useFieldArray, useForm } from "react-hook-form";
import { useFetcher, useLocation } from "react-router";
import { Field } from "./FormFields.jsx";
import { Notice } from "./PageState.jsx";
import { youtubeVideo } from "../services/youtube.js";
import QuestionFields from "./QuestionFields.jsx";
import AppModal from "./AppModal.jsx";
import { zodResolver } from "@hookform/resolvers/zod";
import { questionDefaults, questionPayload } from "../services/questionSchema.js";
import { educationSchema } from "../services/educationSchema.js";

export default function EducationDialog({ kind, item, remove, onClose }) {
  const fetcher = useFetcher();
  const location = useLocation();
  const initialLocation = useRef(location.key);
  const lesson = kind === "aula";
  const form = useForm({
    resolver: remove ? undefined : zodResolver(educationSchema(lesson, item?.mediaUrl)),
    defaultValues: {
      title: (lesson ? item?.title : item?.name) ?? "",
      description: item?.description ?? "",
      mediaUrl: item?.mediaUrl ?? "",
      questions: [],
    },
  });
  const { fields: questions, append, remove: removeQuestion } = useFieldArray({ control: form.control, name: "questions" });
  const { errors } = form.formState;
  const isSubmitting = fetcher.state !== "idle";

  useEffect(() => {
    if (location.key !== initialLocation.current || (!isSubmitting && fetcher.data?.ok)) {
      onClose();
    }
  }, [location.key, isSubmitting, fetcher.data, onClose]);

  const submit = form.handleSubmit(({ title, description, mediaUrl, questions }) => {
    const operation = remove ? "delete" : item ? "update" : "create";
    const video = lesson && !remove ? youtubeVideo(mediaUrl) : null;
    const payload = lesson
      ? { title, description, mediaUrl: video?.watchUrl ?? mediaUrl, thumbnailUrl: video?.thumbnailUrl ?? item?.thumbnailUrl }
      : { name: title, description };
    if (!lesson && !item && questions.length) payload.questions = questions.map(questionPayload);

    return fetcher.submit({ intent: `${operation}-${kind}`, id: item?.id, payload }, {
      method: "post",
      encType: "application/json",
      action: location.pathname + location.search,
    });
  });

  return (
    <AppModal title={remove ? "Excluir " + kind : item ? "Editar " + kind : lesson ? "Nova aula" : "Novo curso"}
      onClose={onClose} busy={isSubmitting}>
      <form onSubmit={submit} className="space-y-4 pt-4" noValidate>
        {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
        {remove ? (
          <p className="text-sm leading-relaxed text-muted">
            {lesson ? "A aula deixará de aparecer no curso." : "O curso e suas aulas deixarão de aparecer no catálogo."} Deseja excluir <strong className="text-ink">{item.title || item.name}</strong>?
          </p>
        ) : (
          <>
            <Field label={lesson ? "Título da aula" : "Nome do curso"} maxLength={100} error={errors.title?.message}
              {...form.register("title")} />
            <Field label="Descrição" multiline rows={4} error={errors.description?.message} {...form.register("description")} />
            {lesson && (
              <Field
                label="URL do vídeo no YouTube"
                type="url"
                placeholder="https://www.youtube.com/watch?v=…"
                hint="Envie o vídeo pelo YouTube Studio e cole o link. Se for privado, convide os alunos pelo YouTube Studio."
                error={errors.mediaUrl?.message}
                {...form.register("mediaUrl")}
              />
            )}
            {!lesson && !item && <section className="space-y-4 border-t border-line pt-4" aria-label="Perguntas do novo curso">
              <div className="space-y-1"><h3 className="font-semibold">Perguntas do curso</h3><p className="text-xs leading-relaxed text-muted">Opcional: adicione agora ou depois de cadastrar as aulas.</p></div>
              {questions.map((question, index) => <fieldset key={question.id} className="min-w-0 space-y-3 rounded-xl border border-line bg-canvas p-3" disabled={isSubmitting}>
                <legend className="px-1 text-sm font-semibold">Pergunta {index + 1}</legend>
                <QuestionFields form={form} name={`questions.${index}`} />
                <button type="button" className="button-danger" onClick={() => removeQuestion(index)}><Trash2 size={15} /> Remover pergunta {index + 1}</button>
              </fieldset>)}
              <button type="button" className="button-secondary" disabled={isSubmitting || questions.length >= 100}
                onClick={() => append(questionDefaults(undefined, 0, questions.length))}><Plus size={15} /> Adicionar pergunta</button>
            </section>}
          </>
        )}
        <div className="flex flex-wrap justify-end gap-2 border-t border-line pt-4">
          <button type="button" className="button-secondary" disabled={isSubmitting} onClick={onClose}>Cancelar</button>
          <button className={remove ? "button-danger" : "button-primary"} disabled={isSubmitting}>
            {isSubmitting ? "Processando…" : remove ? "Confirmar exclusão" : "Salvar " + kind}
          </button>
        </div>
      </form>
    </AppModal>
  );
}
