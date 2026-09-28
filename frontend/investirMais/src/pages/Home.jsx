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
  const [selectedContent, setSelectedContent] = useState(null);

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

  const handleCardClick = (item) => {
    setSelectedContent(item);
    if (onSelectContent) onSelectContent(item);
  };

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

          {!loading && !erro && conteudos.length === 0 && (
            <p className="text-zinc-500 text-sm">Nenhum conteúdo publicado ainda.</p>
          )}

          {destaques.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-10">
              {destaques.map((item, i) => (
                <button
                  key={item.id}
                  onClick={() => handleCardClick(item)}
                  className={`relative rounded-2xl p-6 text-left overflow-hidden min-h-[220px] flex flex-col justify-between cursor-pointer ${
                    DESTAQUE_CORES[i % DESTAQUE_CORES.length]
                  }`}
                >
                  <h2 className="text-xl font-bold text-white max-w-[70%]">{item.title}</h2>
                  <span className="absolute bottom-4 right-4 bg-black/40 text-white text-xs px-3 py-1 rounded-full flex items-center gap-1.5">
                    <Play size={12} />
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
                    onClick={() => handleCardClick(item)}
                    className="text-left rounded-xl overflow-hidden bg-[#171522] hover:bg-[#1f1d2e] transition-colors cursor-pointer group"
                  >
                    <div className="relative aspect-video bg-amber-500 flex items-center justify-center">
                      {item.thumbnailUrl ? (
                        <img
                          src={item.thumbnailUrl}
                          alt={item.title}
                          className="w-full h-full object-cover"
                        />
                      ) : (
                        <Play className="text-zinc-900/40 group-hover:scale-110 transition-transform" size={32} />
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

          {/* Modal de Player de Vídeo / Leitor de Conteúdo para o Aluno */}
          {selectedContent && (
            <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4">
              <div className="bg-[#171522] border border-white/10 rounded-2xl w-full max-w-3xl overflow-hidden shadow-2xl">
                {/* Visualizador de Mídia */}
                <div className="relative aspect-video bg-black flex items-center justify-center">
                  {selectedContent.mediaUrl ? (
                    <video
                      src={selectedContent.mediaUrl}
                      controls
                      autoPlay
                      className="w-full h-full object-contain"
                    />
                  ) : (
                    <div className="p-8 text-center text-zinc-400">
                      <Play size={48} className="mx-auto mb-2 opacity-50" />
                      <p>Mídia não disponível para reprodução direta.</p>
                    </div>
                  )}
                </div>

                {/* Detalhes do Conteúdo */}
                <div className="p-6">
                  <div className="flex items-start justify-between gap-4 mb-2">
                    <div>
                      <h2 className="text-xl font-bold text-white">{selectedContent.title}</h2>
                      <p className="text-xs text-amber-500 mt-0.5">Professor: {selectedContent.professorName || "Investir+"}</p>
                    </div>
                    <button
                      onClick={() => setSelectedContent(null)}
                      className="text-zinc-400 hover:text-white bg-white/5 hover:bg-white/10 px-3 py-1.5 rounded-lg text-xs transition-colors cursor-pointer"
                    >
                      Fechar
                    </button>
                  </div>
                  {selectedContent.description && (
                    <p className="text-sm text-zinc-300 mt-3 leading-relaxed border-t border-white/5 pt-3">
                      {selectedContent.description}
                    </p>
                  )}
                </div>
              </div>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
