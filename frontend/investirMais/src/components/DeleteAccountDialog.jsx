import { Button, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { ArrowRight, Trash2 } from "lucide-react";
import AppModal from "./AppModal.jsx";
import { Field } from "./FormFields.jsx";
import { Notice } from "./PageState.jsx";
import { deleteAccount, logout } from "../services/authService.js";
import { apiError } from "../services/api.js";
export default function DeleteAccountDialog({
  user,
  onClose
}) {
  const [step, setStep] = useState(1);
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
      await deleteAccount(user.id, password);
      await logout().catch(() => {}); // Account deletion already revokes its sessions.
      window.location.href = "/entrar?contaExcluida=true";
    } catch (error) {
      form.setError("root", {
        message: apiError(error, "Senha incorreta ou não foi possível excluir a conta. Tente novamente.")
      });
    }
  });
  return <AppModal title={step === 1 ? "Excluir Conta — Confirmação (1 de 2)" : "Excluir Conta — Autenticação (2 de 2)"} onClose={onClose} busy={busy} closeLabel="Fechar modal">
    <Stack onSubmit={submit} noValidate spacing={2} component="form">
      {(errors.root || errors.password) && <Notice error>{errors.root?.message || errors.password?.message}</Notice>}
      {step === 1 ? <>
        <Stack spacing={2}>
          <Typography>Atenção às consequências da exclusão:</Typography>
          <ul>
            <li>Sua conta será desativada de forma definitiva</li>
            <li>Todas as sessões abertas em qualquer dispositivo serão desconectadas na hora.</li>
            <li>Seu progresso em cursos, matrículas e certificados serão tornados inacessíveis.</li>
            <li>Você não poderá mais utilizar este e-mail para acessar a conta atual.</li>
          </ul>
        </Stack>
        <label>
          <input type="checkbox" {...form.register("consent")} />
          <span><strong>1ª Confirmação:</strong> Li com atenção e tenho plena ciência de que minha conta será desativada e todo o meu acesso à plataforma será revogado.</span>
        </label>
        <Stack spacing={2}>
          <Button type="button" onClick={onClose} variant="outlined">Cancelar</Button>
          <Button type="button" disabled={!form.watch("consent")} onClick={() => setStep(2)} variant="outlined" color="error">
            Prosseguir para confirmação com senha <ArrowRight size={14} />
          </Button>
        </Stack>
      </> : <>
        <Typography><strong>2ª Confirmação:</strong> Para garantir que você é realmente o titular da conta e evitar exclusões acidentais ou não autorizadas, digite a sua <strong>senha atual</strong>:</Typography>
        <Field label="Senha atual da conta" type="password" autoFocus autoComplete="current-password" disabled={busy} {...form.register("password", {
          validate: value => Boolean(value.trim()) || "Informe sua senha atual para autorizar a exclusão."
        })} />
        <Stack spacing={2}>
          <Button type="button" disabled={busy} onClick={() => setStep(1)} variant="outlined">Voltar</Button>
          <Stack spacing={2}>
            <Button type="button" disabled={busy} onClick={onClose} variant="outlined">Cancelar</Button>
            <Button disabled={busy || !form.watch("password")} type="submit" variant="outlined" color="error"><Trash2 size={14} />
              {busy ? "Excluindo conta…" : "Confirmar e Desativar Conta"}
            </Button>
          </Stack>
        </Stack>
      </>}
    </Stack>
  </AppModal>;
}
