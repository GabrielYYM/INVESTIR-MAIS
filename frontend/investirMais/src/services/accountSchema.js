import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ForgotPasswordBody, RegisterUserBody, ResetPasswordBody, UpdatePasswordBody, UpdateProfileBody } from "../generated/account.zod.ts";

const trimmed = schema => z.string().trim().pipe(schema);
const password = schema => schema.refine(value => Boolean(value.trim()), "A senha não pode conter apenas espaços.");
export const passwordRequirements = "Use de 8 a 100 caracteres, com uma letra minúscula, uma maiúscula, um número e um caractere especial.";
const confirmed = (schema, field, required = "Confirme a senha.") => schema
  .extend({ confirmPassword: z.string().min(1, required) })
  .refine(values => values.confirmPassword === values[field], {
    path: ["confirmPassword"], message: "As senhas não coincidem.",
    when: ({ value }) => typeof value?.[field] === "string" && typeof value?.confirmPassword === "string",
  });

const birthDateSchema = z.iso.date().refine(value => {
  const today = new Date();
  const currentDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
  return value <= currentDate;
}, "A data de nascimento não pode estar no futuro.");

export const emailSchema = ForgotPasswordBody.extend({ email: trimmed(ForgotPasswordBody.shape.email) });
export const loginSchema = emailSchema.extend({ password: z.string().min(1, "Informe sua senha.") });
export const registrationSchema = confirmed(RegisterUserBody.omit({ role: true }).extend({
  name: trimmed(RegisterUserBody.shape.name),
  email: emailSchema.shape.email,
  birthDate: birthDateSchema,
  password: password(RegisterUserBody.shape.password),
  acceptTerms: z.literal(true, { error: "Aceite os termos para continuar." }),
}), "password");
export const profileSchema = UpdateProfileBody.omit({ email: true }).extend({
  name: trimmed(UpdateProfileBody.shape.name),
  birthDate: z.union([z.literal(""), birthDateSchema]).optional().transform(value => value || undefined),
  currentPassword: UpdateProfileBody.shape.currentPassword.min(1, "Informe sua senha atual para confirmar."),
});
export const passwordSchema = confirmed(UpdatePasswordBody.extend({
  currentPassword: UpdatePasswordBody.shape.currentPassword.min(1, "Informe a senha atual."),
  newPassword: password(UpdatePasswordBody.shape.newPassword),
}), "newPassword", "Confirme a nova senha.");
export const resetPasswordSchema = confirmed(z.object({
  token: trimmed(ResetPasswordBody.shape.token).refine(Boolean, "Informe o código recebido."),
  password: password(ResetPasswordBody.shape.newPassword),
}), "password");

function validationMessage(issue) {
  const field = issue.path?.at(-1);
  switch (field) {
    case "name": return !issue.input ? "Informe seu nome." : issue.code === "too_big" ? `O nome deve ter no máximo ${issue.maximum} caracteres.` : `Informe pelo menos ${issue.minimum} caracteres.`;
    case "birthDate": return "Informe uma data de nascimento válida.";
    case "email": return !issue.input ? "Informe seu e-mail." : issue.input.length > 254 ? "O e-mail deve ter no máximo 254 caracteres." : "Informe um e-mail válido.";
    case "password":
    case "newPassword": {
      if (typeof issue.input !== "string" || !issue.input) return "Informe a senha.";
      if (!issue.input.trim()) return "A senha não pode conter apenas espaços.";
      if (issue.input.length < 8) return "A senha deve ter no mínimo 8 caracteres.";
      if (issue.input.length > 100) return "A senha deve ter no máximo 100 caracteres.";
      return passwordRequirements;
    }
    case "currentPassword": return `A senha atual deve ter no máximo ${issue.maximum} caracteres.`;
    case "token": return issue.code === "too_big" ? `O código deve ter no máximo ${issue.maximum} caracteres.` : "Informe o código recebido.";
  }
}
export const accountResolver = schema => zodResolver(schema, { error: validationMessage });
