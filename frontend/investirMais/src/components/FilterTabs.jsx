const TABS = [
  "Todos",
  "Ações internacionais",
  "Ações nacionais",
  "Fundos Imobiliarios",
  "REITs",
  "Criptomoedas",
  "Renda Fixa",
];

export default function FilterTabs({ ativo, onChange }) {
  return (
    <div className="flex flex-wrap gap-3">
      {TABS.map((tab) => {
        const selecionado = tab === ativo;
        return (
          <button
            key={tab}
            onClick={() => onChange(tab)}
            className={`px-4 py-2 rounded-full text-sm border transition-colors ${
              selecionado
                ? "bg-amber-500 border-amber-500 text-zinc-900 font-medium"
                : "border-amber-500/40 text-amber-400 hover:bg-amber-500/10"
            }`}
          >
            {tab}
          </button>
        );
      })}
    </div>
  );
}
