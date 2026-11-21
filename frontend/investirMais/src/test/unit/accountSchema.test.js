import { describe, expect, it } from "vitest";
import { accountResolver, emailSchema, loginSchema, passwordRequirements, passwordSchema, profileSchema, registrationSchema, resetPasswordSchema } from "../../services/accountSchema.js";

const valid = { name: "Ana Teste", email: "ana@example.test", password: "SenhaTeste1!", confirmPassword: "SenhaTeste1!" };
const validate = (schema, values) => accountResolver(schema)(values, {}, { fields: {}, shouldUseNativeValidation: false });

describe("validação compartilhada dos formulários de conta", () => {
  it.each([8, 100, 101])("aplica o limite da nova senha com %i caracteres nos três formulários", async length => {
    const password = "Aa1!" + "x".repeat(length - 4);
    const registration = await validate(registrationSchema, { ...valid, password, confirmPassword: password });
    const update = await validate(passwordSchema, { currentPassword: "antiga", newPassword: password, confirmPassword: password });
    const reset = await validate(resetPasswordSchema, { token: "codigo", password, confirmPassword: password });
    for (const [result, field] of [[registration, "password"], [update, "newPassword"], [reset, "password"]]) {
      if (length <= 100) expect(result.errors).toEqual({});
      else expect(result.errors[field].message).toBe("A senha deve ter no máximo 100 caracteres.");
    }
  });
  it.each(["SENHAFORTE1!", "senhaforte1!", "SenhaForte!!", "SenhaForte12", "SenhaForte1 ", "SenhaForte1á"])("recusa senha sem todos os requisitos: %s", async password => {
    const registration = await validate(registrationSchema, { ...valid, password, confirmPassword: password });
    const update = await validate(passwordSchema, { currentPassword: "antiga", newPassword: password, confirmPassword: password });
    const reset = await validate(resetPasswordSchema, { token: "codigo", password, confirmPassword: password });
    expect(registration.errors.password.message).toBe(passwordRequirements);
    expect(update.errors.newPassword.message).toBe(passwordRequirements);
    expect(reset.errors.password.message).toBe(passwordRequirements);
  });
  it("normaliza apelido e e-mail, preserva a senha e descarta campos externos ao cadastro público", async () => {
    const result = await validate(registrationSchema, { ...valid, name: " Ana Teste ", email: " ana@example.test ", password: " SenhaTeste1! ", confirmPassword: " SenhaTeste1! ", role: "ADMIN", age: 25, birthDate: "2000-01-15" });
    expect(result.errors).toEqual({});
    expect(result.values).toMatchObject({ name: valid.name, email: valid.email, password: " SenhaTeste1! " });
    expect(result.values).not.toHaveProperty("role");
    expect(result.values).not.toHaveProperty("age");
    expect(result.values).not.toHaveProperty("birthDate");
  });
  it.each([
    ["name", "   ", "Informe seu apelido."], ["name", " A ", "Informe pelo menos 2 caracteres."],
    ["name", "a".repeat(101), "O apelido deve ter no máximo 100 caracteres."],
    ["password", "", "Informe a senha."], ["password", "curta", "A senha deve ter no mínimo 8 caracteres."],
    ["password", "        ", "A senha não pode conter apenas espaços."],
    ["password", "x".repeat(101), "A senha deve ter no máximo 100 caracteres."],
  ])("preserva os limites e mensagens de %s %#", async (field, value, message) => {
    const result = await validate(registrationSchema, { ...valid, [field]: value });
    expect(result.errors[field].message).toBe(message);
    expect(result.values).toEqual({});
  });
  it("valida a confirmação mesmo que o apelido também seja inválido", async () => {
    const { errors } = await validate(registrationSchema, { ...valid, name: "", confirmPassword: "outra" });
    expect(errors.name.message).toBe("Informe seu apelido.");
    expect(errors.confirmPassword.message).toBe("As senhas não coincidem.");
  });
  it.each([["", "Informe seu e-mail."], ["invalido", "Informe um e-mail válido."], ["a".repeat(255), "O e-mail deve ter no máximo 254 caracteres."]])("traduz a validação de e-mail %#", async (email, message) => {
    expect((await validate(emailSchema, { email })).errors.email.message).toBe(message);
  });
  it("aceita a senha de acesso existente sem impor o tamanho mínimo de uma nova senha", async () => {
    expect((await validate(loginSchema, { email: valid.email, password: "antiga" })).errors).toEqual({});
  });
  it("exige o código e a senha atual sem remover os espaços das credenciais", async () => {
    expect((await validate(resetPasswordSchema, { token: " ", password: valid.password, confirmPassword: valid.password })).errors.token.message).toBe("Informe o código recebido.");
    expect((await validate(resetPasswordSchema, { token: "x".repeat(65), password: valid.password, confirmPassword: valid.password })).errors.token.message).toBe("O código deve ter no máximo 64 caracteres.");
    expect((await validate(profileSchema, { name: valid.name, currentPassword: " senha atual " })).values.currentPassword).toBe(" senha atual ");
    expect((await validate(profileSchema, { name: valid.name, currentPassword: "x".repeat(4097) })).errors.currentPassword.message).toBe("A senha atual deve ter no máximo 4096 caracteres.");
    expect((await validate(passwordSchema, { currentPassword: "atual", newPassword: valid.password, confirmPassword: "" })).errors.confirmPassword.message).toBe("Confirme a nova senha.");
  });
});
