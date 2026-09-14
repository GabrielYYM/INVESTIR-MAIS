import { Search, Bell, ChevronDown } from "lucide-react";

export default function Topbar({ usuario }) {
  return (
    <header className="flex items-center justify-between px-8 py-5">
      <span className="font-bold tracking-wide text-white">INVESTIR MAIS</span>

      <div className="flex-1 max-w-md mx-8 relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
        <input
          type="text"
          placeholder="Pesquise"
          className="w-full bg-white/5 rounded-full pl-9 pr-4 py-2 text-sm text-zinc-200 placeholder:text-zinc-500 outline-none"
        />
      </div>

      <div className="flex items-center gap-4">
        <button className="relative text-zinc-300">
          <Bell size={20} />
          <span className="absolute -top-0.5 -right-0.5 w-2 h-2 rounded-full bg-red-500" />
        </button>
        <div className="flex items-center gap-2">
          <img
            src={usuario?.avatarUrl || "https://i.pravatar.cc/40"}
            alt={usuario?.nome}
            className="w-8 h-8 rounded-full object-cover"
          />
          <span className="text-sm text-zinc-200">{usuario?.nome || "Usuário"}</span>
          <ChevronDown size={16} className="text-zinc-500" />
        </div>
      </div>
    </header>
  );
}
