import { useState } from 'react';

export function EvaluationSwipe({ asset, questions, onClose, onSave }) {
    const [currentIndex, setCurrentIndex] = useState(0);
    const [answers, setAnswers] = useState([]);

    const currentQuestion = questions[currentIndex];

    const handleAnswer = (isPositive) => {
        const newAnswers = [...answers, { questionId: currentQuestion.id, isPositive }];

        if (currentIndex < questions.length - 1) {
            setAnswers(newAnswers);
            setCurrentIndex(currentIndex + 1);
        } else {
            onSave(asset.id, newAnswers);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
            <div className="bg-[#1A1A24] border border-gray-700 w-full max-w-md rounded-2xl p-8 shadow-2xl relative">

                <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-white">✕</button>

                <div className="text-center mb-8">
                    <p className="text-yellow-500 font-semibold mb-1">Avaliando: {asset?.ticker}</p>
                    <p className="text-gray-400 text-sm">Pergunta {currentIndex + 1} de {questions.length}</p>
                </div>

                <div className="bg-gray-800 rounded-xl p-8 min-h-[200px] flex items-center justify-center text-center mb-8 shadow-inner">
                    <h3 className="text-xl font-medium text-white">
                        {currentQuestion?.text}
                    </h3>
                </div>

                <div className="flex justify-between gap-4">
                    <button
                        onClick={() => handleAnswer(false)}
                        className="flex-1 py-4 bg-red-500/10 text-red-500 border border-red-500/30 rounded-xl hover:bg-red-500 hover:text-white transition-all font-semibold text-lg"
                    >
                        Sim
                    </button>
                    <button
                        onClick={() => handleAnswer(true)}
                        className="flex-1 py-4 bg-green-500/10 text-green-500 border border-green-500/30 rounded-xl hover:bg-green-500 hover:text-white transition-all font-semibold text-lg"
                    >
                        Não
                    </button>
                </div>
            </div>
        </div>
    );
}