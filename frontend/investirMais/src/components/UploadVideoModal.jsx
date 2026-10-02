import { useRef, useState } from "react";
import { Upload, FileText, X as XIcon, AlertCircle } from "lucide-react";
import ConfirmDialog from "./ConfirmDialog.jsx";
import {
  getUploadSignature,
  criarConteudo,
  atualizarConteudo,
  publicarConteudo,
} from "../services/educationService";
import { uploadVideoToCloudinary, validarArquivoVideo } from "../services/cloudinaryUpload";

/**
 * `initialContent`: quando presente, o modal abre direto na etapa "details"
 * para EDITAR um conteúdo já existente (veio do "Editar" no menu de opções
 * da listagem) — pula toda a etapa de upload.
 */
export default function UploadVideoModal({ open, onClose, onSaved, modulos = [], initialContent = null }) {
  const [step, setStep] = useState(initialContent ? "details" : "select");
  const [progress, setProgress] = useState(0);
  const [fileName, setFileName] = useState(initialContent?.mediaUrl ? "" : "");
  const [fileError, setFileError] = useState("");
  const [contentId, setContentId] = useState(initialContent?.id ?? null);
  const [uploadHandle, setUploadHandle] = useState(null); // { abort }

  const [title, setTitle] = useState(initialContent?.title ?? "");
  const [description, setDescription] = useState(initialContent?.description ?? "");
  const [moduleId, setModuleId] = useState(initialContent?.moduleId ?? "");
  const [thumbnailPreview, setThumbnailPreview] = useState(initialContent?.thumbnailUrl ?? null);
  const [saving, setSaving] = useState(false);

  const [confirmExitOpen, setConfirmExitOpen] = useState(false);
  const fileInputRef = useRef(null);
  const thumbInputRef = useRef(null);

  if (!open) return null;

  function resetAndClose() {
    setStep(initialContent ? "details" : "select");
    setProgress(0);
    setFileError("");
    setConfirmExitOpen(false);
    onClose?.();
  }

  function handleCloseClick() {
    // Só pede confirmação quando há algo em risco de se perder:
    // upload em andamento, ou já em "details" preenchendo dados.
    if (step === "uploading" || step === "details") {
      setConfirmExitOpen(true);
    } else {
      resetAndClose();
    }
  }

  function confirmExit() {
    uploadHandle?.abort?.();
    resetAndClose();
  }

  async function handleFileSelected(e) {
    const file = e.target.files?.[0];
    if (!file) return;

    const erro = validarArquivoVideo(file);
    if (erro) {
      setFileError(erro);
      return;
    }
    setFileError("");
    setFileName(file.name);
    setStep("uploading");
    setProgress(0);

    try {
      const signatureData = await getUploadSignature();
      const { promise, abort } = uploadVideoToCloudinary(file, signatureData, setProgress);
      setUploadHandle({ abort });

      const cloudinaryResult = await promise;

      const created = await criarConteudo({
        title: file.name.replace(/\.[^/.]+$/, ""), // nome do arquivo sem extensão, editável depois
        type: "VIDEO",
        mediaUrl: cloudinaryResult.secure_url,
      });

      setContentId(created.id);
      setTitle(created.title);
      setStep("details");
    } catch (err) {
      console.error(err);
      setStep("error");
    }
  }

  function handleThumbnailSelected(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    // NOTA: preview local apenas. Persistir a miniatura no Cloudinary exige
    // um segundo upload preset (para imagens) + endpoint de assinatura próprio,
    // já que o preset atual só aceita mp4/mov/webm. Ver observação na resposta.
    const reader = new FileReader();
    reader.onload = () => setThumbnailPreview(reader.result);
    reader.readAsDataURL(file);
  }

  async function handlePublicar() {
    if (!title.trim()) return;
    setSaving(true);
    try {
      await atualizarConteudo(contentId, {
        title,
        description,
        thumbnailUrl: thumbnailPreview?.startsWith("data:") ? null : thumbnailPreview,
        moduleId: moduleId || null,
        orderInModule: null,
      });
      await publicarConteudo(contentId);
      onSaved?.();
      resetAndClose();
    } catch (err) {
      console.error(err);
      alert("Não foi possível publicar o vídeo. Tente novamente.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center bg-black/50 p-4">
      <div className="bg-[#171522] text-white rounded-2xl w-full max-w-md">
        <div className="flex items-center justify-between px-6 py-4 border-b border-white/10">
          <h2 className="font-semibold">
            {step === "details" ? "Detalhes do vídeo" : step === "error" ? "Erro ao enviar o vídeo" : "Enviar o vídeo"}
          </h2>
          <button onClick={handleCloseClick} className="text-zinc-400 hover:text-white">
            <XIcon size={18} />
          </button>
        </div>

        <div className="p-6">
          {step === "select" && (
            <>
              <div className="flex flex-col items-center py-8">
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="w-16 h-16 rounded-full bg-white/10 flex items-center justify-center mb-4 hover:bg-white/15"
                >
                  <Upload size={24} />
                </button>
                <button
                  onClick={() => fileInputRef.current?.click()}
                  className="bg-amber-500 hover:bg-amber-400 text-zinc-900 font-medium rounded-full px-5 py-2 text-sm"
                >
                  Selecionar o Arquivo
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="video/mp4,video/quicktime,video/webm"
                  className="hidden"
                  onChange={handleFileSelected}
                />
                <p className="text-xs text-zinc-500 mt-3">Vídeo até 100MB · MP4, MOV ou WebM</p>
                {fileError && (
                  <p className="text-xs text-red-400 mt-2 flex items-center gap-1">
                    <AlertCircle size={14} /> {fileError}
                  </p>
                )}
              </div>
              <p className="text-[11px] text-zinc-500 text-center leading-relaxed">
                Ao enviar seus vídeos para o Investir Mais, você concorda com os{" "}
                <span className="text-amber-500">Termos de Serviço</span> e com as{" "}
                <span className="text-amber-500">diretrizes</span> do Investir Mais. Verifique se
                seus vídeos não violam a privacidade ou os direitos autorais de terceiros.{" "}
                <span className="text-amber-500">Saiba mais</span>
              </p>
            </>
          )}

          {step === "uploading" && (
            <div className="py-8">
              <div className="flex items-center gap-3 mb-3">
                <FileText size={20} className="text-zinc-400" />
                <div className="flex-1">
                  <p className="text-sm">{fileName}</p>
                  <p className="text-xs text-zinc-500">{progress < 100 ? "Loading" : "Finalizando..."}</p>
                </div>
              </div>
              <div className="w-full h-1.5 bg-white/10 rounded-full overflow-hidden">
                <div
                  className="h-full bg-blue-500 transition-all"
                  style={{ width: `${progress}%` }}
                />
              </div>
            </div>
          )}

          {step === "details" && (
            <div className="space-y-4">
              <div>
                <label className="text-xs text-zinc-400">Título (obrigatório)</label>
                <input
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  className="w-full bg-white/5 rounded-lg px-3 py-2 text-sm mt-1 outline-none focus:ring-1 focus:ring-amber-500"
                />
              </div>
              <div>
                <label className="text-xs text-zinc-400">Descrição</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={2}
                  className="w-full bg-white/5 rounded-lg px-3 py-2 text-sm mt-1 outline-none focus:ring-1 focus:ring-amber-500"
                />
              </div>
              <div>
                <label className="text-xs text-zinc-400">Adicionar ao módulo</label>
                <select
                  value={moduleId}
                  onChange={(e) => setModuleId(e.target.value)}
                  className="w-full bg-white/5 rounded-lg px-3 py-2 text-sm mt-1 outline-none focus:ring-1 focus:ring-amber-500"
                >
                  <option value="">Selecionar</option>
                  {modulos.map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex items-center gap-4">
                <div>
                  <label className="text-xs text-zinc-400 block mb-1">Miniatura</label>
                  <button
                    onClick={() => thumbInputRef.current?.click()}
                    className="w-20 h-14 rounded-lg border border-dashed border-zinc-600 flex items-center justify-center text-zinc-500 hover:border-amber-500 overflow-hidden"
                  >
                    {thumbnailPreview ? (
                      <img src={thumbnailPreview} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <Upload size={16} />
                    )}
                  </button>
                  <input
                    ref={thumbInputRef}
                    type="file"
                    accept="image/*"
                    className="hidden"
                    onChange={handleThumbnailSelected}
                  />
                </div>
                <p className="text-[11px] text-zinc-500">
                  Defina uma miniatura que se destaque e chame a atenção dos espectadores
                </p>
              </div>

              <button
                onClick={handlePublicar}
                disabled={saving || !title.trim()}
                className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-full py-2.5 text-sm mt-2"
              >
                {saving ? "Publicando..." : "Publicar"}
              </button>
            </div>
          )}

          {step === "error" && (
            <div className="flex flex-col items-center py-8">
              <div className="w-16 h-16 rounded-full bg-white/10 flex items-center justify-center mb-4">
                <XIcon size={28} className="text-red-500" />
              </div>
              <p className="font-medium mb-4">Falha de Upload</p>
              <button
                onClick={() => setStep("select")}
                className="bg-amber-500 hover:bg-amber-400 text-zinc-900 font-medium rounded-full px-5 py-2 text-sm"
              >
                Tente Novamente
              </button>
            </div>
          )}
        </div>
      </div>

      <ConfirmDialog
        open={confirmExitOpen}
        onCancel={() => setConfirmExitOpen(false)}
        onConfirm={confirmExit}
      />
    </div>
  );
}
