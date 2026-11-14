import { useMemo } from "react";
import { useQueryStates } from "nuqs";
import { Controller, useForm } from "react-hook-form";
import { DatePicker, Progress, Table } from "antd";
import { AlertCircle, BookOpen, Calendar, ChevronRight, Filter, PlayCircle, Plus, RotateCcw, TrendingUp, Users } from "lucide-react";
import { Link } from "react-router";
import { getPresets, isoDate, validatePeriod } from "../services/analysisPeriod.js";
import { periodParams } from "../services/searchParams.js";

export default function TeacherDashboardView({ stats, firstName, role }) {
  const isAdmin = role === "ADMIN";
  const [{ startDate: currentStartDate, endDate: currentEndDate }, setPeriod] = useQueryStates(periodParams, {
    shallow: false, history: "push",
  });
  const { control, watch, handleSubmit } = useForm({
    values: { startDate: currentStartDate || stats.startDate || "", endDate: currentEndDate || stats.endDate || "" },
  });
  const [startVal, endVal] = watch(["startDate", "endDate"]);

  const presets = useMemo(() => getPresets(), []);
  const validation = useMemo(() => validatePeriod(startVal, endVal), [startVal, endVal]);

  const hasFilterInUrl = Boolean(currentStartDate || currentEndDate);

  const isCurrentFilter =
    startVal === (currentStartDate || stats.startDate) &&
    endVal === (currentEndDate || stats.endDate);

  const submitPeriod = handleSubmit((values) => {
    if (validation.valid) return setPeriod(values);
  });

  return (
    <div className="space-y-6">
      <header className="panel flex flex-wrap items-end justify-between gap-5 border-white/5 p-6 sm:p-8">
        <div className="max-w-xl">
          <p className="eyebrow">
            {isAdmin ? "Painel de Ensino & Gestão (Admin)" : "Painel do Professor"}
          </p>
          <h1 className="mt-2 text-3xl font-bold">Olá, {firstName}.</h1>
          <p className="mt-2 text-sm leading-relaxed text-muted sm:text-base">
            {isAdmin
              ? "Visão global de métricas educacionais da plataforma. Acompanhe visualizações, matrículas e taxas de conclusão."
              : "Acompanhe em tempo real o engajamento dos seus alunos, aulas assistidas e o desempenho dos seus cursos."}
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
            {presets.map((p) => {
              const isActive =
                (currentStartDate || stats.startDate) === p.start &&
                (currentEndDate || stats.endDate) === p.end;
              return (
                <button
                  key={p.label}
                  type="button"
                  onClick={() => setPeriod({ startDate: p.start, endDate: p.end })}
                  className={
                    isActive
                      ? "button-primary !min-h-7 !px-2.5 !py-1 !text-xs"
                      : "button-secondary !min-h-7 !px-2.5 !py-1 !text-xs"
                  }
                >
                  {p.label}
                </button>
              );
            })}
          </div>
        </div>

        <form onSubmit={submitPeriod} className="flex flex-wrap items-center gap-3 border-t border-line pt-3">
          <div className="flex items-center gap-2">
            <label htmlFor="startDateInput" className="text-xs font-medium text-muted">De:</label>
            <Controller name="startDate" control={control} render={({ field }) =>
              <DatePicker id="startDateInput" format="YYYY-MM-DD" value={isoDate(field.value)} size="small"
                onBlur={field.onBlur} onChange={(value) => field.onChange(value?.format("YYYY-MM-DD") ?? "")} />} />
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="endDateInput" className="text-xs font-medium text-muted">Até:</label>
            <Controller name="endDate" control={control} render={({ field }) =>
              <DatePicker id="endDateInput" format="YYYY-MM-DD" value={isoDate(field.value)} size="small"
                onBlur={field.onBlur} onChange={(value) => field.onChange(value?.format("YYYY-MM-DD") ?? "")} />} />
          </div>

          <button
            type="submit"
            disabled={!validation.valid || isCurrentFilter}
            className="button-primary !min-h-8 !px-3 !py-1 text-xs disabled:opacity-40"
          >
            <Filter size={13} /> Filtrar
          </button>

          {hasFilterInUrl && (
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
            <p className="flex items-center gap-1.5 text-xs text-amber-400 w-full mt-1">
              <AlertCircle size={14} className="shrink-0" />
              <span>{validation.error}</span>
            </p>
          )}
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
            <div
              className="h-full rounded-full bg-emerald-400 transition-all duration-500"
              style={{ width: `${Math.min(100, stats.averageCompletionRate)}%` }}
            />
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

        {stats.courses.length === 0 ? (
          <div className="flex flex-col items-center justify-center gap-3 rounded-xl border border-line bg-canvas p-8 text-center text-muted">
            <BookOpen size={28} className="text-brand opacity-60" />
            <div>
              <p className="font-semibold text-zinc-200">Nenhum curso cadastrado ainda</p>
              <p className="text-xs text-muted mt-1">Crie seu primeiro curso para começar a acompanhar métricas de alunos e aulas.</p>
            </div>
            <Link to="/educacao" className="button-primary text-xs mt-2">
              <Plus size={15} /> Criar Primeiro Curso
            </Link>
          </div>
        ) : (
          <Table rowKey="courseId" size="small" dataSource={stats.courses} scroll={{ x: 650 }}
            pagination={{ pageSize: 10, hideOnSinglePage: true, showSizeChanger: false }} columns={[
              { title: "Curso", dataIndex: "courseName", render: (name, course) => <Link to={`/educacao/${course.courseId}`} className="font-semibold hover:text-brand">{name}</Link> },
              { title: "Aulas", dataIndex: "totalLessons", align: "center" },
              { title: "Alunos", dataIndex: "enrolledStudents", align: "center" },
              { title: "Aulas Assistidas", dataIndex: "lessonsWatched", align: "center" },
              { title: "Taxa de Conclusão", dataIndex: "completionRate", render: (rate) => <Progress percent={rate} size="small" /> },
              { title: "Ação", render: (_, course) => <Link to={`/educacao/${course.courseId}`} className="button-secondary !min-h-8 !px-2.5 !py-1 text-xs">Ver Curso <ChevronRight size={13} /></Link> },
            ]} />
        )}
      </section>
    </div>
  );
}

