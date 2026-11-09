import { useEffect } from "react";
import { Lock, Mail, Save, ShieldCheck, User } from "lucide-react";
import { useForm } from "react-hook-form";
import { data, Link, redirect, useFetcher, useRouteLoaderData } from "react-router";
import { Field } from "../components/FormFields.jsx";
import { Notice } from "../components/PageState.jsx";
import { roleNames } from "../components/Topbar.jsx";
import { apiError } from "../services/api.js";
import { getCurrentUser, updateUserProfile } from "../services/authService.js";

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

  useEffect(() => {
    if (updated && !isSubmitting) reset({ name: updated.name, currentPassword: "" });
  }, [isSubmitting, reset, updated]);

  const submit = form.handleSubmit((fields) => fetcher.submit({ ...fields, id: user.id, email: user.email }, {
    method: "post",
    encType: "application/json",
  }));

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
      {!isSubmitting && updated && <Notice>Perfil atualizado.</Notice>}
      {!isSubmitting && fetcher.data?.error && <Notice error>{fetcher.data.error}</Notice>}
      <form onSubmit={submit} className="panel space-y-6 p-6 shadow-xl sm:p-8" noValidate>
        <h2 className="border-b border-line pb-4 font-semibold">Alterar informações</h2>
        <Field label="Nome completo" icon={User} autoComplete="name" maxLength={100} error={errors.name?.message}
          {...form.register("name", { setValueAs: (value) => value.trim(), required: "Informe seu nome.", minLength: { value: 2, message: "Informe pelo menos 2 caracteres." } })} />
        <Field label="E-mail" icon={Mail} type="email" value={user.email} disabled readOnly hint="Somente leitura. O e-mail não pode ser alterado." />
        <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password" error={errors.currentPassword?.message}
          {...form.register("currentPassword", { required: "Informe sua senha atual para confirmar." })} />
        <button className="button-primary" disabled={isSubmitting}><Save size={17} /> {isSubmitting ? "Salvando…" : "Salvar perfil"}</button>
      </form>

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
            Para exercer seus direitos de titular ou solicitar exclusão definitiva de dados cadastrais, encaminhe uma requisição ao e-mail do Encarregado indicado acima.
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
    </div>
  );
}
