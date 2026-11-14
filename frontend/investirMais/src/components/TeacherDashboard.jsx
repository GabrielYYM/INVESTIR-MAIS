import { AlertCircle, BookOpen, Calendar, ChevronRight, Filter, PlayCircle, Plus, RotateCcw, TrendingUp, Users } from "lucide-react";
import { useMemo } from "react";
import { Button, LinearProgress, Stack, Typography, TextField } from "@mui/material";
import { DataTable, ListContextProvider, useList } from "react-admin";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { useQueryStates } from "nuqs";
import { getPresets, isoDate, validatePeriod } from "../services/analysisPeriod.js";
import { periodParams } from "../services/searchParams.js";
import ResourcePagination from "./ResourcePagination.jsx";
export default function TeacherDashboardView({
  stats,
  firstName,
  role
}) {
  const [period, setPeriod] = useQueryStates(periodParams, {
    shallow: false,
    history: "push"
  });
  const current = {
    startDate: period.startDate || stats.startDate,
    endDate: period.endDate || stats.endDate
  };
  const form = useForm({
    values: current
  });
  const {
    startDate,
    endDate
  } = form.watch();
  const validation = validatePeriod(startDate, endDate);
  const presets = useMemo(() => getPresets(), []);
  const list = useList({
    data: stats.courses.map(course => ({
      ...course,
      id: course.courseId
    })),
    perPage: 10
  });
  const submit = form.handleSubmit(values => validation.valid && setPeriod(values));
  return <div className="space-y-6">
      <header className="panel flex flex-wrap items-end justify-between gap-5 border-white/5 p-6 sm:p-8">
        <div className="max-w-xl">
          <p className="eyebrow">
            {role === "ADMIN" ? "Painel de Ensino & Gestão (Admin)" : "Painel do Professor"}
          </p>
          <h1 className="mt-2 text-3xl font-bold">Olá, {firstName}.</h1>
          <p className="mt-2 text-sm leading-relaxed text-muted sm:text-base">
            {role === "ADMIN" ? "Visão global de métricas educacionais da plataforma. Acompanhe visualizações, matrículas e taxas de conclusão." : "Acompanhe em tempo real o engajamento dos seus alunos, aulas assistidas e o desempenho dos seus cursos."}
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Link to="/educacao" className="button-primary flex items-center gap-2">
            <Plus size={16} />
            <span>Gerenciar Cursos</span>
          </Link>
        </div>
      </header>

      {/* Toolbar de Filtro por Período (Limite máx 6 meses) */}
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
            <span className="text-[11px] text-muted">
              (Limite de até 6 meses por consulta)
            </span>
          </div>

          <div className="flex flex-wrap items-center gap-1.5">
            {presets.map(p => {
            const isActive = (period.startDate || stats.startDate) === p.start && (period.endDate || stats.endDate) === p.end;
            return <button key={p.label} type="button" onClick={() => setPeriod({
              startDate: p.start,
              endDate: p.end
            })} className={isActive ? "button-primary !min-h-7 !px-2.5 !py-1 !text-xs" : "button-secondary !min-h-7 !px-2.5 !py-1 !text-xs"}>
                  {p.label}
                </button>;
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

          <button type="submit" disabled={!validation.valid || startDate === current.startDate && endDate === current.endDate} className="button-primary !min-h-8 !px-3 !py-1 text-xs disabled:opacity-40">
            <Filter size={13} /> Filtrar
          </button>

          {(period.startDate || period.endDate) && <button type="button" onClick={() => setPeriod(null)} className="button-secondary !min-h-8 !px-3 !py-1 text-xs" title="Voltar ao período padrão de 6 meses">
              <RotateCcw size={13} /> Padrão (6 meses)
            </button>}

          {!validation.valid && <p className="flex items-center gap-1.5 text-xs text-amber-400 w-full mt-1">
              <AlertCircle size={14} className="shrink-0" />
              <span>{validation.error}</span>
            </p>}
        </form>
      </section>

      {/* Estatísticas consolidadas no período */}
      <section className="grid grid-cols-2 gap-4 sm:grid-cols-4" aria-label="Estatísticas do Professor">
        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Aulas Assistidas</span>
            <PlayCircle size={18} className="text-amber-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-400">{stats.totalLessonsWatched}</p>
          <span className="text-xs text-muted">No período selecionado</span>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Alunos Matriculados</span>
            <Users size={18} className="text-sky-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-sky-400">{stats.totalStudents}</p>
          <span className="text-xs text-muted">Alunos com matrículas</span>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Taxa de Conclusão</span>
            <TrendingUp size={18} className="text-emerald-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-400">{stats.averageCompletionRate}%</p>
          <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-white/10">
            <div className="h-full rounded-full bg-emerald-400 transition-all duration-500" style={{
            width: `${Math.min(100, stats.averageCompletionRate)}%`
          }} />
          </div>
        </div>

        <div className="panel p-5">
          <div className="flex items-center justify-between text-muted">
            <span className="text-xs font-medium">Cursos & Aulas</span>
            <BookOpen size={18} className="text-purple-400" />
          </div>
          <p className="mt-2 text-2xl font-bold text-purple-400">{stats.totalCourses}</p>
          <span className="text-xs text-muted">{stats.totalLessons} videoaulas cadastradas</span>
        </div>
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

        {stats.courses.length === 0 ? <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-line bg-canvas p-8 text-center text-muted">
            <BookOpen size={28} className="text-brand opacity-60" />
            <div>
              <p className="font-semibold text-zinc-200">Nenhum curso cadastrado ainda</p>
              <p className="text-xs text-muted mt-1">Crie seu primeiro curso para começar a acompanhar métricas de alunos e aulas.</p>
            </div>
            <Link to="/educacao" className="button-primary text-xs mt-2">
              <Plus size={15} /> Criar Primeiro Curso
            </Link>
          </div> : <ListContextProvider value={list}>
      <div className="overflow-x-auto"><DataTable resource="courses" bulkActionButtons={false} rowClick={false} sx={{ minWidth: 650 }}>
        <DataTable.Col source="courseName" label="Curso" render={course => <Link to={`/educacao/${course.courseId}`}>{course.courseName}</Link>} />
        <DataTable.Col source="totalLessons" label="Aulas" /><DataTable.Col source="enrolledStudents" label="Alunos" /><DataTable.Col source="lessonsWatched" label="Aulas Assistidas" />
        <DataTable.Col source="completionRate" label="Taxa de Conclusão" render={course => <Stack direction="row" spacing={1} sx={{ alignItems: "center", minWidth: 120 }}><LinearProgress sx={{ flex: 1 }} variant="determinate" value={Math.min(100, course.completionRate)} /><Typography sx={{ fontSize: 12 }}>{course.completionRate}%</Typography></Stack>} />
        <DataTable.Col label="Ação" render={course => <Button size="small" component={Link} to={`/educacao/${course.courseId}`} endIcon={<ChevronRight size={14} />}>Ver Curso</Button>} />
      </DataTable></div><ResourcePagination />
    </ListContextProvider>}
      </section>
    </div>;
}
