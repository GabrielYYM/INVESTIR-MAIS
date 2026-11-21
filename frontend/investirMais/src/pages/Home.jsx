import { StudentLearningDashboard, TeacherCourses } from "../components/education/LearningDashboard.jsx";
import { useMemo } from "react";
import { AlertCircle, ArrowRight, BookOpen, Calendar, ChevronRight, Filter, PlayCircle, Plus, RotateCcw, TrendingUp, Users } from "lucide-react";
import { Button, LinearProgress, Stack, TextField, Typography } from "@mui/material";
import { DataTable, ListContextProvider, useList } from "react-admin";
import { useForm } from "react-hook-form";
import { Link, redirect, useLoaderData, useRouteLoaderData } from "react-router";
import { useQueryStates } from "nuqs";
import { CourseCard } from "../components/education/EducationParts.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import ResourcePagination from "../components/ui/ResourcePagination.jsx";
import StatCard from "../components/ui/StatCard.jsx";
import { getPresets, isoDate, validatePeriod } from "../services/analysisPeriod.js";
import { apiError } from "../services/api.js";
import { dashboardCourses } from "../services/educationData.js";
import { loadPeriod, periodParams } from "../services/searchParams.js";
import { getTeacherDashboard } from "../services/teacherService.js";

export async function dashboardLoader({ request }) {
  try {
    const url = new URL(request.url);
    const { startDate, endDate } = loadPeriod(url.searchParams);

    let teacherError = null;
    const [page, teacherStats] = await Promise.all([
      dashboardCourses(request.signal).catch((error) => {
        if (error.response?.status === 401) throw error;
        return null;
      }),
      getTeacherDashboard({ startDate: startDate || undefined, endDate: endDate || undefined }, request.signal).catch((error) => {
        if (error.response?.status === 401) throw error;
        if (error.response?.status !== 403) {
          teacherError = apiError(error, "Não foi possível consultar as métricas. Tente novamente.");
        }
        return null;
      }),
    ]);
    return { page, teacherStats, teacherError };
  } catch (error) {
    if (error.response?.status === 401) throw redirect("/entrar");
    return { page: null, teacherStats: null, teacherError: null };
  }
}

