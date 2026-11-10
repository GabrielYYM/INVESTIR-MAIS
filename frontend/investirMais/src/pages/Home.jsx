import { useEffect, useMemo, useState } from "react";
import {
  AlertCircle,
  ArrowRight,
  BookOpen,
  Calendar,
  ChevronRight,
  Filter,
  PlayCircle,
  Plus,
  RotateCcw,
  TrendingUp,
  Users,
} from "lucide-react";
import { Link, redirect, useLoaderData, useRouteLoaderData, useSearchParams } from "react-router";
import { CourseCard } from "../components/EducationParts.jsx";
import { apiError } from "../services/api.js";
import { dashboardCourses } from "../services/educationData.js";
import { getTeacherDashboard } from "../services/teacherService.js";

function formatDateBr(isoStr) {
  if (!isoStr) return "";
  const parts = String(isoStr).split("-");
  if (parts.length !== 3) return isoStr;
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
}

function toIsoDate(d) {
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function validatePeriod(startStr, endStr) {
  if (!startStr || !endStr) return { valid: false, error: "Informe as datas de início e fim." };
  const start = new Date(startStr + "T00:00:00");
  const end = new Date(endStr + "T00:00:00");
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return { valid: false, error: "Data em formato inválido." };
  }
  if (start > end) {
    return { valid: false, error: "A data inicial não pode ser posterior à data final." };
  }
  const maxEnd = new Date(start);
  maxEnd.setMonth(maxEnd.getMonth() + 6);
  if (end > maxEnd) {
    return {
      valid: false,
      error: "O período de consulta não pode exceder 6 meses para proteger a performance da plataforma.",
    };
  }
  return { valid: true, error: null };
}

function getPresets() {
  const today = new Date();
  const end = toIsoDate(today);

  const d30 = new Date(today);
  d30.setDate(d30.getDate() - 30);
  const start30 = toIsoDate(d30);

  const d3m = new Date(today);
  d3m.setMonth(d3m.getMonth() - 3);
  const start3m = toIsoDate(d3m);

  const d6m = new Date(today);
  d6m.setMonth(d6m.getMonth() - 6);
  const start6m = toIsoDate(d6m);

  return [
    { label: "Últimos 30 dias", start: start30, end },
    { label: "Últimos 3 meses", start: start3m, end },
    { label: "Últimos 6 meses", start: start6m, end },
  ];
}

