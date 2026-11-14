import { lazy, Suspense } from "react";
import { AlertCircle, ArrowRight, RotateCcw } from "lucide-react";
import { Link, redirect, useLoaderData, useRouteLoaderData } from "react-router";
import { useQueryStates } from "nuqs";
import { CourseCard } from "../components/EducationParts.jsx";
import { apiError } from "../services/api.js";
import { dashboardCourses } from "../services/educationData.js";
import { getTeacherDashboard } from "../services/teacherService.js";
import { loadPeriod, periodParams } from "../services/searchParams.js";

const TeacherDashboardView = lazy(() => import("../components/TeacherDashboard.jsx"));

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

export default function Home() {
  const { page, teacherStats, teacherError } = useLoaderData();
  const user = useRouteLoaderData("app");
  const [, setPeriod] = useQueryStates(periodParams, { shallow: false, history: "push" });
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
            onClick={() => setPeriod(null)}
            className="button-secondary text-xs"
          >
            <RotateCcw size={14} /> Voltar para o período padrão (últimos 6 meses)
          </button>
        </section>
      )}

      {isTeacherOrAdmin && teacherStats ? (
        <Suspense fallback={<p role="status" className="text-muted">Carregando métricas…</p>}><TeacherDashboardView stats={teacherStats} firstName={firstName} role={user.role} /></Suspense>
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
