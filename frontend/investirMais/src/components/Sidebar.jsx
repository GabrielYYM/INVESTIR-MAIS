import { Home, LogOut, User } from "lucide-react";
import { logout } from "../services/authService";

const MENU_ITEMS = [
  { label: "Início", icon: Home, page: "Home" },
  { label: "Meu Perfil", icon: User, page: "Perfil" }
];

export default function Sidebar({ activePage = "Home", onNavigate }) {
  return (
    <aside className="w-60 shrink-0 bg-[#171522] px-4 py-6 flex flex-col justify-between min-h-screen border-r border-white/5">
      <div>
        <p className="text-xs tracking-wide text-zinc-500 mb-3 px-2 font-medium">MENU</p>
        <nav className="space-y-1">
          {MENU_ITEMS.map(({ label, icon: Icon, page }) => {
            const isActive = activePage === page;
            return (
              <button
                key={label}
                id={`sidebar-${label.toLowerCase().replace(/\s+/g, "-")}`}
                onClick={() => page && onNavigate?.(page)}
                className={`w-full flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-left transition-colors cursor-pointer ${
                  isActive
                    ? "bg-amber-500 text-zinc-900 font-semibold"
                    : "text-zinc-400 hover:bg-white/5 hover:text-zinc-200"
                }`}
              >
                <Icon size={18} />
                {label}
              </button>
            );
          })}
        </nav>
      </div>

      <button
        type="button"
        onClick={async () => {
          try {
            await logout();
          } finally {
            if (onNavigate) onNavigate("Login");
          }
        }}
        className="flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-zinc-400 hover:bg-white/5 hover:text-red-400 transition-colors cursor-pointer"
      >
        <LogOut size={18} />
        Sair
      </button>
    </aside>
  );
}
