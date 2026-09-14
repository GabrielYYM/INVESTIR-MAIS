import { Pencil, Trash2 } from "lucide-react";

export default function QuestionList({ questions, onEdit, onDelete }) {
  if (questions.length === 0) {
    return (
      <div className="bg-[#0e0c14] rounded-2xl py-12 text-center">
        <p className="text-zinc-500">
          Nenhuma pergunta cadastrada. Clique em &ldquo;+ Nova Pergunta&rdquo;
          para começar.
        </p>
      </div>
    );
  }

  return (
    <div className="bg-[#0e0c14] rounded-2xl overflow-hidden">
      <div className="px-6 py-5 border-b border-white/5">
        <h2 className="text-xl font-bold text-white">Lista de Perguntas</h2>
      </div>
      <div className="divide-y divide-white/5">
        {questions.map((question, index) => (
          <div
            key={question.id}
            className="flex items-center justify-between px-6 py-4 hover:bg-white/[0.02] transition-colors"
          >
            <div className="flex items-center gap-4">
              <span className="text-xs text-zinc-600 font-mono w-5 text-right shrink-0">
                {index + 1}
              </span>
              <p className="text-zinc-200 text-sm">{question.text}</p>
            </div>

            <div className="flex items-center gap-2 shrink-0 ml-4">
              <button
                id={`btn-editar-${question.id}`}
                type="button"
                onClick={() => onEdit(question.id)}
                className="flex items-center gap-1.5 rounded-lg bg-white/10 px-3 py-1.5 text-xs text-zinc-300 hover:bg-white/20 transition-colors cursor-pointer"
              >
                <Pencil size={12} />
                Editar
              </button>
              <button
                id={`btn-excluir-${question.id}`}
                type="button"
                onClick={() => onDelete(question.id)}
                className="flex items-center gap-1.5 rounded-lg bg-red-500/20 px-3 py-1.5 text-xs text-red-400 hover:bg-red-500/30 transition-colors cursor-pointer"
              >
                <Trash2 size={12} />
                Excluir
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
