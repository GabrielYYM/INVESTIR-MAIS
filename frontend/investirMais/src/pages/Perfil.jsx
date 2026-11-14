import { lazy, Suspense, useState } from "react";
import { AlertTriangle, ArrowRight, KeyRound, Lock, Mail, Save, ShieldCheck, Trash2, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, useNavigate, useRevalidator, useRouteLoaderData } from "react-router";
import { useForgotPassword, useUpdatePassword, useUpdateProfile } from "../generated/account.ts";
import { Field } from "../components/ui/FormFields.jsx";
import PasswordFields from "../components/account/PasswordFields.jsx";
import { Notice } from "../components/ui/PageState.jsx";
import { roleNames } from "../services/dataProvider.js";
import { apiError } from "../services/api.js";
import { isSessionExpired } from "../services/authService.js";
import { accountResolver, passwordSchema, profileSchema } from "../services/accountSchema.js";
const DeleteAccountDialog = lazy(() => import("../components/account/DeleteAccountDialog.jsx"));
export default function Perfil() {
  const user = useRouteLoaderData("app");
  const navigate = useNavigate();
  const revalidator = useRevalidator();
  const form = useForm({
    resolver: accountResolver(profileSchema),
    defaultValues: {
      name: user.name,
      currentPassword: ""
    }
  });
  const passwordForm = useForm({
    resolver: accountResolver(passwordSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    }
  });
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const profile = useUpdateProfile({
    mutation: {
      onSuccess: async updated => {
        form.reset({
          name: updated.name,
          currentPassword: ""
        });
        await revalidator.revalidate();
      },
      onError: async error => {
        if (await isSessionExpired(error)) await navigate("/entrar?retorno=%2Fperfil", {
          replace: true
        });
      }
    }
  });
  const password = useUpdatePassword({
    mutation: {
      onSuccess: () => passwordForm.reset()
    }
  });
  const recovery = useForgotPassword();
  const savingProfile = profile.isPending || revalidator.state !== "idle";
  const passwordError = password.error && apiError(password.error, "Não foi possível atualizar a senha. Confira a senha atual e tente novamente.");
  const recoveryStatus = recovery.error ? apiError(recovery.error, "Não foi possível solicitar o código de recuperação no momento.") : recovery.isSuccess ? `Enviamos instruções e código de recuperação para ${user.email}.` : "";
  const submit = form.handleSubmit(data => profile.mutate({
    id: user.id,
    data: {
      ...data,
      email: user.email
    }
  }));
  const updatePassword = passwordForm.handleSubmit(({
    currentPassword,
    newPassword
  }) => password.mutate({
    data: {
      currentPassword,
      newPassword
    }
  }));
  return <div className="mx-auto max-w-4xl space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Meu perfil</h1>
        <p className="mt-1 text-sm text-muted">Consulte seus dados, gerencie suas credenciais e configurações de segurança.</p>
      </header>

      <section className="panel p-6 shadow-xl">
        <h2 className="text-lg font-bold break-words">{user.name}</h2>
        <p className="text-sm break-words text-muted">{user.email}</p>
        <p className="mt-2 text-xs font-semibold text-amber-400">{roleNames[user.role]}</p>
      </section>

      {profile.isSuccess && !savingProfile && <Notice>Perfil atualizado.</Notice>}
      {!savingProfile && profile.error && <Notice error>{apiError(profile.error, "Não foi possível salvar. Confira a senha atual e tente novamente.")}</Notice>}
      <form onSubmit={submit} className="panel space-y-6 p-6 shadow-xl sm:p-8" noValidate>
        <h2 className="border-b border-line pb-4 font-semibold text-lg">Alterar informações</h2>
        <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={form.formState.errors.name?.message} {...form.register("name")} />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} disabled readOnly hint="Somente leitura. O e-mail não pode ser alterado diretamente." />
        <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={form.formState.errors.currentPassword?.message} hint="Necessária para confirmar a atualização do seu nome." {...form.register("currentPassword")} />
        <button className="button-primary" disabled={savingProfile}>
          <Save size={17} /> {savingProfile ? "Salvando…" : "Salvar perfil"}
        </button>
      </form>

      <section className="panel space-y-6 p-6 shadow-xl sm:p-8">
        <div className="flex items-center gap-2.5 border-b border-line pb-4">
          <KeyRound className="text-brand size-5" />
          <h2 className="font-semibold text-lg">Segurança e Senha</h2>
        </div>

        {password.isSuccess && <Notice>Senha atualizada com sucesso!</Notice>}
        {passwordError && <Notice error>{passwordError}</Notice>}

        <form onSubmit={updatePassword} className="space-y-4" noValidate>
          <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={passwordForm.formState.errors.currentPassword?.message} {...passwordForm.register("currentPassword")} />

          <PasswordFields form={passwordForm} name="newPassword" />

          <button className="button-primary" disabled={password.isPending}>
            <Save size={17} /> {password.isPending ? "Atualizando senha…" : "Atualizar senha"}
          </button>
        </form>

        <div className="rounded-xl border border-line bg-canvas p-4 space-y-3">
          <div className="flex items-start justify-between gap-3 flex-wrap">
            <div>
              <p className="text-sm font-semibold text-zinc-200">Esqueceu sua senha atual?</p>
              <p className="text-xs text-muted mt-0.5">
                Você pode solicitar um link seguro com código de recuperação para o seu e-mail cadastrado ({user.email}).
              </p>
            </div>
            <button type="button" onClick={() => recovery.mutate({
            data: {
              email: user.email
            }
          })} disabled={recovery.isPending} className="button-secondary text-xs !min-h-8 !px-3">
              <Mail size={14} /> {recovery.isPending ? "Enviando…" : "Enviar link de recuperação"}
            </button>
          </div>
          {recoveryStatus && <div className="pt-2">
              <Notice>{recoveryStatus}</Notice>
              <div className="mt-2">
                <Link to="/recuperar-senha" className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand hover:underline">
                  Já recebeu o código? Ir para redefinição de senha <ArrowRight size={13} />
                </Link>
              </div>
            </div>}
        </div>
      </section>

      <section className="panel space-y-4 p-6 shadow-xl sm:p-8">
        <div className="flex items-center gap-2.5 border-b border-line pb-4">
          <ShieldCheck className="text-brand size-5" />
          <h2 className="font-semibold text-lg">Privacidade e Seus Dados (LGPD)</h2>
        </div>
        <p className="text-sm text-muted leading-relaxed">
          O Investir Mais trata seus dados em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018).
          Você tem direito a solicitar confirmação de tratamento, acesso, retificação, portabilidade ou eliminação de seus dados pessoais.
        </p>
        <div className="rounded-xl border border-line bg-raised/30 p-4 text-xs text-muted space-y-2">
          <p>
            <strong className="text-zinc-200">Encarregado pelo Tratamento de Dados Pessoais (DPO):</strong>{" "}
            <a href="mailto:gabrielyyminato@gmail.com" className="text-brand hover:underline font-medium">
              gabrielyyminato@gmail.com
            </a>
          </p>
          <p>
            Para exercer seus direitos de titular ou tirar dúvidas sobre o tratamento de seus dados, contate o Encarregado pelo e-mail acima.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3 pt-1 text-xs">
          <Link to="/termos-de-uso" className="font-semibold text-brand hover:underline">
            Termos de Uso
          </Link>
          <span className="text-zinc-600">•</span>
          <Link to="/politica-de-privacidade" className="font-semibold text-brand hover:underline">
            Política de Privacidade
          </Link>
        </div>
      </section>

      <section className="panel border-red-500/20 bg-red-500/5 p-6 shadow-xl sm:p-8 space-y-4" aria-labelledby="zona-perigo-titulo">
        <div className="flex items-center gap-2.5 border-b border-red-500/20 pb-4 text-rose-400">
          <AlertTriangle className="size-5 shrink-0" />
          <h2 id="zona-perigo-titulo" className="font-bold text-lg">Zona de Perigo — Exclusão de Conta</h2>
        </div>
        <p className="text-sm text-zinc-300 leading-relaxed">
          Ao solicitar a exclusão, sua conta será desativada de forma lógica. 
          O seu acesso imediato à plataforma será revogado, todas as suas sessões ativas serão encerradas e suas matrículas serão desativadas. 
          Por motivos de segurança e integridade, esta operação exige <strong>dupla confirmação</strong> e autenticação com sua senha atual.
        </p>
        <div>
          <button type="button" onClick={() => setShowDeleteModal(true)} className="button-danger">
            <Trash2 size={16} /> Excluir minha conta
          </button>
        </div>
      </section>

      {showDeleteModal && <Suspense fallback={null}><DeleteAccountDialog user={user} onClose={() => setShowDeleteModal(false)} /></Suspense>}

    </div>;
}