function TeacherMetricsSection({ stats, period, setPeriod }) {
  const current = {
    startDate: period.startDate || stats.startDate,
    endDate: period.endDate || stats.endDate,
  };
  const form = useForm({ values: current });
  const { startDate, endDate } = form.watch();
  const validation = validatePeriod(startDate, endDate);
  const presets = useMemo(() => getPresets(), []);
  const list = useList({
    data: stats.courses.map((course) => ({ ...course, id: course.courseId })),
    perPage: 10,
  });
  const submit = form.handleSubmit((values) => validation.valid && setPeriod(values));

  return (
    <div className="space-y-6">
      <section className="panel space-y-4 p-4 sm:p-5" aria-labelledby="filtro-periodo-titulo">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2">
            <Calendar size={18} className="text-amber-400" />
            <h2 id="filtro-periodo-titulo" className="text-sm font-semibold text-zinc-100">
              Período de Análise
            </h2>
            <span className="rounded-md border border-line bg-raised px-2 py-0.5 text-xs font-mono text-zinc-300">
              {isoDate(stats.startDate)?.format("DD/MM/YYYY")} a {isoDate(stats.endDate)?.format("DD/MM/YYYY")}
            </span>
            <span className="text-[11px] text-muted">(Limite de até 6 meses por consulta)</span>
          </div>

          <div className="flex flex-wrap items-center gap-1.5">
            {presets.map((p) => {
              const isActive = (period.startDate || stats.startDate) === p.start && (period.endDate || stats.endDate) === p.end;
              return (
                <button
                  key={p.label}
                  type="button"
                  onClick={() => setPeriod({ startDate: p.start, endDate: p.end })}
                  className={isActive ? "button-primary !min-h-7 !px-2.5 !py-1 !text-xs" : "button-secondary !min-h-7 !px-2.5 !py-1 !text-xs"}
                >
                  {p.label}
                </button>
              );
            })}
          </div>
        </div>

        <form onSubmit={submit} className="flex flex-wrap items-center gap-3 border-t border-line pt-3">
          <div className="flex items-center gap-2">
            <label htmlFor="startDateInput" className="text-xs font-medium text-muted">De:</label>
            <TextField id="startDateInput" type="date" className="period-date" {...form.register("startDate")} />
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="endDateInput" className="text-xs font-medium text-muted">Até:</label>
            <TextField id="endDateInput" type="date" className="period-date" {...form.register("endDate")} />
          </div>

          <button
            type="submit"
            disabled={!validation.valid || (startDate === current.startDate && endDate === current.endDate)}
            className="button-primary !min-h-8 !px-3 !py-1 text-xs disabled:opacity-40"
          >
            <Filter size={13} /> Filtrar
          </button>

          {(period.startDate || period.endDate) && (
            <button
              type="button"
              onClick={() => setPeriod(null)}
              className="button-secondary !min-h-8 !px-3 !py-1 text-xs"
              title="Voltar ao período padrão de 6 meses"
            >
              <RotateCcw size={13} /> Padrão (6 meses)
            </button>
          )}

          {!validation.valid && (
            <p className="mt-1 flex w-full items-center gap-1.5 text-xs text-amber-400">
              <AlertCircle size={14} className="shrink-0" />
              <span>{validation.error}</span>
            </p>
          )}
        </form>
      </section>

      <section className="grid grid-cols-2 gap-4 sm:grid-cols-4" aria-label="Estatísticas do Professor">
        <StatCard
          label="Aulas Assistidas"
          value={stats.totalLessonsWatched}
          icon={PlayCircle}
          color="text-amber-400"
          caption="No período selecionado"
        />
        <StatCard
          label="Alunos Matriculados"
          value={stats.totalStudents}
          icon={Users}
          color="text-sky-400"
          caption="Alunos com matrículas"
        />
        <StatCard
          label="Aulas concluídas (%)"
          value={`${stats.averageCompletionRate}%`}
          icon={TrendingUp}
          color="text-emerald-400"
          progress={stats.averageCompletionRate}
        />
        <StatCard
          label="Cursos & Aulas"
          value={stats.totalCourses}
          icon={BookOpen}
          color="text-purple-400"
          caption={`${stats.totalLessons} videoaulas cadastradas`}
        />
      </section>

      <section className="panel space-y-4 p-5 sm:p-6" aria-labelledby="cursos-metricas-titulo">
        <div className="flex flex-wrap items-baseline justify-between gap-2">
          <div>
            <h2 id="cursos-metricas-titulo" className="text-lg font-bold">Desempenho por Curso no Período</h2>
            <p className="text-xs text-muted">Métricas de matrículas e aulas concluídas durante o intervalo selecionado.</p>
          </div>
          <span className="text-xs text-muted">
            {stats.courses.length} {stats.courses.length === 1 ? "curso" : "cursos"}
          </span>
        </div>

        {stats.courses.length === 0 ? (
          <EmptyState
            panel={false}
            icon={BookOpen}
            title="Nenhum curso cadastrado ainda"
            description="Crie seu primeiro curso para começar a acompanhar métricas de alunos e aulas."
            action={
              <Link to="/educacao" className="button-primary mt-2 text-xs">
                <Plus size={15} /> Criar Primeiro Curso
              </Link>
            }
          />
        ) : (
          <ListContextProvider value={list}>
            <div className="overflow-x-auto">
              <DataTable resource="courses" bulkActionButtons={false} rowClick={false} sx={{ minWidth: 650 }}>
                <DataTable.Col source="courseName" label="Curso" render={(course) => <Link to={`/educacao/${course.courseId}`}>{course.courseName}</Link>} />
                <DataTable.Col source="totalLessons" label="Aulas" />
                <DataTable.Col source="enrolledStudents" label="Alunos" />
                <DataTable.Col source="lessonsWatched" label="Aulas Assistidas" />
                <DataTable.Col
                  source="completionRate"
                  label="Aulas concluídas (%)"
                  render={(course) => (
                    <Stack direction="row" spacing={1} sx={{ alignItems: "center", minWidth: 120 }}>
                      <LinearProgress sx={{ flex: 1 }} variant="determinate" value={Math.min(100, course.completionRate)} />
                      <Typography sx={{ fontSize: 12 }}>{course.completionRate}%</Typography>
                    </Stack>
                  )}
                />
                <DataTable.Col
                  label="Ação"
                  render={(course) => (
                    <Button size="small" component={Link} to={`/educacao/${course.courseId}`} endIcon={<ChevronRight size={14} />}>
                      Ver Curso
                    </Button>
                  )}
                />
              </DataTable>
            </div>
            <ResourcePagination />
          </ListContextProvider>
        )}
      </section>
    </div>
  );
}

