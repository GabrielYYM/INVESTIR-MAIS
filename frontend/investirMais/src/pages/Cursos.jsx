import { BookOpen, Pencil, Plus } from "lucide-react";
import { Link, Outlet, useLoaderData, useRouteLoaderData, useSearchParams } from "react-router";
import { deleteCourse, getAllCourses } from "../generated/spring";
import { loadPage, routeAction, routeLoader } from "../services/routing";
import { mayTeach, mayEdit } from "../utils/permissions";
import { PageHeading, Pagination } from "../components/ui";
import DeleteButton from "../components/DeleteButton";

export const loader = routeLoader(({ request }) => loadPage(getAllCourses, request));
export const action = routeAction(async (form, { request }) => { await deleteCourse(form.get("id"), { signal: request.signal }); return { deleted: true }; });

export function Component() {
  const data = useLoaderData();
  const user = useRouteLoaderData("session");
  const [search] = useSearchParams();
  const suffix = `?${search}`;
  return <>
    <PageHeading title="Cursos" action={mayTeach(user) && <Link className="btn" to={`novo${suffix}`}><Plus size={18} /> Novo curso</Link>}>Conhecimento para tomar melhores decisões.</PageHeading>
    {!data.content.length && <p className="panel text-zinc-400">Nenhum curso disponível no momento.</p>}
    <div className="grid gap-5 md:grid-cols-2">
      {data.content.map((course) => <article key={course.id} className="panel flex flex-col gap-5">
        <BookOpen className="text-amber-400" size={28} />
        <div className="flex-1"><h2 className="text-xl font-semibold">{course.name}</h2><p className="mt-2 line-clamp-3 text-sm text-zinc-400">{course.description}</p></div>
        <div className="flex flex-wrap items-center justify-between gap-3"><Link className="btn-secondary" to={course.id}>Ver aulas</Link>{mayEdit(user, course) && <div className="flex gap-3">
          <Link className="btn-secondary" aria-label={`Editar ${course.name}`} to={`${course.id}/editar${suffix}`}><Pencil size={16} /></Link>
          <DeleteButton id={course.id} label={course.name} confirmation={`Excluir “${course.name}” e todas as suas aulas?`} />
        </div>}</div>
      </article>)}
    </div>
    <Pagination page={data.page} />
    <Outlet />
  </>;
}
