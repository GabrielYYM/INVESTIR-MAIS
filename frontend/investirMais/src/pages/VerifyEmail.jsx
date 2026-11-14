import { Button, Stack } from "@mui/material";
import { CheckCircle2, MailCheck, ShieldAlert } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useResendVerification, useVerifyEmail } from "../generated/account.ts";
import { AuthCard, emailRules, Field } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { apiError } from "../services/api.js";
export default function VerifyEmail() {
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const token = search.get("token")?.trim() || "";
  const form = useForm();
  const confirmation = useVerifyEmail({ mutation: { retry: false, onSuccess: async result => { if(result.signedIn) await navigate("/",{replace:true}); } } });
  const resend = useResendVerification();
  const {isSuccess,isError} = confirmation;
  return <AuthCard icon={isSuccess?CheckCircle2:isError?ShieldAlert:MailCheck} title={isSuccess?"E-mail confirmado!":isError?"Falha na confirmação":"Confirme seu e-mail"}
    description={isSuccess?"Sua conta foi ativada com sucesso. Você já pode acessar a plataforma.":isError?"Não foi possível ativar sua conta com este link.":"Clique no botão abaixo para ativar sua conta no Investir Mais."}>
    {isSuccess ? <><Notice>Conta ativada com sucesso!</Notice><Button component={Link} to="/entrar">Ir para o login</Button></>
      : isError ? <Stack spacing={2}><Notice error>{apiError(confirmation.error,"O link de confirmação é inválido ou expirou.")}</Notice>
        <Stack component="form" spacing={2} onSubmit={form.handleSubmit(({email})=>resend.mutate({data:{email}}))}>
          <Field label="E-mail" type="email" placeholder="seu-email@dominio.com" error={form.formState.errors.email?.message} {...form.register("email",emailRules)} />
          {(resend.error || resend.isSuccess) && <Notice error={Boolean(resend.error)}>{resend.error?apiError(resend.error,"Não foi possível reenviar o link agora. Tente novamente em instantes."):resend.data?.message || "Se houver uma conta pendente para este e-mail, um novo link será enviado."}</Notice>}
          <Button type="submit" disabled={resend.isPending}>Reenviar link de confirmação</Button>
        </Stack><Button component={Link} to="/entrar">Voltar para o login</Button>
      </Stack> : <Stack spacing={2}>{!token && <Notice error>Nenhum código de confirmação foi fornecido no link.</Notice>}
        <Button variant="contained" disabled={confirmation.isPending || !token} onClick={()=>confirmation.mutate({data:{token}})}>{confirmation.isPending?"Confirmando…":"Confirmar e-mail"}</Button>
        <Button component={Link} to="/entrar">Voltar para entrar</Button>
      </Stack>}
  </AuthCard>;
}