export default function Home() {
  const { page, teacherStats, teacherError } = useLoaderData();
  const user = useRouteLoaderData("app");
  const [period, setPeriod] = useQueryStates(periodParams, { shallow: false, history: "push" });
  const nickname = user.name?.trim() || "investidor";
  const isTeacherOrAdmin = user.role === "TEACHER" || user.role === "ADMIN";
  const showTeacherDashboard = isTeacherOrAdmin && Boolean(teacherStats);

  return (
    <>
      {isTeacherOrAdmin && teacherError && (
        <section className="panel space-y-3 border-red-500/20 bg-red-500/10 p-5">
          <div className="flex items-center gap-2 font-semibold text-negative">
            <AlertCircle size={18} />
            <span>Não foi possível carregar as métricas do período</span>
          </div>
          <p className="text-sm text-zinc-300">{teacherError}</p>
          <button type="button" onClick={() => setPeriod(null)} className="button-secondary text-xs">
            <RotateCcw size={14} /> Voltar para o período padrão (últimos 6 meses)
          </button>
        </section>
      )}

      <PageHeader
        title={`Olá, ${nickname}.`}
        description={
          showTeacherDashboard
            ? user.role === "ADMIN"
              ? "Visão global de métricas educacionais da plataforma. Acompanhe visualizações, matrículas e conclusão de aulas."
              : "Acompanhe em tempo real o engajamento dos seus alunos, aulas assistidas e o desempenho dos seus cursos."
            : <>Bem-vindo ao <span className="font-semibold text-amber-400">Investir+</span>. Explore o catálogo de cursos sobre finanças.</>
        }
      />

      {showTeacherDashboard && (
        <TeacherMetricsSection stats={teacherStats} period={period} setPeriod={setPeriod} />
      )}

      {user.role === "TEACHER" && <TeacherCourses userId={user.id} />}
      {["STUDENT", "TEACHER"].includes(user.role) && <StudentLearningDashboard />}
      <section className="space-y-4" aria-labelledby="educacao-titulo">
        <div className="flex flex-wrap items-end justify-between gap-3">
          <div>
            <p className="eyebrow">Educação</p>
            <h2 id="educacao-titulo" className="mt-1 text-2xl font-bold tracking-tight">Catálogo de Cursos</h2>
          </div>
          <Link to="/educacao" className="button-secondary">Ver catálogo <ArrowRight size={16} /></Link>
        </div>
        {page?.items.length ? (
          <div className="grid gap-3 sm:grid-cols-3">{page.items.map((course) => <CourseCard key={course.id} course={course} />)}</div>
        ) : (
          <p className="panel p-5 text-sm text-muted">{page ? "Ainda não há cursos no catálogo." : "O catálogo está indisponível no momento."}</p>
        )}
      </section>
    </>
  );
}
