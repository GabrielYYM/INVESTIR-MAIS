import { Form, redirect, useActionData, useLoaderData, useNavigation } from "react-router";
import { getProfile, updatePassword, updateProfile } from "../generated/spring";
import { confirmPassword, requireUser, routeAction, routeLoader } from "../services/routing";
import { Field, Notice, PageHeading, PasswordField, Submit } from "../components/ui";

export const loader = routeLoader(async (args) => {
  const user = await requireUser(args);
  return getProfile(user.id, { signal: args.request.signal });
});
export const action = routeAction(async (form, args) => {
  const values = Object.fromEntries(form);
  if (values.intent === "password") confirmPassword(form);
  const user = await requireUser(args);
  if (values.intent === "password") {
    await updatePassword({ currentPassword: values.currentPassword, newPassword: values.newPassword }, { signal: args.request.signal });
    return redirect("/login?notice=password");
  }
  const updated = await updateProfile(user.id, { name: values.name, email: values.email, age: Number(values.age), currentPassword: values.currentPassword }, { signal: args.request.signal });
  if (updated.email !== user.email) return redirect("/login?notice=email");
  return { intent: "profile", message: "Perfil atualizado com sucesso." };
}, (form) => ({ intent: form.get("intent") }));

export function Component() {
  const profile = useLoaderData();
  const result = useActionData();
  const navigation = useNavigation();
  const pending = navigation.state !== "idle";
  return <>
    <PageHeading title="Meu perfil">Gerencie seus dados e a segurança da sua conta.</PageHeading>
    <div className="grid gap-6 lg:grid-cols-2">
      <Form method="post" replace className="panel grid content-start gap-5" key={`${profile.name}:${profile.email}:${profile.age}`}>
        <h2 className="text-lg font-semibold">Informações pessoais</h2>
        {result?.intent === "profile" && <Notice error={result.error}>{result.message}</Notice>}
        <fieldset disabled={pending} className="grid gap-4">
          <input type="hidden" name="intent" value="profile" />
          <Field label="Nome completo" name="name" minLength={2} maxLength={100} autoComplete="name" defaultValue={profile.name} />
          <Field label="E-mail" name="email" type="email" autoComplete="email" defaultValue={profile.email} />
          <Field label="Idade" name="age" type="number" min={0} max={120} step={1} defaultValue={profile.age} />
          <p className="text-xs text-zinc-400">Ao alterar o e-mail, será necessário confirmar o novo endereço e entrar novamente.</p>
          <PasswordField label="Senha atual para confirmar" name="currentPassword" autoComplete="current-password" minLength={1} />
          <Submit pending={pending} />
        </fieldset>
      </Form>
      <Form method="post" replace className="panel grid content-start gap-5">
        <h2 className="text-lg font-semibold">Alterar senha</h2>
        {result?.intent === "password" && <Notice error={result.error} />}
        <fieldset disabled={pending} className="grid gap-4">
          <input type="hidden" name="intent" value="password" />
          <PasswordField label="Senha atual" name="currentPassword" autoComplete="current-password" minLength={1} />
          <PasswordField label="Nova senha" name="newPassword" />
          <PasswordField label="Confirmar nova senha" name="confirmPassword" />
          <p className="text-xs text-zinc-400">A alteração encerra todas as suas sessões.</p>
          <Submit pending={pending}>Alterar senha</Submit>
        </fieldset>
      </Form>
    </div>
  </>;
}
