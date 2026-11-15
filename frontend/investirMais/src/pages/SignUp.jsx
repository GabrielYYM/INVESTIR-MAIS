import { lazy, Suspense, useState } from "react";
import { Calendar, CheckCircle2, Mail, ShieldCheck, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { useRegisterUser, useResendVerification } from "../generated/account.ts";
import { AuthCard, Field } from "../components/ui/FormFields.jsx";
import PasswordFields from "../components/account/PasswordFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import { accountResolver, registrationSchema } from "../services/accountSchema.js";
import { apiError } from "../services/api.js";
const TermsModal = lazy(() => import("../components/account/TermsModal.jsx"));
export default function SignUp() {
  const [termsModalTab, setTermsModalTab] = useState(null);
  const form = useForm({
    resolver: accountResolver(registrationSchema)
  });
  const registration = useRegisterUser({
    mutation: {
      onSuccess: () => form.reset()
    }
  });
  const resend = useResendVerification();
  const email = registration.isSuccess ? registration.variables.data.email : "";
  const {
    errors
  } = form.formState;
  const submit = form.handleSubmit(({
    name,
    age,
    email,
    password
  }) => registration.mutate({
    data: {
      name,
      age,
      email,
      password
    }
  }));
  return <AuthCard icon={email ? CheckCircle2 : ShieldCheck} title={email ? "Confirme seu e-mail" : "Crie sua conta"} description={email ? `Se o cadastro puder ser realizado, você receberá um link em ${email}. Confira também a pasta de spam.` : "Crie uma conta para explorar os cursos de educação financeira."}>
    {email ? <form onSubmit={event => {
      event.preventDefault();
      resend.mutate({
        data: {
          email
        }
      });
    }} className="space-y-4">
      {resend.error && <Notice error>{apiError(resend.error, "Não foi possível reenviar o link agora.")}</Notice>}
      {resend.isSuccess && <Notice>Link de confirmação solicitado novamente.</Notice>}
      <button disabled={resend.isPending} type="submit" className="button-secondary w-full">Reenviar link de confirmação</button>
    </form> : <form onSubmit={submit} noValidate className="space-y-4">
      {registration.error && <Notice error>{apiError(registration.error, "Não foi possível criar sua conta. Verifique os dados informados.")}</Notice>}
      <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} placeholder="Ex: Maria da Silva" error={errors.name?.message} {...form.register("name")} />
      <Field label="Idade" icon={Calendar} type="number" min={0} max={120} placeholder="Ex: 25" error={errors.age?.message} {...form.register("age", {
        valueAsNumber: true
      })} />
      <Field label="E-mail" icon={Mail} type="email" autoComplete="email" placeholder="seu.email@exemplo.com" error={errors.email?.message} {...form.register("email")} />
      <PasswordFields form={form} creating />
      <label className="flex items-start gap-3 rounded-xl border border-line bg-canvas p-4 text-xs leading-relaxed text-muted">
        <input type="checkbox" className="mt-0.5 size-4 shrink-0 accent-amber-500" {...form.register("acceptTerms")} />
        <span>Concordo com os{" "}
          <button type="button" onClick={event => {
            event.preventDefault();
            setTermsModalTab("termos");
          }} className="font-semibold text-brand underline hover:text-amber-400">Termos de Uso</button>{" "}
          e a{" "}<button type="button" onClick={event => {
            event.preventDefault();
            setTermsModalTab("privacidade");
          }} className="font-semibold text-brand underline hover:text-amber-400">Política de Privacidade</button>{" "}
          do Investir Mais.
        </span>
      </label>
      {errors.acceptTerms && <p role="alert" className="text-sm text-negative">{errors.acceptTerms.message}</p>}
      <p className="text-xs text-muted">Após verificar seu e-mail, você confirmará a versão vigente dos documentos. Essa confirmação ficará disponível no seu perfil.</p>
      <button disabled={registration.isPending} type="submit" className="button-primary w-full">{registration.isPending ? "Criando conta…" : "Criar conta"}</button>
    </form>}
    <Link to="/entrar" className="block text-center text-sm font-semibold text-brand hover:underline">Voltar para entrar</Link>
    {termsModalTab && <Suspense fallback={null}><TermsModal initialTab={termsModalTab} onClose={() => setTermsModalTab(null)} /></Suspense>}
  </AuthCard>;
}
