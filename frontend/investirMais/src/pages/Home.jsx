import Sidebar from "../components/Sidebar.jsx";
import Topbar from "../components/Topbar.jsx";
import { BookOpen, TrendingUp, Award, Play } from "lucide-react";

export default function Home({ onNavigate, user }) {
  return (
    <div className="flex min-h-screen bg-[#171522] text-zinc-100">
      <Sidebar activePage="Home" onNavigate={onNavigate} />

      <div className="flex-1 flex flex-col">
        <Topbar />

        <main className="p-8 max-w-6xl w-full mx-auto space-y-8">
          <div className="bg-[#1e1c2a] border border-white/5 rounded-2xl p-8">
            <h1 className="text-3xl font-bold text-white mb-2">
              Olá, {user?.name || "Investidor"}!
            </h1>
            <p className="text-zinc-400 max-w-xl">
              Bem-vindo ao <span className="text-amber-400 font-semibold">Investir+</span>. Explore os cursos sobre finanças e acompanhe o mercado financeiro em tempo real.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
            <div className="bg-[#1e1c2a] border border-white/5 rounded-2xl p-6 flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-amber-500/20 text-amber-400 flex items-center justify-center shrink-0">
                <BookOpen size={24} />
              </div>
              <div>
                <p className="text-xs text-zinc-500 uppercase tracking-wider font-semibold">Educação</p>
                <p className="text-lg font-bold text-white">Cursos & Aulas</p>
              </div>
            </div>

            <div className="bg-[#1e1c2a] border border-white/5 rounded-2xl p-6 flex items-center gap-4">
              <div className="w-12 h-12 rounded-xl bg-blue-500/20 text-blue-400 flex items-center justify-center shrink-0">
                <Award size={24} />
              </div>
              <div>
                <p className="text-xs text-zinc-500 uppercase tracking-wider font-semibold">Perfil</p>
                <p className="text-lg font-bold text-white">{user?.role || "STUDENT"}</p>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

