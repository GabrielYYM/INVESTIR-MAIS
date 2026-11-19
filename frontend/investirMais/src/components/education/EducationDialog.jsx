import VideoUpload from "./VideoUpload.jsx";
import { SelectInput, TextInput } from "react-admin";
import { categoryChoices } from "../../services/courseCategories.js";
import { useLocation, useNavigate, useParams } from "react-router";
import { youtubeVideo, cloudinaryVideo } from "../../services/youtube.js";
import { educationSchema } from "../../services/educationSchema.js";
import { questionPayload } from "../../services/questionSchema.js";
import { loadPagination, pageHref } from "../../services/searchParams.js";
import CrudDialog, { DeleteRecordDialog } from "../ui/CrudDialog.jsx";

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
  const transform = ({ title, description, mediaUrl, questions, category }) => {
    const video = lesson ? youtubeVideo(mediaUrl) ?? (cloudinaryVideo(mediaUrl) ? { watchUrl: mediaUrl, thumbnailUrl: mediaUrl.replace(/\.(mp4|webm|mov)$/, ".jpg") } : null) : null;
    return lesson ? { title, description, mediaUrl: video?.watchUrl ?? mediaUrl, thumbnailUrl: video?.thumbnailUrl ?? item?.thumbnailUrl }
      : { name: title, description, category, ...(!item && questions.length ? { questions: questions.map(questionPayload) } : {}) };
  };
  return <CrudDialog resource={resource} item={item} meta={{ courseId }} title={item ? "Editar " + kind : lesson ? "Nova aula" : "Novo curso"}
    label={"Salvar " + kind} schema={educationSchema(lesson, item?.mediaUrl)} defaults={{ title: (lesson ? item?.title : item?.name) ?? "", description: item?.description ?? "", mediaUrl: item?.mediaUrl ?? "", questions: [], category: item?.category || "Investimentos" }}
    transform={transform} onClose={onClose} onSuccess={!item ? created => navigate(lesson ? coursePath + "/" + created.id : "/educacao/" + created.id) : undefined}>
    <TextInput source="title" label={lesson ? "Título da aula" : "Nome do curso"} fullWidth slotProps={{ htmlInput: { maxLength: 100 } }} />
    <TextInput source="description" label="Descrição" multiline rows={4} fullWidth />
    {lesson && <><TextInput source="mediaUrl" label="URL do vídeo (YouTube ou Cloudinary)" type="url" fullWidth placeholder="https://www.youtube.com/watch?v=…" helperText="Cole um link do YouTube ou envie um vídeo abaixo." />
      <VideoUpload courseId={courseId} />
      <p className="text-xs text-muted">Confira o conteúdo e a reprodução do vídeo antes de publicar. A plataforma não faz análise nem aprovação automática dos vídeos.</p></>}
    {!lesson && <><SelectInput source="category" label="Categoria" choices={categoryChoices(item?.category)} isRequired fullWidth /><p className="text-xs text-muted">Depois de criar o curso, adicione aulas e cadastre as questões dentro de cada aula.</p></>}
  </CrudDialog>;
}
