<<<<<<< HEAD
import { useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  KeyRound,
  Lock,
  Mail,
  Save,
  ShieldAlert,
  ShieldCheck,
  Trash2,
  User,
  X,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { data, Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field, newPasswordRules } from "../components/FormFields.jsx";
=======
import { useEffect } from "react";
import { Lock, Mail, Save, ShieldCheck, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { data, Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field } from "../components/FormFields.jsx";
>>>>>>> origin/dev
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { apiError } from "../services/api.js";
import {
  deleteAccount,
  forgotPassword,
  getCurrentUser,
  logout,
  updatePassword,
  updateUserProfile,
} from "../services/authService.js";

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
  const [passwordSuccess, setPasswordSuccess] = useState("");
  const [passwordError, setPasswordError] = useState("");
  const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);

  const [recoveryStatus, setRecoveryStatus] = useState("");
  const [isSendingRecovery, setIsSendingRecovery] = useState(false);

  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteStep, setDeleteStep] = useState(1);
  const [firstConsent, setFirstConsent] = useState(false);
  const [deletePassword, setDeletePassword] = useState("");
  const [deleteError, setDeleteError] = useState("");
  const [isDeleting, setIsDeleting] = useState(false);

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
    setPasswordSuccess("");
    setPasswordError("");
    setIsUpdatingPassword(true);
    try {
      await updatePassword(fields.currentPassword, fields.newPassword);
      setPasswordSuccess("Senha atualizada com sucesso!");
      passwordForm.reset();
    } catch (error) {
      setPasswordError(apiError(error, "Não foi possível atualizar a senha. Confira a senha atual e tente novamente."));
    } finally {
      setIsUpdatingPassword(false);
    }
  });

  const handleRequestRecoveryEmail = async () => {
    setRecoveryStatus("");
    setIsSendingRecovery(true);
    try {
      await forgotPassword(user.email);
      setRecoveryStatus(`Enviamos instruções e código de recuperação para ${user.email}.`);
    } catch (error) {
      setRecoveryStatus(apiError(error, "Não foi possível solicitar o código de recuperação no momento."));
    } finally {
      setIsSendingRecovery(false);
    }
  };

  const handleOpenDeleteModal = () => {
    setShowDeleteModal(true);
    setDeleteStep(1);
    setFirstConsent(false);
    setDeletePassword("");
    setDeleteError("");
  };

  const handleConfirmDelete = async (e) => {
    e.preventDefault();
    if (!deletePassword.trim()) {
      setDeleteError("Informe sua senha atual para autorizar a exclusão.");
      return;
    }
    setIsDeleting(true);
    setDeleteError("");
    try {
      await deleteAccount(user.id, deletePassword);
      try {
        await logout();
      } catch {
        // Se a sessão já foi revogada no backend, prossegue
      }
      window.location.href = "/entrar?contaExcluida=true";
    } catch (error) {
      setDeleteError(apiError(error, "Senha incorreta ou não foi possível excluir a conta. Tente novamente."));
      setIsDeleting(false);
    }
  };

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
<<<<<<< HEAD
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

        {passwordSuccess && <Notice>{passwordSuccess}</Notice>}
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
              disabled={isSendingRecovery}
              className="button-secondary text-xs !min-h-8 !px-3"
            >
              <Mail size={14} /> {isSendingRecovery ? "Enviando…" : "Enviar link de recuperação"}
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

=======
        <h2 className="border-b border-line pb-4 font-semibold">Alterar informações</h2>
        <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={errors.name?.message}
          {...form.register("name", { setValueAs: (value) => value.trim(), required: "Informe seu nome.", minLength: { value: 2, message: "Informe pelo menos 2 caracteres." } })} />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} disabled readOnly hint="Somente leitura. O e-mail não pode ser alterado." />
        <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={errors.currentPassword?.message}
          {...form.register("currentPassword", { required: "Informe sua senha atual para confirmar." })} />
        <button className="button-primary" disabled={isSubmitting}><Save size={17} /> {isSubmitting ? "Salvando…" : "Salvar perfil"}</button>
      </form>

>>>>>>> origin/dev
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
<<<<<<< HEAD
            Para exercer seus direitos de titular ou tirar dúvidas sobre o tratamento de seus dados, contate o Encarregado pelo e-mail acima.
=======
            Para exercer seus direitos de titular ou solicitar exclusão definitiva de dados cadastrais, encaminhe uma requisição ao e-mail do Encarregado indicado acima.
