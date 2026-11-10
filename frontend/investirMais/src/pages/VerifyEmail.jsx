import { useState } from "react";
import { CheckCircle2, MailCheck, ShieldAlert } from "lucide-react";
import { Link, useNavigate, useSearchParams } from "react-router";
import { AuthCard } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { resendVerification, verifyEmail } from "../services/authService.js";

export default function VerifyEmail() {
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const token = search.get("token")?.trim() || "";
  const [status, setStatus] = useState("idle");
  const [errorMessage, setErrorMessage] = useState("");
  const [email, setEmail] = useState("");
  const [resendStatus, setResendStatus] = useState("");
  const [isResending, setIsResending] = useState(false);

  const handleConfirm = async () => {
    if (!token) {
      setStatus("error");
      setErrorMessage("Link inválido");
      return;
    }
    setStatus("loading");
    setErrorMessage("");
    try {
      const { signedIn } = await verifyEmail(token);
      if (signedIn) {
        await navigate("/", { replace: true });
      } else {
        setStatus("success");
      }
    } catch (error) {
      setStatus("error");
      setErrorMessage(apiError(error, "O link de confirmação é inválido ou expirou."));
    }
  };

  const handleResend = async (e) => {
    e.preventDefault();
    if (!email.trim()) return;
    setIsResending(true);
    setResendStatus("");
    try {
      const response = await resendVerification(email.trim());
      setResendStatus(response.message || "Se houver uma conta pendente para este e-mail, um novo link será enviado.");
    } catch (error) {
      setResendStatus(apiError(error, "Não foi possível reenviar o link agora. Tente novamente em instantes."));
    } finally {
      setIsResending(false);
    }
  };

  const isSuccess = status === "success";
  const isError = status === "error";

  return (
    <AuthCard
      icon={isSuccess ? CheckCircle2 : isError ? ShieldAlert : MailCheck}
      title={isSuccess ? "E-mail confirmado!" : isError ? "Falha na confirmação" : "Confirme seu e-mail"}
      description={
        isSuccess
          ? "Sua conta foi ativada com sucesso. Você já pode acessar a plataforma."
          : isError
          ? "Não foi possível ativar sua conta com este link."
          : "Clique no botão abaixo para ativar sua conta no Investir Mais."
      }
    >
      {isSuccess ? (
        <div className="space-y-4">
          <Notice>Conta ativada com sucesso!</Notice>
          <Link to="/entrar" className="button-primary w-full">
            Ir para o login
          </Link>
        </div>
      ) : isError ? (
        <div className="space-y-4">
          <Notice error>{errorMessage}</Notice>
          <form onSubmit={handleResend} className="space-y-3 pt-2">
            <p className="text-xs text-muted">
              Deseja receber outro link? Digite seu e-mail abaixo:
            </p>
            {resendStatus && <Notice>{resendStatus}</Notice>}
            <input
              type="email"
              required
              placeholder="seu-email@dominio.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="field"
            />
            <button
              type="submit"
              disabled={isResending}
              className="button-secondary w-full"
            >
              {isResending ? "Reenviando…" : "Reenviar link de confirmação"}
            </button>
          </form>
          <Link to="/entrar" className="block text-center text-sm font-semibold text-brand hover:underline pt-2">
            Voltar para o login
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {!token && (
            <Notice error>
              Nenhum código de confirmação foi fornecido no link.
            </Notice>
          )}
          <button
            type="button"
            onClick={handleConfirm}
            disabled={status === "loading" || !token}
            className="button-primary w-full"
          >
            {status === "loading" ? "Confirmando…" : "Confirmar e-mail"}
          </button>
          <Link to="/entrar" className="block text-center text-sm text-muted hover:text-ink">
            Voltar para entrar
          </Link>
        </div>
      )}
    </AuthCard>
  );
}
