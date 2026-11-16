import { useMutation } from "@tanstack/react-query";
import { useFormContext } from "react-hook-form";
import { useState } from "react";
import { apiClient, apiError } from "../../services/api.js";
import { Notice } from "../ui/PageState.jsx";

export default function VideoUpload({ courseId }) {
  const { setValue } = useFormContext();
  const [validation, setValidation] = useState("");
  const upload = useMutation({ mutationFn: async file => {
    const body = new FormData(); body.append("file", file);
    return (await apiClient.post(`/api/courses/${courseId}/videos`, body, { timeout: 180000 })).data;
  }, onSuccess: video => { setValue("mediaUrl", video.watchUrl, { shouldDirty: true, shouldValidate: true }); } });
  return <div className="space-y-2"><label className="block space-y-2"><span>Ou enviar vídeo para o Cloudinary</span><input type="file" accept="video/mp4,video/webm,video/quicktime" disabled={upload.isPending} onChange={event => {
    const file = event.target.files[0]; if (!file) return;
    if (file.size > 100 * 1024 * 1024) { setValidation("O vídeo deve ter até 100 MB."); return; }
    setValidation(""); upload.mutate(file);
  }} /></label>
  {upload.isPending && <p role="status">Enviando vídeo… Aguarde antes de salvar a aula.</p>}
  {upload.isSuccess && <Notice>Vídeo enviado. Salve a aula para vinculá-lo ao curso.</Notice>}
  {(validation || upload.error) && <Notice error>{validation || apiError(upload.error, "Não foi possível enviar o vídeo.")}</Notice>}
  </div>;
}
