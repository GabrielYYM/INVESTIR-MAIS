import { useMemo, useState } from "react";
import { ArrowLeft, CheckCircle2, ExternalLink, LockKeyhole, Pencil, Play, Plus, Trash2, Video } from "lucide-react";
import { Link, NavLink, useFetcher, useLoaderData, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from "react-router";
import EducationDialog from "../components/EducationDialog.jsx";
import QuestionDialog from "../components/QuestionDialog.jsx";
import CourseQuestions from "../components/CourseQuestions.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { Pagination } from "../components/EducationParts.jsx";
import { canManageEducation, pageHref } from "../services/educationData.js";
import { updateLessonProgress } from "../services/teacherService.js";
import { externalMediaUrl, youtubeVideo } from "../services/youtube.js";

export { courseLoader } from "../services/educationData.js";

function LessonPlayer({ lesson, isCompleted, onToggleCompleted, busy, trackProgress }) {
  const [showPlayer, setShowPlayer] = useState(false);
  const video = youtubeVideo(lesson.mediaUrl);
  const link = video?.watchUrl ?? externalMediaUrl(lesson.mediaUrl);

  const handleOpenVideo = () => {
    setShowPlayer(true);
    if (!isCompleted && onToggleCompleted) onToggleCompleted(true);
  };

  const handleOpenYouTube = () => {
    if (!isCompleted && onToggleCompleted) onToggleCompleted(true);
  };

  return (
    <article className="panel min-w-0 overflow-hidden">
      {video && showPlayer ? (
        <iframe
          className="aspect-video min-h-50 w-full border-0 bg-black"
          title={"Vídeo da aula " + lesson.title}
          src={video.embedUrl}
          referrerPolicy="strict-origin-when-cross-origin"
          allow="encrypted-media; picture-in-picture"
          allowFullScreen
        />
      ) : (
        <div className="grid min-h-44 place-items-center border-b border-white/5 bg-canvas px-5 py-8 sm:min-h-56">
          <span className="grid size-16 place-items-center rounded-2xl bg-amber-500/20 text-amber-400">
            <Video size={30} />
          </span>
        </div>
      )}
      <div className="space-y-3 p-5 sm:p-6">
        <div className="flex items-center justify-between gap-2">
          <p className="eyebrow">Videoaula</p>
          {trackProgress && isCompleted && (
            <span className="inline-flex items-center gap-1 text-xs font-semibold text-emerald-400">
              <CheckCircle2 size={15} /> Assistida
            </span>
          )}
        </div>
        <h2 className="text-2xl font-bold break-words">{lesson.title}</h2>
        <p className="text-sm leading-relaxed break-words whitespace-pre-wrap text-muted">{lesson.description}</p>
        <div className="flex flex-wrap items-center gap-2 pt-1">
          {link && (
            <>
              <a
                className="button-primary"
                href={link}
                target="_blank"
                rel="noopener noreferrer"
                onClick={handleOpenYouTube}
              >
                {video ? "Abrir no YouTube" : "Abrir vídeo"} <ExternalLink size={15} />
              </a>
              {video && !showPlayer && (
                <button className="button-secondary" onClick={handleOpenVideo}>
                  <Play size={16} /> Abrir player
                </button>
              )}
            </>
          )}
          {trackProgress && <button
            type="button"
            disabled={busy}
            onClick={() => onToggleCompleted && onToggleCompleted(!isCompleted)}
            className={`button-secondary !px-3 !text-xs sm:!text-sm ${
              isCompleted ? "!border-emerald-500/30 text-emerald-400" : ""
            }`}
          >
            <CheckCircle2 size={16} className={isCompleted ? "text-emerald-400" : "text-muted"} />
            <span>{busy ? "Atualizando…" : isCompleted ? "Aula concluída" : "Marcar como assistida"}</span>
          </button>}
        </div>
        {video && <p className="text-xs leading-relaxed text-muted">Vídeos privados devem ser abertos no YouTube com a conta convidada pelo professor.</p>}
        {!link && <p className="text-sm text-muted">O endereço do vídeo está indisponível.</p>}
      </div>
    </article>
  );
}

export default function Course() {
  const { course, page, lesson, progress, questions = [], questionsError, progressError, needsEnrollment } = useLoaderData();
  const user = useRouteLoaderData("app");
  const canManage = canManageEducation(user, course);
  const [editor, setEditor] = useState(null);
  const [questionEditor, setQuestionEditor] = useState(null);
  const [progressBusy, setProgressBusy] = useState(false);
  const [progressFailure, setProgressFailure] = useState(null);
  const revalidator = useRevalidator();
  const enroll = useFetcher();
  const location = useLocation();
  const navigate = useNavigate();
  const coursePath = "/educacao/" + course.id;

  const completedIds = useMemo(
    () => new Set(progress?.completedLessonIds ?? []),
    [progress?.completedLessonIds]
  );

  const handleToggleCompleted = async (lessonId, completed) => {
    if (progressBusy) return;
    setProgressBusy(true);
    setProgressFailure(null);
    try {
      await updateLessonProgress(course.id, lessonId, completed);
      await revalidator.revalidate();
    } catch (error) {
      if (error.response?.status === 401) {
        navigate("/entrar?" + new URLSearchParams({ retorno: location.pathname + location.search }), { replace: true });
      } else setProgressFailure(apiError(error, "Não foi possível atualizar o progresso. Tente novamente."));
    } finally {
      setProgressBusy(false);
    }
  };

  const isCurrentLessonCompleted = lesson ? completedIds.has(lesson.id) : false;

  return (
    <>
      <header className="space-y-3">
        <Link to="/educacao" className="inline-flex items-center gap-2 text-sm font-semibold text-brand hover:underline"><ArrowLeft size={16} /> Voltar ao catálogo</Link>
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="max-w-2xl min-w-0">
            <p className="eyebrow">Curso</p>
            <h1 className="mt-2 text-3xl font-bold break-words">{course.name}</h1>
            <p className="mt-3 text-sm leading-relaxed break-words whitespace-pre-wrap text-muted">{course.description}</p>
          </div>
          {canManage && (
            <div className="flex flex-wrap gap-2">
              <button className="button-secondary" onClick={() => setEditor({ kind: "curso", item: course })}><Pencil size={15} /> Editar curso</button>
              <button className="button-danger" onClick={() => setEditor({ kind: "curso", item: course, remove: true })}><Trash2 size={15} /> Excluir curso</button>
            </div>
          )}
        </div>
      </header>

      <div className="grid items-start gap-5 xl:grid-cols-[minmax(0,1fr)_minmax(18rem,0.62fr)]">
        <div className="min-w-0 space-y-5">
        {(progressFailure || progressError || enroll.data?.error) && <Notice error>{progressFailure || progressError || enroll.data.error}</Notice>}
        {progressError && <button className="button-secondary" disabled={revalidator.state !== "idle"} onClick={() => revalidator.revalidate()}>Tentar novamente</button>}
        {lesson?.locked ? <div className="panel space-y-3 p-6">
          <LockKeyhole size={28} className="text-brand" />
          <h2 className="text-xl font-bold">Aula bloqueada</h2>
          <p className="text-sm leading-relaxed text-muted">Conclua o conteúdo anterior e acerte as perguntas para liberar esta aula.</p>
          {(questions.length > 0 || needsEnrollment) && <a href="#perguntas" className="button-secondary">Ir para as perguntas</a>}
        </div> : lesson ? (
          <LessonPlayer
            key={lesson.id}
            lesson={lesson}
            isCompleted={isCurrentLessonCompleted}
            onToggleCompleted={canManage ? undefined : (completed) => handleToggleCompleted(lesson.id, completed)}
            busy={progressBusy}
            trackProgress={!canManage}
          />
        ) : (
          <p className="panel p-6 text-sm text-muted">
            {canManage ? "Adicione uma videoaula para começar." : "Este curso ainda não tem aulas cadastradas."}
          </p>
        )}
        <CourseQuestions questions={questions} canManage={canManage} error={questionsError}
          loading={revalidator.state !== "idle" || enroll.state !== "idle"} onRetry={() => revalidator.revalidate()}
          needsEnrollment={needsEnrollment}
          onEnroll={() => enroll.submit({ intent: "enroll-course" }, { method: "post", encType: "application/json", action: location.pathname + location.search })}
          onEdit={(options) => {
            const afterLesson = lesson?.position ?? 0;
            const position = questions.filter((q) => q.afterLesson === afterLesson).reduce((max, q) => Math.max(max, q.position), -1) + 1;
            setQuestionEditor({ afterLesson, position, ...options });
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
            {canManage && (
              <button className="button-primary" onClick={() => setEditor({ kind: "aula" })}>
                <Plus size={16} /> Nova aula
              </button>
            )}
          </div>

          {!canManage && progress && (progress.totalLessons > 0 || progress.totalQuestions > 0) && (
            <div className="space-y-1.5 rounded-xl border border-line bg-canvas p-3">
              <div className="flex items-center justify-between text-xs font-medium">
                <span className="text-muted">Seu progresso</span>
                <span className="font-bold text-emerald-400">
                  {progress.completedLessons} de {progress.totalLessons} aulas ({progress.progressPercentage}%)
                </span>
              </div>
              {progress.totalQuestions > 0 && <p className="text-xs text-muted">{progress.passedQuestions ?? 0} de {progress.totalQuestions} perguntas concluídas</p>}
              <div className="h-1.5 w-full overflow-hidden rounded-full bg-white/10">
                <div
                  className="h-full rounded-full bg-emerald-400 transition-all duration-500"
                  style={{ width: `${Math.min(100, progress.progressPercentage)}%` }}
                />
              </div>
            </div>
          )}

          {page.items.length > 0 && (
            <ul className="space-y-2">
              {page.items.map((item) => {
                const selected = item.id === lesson?.id;
                const completed = completedIds.has(item.id);
                return (
                  <li
                    key={item.id}
                    className={
                      "flex min-w-0 items-center gap-1 rounded-lg border p-1 " +
                      (selected ? "border-brand/35 bg-brand/8" : "border-line bg-canvas")
                    }
                  >
                    {item.locked && !canManage ? <span className="flex min-w-0 flex-1 items-center gap-2 p-2 text-sm text-muted" aria-disabled="true">
                      <LockKeyhole size={15} className="shrink-0" /><span className="min-w-0 break-words">{item.position > 0 && <span className="mr-1 text-xs">{item.position}.</span>}{item.title}</span>
                      <span className="sr-only"> (Bloqueada)</span>
                    </span> : <NavLink
                      end
                      to={pageHref(coursePath + "/" + item.id, page.number)}
                      aria-label={item.title}
                      aria-current={selected ? "true" : undefined}
                      className="flex min-w-0 flex-1 items-center gap-2 rounded-md p-2 text-sm font-semibold hover:text-brand"
                    >
                      <Play
                        size={15}
                        className={"shrink-0 " + (selected ? "text-brand" : "text-muted")}
                      />
                      <span className="break-words flex-1">{item.position > 0 && <span className="mr-1 text-xs text-muted">{item.position}.</span>}{item.title}</span>
                      {completed && (
                        <CheckCircle2
                          size={15}
                          className="shrink-0 text-emerald-400"
                          title="Aula concluída"
                        />
                      )}
                    </NavLink>}
                    {canManage && (
                      <div className="flex shrink-0 gap-1 pr-1">
                        <button
                          className="rounded-md p-2 text-muted hover:bg-raised hover:text-ink"
                          aria-label={"Editar aula " + item.title}
                          onClick={() => setEditor({ kind: "aula", item })}
                        >
                          <Pencil size={15} />
                        </button>
                        <button
                          className="rounded-md p-2 text-muted hover:bg-negative/10 hover:text-negative"
                          aria-label={"Excluir aula " + item.title}
                          onClick={() => setEditor({ kind: "aula", item, remove: true })}
                        >
                          <Trash2 size={15} />
                        </button>
                      </div>
                    )}
                  </li>
                );
              })}
            </ul>
          )}
          <Pagination page={page} pathname={coursePath} />
        </section>
      </div>
      {editor && canManage && (
        <EducationDialog {...editor} onClose={() => setEditor(null)} />
      )}
      {questionEditor && canManage && <QuestionDialog {...questionEditor} onClose={() => setQuestionEditor(null)} />}
    </>
  );
}
