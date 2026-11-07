import { useState } from "react";
import { Calendar, CheckCircle2, Lock, Mail, ShieldCheck, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { AuthCard, confirmPasswordRules, emailRules, Field, newPasswordRules } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { register as createAccount, resendVerification } from "../services/authService.js";

export default function SignUp() {
  const [email, setEmail] = useState("");
  const form = useForm();
  const resend = useForm();
  const { errors, isSubmitting } = form.formState;

  const submit = form.handleSubmit(async ({ name, age, email, password }) => {
    try {
      await createAccount({ name, age, email, password });
      form.reset();
      setEmail(email);
    } catch (error) {
      const fallback = "Não foi possível criar sua conta. Verifique os dados informados.";
      form.setError("root", { message: apiError(error, fallback) });
    }
  });

  const resendLink = resend.handleSubmit(async () => {
    try {
      await resendVerification(email);
    } catch (error) {
      resend.setError("root", { message: apiError(error, "Não foi possível reenviar o link agora.") });
    }
  });

  return (
    <AuthCard
      icon={email ? CheckCircle2 : ShieldCheck}
      title={email ? "Confirme seu e-mail" : "Crie sua conta"}
      description={email ? "Se o cadastro puder ser realizado, você receberá um link em " + email + ". Confira também a pasta de spam." : "Crie uma conta para explorar os cursos e consultar cotações."}
    >
      {email ? (
        <form onSubmit={resendLink} className="space-y-4">
          {resend.formState.errors.root && <Notice error>{resend.formState.errors.root.message}</Notice>}
          {resend.formState.isSubmitSuccessful && <Notice>Link de confirmação solicitado novamente.</Notice>}
          <button className="button-secondary w-full" disabled={resend.formState.isSubmitting}>Reenviar link de confirmação</button>
        </form>
      ) : (
        <form onSubmit={submit} className="space-y-4" noValidate>
          {errors.root && <Notice error>{errors.root.message}</Notice>}
          <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={errors.name?.message}
            {...form.register("name", { setValueAs: (value) => value.trim(), required: "Informe seu nome.", minLength: { value: 2, message: "Informe pelo menos 2 caracteres." } })} />
          <Field label="Idade" icon={Calendar} type="number" min={0} max={120} error={errors.age?.message}
            {...form.register("age", { valueAsNumber: true, validate: (value) => (Number.isInteger(value) && value >= 0 && value <= 120) || "Informe uma idade inteira de 0 a 120." })} />
          <Field label="E-mail" icon={Mail} type="email" autoComplete="email" error={errors.email?.message} {...form.register("email", emailRules)} />
          <Field label="Senha" icon={Lock} type="password" autoComplete="new-password" error={errors.password?.message} {...form.register("password", newPasswordRules)} />
          <Field label="Confirme a senha" icon={Lock} type="password" autoComplete="new-password" error={errors.confirmPassword?.message}
            {...form.register("confirmPassword", confirmPasswordRules)} />
          <label className="flex items-start gap-2.5 text-sm text-muted">
            <input type="checkbox" className="mt-0.5 size-4 shrink-0 accent-brand" {...form.register("acceptTerms", { required: "Aceite os termos para continuar." })} />
            Concordo com os Termos de Uso e a Política de Privacidade do Investir Mais.
          </label>
          {errors.acceptTerms && <p role="alert" className="text-xs text-negative">{errors.acceptTerms.message}</p>}
          <button className="button-primary w-full" disabled={isSubmitting}>{isSubmitting ? "Criando conta…" : "Criar conta"}</button>
        </form>
      )}
      <Link to="/entrar" className="block text-center text-sm font-semibold text-brand hover:underline">Voltar para entrar</Link>
    </AuthCard>
  );
}
