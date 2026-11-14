import { lazy, Suspense, useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowRight,
  KeyRound,
  Lock,
  Mail,
  Save,
  ShieldCheck,
  Trash2,
  User,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { data, Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field, newPasswordRules } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { apiError } from "../services/api.js";
import {
  forgotPassword,
  getCurrentUser,
  updatePassword,
  updateUserProfile,
} from "../services/authService.js";

const DeleteAccountDialog = lazy(() => import("../components/DeleteAccountDialog.jsx"));

export async function profileAction({ request }) {
  const { id, name, email, currentPassword } = await request.json();
  try {
    return { updated: await updateUserProfile(id, { name, email, currentPassword }) };
  } catch (error) {
    const status = error.response?.status ?? 500;
    if (status === 401) {
      try {
        await getCurrentUser(request.signal);
      } catch (sessionError) {
        if ([401, 403].includes(sessionError.response?.status)) return redirect("/entrar?retorno=%2Fperfil");
      }
    }
    return data({ error: apiError(error, "Não foi possível salvar. Confira a senha atual e tente novamente.") }, { status });
  }
}

export default function Perfil() {
  const user = useRouteLoaderData("app");
  const fetcher = useFetcher();

  const form = useForm({ defaultValues: { name: user.name, currentPassword: "" } });
  const { errors } = form.formState;
  const { reset } = form;
  const isSubmitting = fetcher.state !== "idle";
  const updated = fetcher.data?.updated;

  const passwordForm = useForm({ defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" } });
  const recovery = useForm();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const isUpdatingPassword = passwordForm.formState.isSubmitting;
  const passwordError = passwordForm.formState.errors.root?.message;
  const recoveryStatus = recovery.formState.errors.root?.message ||
    (recovery.formState.isSubmitSuccessful ? `Enviamos instruções e código de recuperação para ${user.email}.` : "");

  useEffect(() => {
    if (updated && !isSubmitting) reset({ name: updated.name, currentPassword: "" });
  }, [isSubmitting, reset, updated]);

  const submit = form.handleSubmit((fields) =>
    fetcher.submit(
      { ...fields, id: user.id, email: user.email },
      {
        method: "post",
        encType: "application/json",
      }
    )
  );

  const handlePasswordSubmit = passwordForm.handleSubmit(async (fields) => {
    try {
      await updatePassword(fields.currentPassword, fields.newPassword);
      passwordForm.reset();
    } catch (error) {
      passwordForm.setError("root", { message: apiError(error, "Não foi possível atualizar a senha. Confira a senha atual e tente novamente.") });
    }
  });

  const handleRequestRecoveryEmail = recovery.handleSubmit(async () => {
    try { await forgotPassword(user.email); }
    catch (error) { recovery.setError("root", { message: apiError(error, "Não foi possível solicitar o código de recuperação no momento.") }); }
  });

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Meu perfil</h1>
        <p className="mt-1 text-sm text-muted">Consulte seus dados, gerencie suas credenciais e configurações de segurança.</p>
      </header>

      <section className="panel p-6 shadow-xl">
        <h2 className="text-lg font-bold break-words">{user.name}</h2>
        <p className="text-sm break-words text-muted">{user.email}</p>
        <p className="mt-2 text-xs font-semibold text-amber-400">{roleNames[user.role]}</p>
      </section>

      {!isSubmitting && updated && <Notice>Perfil atualizado.</Notice>}
      {!isSubmitting && fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
      <form onSubmit={submit} className="panel space-y-6 p-6 shadow-xl sm:p-8" noValidate>
        <h2 className="border-b border-line pb-4 font-semibold text-lg">Alterar informações</h2>
        <Field
          label="Nome completo"
          icon={User}
          autoComplete="name"
          maxLength={100}
          error={errors.name?.message}
          {...form.register("name", {
            setValueAs: (value) => value.trim(),
            required: "Informe seu nome.",
            minLength: { value: 2, message: "Informe pelo menos 2 caracteres." },
          })}
        />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} disabled readOnly hint="Somente leitura. O e-mail não pode ser alterado diretamente." />
        <Field
          label="Senha atual"
          icon={Lock}
          type="password"
          autoComplete="current-password"
          error={errors.currentPassword?.message}
          hint="Necessária para confirmar a atualização do seu nome."
          {...form.register("currentPassword", { required: "Informe sua senha atual para confirmar." })}
        />
        <button className="button-primary" disabled={isSubmitting}>
          <Save size={17} /> {isSubmitting ? "Salvando…" : "Salvar perfil"}
        </button>
      </form>

      <section className="panel space-y-6 p-6 shadow-xl sm:p-8">
        <div className="flex items-center gap-2.5 border-b border-line pb-4">
          <KeyRound className="text-brand size-5" />
          <h2 className="font-semibold text-lg">Segurança e Senha</h2>
        </div>

        {passwordForm.formState.isSubmitSuccessful && <Notice>Senha atualizada com sucesso!</Notice>}
        {passwordError && <Notice error>{passwordError}</Notice>}

        <form onSubmit={handlePasswordSubmit} className="space-y-4" noValidate>
          <Field
            label="Senha atual"
            icon={Lock}
            type="password"
            autoComplete="current-password"
            error={passwordForm.formState.errors.currentPassword?.message}
            {...passwordForm.register("currentPassword", { required: "Informe a senha atual." })}
          />

          <Field
            label="Nova senha"
            icon={Lock}
            type="password"
            autoComplete="new-password"
            error={passwordForm.formState.errors.newPassword?.message}
            hint="A senha deve ter no mínimo 8 caracteres."
            {...passwordForm.register("newPassword", newPasswordRules)}
          />

          <Field
            label="Confirme a nova senha"
            icon={Lock}
            type="password"
            autoComplete="new-password"
            error={passwordForm.formState.errors.confirmPassword?.message}
            {...passwordForm.register("confirmPassword", {
              required: "Confirme a nova senha.",
              validate: (val) => val === passwordForm.getValues("newPassword") || "As senhas não coincidem.",
            })}
          />

          <button className="button-primary" disabled={isUpdatingPassword}>
            <Save size={17} /> {isUpdatingPassword ? "Atualizando senha…" : "Atualizar senha"}
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
            <button
              type="button"
              onClick={handleRequestRecoveryEmail}
              disabled={recovery.formState.isSubmitting}
              className="button-secondary text-xs !min-h-8 !px-3"
            >
              <Mail size={14} /> {recovery.formState.isSubmitting ? "Enviando…" : "Enviar link de recuperação"}
            </button>
          </div>
          {recoveryStatus && (
            <div className="pt-2">
              <Notice>{recoveryStatus}</Notice>
              <div className="mt-2">
                <Link to="/recuperar-senha" className="inline-flex items-center gap-1.5 text-xs font-semibold text-brand hover:underline">
                  Já recebeu o código? Ir para redefinição de senha <ArrowRight size={13} />
                </Link>
              </div>
            </div>
          )}
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
          <button
            type="button"
            onClick={() => setShowDeleteModal(true)}
            className="button-danger"
          >
            <Trash2 size={16} /> Excluir minha conta
          </button>
        </div>
      </section>

      {showDeleteModal && <Suspense fallback={null}><DeleteAccountDialog user={user} onClose={() => setShowDeleteModal(false)} /></Suspense>}

    </div>
  );
}
