import { lazy, Suspense, useState } from "react";
import { BookOpen, Plus, Search } from "lucide-react";
import { useLoaderData, useRouteLoaderData, Form, useSearchParams } from "react-router";
import { CourseCard, Pagination } from "../components/education/EducationParts.jsx";
import PageHeader from "../components/layout/PageHeader.jsx";
import EmptyState from "../components/ui/EmptyState.jsx";
import { Field, SelectField } from "../components/ui/FormFields.jsx";
import { canManageEducation } from "../services/educationData.js";
import { categoryChoices } from "../services/courseCategories.js";

const EducationDialog = lazy(() => import("../components/education/EducationDialog.jsx"));

export { catalogLoader } from "../services/educationData.js";

const sortOptions = [
  ["name", "Nome"],
  ["newest", "Mais recentes"],
  ["oldest", "Mais antigos"],
];

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

      <Form method="get" className="panel grid grid-cols-1 items-end gap-4 p-5 sm:grid-cols-2 lg:grid-cols-[1.5fr_1fr_1fr_auto]" key={search.toString()}>
        <Field
          label="Buscar curso"
          name="busca"
          icon={Search}
          placeholder="Nome ou assunto do curso..."
          defaultValue={search.get("busca") || ""}
          maxLength={100}
        />
        <SelectField
          label="Categoria"
          name="categoria"
          defaultValue={search.get("categoria") || ""}
          emptyOption="Todas as categorias"
          options={categoryChoices(search.get("categoria"))}
        />
        <SelectField
          label="Ordenar por"
          name="ordem"
          defaultValue={search.get("ordem") || "name"}
          options={sortOptions}
        />
        <button type="submit" className="button-primary h-11">Pesquisar cursos</button>
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
