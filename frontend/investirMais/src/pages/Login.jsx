import { useEffect, useRef, useState } from "react";
import { KeyRound, Lock, Mail, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router";
import { AuthCard, emailRules, Field } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { login, resendVerification, verify2FA } from "../services/authService.js";

function returnPath(value) {
  if (!value?.startsWith("/") || !URL.canParse(value, window.location.origin)) return "/";
  const url = new URL(value, window.location.origin);
  const protectedPath = url.pathname === "/" || url.pathname === "/perfil" || /^\/educacao(?:\/|$)/.test(url.pathname);
  return url.origin === window.location.origin && protectedPath ? url.pathname + url.search : "/";
}

export function LoginLink() {
  const [token] = useState(() => new URLSearchParams(window.location.hash.slice(1)).get("ott"));
  const [error, setError] = useState("");
  const attempt = useRef();
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const destination = returnPath(search.get("retorno"));
  const loginPath = "/entrar?" + new URLSearchParams({ retorno: destination });
  const failure = error || (!token && "Abra o link enviado por e-mail ou entre novamente para solicitar outro.");

  useEffect(() => {
    if (!token) return;
    if (!attempt.current) {
      const { pathname, search } = window.location;
      window.history.replaceState(window.history.state, "", pathname + search);
      attempt.current = verify2FA(token);
    }

    let active = true;
    attempt.current.then(
      () => { if (active) void navigate(destination, { replace: true }); },
      (error) => {
        if (active) setError(apiError(error, "O link expirou ou não pertence a esta sessão. Entre novamente neste navegador."));
      },
    );
    return () => { active = false; };
  }, [destination, navigate, token]);

  return (
    <AuthCard icon={KeyRound} title={failure ? "Não foi possível concluir o acesso" : "Confirmando acesso"}
      description={failure ? "Você pode iniciar outro login para receber um novo link." : "Estamos validando o link enviado por e-mail."}>
      {failure && <><Notice error>{failure}</Notice><Link className="button-primary w-full" to={loginPath} replace>Voltar para entrar</Link></>}
    </AuthCard>
  );
}

export default function Login() {
  const [email, setEmail] = useState("");
  const [resendStatus, setResendStatus] = useState("");
  const [isResending, setIsResending] = useState(false);
  const [search] = useSearchParams();
  const navigate = useNavigate();
  const credentials = useForm();
  const verification = useForm();
  const { errors, isSubmitting } = email ? verification.formState : credentials.formState;

  const handleResend = async () => {
    const enteredEmail = credentials.getValues("email")?.trim();
    if (!enteredEmail) {
      credentials.setError("email", { message: "Informe seu e-mail para receber a confirmação." });
      return;
    }
    setIsResending(true);
    setResendStatus("");
    try {
      const response = await resendVerification(enteredEmail);
      setResendStatus(response.message || "Se houver uma conta pendente para este e-mail, uma nova confirmação será enviada.");
    } catch (error) {
      setResendStatus(apiError(error, "Não foi possível reenviar o link agora. Tente novamente em instantes."));
    } finally {
      setIsResending(false);
    }
  };

  const sendLink = credentials.handleSubmit(async ({ email, password }) => {
    try {
      setResendStatus("");
      await login(email, password, returnPath(search.get("retorno")));
      credentials.reset({ email, password: "" });
      setEmail(email);
    } catch (error) {
      credentials.setError("root", { message: apiError(error, "Não foi possível entrar. Confira o e-mail, a senha e a confirmação da conta.") });
    }
  });

  const verify = verification.handleSubmit(async ({ code }) => {
    try {
      await verify2FA(code);
      await navigate(returnPath(search.get("retorno")), { replace: true });
    } catch (error) {
      verification.setError("root", { message: apiError(error, "Código inválido ou expirado.") });
    }
  });

  return (
    <AuthCard
      icon={email ? KeyRound : ShieldCheck}
      title={email ? "Confira seu e-mail" : "Bem-vindo de volta"}
      description={email ? `Enviamos um link para ${email}. Abra a mensagem e clique em “Concluir acesso” neste navegador. O link expira em cinco minutos.` : "Entre na sua conta para acessar o Investir+."}
    >
      {email ? (
        <div className="space-y-4">
          <details className="rounded-xl border border-white/10 p-3">
            <summary className="cursor-pointer text-sm font-medium text-muted hover:text-ink">Usar código manualmente</summary>
            <form onSubmit={verify} className="mt-4 space-y-4" noValidate>
              {errors.root && <Notice error>{errors.root.message}</Notice>}
              <Field
                label="Código de verificação"
                autoComplete="one-time-code"
                maxLength={64}
                placeholder="Insira o código de verificação recebido"
                hint="Caso prefira, você também pode clicar diretamente no link enviado para o seu e-mail."
                error={errors.code?.message}
                {...verification.register("code", { required: "Informe o código recebido.", setValueAs: (value) => value.trim() })}
              />
              <button className="button-primary w-full" disabled={isSubmitting}>{isSubmitting ? "Confirmando…" : "Confirmar acesso"}</button>
            </form>
          </details>
          <button type="button" className="w-full text-sm text-muted hover:text-ink" disabled={isSubmitting} onClick={() => { verification.reset(); setEmail(""); }}>
            Voltar e usar outro e-mail
          </button>
        </div>
      ) : (
        <form key="credentials" onSubmit={sendLink} className="space-y-4" noValidate>
          {errors.root && (
            <div className="space-y-2">
              <Notice error>{errors.root.message}</Notice>
              <button
                type="button"
                onClick={handleResend}
                disabled={isResending}
                className="text-xs font-semibold text-brand hover:underline block text-left"
              >
                {isResending ? "Reenviando confirmação…" : "Não confirmou seu e-mail? Clique aqui para reenviar"}
              </button>
            </div>
          )}
          {resendStatus && <Notice>{resendStatus}</Notice>}
          <Field
            label="E-mail"
            icon={Mail}
            type="email"
            autoComplete="username"
            placeholder="seu.email@exemplo.com"
            error={errors.email?.message}
            {...credentials.register("email", emailRules)}
          />
          <Field
            label="Senha"
            icon={Lock}
            type="password"
            autoComplete="current-password"
            placeholder="Digite sua senha de acesso"
            error={errors.password?.message}
            {...credentials.register("password", { required: "Informe sua senha." })}
          />
          <div className="flex items-center justify-between text-xs">
            <button
              type="button"
              onClick={handleResend}
              disabled={isResending}
              className="text-muted hover:text-brand hover:underline"
            >
              Reenviar confirmação
            </button>
            <Link to="/recuperar-senha" className="text-brand hover:underline">Esqueceu a senha?</Link>
          </div>
          <button className="button-primary w-full" disabled={isSubmitting}>{isSubmitting ? "Verificando…" : "Continuar"}</button>
          <p className="text-center text-sm text-muted">Ainda sem conta? <Link to="/cadastro" className="font-semibold text-brand hover:underline">Cadastre-se</Link></p>
        </form>
      )}
    </AuthCard>
  );
}
