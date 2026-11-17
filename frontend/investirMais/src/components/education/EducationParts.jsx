import CourseProgressBar from "./CourseProgressBar.jsx";
import { useLearningCourses } from "../../services/learningQueries.js";
import { ArrowLeft, ArrowRight, BookOpen, ChevronRight } from "lucide-react";
import { Link, useLocation, useRouteLoaderData } from "react-router";
import { pageHref } from "../../services/searchParams.js";

export function CourseCard({ course }) {
  const user = useRouteLoaderData("app");
  const showProgress = user?.role === "STUDENT";
  const learning = useLearningCourses(showProgress);
  const progress = learning.data?.find(item => item.id === course.id);
  return (
    <Link to={"/educacao/" + course.id} className="panel group flex min-w-0 flex-col overflow-hidden transition-colors hover:border-brand/55">
      <div className="flex items-center gap-3 px-5 pt-5">
        <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-amber-500/20 text-amber-400"><BookOpen size={20} /></span>
        <h3 className="min-w-0 font-bold break-words">{course.name}</h3>
      </div>
      <div className="flex flex-1 flex-col p-5 pt-3">
        <p className="mb-4 line-clamp-3 text-sm leading-relaxed break-words text-muted">{course.description}</p>
        {showProgress && <div className="mb-4">{learning.isPending ? <p className="text-xs text-muted">Carregando progresso…</p> : learning.error ? <p className="text-xs text-muted">Progresso indisponível</p> : <CourseProgressBar value={progress?.progress_percentage} completed={progress?.completed} label={"Seu progresso em " + course.name} />}</div>}
        <span className="mt-auto inline-flex items-center gap-1.5 text-sm font-semibold text-brand">Ver curso <ChevronRight size={16} className="transition-transform group-hover:translate-x-0.5" /></span>
      </div>
    </Link>
  );
}

export function Pagination({ page, pathname }) {
  const { search } = useLocation();
  const base = pathname + search;
  if (page.totalPages <= 1) return null;
  return (
    <nav className="flex flex-wrap items-center justify-between gap-2" aria-label="Paginação">
      {page.number > 0
        ? <Link className="button-secondary" to={pageHref(base, page.number - 1)}><ArrowLeft size={15} /> Anterior</Link>
        : <span />}
      <span className="text-xs font-semibold text-muted">Página {page.number + 1} de {page.totalPages}</span>
      {page.number + 1 < page.totalPages
        ? <Link className="button-secondary" to={pageHref(base, page.number + 1)}>Próxima <ArrowRight size={15} /></Link>
        : <span />}
    </nav>
  );
}
