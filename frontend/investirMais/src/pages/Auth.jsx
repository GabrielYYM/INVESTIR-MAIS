import { BookOpen } from "lucide-react";
import { Form, Link, redirect, useActionData, useMatches, useNavigation, useSearchParams } from "react-router";
import * as auth from "../services/authService";
import { forgotPassword, registerUser, resendVerification, resetPassword } from "../generated/spring";
import { routeAction, confirmPassword } from "../services/routing";
import { Field, Notice, PasswordField } from "../components/ui";

const TITLES = { login: "Bem-vindo de volta", register: "Crie sua conta", forgot: "Recuperar senha", reset: "Redefinir senha", resend: "Confirmar seu e-mail" };
const NOTICES = { expired: "Sua sessão expirou. Entre novamente.", password: "Senha alterada. Entre novamente com a nova senha.", email: "E-mail alterado. Confirme o novo endereço pelo link recebido e entre novamente." };

export const authAction = (mode) => routeAction(async (form) => {
  const values = Object.fromEntries(form);
  const email = values.email?.trim();
  if (mode === "login") {
    if (values.intent === "verify") { await auth.verify2FA(values.token); return redirect("/cursos"); }
    if (values.intent === "resend") { await auth.resendCode(); return { phase: "verify", email, message: "Um novo código foi enviado ao seu e-mail." }; }
    await auth.login(email, values.password);
    return { phase: "verify", email };
  }
  if (mode === "register") {
    confirmPassword(form);
    await registerUser({ name: values.name.trim(), age: Number(values.age), email, password: values.password });
    return { message: "Conta criada. Confirme seu e-mail pelo link recebido antes de entrar." };
  }
  if (mode === "forgot") return { message: (await forgotPassword({ email })).message };
  if (mode === "resend") return { message: (await resendVerification({ email })).message };
  confirmPassword(form);
  await resetPassword({ token: values.token.trim(), newPassword: values.newPassword });
  return redirect("/login?notice=password");
}, (form) => ({ phase: form.get("phase"), email: form.get("email") }));

export function Component() {
  const mode = useMatches().at(-1).id;
  const result = useActionData();
  const navigation = useNavigation();
  const [search] = useSearchParams();
  const pending = navigation.state !== "idle";
  const secondFactor = result?.phase === "verify";
  return <main className="flex min-h-dvh items-center justify-center px-4 py-10"><section className="panel w-full max-w-md">
    <Link to="/login" className="mb-8 flex items-center gap-3 font-bold tracking-wide"><BookOpen className="text-amber-400" /> INVESTIR MAIS</Link>
    <h1 className="text-2xl font-bold">{secondFactor ? "Verificação em duas etapas" : TITLES[mode]}</h1>
    <p className="mt-2 mb-6 text-sm text-zinc-400">{secondFactor ? `Cole o código enviado para ${result.email}. Ele expira em cinco minutos.` : "Aprenda e acompanhe o mercado em um só lugar."}</p>
    <Form method="post" replace className="grid gap-4">
      <Notice error={result?.error}>{result?.message || NOTICES[search.get("notice")]}</Notice>
      <fieldset disabled={pending} className="grid gap-4 disabled:opacity-60">
        <input type="hidden" name="phase" value={secondFactor ? "verify" : "login"} />
        {mode === "register" && <><Field label="Nome completo" name="name" autoComplete="name" minLength={2} maxLength={100} /><Field label="Idade" name="age" type="number" min={0} max={120} step={1} /></>}
        {!secondFactor && mode !== "reset" && <Field label="E-mail" name="email" type="email" autoComplete="username" defaultValue={result?.email} />}
        {secondFactor && <input type="hidden" name="email" value={result.email} />}
        {(secondFactor || mode === "reset") && <Field label={secondFactor ? "Código de verificação" : "Token de recuperação"} name="token" autoComplete="one-time-code" maxLength={64} />}
        {mode === "login" && !secondFactor && <PasswordField label="Senha" name="password" autoComplete="current-password" minLength={1} />}
        {(mode === "register" || mode === "reset") && <><PasswordField label={mode === "reset" ? "Nova senha" : "Senha"} name={mode === "reset" ? "newPassword" : "password"} /><PasswordField label="Confirmar senha" name="confirmPassword" /></>}
        <button className="btn" name="intent" value={secondFactor ? "verify" : "password"} disabled={pending}>{pending ? "Aguarde…" : secondFactor ? "Confirmar acesso" : ({ login: "Continuar", register: "Criar conta", forgot: "Enviar recuperação", reset: "Redefinir senha", resend: "Reenviar confirmação" })[mode]}</button>
      </fieldset>
    </Form>
    {secondFactor && <Form method="post" replace className="mt-4">
      <input type="hidden" name="intent" value="resend" /><input type="hidden" name="phase" value="verify" /><input type="hidden" name="email" value={result.email} />
      <button className="link" disabled={pending}>Reenviar código</button>
    </Form>}
    <nav aria-label="Acesso à conta" className="mt-6 flex flex-wrap gap-x-5 gap-y-3">
      {(secondFactor || mode !== "login") && <Link to="/login" className="link">{secondFactor ? "Voltar ao login" : "Entrar"}</Link>}
      {mode === "login" && !secondFactor && <><Link to="/register" className="link">Criar conta</Link><Link to="/forgot" className="link">Esqueci minha senha</Link><Link to="/resend" className="link">Reenviar confirmação</Link></>}
      {mode === "forgot" && <Link to="/reset" className="link">Já tenho o token</Link>}
    </nav>
  </section></main>;
}
