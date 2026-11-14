import { lazy, Suspense, useState } from "react";
import { BookOpen, Plus } from "lucide-react";
import { useLoaderData, useRouteLoaderData } from "react-router";
import { CourseCard, Pagination } from "../components/EducationParts.jsx";
const EducationDialog = lazy(() => import("../components/EducationDialog.jsx"));
import { canManageEducation } from "../services/educationData.js";

export { catalogLoader } from "../services/educationData.js";

export default function Education() {
  const page = useLoaderData();
  const user = useRouteLoaderData("app");
  const [creating, setCreating] = useState(false);
  const canCreate = canManageEducation(user);

  return (
    <>
      <header className="panel flex flex-wrap items-end justify-between gap-5 border-white/5 p-6 sm:p-8">
        <div className="max-w-xl">
          <p className="eyebrow">Aprendizado</p>
          <h1 className="mt-2 text-3xl font-bold">Educação financeira</h1>
          <p className="mt-3 text-sm leading-relaxed text-muted sm:text-base">Explore cursos e videoaulas para entender melhor o mundo dos investimentos.</p>
        </div>
        {canCreate && <button className="button-primary" onClick={() => setCreating(true)}><Plus size={18} /> Novo curso</button>}
      </header>

      <section className="space-y-4" aria-labelledby="catalogo-titulo">
        <div className="flex flex-wrap items-baseline justify-between gap-2">
          <h2 id="catalogo-titulo" className="text-xl font-bold">Catálogo de cursos</h2>
          <span className="text-sm text-muted">{page.totalItems} {page.totalItems === 1 ? "curso" : "cursos"}</span>
        </div>
        {page.items.length ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{page.items.map((course) => <CourseCard key={course.id} course={course} />)}</div>
        ) : (
          <div className="panel flex flex-col items-center gap-2 p-8 text-center">
            <BookOpen className="mb-2 text-brand" size={25} />
            <h3 className="font-bold">O catálogo ainda está vazio</h3>
            <p className="text-sm text-muted">{canCreate ? "Crie um curso para começar a adicionar aulas." : "Os cursos aparecerão aqui quando forem cadastrados."}</p>
          </div>
        )}
        <Pagination page={page} pathname="/educacao" />
      </section>
      {creating && canCreate && <Suspense fallback={null}><EducationDialog kind="curso" onClose={() => setCreating(false)} /></Suspense>}
    </>
  );
}