>>>>>>> origin/dev
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
<<<<<<< HEAD

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
            onClick={handleOpenDeleteModal}
            className="button-danger"
          >
            <Trash2 size={16} /> Excluir minha conta
          </button>
        </div>
      </section>

      {showDeleteModal && (
        <div
          role="dialog"
          aria-modal="true"
          aria-labelledby="modal-exclusao-titulo"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-xs animate-in fade-in duration-200"
        >
          <div className="panel w-full max-w-lg border-red-500/30 bg-[#151320] p-6 shadow-2xl space-y-5">
            <div className="flex items-start justify-between gap-3 border-b border-white/10 pb-4">
              <div className="flex items-center gap-2.5 text-rose-400">
                <ShieldAlert size={22} className="shrink-0" />
                <h3 id="modal-exclusao-titulo" className="font-bold text-lg text-zinc-100">
                  {deleteStep === 1 ? "Excluir Conta — Confirmação (1 de 2)" : "Excluir Conta — Autenticação (2 de 2)"}
                </h3>
              </div>
              <button
                type="button"
                onClick={() => setShowDeleteModal(false)}
                className="text-muted hover:text-zinc-100 transition-colors p-1"
                aria-label="Fechar modal"
                disabled={isDeleting}
              >
                <X size={20} />
              </button>
            </div>

            {deleteError && <Notice error>{deleteError}</Notice>}

            {deleteStep === 1 && (
              <div className="space-y-4">
                <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-xs text-zinc-300 space-y-2 leading-relaxed">
                  <p className="font-bold text-rose-300">Atenção às consequências da exclusão:</p>
                  <ul className="list-disc list-inside space-y-1">
                    <li>Sua conta será desativada de forma definitiva</li>
                    <li>Todas as sessões abertas em qualquer dispositivo serão desconectadas na hora.</li>
                    <li>Seu progresso em cursos, matrículas e certificados serão tornados inacessíveis.</li>
                    <li>Você não poderá mais utilizar este e-mail para acessar a conta atual.</li>
                  </ul>
                </div>

                <label className="flex items-start gap-3 p-3 rounded-xl border border-line bg-canvas cursor-pointer hover:border-zinc-500 transition-colors">
                  <input
                    type="checkbox"
                    checked={firstConsent}
                    onChange={(e) => setFirstConsent(e.target.checked)}
                    className="mt-0.5 size-4 accent-amber-500"
                  />
                  <span className="text-xs text-zinc-200 select-none">
                    <strong>1ª Confirmação:</strong> Li com atenção e tenho plena ciência de que minha conta será desativada e todo o meu acesso à plataforma será revogado.
                  </span>
                </label>

                <div className="flex items-center justify-end gap-3 pt-2 border-t border-white/10">
                  <button
                    type="button"
                    onClick={() => setShowDeleteModal(false)}
                    className="button-secondary text-xs"
                  >
                    Cancelar
                  </button>
                  <button
                    type="button"
                    disabled={!firstConsent}
                    onClick={() => setDeleteStep(2)}
                    className="button-danger text-xs disabled:opacity-40"
                  >
                    Prosseguir para confirmação com senha <ArrowRight size={14} />
                  </button>
                </div>
              </div>
            )}

            {deleteStep === 2 && (
              <form onSubmit={handleConfirmDelete} className="space-y-4">
                <p className="text-xs text-zinc-300 leading-relaxed">
                  <strong>2ª Confirmação:</strong> Para garantir que você é realmente o titular da conta e evitar exclusões acidentais ou não autorizadas, digite a sua <strong>senha atual</strong>:
                </p>

                <div className="space-y-1.5">
                  <label htmlFor="deletePasswordInput" className="block text-xs font-medium text-muted">
                    Senha atual da conta
                  </label>
                  <input
                    id="deletePasswordInput"
                    type="password"
                    autoFocus
                    required
                    value={deletePassword}
                    onChange={(e) => setDeletePassword(e.target.value)}
                    placeholder="Sua senha atual"
                    className="field text-sm"
                    autoComplete="current-password"
                  />
                </div>

                <div className="flex items-center justify-between gap-3 pt-3 border-t border-white/10">
                  <button
                    type="button"
                    onClick={() => setDeleteStep(1)}
                    disabled={isDeleting}
                    className="button-secondary text-xs"
                  >
                    Voltar
                  </button>
                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => setShowDeleteModal(false)}
                      disabled={isDeleting}
                      className="button-secondary text-xs"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={isDeleting || !deletePassword}
                      className="button-danger text-xs disabled:opacity-40"
                    >
                      <Trash2 size={14} />
                      {isDeleting ? "Excluindo conta…" : "Confirmar e Desativar Conta"}
                    </button>
                  </div>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
=======
>>>>>>> origin/dev
    </div>
  );
}
