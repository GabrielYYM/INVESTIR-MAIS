import { useState } from "react";
import { useForm } from "react-hook-form";
import { ArrowRight, Trash2 } from "lucide-react";
import AppModal from "./AppModal.jsx";
import { Field } from "./FormFields.jsx";
import { Notice } from "./PageState.jsx";
import { deleteAccount, logout } from "../services/authService.js";
import { apiError } from "../services/api.js";

export default function DeleteAccountDialog({ user, onClose }) {
  const [step, setStep] = useState(1);
  const form = useForm({ defaultValues: { consent: false, password: "" } });
  const { errors, isSubmitting, isSubmitSuccessful } = form.formState;
  const busy = isSubmitting || isSubmitSuccessful;
  const submit = form.handleSubmit(async ({ consent, password }) => {
    if (!consent || step !== 2) return;
    try {
      await deleteAccount(user.id, password);
      await logout().catch(() => {}); // Account deletion already revokes its sessions.
      window.location.href = "/entrar?contaExcluida=true";
    } catch (error) {
      form.setError("root", { message: apiError(error, "Senha incorreta ou não foi possível excluir a conta. Tente novamente.") });
    }
  });
  return <AppModal title={step === 1 ? "Excluir Conta — Confirmação (1 de 2)" : "Excluir Conta — Autenticação (2 de 2)"}
    onClose={onClose} busy={busy} closeLabel="Fechar modal">
    <form onSubmit={submit} className="space-y-4 pt-4" noValidate>
      {(errors.root || errors.password) && <Notice error>{errors.root?.message || errors.password?.message}</Notice>}
      {step === 1 ? <>
        <div className="rounded-xl border border-red-500/20 bg-red-500/10 p-4 text-xs text-zinc-300 space-y-2 leading-relaxed">
          <p className="font-bold text-rose-300">Atenção às consequências da exclusão:</p>
          <ul className="list-disc list-inside space-y-1">
            <li>Sua conta será desativada de forma definitiva</li>
            <li>Todas as sessões abertas em qualquer dispositivo serão desconectadas na hora.</li>
            <li>Seu progresso em cursos, matrículas e certificados serão tornados inacessíveis.</li>
            <li>Você não poderá mais utilizar este e-mail para acessar a conta atual.</li>
          </ul>
        </div>
        <label className="flex items-start gap-3 p-3 rounded-xl border border-line bg-canvas cursor-pointer">
          <input type="checkbox" className="mt-0.5 size-4 accent-amber-500" {...form.register("consent")} />
          <span className="text-xs"><strong>1ª Confirmação:</strong> Li com atenção e tenho plena ciência de que minha conta será desativada e todo o meu acesso à plataforma será revogado.</span>
        </label>
        <div className="flex justify-end gap-3 border-t border-line pt-4">
          <button type="button" onClick={onClose} className="button-secondary">Cancelar</button>
          <button type="button" disabled={!form.watch("consent")} onClick={() => setStep(2)} className="button-danger">
            Prosseguir para confirmação com senha <ArrowRight size={14} />
          </button>
        </div>
      </> : <>
        <p className="text-sm text-muted"><strong>2ª Confirmação:</strong> Para garantir que você é realmente o titular da conta e evitar exclusões acidentais ou não autorizadas, digite a sua <strong>senha atual</strong>:</p>
        <Field label="Senha atual da conta" type="password" autoFocus autoComplete="current-password" disabled={busy}
          {...form.register("password", { validate: (value) => Boolean(value.trim()) || "Informe sua senha atual para autorizar a exclusão." })} />
        <div className="flex justify-between gap-3 border-t border-line pt-4">
          <button type="button" disabled={busy} onClick={() => setStep(1)} className="button-secondary">Voltar</button>
          <div className="flex gap-2">
            <button type="button" disabled={busy} onClick={onClose} className="button-secondary">Cancelar</button>
            <button disabled={busy || !form.watch("password")} className="button-danger"><Trash2 size={14} />
              {busy ? "Excluindo conta…" : "Confirmar e Desativar Conta"}
            </button>
          </div>
        </div>
      </>}
    </form>
  </AppModal>;
}
