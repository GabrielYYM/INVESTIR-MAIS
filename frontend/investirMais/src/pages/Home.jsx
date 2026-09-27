import { useEffect, useState } from "react";
import { Play } from "lucide-react";
import Sidebar from "../components/Sidebar.jsx";
import Topbar from "../components/Topbar.jsx";
import { getConteudoPublicado } from "../services/educationService";

// Cores rotativas pros cards de destaque, já que o modelo não tem
// conceito de "cor do card" — puramente visual, sem vir do backend.
const DESTAQUE_CORES = ["bg-teal-500", "bg-red-500", "bg-indigo-500"];

export default function Home({ onNavigate, onLogout, onSelectContent, usuario }) {
  const [conteudos, setConteudos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    getConteudoPublicado()
      .then((data) => {
        if (ativo) setConteudos(data);
      })
      .catch(() => {
        if (ativo) setErro("Não foi possível carregar os conteúdos agora.");
      })
      .finally(() => {
        if (ativo) setLoading(false);
      });
    return () => {
      ativo = false;
    };
  }, []);

  const destaques = conteudos.slice(0, 2);
  const maisVistos = conteudos.slice(2);

  return (
    <div className="flex min-h-screen bg-[#0f0e17]">
      <Sidebar activePage="Home" onNavigate={onNavigate} onLogout={onLogout} role={usuario?.role} />

      <div className="flex-1">
        <Topbar usuario={usuario} />

        <main className="px-8 pb-12">
          <h1 className="text-2xl font-bold text-white mb-4">Descubra</h1>

          {loading && <p className="text-zinc-500 text-sm">Carregando...</p>}
          {erro && <p className="text-red-400 text-sm">{erro}</p>}

          {!loading && !erro && destaques.length === 0 && (
            <p className="text-zinc-500 text-sm">Nenhum conteúdo publicado ainda.</p>
          )}

          {destaques.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-10">
              {destaques.map((item, i) => (
                <button
                  key={item.id}
                  onClick={() => onSelectContent?.(item)}
                  className={`relative rounded-2xl p-6 text-left overflow-hidden min-h-[220px] flex flex-col justify-between ${
                    DESTAQUE_CORES[i % DESTAQUE_CORES.length]
                  }`}
                >
                  <h2 className="text-xl font-bold text-white max-w-[70%]">{item.title}</h2>
                  <span className="absolute bottom-4 right-4 bg-black/40 text-white text-xs px-2 py-1 rounded-full">
                    {item.type === "VIDEO" ? "vídeo" : "artigo"}
                  </span>
                </button>
              ))}
            </div>
          )}

          {maisVistos.length > 0 && (
            <>
              <h2 className="text-lg font-semibold text-white mb-4">Mais vistos</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                {maisVistos.map((item) => (
                  <button
                    key={item.id}
                    onClick={() => onSelectContent?.(item)}
                    className="text-left rounded-xl overflow-hidden bg-[#171522] hover:bg-[#1f1d2e] transition-colors"
                  >
                    <div className="relative aspect-video bg-amber-500 flex items-center justify-center">
                      {item.thumbnailUrl ? (
                        <img
                          src={item.thumbnailUrl}
                          alt={item.title}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <Play className="text-zinc-900/40" size={32} />
                      )}
                    </div>
                    <div className="p-3">
                      <p className="text-sm text-zinc-100 font-medium line-clamp-2">{item.title}</p>
                      <p className="text-xs text-zinc-500 mt-1">{item.professorName}</p>
                    </div>
                  </button>
                ))}
              </div>
            </>
          )}
        </main>
      </div>
    </div>
  );
}