export async function dashboardLoader({ request }) {
  try {
    const url = new URL(request.url);
    const startDate = url.searchParams.get("startDate") || undefined;
    const endDate = url.searchParams.get("endDate") || undefined;

    let teacherError = null;
    const [page, teacherStats] = await Promise.all([
      dashboardCourses(request.signal).catch((error) => {
        if (error.response?.status === 401) throw error;
        return null;
      }),
      getTeacherDashboard({ startDate, endDate }, request.signal).catch((error) => {
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

function TeacherDashboardView({ stats, firstName, role }) {
  const isAdmin = role === "ADMIN";
  const [searchParams, setSearchParams] = useSearchParams();
  const [startVal, setStartVal] = useState(stats.startDate || "");
  const [endVal, setEndVal] = useState(stats.endDate || "");

  useEffect(() => {
    if (stats.startDate) setStartVal(stats.startDate);
    if (stats.endDate) setEndVal(stats.endDate);
  }, [stats.startDate, stats.endDate]);

  const presets = useMemo(() => getPresets(), []);
  const validation = useMemo(() => validatePeriod(startVal, endVal), [startVal, endVal]);

  const currentStartDate = searchParams.get("startDate");
  const currentEndDate = searchParams.get("endDate");
  const hasFilterInUrl = Boolean(currentStartDate || currentEndDate);

  const isCurrentFilter =
    startVal === (currentStartDate || stats.startDate) &&
    endVal === (currentEndDate || stats.endDate);

  const handlePreset = (start, end) => {
    setStartVal(start);
    setEndVal(end);
    const newParams = new URLSearchParams(searchParams);
    newParams.set("startDate", start);
    newParams.set("endDate", end);
    setSearchParams(newParams);
  };

  const handleCustomSubmit = (e) => {
    e.preventDefault();
    if (!validation.valid) return;
    const newParams = new URLSearchParams(searchParams);
    newParams.set("startDate", startVal);
    newParams.set("endDate", endVal);
    setSearchParams(newParams);
  };

  const handleReset = () => {
    const newParams = new URLSearchParams(searchParams);
    newParams.delete("startDate");
    newParams.delete("endDate");
    setSearchParams(newParams);
  };

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
              {formatDateBr(stats.startDate)} a {formatDateBr(stats.endDate)}
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
                  onClick={() => handlePreset(p.start, p.end)}
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

        <form onSubmit={handleCustomSubmit} className="flex flex-wrap items-center gap-3 border-t border-line pt-3">
          <div className="flex items-center gap-2">
            <label htmlFor="startDateInput" className="text-xs font-medium text-muted">De:</label>
            <input
              id="startDateInput"
              type="date"
              value={startVal}
              onChange={(e) => setStartVal(e.target.value)}
              className="field !w-auto !min-h-8 !py-1 !px-2.5 text-xs text-zinc-100 bg-raised"
            />
          </div>

          <div className="flex items-center gap-2">
            <label htmlFor="endDateInput" className="text-xs font-medium text-muted">Até:</label>
            <input
              id="endDateInput"
              type="date"
              value={endVal}
              onChange={(e) => setEndVal(e.target.value)}
              className="field !w-auto !min-h-8 !py-1 !px-2.5 text-xs text-zinc-100 bg-raised"
            />
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
              onClick={handleReset}
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
          <div className="overflow-x-auto rounded-xl border border-line">
            <table className="w-full text-left text-xs sm:text-sm">
              <thead className="border-b border-line bg-raised font-medium text-muted">
                <tr>
                  <th className="px-4 py-3">Curso</th>
                  <th className="px-4 py-3 text-center">Aulas</th>
                  <th className="px-4 py-3 text-center">Alunos</th>
                  <th className="px-4 py-3 text-center">Aulas Assistidas</th>
                  <th className="px-4 py-3">Taxa de Conclusão</th>
                  <th className="px-4 py-3 text-right">Ação</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-line">
                {stats.courses.map((c) => (
                  <tr key={c.courseId} className="transition-colors hover:bg-white/[0.02]">
                    <td className="px-4 py-3">
                      <Link
                        to={`/educacao/${c.courseId}`}
                        className="font-semibold text-zinc-100 hover:text-brand transition-colors"
                      >
                        {c.courseName}
                      </Link>
                    </td>
                    <td className="px-4 py-3 text-center text-muted font-mono">{c.totalLessons}</td>
                    <td className="px-4 py-3 text-center text-sky-400 font-semibold">{c.enrolledStudents}</td>
                    <td className="px-4 py-3 text-center text-amber-400 font-semibold">{c.lessonsWatched}</td>
                    <td className="px-4 py-3 min-w-36">
                      <div className="flex items-center gap-2">
                        <div className="h-1.5 flex-1 overflow-hidden rounded-full bg-white/10">
                          <div
                            className="h-full rounded-full bg-brand transition-all duration-500"
                            style={{ width: `${Math.min(100, c.completionRate)}%` }}
                          />
                        </div>
                        <span className="w-10 text-right font-mono text-xs font-bold text-zinc-200">
                          {c.completionRate}%
                        </span>
                      </div>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <Link
                        to={`/educacao/${c.courseId}`}
                        className="button-secondary !min-h-8 !px-2.5 !py-1 text-xs"
                      >
                        Ver Curso <ChevronRight size={13} />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default function Home() {
  const { page, teacherStats, teacherError } = useLoaderData();
  const user = useRouteLoaderData("app");
  const [, setSearchParams] = useSearchParams();
  const firstName = user.name?.trim().split(/\s+/)[0] || "investidor";
  const isTeacherOrAdmin = user.role === "TEACHER" || user.role === "ADMIN";

  return (
    <>
      {isTeacherOrAdmin && teacherError && (
        <section className="panel space-y-3 border-red-500/20 bg-red-500/10 p-5">
          <div className="flex items-center gap-2 text-negative font-semibold">
            <AlertCircle size={18} />
            <span>Não foi possível carregar as métricas do período</span>
          </div>
          <p className="text-sm text-zinc-300">{teacherError}</p>
          <button
            type="button"
            onClick={() => setSearchParams({})}
            className="button-secondary text-xs"
          >
            <RotateCcw size={14} /> Voltar para o período padrão (últimos 6 meses)
          </button>
        </section>
      )}

      {isTeacherOrAdmin && teacherStats ? (
        <TeacherDashboardView stats={teacherStats} firstName={firstName} role={user.role} />
      ) : (
        <header className="panel border-white/5 p-6 sm:p-8">
          <h1 className="mb-2 text-3xl font-bold break-words">Olá, {firstName}.</h1>
          <p className="max-w-xl text-muted">Bem-vindo ao <span className="font-semibold text-amber-400">Investir+</span>. Explore o catálogo de cursos sobre finanças.</p>
          <Link to="/educacao" className="button-primary mt-5">Explorar cursos <ArrowRight size={17} /></Link>
        </header>
      )}

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
