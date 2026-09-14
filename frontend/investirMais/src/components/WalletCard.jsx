import { Landmark } from "lucide-react";

function formatarMoeda(valor) {
  return valor.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
}

export default function WalletCard({ valorTotal }) {
  return (
    <div className="bg-[#0e0c14] rounded-2xl p-6 w-72">
      <div className="w-11 h-11 rounded-full bg-amber-500/20 flex items-center justify-center mb-4">
        <Landmark size={20} className="text-amber-400" />
      </div>
      <p className="text-lg text-zinc-200 mb-1">Carteira</p>
      <p className="text-2xl font-bold text-white">{formatarMoeda(valorTotal)}</p>
    </div>
  );
}
