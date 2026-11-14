import { lazy, Suspense, useState } from "react";
import { BookOpen, Plus } from "lucide-react";
import { useLoaderData, useRouteLoaderData } from "react-router";
import { CourseCard, Pagination } from "../components/education/EducationParts.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
const EducationDialog = lazy(() => import("../components/education/EducationDialog.jsx"));
import { canManageEducation } from "../services/educationData.js";

export { catalogLoader } from "../services/educationData.js";

export default function Education() {
  const page = useLoaderData();
  const user = useRouteLoaderData("app");
  const [creating, setCreating] = useState(false);
  const canCreate = canManageEducation(user);

  return (
    <>
      <PageHeader
        eyebrow="Aprendizado"
        title="Educação financeira"
        description="Explore cursos e videoaulas para entender melhor o mundo dos investimentos."
        actions={canCreate && <button className="button-primary" onClick={() => setCreating(true)}><Plus size={18} /> Novo curso</button>}
      />

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
