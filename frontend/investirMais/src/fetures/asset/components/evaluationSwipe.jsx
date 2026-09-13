import { useEffect, useState } from 'react';
import { figmaAssets } from '../../../assets/figma';
import { FigmaIcon } from '../../../components/FigmaIcon';

export function EvaluationSwipe({ isOpen, asset, questions, onClose, onSave }) {
    const [answers, setAnswers] = useState({});

    useEffect(() => {
        if (isOpen) setAnswers({});
    }, [isOpen, asset?.id]);

    if (!isOpen || !asset || !questions?.length) return null;

    const handleAnswer = (questionId, isPositive) => {
        const next = { ...answers, [questionId]: isPositive };
        setAnswers(next);
        if (questions.every((question) => next[question.id] !== undefined)) {
            onSave(
                asset.id,
                questions.map((question) => ({
                    questionId: question.id,
                    isPositive: next[question.id],
                })),
            );
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/55" onClick={onClose}>
            <div
                className="w-full max-w-[473px] rounded-[46px] bg-[#0d0d0d] px-4 py-8 shadow-2xl"
                onClick={(event) => event.stopPropagation()}
            >
                <div className="mb-8 flex items-start justify-between px-1">
                    <h2 className="font-['Poppins'] text-[24px] font-semibold tracking-[0.3px]">Perguntas</h2>
                    <div className="flex items-center gap-3">
                        <FigmaIcon src={figmaAssets.iconFilter} outer={[32, 32]} leaf={[32, 32]} />
                        <FigmaIcon src={figmaAssets.iconSort} outer={[32, 32]} leaf={[32, 32]} />
                    </div>
                </div>

                <ul className="space-y-6">
                    {questions.map((question) => {
                        const value = answers[question.id];
                        return (
                            <li key={question.id} className="flex items-center justify-between gap-4">
                                <p className="font-['Poppins'] text-[12px] font-medium tracking-[0.3px] text-white">
                                    {question.text}
                                </p>
                                <div className="flex shrink-0 items-center gap-4">
                                    <button
                                        type="button"
                                        aria-label="Resposta positiva"
                                        onClick={() => handleAnswer(question.id, true)}
                                        className={`rounded-md ${value === true ? 'ring-2 ring-[#e29f26]' : 'opacity-80'}`}
                                    >
                                        <FigmaIcon src={figmaAssets.iconThumbsUp} outer={[24, 24]} leaf={[22.18, 22.5]} />
                                    </button>
                                    <button
                                        type="button"
                                        aria-label="Resposta negativa"
                                        onClick={() => handleAnswer(question.id, false)}
                                        className={`rounded-md ${value === false ? 'ring-2 ring-[#e29f26]' : 'opacity-80'}`}
                                    >
                                        <FigmaIcon src={figmaAssets.iconThumbsDown} outer={[24, 24]} leaf={[22.68, 23]} />
                                    </button>
                                </div>
                            </li>
                        );
                    })}
                </ul>
            </div>
        </div>
    );
}
