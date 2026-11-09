import { useEffect, useState } from "react";
import {
  AlertTriangle,
  ArrowRight,
  ChevronDown,
  KeyRound,
  Lock,
  Mail,
  Save,
  ShieldAlert,
  Trash2,
  User,
  X,
} from "lucide-react";
import { useForm } from "react-hook-form";
import { Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field, newPasswordRules } from "../components/FormFields.jsx";
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
    return { error: apiError(error, "Não foi possível salvar. Confira a senha atual e tente novamente.") };
  }
}

//dose diaria de depressão
export default function Perfil() {
  const user = useRouteLoaderData("app");

  const [activeTab, setActiveTab] = useState("nome");

  const [nameSuccess, setNameSuccess] = useState("");
  const [nameError, setNameError] = useState("");
  const [isUpdatingName, setIsUpdatingName] = useState(false);

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

  const nameForm = useForm({
    defaultValues: { name: user.name, currentPassword: "" },
  });

  const passwordForm = useForm({
    defaultValues: { currentPassword: "", newPassword: "", confirmPassword: "" },
  });

  const toggleTab = (tabName) => {
    setActiveTab((current) => (current === tabName ? null : tabName));
  };

  const handleNameSubmit = nameForm.handleSubmit(async (fields) => {
    setNameSuccess("");
    setNameError("");
    setIsUpdatingName(true);
    try {
      await updateUserProfile(user.id, {
        name: fields.name.trim(),
        email: user.email,
        currentPassword: fields.currentPassword,
      });
      setNameSuccess("Nome atualizado com sucesso!");
      nameForm.setValue("currentPassword", "");
    } catch (error) {
      setNameError(apiError(error, "Não foi possível atualizar o nome. Confira sua senha atual."));
    } finally {
      setIsUpdatingName(false);
    }
  });

  const handlePasswordSubmit = passwordForm.handleSubmit(async (fields) => {
    setPasswordSuccess("");
    setPasswordError("");
    setIsUpdatingPassword(true);
    try {
      await updatePassword(fields.currentPassword, fields.newPassword);
      setPasswordSuccess("Senha atualizada com sucesso!");
      passwordForm.reset();
    } catch (error) {
      setPasswordError(apiError(error, "Não foi possível atualizar a senha. Confira sua senha atual."));
    } finally {
      setIsUpdatingPassword(false);
    }
  });

  const handleRequestRecoveryEmail = async () => {
    setRecoveryStatus("");
    setIsSendingRecovery(true);
    try {
      await forgotPassword(user.email);
      setRecoveryStatus(`Enviamos instruções de recuperação para ${user.email}.`);
    } catch (error) {
      setRecoveryStatus(apiError(error, "Não foi possível enviar o link de recuperação."));
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

      }
      window.location.href = "/entrar?contaExcluida=true";
    } catch (error) {
      setDeleteError(apiError(error, "Senha incorreta ou erro ao desativar conta."));
      setIsDeleting(false);
    }
  };

  return (
    <div className="mx-auto max-w-4xl space-y-5">
      <header>
        <h1 className="text-2xl font-bold">Meu perfil</h1>
        <p className="mt-1 text-sm text-muted">Gerencie suas informações pessoais e credenciais de segurança.</p>
      </header>

      <section className="panel p-5 shadow-xl">
        <h2 className="text-lg font-bold break-words">{user.name}</h2>
        <p className="text-sm break-words text-muted">{user.email}</p>
        <p className="mt-2 text-xs font-semibold text-amber-400">{roleNames[user.role]}</p>
      </section>

      <div className="panel overflow-hidden border border-line shadow-xl">
        <button
          type="button"
          onClick={() => toggleTab("nome")}
          className="flex w-full items-center justify-between p-5 text-left transition-colors hover:bg-white/[0.02]"
        >
          <div className="flex items-center gap-3">
            <div className="grid size-9 place-items-center rounded-lg bg-amber-500/10 text-amber-400">
              <User size={18} />
            </div>
            <div>
              <h2 className="font-semibold text-zinc-100 text-base">Trocar Nome</h2>
              <p className="text-xs text-muted">Atualize seu nome de exibição na plataforma</p>
            </div>
          </div>
          <ChevronDown
            size={18}
            className={`text-muted transition-transform duration-200 ${activeTab === "nome" ? "rotate-180" : ""}`}
          />
        </button>

        {activeTab === "nome" && (
          <div className="border-t border-line p-5 sm:p-6 space-y-4 animate-in fade-in duration-150">
            {nameSuccess && <Notice>{nameSuccess}</Notice>}
            {nameError && <Notice error>{nameError}</Notice>}

            <form onSubmit={handleNameSubmit} className="space-y-4" noValidate>
              <div className="grid gap-4 sm:grid-cols-2">
                <Field
                  label="Nome completo"
                  icon={User}
                  autoComplete="name"
                  maxLength={100}
                  error={nameForm.formState.errors.name?.message}
                  {...nameForm.register("name", {
                    setValueAs: (value) => value?.trim() ?? "",
                    required: "Informe seu nome.",
                    minLength: { value: 2, message: "Mínimo de 2 caracteres." },
                  })}
                />
                <Field
                  label="E-mail"
                  icon={Mail}
                  type="email"
                  value={user.email}
                  disabled
                  readOnly
                  hint="O e-mail não pode ser alterado."
                />
              </div>

              <Field
                label="Senha atual"
                icon={Lock}
                type="password"
                autoComplete="current-password"
                error={nameForm.formState.errors.currentPassword?.message}
                hint="Digite sua senha atual para confirmar a troca do nome."
                {...nameForm.register("currentPassword", { required: "Informe sua senha atual." })}
              />

              <button className="button-primary" disabled={isUpdatingName}>
                <Save size={16} /> {isUpdatingName ? "Salvando…" : "Salvar Nome"}
              </button>
            </form>
          </div>
        )}
      </div>

      <div className="panel overflow-hidden border border-line shadow-xl">
        <button
          type="button"
          onClick={() => toggleTab("senha")}
          className="flex w-full items-center justify-between p-5 text-left transition-colors hover:bg-white/[0.02]"
        >
          <div className="flex items-center gap-3">
            <div className="grid size-9 place-items-center rounded-lg bg-sky-500/10 text-sky-400">
              <KeyRound size={18} />
            </div>
            <div>
              <h2 className="font-semibold text-zinc-100 text-base">Trocar Senha</h2>
              <p className="text-xs text-muted">Altere sua senha de acesso e segurança</p>
            </div>
          </div>
          <ChevronDown
            size={18}
            className={`text-muted transition-transform duration-200 ${activeTab === "senha" ? "rotate-180" : ""}`}
          />
        </button>

        {activeTab === "senha" && (
          <div className="border-t border-line p-5 sm:p-6 space-y-4 animate-in fade-in duration-150">
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

              <div className="grid gap-4 sm:grid-cols-2">
                <Field
                  label="Nova senha"
                  icon={Lock}
                  type="password"
                  autoComplete="new-password"
                  error={passwordForm.formState.errors.newPassword?.message}
                  hint="Mínimo de 8 caracteres."
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
              </div>

              <div className="flex flex-wrap items-center justify-between gap-3 pt-1">
                <button className="button-primary" disabled={isUpdatingPassword}>
                  <Save size={16} /> {isUpdatingPassword ? "Atualizando…" : "Salvar Nova Senha"}
                </button>

                <button
                  type="button"
                  onClick={handleRequestRecoveryEmail}
                  disabled={isSendingRecovery}
                  className="text-xs text-brand hover:underline font-medium inline-flex items-center gap-1.5"
                >
                  <Mail size={13} /> {isSendingRecovery ? "Enviando…" : "Esqueceu a senha atual?"}
                </button>
              </div>

              {recoveryStatus && (
                <div className="pt-2">
                  <Notice>{recoveryStatus}</Notice>
                  <div className="mt-1">
                    <Link to="/recuperar-senha" className="inline-flex items-center gap-1 text-xs font-semibold text-brand hover:underline">
                      Ir para redefinição com código <ArrowRight size={13} />
                    </Link>
                  </div>
                </div>
              )}
            </form>
          </div>
        )}
      </div>

      <div className="panel overflow-hidden border border-red-500/30 bg-red-500/5 shadow-xl">
        <button
          type="button"
          onClick={() => toggleTab("exclusao")}
          className="flex w-full items-center justify-between p-5 text-left transition-colors hover:bg-red-500/10"
        >
          <div className="flex items-center gap-3">
            <div className="grid size-9 place-items-center rounded-lg bg-red-500/20 text-rose-400">
              <AlertTriangle size={18} />
            </div>
            <div>
              <h2 className="font-semibold text-rose-400 text-base">Exclusão de Conta</h2>
              <p className="text-xs text-rose-300/70">Desativar sua conta e revogar acessos definitivamente</p>
            </div>
          </div>
          <ChevronDown
            size={18}
            className={`text-rose-400 transition-transform duration-200 ${activeTab === "exclusao" ? "rotate-180" : ""}`}
          />
        </button>

        {activeTab === "exclusao" && (
          <div className="border-t border-red-500/20 p-5 sm:p-6 space-y-4 animate-in fade-in duration-150">
            <p className="text-xs text-zinc-300 leading-relaxed">
              Ao desativar sua conta, suas sessões ativas serão encerradas e suas matrículas serão desativadas.
            </p>
            <div>
              <button
                type="button"
                onClick={handleOpenDeleteModal}
                className="button-danger text-xs !min-h-9"
              >
                <Trash2 size={15} /> Excluir minha conta
              </button>
            </div>
          </div>
        )}
      </div>

      <footer className="pt-2 text-center text-xs text-muted flex items-center justify-center gap-3">
        <Link to="/termos-de-uso" className="hover:text-brand transition-colors">Termos de Uso</Link>
        <span>•</span>
        <Link to="/politica-de-privacidade" className="hover:text-brand transition-colors">Política de Privacidade (LGPD)</Link>
      </footer>

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
                <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-xs text-zinc-300 space-y-1.5 leading-relaxed">
                  <p className="font-bold text-rose-300">Consequências da exclusão:</p>
                  <ul className="list-disc list-inside space-y-1 text-zinc-300">
                    <li>Sua conta será desativada de forma definitiva.</li>
                    <li>Todas as sessões ativas serão desconectadas imediatamente.</li>
                    <li>Seu acesso aos cursos e videoaulas será revogado.</li>
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
                    Tenho plena ciência de que minha conta será desativada e meu acesso revogado.
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
                  Para confirmar que você é o titular da conta, digite sua <strong>senha atual</strong>:
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
                      {isDeleting ? "Excluindo…" : "Confirmar Exclusão"}
                    </button>
                  </div>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
