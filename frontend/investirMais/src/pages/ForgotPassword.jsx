import { CheckCircle2, KeyRound, Mail, ShieldCheck } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link } from "react-router";
import { useForgotPassword, useResetPassword } from "../generated/account.ts";
import { AuthCard, Field } from "../components/ui/FormFields.jsx";
import PasswordFields from "../components/account/PasswordFields.jsx";
import ResendActionForm from "../components/account/ResendActionForm.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import SubmitButton from "../components/ui/SubmitButton.jsx";
import { accountResolver, emailSchema, resetPasswordSchema } from "../services/accountSchema.js";
import { apiError } from "../services/api.js";
export default function ForgotPassword() {
  const requestForm = useForm({
    resolver: accountResolver(emailSchema)
  });
  const resetForm = useForm({
    resolver: accountResolver(resetPasswordSchema)
  });
  const request = useForgotPassword();
  const resend = useForgotPassword();
  const reset = useResetPassword({
    mutation: {
      onSuccess: () => resetForm.reset()
    }
  });
  const email = request.isSuccess ? request.variables.data.email : "";
  const complete = reset.isSuccess;
  const busy = reset.isPending || resend.isPending;
  const sendCode = requestForm.handleSubmit(data => request.mutate({
    data
  }));
  const changePassword = resetForm.handleSubmit(({
    token,
    password
  }) => reset.mutate({
    data: {
      token,
      newPassword: password
    }
  }));
  return <AuthCard icon={complete ? CheckCircle2 : email ? KeyRound : ShieldCheck} title={complete ? "Senha atualizada" : email ? "Redefinir senha" : "Recuperar senha"} description={complete ? "Você já pode entrar usando sua nova senha." : email ? "Se houver uma conta com esse e-mail, o código chegará em instantes." : "Informe o e-mail da conta para receber um código de recuperação."}>
    {!complete && (email ? <>
      <form onSubmit={changePassword} noValidate className="space-y-4">
        {reset.error && <Notice error>{apiError(reset.error, "Código inválido ou expirado. Confira os dados ou solicite outro código.")}</Notice>}
        <Field label="Código de recuperação" icon={KeyRound} autoComplete="one-time-code" maxLength={64} placeholder="Cole ou digite o código recebido por e-mail" error={resetForm.formState.errors.token?.message} {...resetForm.register("token")} />
        <PasswordFields form={resetForm} />
        <SubmitButton loading={busy} fullWidth>Redefinir senha</SubmitButton>
      </form>
      <ResendActionForm
        mutation={resend}
        email={email}
        busy={busy}
        buttonLabel="Solicitar outro código"
        successMessage="Solicitamos um novo código para o e-mail informado."
        errorMessage="Não foi possível solicitar o código agora."
      />
    </> : <form onSubmit={sendCode} noValidate className="space-y-4">
      {request.error && <Notice error>{apiError(request.error, "Não foi possível solicitar o código agora.")}</Notice>}
      <Field label="E-mail" icon={Mail} type="email" autoComplete="email" placeholder="seu.email@exemplo.com" hint="Enviaremos as instruções de recuperação para este endereço." error={requestForm.formState.errors.email?.message} {...requestForm.register("email")} />
      <SubmitButton loading={request.isPending} fullWidth>Enviar código</SubmitButton>
    </form>)}
    <Link to="/entrar" className="block text-center text-sm font-semibold text-brand hover:underline">Voltar para entrar</Link>
  </AuthCard>;
}
