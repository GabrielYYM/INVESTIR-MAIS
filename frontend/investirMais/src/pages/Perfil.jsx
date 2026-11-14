import { Button, Paper, Stack, Typography } from "@mui/material";
import { lazy, Suspense, useEffect, useState } from "react";
import { AlertTriangle, ArrowRight, KeyRound, Lock, Mail, Save, ShieldCheck, Trash2, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { data, Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field, newPasswordRules } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../services/dataProvider.js";
import { apiError } from "../services/api.js";
import { forgotPassword, getCurrentUser, updatePassword, updateUserProfile } from "../services/authService.js";
const DeleteAccountDialog = lazy(() => import("../components/DeleteAccountDialog.jsx"));
export async function profileAction({
  request
}) {
  const {
    id,
    name,
    email,
    currentPassword
  } = await request.json();
  try {
    return {
      updated: await updateUserProfile(id, {
        name,
        email,
        currentPassword
      })
    };
  } catch (error) {
    const status = error.response?.status ?? 500;
    if (status === 401) {
      try {
        await getCurrentUser(request.signal);
      } catch (sessionError) {
        if ([401, 403].includes(sessionError.response?.status)) return redirect("/entrar?retorno=%2Fperfil");
      }
    }
    return data({
      error: apiError(error, "Não foi possível salvar. Confira a senha atual e tente novamente.")
    }, {
      status
    });
  }
}
export default function Perfil() {
  const user = useRouteLoaderData("app");
  const fetcher = useFetcher();
  const form = useForm({
    defaultValues: {
      name: user.name,
      currentPassword: ""
    }
  });
  const {
    errors
  } = form.formState;
  const {
    reset
  } = form;
  const isSubmitting = fetcher.state !== "idle";
  const updated = fetcher.data?.updated;
  const passwordForm = useForm({
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: ""
    }
  });
  const recovery = useForm();
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const isUpdatingPassword = passwordForm.formState.isSubmitting;
  const passwordError = passwordForm.formState.errors.root?.message;
  const recoveryStatus = recovery.formState.errors.root?.message || (recovery.formState.isSubmitSuccessful ? `Enviamos instruções e código de recuperação para ${user.email}.` : "");
  useEffect(() => {
    if (updated && !isSubmitting) reset({
      name: updated.name,
      currentPassword: ""
    });
  }, [isSubmitting, reset, updated]);
  const submit = form.handleSubmit(fields => fetcher.submit({
    ...fields,
    id: user.id,
    email: user.email
  }, {
    method: "post",
    encType: "application/json"
  }));
  const handlePasswordSubmit = passwordForm.handleSubmit(async fields => {
    try {
      await updatePassword(fields.currentPassword, fields.newPassword);
      passwordForm.reset();
    } catch (error) {
      passwordForm.setError("root", {
        message: apiError(error, "Não foi possível atualizar a senha. Confira a senha atual e tente novamente.")
      });
    }
  });
  const handleRequestRecoveryEmail = recovery.handleSubmit(async () => {
    try {
      await forgotPassword(user.email);
    } catch (error) {
      recovery.setError("root", {
        message: apiError(error, "Não foi possível solicitar o código de recuperação no momento.")
      });
    }
  });
  return <Stack spacing={2}>
      <Stack spacing={2} component="header">
        <Typography variant="h1" component="h1">Meu perfil</Typography>
        <Typography>Consulte seus dados, gerencie suas credenciais e configurações de segurança.</Typography>
      </Stack>

      <Paper component="section" variant="outlined" sx={{
      p: 3
    }}>
        <Typography variant="h2" component="h2">{user.name}</Typography>
        <Typography>{user.email}</Typography>
        <Typography>{roleNames[user.role]}</Typography>
      </Paper>

      {!isSubmitting && updated && <Notice>Perfil atualizado.</Notice>}
      {!isSubmitting && fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
      <Stack onSubmit={submit} noValidate spacing={2} component="form">
        <Typography variant="h2" component="h2">Alterar informações</Typography>
        <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={errors.name?.message} {...form.register("name", {
        setValueAs: value => value.trim(),
        required: "Informe seu nome.",
        minLength: {
          value: 2,
          message: "Informe pelo menos 2 caracteres."
        }
      })} />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} disabled readOnly hint="Somente leitura. O e-mail não pode ser alterado diretamente." />
        <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={errors.currentPassword?.message} hint="Necessária para confirmar a atualização do seu nome." {...form.register("currentPassword", {
        required: "Informe sua senha atual para confirmar."
      })} />
        <Button disabled={isSubmitting} type="submit" variant="contained">
          <Save size={17} /> {isSubmitting ? "Salvando…" : "Salvar perfil"}
        </Button>
      </Stack>

      <Paper component="section" variant="outlined" sx={{
      p: 3
    }}>
        <Stack spacing={2}>
          <KeyRound />
          <Typography variant="h2" component="h2">Segurança e Senha</Typography>
        </Stack>

        {passwordForm.formState.isSubmitSuccessful && <Notice>Senha atualizada com sucesso!</Notice>}
        {passwordError && <Notice error>{passwordError}</Notice>}

        <Stack onSubmit={handlePasswordSubmit} noValidate spacing={2} component="form">
          <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={passwordForm.formState.errors.currentPassword?.message} {...passwordForm.register("currentPassword", {
          required: "Informe a senha atual."
        })} />

          <Field label="Nova senha" icon={Lock} type="password" autoComplete="new-password" error={passwordForm.formState.errors.newPassword?.message} hint="A senha deve ter no mínimo 8 caracteres." {...passwordForm.register("newPassword", newPasswordRules)} />

          <Field label="Confirme a nova senha" icon={Lock} type="password" autoComplete="new-password" error={passwordForm.formState.errors.confirmPassword?.message} {...passwordForm.register("confirmPassword", {
          required: "Confirme a nova senha.",
          validate: val => val === passwordForm.getValues("newPassword") || "As senhas não coincidem."
        })} />

          <Button disabled={isUpdatingPassword} type="submit" variant="contained">
            <Save size={17} /> {isUpdatingPassword ? "Atualizando senha…" : "Atualizar senha"}
          </Button>
        </Stack>

        <Stack spacing={2}>
          <Stack spacing={2}>
            <Stack spacing={2}>
              <Typography>Esqueceu sua senha atual?</Typography>
              <Typography>
                Você pode solicitar um link seguro com código de recuperação para o seu e-mail cadastrado ({user.email}).
              </Typography>
            </Stack>
            <Button type="button" onClick={handleRequestRecoveryEmail} disabled={recovery.formState.isSubmitting} variant="outlined">
              <Mail size={14} /> {recovery.formState.isSubmitting ? "Enviando…" : "Enviar link de recuperação"}
            </Button>
          </Stack>
          {recoveryStatus && <Stack spacing={2}>
              <Notice>{recoveryStatus}</Notice>
              <Stack spacing={2}>
                <Link to="/recuperar-senha">
                  Já recebeu o código? Ir para redefinição de senha <ArrowRight size={13} />
                </Link>
              </Stack>
            </Stack>}
        </Stack>
      </Paper>

      <Paper component="section" variant="outlined" sx={{
      p: 3
    }}>
        <Stack spacing={2}>
          <ShieldCheck />
          <Typography variant="h2" component="h2">Privacidade e Seus Dados (LGPD)</Typography>
        </Stack>
        <Typography>
          O Investir Mais trata seus dados em conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018).
          Você tem direito a solicitar confirmação de tratamento, acesso, retificação, portabilidade ou eliminação de seus dados pessoais.
        </Typography>
        <Stack spacing={2}>
          <Typography>
            <strong>Encarregado pelo Tratamento de Dados Pessoais (DPO):</strong>{" "}
            <a href="mailto:gabrielyyminato@gmail.com">
              gabrielyyminato@gmail.com
            </a>
          </Typography>
          <Typography>
            Para exercer seus direitos de titular ou tirar dúvidas sobre o tratamento de seus dados, contate o Encarregado pelo e-mail acima.
          </Typography>
        </Stack>
        <Stack spacing={2}>
          <Link to="/termos-de-uso">
            Termos de Uso
          </Link>
          <span>•</span>
          <Link to="/politica-de-privacidade">
            Política de Privacidade
          </Link>
        </Stack>
      </Paper>

      <Paper aria-labelledby="zona-perigo-titulo" component="section" variant="outlined" sx={{
      p: 3
    }}>
        <Stack spacing={2}>
          <AlertTriangle />
          <Typography id="zona-perigo-titulo" variant="h2" component="h2">Zona de Perigo — Exclusão de Conta</Typography>
        </Stack>
        <Typography>
          Ao solicitar a exclusão, sua conta será desativada de forma lógica. 
          O seu acesso imediato à plataforma será revogado, todas as suas sessões ativas serão encerradas e suas matrículas serão desativadas. 
          Por motivos de segurança e integridade, esta operação exige <strong>dupla confirmação</strong> e autenticação com sua senha atual.
        </Typography>
        <Stack spacing={2}>
          <Button type="button" onClick={() => setShowDeleteModal(true)} variant="outlined" color="error">
            <Trash2 size={16} /> Excluir minha conta
          </Button>
        </Stack>
      </Paper>

      {showDeleteModal && <Suspense fallback={null}><DeleteAccountDialog user={user} onClose={() => setShowDeleteModal(false)} /></Suspense>}

    </Stack>;
}
