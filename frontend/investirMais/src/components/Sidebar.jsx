import { Home, Wallet, PiggyBank, Wrench, HelpCircle, LogOut } from "lucide-react";

const MENU_ITEMS = [
  { label: "Home", icon: Home, page: null },
  { label: "Carteira", icon: Wallet, page: "Carteira" },
  { label: "Questões", icon: HelpCircle, page: "Questões" },
  { label: "Orçamento Doméstico", icon: PiggyBank, page: null },
  { label: "Ferramentas", icon: Wrench, page: null },
];

export default function Sidebar({ activePage = "Carteira", onNavigate }) {
  return (
    <aside className="w-60 shrink-0 bg-[#171522] px-4 py-6 flex flex-col justify-between min-h-screen">
      <div>
        <p className="text-xs tracking-wide text-zinc-500 mb-3 px-2">MENU</p>
        <nav className="space-y-1">
          {MENU_ITEMS.map(({ label, icon: Icon, page }) => {
            const isActive = activePage === page;
            return (
              <button
                key={label}
                id={`sidebar-${label.toLowerCase().replace(/\s+/g, "-")}`}
                onClick={() => page && onNavigate?.(page)}
                className={`w-full flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-left transition-colors ${
                  isActive
                    ? "bg-amber-500 text-zinc-900 font-medium"
                    : page
                    ? "text-zinc-400 hover:bg-white/5 hover:text-zinc-200 cursor-pointer"
                    : "text-zinc-600 cursor-not-allowed opacity-50"
                }`}
                disabled={!page}
              >
                <Icon size={18} />
                {label}
              </button>
            );
          })}
        </nav>
      </div>

      <button className="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-zinc-400 hover:bg-white/5 hover:text-zinc-200 cursor-pointer">
        <LogOut size={18} />
        Sair
      </button>
    </aside>
  );
}
