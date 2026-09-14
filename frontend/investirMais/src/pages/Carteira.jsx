import { useMemo, useState } from "react";
import Sidebar from "../components/Sidebar";
import Topbar from "../components/Topbar";
import WalletCard from "../components/WalletCard";
import FilterTabs from "../components/FilterTabs";
import AssetsTable from "../components/AssetsTable";
import AddAssetModal from "../components/AddAssetModal";
import EditAssetModal from "../components/EditAssetModal";
import EvaluateAssetModal from "../components/EvaluateAssetModal";
import { Search } from "lucide-react";
import { useCarteira } from "../hooks/useCarteira";
import { useScore } from "../hooks/useScore";
import { adicionarAtivo, atualizarAtivo, excluirAtivo, apiClient } from "../services/carteiraService";

export default function Carteira({ onNavigate, questions = [] }) {
  const { ativos, valorTotal, loading, error, recarregar } = useCarteira();
  const { scores, getScore, saveScore } = useScore();

  const [filtroTipo, setFiltroTipo] = useState("Todos");
  const [busca, setBusca] = useState("");

  // Modal — adicionar ativo
  const [addOpen, setAddOpen] = useState(false);
  const [addLoading, setAddLoading] = useState(false);

  // Modal — editar ativo
  const [editingAtivo, setEditingAtivo] = useState(null);
  const [editLoading, setEditLoading] = useState(false);

  // Modal — avaliar ativo
  const [evaluatingAtivo, setEvaluatingAtivo] = useState(null);

  const ativosFiltrados = useMemo(() => {
    return ativos
      .filter((ativo) => {
        const tipoOk =
          filtroTipo === "Todos" ||
          ativo.tipo.toLowerCase() === filtroTipo.toLowerCase();
        const buscaOk = ativo.ticker
          .toLowerCase()
          .includes(busca.toLowerCase());
        return tipoOk && buscaOk;
      })
      .sort((a, b) => {
        const scoreA = scores?.[String(a.id ?? a.ticker)]?.score ?? a.rawScore ?? 0;
        const scoreB = scores?.[String(b.id ?? b.ticker)]?.score ?? b.rawScore ?? 0;
        return scoreB - scoreA;
      });
  }, [ativos, filtroTipo, busca, scores]);

  // Adicionar ativo via API
  const handleAddAtivo = async (novoAtivo) => {
    setAddLoading(true);
    try {
      await adicionarAtivo(novoAtivo);
      await recarregar();
    } finally {
      setAddLoading(false);
    }
  };

  // Editar ativo via API
  const handleEditAtivo = async (ativoAtualizado) => {
    if (!ativoAtualizado.id) return;
    setEditLoading(true);
    try {
      await atualizarAtivo(ativoAtualizado.id, ativoAtualizado);
      await recarregar();
      setEditingAtivo(null);
    } finally {
      setEditLoading(false);
    }
  };

  // Excluir ativo via API
  const handleDeleteAtivo = async (ativo) => {
    if (!ativo.id) return;
    if (!window.confirm(`Tem certeza que deseja excluir o ativo ${ativo.ticker}?`)) {
      return;
    }
    try {
      await excluirAtivo(ativo.id);
      await recarregar();
    } catch (err) {
      alert(`Erro ao excluir ativo: ${err?.response?.data?.message || err?.message}`);
    }
  };

  // Salvar avaliação de um ativo (localStorage + backend)
  const handleSaveScore = async (answers) => {
    if (!evaluatingAtivo) return;
    const id = evaluatingAtivo.id ?? evaluatingAtivo.ticker;
    saveScore(id, answers, questions);

    // Persiste avaliações no backend se o ativo tem UUID real
    if (evaluatingAtivo.id) {
      try {
        const evaluations = Object.entries(answers).map(([questionId, value]) => ({
          questionId,
          isPositive: value === 1,
        }));
        await apiClient.post(`/api/questions/assets/${evaluatingAtivo.id}/evaluations`, evaluations);
      } catch (err) {
        console.warn("Não foi possível persistir avaliação no backend:", err.message);
      }
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex">
      <Sidebar activePage="Carteira" onNavigate={onNavigate} />

      <div className="flex-1">
        <Topbar />

        <main className="px-8 pb-10">
          <div className="flex items-start justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-white mb-1">
                Investimentos
              </h1>
              <p className="text-zinc-400 mb-6">
                Gerencie aqui os ativos que você possui
              </p>

              {/* Busca */}
              <div className="relative w-96 mb-6">
                <Search
                  size={16}
                  className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500"
                />
                <input
                  type="text"
                  value={busca}
                  onChange={(e) => setBusca(e.target.value)}
                  placeholder="Pesquise por ticker..."
                  className="w-full bg-white/5 rounded-full pl-9 pr-4 py-2.5 text-sm text-zinc-200 placeholder:text-zinc-500 outline-none focus:ring-1 focus:ring-amber-500/30 transition-all"
                />
              </div>

              {/* Botão adicionar */}
              <button
                id="btn-adicionar-ativo"
                type="button"
                onClick={() => setAddOpen(true)}
                className="bg-amber-500 hover:bg-amber-400 transition-colors text-zinc-900 font-medium rounded-full px-5 py-2.5 text-sm cursor-pointer"
              >
                + Adicionar ativo
              </button>
            </div>

            <WalletCard valorTotal={valorTotal} />
          </div>

          {/* Filtros */}
          <div className="mb-6">
            <FilterTabs ativo={filtroTipo} onChange={setFiltroTipo} />
          </div>

          {/* Tabela */}
          <AssetsTable
            ativos={ativosFiltrados}
            loading={loading}
            error={error}
            scores={scores}
            onEvaluate={setEvaluatingAtivo}
            onEdit={setEditingAtivo}
            onDelete={handleDeleteAtivo}
          />
        </main>
      </div>

      {/* Modal: adicionar ativo */}
      <AddAssetModal
        isOpen={addOpen}
        onClose={() => setAddOpen(false)}
        onSave={handleAddAtivo}
        loading={addLoading}
      />

      {/* Modal: editar ativo */}
      <EditAssetModal
        isOpen={!!editingAtivo}
        onClose={() => setEditingAtivo(null)}
        ativo={editingAtivo}
        onSave={handleEditAtivo}
        loading={editLoading}
      />

      {/* Modal: avaliar ativo */}
      <EvaluateAssetModal
        isOpen={!!evaluatingAtivo}
        onClose={() => setEvaluatingAtivo(null)}
        ativo={evaluatingAtivo}
        questions={questions}
        savedEntry={evaluatingAtivo ? getScore(evaluatingAtivo.id ?? evaluatingAtivo.ticker) : null}
        onSave={handleSaveScore}
      />
    </div>
  );
}
