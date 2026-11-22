import CourseProgressBar from "./CourseProgressBar.jsx";
import { useLearningCourses } from "../../services/learningQueries.js";
import { Accordion, AccordionDetails, AccordionSummary } from "@mui/material";
import { ChevronDown } from "lucide-react";
import { Link } from "react-router";
import { useQuery } from "@tanstack/react-query";
import { apiClient, apiError } from "../../services/api.js";
import { Notice } from "../ui/PageState.jsx";
import StatePagination from "../ui/StatePagination.jsx";
import { useState } from "react";

export function TeacherCourses({ userId }) {
  const [page, setPage] = useState(0);
  const courses = useQuery({ queryKey: ["teacher-courses", userId, page], queryFn: async () => (await apiClient.get("/api/learning/teaching/courses", { params: { page, size: 9, sort: "name" } })).data });
  return <section className="space-y-4" aria-label="Cursos que criei"><h2 className="text-xl font-bold">Cursos que criei</h2>
    {courses.isPending && <p>Carregando seus cursos…</p>}{courses.error && <Notice error>{apiError(courses.error, "Não foi possível carregar seus cursos.")}</Notice>}
    {courses.data?.items.length === 0 && <p>Você ainda não cadastrou cursos.</p>}
    <div className="grid gap-3 sm:grid-cols-2">{courses.data?.items.map(course => <Link key={course.id} to={"/educacao/" + course.id} className="panel p-4"><h3 className="font-bold">{course.name}</h3><p className="mt-2 mb-4 text-sm text-muted">Inscritos: {course.enrolled_students} · Concluíram: {course.completed_students}</p><CourseProgressBar value={course.average_progress} label={"Progresso médio em " + course.name} /><p className="mt-3 text-sm text-muted">Taxa de conclusão: <strong className="text-positive">{Number(course.completion_rate).toLocaleString("pt-BR")}%</strong></p></Link>)}</div>
    <StatePagination page={page} totalPages={courses.data?.totalPages} onChange={setPage} showPageNumber={false} />
  </section>;
}

export function StudentLearningDashboard() {
  const [page, setPage] = useState(0);
  const courses = useLearningCourses();
  const answers = useQuery({ queryKey: ["my-answers", page], queryFn: async () => (await apiClient.get("/api/learning/answers", { params: { page, size: 10 } })).data });
  return <section className="space-y-5" aria-label="Meu aprendizado">
    <h2 className="text-xl font-bold">Meus cursos</h2>
    {courses.isPending && <p>Carregando seus cursos…</p>}
    {courses.error && <Notice error>{apiError(courses.error, "Não foi possível carregar suas matrículas.")}</Notice>}
    {courses.data?.length === 0 && <p className="text-muted">Você ainda não está matriculado em nenhum curso.</p>}
    <div className="grid gap-3 sm:grid-cols-2">{courses.data?.map(course => <Link className="panel space-y-2 p-4" key={course.id} to={"/educacao/" + course.id}><h3 className="font-bold">{course.name}</h3><CourseProgressBar value={course.progress_percentage} label={"Progresso em " + course.name} completed={course.completed} /><p className="text-sm text-muted">{course.completed_lessons} de {course.total_lessons} aulas · {course.passed_questions ?? 0} de {course.total_questions ?? 0} questões aprovadas</p></Link>)}</div>
    <h2 className="text-xl font-bold">Minhas respostas</h2>
    {answers.isPending && <p>Carregando suas respostas…</p>}
    {answers.error && <Notice error>{apiError(answers.error, "Não foi possível carregar suas respostas.")}</Notice>}
    {answers.data?.content.length === 0 && <p className="text-muted">Suas novas respostas aparecerão aqui. Respostas anteriores à atualização não possuem alternativa registrada.</p>}
    {answers.data?.content.map(answer => <article key={answer.id} className="panel space-y-2 p-4"><h3 className="font-semibold">{answer.prompt}</h3><p>Sua resposta: {answer.selectedAnswer}</p><p className={answer.correct ? "text-positive" : "text-negative"}>{answer.correct ? "Correta" : "Incorreta"} · {new Date(answer.answeredAt).toLocaleString("pt-BR")}</p><Link className="text-brand" to={"/educacao/" + answer.courseId}>Abrir curso</Link></article>)}
    <StatePagination page={page} totalPages={answers.data?.page.totalPages} onChange={setPage} />
  </section>;
}

