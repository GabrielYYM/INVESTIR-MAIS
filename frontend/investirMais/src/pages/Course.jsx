import { ArrowLeft, CheckCircle2, CircleHelp, ExternalLink, LockKeyhole, Pencil, Play, Plus, Trash2, Video } from "lucide-react";
import { lazy, Suspense, useState } from "react";
import { Radio, RadioGroup } from "@mui/material";
import { Link, useFetcher, useLoaderData, useLocation, useNavigate, useRevalidator, useRouteLoaderData, NavLink } from "react-router";
import { useUpdateLessonProgress } from "../generated/education.ts";
import { apiError } from "../services/api.js";
import { canManageEducation } from "../services/educationData.js";
import { pageHref } from "../services/searchParams.js";
import { externalMediaUrl, youtubeVideo } from "../services/youtube.js";
import { Notice } from "../components/ui/PageState.jsx";
import SubmitButton from "../components/ui/SubmitButton.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import { Pagination } from "../components/education/EducationParts.jsx";
const EducationDialog = lazy(() => import("../components/education/EducationDialog.jsx"));
const QuestionDialog = lazy(() => import("../components/education/QuestionDialog.jsx"));
export { courseLoader } from "../services/educationData.js";
function StudentQuestion({
  question
}) {
  const [selected, setSelected] = useState(null);
  const fetcher = useFetcher();
  const location = useLocation();
  const busy = fetcher.state !== "idle";
  const submit = event => {
    event.preventDefault();
    if (selected === null || busy) return;
    fetcher.submit({
      intent: "answer-question",
      id: question.id,
      payload: {
        optionIndex: selected
      }
    }, {
      method: "post",
      encType: "application/json",
      action: location.pathname + location.search
    });
  };
  return <form onSubmit={submit} className="space-y-3">
    <fieldset disabled={busy || question.locked || question.passed} className="min-w-0">
      <legend className="sr-only">{question.prompt}</legend>
      <RadioGroup className="space-y-2" value={selected ?? ""} onChange={(_, value) => setSelected(Number(value))}>{question.options.map((text, index) => <label key={index} className={"flex cursor-pointer items-start gap-3 rounded-xl border px-3 py-2.5 text-sm " + (selected === index ? "border-brand/50 bg-brand/5 text-ink" : "border-line text-muted")}>
        <Radio value={index} disabled={busy || question.locked || question.passed} sx={{ p: 0, mt: .25, "& svg": { fontSize: 16 } }} /><span className="min-w-0 break-words">{text}</span>
      </label>)}</RadioGroup>
    </fieldset>
    {question.passed ? <p className="flex items-center gap-2 text-sm font-semibold text-positive"><CheckCircle2 size={16} />Pergunta concluída</p> : question.locked ? <p className="text-sm text-muted">Conclua as aulas e acerte as perguntas anteriores para responder.</p> : <>
      {fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}{fetcher.data?.passed === false && <Notice error>Ainda não é a alternativa correta. Escolha outra opção e tente novamente.</Notice>}
      <SubmitButton loading={busy} disabled={selected === null} loadingText="Verificando…">Responder pergunta</SubmitButton>
    </>}
  </form>;
}
function CourseQuestions({
  questions,
  canManage,
  onEdit,
  error,
  loading,
  onRetry,
  needsEnrollment,
  onEnroll
}) {
  if (!canManage && !questions.length && !error && !needsEnrollment) return null;
  return <section className="panel min-w-0 space-y-5 p-5 sm:p-6" aria-labelledby="perguntas-titulo" id="perguntas">
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p className="eyebrow">Pratique o que aprendeu</p>
          <h2 id="perguntas-titulo" className="mt-1 text-xl font-bold">Perguntas do curso</h2>
        </div>
        {canManage && <button className="button-primary" onClick={() => onEdit({})}><Plus size={16} /> Nova pergunta</button>}
      </header>
      {error ? <div className="space-y-3"><Notice error>{error}</Notice><button className="button-secondary" disabled={loading} onClick={onRetry}>Tentar novamente</button></div> : needsEnrollment ? <div className="space-y-3"><p className="text-sm leading-relaxed text-muted">Entre no curso para responder às perguntas e liberar os próximos conteúdos.</p><SubmitButton type="button" loading={loading} loadingText="Entrando…" onClick={onEnroll}>Começar curso</SubmitButton></div> : questions.length ? <div className="space-y-4">{questions.map((question, index) => <article key={question.id} className="min-w-0 space-y-4 rounded-xl border border-line bg-raised p-4" aria-labelledby={"question-" + question.id}>
              <div className="flex items-start justify-between gap-3">
                <div className="min-w-0 space-y-2">
                  <p className="flex flex-wrap items-center gap-2 text-xs text-muted"><CircleHelp size={15} className="text-brand" /> Pergunta {index + 1}<span aria-hidden="true">·</span>{question.afterLesson === 0 ? "Antes da primeira aula" : "Após a aula " + question.afterLesson}</p>
                  <h3 id={"question-" + question.id} className="font-semibold break-words whitespace-pre-wrap">{question.prompt}</h3>
                </div>
                {canManage && <div className="flex shrink-0 gap-1">
                  <button className="rounded-lg p-2 text-muted hover:bg-raised hover:text-ink" aria-label={"Editar pergunta " + question.prompt} onClick={() => onEdit({
              item: question
            })}><Pencil size={16} /></button>
                  <button className="rounded-lg p-2 text-muted hover:bg-negative/10 hover:text-negative" aria-label={"Excluir pergunta " + question.prompt} onClick={() => onEdit({
              item: question,
              remove: true
            })}><Trash2 size={16} /></button>
                </div>}
              </div>
              {canManage ? <ul className="space-y-2">{question.options.map((text, option) => <li key={option} className={"flex items-start gap-2 rounded-lg border px-3 py-2 text-sm " + (option === question.correctOptionIndex ? "border-emerald-500/25 bg-emerald-500/5 text-positive" : "border-line text-muted")}>
                  {option === question.correctOptionIndex && <CheckCircle2 size={16} className="mt-0.5 shrink-0" />}<span className="min-w-0 break-words">{text}</span>{option === question.correctOptionIndex && <span className="sr-only"> (Correta)</span>}
                </li>)}</ul> : <StudentQuestion key={JSON.stringify([question.prompt, question.options])} question={question} />}
            </article>)}</div> : <p className="text-sm text-muted">Adicione perguntas de múltipla escolha para os alunos praticarem durante o curso.</p>}
    </section>;
}
function LessonPlayer({
  lesson,
  isCompleted,
  onToggleCompleted,
  busy,
  trackProgress
}) {
  const [showPlayer, setShowPlayer] = useState(false);
  const video = youtubeVideo(lesson.mediaUrl);
  const link = video?.watchUrl ?? externalMediaUrl(lesson.mediaUrl);
  const complete = () => {
    if (!isCompleted) onToggleCompleted?.(true);
  };
  return <article className="panel min-w-0 overflow-hidden">
    {video && showPlayer ? <iframe className="aspect-video min-h-50 w-full border-0 bg-black" title={"Vídeo da aula " + lesson.title} src={video.embedUrl} referrerPolicy="strict-origin-when-cross-origin" allow="encrypted-media; picture-in-picture" allowFullScreen /> : <div className="grid min-h-44 place-items-center border-b border-white/5 bg-canvas px-5 py-8 sm:min-h-56">
      <span className="grid size-16 place-items-center rounded-2xl bg-amber-500/20 text-amber-400">
        <Video size={30} />
      </span>
    </div>}
    <div className="space-y-3 p-5 sm:p-6">
      <div className="flex items-center justify-between gap-2">
        <p className="eyebrow">Videoaula</p>
        {trackProgress && isCompleted && <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-400">
          <CheckCircle2 size={15} /> Assistida
        </span>}
      </div>
      <h2 className="text-2xl font-bold break-words">{lesson.title}</h2>
      <p className="text-sm leading-relaxed break-words whitespace-pre-wrap text-muted">{lesson.description}</p>
      <div className="flex flex-wrap items-center gap-2 pt-1">
        {link && <>
          <a className="button-primary" href={link} target="_blank" rel="noopener noreferrer" onClick={complete}>
            {video ? "Abrir no YouTube" : "Abrir vídeo"} <ExternalLink size={15} />
          </a>
          {video && !showPlayer && <button className="button-secondary" onClick={() => {
            setShowPlayer(true);
            complete();
          }}>
            <Play size={16} /> Abrir player
          </button>}
        </>}
        {trackProgress && <button type="button" disabled={busy} onClick={() => onToggleCompleted && onToggleCompleted(!isCompleted)} className={`button-secondary !px-3 !text-xs sm:!text-sm ${isCompleted ? "!border-emerald-500/30 text-emerald-400" : ""}`}>
          <CheckCircle2 size={16} className={isCompleted ? "text-emerald-400" : "text-muted"} />
          <span>{busy ? "Atualizando…" : isCompleted ? "Aula concluída" : "Marcar como assistida"}</span>
        </button>}
      </div>
      {video && <p className="text-xs leading-relaxed text-muted">Vídeos privados devem ser abertos no YouTube com a conta convidada pelo professor.</p>}
      {!link && <p className="text-sm text-muted">O endereço do vídeo está indisponível.</p>}
    </div>
  </article>;
}
export default function Course() {
  const {
    course,
    page,
    lesson,
    progress,
    questions = [],
    questionsError,
    progressError,
    needsEnrollment
  } = useLoaderData();
  const user = useRouteLoaderData("app");
  const canManage = canManageEducation(user, course);
  const [editor, setEditor] = useState(null);
  const [questionEditor, setQuestionEditor] = useState(null);
  const revalidator = useRevalidator();
  const enroll = useFetcher();
  const location = useLocation();
  const navigate = useNavigate();
  const coursePath = "/educacao/" + course.id;
  const completedIds = new Set(progress?.completedLessonIds ?? []);
  const progressMutation = useUpdateLessonProgress({
    mutation: {
      retry: false,
      onSuccess: () => revalidator.revalidate(),
      onError: error => {
        if (error.response?.status === 401) navigate("/entrar?" + new URLSearchParams({
          retorno: location.pathname + location.search
        }), {
          replace: true
        });
      }
    }
  });
  const updateProgress = completed => {
    if (!progressMutation.isPending) progressMutation.mutate({
      courseId: course.id,
      lessonId: lesson.id,
      data: {
        completed
      }
    });
  };
  const failure = progressMutation.error ? apiError(progressMutation.error, "Não foi possível atualizar o progresso. Tente novamente.") : progressError || enroll.data?.error;
  return <>
    <PageHeader
      backLink={<Link to="/educacao" className="inline-flex items-center gap-2 text-sm font-semibold text-brand hover:underline"><ArrowLeft size={16} /> Voltar ao catálogo</Link>}
      eyebrow="Curso"
      title={course.name}
      description={course.description}
      actions={canManage && <div className="flex flex-wrap gap-2">
        <button className="button-secondary" onClick={() => setEditor({
          kind: "curso",
          item: course
        })}><Pencil size={15} /> Editar curso</button>
        <button className="button-danger" onClick={() => setEditor({
          kind: "curso",
          item: course,
          remove: true
        })}><Trash2 size={15} /> Excluir curso</button>
      </div>}
    />

    <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(18rem,0.62fr)]">
      <div className="min-w-0 space-y-5">
        {failure && <Notice error>{failure}</Notice>}
        {progressError && <button className="button-secondary" disabled={revalidator.state !== "idle"} onClick={() => revalidator.revalidate()}>Tentar novamente</button>}
        {lesson?.locked ? <div className="panel space-y-3 p-6">
          <LockKeyhole size={28} className="text-brand" />
          <h2 className="text-xl font-bold">Aula bloqueada</h2>
          <p className="text-sm leading-relaxed text-muted">Conclua o conteúdo anterior e acerte as perguntas para liberar esta aula.</p>
          {(questions.length > 0 || needsEnrollment) && <a href="#perguntas" className="button-secondary">Ir para as perguntas</a>}
        </div> : lesson ? <LessonPlayer key={lesson.id} lesson={lesson} isCompleted={completedIds.has(lesson.id)} onToggleCompleted={canManage ? undefined : completed => updateProgress(completed)} busy={progressMutation.isPending} trackProgress={!canManage} /> : <p className="panel p-6 text-sm text-muted">
          {canManage ? "Adicione uma videoaula para começar." : "Este curso ainda não tem aulas cadastradas."}
        </p>}
        <CourseQuestions questions={questions} canManage={canManage} error={questionsError} loading={revalidator.state !== "idle" || enroll.state !== "idle"} onRetry={() => revalidator.revalidate()} needsEnrollment={needsEnrollment} onEnroll={() => enroll.submit({
          intent: "enroll-course"
        }, {
          method: "post",
          encType: "application/json",
          action: location.pathname + location.search
        })} onEdit={options => {
          const afterLesson = lesson?.position ?? 0;
          const position = questions.filter(q => q.afterLesson === afterLesson).reduce((max, q) => Math.max(max, q.position), -1) + 1;
          setQuestionEditor({
            afterLesson,
            position,
            ...options
          });
        }} />
        {!canManage && progress?.totalQuestions > 0 && progress.progressPercentage === 100 && <Notice>Curso concluído! Você concluiu todas as aulas e perguntas.</Notice>}
      </div>
      <section className="panel min-w-0 space-y-4 p-4 sm:p-5" aria-labelledby="aulas-titulo">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div>
            <p className="eyebrow">Conteúdo</p>
            <h2 id="aulas-titulo" className="mt-1 text-xl font-bold">
              Aulas <span className="ml-1 text-sm text-muted">{page.totalItems}</span>
            </h2>
          </div>
          {canManage && <button className="button-primary" onClick={() => setEditor({
            kind: "aula"
          })}>
            <Plus size={16} /> Nova aula
          </button>}
        </div>

        {!canManage && progress && (progress.totalLessons > 0 || progress.totalQuestions > 0) && <div className="space-y-1.5 rounded-xl border border-line bg-canvas p-3">
          <div className="flex items-center justify-between text-xs font-medium">
            <span className="text-muted">Seu progresso</span>
            <span className="font-bold text-emerald-400">
              {progress.completedLessons} de {progress.totalLessons} aulas ({progress.progressPercentage}%)
            </span>
          </div>
          {progress.totalQuestions > 0 && <p className="text-xs text-muted">{progress.passedQuestions ?? 0} de {progress.totalQuestions} perguntas concluídas</p>}
          <div className="h-1.5 w-full overflow-hidden rounded-full bg-white/10">
            <div className="h-full rounded-full bg-emerald-400 transition-all duration-500" style={{
              width: `${Math.min(100, progress.progressPercentage)}%`
            }} />
          </div>
        </div>}

        {page.items.length > 0 && <ul className="space-y-2">
          {page.items.map(item => {
            const selected = item.id === lesson?.id;
            const completed = completedIds.has(item.id);
            return <li key={item.id} className={"flex min-w-0 items-center gap-1 rounded-lg border p-1 " + (selected ? "border-brand/35 bg-brand/8" : "border-line bg-canvas")}>
              {item.locked && !canManage ? <span className="flex min-w-0 flex-1 items-center gap-2 p-2 text-sm text-muted" aria-disabled="true">
                <LockKeyhole size={15} className="shrink-0" /><span className="min-w-0 break-words">{item.position > 0 && <span className="mr-1 text-xs">{item.position}.</span>}{item.title}</span>
                <span className="sr-only"> (Bloqueada)</span>
              </span> : <NavLink end to={pageHref(coursePath + "/" + item.id, page.number)} aria-label={item.title} aria-current={selected ? "true" : undefined} className="flex min-w-0 flex-1 items-center gap-2 rounded-md p-2 text-sm font-semibold hover:text-brand">
                <Play size={15} className={"shrink-0 " + (selected ? "text-brand" : "text-muted")} />
                <span className="break-words flex-1">{item.position > 0 && <span className="mr-1 text-xs text-muted">{item.position}.</span>}{item.title}</span>
                {completed && <CheckCircle2 size={15} className="shrink-0 text-emerald-400" title="Aula concluída" />}
              </NavLink>}
              {canManage && <div className="flex shrink-0 gap-1 pr-1">
                <button className="rounded-md p-2 text-muted hover:bg-raised hover:text-ink" aria-label={"Editar aula " + item.title} onClick={() => setEditor({
                  kind: "aula",
                  item
                })}>
                  <Pencil size={15} />
                </button>
                <button className="rounded-md p-2 text-muted hover:bg-negative/10 hover:text-negative" aria-label={"Excluir aula " + item.title} onClick={() => setEditor({
                  kind: "aula",
                  item,
                  remove: true
                })}>
                  <Trash2 size={15} />
                </button>
              </div>}
            </li>;
          })}
        </ul>}
        <Pagination page={page} pathname={coursePath} />
      </section>
    </div>
    {editor && canManage && <Suspense fallback={null}><EducationDialog {...editor} onClose={() => setEditor(null)} /></Suspense>}
    {questionEditor && canManage && <Suspense fallback={null}><QuestionDialog {...questionEditor} onClose={() => setQuestionEditor(null)} /></Suspense>}
  </>;
}
