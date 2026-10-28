import { ArrowLeft, ExternalLink, Pencil, Plus } from "lucide-react";
import { Link, Outlet, useLoaderData, useRouteLoaderData, useSearchParams } from "react-router";
import { deleteLesson, getCourseById, getLessonsByCourse } from "../generated/spring";
import { loadPage, routeAction, routeLoader } from "../services/routing";
import { mayEdit } from "../utils/permissions";
import { PageHeading, Pagination } from "../components/ui";
import DeleteButton from "../components/DeleteButton";

export const loader = routeLoader(async ({ params, request: navigation }) => {
  const [course, lessons] = await Promise.all([
    getCourseById(params.courseId, { signal: navigation.signal }),
    loadPage((page, options) => getLessonsByCourse(params.courseId, page, options), navigation, { size: 20, sort: "title" }),
  ]);
  return { course, lessons };
});
export const action = routeAction(async (form, { request }) => { await deleteLesson(form.get("id"), { signal: request.signal }); return { deleted: true }; });

export function Component() {
  const { course, lessons } = useLoaderData();
  const user = useRouteLoaderData("session");
  const [search] = useSearchParams();
  const editable = mayEdit(user, course);
  return <>
    <Link className="link mb-5 flex items-center gap-2" to="/cursos"><ArrowLeft size={16} /> Todos os cursos</Link>
    <PageHeading title={course.name} action={editable && <div className="flex flex-wrap gap-3"><Link className="btn-secondary" to={`editar?${search}`}>Editar curso</Link><Link className="btn" to={`aulas/nova?${search}`}><Plus size={18} /> Nova aula</Link></div>}>{course.description}</PageHeading>
    {!lessons.content.length && <p className="panel text-zinc-400">Este curso ainda não tem aulas.</p>}
    <div className="grid gap-4">
      {lessons.content.map((lesson) => <article key={lesson.id} className="panel flex flex-col gap-5 sm:flex-row">
        {/^https?:\/\//i.test(lesson.thumbnailUrl) && <img src={lesson.thumbnailUrl} alt="" loading="lazy" referrerPolicy="no-referrer" className="h-28 w-full rounded-xl object-cover sm:w-44" />}
        <div className="min-w-0 flex-1"><h2 className="text-xl font-semibold">{lesson.title}</h2><p className="mt-2 text-sm whitespace-pre-line text-zinc-400">{lesson.description}</p>
          {/^https?:\/\//i.test(lesson.mediaUrl) && <a className="link mt-4 inline-flex items-center gap-2" href={lesson.mediaUrl} target="_blank" rel="noopener noreferrer">Abrir aula <ExternalLink size={14} /></a>}
        </div>
        {editable && <div className="flex items-start gap-3"><Link className="btn-secondary" aria-label={`Editar ${lesson.title}`} to={`aulas/${lesson.id}/editar?${search}`}><Pencil size={16} /></Link><DeleteButton id={lesson.id} label={lesson.title} confirmation={`Excluir a aula “${lesson.title}”?`} /></div>}
      </article>)}
    </div>
    <Pagination page={lessons.page} />
    <Outlet />
  </>;
}
