import { Lock } from "lucide-react";
import { Field } from "../ui/FormFields.jsx";

export default function PasswordFields({ form, name = "password", creating = false }) {
  const { errors } = form.formState;
  return <>
    <Field label={creating ? "Senha" : "Nova senha"} icon={Lock} type="password" autoComplete="new-password"
      placeholder={creating ? "Crie uma senha (mínimo 8 caracteres)" : "Digite a nova senha (mínimo 8 caracteres)"}
      hint="A senha deve ter no mínimo 8 caracteres." error={errors[name]?.message} {...form.register(name)} />
    <Field label={creating ? "Confirme a senha" : "Confirme a nova senha"} icon={Lock} type="password" autoComplete="new-password"
      placeholder={creating ? "Repita a senha criada acima" : "Repita a nova senha criada"}
      error={errors.confirmPassword?.message} {...form.register("confirmPassword")} />
  </>;
}
