import { Typography } from "@mui/material";
import { useParams } from "react-router";
import { questionDefaults, questionPayload, questionSchema } from "../services/questionSchema.js";
import CrudDialog, { DeleteRecordDialog } from "./CrudDialog.jsx";
import QuestionFields from "./QuestionFields.jsx";
export default function QuestionDialog({ item, remove, afterLesson = 0, position = 0, onClose }) {
  const { courseId } = useParams();
  if (remove) return <DeleteRecordDialog resource="questions" item={item} meta={{ courseId }} title="Excluir pergunta" onClose={onClose}>
    A pergunta deixará de aparecer no curso e de bloquear o avanço. Deseja excluir {item.prompt}?
  </DeleteRecordDialog>;
  return <CrudDialog resource="questions" item={item} meta={{ courseId }} title={item ? "Editar pergunta" : "Nova pergunta"} label="Salvar pergunta"
    schema={questionSchema} defaults={questionDefaults(item, afterLesson, position)} transform={questionPayload} onClose={onClose}>
    <QuestionFields />{item && <Typography color="warning.main">Ao salvar, os alunos precisarão acertar esta pergunta novamente.</Typography>}
  </CrudDialog>;
}
