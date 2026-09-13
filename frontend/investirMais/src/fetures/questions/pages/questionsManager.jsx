import { useState } from 'react';
import { QuestionList } from '../components/questionList';
import { QuestionFormModal } from '../components/questionFormModal';

const MOCK_QUESTIONS = [
    { id: 1, text: 'A empresa possui lucros consistentes nos últimos 5 anos?' },
    { id: 2, text: 'Possui uma boa governança corporativa?' },
    { id: 3, text: 'O setor de atuação tem barreiras de entrada claras?' }
];

export function QuestionsManager() {
    const [questions, setQuestions] = useState(MOCK_QUESTIONS);

    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingQuestion, setEditingQuestion] = useState(null);

    const handleOpenCreate = () => {
        setEditingQuestion(null);
        setIsFormOpen(true);
    };

    const handleOpenEdit = (id) => {
        const question = questions.find(q => q.id === id);
        setEditingQuestion(question);
        setIsFormOpen(true);
    };

    const handleDelete = (id) => {
        if (confirm('Tem certeza que deseja deletar esta pergunta? Ela será removida das avaliações.')) {
            setQuestions(questions.filter(q => q.id !== id));
        }
    };

    const handleSaveQuestion = (formData) => {
        if (editingQuestion) {
            setQuestions(questions.map(q =>
                q.id === editingQuestion.id ? { ...q, ...formData } : q
            ));
        } else {
            setQuestions([...questions, { id: Date.now(), ...formData }]);
        }

        setIsFormOpen(false);
    };

    return (
        <div className="flex flex-col gap-6">
            <div>
                <h1 className="mb-2 font-['Poppins'] text-[32px] font-semibold tracking-[0.3px]">Perguntas</h1>
                <p className="font-['Poppins'] text-[20px] text-white/80">Gerencie as perguntas que serão usadas para avaliar seus ativos.</p>
            </div>

            <div className="flex items-center justify-between">
                <button
                    type="button"
                    onClick={handleOpenCreate}
                    className="h-[39px] w-[200px] rounded-[10px] bg-[#e29f26] hover:bg-[#d18e1b] px-5 font-['Figtree'] text-[15px] font-bold text-white transition-colors cursor-pointer"
                >
                    Nova Pergunta
                </button>
            </div>

            <QuestionList
                questions={questions}
                onEdit={handleOpenEdit}
                onDelete={handleDelete}
            />

            <QuestionFormModal
                isOpen={isFormOpen}
                onClose={() => setIsFormOpen(false)}
                onSave={handleSaveQuestion}
                initialData={editingQuestion}
            />
        </div>
    );
}