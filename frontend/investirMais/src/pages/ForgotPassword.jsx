import { useState } from "react";
import { Mail, Lock, KeyRound, ShieldCheck, Eye, EyeOff, ArrowLeft, CheckCircle2 } from "lucide-react";
import { forgotPassword, resetPassword } from "../services/authService";

export default function ForgotPassword({ onNavigate }) {
  const [step, setStep] = useState(1); 
  const [email, setEmail] = useState("");
  const [token, setToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [error, setError] = useState("");
  const [infoMessage, setInfoMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleRequestReset = async (e) => {
    e.preventDefault();
    setError("");
    setInfoMessage("");

    if (!email.trim()) {
      return setError("Por favor, digite seu e-mail cadastrado.");
    }

    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setStep(2);
      setInfoMessage("Se este e-mail estiver cadastrado, você receberá um código de recuperação em instantes.");
    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.detail ||
        "Não foi possível enviar o código. Tente novamente mais tarde."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError("");

    if (!token.trim()) {
      return setError("Por favor, informe o código recebido por e-mail.");
    }

    if (newPassword.length < 8) {
      return setError("A nova senha deve ter no mínimo 8 caracteres.");
    }

    if (newPassword !== confirmPassword) {
      return setError("As senhas não coincidem.");
    }

    setLoading(true);
    try {
      await resetPassword(token.trim(), newPassword);
      setStep(3);
    } catch (err) {
      setError(
        err?.response?.data?.message ||
        err?.response?.data?.detail ||
        "Código inválido ou expirado. Verifique os dados ou solicite um novo código."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    setError("");
    setInfoMessage("");
    setLoading(true);
    try {
      await forgotPassword(email.trim());
      setInfoMessage("Novo código solicitado com sucesso! Verifique sua caixa de entrada.");
    } catch (err) {
      setError("Não foi possível reenviar o código no momento.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-[#1e1c2a] rounded-2xl border border-white/10 shadow-2xl overflow-hidden">

        <div className="px-8 pt-8 pb-6 text-center border-b border-white/10">
          <div className="w-16 h-16 bg-amber-500/20 text-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">
            {step === 3 ? <CheckCircle2 size={32} /> : step === 2 ? <KeyRound size={32} /> : <ShieldCheck size={32} />}
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {step === 1 && "Recuperar Senha"}
            {step === 2 && "Redefinir Senha"}
            {step === 3 && "Senha Atualizada!"}
          </h1>
          <p className="text-sm text-zinc-400">
            {step === 1 && "Informe seu e-mail para receber o código de recuperação"}
            {step === 2 && (
              <>
                Digite o código enviado para <span className="text-amber-400 font-medium">{email}</span>
              </>
            )}
            {step === 3 && "Sua senha foi redefinida com sucesso."}
          </p>
        </div>

        {step === 1 && (
          <form onSubmit={handleRequestReset} className="p-8 space-y-5">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            <div>
              <label className="block text-zinc-400 text-sm mb-2">E-mail cadastrado</label>
              <div className="relative">
                <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  required
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="seu@email.com"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer"
            >
              {loading ? "Enviando código..." : "Enviar código de recuperação"}
            </button>

            <button
              type="button"
              onClick={() => onNavigate?.("Login")}
              className="w-full flex items-center justify-center gap-2 text-sm text-zinc-400 hover:text-white transition-colors cursor-pointer pt-2"
            >
              <ArrowLeft size={16} /> Voltar para o Login
            </button>
          </form>
        )}

        {step === 2 && (
          <form onSubmit={handleResetPassword} className="p-8 space-y-4">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            {infoMessage && (
              <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400 text-xs text-center">
                {infoMessage}
              </div>
            )}

            <div>
              <label className="block text-zinc-400 text-sm mb-2">Código recebido por e-mail</label>
              <div className="relative">
                <KeyRound size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  required
                  type="text"
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                  placeholder="Cole aqui o código do e-mail"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm font-mono text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-zinc-400 text-sm mb-2">Nova senha (mínimo 8 caracteres)</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  required
                  type={showPassword ? "text" : "password"}
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-11 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors"
                  aria-label={showPassword ? "Ocultar senha" : "Exibir senha"}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-zinc-400 text-sm mb-2">Confirmar nova senha</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  required
                  type={showConfirmPassword ? "text" : "password"}
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-11 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword((v) => !v)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors"
                  aria-label={showConfirmPassword ? "Ocultar senha" : "Exibir senha"}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer mt-2"
            >
              {loading ? "Redefinindo senha..." : "Salvar nova senha"}
            </button>

            <div className="flex items-center justify-between pt-2">
              <button
                type="button"
                onClick={handleResend}
                disabled={loading}
                className="text-xs text-amber-500 hover:text-amber-400 transition-colors"
              >
                Reenviar código
              </button>
              <button
                type="button"
                onClick={() => {
                  setStep(1);
                  setError("");
                  setInfoMessage("");
                }}
                className="text-xs text-zinc-400 hover:text-zinc-200 transition-colors"
              >
                Alterar e-mail
              </button>
            </div>

            <button
              type="button"
              onClick={() => onNavigate?.("Login")}
              className="w-full flex items-center justify-center gap-2 text-sm text-zinc-400 hover:text-white transition-colors cursor-pointer pt-3"
            >
              <ArrowLeft size={16} /> Voltar para o Login
            </button>
          </form>
        )}

        {step === 3 && (
          <div className="p-8 text-center space-y-6">
            <div className="p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400 text-sm">
              Sua nova senha foi cadastrada e seu e-mail foi validado! Agora você já pode fazer login na sua conta.
            </div>

            <button
              type="button"
              onClick={() => onNavigate?.("Login")}
              className="w-full bg-amber-500 hover:bg-amber-400 transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer"
            >
              Fazer login agora
            </button>
          </div>
        )}

      </div>
    </div>
  );
}

