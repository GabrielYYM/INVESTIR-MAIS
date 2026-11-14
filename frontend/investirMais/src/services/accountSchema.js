import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { ForgotPasswordBody, RegisterUserBody, ResetPasswordBody, UpdatePasswordBody, UpdateProfileBody } from "../generated/account.zod.ts";

const trimmed = schema => z.string().trim().pipe(schema);
const password = schema => schema.refine(value => Boolean(value.trim()), "A senha não pode conter apenas espaços.");
const confirmed = (schema, field, required = "Confirme a senha.") => schema
  .extend({ confirmPassword: z.string().min(1, required) })
  .refine(values => values.confirmPassword === values[field], {
    path: ["confirmPassword"], message: "As senhas não coincidem.",
    when: ({ value }) => typeof value?.[field] === "string" && typeof value?.confirmPassword === "string",
  });

export const emailSchema = ForgotPasswordBody.extend({ email: trimmed(ForgotPasswordBody.shape.email) });
export const loginSchema = emailSchema.extend({ password: z.string().min(1, "Informe sua senha.") });
export const registrationSchema = confirmed(RegisterUserBody.omit({ role: true }).extend({
  name: trimmed(RegisterUserBody.shape.name),
  email: emailSchema.shape.email,
  age: RegisterUserBody.shape.age.min(0).max(120),
  password: password(RegisterUserBody.shape.password),
  acceptTerms: z.literal(true, { error: "Aceite os termos para continuar." }),
}), "password");
export const profileSchema = UpdateProfileBody.omit({ age: true, email: true }).extend({
  name: trimmed(UpdateProfileBody.shape.name),
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
    case "age": return "Informe uma idade inteira de 0 a 120.";
    case "email": return !issue.input ? "Informe seu e-mail." : issue.input.length > 254 ? "O e-mail deve ter no máximo 254 caracteres." : "Informe um e-mail válido.";
    case "password":
    case "newPassword": return !issue.input ? "Informe a senha." : `A senha deve ter no ${issue.code === "too_big" ? `máximo ${issue.maximum}` : `mínimo ${issue.minimum}`} caracteres.`;
    case "currentPassword": return `A senha atual deve ter no máximo ${issue.maximum} caracteres.`;
    case "token": return issue.code === "too_big" ? `O código deve ter no máximo ${issue.maximum} caracteres.` : "Informe o código recebido.";
  }
}
export const accountResolver = schema => zodResolver(schema, { error: validationMessage });
