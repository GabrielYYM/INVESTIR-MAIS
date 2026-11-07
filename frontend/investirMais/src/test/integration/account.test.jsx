import { screen, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";
import { student } from "../fixtures.js";

async function fillSignUp(user) {
  await user.type(screen.getByLabelText("Nome completo"), "  Ana Teste  ");
  await user.type(screen.getByLabelText("Idade"), "25");
  await user.type(screen.getByLabelText("E-mail"), student.email);
  await user.type(screen.getByLabelText("Senha", { exact: true }), "senha-teste");
  await user.type(screen.getByLabelText("Confirme a senha"), "senha-teste");
  await user.click(screen.getByRole("checkbox"));
}

describe("cadastro", () => {
  it("valida nome, idade, senha, confirmação e aceite antes de enviar", async () => {
    const register = vi.fn();
    server.use(http.post(origin + "/api/users/register", register));
    const { user } = renderApp("/cadastro");
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByText("Informe seu nome.")).toBeInTheDocument();
    expect(screen.getByText("Informe uma idade inteira de 0 a 120.")).toBeInTheDocument();
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
    server.use(
      http.post(origin + "/api/users/register", async ({ request }) => {
        registered.push(await request.json());
        return HttpResponse.json({ message: "Verifique o e-mail." });
      }),
      http.post(origin + "/api/users/resend-verification", async ({ request }) => {
        resent.push(await request.json());
        return HttpResponse.json({ message: "Solicitado." });
      }),
    );
    const { user } = renderApp("/cadastro");
    await fillSignUp(user);
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByRole("heading", { name: "Confirme seu e-mail" })).toBeInTheDocument();
    expect(registered).toEqual([{ name: student.name, age: 25, email: student.email, password: "senha-teste" }]);
    await user.click(screen.getByRole("button", { name: "Reenviar link de confirmação" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Link de confirmação solicitado novamente.");
    expect(resent).toEqual([{ email: student.email }]);
  });

  it("mantém os dados ao receber falha do cadastro", async () => {
    server.use(http.post(origin + "/api/users/register", () => HttpResponse.json({ detail: "Dados recusados." }, { status: 422 })));
    const { user } = renderApp("/cadastro");
    await fillSignUp(user);
    await user.click(screen.getByRole("button", { name: "Criar conta" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Dados recusados.");
    expect(screen.getByLabelText("E-mail")).toHaveValue(student.email);
  });

  it("exibe limite de tentativas no reenvio do link", async () => {
    server.use(
      http.post(origin + "/api/users/register", () => HttpResponse.json({})),
      http.post(origin + "/api/users/resend-verification", () => HttpResponse.json({}, { status: 429 })),
    );
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
    server.use(
      http.post(origin + "/api/users/forgot-password", async ({ request }) => {
        requests.push(await request.json());
        return HttpResponse.json({});
      }),
      http.post(origin + "/api/users/reset-password", async ({ request }) => {
        resets.push(await request.json());
        return HttpResponse.json({});
      }),
    );
    const { user } = renderApp("/recuperar-senha");
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByText("Informe seu e-mail.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("E-mail"), "  ana@example.test  ");
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    await screen.findByRole("heading", { name: "Redefinir senha" });
    await user.click(screen.getByRole("button", { name: "Solicitar outro código" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Solicitamos um novo código");
    await user.type(screen.getByLabelText("Código de recuperação"), "  codigo-recuperacao  ");
    await user.type(screen.getByLabelText("Nova senha", { exact: true }), "senha-nova");
    await user.type(screen.getByLabelText("Confirme a nova senha"), "senha-nova");
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByRole("heading", { name: "Senha atualizada" })).toBeInTheDocument();
    expect(requests).toEqual([{ email: student.email }, { email: student.email }]);
    expect(resets).toEqual([{ token: "codigo-recuperacao", newPassword: "senha-nova" }]);
    expect(screen.queryByLabelText("Nova senha", { exact: true })).not.toBeInTheDocument();
  });

  it("exibe falha de solicitação sem avançar à redefinição", async () => {
    server.use(http.post(origin + "/api/users/forgot-password", () => HttpResponse.json({}, { status: 503 })));
    const { user } = renderApp("/recuperar-senha");
    await user.type(screen.getByLabelText("E-mail"), student.email);
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível solicitar o código agora.");
    expect(screen.getByRole("heading", { name: "Recuperar senha" })).toBeInTheDocument();
  });

  it("preserva o formulário quando o código de recuperação expira", async () => {
    server.use(
      http.post(origin + "/api/users/forgot-password", () => HttpResponse.json({})),
      http.post(origin + "/api/users/reset-password", () => HttpResponse.json({}, { status: 400 })),
    );
    const { user } = renderApp("/recuperar-senha");
    await user.type(screen.getByLabelText("E-mail"), student.email);
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    await user.type(await screen.findByLabelText("Código de recuperação"), "expirado");
    await user.type(screen.getByLabelText("Nova senha", { exact: true }), "senha-nova");
    await user.type(screen.getByLabelText("Confirme a nova senha"), "senha-nova");
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Código inválido ou expirado.");
    expect(screen.getByLabelText("Código de recuperação")).toHaveValue("expirado");
  });
});

describe("edição do perfil", () => {
  it("exige a senha, salva o nome e atualiza a identidade da sessão", async () => {
    let currentUser = { ...student };
    const updates = [];
    server.use(
      http.get(origin + "/auth/me", () => HttpResponse.json(currentUser)),
      http.put(origin + "/api/users/:id", async ({ request, params }) => {
        expect(params.id).toBe(student.id);
        const body = await request.json();
        updates.push(body);
        currentUser = { ...currentUser, name: body.name };
        return HttpResponse.json(currentUser);
      }),
    );
    const { user } = renderApp("/perfil");
    await screen.findByRole("heading", { name: "Meu perfil" });
    expect(screen.getByLabelText("E-mail")).toHaveAttribute("readonly");
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByText("Informe sua senha atual para confirmar.")).toBeInTheDocument();
    expect(updates).toHaveLength(0);
    await user.clear(screen.getByLabelText("Nome completo"));
    await user.type(screen.getByLabelText("Nome completo"), "  Ana Atualizada  ");
    await user.type(screen.getByLabelText("Senha atual"), "senha-teste");
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByRole("status")).toHaveTextContent("Perfil atualizado.");
    expect(screen.getByRole("heading", { name: "Ana Atualizada" })).toBeInTheDocument();
    await waitFor(() => expect(screen.getByLabelText("Senha atual")).toHaveValue(""));
    expect(updates).toEqual([{ name: "Ana Atualizada", email: student.email, currentPassword: "senha-teste" }]);
  });

  it("permanece no perfil se a senha atual for recusada com sessão válida", async () => {
    server.use(http.put(origin + "/api/users/:id", () => HttpResponse.json({ detail: "Senha atual incorreta." }, { status: 401 })));
    const { user, router } = renderApp("/perfil");
    await user.type(await screen.findByLabelText("Senha atual"), "errada");
    await user.click(screen.getByRole("button", { name: "Salvar perfil" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Senha atual incorreta.");
    expect(router.state.location.pathname).toBe("/perfil");
  });
});
