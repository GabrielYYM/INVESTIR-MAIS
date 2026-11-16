import { fireEvent, screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { HttpResponse } from "msw/http";
import { renderApp } from "../renderApp.jsx";
import { student } from "../fixtures.js";
import { fillFields, mockApi } from "../helpers.js";

async function fillSignUp(user) {
  await fillFields(user, {
    "Apelido": "  Ana Teste  ", "E-mail": student.email,
    Senha: "SenhaTeste1!", "Confirme a senha": "SenhaTeste1!",
  });
  await user.click(screen.getByRole("checkbox"));
}

describe("cadastro", () => {
  it("recusa senha composta apenas por espaços e preserva espaços em uma senha válida", async () => {
    const bodies = [];
    const register = mockApi("post", "/api/users/register", async ({ request }) => {
      bodies.push(await request.json());
      return {};
    }, { status: 202 });
    const { user } = renderApp("/cadastro");
    expect(screen.queryByLabelText("Senha atual")).not.toBeInTheDocument();
    await fillSignUp(user);
    const password = screen.getByLabelText("Senha", { exact: true });
    const confirmation = screen.getByLabelText("Confirme a senha");
    fireEvent.change(password, { target: { value: "        " } });
    fireEvent.change(confirmation, { target: { value: "        " } });
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByText("A senha não pode conter apenas espaços.")).toBeInTheDocument();
    expect(register).not.toHaveBeenCalled();

    fireEvent.change(password, { target: { value: " SenhaTeste1! " } });
    fireEvent.change(confirmation, { target: { value: " SenhaTeste1! " } });
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByRole("heading", { name: "Confirme seu e-mail" })).toBeInTheDocument();
    expect(bodies[0].password).toBe(" SenhaTeste1! ");
  });

  it("valida apelido, senha, confirmação e aceite antes de enviar", async () => {
    const register = mockApi("post", "/api/users/register", {});
    const { user } = renderApp("/cadastro");
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByText("Informe seu apelido.")).toBeInTheDocument();
    expect(screen.queryByLabelText("Data de nascimento")).not.toBeInTheDocument();
    expect(screen.getByRole("alert")).toHaveTextContent("Aceite os termos para continuar.");
    await user.type(screen.getByLabelText("Senha", { exact: true }), "curta");
    await user.type(screen.getByLabelText("Confirme a senha"), "diferente");
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByText("A senha deve ter no mínimo 8 caracteres.")).toBeInTheDocument();
    expect(screen.getByText("As senhas não coincidem.")).toBeInTheDocument();
    expect(register).not.toHaveBeenCalled();
  });

  it("envia dados normalizados, exibe confirmação e reenvia o link", async () => {
    const registered = [];
    const resent = [];
    mockApi("post", "/api/users/register", async ({ request }) => {
      registered.push(await request.json());
      return { message: "Verifique o e-mail." };
    });
    mockApi("post", "/api/users/resend-verification", async ({ request }) => {
      resent.push(await request.json());
      return { message: "Solicitado." };
    });
    const { user } = renderApp("/cadastro");
    await fillSignUp(user);
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByRole("heading", { name: "Confirme seu e-mail" })).toBeInTheDocument();
    expect(registered).toEqual([{ name: student.name, email: student.email, password: "SenhaTeste1!" }]);
    await user.click(screen.getByRole("button", { name: "Reenviar link de confirmação" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Link de confirmação solicitado novamente.");
    expect(resent).toEqual([{ email: student.email }]);
  });

  it("mantém os dados ao receber falha do cadastro", async () => {
    mockApi("post", "/api/users/register", { detail: "Dados recusados." }, { status: 422 });
    const { user } = renderApp("/cadastro");
    await fillSignUp(user);
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Dados recusados.");
    expect(screen.getByLabelText("E-mail")).toHaveValue(student.email);
  });

  it("exibe limite de tentativas no reenvio do link", async () => {
    mockApi("post", "/api/users/register", {});
    mockApi("post", "/api/users/resend-verification", {}, { status: 429 });
    const { user } = renderApp("/cadastro");
    await fillSignUp(user);
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    await user.click(await screen.findByRole("button", { name: "Reenviar link de confirmação" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Muitas tentativas.");
    expect(screen.queryByRole("status")).not.toBeInTheDocument();
  });
});

describe("recuperação de senha", () => {
  it("solicita, reenvia e usa o código com a nova senha", async () => {
    const requests = [];
    const resets = [];
    mockApi("post", "/api/users/forgot-password", async ({ request }) => {
      requests.push(await request.json());
      return {};
    });
    mockApi("post", "/api/users/reset-password", async ({ request }) => {
      resets.push(await request.json());
      return {};
    });
    const { user } = renderApp("/recuperar-senha");
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByText("Informe seu e-mail.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("E-mail"), "  ana@example.test  ");
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    await screen.findByRole("heading", { name: "Redefinir senha" });
    await user.click(screen.getByRole("button", { name: "Solicitar outro código" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Solicitamos um novo código");
    await user.type(screen.getByLabelText("Código de recuperação"), "  codigo-recuperacao  ");
    await user.type(screen.getByLabelText("Nova senha", { exact: true }), "SenhaNova1!");
    await user.type(screen.getByLabelText("Confirme a nova senha"), "SenhaNova1!");
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByRole("heading", { name: "Senha atualizada" })).toBeInTheDocument();
    expect(requests).toEqual([{ email: student.email }, { email: student.email }]);
    expect(resets).toEqual([{ token: "codigo-recuperacao", newPassword: "SenhaNova1!" }]);
    expect(screen.queryByLabelText("Nova senha", { exact: true })).not.toBeInTheDocument();
  });

  it("exibe falha de solicitação sem avançar à redefinição", async () => {
    mockApi("post", "/api/users/forgot-password", {}, { status: 503 });
    const { user } = renderApp("/recuperar-senha");
    await user.type(screen.getByLabelText("E-mail"), student.email);
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível solicitar o código agora.");
    expect(screen.getByRole("heading", { name: "Recuperar senha" })).toBeInTheDocument();
  });

  it("preserva o formulário quando o código de recuperação expira", async () => {
    mockApi("post", "/api/users/forgot-password", {});
    mockApi("post", "/api/users/reset-password", {}, { status: 400 });
    const { user } = renderApp("/recuperar-senha");
    await user.type(screen.getByLabelText("E-mail"), student.email);
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    await user.type(await screen.findByLabelText("Código de recuperação"), "expirado");
    expect(screen.queryByLabelText("Senha atual")).not.toBeInTheDocument();
    await user.type(screen.getByLabelText("Nova senha", { exact: true }), "SenhaNova1!");
    await user.type(screen.getByLabelText("Confirme a nova senha"), "SenhaNova1!");
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Código inválido ou expirado.");
    expect(screen.getByLabelText("Código de recuperação")).toHaveValue("expirado");
  });
});

describe("edição do perfil", () => {
  it.each([401, 403, 503, "network"])("distingue a senha recusada da sessão encerrada (%s)", async status => {
    let submitted = false;
    mockApi("get", "/auth/me", () => !submitted ? student : status === "network" ? HttpResponse.error() : HttpResponse.json({}, { status }));
    mockApi("put", `/api/users/${student.id}`, () => {
      submitted = true;
      return HttpResponse.json({ detail: "Senha atual incorreta." }, { status: 401 });
    });
    const { user, router } = renderApp("/perfil");
    const form = within((await screen.findByRole("heading", { name: "Alterar informações" })).closest("form"));
    await fillFields(user, { "Senha atual": "senha-incorreta" }, form);
    await user.click(form.getByRole("button", { name: "Salvar perfil" }));
    if ([401, 403].includes(status)) {
      expect(await screen.findByRole("heading", { name: "Bem-vindo de volta" })).toBeInTheDocument();
      expect(new URLSearchParams(router.state.location.search).get("retorno")).toBe("/perfil");
    } else {
      expect(await screen.findByRole("alert")).toHaveTextContent("Senha atual incorreta.");
      expect(router.state.location.pathname).toBe("/perfil");
      expect(form.getByLabelText("Senha atual")).toHaveValue("senha-incorreta");
    }
  });

  it("preserva os dados e permite tentar novamente quando a atualização fica sem conexão", async () => {
    const update = mockApi("put", `/api/users/${student.id}`, HttpResponse.error());
    const { user, router } = renderApp("/perfil");
    const form = within((await screen.findByRole("heading", { name: "Alterar informações" })).closest("form"));
    await fillFields(user, { "Apelido": "Ana Offline", "Senha atual": "senha-atual" }, form);
    await user.click(form.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível salvar.");
    expect(form.getByLabelText("Apelido")).toHaveValue("Ana Offline");
    expect(form.getByRole("button", { name: "Salvar perfil" })).toBeEnabled();
    expect(router.state.location.pathname).toBe("/perfil");
    expect(update).toHaveBeenCalledOnce();
  });
  it("exige a senha, salva o apelido e atualiza a identidade da sessão", async () => {
    let currentUser = { ...student };
    const updates = [];
    mockApi("get", "/auth/me", () => currentUser);
    mockApi("put", "/api/users/:id", async ({ request, params }) => {
      expect(params.id).toBe(student.id);
      const body = await request.json();
      updates.push(body);
      currentUser = { ...currentUser, name: body.name };
      return currentUser;
    });
    const { user } = renderApp("/perfil");
    const profileForm = within((await screen.findByRole("heading", { name: "Alterar informações" })).closest("form"));
    expect(profileForm.queryByLabelText("Nova senha", { exact: true })).not.toBeInTheDocument();
    expect(profileForm.queryByLabelText("Confirme a nova senha")).not.toBeInTheDocument();
    expect(profileForm.getByLabelText("Senha atual")).toHaveAccessibleDescription("Necessária para confirmar a atualização dos seus dados.");
    expect(screen.getByLabelText("E-mail")).toHaveAttribute("readonly");
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByText("Informe sua senha atual para confirmar.")).toBeInTheDocument();
    expect(updates).toHaveLength(0);
    await fillFields(user, { "Apelido": "  Ana Atualizada  ", "Senha atual": "senha-teste" }, profileForm);
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Perfil atualizado.");
    expect(await screen.findByRole("heading", { name: "Ana Atualizada" })).toBeInTheDocument();
    await waitFor(() => expect(profileForm.getByLabelText("Senha atual")).toHaveValue(""));
    expect(updates).toEqual([{ name: "Ana Atualizada", email: student.email, currentPassword: "senha-teste" }]);
  });

  it("permanece no perfil se a senha atual for recusada com sessão válida", async () => {
    mockApi("put", "/api/users/:id", { detail: "Senha atual incorreta." }, { status: 401 });
    const { user, router } = renderApp("/perfil");
    const profileForm = within((await screen.findByRole("heading", { name: "Alterar informações" })).closest("form"));
    await user.type(profileForm.getByLabelText("Senha atual"), "errada");
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Senha atual incorreta.");
    expect(router.state.location.pathname).toBe("/perfil");
  });
});
