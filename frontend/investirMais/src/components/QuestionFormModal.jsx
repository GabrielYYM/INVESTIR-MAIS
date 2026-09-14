import { useState, useEffect } from "react";
import Modal from "./Modal";

export default function QuestionFormModal({
  isOpen,
  onClose,
  onSave,
  initialData,
}) {
  const [formData, setFormData] = useState({ text: "" });

  useEffect(() => {
    if (initialData) {
      setFormData(initialData);
    } else {
      setFormData({ text: "" });
    }
  }, [initialData, isOpen]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!formData.text.trim()) return;
    onSave(formData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={initialData ? "Editar Pergunta" : "Nova Pergunta"}
      maxWidth="max-w-lg"
    >
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label
            htmlFor="question-text"
            className="block text-zinc-400 mb-2 text-sm"
          >
            Pergunta Qualitativa
          </label>
          <textarea
            id="question-text"
            required
            rows={4}
            value={formData.text}
            onChange={(e) =>
              setFormData((prev) => ({ ...prev, text: e.target.value }))
            }
            placeholder="Ex: A empresa possui lucros consistentes nos últimos 5 anos?"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors resize-none"
          />
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            id="btn-cancelar-pergunta"
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm text-zinc-400 hover:text-zinc-200 hover:bg-white/5 rounded-lg transition-colors cursor-pointer"
          >
            Cancelar
          </button>
          <button
            id="btn-salvar-pergunta"
            type="submit"
            className="bg-amber-500 hover:bg-amber-400 transition-colors text-zinc-900 font-medium rounded-lg px-5 py-2 text-sm cursor-pointer"
          >
            {initialData ? "Salvar Alterações" : "Criar Pergunta"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
