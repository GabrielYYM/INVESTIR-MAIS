import { useState } from "react";
import { useForm } from "react-hook-form";
import { ArrowRight, Trash2 } from "lucide-react";
import AppModal from "../ui/AppModal.jsx";
import { Field } from "../ui/FormFields.jsx";
import { Notice } from "../ui/PageState.jsx";
import SubmitButton from "../ui/SubmitButton.jsx";
import { deleteAccount, logout } from "../../services/authService.js";
import { apiError } from "../../services/api.js";
export default function DeleteAccountDialog({
  user,
  onClose
}) {
  const [step, setStep] = useState(1);
  const [immediately, setImmediately] = useState(false);
  const form = useForm({
    defaultValues: {
      consent: false,
      password: ""
    }
  });
  const {
    errors,
    isSubmitting,
    isSubmitSuccessful
  } = form.formState;
  const busy = isSubmitting || isSubmitSuccessful;
  const submit = form.handleSubmit(async ({
    consent,
    password
  }) => {
    if (!consent || step !== 2) return;
    try {
      await deleteAccount(user.id, password, immediately);
      await logout().catch(() => {});
      window.location.href = "/entrar?contaExcluida=true";
    } catch (error) {
      form.setError("root", {
        message: apiError(error, "Senha incorreta ou não foi possível excluir a conta. Tente novamente.")
      });
    }
  });
  return <AppModal title={step === 1 ? "Excluir Conta — Confirmação (1 de 2)" : "Excluir Conta — Autenticação (2 de 2)"} onClose={onClose} busy={busy} closeLabel="Fechar modal">
    <form onSubmit={submit} className="space-y-4 pt-4" noValidate>
      {(errors.root || errors.password) && <Notice error>{errors.root?.message || errors.password?.message}</Notice>}
      {step === 1 ? <>
        <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-xs text-zinc-300 space-y-2 leading-relaxed">
          <p className="font-bold text-rose-300">Atenção às consequências da exclusão:</p>
          <ul className="list-disc list-inside space-y-1">
            <li>{immediately ? "Seus dados e conteúdos no banco ativo serão eliminados definitivamente, sem recuperação." : "Sua conta ficará bloqueada, com dados preservados para recuperação por 30 dias. Depois desse prazo, a eliminação definitiva será processada automaticamente."}</li>
            <li>Todas as sessões abertas em qualquer dispositivo serão desconectadas na hora.</li>
            <li>Seu progresso em cursos, matrículas e certificados serão tornados inacessíveis.</li>
            <li>{immediately ? "O administrador não poderá restaurar esta conta." : "Para recuperar a conta dentro de 30 dias, solicite ao suporte: investirmaisinfo@gmail.com. O administrador verificará sua identidade antes de restaurar."}</li>
          </ul>
        </div>
        <fieldset className="space-y-2"><legend>Como deseja encerrar a conta?</legend><label className="flex gap-2"><input type="radio" name="deletion-mode" checked={!immediately} onChange={() => setImmediately(false)} />Desativar com recuperação por 30 dias</label><label className="flex gap-2"><input type="radio" name="deletion-mode" checked={immediately} onChange={() => setImmediately(true)} />Solicitar eliminação definitiva agora</label></fieldset>
        <p className="text-xs text-muted">A janela de 30 dias é uma política de recuperação, não um prazo obrigatório da LGPD. Registros sujeitos a obrigação legal específica podem ter guarda separada. Backups e serviços externos têm procedimentos próprios de eliminação.</p>
        <label className="flex items-start gap-3 p-3 rounded-xl border border-line bg-canvas cursor-pointer">
          <input type="checkbox" className="mt-0.5 size-4 accent-amber-500" {...form.register("consent")} />
          <span className="text-xs"><strong>1ª Confirmação:</strong> Li com atenção e tenho plena ciência de que minha conta será desativada e todo o meu acesso à plataforma será revogado.</span>
        </label>
        <div className="flex flex-wrap justify-end gap-3 border-t border-line pt-4">
          <button type="button" onClick={onClose} className="button-secondary">Cancelar</button>
          <button type="button" disabled={!form.watch("consent")} onClick={() => setStep(2)} className="button-danger">
            Prosseguir para confirmação com senha <ArrowRight size={14} />
          </button>
        </div>
      </> : <>
        <p className="text-sm text-muted"><strong>2ª Confirmação:</strong> Para garantir que você é realmente o titular da conta e evitar exclusões acidentais ou não autorizadas, digite a sua <strong>senha atual</strong>:</p>
        <Field label="Senha atual da conta" type="password" autoFocus autoComplete="current-password" disabled={busy} {...form.register("password", {
          validate: value => Boolean(value.trim()) || "Informe sua senha atual para autorizar a exclusão."
        })} />
        <div className="flex flex-wrap justify-between gap-3 border-t border-line pt-4">
          <button type="button" disabled={busy} onClick={() => setStep(1)} className="button-secondary">Voltar</button>
          <div className="flex gap-2">
            <button type="button" disabled={busy} onClick={onClose} className="button-secondary">Cancelar</button>
            <SubmitButton variant="danger" loading={busy} disabled={!form.watch("password")} loadingText="Excluindo conta…" icon={Trash2} iconSize={14}>
              {immediately ? "Confirmar eliminação definitiva" : "Confirmar e Desativar Conta"}
            </SubmitButton>
          </div>
        </div>
      </>}
    </form>
  </AppModal>;
}
