import { useState } from "react";
import { Mail, Lock, ShieldCheck, KeyRound, Eye, EyeOff } from "lucide-react";
import { login, verify2FA } from "../services/authService";

export default function Login({ onNavigate, onAuthenticated }) {
  const [form, setForm] = useState({ email: "", password: "" });
  const [step, setStep] = useState(1);
  const [code, setCode] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(form.email, form.password);
      setStep(2);
    } catch (err) {
      setError(err?.response?.data?.detail || err?.response?.data?.message || "Não foi possível entrar. Confira seus dados e se o e-mail foi verificado.");
    } finally {
      setLoading(false);
    }
  };

  const handleVerify = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const user = await verify2FA(code);
      onAuthenticated?.(user);
      onNavigate?.("Carteira");
    } catch (err) {
      setError(err?.response?.data?.detail || err?.response?.data?.message || "Código inválido ou expirado. Se necessário, volte e inicie o login novamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-[#1e1c2a] rounded-2xl border border-white/10 shadow-2xl overflow-hidden">
        <div className="px-8 pt-8 pb-6 text-center border-b border-white/10">
          <div className="w-16 h-16 bg-amber-500/20 text-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">{step === 1 ? <ShieldCheck size={32} /> : <KeyRound size={32} />}</div>
          <h1 className="text-2xl font-bold text-white mb-2">{step === 1 ? "Bem-vindo de volta" : "Verificação em 2 etapas"}</h1>
          <p className="text-sm text-zinc-400">{step === 1 ? <>Acesse o <span className="text-amber-500 font-medium">Investir+</span></> : <>Digite o código enviado para <span className="text-amber-400">{form.email}</span></>}</p>
        </div>
        {step === 1 ? <form onSubmit={handleSubmit} className="p-8 space-y-5">
          {error && <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">{error}</div>}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">E-mail</label>
            <div className="relative"><Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" /><input required type="email" value={form.email} onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))} placeholder="seu@email.com" className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50" /></div>
          </div>
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Senha</label>
            <div className="relative"><Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" /><input required type={showPassword ? "text" : "password"} value={form.password} onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))} placeholder="••••••••" className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-12 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50" /><button type="button" onClick={() => setShowPassword((value) => !value)} className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500" aria-label={showPassword ? "Ocultar senha" : "Exibir senha"}>{showPassword ? <EyeOff size={18} /> : <Eye size={18} />}</button></div>
          </div>
          <button type="submit" disabled={loading} className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm">{loading ? "Verificando..." : "Continuar"}</button>
          <p className="text-center text-sm text-zinc-400">Ainda não tem uma conta? <button type="button" onClick={() => onNavigate?.("SignUp")} className="text-amber-500 font-medium">Cadastre-se</button></p>
        </form> : <form onSubmit={handleVerify} className="p-8 space-y-5">
          {error && <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">{error}</div>}
          <div>
            <label className="block text-zinc-400 text-sm mb-2">Código de verificação</label>
            <input required autoComplete="one-time-code" maxLength={64} value={code} onChange={(event) => setCode(event.target.value)} placeholder="Cole o código recebido por e-mail" className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-center text-sm font-mono text-zinc-200" />
            <p className="text-xs text-zinc-500 mt-2 text-center">O código expira em cinco minutos.</p>
          </div>
          <button type="submit" disabled={loading} className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm">{loading ? "Confirmando..." : "Confirmar acesso"}</button>
          <button type="button" onClick={() => { setStep(1); setError(""); setCode(""); }} className="w-full text-sm text-zinc-500">Voltar e usar outro e-mail</button>
        </form>}
      </div>
    </div>
  );
}
