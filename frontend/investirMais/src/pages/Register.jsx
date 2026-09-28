import { useState } from "react";
import { Mail, Lock, User, Calendar, ShieldCheck, ShieldAlert, Eye, EyeOff, CheckCircle2, RefreshCw, KeyRound, ArrowLeft } from "lucide-react";
import { registrar, verificarCadastro, reenviarCodigoCadastro } from "../services/authService";
import LegalModal from "../components/LegalModal";

export default function Register({ onRegisterSuccess, onNavigateToLogin, onNavigate }) {
  const [step, setStep] = useState("form"); 

  const [form, setForm] = useState({
    name: "",
    email: "",
    birthDate: "",
    guardianEmail: "",
    password: "",
    confirmPassword: "",
    acceptTerms: false,
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [childCode, setChildCode] = useState("");
  const [guardianCode, setGuardianCode] = useState("");

  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  
  const [legalModalOpen, setLegalModalOpen] = useState(false);
  const [legalModalType, setLegalModalType] = useState("termos");

  const handleNavigateToLogin = () => {
    if (onNavigateToLogin) {
      onNavigateToLogin();
    } else if (onNavigate) {
      onNavigate("Login");
    }
  };

  function isUnder12YearsOld(dateStr) {
    if (!dateStr) return false;
    const birth = new Date(dateStr + "T00:00:00");
    if (isNaN(birth.getTime())) return false;
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age < 12;
  }

  const isMinor = isUnder12YearsOld(form.birthDate);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  async function handleSubmitForm(e) {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (!form.name || !form.email || !form.password || !form.confirmPassword) {
      return setError("Por favor, preencha todos os campos obrigatórios.");
    }

    if (form.password !== form.confirmPassword) {
      return setError("As senhas não coincidem.");
    }

    if (!form.acceptTerms) {
      return setError("Você precisa aceitar os Termos de Uso e Política de Privacidade.");
    }

    if (isMinor) {
      if (!form.guardianEmail || form.guardianEmail.trim() === "") {
        return setError("Para menores de 12 anos, o e-mail do responsável legal é obrigatório.");
      }
      if (form.guardianEmail.trim().toLowerCase() === form.email.trim().toLowerCase()) {
        return setError("O e-mail do responsável não pode ser idêntico ao seu e-mail.");
      }
    }

    setLoading(true);
    try {
      await registrar(
        form.name,
        form.email,
        form.password,
        form.birthDate || null,
        isMinor ? form.guardianEmail : null,
        form.acceptTerms
      );
      setStep("verify");
      setSuccessMessage(
        isMinor
          ? "Cadastrado com sucesso! Códigos de verificação enviados para o seu e-mail e do seu responsável."
          : "Cadastrado com sucesso! Código de verificação enviado para o seu e-mail."
      );
    } catch (err) {
      setError(
        err?.response?.data?.detail ||
        err?.response?.data?.message ||
        err.message ||
        "Erro ao realizar cadastro."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyCodes(e) {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (!childCode.trim()) {
      return setError("Por favor, insira o seu código de verificação.");
    }

    if (isMinor && !guardianCode.trim()) {
      return setError("Por favor, insira o código de verificação do responsável.");
    }

    setLoading(true);
    try {
      await verificarCadastro(
        form.email,
        childCode.trim(),
        isMinor ? guardianCode.trim() : null
      );
      setSuccessMessage("Cadastro verificado com sucesso!");
      if (onRegisterSuccess) {
        onRegisterSuccess();
      } else {
        handleNavigateToLogin();
      }
    } catch (err) {
      setError(
        err?.response?.data?.detail ||
        err?.response?.data?.message ||
        err.message ||
        "Código de verificação inválido ou expirado."
      );
    } finally {
      setLoading(false);
    }
  }

  async function handleResendCode() {
    setError("");
    setSuccessMessage("");
    setResending(true);
    try {
      await reenviarCodigoCadastro(form.email);
      setSuccessMessage("Códigos reenviados com sucesso. Verifique sua caixa de entrada.");
    } catch (err) {
      setError(
        err?.response?.data?.detail ||
        err?.response?.data?.message ||
        err.message ||
        "Erro ao reenviar código."
      );
    } finally {
      setResending(false);
    }
  }
//TODO Refazer TUDO ISSO não ta dando não fi
  return (
    <div className="min-h-screen bg-[#171522] flex items-center justify-center p-4">
      <div className="w-full max-w-lg bg-[#1e1c2a] rounded-2xl border border-white/10 shadow-2xl overflow-hidden">
        {/* Header */}
        <div className="px-8 pt-8 pb-6 text-center border-b border-white/10">
          <div className="w-16 h-16 bg-amber-500/20 text-amber-500 rounded-full flex items-center justify-center mx-auto mb-4">
            {step === "form" ? <ShieldCheck size={32} /> : <KeyRound size={32} />}
          </div>
          <h1 className="text-2xl font-bold text-white mb-2">
            {step === "form" ? "Criar Conta" : "Verificar Cadastro"}
          </h1>
          <p className="text-sm text-zinc-400">
            {step === "form" ? (
              <>Junte-se ao <span className="text-amber-500 font-medium">Investir Mais</span></>
            ) : (
              <>Insira o código enviado para <span className="text-amber-400 font-medium">{form.email}</span></>
            )}
          </p>
        </div>

        {/* Form Etapa 1: Dados Pessoais */}
        {step === "form" && (
          <form onSubmit={handleSubmitForm} className="p-8 space-y-5">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            {/* Nome Completo */}
            <div>
              <label className="block text-zinc-400 text-sm mb-2">Nome completo</label>
              <div className="relative">
                <User size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type="text"
                  name="name"
                  required
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
                  required
                  value={form.email}
                  onChange={handleChange}
                  placeholder="seu@email.com"
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            </div>

            {/* Data de Nascimento */}
            <div>
              <label className="block text-zinc-400 text-sm mb-2">Data de nascimento</label>
              <div className="relative">
                <Calendar size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type="date"
                  name="birthDate"
                  value={form.birthDate}
                  onChange={handleChange}
                  className="w-full rounded-xl bg-white/5 border border-white/10 pl-11 pr-4 py-3 text-sm text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors [color-scheme:dark]"
                />
              </div>
            </div>

            {/* Alerta e Campo de E-mail do Responsável para Menores de 12 Anos */}
            {isMinor && (
              <div className="p-4 bg-amber-500/10 border border-amber-500/20 rounded-xl space-y-3">
                <div className="flex items-start gap-2.5 text-amber-400 text-xs">
                  <ShieldAlert size={18} className="shrink-0 mt-0.5" />
                  <span>
                    Identificamos que você tem menos de 12 anos. Em conformidade com a LGPD, solicitamos o e-mail do seu responsável legal para autorização.
                  </span>
                </div>
                <div>
                  <label className="block text-zinc-300 text-xs font-medium mb-1.5">
                    Email do Responsável Legal
                  </label>
                  <div className="relative">
                    <Mail size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-zinc-500" />
                    <input
                      type="email"
                      name="guardianEmail"
                      required={isMinor}
                      value={form.guardianEmail}
                      onChange={handleChange}
                      placeholder="responsavel@email.com"
                      className="w-full rounded-lg bg-white/5 border border-white/10 pl-10 pr-3 py-2.5 text-xs text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50"
                    />
                  </div>
                </div>
              </div>
            )}

            {/* Senha */}
            <div>
              <label className="block text-zinc-400 text-sm mb-2">Senha</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  required
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

            {/* Confirmar Senha */}
            <div>
              <label className="block text-zinc-400 text-sm mb-2">Confirmar Senha</label>
              <div className="relative">
                <Lock size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-zinc-500" />
                <input
                  type={showConfirmPassword ? "text" : "password"}
                  name="confirmPassword"
                  required
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="••••••••"
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
                <button
                  type="button"
                  onClick={(e) => { e.preventDefault(); setLegalModalType("termos"); setLegalModalOpen(true); }}
                  className="text-amber-500 hover:text-amber-400 underline transition-colors cursor-pointer"
                >
                  Termos de Uso
                </button>
                {" "}e a{" "}
                <button
                  type="button"
                  onClick={(e) => { e.preventDefault(); setLegalModalType("politica"); setLegalModalOpen(true); }}
                  className="text-amber-500 hover:text-amber-400 underline transition-colors cursor-pointer"
                >
                  Política de Privacidade
                </button>.
              </label>
            </div>

            <button
              type="submit"
              disabled={loading || !form.acceptTerms}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer mt-6 shadow-lg shadow-amber-500/10"
            >
              {loading ? "Criando conta..." : "Criar conta"}
            </button>

            {/* Link para Login */}
            <div className="text-center mt-6">
              <p className="text-sm text-zinc-400">
                Já tem uma conta?{" "}
                <button
                  type="button"
                  onClick={handleNavigateToLogin}
                  className="text-amber-500 hover:text-amber-400 font-medium transition-colors cursor-pointer"
                >
                  Fazer login
                </button>
              </p>
            </div>
          </form>
        )}

        {/* Form Etapa 2: Verificação de Código enviado por e-mail */}
        {step === "verify" && (
          <form onSubmit={handleVerifyCodes} className="p-8 space-y-5">
            {error && (
              <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm text-center">
                {error}
              </div>
            )}

            {successMessage && (
              <div className="p-3 bg-emerald-500/10 border border-emerald-500/20 rounded-xl text-emerald-400 text-sm flex items-center justify-center gap-2 text-center">
                <CheckCircle2 size={18} className="shrink-0" />
                <span>{successMessage}</span>
              </div>
            )}

            {/* Código do Aluno */}
            <div>
              <label className="block text-zinc-400 text-sm mb-2">
                Código de verificação do usuário (6 dígitos)
              </label>
              <input
                type="text"
                required
                maxLength={6}
                value={childCode}
                onChange={(e) => setChildCode(e.target.value.replace(/\D/g, ""))}
                placeholder="000000"
                className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-center text-2xl font-mono tracking-[0.4em] text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
              />
            </div>

            {/* Código do Responsável se menor de 12 anos */}
            {isMinor && (
              <div>
                <label className="block text-zinc-400 text-sm mb-2">
                  Código de autorização do Responsável ({form.guardianEmail})
                </label>
                <input
                  type="text"
                  required
                  maxLength={6}
                  value={guardianCode}
                  onChange={(e) => setGuardianCode(e.target.value.replace(/\D/g, ""))}
                  placeholder="000000"
                  className="w-full rounded-xl bg-white/5 border border-white/10 px-4 py-3 text-center text-2xl font-mono tracking-[0.4em] text-zinc-200 placeholder:text-zinc-600 outline-none focus:border-amber-500/50 focus:ring-1 focus:ring-amber-500/30 transition-colors"
                />
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 disabled:cursor-not-allowed transition-colors text-zinc-900 font-medium rounded-xl px-5 py-3.5 text-sm cursor-pointer mt-4"
            >
              {loading ? "Verificando..." : "Confirmar Cadastro"}
            </button>

            {/* Reenviar Código */}
            <div className="flex items-center justify-between pt-2">
              <button
                type="button"
                onClick={() => setStep("form")}
                className="text-xs text-zinc-500 hover:text-zinc-300 flex items-center gap-1 cursor-pointer transition-colors"
              >
                <ArrowLeft size={14} /> Corrigir dados
              </button>

              <button
                type="button"
                onClick={handleResendCode}
                disabled={resending}
                className="text-xs text-amber-500 hover:text-amber-400 flex items-center gap-1 cursor-pointer disabled:opacity-50 transition-colors"
              >
                <RefreshCw size={14} className={resending ? "animate-spin" : ""} />
                {resending ? "Reenviando..." : "Reenviar código"}
              </button>
            </div>
          </form>
        )}
      </div>

      <LegalModal 
        isOpen={legalModalOpen} 
        onClose={() => setLegalModalOpen(false)} 
        type={legalModalType} 
      />
    </div>
  );
}
