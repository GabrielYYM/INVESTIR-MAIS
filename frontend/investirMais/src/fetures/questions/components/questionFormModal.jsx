import { useState, useEffect } from 'react';
import { Modal } from '../../../components/Modal';

export function QuestionFormModal({ isOpen, onClose, onSave, initialData }) {
    const [formData, setFormData] = useState({
        text: ''
    });

    useEffect(() => {
        if (initialData) {
            setFormData(initialData);
        } else {
            setFormData({ text: '' });
        }
    }, [initialData, isOpen]);

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title={initialData ? 'Editar Pergunta' : 'Nova Pergunta'}
            maxWidth="max-w-lg"
        >
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-gray-400 mb-1 text-sm">Pergunta Qualitativa</label>
                    <textarea
                        required
                        rows="3"
                        value={formData.text}
                        onChange={(e) => setFormData({ ...formData, text: e.target.value })}
                        className="w-full bg-gray-800 border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-yellow-500 resize-none"
                        placeholder="Ex: A empresa possui vantagens competitivas claras?"
                    />
                </div>

                <div className="flex justify-end gap-3 mt-8">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-6 py-2 rounded-lg text-gray-400 hover:text-white transition-colors"
                    >
                        Cancelar
                    </button>
                    <button
                        type="submit"
                        className="rounded-lg bg-[#e29f26] px-6 py-2 font-semibold text-white transition-colors hover:bg-[#c98b1d] shadow-lg"
                    >
                        Salvar
                    </button>
                </div>
            </form>
        </Modal>
    );
}