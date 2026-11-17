import { lazy, Suspense, useState } from "react";
import { BookOpen, Plus } from "lucide-react";
import { useLoaderData, useRouteLoaderData, Form, useSearchParams } from "react-router";
import { CourseCard, Pagination } from "../components/education/EducationParts.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import { canManageEducation } from "../services/educationData.js";

const EducationDialog = lazy(() => import("../components/education/EducationDialog.jsx"));

export { catalogLoader } from "../services/educationData.js";

export default function Education() {
  const page = useLoaderData();
  const [search] = useSearchParams();
  const user = useRouteLoaderData("app");
  const [creating, setCreating] = useState(false);
  const canCreate = canManageEducation(user);

  return (
    <>
      <PageHeader
        title="Educação financeira"
        description="Explore cursos e videoaulas para entender melhor o mundo dos investimentos."
        actions={canCreate && <button className="button-primary" onClick={() => setCreating(true)}><Plus size={18} /> Novo curso</button>}
      />

      <Form method="get" className="panel flex flex-wrap gap-4 p-5" key={search.toString()}>
        <label>Nome ou palavra-chave<input className="block rounded border border-line bg-canvas p-2" name="busca" defaultValue={search.get("busca") || ""} maxLength={100} /></label>
        <label>Categoria<input className="block rounded border border-line bg-canvas p-2" name="categoria" defaultValue={search.get("categoria") || ""} maxLength={100} /></label>
        <label>Ordenar por<select className="block rounded border border-line bg-canvas p-2" name="ordem" defaultValue={search.get("ordem") || "name"}><option value="name">Nome</option><option value="newest">Mais recentes</option><option value="oldest">Mais antigos</option></select></label>
        <button className="button-primary">Pesquisar cursos</button>
      </Form>
      <section className="space-y-4" aria-labelledby="catalogo-titulo">
        <div className="flex flex-wrap items-baseline justify-between gap-2">
          <h2 id="catalogo-titulo" className="text-xl font-bold">Catálogo de cursos</h2>
          <span className="text-sm text-muted">{page.totalItems} {page.totalItems === 1 ? "curso" : "cursos"}</span>
        </div>
        {page.items.length ? (
          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">{page.items.map((course) => <CourseCard key={course.id} course={course} />)}</div>
        ) : (
          <EmptyState
            icon={BookOpen}
            title="O catálogo ainda está vazio"
            description={canCreate ? "Crie um curso para começar a adicionar aulas." : "Os cursos aparecerão aqui quando forem cadastrados."}
          />
        )}
        <Pagination page={page} pathname="/educacao" />
      </section>
      {creating && canCreate && <Suspense fallback={null}><EducationDialog kind="curso" onClose={() => setCreating(false)} /></Suspense>}
    </>
  );
}
