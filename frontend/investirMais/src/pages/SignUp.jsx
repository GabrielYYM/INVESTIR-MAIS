import { useState } from "react";
import { Mail, Lock, User, ShieldCheck } from "lucide-react";
import { register } from "../services/authService";

export default function SignUp({ onNavigate }) {
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    acceptTerms: false,
  });

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.name || !form.email || !form.password || !form.confirmPassword) {
      return setError("Por favor, preencha todos os campos.");
    }

    if (form.password !== form.confirmPassword) {
      return setError("As senhas não coincidem.");
    }

    if (!form.acceptTerms) {
      return setError("Você precisa aceitar os Termos de Uso e Política de Privacidade.");
    }

    setLoading(true);
    try {
      await register({
        name: form.name,
        email: form.email,
        password: form.password,
      });

      if (onNavigate) {
        onNavigate("Login");
      }
    } catch (err) {
      setError(err?.response?.data?.message || err.message || "Erro ao criar conta.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-[#1e1c2a] rounded-2xl border border-white/10 shadow-2xl overflow-hidden">

        {/* Header */}
        <div className="px-8 pt-8 pb-6 text-center border-b border-white/10">
          <div className="w-16 h-16 bg-amber-500/20 text-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">
            <ShieldCheck size={32} />
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">Criar Conta</h1>
          <p className="text-sm text-zinc-400">
            Junte-se ao <span className="text-amber-500 font-medium">Investir Mais</span>
          </p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-8 space-y-5">
          {error && (
            <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
              {error}
            </div>
          )}

          {/* Nome */}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Nome completo</label>
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

          {/* Email */}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Email</label>
            <div className="relative">
              <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="seu@email.com"
                className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
              />
            </div>
          </div>

          {/* Senha */}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Senha</label>
            <div className="relative">
              <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input
                type="password"
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="••••••••"
                className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
              />
            </div>
          </div>

          {/* Confirmar Senha */}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Confirmar Senha</label>
            <div className="relative">
              <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input
                type="password"
                name="confirmPassword"
                value={form.confirmPassword}
                onChange={handleChange}
                placeholder="••••••••"
                className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
              />
            </div>
          </div>

          {/* Termos de Uso */}
          <div className="flex items-start gap-3 mt-4">
            <div className="flex items-center h-5">
              <input
                id="terms"
                type="checkbox"
                name="acceptTerms"
                checked={form.acceptTerms}
                onChange={handleChange}
                className="w-4 h-4 bg-white/5 border-white/10 rounded text-amber-500 focus:ring-amber-500/30 focus:ring-offset-0 cursor-pointer"
              />
            </div>
            <label htmlFor="terms" className="text-sm text-zinc-400 cursor-pointer">
              Eu concordo com os{" "}
              <a href="#" className="text-amber-500 hover:text-amber-400 underline transition-colors">
                Termos de Uso
              </a>{" "}
              e a{" "}
              <a href="#" className="text-amber-500 hover:text-amber-400 underline transition-colors">
                Política de Privacidade
              </a>.
            </label>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer mt-6"
          >
            {loading ? "Criando conta..." : "Criar conta"}
          </button>

          {/* Login Link */}
          <div className="text-center mt-6">
            <p className="text-sm text-zinc-400">
              Já tem uma conta?{" "}
              <button
                type="button"
                onClick={() => onNavigate && onNavigate("Login")}
                className="text-amber-500 hover:text-amber-400 font-medium transition-colors cursor-pointer"
              >
                Fazer login
              </button>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
}