export function CourseLearningDashboard({ courseId }) {
  const stats = useQuery({ queryKey: ["course-students", courseId], queryFn: async () => {
    const [students, questions] = await Promise.all([apiClient.get(`/api/learning/courses/${courseId}/students`), apiClient.get(`/api/learning/courses/${courseId}/questions`)]);
    return { students: students.data, questions: questions.data };
  } });
  const students = stats.data?.students ?? [];
  const completed = students.filter(student => student.completed).length;
  const completionRate = students.length ? Math.round(1000 * completed / students.length) / 10 : 0;

  return <section className="space-y-4" aria-label="Desempenho do curso">
    <Accordion defaultExpanded className="panel">
      <AccordionSummary expandIcon={<ChevronDown size={20} />}>
        <h2 className="text-xl font-bold">Desempenho do curso</h2>
      </AccordionSummary>
      <AccordionDetails className="space-y-4">
        {stats.isPending && <p>Carregando desempenho…</p>}
        {stats.error && <Notice error>{apiError(stats.error, "Não foi possível carregar o desempenho.")}</Notice>}
        {stats.data && <>
          <div className="grid gap-3 sm:grid-cols-3">{[["Alunos inscritos", students.length], ["Alunos que concluíram", completed], ["Taxa de conclusão", completionRate.toLocaleString("pt-BR") + "%"]].map(([label, value]) => <div key={label} className="rounded-xl border border-line bg-canvas p-4"><p className="text-sm text-muted">{label}</p><p className="mt-1 text-2xl font-bold">{value}</p></div>)}</div>
          <p className="text-xs text-muted">Conclusão considera todas as aulas assistidas e questões aprovadas. A taxa compara os alunos que concluíram com os inscritos ativos.</p>
          <h3 className="font-semibold">Taxa de acerto por questão</h3>
          {stats.data.questions.map(question => <p key={question.id}>{question.prompt}: {question.accuracy}% · {question.attempts} respostas</p>)}
        </>}
      </AccordionDetails>
    </Accordion>

    <Accordion defaultExpanded className="panel">
      <AccordionSummary expandIcon={<ChevronDown size={20} />}>
        <h2 className="text-xl font-bold">Progresso individual dos alunos</h2>
      </AccordionSummary>
      <AccordionDetails className="space-y-3">
        {stats.isPending && <p>Carregando progresso dos alunos…</p>}
        {stats.error && <Notice error>{apiError(stats.error, "Não foi possível carregar o progresso dos alunos.")}</Notice>}
        {stats.data && (!students.length ? <p>Nenhum aluno matriculado.</p> : <div className="space-y-3">{students.map(student => <article key={student.id} className="space-y-3 rounded-xl border border-line p-4"><h3 className="font-semibold">{student.name || "Aluno " + student.id.slice(0, 8)}</h3>
          <CourseProgressBar value={student.progress_percentage} label={"Progresso de " + (student.name || student.id.slice(0, 8))} completed={student.completed} />
          <p className="text-sm text-muted">{student.completed_lessons} de {student.total_lessons} aulas · {student.passed_questions} de {student.total_questions} questões aprovadas</p>
          <p className="text-sm text-muted">{student.attempts} respostas · {student.accuracy ?? 0}% de acerto</p>
        </article>)}</div>)}
      </AccordionDetails>
    </Accordion>
  </section>;
}
