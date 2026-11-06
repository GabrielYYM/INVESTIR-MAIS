import { useState } from "react";
import { Lock, Mail, Save, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { useRevalidator, useRouteLoaderData } from "react-router";
import { Field } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { apiError } from "../services/api.js";
import { updateUserProfile } from "../services/authService.js";

export default function Perfil() {
  const user = useRouteLoaderData("app");
  const { revalidate } = useRevalidator();
  const [saved, setSaved] = useState(false);
  const form = useForm({ defaultValues: { name: user.name, currentPassword: "" } });
  const { errors, isSubmitting } = form.formState;

  const submit = form.handleSubmit(async ({ name, currentPassword }) => {
    setSaved(false);
    try {
      const updated = await updateUserProfile(user.id, { name, email: user.email, currentPassword });
      form.reset({ name: updated.name, currentPassword: "" });
      setSaved(true);
      await revalidate();
    } catch (error) {
      form.setError("root", { message: apiError(error, "Não foi possível salvar. Confira a senha atual e tente novamente.") });
      if (error.response?.status === 401) await revalidate();
    }
  });

  return (
    <div className="mx-auto max-w-4xl space-y-6">
      <header>
        <h1 className="text-2xl font-bold">Meu perfil</h1>
        <p className="mt-1 text-sm text-muted">Consulte seus dados e atualize seu nome de exibição.</p>
      </header>
      <section className="panel p-6 shadow-xl">
        <h2 className="text-lg font-bold break-words">{user.name}</h2>
        <p className="text-sm break-words text-muted">{user.email}</p>
        <p className="mt-2 text-xs font-semibold text-amber-400">{roleNames[user.role]}</p>
      </section>
      {saved && <Notice>Perfil atualizado.</Notice>}
      {errors.root && <Notice error>{errors.root.message}</Notice>}
      <form onSubmit={submit} className="panel space-y-6 p-6 shadow-xl sm:p-8" noValidate>
        <h2 className="border-b border-line pb-4 font-semibold">Alterar informações</h2>
        <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={errors.name?.message}
          {...form.register("name", { setValueAs: (value) => value.trim(), required: "Informe seu nome.", minLength: { value: 2, message: "Informe pelo menos 2 caracteres." } })} />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} readOnly hint="Somente leitura." />
        <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={errors.currentPassword?.message}
          {...form.register("currentPassword", { required: "Informe sua senha atual para confirmar." })} />
        <button className="button-primary" disabled={isSubmitting}><Save size={17} /> {isSubmitting ? "Salvando…" : "Salvar perfil"}</button>
      </form>
    </div>
  );
}
