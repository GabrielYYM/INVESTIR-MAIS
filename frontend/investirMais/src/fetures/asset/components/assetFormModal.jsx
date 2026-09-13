import { useState, useEffect } from 'react';
import { Modal } from '../../../components/Modal';

export function AssetFormModal({ isOpen, onClose, onSave, initialData }) {
    const [formData, setFormData] = useState({
        tipo: '',
        ticker: '',
        valorAtual: '',
        quantidade: ''
    });

    useEffect(() => {
        if (initialData) setFormData(initialData);
        else setFormData({ tipo: '', ticker: '', valorAtual: '', quantidade: '' });
    }, [initialData]);

    const handleSubmit = (e) => {
        e.preventDefault();
        onSave(formData);
    };

    return (
        <Modal 
            isOpen={isOpen} 
            title={initialData ? 'Editar Ativo' : 'Adicionar Ativo'}
            maxWidth="max-w-lg"
        >
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block text-gray-400 mb-1 text-sm">Categoria / Tipo</label>
                    <input
                        required
                        type="text"
                        value={formData.tipo}
                        onChange={(e) => setFormData({ ...formData, tipo: e.target.value })}
                        className="w-full bg-gray-800 border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-yellow-500 transition-colors"
                        placeholder="Ex: Ações internacionais"
                    />
                </div>

                <div>
                    <label className="block text-gray-400 mb-1 text-sm">Ticker</label>
                    <input
                        required
                        type="text"
                        value={formData.ticker}
                        onChange={(e) => setFormData({ ...formData, ticker: e.target.value.toUpperCase() })}
                        className="w-full bg-gray-800 border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-yellow-500 transition-colors"
                        placeholder="Ex: QQQ"
                    />
                </div>

                <div className="flex gap-4">
                    <div className="flex-1">
                        <label className="block text-gray-400 mb-1 text-sm">Valor Atual (R$)</label>
                        <input
                            required
                            type="number"
                            step="0.01"
                            value={formData.valorAtual}
                            onChange={(e) => setFormData({ ...formData, valorAtual: e.target.value })}
                            className="w-full bg-gray-800 border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-yellow-500 transition-colors"
                        />
                    </div>
                    <div className="flex-1">
                        <label className="block text-gray-400 mb-1 text-sm">Quantidade</label>
                        <input
                            required
                            type="number"
                            step="0.00001"
                            value={formData.quantidade}
                            onChange={(e) => setFormData({ ...formData, quantidade: e.target.value })}
                            className="w-full bg-gray-800 border border-gray-700 rounded-lg p-3 text-white focus:outline-none focus:border-yellow-500 transition-colors"
                        />
                    </div>
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