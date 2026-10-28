import { Form, redirect, useActionData, useLoaderData, useNavigate, useNavigation } from "react-router";
import { ApiError } from "../services/api";
import { createCourse, createLesson, getCourseById, getLessonById, updateCourse, updateLesson } from "../generated/spring";
import { requireUser, routeAction, routeLoader } from "../services/routing";
import { mayTeach, mayEdit } from "../utils/permissions";
import { Field, Notice, Submit } from "../components/ui";
import Modal from "../components/modal";

const returnTo = ({ params, request }) => `${params.courseId ? `/cursos/${params.courseId}` : "/cursos"}${new URL(request.url).search}`;
export const loadEditor = (kind) => routeLoader(async (args) => {
  const user = await requireUser(args);
  const { params, request: navigation } = args;
  if (!mayTeach(user)) throw new ApiError(403);
  const course = params.courseId ? await getCourseById(params.courseId, { signal: navigation.signal }) : null;
  if (course && !mayEdit(user, course)) throw new ApiError(403);
  const value = kind === "course" ? course || {} : params.lessonId ? await getLessonById(params.lessonId, { signal: navigation.signal }) : {};
  if (value.courseId && value.courseId !== params.courseId) throw new ApiError(404);
  return { kind, value, returnTo: returnTo(args) };
});
export const saveEditor = (kind) => routeAction(async (form, args) => {
  const id = kind === "course" ? args.params.courseId : args.params.lessonId;
  const fields = kind === "course" ? ["name", "description"] : ["title", "description", "mediaUrl", "thumbnailUrl"];
  const values = Object.fromEntries(fields.map((name) => [name, form.get(name)]));
  const options = { signal: args.request.signal };
  if (id) await (kind === "course" ? updateCourse : updateLesson)(id, values, options);
  else if (kind === "course") await createCourse(values, options);
  else await createLesson(args.params.courseId, values, options);
  return redirect(returnTo(args));
});

export function Component() {
  const { kind, value, returnTo } = useLoaderData();
  const result = useActionData();
  const navigation = useNavigation();
  const navigate = useNavigate();
  const pending = navigation.state !== "idle";
  const course = kind === "course";
  return <Modal isOpen onClose={() => { if (!pending) navigate(returnTo); }} title={`${value.id ? "Editar" : "Criar"} ${course ? "curso" : "aula"}`}>
    <Form method={value.id ? "put" : "post"} replace className="grid gap-4">
      <Notice error={result?.error} />
      <fieldset disabled={pending} className="grid gap-4">
        <Field label={course ? "Nome" : "Título"} name={course ? "name" : "title"} minLength={3} maxLength={100} defaultValue={course ? value.name : value.title} />
        <Field label="Descrição" name="description" multiline rows={4} defaultValue={value.description} />
        {!course && <><Field label="URL do vídeo ou material" name="mediaUrl" type="url" defaultValue={value.mediaUrl} /><Field label="URL da imagem de capa" name="thumbnailUrl" type="url" defaultValue={value.thumbnailUrl} /></>}
        <Submit pending={pending} />
      </fieldset>
    </Form>
  </Modal>;
}
