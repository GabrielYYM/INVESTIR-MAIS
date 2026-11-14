import { lazy, Suspense, useState } from "react";
import { Box, Button, Card, Chip, LinearProgress, List, ListItem, ListItemButton, ListItemText, Stack, Typography } from "@mui/material";
import { Link, useFetcher, useLoaderData, useLocation, useNavigate, useRevalidator, useRouteLoaderData } from "react-router";
import { useUpdateLessonProgress } from "../generated/education.ts";
import { apiError } from "../services/api.js";
import { canManageEducation } from "../services/educationData.js";
import { pageHref } from "../services/searchParams.js";
import { externalMediaUrl, youtubeVideo } from "../services/youtube.js";
import { Notice } from "../components/PageState.jsx";
import { Pagination } from "../components/EducationParts.jsx";
import CourseQuestions from "../components/CourseQuestions.jsx";
const EducationDialog = lazy(() => import("../components/EducationDialog.jsx"));
const QuestionDialog = lazy(() => import("../components/QuestionDialog.jsx"));
export { courseLoader } from "../services/educationData.js";
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
  return <Card component="article"><Stack spacing={2} sx={{
      p: 3
    }}>
    {video && showPlayer && <Box component="iframe" title={"Vídeo da aula " + lesson.title} src={video.embedUrl} referrerPolicy="strict-origin-when-cross-origin" allow="encrypted-media; picture-in-picture" allowFullScreen sx={{
        width: "100%",
        aspectRatio: "16/9",
        border: 0,
        bgcolor: "black"
      }} />}
    <Typography variant="overline">Videoaula</Typography><Typography variant="h2">{lesson.title}</Typography><Typography color="text.secondary" sx={{
        whiteSpace: "pre-wrap"
      }}>{lesson.description}</Typography>
    {trackProgress && isCompleted && <Chip label="Assistida" color="success" sx={{
        alignSelf: "start"
      }} />}
    <Stack direction="row" sx={{
        gap: 1,
        flexWrap: "wrap"
      }}>
      {link && <Button component="a" href={link} target="_blank" rel="noopener noreferrer" onClick={complete}>{video ? "Abrir no YouTube" : "Abrir vídeo"}</Button>}
      {video && !showPlayer && <Button onClick={() => {
          setShowPlayer(true);
          complete();
        }}>Abrir player</Button>}
      {trackProgress && <Button disabled={busy} onClick={() => onToggleCompleted?.(!isCompleted)}>{busy ? "Atualizando…" : isCompleted ? "Aula concluída" : "Marcar como assistida"}</Button>}
    </Stack>{video && <Typography variant="caption">Vídeos privados devem ser abertos no YouTube com a conta convidada pelo professor.</Typography>}
    {!link && <Typography>O endereço do vídeo está indisponível.</Typography>}
  </Stack></Card>;
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
  return <Stack spacing={3}><Button component={Link} to="/educacao" sx={{
      alignSelf: "start"
    }}>Voltar ao catálogo</Button>
    <Stack direction="row" sx={{
      justifyContent: "space-between",
      gap: 2,
      flexWrap: "wrap"
    }}><Box><Typography variant="overline" color="primary">Curso</Typography><Typography variant="h1">{course.name}</Typography><Typography color="text.secondary">{course.description}</Typography></Box>
      {canManage && <Stack direction="row" spacing={1}><Button onClick={() => setEditor({
          kind: "curso",
          item: course
        })}>Editar curso</Button><Button color="error" onClick={() => setEditor({
          kind: "curso",
          item: course,
          remove: true
        })}>Excluir curso</Button></Stack>}
    </Stack><Box sx={{
      display: "grid",
      gridTemplateColumns: {
        xs: "1fr",
        lg: "minmax(0,1.6fr) minmax(300px,1fr)"
      },
      gap: 3,
      alignItems: "start"
    }}><Stack spacing={3}>
      {failure && <Notice error>{failure}</Notice>}{progressError && <Button disabled={revalidator.state !== "idle"} onClick={() => revalidator.revalidate()}>Tentar novamente</Button>}
      {lesson?.locked ? <Card sx={{
          p: 3
        }}><Stack spacing={2}><Typography variant="h2">Aula bloqueada</Typography><Typography>Conclua o conteúdo anterior e acerte as perguntas para liberar esta aula.</Typography>{(questions.length > 0 || needsEnrollment) && <Button component="a" href="#perguntas">Ir para as perguntas</Button>}</Stack></Card> : lesson ? <LessonPlayer key={lesson.id} lesson={lesson} isCompleted={completedIds.has(lesson.id)} onToggleCompleted={canManage ? undefined : updateProgress} busy={progressMutation.isPending} trackProgress={!canManage} /> : <Typography>{canManage ? "Adicione uma videoaula para começar." : "Este curso ainda não tem aulas cadastradas."}</Typography>}
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
    </Stack><Card component="section" aria-labelledby="aulas-titulo" sx={{
        p: 2
      }}><Stack spacing={2}>
      <Stack direction="row" sx={{
            justifyContent: "space-between",
            alignItems: "center"
          }}><Typography variant="h2" id="aulas-titulo">Aulas {page.totalItems}</Typography>{canManage && <Button onClick={() => setEditor({
              kind: "aula"
            })}>Nova aula</Button>}</Stack>
      {!canManage && progress && (progress.totalLessons > 0 || progress.totalQuestions > 0) && <Stack spacing={1}><Typography>Seu progresso</Typography><Typography>{progress.completedLessons} de {progress.totalLessons} aulas ({progress.progressPercentage}%)</Typography>
        {progress.totalQuestions > 0 && <Typography>{progress.passedQuestions ?? 0} de {progress.totalQuestions} perguntas concluídas</Typography>}<LinearProgress variant="determinate" value={Math.min(100, progress.progressPercentage)} /></Stack>}
      <List>{page.items.map(item => <ListItem key={item.id} disablePadding sx={{
              gap: 1,
              mb: 1
            }}>
        {item.locked && !canManage ? <ListItemText primary={`${item.position > 0 ? item.position + ". " : ""}${item.title} (Bloqueada)`} aria-disabled="true" /> : <ListItemButton component={Link} to={pageHref(coursePath + "/" + item.id, page.number)} aria-label={item.title} aria-current={item.id === lesson?.id ? "true" : undefined} selected={item.id === lesson?.id}><ListItemText primary={item.title} secondary={completedIds.has(item.id) ? "Aula concluída" : undefined} /></ListItemButton>}
        {canManage && <Stack direction="row"><Button size="small" aria-label={"Editar aula " + item.title} onClick={() => setEditor({
                  kind: "aula",
                  item
                })}>Editar</Button><Button size="small" color="error" aria-label={"Excluir aula " + item.title} onClick={() => setEditor({
                  kind: "aula",
                  item,
                  remove: true
                })}>Excluir</Button></Stack>}
      </ListItem>)}</List><Pagination page={page} pathname={coursePath} />
    </Stack></Card></Box>
    {editor && canManage && <Suspense fallback={null}><EducationDialog {...editor} onClose={() => setEditor(null)} /></Suspense>}
    {questionEditor && canManage && <Suspense fallback={null}><QuestionDialog {...questionEditor} onClose={() => setQuestionEditor(null)} /></Suspense>}
  </Stack>;
}
