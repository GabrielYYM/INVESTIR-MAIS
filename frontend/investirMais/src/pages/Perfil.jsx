import { useState, useEffect } from "react";
import Sidebar from "../components/Sidebar.jsx";
import Topbar from "../components/Topbar.jsx";
import { User, Mail, Lock, Eye, EyeOff, Save, CheckCircle2, AlertCircle } from "lucide-react";
import { getUserIdFromToken, getUserProfile, updateUserProfile } from "../services/authService";

export default function Perfil({ onNavigate, usuario }) {
  const [profile, setProfile] = useState({
    id: "",
    name: "",
    email: "",
    role: usuario?.role || "",
  });

  const [form, setForm] = useState({
    name: "",
    email: "",
    currentPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  useEffect(() => {
    async function loadUser() {
      const userId = getUserIdFromToken();
      if (!userId) {
        if (onNavigate) onNavigate("Login");
        return;
      }

      try {
        setLoading(true);
        const data = await getUserProfile(userId);
        setProfile(data);
        setForm({
          name: data.name || "",
          email: data.email || "",
          currentPassword: "",
        });
      } catch (err) {
        console.error("Erro ao carregar perfil:", err);
        setMessage({
          type: "error",
          text: "Não foi possível carregar os dados do seu perfil.",
        });
      } finally {
        setLoading(false);
      }
    }

    loadUser();
  }, [onNavigate]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage({ type: "", text: "" });

    if (!form.name || !form.currentPassword) {
      return setMessage({
        type: "error",
        text: "Preencha o nome e sua senha atual para confirmar.",
      });
    }

    setSaving(true);
    try {
      const userId = profile.id || getUserIdFromToken();
      const updated = await updateUserProfile(userId, {
        name: form.name,
        email: profile.email,
        currentPassword: form.currentPassword,
      });

      setProfile(updated);
      setForm((prev) => ({ ...prev, currentPassword: "" }));
      setMessage({
        type: "success",
        text: "Perfil atualizado com sucesso!",
      });
    } catch (err) {
      console.error("Erro ao atualizar perfil:", err);
      const errMsg =
        err?.response?.data?.detail ||
        err?.response?.data?.message ||
        err.message ||
        "Erro ao atualizar perfil. Verifique se a sua senha atual está correta.";
      setMessage({ type: "error", text: errMsg });
    } finally {
      setSaving(false);
    }
  };

  const currentRole = usuario?.role || profile?.role;

  return (
    <div className="flex min-h-screen bg-[#171522] text-zinc-100 font-sans">
      <Sidebar activePage="Perfil" onNavigate={onNavigate} role={currentRole} />

      <div className="flex-1 flex flex-col min-w-0">
        <Topbar usuario={usuario || profile} />

        <main className="flex-1 px-8 py-6 max-w-4xl w-full mx-auto">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-white">Meu Perfil</h1>
            <p className="text-sm text-zinc-400 mt-1">
              Gerencie suas informações pessoais e credenciais de acesso.
            </p>
          </div>

          {loading ? (
            <div className="flex justify-center items-center py-20 text-zinc-500">
              Carregando dados do perfil...
            </div>
          ) : (
            <div className="space-y-6">
              {/* Card de Informações */}
              <div className="bg-[#1e1c2a] rounded-2xl border border-white/10 p-6 shadow-xl">
                <h2 className="text-lg font-bold text-white">{profile.name || "Usuário"}</h2>
                <p className="text-sm text-zinc-400">{profile.email}</p>
              </div>

              {/* Mensagem de Feedback */}
              {message.text && (
                <div
                  className={`p-4 rounded-xl text-sm flex items-center gap-3 border ${
                    message.type === "success"
                      ? "bg-emerald-500/10 border-emerald-500/20 text-emerald-400"
                      : "bg-red-500/10 border-red-500/20 text-red-400"
                  }`}
                >
                  {message.type === "success" ? (
                    <CheckCircle2 size={18} className="shrink-0" />
                  ) : (
                    <AlertCircle size={18} className="shrink-0" />
                  )}
                  <span>{message.text}</span>
                </div>
              )}

              {/* Formulário de Edição */}
              <form onSubmit={handleSubmit} className="bg-[#1e1c2a] rounded-2xl border border-white/10 p-8 shadow-xl space-y-6">
                <h3 className="text-md font-semibold text-white border-b border-white/10 pb-4">
                  Alterar Informações
                </h3>

                {/* Nome */}
                <div>
                  <label className="block text-zinc-400 text-sm mb-2">Nome Completo</label>
                  <div className="relative">
                    <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                    <input
                      type="text"
                      name="name"
                      value={form.name}
                      onChange={handleChange}
                      placeholder="Seu nome"
                      className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                    />
                  </div>
                </div>

                {/* Email (Apenas Leitura) */}
                <div>
                  <label className="block text-zinc-400 text-sm mb-2">
                    Email <span className="text-zinc-500 text-xs">(Não pode ser alterado)</span>
                  </label>
                  <div className="relative">
                    <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                    <input
                      type="email"
                      name="email"
                      value={profile.email}
                      disabled
                      readOnly
                      className="w-full rounded-xl bg-white/5 border border-white/5 pl-11 pr-4 py-3 text-sm text-zinc-500 outline-none cursor-not-allowed select-none"
                    />
                  </div>
                </div>

                {/* Senha Atual (Confirmação) */}
                <div>
                  <label className="block text-zinc-400 text-sm mb-2">
                    Senha Atual <span className="text-amber-500 text-xs">(Obrigatória para confirmar alterações)</span>
                  </label>
                  <div className="relative">
                    <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                    <input
                      type={showPassword ? "text" : "password"}
                      name="currentPassword"
                      value={form.currentPassword}
                      onChange={handleChange}
                      placeholder="••••••••"
                      className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-11 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword((prev) => !prev)}
                      className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors cursor-pointer"
                      aria-label={showPassword ? "Ocultar senha" : "Exibir senha"}
                    >
                      {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                    </button>
                  </div>
                </div>

                {/* Botão de Salvar */}
                <div className="pt-4 flex justify-end">
                  <button
                    type="submit"
                    disabled={saving}
                    className="flex items-center gap-2 bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-6 py-3 text-sm cursor-pointer shadow-lg shadow-amber-500/10"
                  >
                    <Save size={18} />
                    {saving ? "Salvando..." : "Salvar Alterações"}
                  </button>
                </div>
              </form>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
