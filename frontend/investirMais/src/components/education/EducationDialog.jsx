import { TextInput } from "react-admin";
import { useLocation, useNavigate, useParams } from "react-router";
import { youtubeVideo } from "../../services/youtube.js";
import { educationSchema } from "../../services/educationSchema.js";
import { questionDefaults, questionPayload } from "../../services/questionSchema.js";
import { loadPagination, pageHref } from "../../services/searchParams.js";
import CrudDialog, { DeleteRecordDialog } from "../ui/CrudDialog.jsx";
import { CourseQuestionFields } from "./QuestionFields.jsx";

export default function EducationDialog({ kind, item, remove, onClose }) {
  const { courseId, lessonId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const lesson = kind === "aula";
  const resource = lesson ? "lessons" : "courses";
  const coursePath = "/educacao/" + courseId;
  if (remove) return <DeleteRecordDialog resource={resource} item={item} meta={{ courseId }} title={"Excluir " + kind} onClose={onClose}
    onSuccess={!lesson ? () => navigate("/educacao", { replace: true }) : item.id === lessonId ? () => navigate(pageHref(coursePath, loadPagination(new URLSearchParams(location.search)).pagina), { replace: true }) : undefined}>
    {lesson ? "A aula deixará de aparecer no curso." : "O curso e suas aulas deixarão de aparecer no catálogo."} Deseja excluir {item.title || item.name}?
  </DeleteRecordDialog>;
  const transform = ({ title, description, mediaUrl, questions }) => {
    const video = lesson ? youtubeVideo(mediaUrl) : null;
    return lesson ? { title, description, mediaUrl: video?.watchUrl ?? mediaUrl, thumbnailUrl: video?.thumbnailUrl ?? item?.thumbnailUrl }
      : { name: title, description, ...(!item && questions.length ? { questions: questions.map(questionPayload) } : {}) };
  };
  return <CrudDialog resource={resource} item={item} meta={{ courseId }} title={item ? "Editar " + kind : lesson ? "Nova aula" : "Novo curso"}
    label={"Salvar " + kind} schema={educationSchema(lesson, item?.mediaUrl)} defaults={{ title: (lesson ? item?.title : item?.name) ?? "", description: item?.description ?? "", mediaUrl: item?.mediaUrl ?? "", questions: [] }}
    transform={transform} onClose={onClose} onSuccess={!item ? created => navigate(lesson ? coursePath + "/" + created.id : "/educacao/" + created.id) : undefined}>
    <TextInput source="title" label={lesson ? "Título da aula" : "Nome do curso"} fullWidth slotProps={{ htmlInput: { maxLength: 100 } }} />
    <TextInput source="description" label="Descrição" multiline rows={4} fullWidth />
    {lesson && <><TextInput source="mediaUrl" label="URL do vídeo no YouTube" type="url" fullWidth placeholder="https://www.youtube.com/watch?v=…" helperText="Use um vídeo público ou não listado de um canal aprovado pelo administrador para o professor responsável." />
      <p className="text-xs text-muted">O administrador seleciona os canais de economia, finanças e investimentos. O vídeo precisa permitir reprodução incorporada no Brasil e não ter restrição etária. Confira se o conteúdo é adequado à aula antes de publicar.</p></>}
    {!lesson && !item && <CourseQuestionFields defaults={index => questionDefaults(undefined, 0, index)} />}
  </CrudDialog>;
}
