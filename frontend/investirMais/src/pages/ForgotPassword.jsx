import { Button, Stack } from "@mui/material";
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
  const sendCode = (form, onSuccess) => form.handleSubmit(async values => {
    try {
      const address = email || values.email;
      await forgotPassword(address);
      onSuccess(address);
    } catch (error) {
      form.setError("root", {
        message: apiError(error, "Não foi possível solicitar o código agora.")
      });
    }
  });
  const changePassword = reset.handleSubmit(async ({
    token,
    password
  }) => {
    try {
      await resetPassword(token, password);
      reset.reset();
      setComplete(true);
    } catch (error) {
      reset.setError("root", {
        message: apiError(error, "Código inválido ou expirado. Confira os dados ou solicite outro código.")
      });
    }
  });
  return <AuthCard icon={complete ? CheckCircle2 : email ? KeyRound : ShieldCheck} title={complete ? "Senha atualizada" : email ? "Redefinir senha" : "Recuperar senha"} description={complete ? "Você já pode entrar usando sua nova senha." : email ? "Se houver uma conta com esse e-mail, o código chegará em instantes." : "Informe o e-mail da conta para receber um código de recuperação."}>
      {!complete && (email ? <>
          <Stack onSubmit={changePassword} noValidate spacing={2} component="form">
            {reset.formState.errors.root && <Notice error>{reset.formState.errors.root.message}</Notice>}
            <Field label="Código de recuperação" icon={KeyRound} autoComplete="one-time-code" maxLength={64} placeholder="Cole ou digite o código recebido por e-mail" error={reset.formState.errors.token?.message} {...reset.register("token", {
          required: "Informe o código recebido.",
          setValueAs: value => value.trim()
        })} />
            <Field label="Nova senha" icon={Lock} type="password" autoComplete="new-password" placeholder="Digite a nova senha (mínimo 8 caracteres)" hint="A senha deve ter no mínimo 8 caracteres." error={reset.formState.errors.password?.message} {...reset.register("password", newPasswordRules)} />
            <Field label="Confirme a nova senha" icon={Lock} type="password" autoComplete="new-password" placeholder="Repita a nova senha criada" error={reset.formState.errors.confirmPassword?.message} {...reset.register("confirmPassword", confirmPasswordRules)} />
            <Button disabled={reset.formState.isSubmitting || resend.formState.isSubmitting} type="submit" variant="contained">Redefinir senha</Button>
          </Stack>
          <Stack onSubmit={sendCode(resend, () => {})} spacing={2} component="form">
            {resend.formState.errors.root && <Notice error>{resend.formState.errors.root.message}</Notice>}
            {resend.formState.isSubmitSuccessful && <Notice>Solicitamos um novo código para o e-mail informado.</Notice>}
            <Button disabled={reset.formState.isSubmitting || resend.formState.isSubmitting} type="submit" variant="outlined">Solicitar outro código</Button>
          </Stack>
        </> : <Stack onSubmit={sendCode(request, setEmail)} noValidate spacing={2} component="form">
          {request.formState.errors.root && <Notice error>{request.formState.errors.root.message}</Notice>}
          <Field label="E-mail" icon={Mail} type="email" autoComplete="email" placeholder="seu.email@exemplo.com" hint="Enviaremos as instruções de recuperação para este endereço." error={request.formState.errors.email?.message} {...request.register("email", emailRules)} />
          <Button disabled={request.formState.isSubmitting} type="submit" variant="contained">Enviar código</Button>
        </Stack>)}
      <Link to="/entrar">Voltar para entrar</Link>
    </AuthCard>;
}
