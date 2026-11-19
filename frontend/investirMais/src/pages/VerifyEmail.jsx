import { CheckCircle2, MailCheck, ShieldAlert } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useSearchParams } from "react-router";
import { useResendVerification, useVerifyEmail } from "../generated/account.ts";
import ResendActionForm from "../components/account/ResendActionForm.jsx";
import { AuthCard, Field } from "../components/ui/FormFields.jsx";
import { accountResolver, emailSchema } from "../services/accountSchema.js";
import { Notice } from "../components/ui/PageState.jsx";
import SubmitButton from "../components/ui/SubmitButton.jsx";
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
        <ResendActionForm
          mutation={resend}
          errorMessage="Não foi possível reenviar o link agora. Tente novamente em instantes."
          onSubmit={form.handleSubmit(({ email }) => resend.mutate({ data: { email } }))}
        >
          <Field label="E-mail" type="email" placeholder="seu-email@dominio.com" error={form.formState.errors.email?.message} {...form.register("email")} />
        </ResendActionForm><Link to="/entrar" className="button-secondary w-full">Voltar para o login</Link>
      </div> : <div className="space-y-4">{!token && <Notice error>Nenhum código de confirmação foi fornecido no link.</Notice>}
        <SubmitButton type="button" loading={confirmation.isPending} disabled={!token} loadingText="Confirmando…" fullWidth onClick={() => confirmation.mutate({
        data: {
          token
        }
      })}>Confirmar e-mail</SubmitButton>
        <Link to="/entrar" className="button-secondary w-full">Voltar para entrar</Link>
      </div>}
  </AuthCard>;
}
