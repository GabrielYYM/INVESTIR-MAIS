import { useState } from "react";
import { Mail, Lock, ShieldCheck, KeyRound, Eye, EyeOff } from "lucide-react";
import { login, verify2FA } from "../services/authService";

export default function Login({ onNavigate, onLoginSuccess, onNavigateToRegister }) {
  // Etapa 1 = credenciais, Etapa 2 = código 2FA
  const [step, setStep] = useState(1);

  const [form, setForm] = useState({ email: "", password: "" });
  const [code, setCode] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleRegisterClick = () => {
    if (onNavigateToRegister) {
      onNavigateToRegister();
    } else if (onNavigate) {
      onNavigate("Register");
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.email || !form.password) {
      return setError("Por favor, preencha todos os campos.");
    }

    setLoading(true);
    try {
      await login(form.email, form.password);
      setStep(2);
    } catch (err) {
      setError(err?.response?.data?.detail || err?.response?.data?.message || err.message || "Erro ao fazer login.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerify2FA = async (e) => {
    e.preventDefault();
    setError("");

    if (!code.trim()) {
      return setError("Por favor, insira o código recebido por e-mail.");
    }

    setLoading(true);
    try {
      await verify2FA(form.email, code);
      if (onLoginSuccess) {
        onLoginSuccess();
      } else if (onNavigate) {
        onNavigate("Carteira");
      }
    } catch (err) {
      setError(err?.response?.data?.detail || err?.response?.data?.message || err.message || "Código inválido ou expirado.");
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
            {step === 1 ? <ShieldCheck size={32} /> : <KeyRound size={32} />}
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {step === 1 ? "Bem-vindo de volta" : "Verificação em 2 etapas"}
          </h1>
          <p className="text-sm text-zinc-400">
            {step === 1
              ? <>Acesse o <span className="text-amber-500 font-medium">Investir+</span></>
              : <>Insira o código enviado para <span className="text-amber-400 font-medium">{form.email}</span></>
            }
          </p>
        </div>

        {/* 1. Email e Senha */}
        {step === 1 && (
          <form onSubmit={handleLogin} className="p-8 space-y-5">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

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

            <div>
              <label className="block text-zinc-400 text-sm mb-2">Senha</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={form.password}
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

            <div className="flex justify-end">
              <a href="#" className="text-sm text-amber-500 hover:text-amber-400 transition-colors">
                Esqueceu sua senha?
              </a>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer"
            >
              {loading ? "Verificando..." : "Continuar"}
            </button>

            <div className="text-center">
              <p className="text-sm text-zinc-400">
                Ainda não tem uma conta?{" "}
                <button
                  type="button"
                  onClick={handleRegisterClick}
                  className="text-amber-500 hover:text-amber-400 font-medium transition-colors cursor-pointer"
                >
                  Cadastre-se
                </button>
              </p>
            </div>
          </form>
        )}

        {/*2. 2FA */}
        {step === 2 && (
          <form onSubmit={handleVerify2FA} className="p-8 space-y-5">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            <div>
              <label className="block text-zinc-400 text-sm mb-2">Código de verificação</label>
              <input
                type="text"
                maxLength={6}
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, ""))}
                placeholder="000000"
                className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-center text-2xl font-mono tracking-[0.5em] text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
              />
              <p className="text-xs text-zinc-600 mt-2 text-center">
                O código expira em alguns minutos. Verifique sua caixa de entrada.
              </p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer"
            >
              {loading ? "Verificando..." : "Confirmar acesso"}
            </button>

            <div className="text-center">
              <button
                type="button"
                onClick={() => { setStep(1); setError(""); setCode(""); }}
                className="text-sm text-zinc-500 hover:text-zinc-300 transition-colors cursor-pointer"
              >
                ← Voltar e usar outro e-mail
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
