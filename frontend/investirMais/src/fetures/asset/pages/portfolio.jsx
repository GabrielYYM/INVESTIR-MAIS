import { useState } from 'react';
import { AssetTable } from '../components/assetTable';
import { AssetFormModal } from '../components/assetFormModal';
import { EvaluationSwipe } from '../components/evaluationSwipe';
import { figmaAssets } from '../../../assets/figma';
import { FigmaIcon } from '../../../components/FigmaIcon';

const CATEGORIES = [
    'Todos',
    'Ações internacionais',
    'Ações nacionais',
    'Fundos Imobiliarios',
    'REITs',
    'Criptomoedas',
    'Renda Fixa',
];

const MOCK_ASSETS = [
    { id: 1, tipo: 'Ações internacionais', ticker: 'QQQ', valorAtual: 9241.64, percentual: 41.38, nota: 28, quantidade: 2.51895 },
    { id: 2, tipo: 'Ações Nacionais', ticker: 'FLRY3', valorAtual: 3335.20, percentual: 15.07, nota: 28, quantidade: 176 },
    { id: 3, tipo: 'Ações Nacionais', ticker: 'WEGE3', valorAtual: 2502.24, percentual: 11.31, nota: 26, quantidade: 52 },
    { id: 4, tipo: 'Ações internacionais', ticker: 'MCHI', valorAtual: 4431.26, percentual: 19.84, nota: 28, quantidade: 2.51895 },
];

const MOCK_QUESTIONS = [
    { id: 1, text: 'Qual é a perspectiva de valorização?' },
    { id: 2, text: 'Tem um nível de risco?' },
    { id: 3, text: 'Tem a capacidade de gerar retorno no longo prazo?' },
    { id: 4, text: 'Esse ativo proporciona diversificação?' },
];

const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

export function Portfolio() {
    const [assets, setAssets] = useState(MOCK_ASSETS);
    const [query, setQuery] = useState('');
    const [category, setCategory] = useState('Todos');
    const [isFormOpen, setIsFormOpen] = useState(false);
    const [editingAsset, setEditingAsset] = useState(null);
    const [isEvaluating, setIsEvaluating] = useState(false);
    const [assetToEvaluate, setAssetToEvaluate] = useState(null);

    const handleOpenCreate = () => {
        setEditingAsset(null);
        setIsFormOpen(true);
    };

    const handleOpenEdit = (id) => {
        const asset = assets.find((a) => a.id === id);
        setEditingAsset(asset);
        setIsFormOpen(true);
    };

    const handleDelete = (id) => {
        if (window.confirm('Deseja realmente deletar este ativo?')) {
            setAssets(assets.filter((a) => a.id !== id));
        }
    };

    const handleSaveAsset = (formData) => {
        const parsedData = {
            ...formData,
            valorAtual: parseFloat(formData.valorAtual) || 0,
            quantidade: parseFloat(formData.quantidade) || 0,
        };
        if (editingAsset) {
            setAssets(assets.map((a) => (a.id === editingAsset.id ? { ...a, ...parsedData } : a)));
        } else {
            setAssets([...assets, { id: Date.now(), nota: 0, percentual: 0, ...parsedData }]);
        }
        setIsFormOpen(false);
    };

    const handleOpenEvaluate = (id) => {
        const asset = assets.find((a) => a.id === id);
        setAssetToEvaluate(asset);
        setIsEvaluating(true);
    };

    const handleSaveEvaluation = (assetId, answers) => {
        const score = answers.filter((answer) => answer.isPositive).length;
        setAssets(assets.map((a) => (a.id === assetId ? { ...a, nota: score } : a)));
        setIsEvaluating(false);
        setAssetToEvaluate(null);
    };

    const filteredAssets = assets.filter((asset) => {
        const matchesCategory =
            category === 'Todos' ||
            asset.tipo.toLowerCase() === category.toLowerCase();
        const haystack = `${asset.tipo} ${asset.ticker}`.toLowerCase();
        return matchesCategory && haystack.includes(query.toLowerCase());
    });



    return (
        <div className="flex flex-col gap-6">
            {/* Parte 1: Cabeçalho com Ações e Resumo da Carteira */}
            <div className="flex flex-wrap items-start justify-between gap-6">
                <div>
                    <h1 className="font-['Poppins'] text-[32px] font-semibold tracking-[0.3px]">Investimentos</h1>
                    <p className="mt-2 font-['Poppins'] text-[20px] text-white/80">Gerencie aqui os ativos que você possui</p>

                    <label className="relative mt-6 block w-full max-w-[403px]">
                        <input
                            type="search"
                            value={query}
                            onChange={(e) => setQuery(e.target.value)}
                            placeholder="Pesquise"
                            className="h-10 w-full rounded-[10px] border-2 border-white/10 bg-transparent px-3 pr-10 font-['Poppins'] text-[12px] tracking-[0.5px] text-white placeholder:text-white/60 focus:border-white/30 focus:outline-none transition-colors"
                        />
                        <span className="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2">
                            <FigmaIcon src={figmaAssets.iconSearchPage} outer={[12, 12]} leaf={[12.84, 13.32]} />
                        </span>
                    </label>

                    <button
                        type="button"
                        onClick={handleOpenCreate}
                        className="mt-10 h-[39px] w-[200px] rounded-[10px] bg-[#e29f26] hover:bg-[#d18e1b] px-5 font-['Figtree'] text-[15px] font-bold text-white transition-colors cursor-pointer"
                    >
                        Adicionar ativo
                    </button>
                </div>


            </div>

            {/* Parte 2: Filtros de Categoria e Tabela de Ativos */}
            <div className="space-y-4">
                <div className="flex flex-wrap gap-[15px]">
                    {CATEGORIES.map((item) => {
                        const isActive = category === item;
                        return (
                            <button
                                key={item}
                                type="button"
                                onClick={() => setCategory(item)}
                                className={`rounded-[10px] px-5 py-2.5 font-['Figtree'] text-[15px] transition-all cursor-pointer ${
                                    isActive
                                        ? 'bg-[#e29f26] font-bold text-white'
                                        : 'border border-[#e29f26]/50 font-normal text-[#e29f26] hover:bg-[#e29f26]/10'
                                }`}
                            >
                                {item}
                            </button>
                        );
                    })}
                </div>

                <AssetTable
                    assets={filteredAssets}
                    onEdit={handleOpenEdit}
                    onDelete={handleDelete}
                    onEvaluate={handleOpenEvaluate}
                />
            </div>

            <AssetFormModal
                isOpen={isFormOpen}
                onClose={() => setIsFormOpen(false)}
                onSave={handleSaveAsset}
                initialData={editingAsset}
            />

            <EvaluationSwipe
                isOpen={isEvaluating}
                asset={assetToEvaluate}
                questions={MOCK_QUESTIONS}
                onClose={() => setIsEvaluating(false)}
                onSave={handleSaveEvaluation}
            />
        </div>
    );
}
