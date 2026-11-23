import { useMemo, useState } from "react";
import { CheckCircle2, Clock, Search, Users, X } from "lucide-react";
import AppModal from "../ui/AppModal.jsx";
import CourseProgressBar from "./CourseProgressBar.jsx";

export default function StudentsProgressDialog({ students = [], onClose }) {
  const [search, setSearch] = useState("");
  const [filter, setFilter] = useState("ALL");

  const completedCount = useMemo(() => students.filter((s) => s.completed).length, [students]);
  const inProgressCount = students.length - completedCount;

  const filteredStudents = useMemo(() => {
    return students.filter((student) => {
      if (filter === "COMPLETED" && !student.completed) return false;
      if (filter === "IN_PROGRESS" && student.completed) return false;

      if (search.trim()) {
        const query = search.trim().toLowerCase();
        const name = (student.name || "").toLowerCase();
        const id = (student.id || "").toLowerCase();
        if (!name.includes(query) && !id.includes(query)) return false;
      }

      return true;
    });
  }, [students, filter, search]);

  return (
    <AppModal
      title="Progresso individual dos alunos"
      width={680}
      onClose={onClose}
    >
      <div className="space-y-4">
        {/* Barra de pesquisa e filtros */}
        <div className="space-y-3">
          <div className="relative">
            <Search size={16} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted" />
            <input
              type="text"
              placeholder="Buscar aluno por nome..."
              aria-label="Buscar aluno por nome"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full rounded-xl border border-line bg-canvas py-2.5 pl-9 pr-9 text-sm text-ink placeholder:text-muted focus:border-brand focus:outline-none"
            />
            {search && (
              <button
                type="button"
                onClick={() => setSearch("")}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted hover:text-ink"
                aria-label="Limpar busca"
              >
                <X size={15} />
              </button>
            )}
          </div>

          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex flex-wrap items-center gap-1.5 rounded-xl border border-line bg-canvas p-1">
              {[
                ["ALL", `Todos (${students.length})`],
                ["COMPLETED", `Concluídos (${completedCount})`],
                ["IN_PROGRESS", `Em andamento (${inProgressCount})`],
              ].map(([key, label]) => (
                <button
                  key={key}
                  type="button"
                  onClick={() => setFilter(key)}
                  className={`rounded-lg px-3 py-1.5 text-xs font-medium transition-colors ${
                    filter === key
                      ? "bg-brand font-semibold text-zinc-900 shadow-sm"
                      : "text-ink hover:bg-raised"
                  }`}
                >
                  {label}
                </button>
              ))}
            </div>

            <span className="text-xs text-muted">
              Exibindo <strong>{filteredStudents.length}</strong> de <strong>{students.length}</strong>
            </span>
          </div>
        </div>

        {/* Lista de alunos */}
        <div className="max-h-[60vh] space-y-3 overflow-y-auto pr-1">
          {filteredStudents.length === 0 ? (
            <div className="rounded-xl border border-line bg-canvas/50 p-8 text-center text-muted">
              <Users size={28} className="mx-auto mb-2 text-muted/60" />
              <p className="font-medium text-ink">Nenhum aluno encontrado.</p>
              <p className="mt-0.5 text-xs">
                {students.length === 0
                  ? "Ainda não há alunos matriculados neste curso."
                  : "Tente ajustar a busca ou os filtros de progresso."}
              </p>
            </div>
          ) : (
            filteredStudents.map((student) => {
              const initial = (student.name || "A").charAt(0).toUpperCase();
              return (
                <article
                  key={student.id}
                  className="space-y-3 rounded-xl border border-line bg-canvas/60 p-4 transition-colors hover:border-line/80"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div className="flex items-center gap-2.5 min-w-0">
                      <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-white/10 text-xs font-bold text-ink">
                        {initial}
                      </div>
                      <h3 className="truncate font-semibold text-ink">
                        {student.name || "Aluno " + student.id.slice(0, 8)}
                      </h3>
                    </div>
                    {student.completed ? (
                      <span className="inline-flex items-center gap-1 rounded-full border border-emerald-500/30 bg-emerald-500/15 px-2.5 py-0.5 text-xs font-semibold text-emerald-400 shrink-0">
                        <CheckCircle2 size={12} /> Concluído
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 rounded-full border border-amber-500/30 bg-amber-500/10 px-2.5 py-0.5 text-xs font-semibold text-amber-400 shrink-0">
                        <Clock size={12} /> Em andamento ({student.progress_percentage}%)
                      </span>
                    )}
                  </div>

                  <CourseProgressBar
                    value={student.progress_percentage}
                    label={"Progresso de " + (student.name || student.id.slice(0, 8))}
                    completed={student.completed}
                  />

                  <div className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1 text-xs text-muted">
                    <span>
                      {student.completed_lessons} de {student.total_lessons} aulas · {student.passed_questions} de {student.total_questions} questões aprovadas
                    </span>
                    <span>
                      {student.attempts} respostas · {student.accuracy ?? 0}% de acerto
                    </span>
                  </div>
                </article>
              );
            })
          )}
        </div>
      </div>
    </AppModal>
  );
}
