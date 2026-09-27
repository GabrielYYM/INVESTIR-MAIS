import { useState } from "react";
import { Eye, EyeOff, TrendingUp, ShieldAlert, CheckCircle2, RefreshCw } from "lucide-react";
import { registrar, verificarCadastro, reenviarCodigoCadastro } from "../services/authService";

export default function Register({ onRegisterSuccess, onNavigateToLogin }) {
  const [step, setStep] = useState("form"); // "form" | "verify"
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [birthDate, setBirthDate] = useState("");
  const [guardianEmail, setGuardianEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  // Estados da etapa de verificação
  const [childCode, setChildCode] = useState("");
  const [guardianCode, setGuardianCode] = useState("");

  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");

  // Calcula se o usuário tem menos de 12 anos com base na data de nascimento
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

  const isMinor = isUnder12YearsOld(birthDate);

  async function handleSubmitForm(e) {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (password !== confirmPassword) {
      setError("As senhas não coincidem.");
      return;
    }

    if (isMinor) {
      if (!guardianEmail || guardianEmail.trim() === "") {
        setError("Para menores de 12 anos, o e-mail do responsável é obrigatório.");
        return;
      }
      if (guardianEmail.trim().toLowerCase() === email.trim().toLowerCase()) {
        setError("O e-mail do responsável não pode ser idêntico ao e-mail do aluno.");
        return;
      }
    }

    setLoading(true);
    try {
      await registrar(name, email, password, birthDate, isMinor ? guardianEmail : null);
      setStep("verify");
    } catch (err) {
      setError(err.response?.data?.detail || "Não foi possível criar a conta. Verifique os dados e tente novamente.");
    } finally {
      setLoading(false);
    }
  }

  async function handleVerify(e) {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (!childCode || childCode.trim().length !== 6) {
      setError("Informe o código de 6 dígitos enviado para seu e-mail.");
      return;
    }

    if (isMinor && (!guardianCode || guardianCode.trim().length !== 6)) {
      setError("Informe o código de 6 dígitos enviado para o e-mail do responsável.");
      return;
    }

    setLoading(true);
    try {
      await verificarCadastro(email, childCode.trim(), isMinor ? guardianCode.trim() : null);
      setSuccessMessage("Conta ativada com sucesso! Redirecionando para o login...");
      setTimeout(() => {
        onRegisterSuccess?.();
      }, 1800);
    } catch (err) {
      setError(err.response?.data?.detail || "Código(s) inválido(s) ou expirado(s). Verifique e tente novamente.");
    } finally {
      setLoading(false);
    }
  }

  async function handleResendCodes() {
    setError("");
    setSuccessMessage("");
    setResending(true);
    try {
      await reenviarCodigoCadastro(email);
      setSuccessMessage("Novos códigos enviados para o(s) e-mail(s) cadastrado(s)!");
    } catch (err) {
      setError(err.response?.data?.detail || "Erro ao reenviar os códigos. Tente novamente mais tarde.");
    } finally {
      setResending(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#EDE58C] p-6">
      <div className="w-full max-w-3xl rounded-2xl overflow-hidden flex flex-col md:flex-row shadow-xl">
        {/* Painel escuro — formulário */}
        <div className="bg-[#171522] text-white flex-1 p-8 sm:p-10 flex flex-col justify-between min-h-[520px]">
          <div>
            {step === "form" ? (
              <>
                <h1 className="text-2xl font-bold mb-1">Registrar</h1>
                <p className="text-sm text-zinc-400 mb-5">Insira os dados da sua conta</p>

                <form onSubmit={handleSubmitForm} className="space-y-3.5">
                  <div>
                    <label className="text-xs text-zinc-400">Nome</label>
                    <input
                      type="text"
                      required
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="Seu nome completo"
                      className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm outline-none focus:border-amber-500 placeholder-zinc-600"
                    />
                  </div>

                  <div>
                    <label className="text-xs text-zinc-400">Email</label>
                    <input
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      placeholder="exemplo@email.com"
                      className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm outline-none focus:border-amber-500 placeholder-zinc-600"
                    />
                  </div>

                  <div>
                    <label className="text-xs text-zinc-400">Data de Nascimento</label>
                    <input
                      type="date"
                      required
                      value={birthDate}
                      onChange={(e) => setBirthDate(e.target.value)}
                      className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm outline-none focus:border-amber-500 text-zinc-200"
                    />
                  </div>

                  {/* Alerta e campo especial para menores de 12 anos */}
                  {isMinor && (
                    <div className="p-3 bg-amber-500/10 border border-amber-500/40 rounded-xl space-y-2 animate-fadeIn">
                      <div className="flex items-start gap-2">
                        <ShieldAlert size={18} className="text-amber-400 shrink-0 mt-0.5" />
                        <p className="text-xs text-amber-200 leading-relaxed">
                          Usuário menor de 12 anos: é obrigatório informar o e-mail do responsável legal. O sistema enviará um código de verificação para o seu e-mail e outro para o responsável.
                        </p>
                      </div>

                      <div>
                        <label className="text-xs text-amber-300 font-medium">E-mail do Responsável Legal</label>
                        <input
                          type="email"
                          required
                          value={guardianEmail}
                          onChange={(e) => setGuardianEmail(e.target.value)}
                          placeholder="responsavel@email.com"
                          className="w-full bg-black/20 border-b border-amber-500/60 py-1.5 px-2 text-sm outline-none focus:border-amber-400 text-zinc-100 placeholder-zinc-500 rounded-t"
                        />
                      </div>
                    </div>
                  )}

                  <div>
                    <label className="text-xs text-zinc-400">Senha</label>
                    <div className="flex items-center border-b border-zinc-600 focus-within:border-amber-500">
                      <input
                        type={showPassword ? "text" : "password"}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full bg-transparent py-1.5 text-sm outline-none placeholder-zinc-600"
                      />
                      <button type="button" onClick={() => setShowPassword((v) => !v)}>
                        {showPassword ? (
                          <EyeOff size={16} className="text-zinc-500 hover:text-zinc-300" />
                        ) : (
                          <Eye size={16} className="text-zinc-500 hover:text-zinc-300" />
                        )}
                      </button>
                    </div>
                  </div>

                  <div>
                    <label className="text-xs text-zinc-400">Confirme a sua senha</label>
                    <div className="flex items-center border-b border-zinc-600 focus-within:border-amber-500">
                      <input
                        type={showPassword ? "text" : "password"}
                        required
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="••••••••"
                        className="w-full bg-transparent py-1.5 text-sm outline-none placeholder-zinc-600"
                      />
                    </div>
                  </div>

                  {error && <p className="text-xs text-red-400 bg-red-950/30 p-2 rounded border border-red-500/30">{error}</p>}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-semibold rounded-full py-2.5 text-sm transition-colors mt-2"
                  >
                    {loading ? "Processando cadastro..." : "Registrar"}
                  </button>
                </form>
              </>
            ) : (
              /* ETAPA DE VERIFICAÇÃO */
              <div className="animate-fadeIn">
                <h1 className="text-2xl font-bold mb-1">Verificação de Cadastro</h1>
                <p className="text-xs text-zinc-400 mb-6 leading-relaxed">
                  {isMinor ? (
                    <>
                      Por ter menos de 12 anos, enviamos um código para seu e-mail (<span className="text-amber-400 font-medium">{email}</span>) e outro para o responsável (<span className="text-amber-400 font-medium">{guardianEmail}</span>). Insira ambos para ativar sua conta.
                    </>
                  ) : (
                    <>
                      Enviamos um código de 6 dígitos para o seu e-mail: <span className="text-amber-400 font-medium">{email}</span>.
                    </>
                  )}
                </p>

                <form onSubmit={handleVerify} className="space-y-4">
                  <div>
                    <label className="text-xs text-zinc-400">
                      {isMinor ? "Código da Criança / Aluno" : "Código de Verificação"}
                    </label>
                    <input
                      type="text"
                      required
                      maxLength={6}
                      value={childCode}
                      onChange={(e) => setChildCode(e.target.value.replace(/\D/g, ""))}
                      placeholder="000000"
                      className="w-full bg-zinc-900/60 border border-zinc-700 focus:border-amber-500 py-2.5 px-3 text-center text-lg tracking-[0.4em] font-mono outline-none rounded-lg text-white"
                    />
                  </div>

                  {isMinor && (
                    <div>
                      <label className="text-xs text-amber-300 font-medium">
                        Código do Responsável Legal
                      </label>
                      <input
                        type="text"
                        required
                        maxLength={6}
                        value={guardianCode}
                        onChange={(e) => setGuardianCode(e.target.value.replace(/\D/g, ""))}
                        placeholder="000000"
                        className="w-full bg-amber-950/20 border border-amber-500/40 focus:border-amber-400 py-2.5 px-3 text-center text-lg tracking-[0.4em] font-mono outline-none rounded-lg text-amber-200"
                      />
                      <p className="text-[11px] text-zinc-400 mt-1">
                        O responsável recebeu este código no e-mail: {guardianEmail}
                      </p>
                    </div>
                  )}

                  {error && (
                    <p className="text-xs text-red-400 bg-red-950/30 p-2 rounded border border-red-500/30">
                      {error}
                    </p>
                  )}

                  {successMessage && (
                    <div className="flex items-center gap-2 text-xs text-emerald-400 bg-emerald-950/40 p-2.5 rounded border border-emerald-500/40">
                      <CheckCircle2 size={16} className="shrink-0" />
                      <span>{successMessage}</span>
                    </div>
                  )}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-semibold rounded-full py-2.5 text-sm transition-colors shadow-lg shadow-amber-500/20"
                  >
                    {loading ? "Validando códigos..." : "Confirmar e Ativar Conta"}
                  </button>

                  <div className="flex items-center justify-between pt-2">
                    <button
                      type="button"
                      disabled={resending}
                      onClick={handleResendCodes}
                      className="text-xs text-zinc-400 hover:text-amber-400 flex items-center gap-1.5 transition-colors disabled:opacity-50"
                    >
                      <RefreshCw size={13} className={resending ? "animate-spin" : ""} />
                      {resending ? "Reenviando..." : "Reenviar códigos"}
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        setStep("form");
                        setError("");
                        setSuccessMessage("");
                      }}
                      className="text-xs text-zinc-400 hover:text-zinc-200 transition-colors"
                    >
                      Alterar dados
                    </button>
                  </div>
                </form>
              </div>
            )}
          </div>

          <div className="flex items-center gap-2 text-sm mt-8 pt-4 border-t border-zinc-800">
            <span className="text-zinc-400 text-xs">Já possui uma conta ativa?</span>
            <button
              onClick={onNavigateToLogin}
              className="bg-white/10 hover:bg-white/20 rounded-lg px-3 py-1.5 text-xs text-zinc-200 transition-colors"
            >
              Fazer Login
            </button>
          </div>
        </div>

        {/* Painel amarelo — boas-vindas */}
        <div className="bg-amber-500 flex-1 p-10 flex flex-col justify-center items-start min-h-[480px]">
          <h2 className="text-3xl font-bold text-zinc-900 leading-tight">
            Bem vindo ao
            <br />
            investir mais
          </h2>
          <p className="text-sm text-zinc-900/80 mt-2 font-medium">
            Educação financeira segura para todas as idades.
          </p>
          <TrendingUp size={96} className="text-zinc-900/20 mt-10" />
        </div>
      </div>
    </div>
  );
}
