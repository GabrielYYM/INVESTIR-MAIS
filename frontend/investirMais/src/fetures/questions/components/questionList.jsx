export function QuestionList({ questions, onEdit, onDelete }) {
    if (questions.length === 0) {
        return (
            <div className="rounded-[46px] bg-[#0d0d0d] py-10 text-center">
                <p className="text-gray-500">Nenhuma pergunta cadastrada.</p>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {questions.map((question) => (
                <div
                    key={question.id}
                    className="flex items-center justify-between rounded-xl bg-[#0d0d0d] p-5"
                >
                    <div className="flex-1 pr-4">
                        <p className="text-white text-lg">{question.text}</p>
                    </div>

                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => onEdit(question.id)}
                            className="text-gray-400 hover:text-white px-3 py-1 bg-gray-800 rounded transition-colors text-sm"
                        >
                            Editar
                        </button>
                        <button
                            onClick={() => onDelete(question.id)}
                            className="text-red-400 hover:text-red-300 px-3 py-1 bg-red-500/10 rounded transition-colors text-sm"
                        >
                            Deletar
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
}