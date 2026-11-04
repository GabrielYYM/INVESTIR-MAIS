import { useState } from "react";
import { Mail, Lock, User, Calendar, ShieldCheck, CheckCircle2, Eye, EyeOff, RefreshCw } from "lucide-react";
import { register, resendVerification } from "../services/authService";

export default function SignUp({ onNavigate }) {
  const [form, setForm] = useState({
    name: "",
    age: "",
    email: "",
    password: "",
    confirmPassword: "",
    acceptTerms: false,
  });

  const [step, setStep] = useState(1);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [error, setError] = useState("");
  const [resendStatus, setResendStatus] = useState("");
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

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

    if (!form.name.trim() || !form.age || !form.email.trim() || !form.password || !form.confirmPassword) {
      return setError("Preencha todos os campos obrigatórios.");
    }

    const ageNumber = Number(form.age);
    if (isNaN(ageNumber) || ageNumber < 0 || ageNumber > 120) {
      return setError("Por favor, informe uma idade válida.");
    }

    if (form.password.length < 8) {
      return setError("A senha deve conter no mínimo 8 caracteres.");
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
        name: form.name.trim(),
        age: ageNumber,
        email: form.email.trim(),
        password: form.password,
      });

      setStep(2);
    } catch (err) {
      if (err?.response?.status === 409) {
        setError("Não foi possível criar sua conta. Verifique os dados informados.");
      } else {
        setError(
          err?.response?.data?.message ||
          err?.response?.data?.detail ||
          "Não foi possível criar sua conta. Verifique os dados informados."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendVerification = async () => {
    setResending(true);
    setResendStatus("");
    try {
      await resendVerification(form.email.trim());
      setResendStatus("Link de confirmação reenviado! Verifique sua caixa de entrada e pasta de spam.");
    } catch (err) {
      setResendStatus("Erro ao reenviar o link. Aguarde alguns instantes antes de tentar novamente.");
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#171522] flex items-center justify-center p-4">
      <div className="w-full max-w-md bg-[#1e1c2a] rounded-2xl border border-white/10 shadow-2xl overflow-hidden">

        <div className="px-8 pt-8 pb-6 text-center border-b border-white/10">
          <div className="w-16 h-16 bg-amber-500/20 text-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">
            {step === 1 ? <ShieldCheck size={32} /> : <CheckCircle2 size={32} />}
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {step === 1 ? "Criar Conta" : "Conta Criada"}
          </h1>
          <p className="text-sm text-zinc-400">
            {step === 1 ? (
              <>Junte-se ao <span className="text-amber-500 font-medium">Investir Mais</span></>
            ) : (
              <>Verifique seu e-mail para ativar seu acesso</>
            )}
          </p>
        </div>

        {step === 1 ? (
          <form onSubmit={handleSubmit} className="p-8 space-y-4">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            <div>
              <label className="block text-zinc-400 text-sm mb-1.5">Nome completo</label>
              <div className="relative">
                <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type="text"
                  name="name"
                  required
                  value={form.name}
                  onChange={handleChange}
                  placeholder="Nome"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-zinc-400 text-sm mb-1.5">Idade</label>
              <div className="relative">
                <Calendar size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type="number"
                  name="age"
                  min="0"
                  max="120"
                  required
                  value={form.age}
                  onChange={handleChange}
                  placeholder="idade"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            <div>
              <label className="block text-zinc-400 text-sm mb-1.5">E-mail</label>
              <div className="relative">
                <Mail size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type="email"
                  name="email"
                  required
                  value={form.email}
                  onChange={handleChange}
                  placeholder="exemplo@email.com"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            {/* Senha */}
            <div>
              <label className="block text-zinc-400 text-sm mb-1.5">Senha (mínimo 8 caracteres)</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  required
                  value={form.password}
                  onChange={handleChange}
                  placeholder="***********"
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

            <div>
              <label className="block text-zinc-400 text-sm mb-1.5">Confirmar Senha</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  name="confirmPassword"
                  required
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="***********"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-11 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirmPassword((prev) => !prev)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors cursor-pointer"
                  aria-label={showConfirmPassword ? "Ocultar senha" : "Exibir senha"}
                >
                  {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className="flex items-start gap-3 pt-1">
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
              <label htmlFor="terms" className="text-xs text-zinc-400 cursor-pointer leading-relaxed">
                Eu concordo com os Termos de Uso e a Política de Privacidade do Investir Mais.
              </label>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer mt-4"
            >
              {loading ? "Criando sua conta..." : "Criar conta"}
            </button>

            <div className="text-center pt-2">
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
        ) : (
          <div className="p-8 text-center space-y-6">
            <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl text-left space-y-2">
              <p className="text-sm text-zinc-200">
                Sua conta foi criada! Enviamos um link de ativação para:
              </p>
              <p className="text-sm font-semibold text-amber-400 break-all">
                {form.email}
              </p>
              <p className="text-xs text-zinc-400 pt-1 leading-relaxed">
                Acesse seu e-mail e clique no link de verificação. O link expira em 5 minutos. Após confirmar, você poderá acessar sua conta normalmente.
              </p>
            </div>

            {resendStatus && (
              <div className="p-3 bg-white/5 border border-white/10 rounded-xl text-xs text-zinc-300">
                {resendStatus}
              </div>
            )}

            <button
              type="button"
              onClick={() => onNavigate && onNavigate("Login")}
              className="w-full bg-amber-500 hover:bg-amber-400 transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer"
            >
              Ir para o Login
            </button>

            <button
              type="button"
              onClick={handleResendVerification}
              disabled={resending}
              className="w-full flex items-center justify-center gap-2 text-xs text-zinc-400 hover:text-amber-400 transition-colors cursor-pointer"
            >
              <RefreshCw size={14} className={resending ? "animate-spin" : ""} />
              {resending ? "Reenviando..." : "Não recebeu o e-mail? Clique para reenviar"}
            </button>
          </div>
        )}

      </div>
    </div>
  );
}
