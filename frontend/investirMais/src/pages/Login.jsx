import { useState } from "react";
import { KeyRound, Lock, Mail, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router";
import { AuthCard, emailRules, Field } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { login, verify2FA } from "../services/authService.js";

function returnPath(value) {
  if (!value?.startsWith("/") || !URL.canParse(value, window.location.origin)) return "/";
  const url = new URL(value, window.location.origin);
  const protectedPath = url.pathname === "/" || url.pathname === "/perfil" || /^\/educacao(?:\/|$)/.test(url.pathname);
  return url.origin === window.location.origin && protectedPath ? url.pathname + url.search : "/";
}

export default function Login() {
  const [email, setEmail] = useState("");
  const [search] = useSearchParams();
  const navigate = useNavigate();
  const credentials = useForm();
  const verification = useForm();
  const { errors, isSubmitting } = email ? verification.formState : credentials.formState;

  const sendCode = credentials.handleSubmit(async ({ email, password }) => {
    try {
      await login(email, password);
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
      title={email ? "Verificação em duas etapas" : "Bem-vindo de volta"}
      description={email ? "Digite o código enviado para " + email + ". Ele expira em cinco minutos." : "Entre na sua conta para acessar o Investir+."}
    >
      {email ? (
        <form key="code" onSubmit={verify} className="space-y-4" noValidate>
          {errors.root && <Notice error>{errors.root.message}</Notice>}
          <Field label="Código de verificação" autoComplete="one-time-code" maxLength={64} error={errors.code?.message}
            {...verification.register("code", { required: "Informe o código recebido.", setValueAs: (value) => value.trim() })} />
          <button className="button-primary w-full" disabled={isSubmitting}>{isSubmitting ? "Confirmando…" : "Confirmar acesso"}</button>
          <button type="button" className="w-full text-sm text-muted hover:text-ink" disabled={isSubmitting} onClick={() => { verification.reset(); setEmail(""); }}>
            Voltar e usar outro e-mail
          </button>
        </form>
      ) : (
        <form key="credentials" onSubmit={sendCode} className="space-y-4" noValidate>
          {errors.root && <Notice error>{errors.root.message}</Notice>}
          <Field label="E-mail" icon={Mail} type="email" autoComplete="username" error={errors.email?.message} {...credentials.register("email", emailRules)} />
          <Field label="Senha" icon={Lock} type="password" autoComplete="current-password" error={errors.password?.message}
            {...credentials.register("password", { required: "Informe sua senha." })} />
          <Link to="/recuperar-senha" className="block text-right text-sm text-brand hover:underline">Esqueceu a senha?</Link>
          <button className="button-primary w-full" disabled={isSubmitting}>{isSubmitting ? "Verificando…" : "Continuar"}</button>
          <p className="text-center text-sm text-muted">Ainda sem conta? <Link to="/cadastro" className="font-semibold text-brand hover:underline">Cadastre-se</Link></p>
        </form>
      )}
    </AuthCard>
  );
}
