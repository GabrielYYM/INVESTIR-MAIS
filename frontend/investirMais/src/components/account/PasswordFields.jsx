import { Lock } from "lucide-react";
import { Field } from "../ui/FormFields.jsx";
import { passwordRequirements } from "../../services/accountSchema.js";

export default function PasswordFields({ form, name = "password", creating = false, onlyCurrent = false, hint }) {
  const { errors } = form.formState;
  return <>
    {(onlyCurrent || name === "newPassword") && <Field label="Senha atual" icon={Lock} type="password" autoComplete="current-password"
      placeholder="Digite sua senha" hint={hint}
      error={errors.currentPassword?.message} {...form.register("currentPassword")}
    />}

    {!onlyCurrent && <>
      <Field label={creating ? "Senha" : "Nova senha"} icon={Lock} type="password" autoComplete="new-password" maxLength={100}
        placeholder={creating ? "Crie uma senha (mínimo 8 caracteres)" : "Digite a nova senha (mínimo 8 caracteres)"}
        hint={passwordRequirements} error={errors[name]?.message} {...form.register(name)}
      />

      <Field label={creating ? "Confirme a senha" : "Confirme a nova senha"} icon={Lock} type="password" autoComplete="new-password" maxLength={100}
        placeholder={creating ? "Repita a senha criada acima" : "Repita a nova senha criada"}
        error={errors.confirmPassword?.message} {...form.register("confirmPassword")}
      />
    </>}
  </>;
}
