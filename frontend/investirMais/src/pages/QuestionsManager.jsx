import { useState } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import QuestionList from "../components/QuestionList";
import QuestionFormModal from "../components/QuestionFormModal";
import { criarQuestao, atualizarQuestao, excluirQuestao } from "../services/questoesService";

export default function QuestionsManager({
  onNavigate,
  questions,
  categorias = [],
  onDadosChange,
}) {
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [saving, setSaving] = useState(false);

  // Usa a primeira categoria disponível para criar perguntas
  // (pode ser expandido para o usuário escolher a categoria)
  const defaultCategoryId = categorias[0]?.id ?? null;

  const handleOpenCreate = () => {
    setEditingQuestion(null);
    setIsFormOpen(true);
  };

  const handleOpenEdit = (id) => {
    const question = questions.find((q) => q.id === id);
    setEditingQuestion(question);
    setIsFormOpen(true);
  };

  const handleDelete = async (id) => {
    if (confirm("Tem certeza que deseja deletar esta pergunta? Ela será removida das avaliações.")) {
      try {
        await excluirQuestao(id);
        await onDadosChange?.();
      } catch (err) {
        console.error("Erro ao excluir pergunta:", err);
        alert("Não foi possível excluir a pergunta. Tente novamente.");
      }
    }
  };

  const handleSaveQuestion = async (formData) => {
    setSaving(true);
    try {
      if (editingQuestion) {
        await atualizarQuestao(editingQuestion.id, formData.text);
      } else {
        const catId = formData.categoryId ?? defaultCategoryId;
        if (!catId) {
          alert("Nenhuma categoria encontrada. Adicione um ativo primeiro para criar categorias.");
          return;
        }
        await criarQuestao(catId, formData.text);
      }
      await onDadosChange?.();
      setIsFormOpen(false);
    } catch (err) {
      console.error("Erro ao salvar pergunta:", err);
      alert("Não foi possível salvar a pergunta. Tente novamente.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex">
      <Sidebar activePage="Questões" onNavigate={onNavigate} />

      <div className="flex-1">
        <Topbar />

        <main className="px-8 pb-10">
          <div className="flex flex-col gap-6">
            {/* Header */}
            <div>
              <h1 className="text-3xl font-bold text-white mb-1">Perguntas</h1>
              <p className="text-zinc-400">
                Gerencie as perguntas usadas para avaliar seus ativos.
                <span className="ml-2 text-amber-400 font-medium">
                  {questions.length} pergunta{questions.length !== 1 ? "s" : ""} cadastrada{questions.length !== 1 ? "s" : ""}
                </span>
              </p>
            </div>

            {/* Actions */}
            <div className="flex items-center justify-between">
              <button
                id="btn-nova-pergunta"
                type="button"
                onClick={handleOpenCreate}
                className="bg-amber-500 hover:bg-amber-400 transition-colors text-zinc-900 font-medium rounded-full px-5 py-2.5 text-sm cursor-pointer"
              >
                + Nova Pergunta
              </button>

              {questions.length > 0 && (
                <p className="text-xs text-zinc-600">
                  Score = (respostas Positivas / total) × 10
                </p>
              )}
            </div>

            {/* Question List */}
            <QuestionList
              questions={questions}
              onEdit={handleOpenEdit}
              onDelete={handleDelete}
            />

            {/* Modal */}
            <QuestionFormModal
              isOpen={isFormOpen}
              onClose={() => setIsFormOpen(false)}
              onSave={handleSaveQuestion}
              initialData={editingQuestion}
              categorias={categorias}
              saving={saving}
            />
          </div>
        </main>
      </div>
    </div>
  );
}
