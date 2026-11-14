import { CheckCircle2, MailCheck, ShieldAlert } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useResendVerification, useVerifyEmail } from "../generated/account.ts";
import { AuthCard, Field } from "../components/ui/FormFields.jsx";
import { accountResolver, emailSchema } from "../services/accountSchema.js";
import { Notice } from "../components/ui/PageState.jsx";
import { apiError } from "../services/api.js";
export default function VerifyEmail() {
  const navigate = useNavigate();
  const [search] = useSearchParams();
  const token = search.get("token")?.trim() || "";
  const form = useForm({
    resolver: accountResolver(emailSchema)
  });
  const confirmation = useVerifyEmail({
    mutation: {
      retry: false,
      onSuccess: async result => {
        if (result.signedIn) await navigate("/", {
          replace: true
        });
      }
    }
  });
  const resend = useResendVerification();
  const {
    isSuccess,
    isError
  } = confirmation;
  return <AuthCard icon={isSuccess ? CheckCircle2 : isError ? ShieldAlert : MailCheck} title={isSuccess ? "E-mail confirmado!" : isError ? "Falha na confirmação" : "Confirme seu e-mail"} description={isSuccess ? "Sua conta foi ativada com sucesso. Você já pode acessar a plataforma." : isError ? "Não foi possível ativar sua conta com este link." : "Clique no botão abaixo para ativar sua conta no Investir Mais."}>
    {isSuccess ? <><Notice>Conta ativada com sucesso!</Notice><Link to="/entrar" className="button-secondary w-full">Ir para o login</Link></> : isError ? <div className="space-y-4"><Notice error>{apiError(confirmation.error, "O link de confirmação é inválido ou expirou.")}</Notice>
        <form onSubmit={form.handleSubmit(({
        email
      }) => resend.mutate({
        data: {
          email
        }
      }))} className="space-y-4">
          <Field label="E-mail" type="email" placeholder="seu-email@dominio.com" error={form.formState.errors.email?.message} {...form.register("email")} />
          {(resend.error || resend.isSuccess) && <Notice error={Boolean(resend.error)}>{resend.error ? apiError(resend.error, "Não foi possível reenviar o link agora. Tente novamente em instantes.") : resend.data?.message || "Se houver uma conta pendente para este e-mail, um novo link será enviado."}</Notice>}
          <button type="submit" disabled={resend.isPending} className="button-secondary w-full">Reenviar link de confirmação</button>
        </form><Link to="/entrar" className="button-secondary w-full">Voltar para o login</Link>
      </div> : <div className="space-y-4">{!token && <Notice error>Nenhum código de confirmação foi fornecido no link.</Notice>}
        <button disabled={confirmation.isPending || !token} onClick={() => confirmation.mutate({
        data: {
          token
        }
      })} className="button-primary w-full" type="button">{confirmation.isPending ? "Confirmando…" : "Confirmar e-mail"}</button>
        <Link to="/entrar" className="button-secondary w-full">Voltar para entrar</Link>
      </div>}
  </AuthCard>;
}
