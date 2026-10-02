import { useState, useEffect } from "react";
import Modal from "./Modal";

export default function EditAssetModal({ isOpen, onClose, ativo, onSave, loading }) {
  const [ticker, setTicker] = useState("");
  const [quantidade, setQuantidade] = useState("");
  const [valorAtual, setValorAtual] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    if (ativo) {
      setTicker(ativo.ticker || "");
      setQuantidade(ativo.quantidade != null ? String(ativo.quantidade) : "");
      setValorAtual(ativo.valorAtual != null ? String(ativo.valorAtual) : "");
      setError("");
    }
  }, [ativo]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const qty = parseFloat(quantidade);
    const val = parseFloat(valorAtual);

    if (!ticker.trim()) return setError("Informe o ticker do ativo.");
    if (isNaN(qty) || qty < 0) return setError("Quantidade deve ser maior ou igual a zero.");
    if (isNaN(val) || val < 0) return setError("Valor atual deve ser maior ou igual a zero.");

    try {
      await onSave({
        ...ativo,
        ticker: ticker.trim().toUpperCase(),
        quantidade: qty,
        valorAtual: val,
        currentPositionValue: val,
        quantity: qty,
      });
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Erro ao atualizar ativo.");
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Editar Ativo - ${ativo?.ticker || ""}`} maxWidth="max-w-md">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Ticker */}
        <div>
          <label htmlFor="edit-ativo-ticker" className="block text-zinc-400 text-sm mb-2">
            Ticker
          </label>
          <input
            id="edit-ativo-ticker"
            type="text"
            required
            value={ticker}
            onChange={(e) => setTicker(e.target.value)}
            placeholder="Ex: PETR4"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors uppercase"
          />
        </div>

        {/* Quantidade */}
        <div>
          <label htmlFor="edit-ativo-quantidade" className="block text-zinc-400 text-sm mb-2">
            Quantidade
          </label>
          <input
            id="edit-ativo-quantidade"
            type="number"
            required
            min="0"
            step="any"
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
            placeholder="Ex: 10"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
          />
        </div>

        {/* Valor Atual */}
        <div>
          <label htmlFor="edit-ativo-valor" className="block text-zinc-400 text-sm mb-2">
            Valor Atual (R$)
          </label>
          <input
            id="edit-ativo-valor"
            type="number"
            required
            min="0"
            step="any"
            value={valorAtual}
            onChange={(e) => setValorAtual(e.target.value)}
            placeholder="Ex: 150.00"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
          />
        </div>

        {/* Mensagem de Erro */}
        {error && (
          <p className="text-sm text-red-400 bg-red-500/10 rounded-lg px-4 py-2">
            {error}
          </p>
        )}

        {/* Botões */}
        <div className="flex justify-end gap-3 pt-2">
          <button
            id="btn-cancelar-editar-ativo"
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm text-zinc-400 hover:text-zinc-200 hover:bg-white/5 rounded-lg transition-colors cursor-pointer"
          >
            Cancelar
          </button>
          <button
            id="btn-salvar-editar-ativo"
            type="submit"
            disabled={loading}
            className="bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-lg px-5 py-2 text-sm cursor-pointer"
          >
            {loading ? "Salvando..." : "Salvar alterações"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
