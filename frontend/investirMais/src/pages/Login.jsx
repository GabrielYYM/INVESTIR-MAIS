import { useState } from "react";
import { Eye, EyeOff, TrendingUp } from "lucide-react";
import { iniciarLogin, confirmarCodigo2FA } from "../services/authService";

export default function Login({ onLoginSuccess, onNavigateToRegister }) {
  const [step, setStep] = useState("credentials"); // "credentials" | "2fa"
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [code, setCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmitCredentials(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await iniciarLogin(email, password);
      setStep("2fa");
    } catch (err) {
      setError(err.response?.data?.detail || "Email ou senha inválidos.");
    } finally {
      setLoading(false);
    }
  }

  async function handleSubmitCode(e) {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await confirmarCodigo2FA(email, code);
      onLoginSuccess?.();
    } catch (err) {
      setError(err.response?.data?.detail || "Código inválido ou expirado.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#EDE58C] p-6">
      <div className="w-full max-w-3xl rounded-2xl overflow-hidden flex flex-col md:flex-row shadow-xl">
        {/* Painel escuro — formulário */}
        <div className="bg-[#171522] text-white flex-1 p-10 flex flex-col justify-between min-h-[420px]">
          <div>
            {step === "credentials" ? (
              <>
                <h1 className="text-2xl font-bold mb-1">Login</h1>
                <p className="text-sm text-zinc-400 mb-6">Insira os dados da sua conta</p>

                <form onSubmit={handleSubmitCredentials} className="space-y-4">
                  <div>
                    <label className="text-xs text-zinc-400">Email</label>
                    <input
                      type="email"
                      required
                      value={email}
                      onChange={(e) => setEmail(e.target.value)}
                      className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="text-xs text-zinc-400">Senha</label>
                    <div className="flex items-center border-b border-zinc-600 focus-within:border-amber-500">
                      <input
                        type={showPassword ? "text" : "password"}
                        required
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="w-full bg-transparent py-1.5 text-sm outline-none"
                      />
                      <button type="button" onClick={() => setShowPassword((v) => !v)}>
                        {showPassword ? (
                          <EyeOff size={16} className="text-zinc-500" />
                        ) : (
                          <Eye size={16} className="text-zinc-500" />
                        )}
                      </button>
                    </div>
                  </div>

                  <button type="button" className="text-xs text-zinc-400 hover:text-amber-500">
                    Esqueceu a senha?
                  </button>

                  {error && <p className="text-xs text-red-400">{error}</p>}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-full py-2.5 text-sm transition-colors"
                  >
                    {loading ? "Enviando..." : "Login"}
                  </button>
                </form>
              </>
            ) : (
              <>
                <h1 className="text-2xl font-bold mb-1">Verificação</h1>
                <p className="text-sm text-zinc-400 mb-6">
                  Enviamos um código para <span className="text-zinc-200">{email}</span>
                </p>

                <form onSubmit={handleSubmitCode} className="space-y-4">
                  <div>
                    <label className="text-xs text-zinc-400">Código</label>
                    <input
                      type="text"
                      required
                      maxLength={6}
                      value={code}
                      onChange={(e) => setCode(e.target.value)}
                      placeholder="000000"
                      className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm tracking-[0.3em] outline-none focus:border-amber-500"
                    />
                  </div>

                  {error && <p className="text-xs text-red-400">{error}</p>}

                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-full py-2.5 text-sm transition-colors"
                  >
                    {loading ? "Verificando..." : "Confirmar"}
                  </button>

                  <button
                    type="button"
                    onClick={() => setStep("credentials")}
                    className="w-full text-xs text-zinc-400 hover:text-amber-500"
                  >
                    Voltar
                  </button>
                </form>
              </>
            )}
          </div>

          <div className="flex items-center gap-2 text-sm mt-8">
            <span className="text-zinc-400">Não possui uma conta?</span>
            <button
              onClick={onNavigateToRegister}
              className="bg-white/10 hover:bg-white/20 rounded-lg px-3 py-1.5 text-xs text-zinc-200"
            >
              Registrar
            </button>
          </div>
        </div>

        {/* Painel amarelo — boas-vindas */}
        <div className="bg-amber-500 flex-1 p-10 flex flex-col justify-center items-start min-h-[420px]">
          <h2 className="text-3xl font-bold text-zinc-900 leading-tight">
            Bem vindo <span className="font-normal">ao</span>
            <br />
            investir mais
          </h2>
          <p className="text-sm text-zinc-900/70 mt-2">faça login para acessar sua conta</p>
          <TrendingUp size={96} className="text-zinc-900/20 mt-10" />
        </div>
      </div>
    </div>
  );
}
