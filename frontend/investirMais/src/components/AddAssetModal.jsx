import { useState } from "react";
import Modal from "./Modal";

const TIPOS = [
  "Ações internacionais",
  "Ações nacionais",
  "Fundos Imobiliarios",
  "REITs",
  "Criptomoedas",
  "Renda Fixa",
];

const EMPTY = { tipo: "Ações nacionais", ticker: "", quantidade: "" };

export default function AddAssetModal({ isOpen, onClose, onSave, loading }) {
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState("");

  const set = (field) => (e) =>
    setForm((prev) => ({ ...prev, [field]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const qty = parseFloat(form.quantidade);
    if (!form.ticker.trim()) return setError("Informe o ticker do ativo.");
    if (isNaN(qty) || qty <= 0) return setError("Quantidade deve ser maior que zero.");

    try {
      await onSave({
        tipo: form.tipo,
        ticker: form.ticker.trim().toUpperCase(),
        quantidade: qty,
      });
      setForm(EMPTY);
      onClose();
    } catch (err) {
      setError(err?.response?.data?.message || err?.message || "Erro ao adicionar ativo.");
    }
  };

  const handleClose = () => {
    setForm(EMPTY);
    setError("");
    onClose();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Adicionar Ativo" maxWidth="max-w-md">
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Tipo */}
        <div>
          <label htmlFor="ativo-tipo" className="block text-zinc-400 text-sm mb-2">
            Tipo
          </label>
          <select
            id="ativo-tipo"
            value={form.tipo}
            onChange={set("tipo")}
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
          >
            {TIPOS.map((t) => (
              <option key={t} value={t} className="bg-[#1e1c2a]">
                {t}
              </option>
            ))}
          </select>
        </div>

        {/* Ticker */}
        <div>
          <label htmlFor="ativo-ticker" className="block text-zinc-400 text-sm mb-2">
            Ticker
          </label>
          <input
            id="ativo-ticker"
            type="text"
            required
            value={form.ticker}
            onChange={set("ticker")}
            placeholder="Ex: PETR4, QQQ, BTC"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors uppercase"
          />
        </div>

        {/* Quantidade */}
        <div>
          <label htmlFor="ativo-quantidade" className="block text-zinc-400 text-sm mb-2">
            Quantidade
          </label>
          <input
            id="ativo-quantidade"
            type="number"
            required
            min="0.000001"
            step="any"
            value={form.quantidade}
            onChange={set("quantidade")}
            placeholder="Ex: 10"
            className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
          />
        </div>

        {/* Erro */}
        {error && (
          <p className="text-sm text-red-400 bg-red-500/10 rounded-lg px-4 py-2">
            {error}
          </p>
        )}

        {/* Ações */}
        <div className="flex justify-end gap-3 pt-2">
          <button
            id="btn-cancelar-ativo"
            type="button"
            onClick={handleClose}
            className="px-4 py-2 text-sm text-zinc-400 hover:text-zinc-200 hover:bg-white/5 rounded-lg transition-colors cursor-pointer"
          >
            Cancelar
          </button>
          <button
            id="btn-salvar-ativo"
            type="submit"
            disabled={loading}
            className="bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-lg px-5 py-2 text-sm cursor-pointer"
          >
            {loading ? "Adicionando..." : "Adicionar"}
          </button>
        </div>
      </form>
    </Modal>
  );
}
