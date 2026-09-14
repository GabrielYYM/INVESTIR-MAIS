import { useState, useEffect } from "react";
import Modal from "./Modal";
import { CheckCircle2, XCircle, MinusCircle } from "lucide-react";

const ANSWER_OPTIONS = [
  { value: 1, label: "Positivo", color: "text-emerald-400 border-emerald-500/40 bg-emerald-500/10 hover:bg-emerald-500/20" },
  { value: 0, label: "Negativo", color: "text-red-400    border-red-500/40    bg-red-500/10    hover:bg-red-500/20" },
];

function calcScore(answers, questions) {
  if (!questions || !questions.length) return 0.0;
  const total = questions.reduce((sum, q) => sum + (answers[q.id] ?? 0), 0);
  return parseFloat(((total / questions.length) * 10).toFixed(1));
}

function ScoreDisplay({ score }) {
  const currentScore = score ?? 0.0;
  const color =
    currentScore >= 7 ? "text-emerald-400" : currentScore >= 4 ? "text-amber-400" : "text-red-400";
  const bg =
    currentScore >= 7 ? "bg-emerald-500" : currentScore >= 4 ? "bg-amber-500" : "bg-red-500";

  return (
    <div className="rounded-xl bg-white/5 border border-white/10 p-4 flex items-center justify-between">
      <div>
        <p className="text-xs text-zinc-500 mb-1">Score calculado</p>
        <p className={`text-3xl font-bold ${color}`}>{currentScore.toFixed(1)}<span className="text-base text-zinc-500">/10</span></p>
      </div>
      <div className="w-32 h-2 bg-white/10 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full ${bg} transition-all duration-500`}
          style={{ width: `${currentScore * 10}%` }}
        />
      </div>
    </div>
  );
}

export default function EvaluateAssetModal({ isOpen, onClose, ativo, questions, savedEntry, onSave }) {
  const [answers, setAnswers] = useState({});

  // Carrega respostas salvas ao abrir
  useEffect(() => {
    if (isOpen) {
      setAnswers(savedEntry?.answers ?? {});
    }
  }, [isOpen, savedEntry]);

  const setAnswer = (questionId, value) =>
    setAnswers((prev) => ({ ...prev, [questionId]: value }));

  const handleSave = () => {
    onSave(answers);
    onClose();
  };

  const score = calcScore(answers, questions);
  const totalRespondidas = questions.filter((q) => answers[q.id] !== undefined).length;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={`Avaliar: ${ativo?.ticker ?? "Ativo"}`}
      maxWidth="max-w-xl"
    >
      <div className="space-y-5">
        {/* Score ao vivo */}
        <ScoreDisplay score={score} />

        {/* Progresso */}
        <p className="text-xs text-zinc-500">
          {totalRespondidas} de {questions.length} pergunta{questions.length !== 1 ? "s" : ""} respondida{totalRespondidas !== 1 ? "s" : ""}
        </p>

        {/* Sem perguntas */}
        {questions.length === 0 && (
          <div className="rounded-xl bg-white/5 py-8 text-center">
            <p className="text-zinc-500 text-sm">
              Nenhuma pergunta cadastrada. Vá até &ldquo;Questões&rdquo; para adicionar.
            </p>
          </div>
        )}

        {/* Lista de perguntas */}
        <div className="space-y-3 max-h-72 overflow-y-auto pr-1">
          {questions.map((q, idx) => (
            <div key={q.id} className="rounded-xl bg-white/5 border border-white/5 p-4">
              <p className="text-sm text-zinc-200 mb-3">
                <span className="text-zinc-600 font-mono text-xs mr-2">{idx + 1}.</span>
                {q.text}
              </p>
              <div className="flex gap-2">
                {ANSWER_OPTIONS.map((opt) => {
                  const selected = answers[q.id] === opt.value;
                  return (
                    <button
                      key={opt.label}
                      id={`eval-q${q.id}-${opt.label.toLowerCase()}`}
                      type="button"
                      onClick={() => setAnswer(q.id, opt.value)}
                      className={`flex-1 rounded-lg border px-3 py-2 text-xs font-medium transition-all cursor-pointer ${opt.color} ${
                        selected ? "ring-2 ring-offset-1 ring-offset-[#1e1c2a] ring-current scale-105 shadow-lg" : "opacity-60"
                      }`}
                    >
                      {opt.label}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>

        {/* Ações */}
        <div className="flex justify-end gap-3 pt-1 border-t border-white/5">
          <button
            id="btn-cancelar-avaliacao"
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm text-zinc-400 hover:text-zinc-200 hover:bg-white/5 rounded-lg transition-colors cursor-pointer"
          >
            Cancelar
          </button>
          <button
            id="btn-salvar-avaliacao"
            type="button"
            onClick={handleSave}
            disabled={!questions.length}
            className="bg-amber-500 hover:bg-amber-400 disabled:opacity-40 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-lg px-5 py-2 text-sm cursor-pointer"
          >
            Salvar Avaliação
          </button>
        </div>
      </div>
    </Modal>
  );
}
