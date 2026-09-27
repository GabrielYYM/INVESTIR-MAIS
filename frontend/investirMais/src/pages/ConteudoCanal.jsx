import { useEffect, useState } from "react";
import { MoreVertical, Pencil, Trash2 } from "lucide-react";
import Sidebar from "../components/Sidebar.jsx";
import Topbar from "../components/Topbar.jsx";
import UploadVideoModal from "../components/UploadVideoModal.jsx";
import ConfirmDialog from "../components/ConfirmDialog.jsx";
import { getMeusConteudos, getMeusModulos, excluirConteudo } from "../services/educationService";

function formatarData(iso) {
  return new Date(iso).toLocaleDateString("pt-BR", { day: "2-digit", month: "long", year: "numeric" });
}

export default function ConteudoCanal({ onNavigate, onLogout, usuario }) {
  const [conteudos, setConteudos] = useState([]);
  const [modulos, setModulos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [menuAbertoId, setMenuAbertoId] = useState(null);
  const [modalAberto, setModalAberto] = useState(false);
  const [editando, setEditando] = useState(null); // conteúdo sendo editado, ou null = novo upload
  const [excluindoId, setExcluindoId] = useState(null);

  async function carregar() {
    setLoading(true);
    try {
      const [conteudosData, modulosData] = await Promise.all([getMeusConteudos(), getMeusModulos()]);
      setConteudos(conteudosData);
      setModulos(modulosData);
    } catch (err) {
      console.error("Erro ao carregar conteúdos:", err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    carregar();
  }, []);

  function abrirNovoUpload() {
    setEditando(null);
    setModalAberto(true);
  }

  function abrirEdicao(conteudo) {
    setEditando(conteudo);
    setMenuAbertoId(null);
    setModalAberto(true);
  }

  async function confirmarExclusao() {
    try {
      await excluirConteudo(excluindoId);
      setExcluindoId(null);
      carregar();
    } catch (err) {
      console.error("Erro ao excluir:", err);
      alert("Não foi possível excluir. Tente novamente.");
    }
  }

  return (
    <div className="flex min-h-screen bg-[#0f0e17]">
      <Sidebar activePage="Video" onNavigate={onNavigate} onLogout={onLogout} role={usuario?.role} />

      <div className="flex-1">
        <Topbar usuario={usuario} />

        <main className="px-8 pb-12">
          <div className="bg-[#171522] rounded-2xl p-6">
            <h1 className="text-lg font-semibold text-white mb-4">Conteúdo do Canal</h1>

            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-zinc-500 text-xs uppercase">
                  <th className="pb-3 font-normal" colSpan={2}></th>
                  <th className="pb-3 font-normal">status</th>
                  <th className="pb-3 font-normal">data</th>
                  <th className="pb-3 font-normal text-right">opções</th>
                </tr>
              </thead>
              <tbody>
                {loading && (
                  <tr>
                    <td colSpan={5} className="text-zinc-500 py-6 text-center">
                      Carregando...
                    </td>
                  </tr>
                )}

                {!loading && conteudos.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-zinc-500 py-6 text-center">
                      Nenhum vídeo enviado ainda.
                    </td>
                  </tr>
                )}

                {conteudos.map((item) => (
                  <tr key={item.id} className="border-t border-white/5">
                    <td className="py-3 w-16">
                      <div className="w-14 h-9 rounded bg-zinc-700 overflow-hidden">
                        {item.thumbnailUrl && (
                          <img src={item.thumbnailUrl} alt="" className="w-full h-full object-cover" />
                        )}
                      </div>
                    </td>
                    <td className="py-3">
                      <p className="text-zinc-100">{item.title}</p>
                      <p className="text-xs text-zinc-500">{item.description || "adicionar descrição"}</p>
                    </td>
                    <td className="py-3">
                      <span
                        className={`text-xs px-2 py-1 rounded-full ${
                          item.status === "PUBLISHED"
                            ? "bg-emerald-500/15 text-emerald-400"
                            : "bg-zinc-500/15 text-zinc-400"
                        }`}
                      >
                        {item.status === "PUBLISHED" ? "Publicado" : "Rascunho"}
                      </span>
                    </td>
                    <td className="py-3 text-zinc-400 text-xs">{formatarData(item.createdAt)}</td>
                    <td className="py-3 text-right relative">
                      <button
                        onClick={() => setMenuAbertoId(menuAbertoId === item.id ? null : item.id)}
                        className="text-zinc-400 hover:text-white p-1"
                      >
                        <MoreVertical size={16} />
                      </button>

                      {menuAbertoId === item.id && (
                        <div className="absolute right-0 mt-1 w-32 bg-[#232130] rounded-lg shadow-lg z-10 py-1 text-left">
                          <button
                            onClick={() => abrirEdicao(item)}
                            className="w-full flex items-center gap-2 px-3 py-2 text-xs text-zinc-200 hover:bg-white/5"
                          >
                            <Pencil size={14} /> Editar
                          </button>
                          <button
                            onClick={() => {
                              setMenuAbertoId(null);
                              setExcluindoId(item.id);
                            }}
                            className="w-full flex items-center gap-2 px-3 py-2 text-xs text-red-400 hover:bg-white/5"
                          >
                            <Trash2 size={14} /> Excluir
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div className="flex justify-end mt-4">
              <button
                onClick={abrirNovoUpload}
                className="bg-amber-500 hover:bg-amber-400 text-zinc-900 font-medium rounded-full px-5 py-2 text-sm"
              >
                upload
              </button>
            </div>
          </div>
        </main>
      </div>

      <UploadVideoModal
        open={modalAberto}
        initialContent={editando}
        modulos={modulos}
        onClose={() => setModalAberto(false)}
        onSaved={carregar}
      />

      <ConfirmDialog
        open={Boolean(excluindoId)}
        title="Excluir este vídeo? Essa ação não pode ser desfeita."
        confirmLabel="Excluir"
        onCancel={() => setExcluindoId(null)}
        onConfirm={confirmarExclusao}
      />
    </div>
  );
}
