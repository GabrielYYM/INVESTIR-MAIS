import { useState } from "react";
import { CheckCircle2, KeyRound, Lock, Mail, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { AuthCard, confirmPasswordRules, emailRules, Field, newPasswordRules } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
import { forgotPassword, resetPassword } from "../services/authService.js";

export default function ForgotPassword() {
  const [email, setEmail] = useState("");
  const [complete, setComplete] = useState(false);
  const request = useForm();
  const reset = useForm();
  const resend = useForm();

  const sendCode = (form, onSuccess) => form.handleSubmit(async (values) => {
    try {
      const address = email || values.email;
      await forgotPassword(address);
      onSuccess(address);
    } catch (error) {
      form.setError("root", { message: apiError(error, "Não foi possível solicitar o código agora.") });
    }
  });

  const changePassword = reset.handleSubmit(async ({ token, password }) => {
    try {
      await resetPassword(token, password);
      reset.reset();
      setComplete(true);
    } catch (error) {
      reset.setError("root", { message: apiError(error, "Código inválido ou expirado. Confira os dados ou solicite outro código.") });
    }
  });

  return (
    <AuthCard
      icon={complete ? CheckCircle2 : email ? KeyRound : ShieldCheck}
      title={complete ? "Senha atualizada" : email ? "Redefinir senha" : "Recuperar senha"}
      description={complete ? "Você já pode entrar usando sua nova senha." : email ? "Se houver uma conta com esse e-mail, o código chegará em instantes." : "Informe o e-mail da conta para receber um código de recuperação."}
    >
      {!complete && (email ? (
        <>
          <form onSubmit={changePassword} className="space-y-4" noValidate>
            {reset.formState.errors.root && <Notice error>{reset.formState.errors.root.message}</Notice>}
            <Field label="Código de recuperação" icon={KeyRound} autoComplete="one-time-code" maxLength={64} error={reset.formState.errors.token?.message}
              {...reset.register("token", { required: "Informe o código recebido.", setValueAs: (value) => value.trim() })} />
            <Field label="Nova senha" icon={Lock} type="password" autoComplete="new-password" error={reset.formState.errors.password?.message}
              {...reset.register("password", newPasswordRules)} />
            <Field label="Confirme a nova senha" icon={Lock} type="password" autoComplete="new-password" error={reset.formState.errors.confirmPassword?.message}
              {...reset.register("confirmPassword", confirmPasswordRules)} />
            <button className="button-primary w-full" disabled={reset.formState.isSubmitting || resend.formState.isSubmitting}>Redefinir senha</button>
          </form>
          <form onSubmit={sendCode(resend, () => {})} className="space-y-3">
            {resend.formState.errors.root && <Notice error>{resend.formState.errors.root.message}</Notice>}
            {resend.formState.isSubmitSuccessful && <Notice>Solicitamos um novo código para o e-mail informado.</Notice>}
            <button className="button-secondary w-full" disabled={reset.formState.isSubmitting || resend.formState.isSubmitting}>Solicitar outro código</button>
          </form>
        </>
      ) : (
        <form onSubmit={sendCode(request, setEmail)} className="space-y-4" noValidate>
          {request.formState.errors.root && <Notice error>{request.formState.errors.root.message}</Notice>}
          <Field label="E-mail" icon={Mail} type="email" autoComplete="email" error={request.formState.errors.email?.message} {...request.register("email", emailRules)} />
          <button className="button-primary w-full" disabled={request.formState.isSubmitting}>Enviar código</button>
        </form>
      ))}
      <Link to="/entrar" className="block text-center text-sm font-semibold text-brand hover:underline">Voltar para entrar</Link>
    </AuthCard>
  );
}
