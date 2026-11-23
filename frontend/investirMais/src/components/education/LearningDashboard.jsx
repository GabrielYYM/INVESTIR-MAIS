import CourseProgressBar from "./CourseProgressBar.jsx";
import StudentsProgressDialog from "./StudentsProgressDialog.jsx";
import { useLearningCourses } from "../../services/learningQueries.js";
import { Accordion, AccordionDetails, AccordionSummary } from "@mui/material";
import { ChevronDown, Users } from "lucide-react";
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
  const courses = useLearningCourses();
  return <section className="space-y-5" aria-label="Meu aprendizado">
    <h2 className="text-xl font-bold">Meus cursos</h2>
    {courses.isPending && <p>Carregando seus cursos…</p>}
    {courses.error && <Notice error>{apiError(courses.error, "Não foi possível carregar suas matrículas.")}</Notice>}
    {courses.data?.length === 0 && <p className="text-muted">Você ainda não está matriculado em nenhum curso.</p>}
    <div className="grid gap-3 sm:grid-cols-2">{courses.data?.map(course => <Link className="panel space-y-2 p-4" key={course.id} to={"/educacao/" + course.id}><h3 className="font-bold">{course.name}</h3><CourseProgressBar value={course.progress_percentage} label={"Progresso em " + course.name} completed={course.completed} /><p className="text-sm text-muted">{course.completed_lessons} de {course.total_lessons} aulas · {course.passed_questions ?? 0} de {course.total_questions ?? 0} questões aprovadas</p></Link>)}</div>
  </section>;
}

export function CourseLearningDashboard({ courseId }) {
  const [showStudentsModal, setShowStudentsModal] = useState(false);
  const stats = useQuery({
    queryKey: ["course-students", courseId],
    queryFn: async () => (await apiClient.get(`/api/learning/courses/${courseId}/students`)).data
  });
  const students = stats.data ?? [];
  const completed = students.filter(student => student.completed).length;
  const completionRate = students.length ? Math.round(1000 * completed / students.length) / 10 : 0;
  return <section className="space-y-3" aria-label="Desempenho do curso">
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
          <div className="pt-2">
            <button
              type="button"
              className="button-secondary flex items-center gap-2"
              disabled={students.length === 0}
              onClick={() => setShowStudentsModal(true)}
            >
              <Users size={16} /> Ver progresso individual dos alunos ({students.length})
            </button>
          </div>
        </>}
      </AccordionDetails>
    </Accordion>

    {showStudentsModal && (
      <StudentsProgressDialog
        students={students}
        onClose={() => setShowStudentsModal(false)}
      />
    )}
  </section>;
}
