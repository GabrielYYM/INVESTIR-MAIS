import { Search } from "lucide-react";

export default function Topbar() {
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
    </header>
  );
}

