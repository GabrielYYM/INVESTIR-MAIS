import { useState } from "react";
import { Eye, EyeOff, TrendingUp } from "lucide-react";
import { registrar } from "../services/authService";

export default function Register({ onRegisterSuccess, onNavigateToLogin }) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");

    if (password !== confirmPassword) {
      setError("As senhas não coincidem.");
      return;
    }

    setLoading(true);
    try {
      await registrar(name, email, password);
      onRegisterSuccess?.();
    } catch (err) {
      setError(err.response?.data?.detail || "Não foi possível criar a conta.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#EDE58C] p-6">
      <div className="w-full max-w-3xl rounded-2xl overflow-hidden flex flex-col md:flex-row shadow-xl">
        {/* Painel escuro — formulário */}
        <div className="bg-[#171522] text-white flex-1 p-10 flex flex-col justify-between min-h-[480px]">
          <div>
            <h1 className="text-2xl font-bold mb-1">Registrar</h1>
            <p className="text-sm text-zinc-400 mb-6">Insira os dados da sua conta</p>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="text-xs text-zinc-400">Nome</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="w-full bg-transparent border-b border-zinc-600 py-1.5 text-sm outline-none focus:border-amber-500"
                />
              </div>

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

              <div>
                <label className="text-xs text-zinc-400">Confirme a sua senha</label>
                <div className="flex items-center border-b border-zinc-600 focus-within:border-amber-500">
                  <input
                    type={showPassword ? "text" : "password"}
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full bg-transparent py-1.5 text-sm outline-none"
                  />
                </div>
              </div>

              {error && <p className="text-xs text-red-400">{error}</p>}

              <button
                type="submit"
                disabled={loading}
                className="w-full bg-amber-500 hover:bg-amber-400 disabled:opacity-50 text-zinc-900 font-medium rounded-full py-2.5 text-sm transition-colors"
              >
                {loading ? "Criando conta..." : "Registrar"}
              </button>
            </form>
          </div>

          <div className="flex items-center gap-2 text-sm mt-8">
            <span className="text-zinc-400">Já possui uma conta?</span>
            <button
              onClick={onNavigateToLogin}
              className="bg-white/10 hover:bg-white/20 rounded-lg px-3 py-1.5 text-xs text-zinc-200"
            >
              Login
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
          <p className="text-sm text-zinc-900/70 mt-2">insira seus dados para criar sua conta</p>
          <TrendingUp size={96} className="text-zinc-900/20 mt-10" />
        </div>
      </div>
    </div>
  );
}
