import { screen } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";
import { student } from "../fixtures.js";
import { fillFields, mockApi } from "../helpers.js";

async function fillCredentials(user) {
  await fillFields(user, { "E-mail": "  ana@example.test  ", Senha: "senha-teste" });
  await user.click(screen.getByRole("button", { name: "Continuar" }));
}

describe("confirmação do cadastro", () => {
  it.each([
    [true, "Olá, Ana.", "/"],
    [false, "E-mail confirmado!", "/confirmar-email"],
  ])("respeita a sessão retornada pelo servidor (%s)", async (signedIn, heading, path) => {
    const confirm = mockApi("post", "/api/users/verify-email", { signedIn });
    const { user, router } = renderApp("/confirmar-email?token=token-teste");
    expect(confirm).not.toHaveBeenCalled();

    await user.click(screen.getByRole("button", { name: "Confirmar e-mail" }));

    expect(await screen.findByRole("heading", { name: heading })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe(path);
    if (!signedIn) expect(screen.getByRole("link", { name: "Ir para o login" })).toBeInTheDocument();
  });
});

describe("login com senha e código", () => {
  it("valida campos obrigatórios sem enviar credenciais", async () => {
    const request = mockApi("post", "/auth/login", null, { status: 204 });
    const { user } = renderApp("/entrar");
    await user.click(screen.getByRole("button", { name: "Continuar" }));
    expect(await screen.findByText("Informe seu e-mail.")).toBeInTheDocument();
    expect(screen.getByText("Informe sua senha.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("E-mail"), "email-invalido");
    await user.click(screen.getByRole("button", { name: "Continuar" }));
    expect(await screen.findByText("Informe um e-mail válido.")).toBeInTheDocument();
    expect(request).not.toHaveBeenCalled();
  });

  it("envia formulário com CSRF, verifica o código e retorna à rota solicitada", async () => {
    const loginBodies = [];
    const tokens = [];
    const csrfHeaders = [];
    server.use(
      http.post(origin + "/auth/login", async ({ request }) => {
        loginBodies.push(Object.fromEntries(new URLSearchParams(await request.text())));
        csrfHeaders.push(request.headers.get("X-XSRF-TOKEN"));
        return new HttpResponse(null, { status: 204 });
      }),
      http.post(origin + "/auth/verify-2fa", async ({ request }) => {
        tokens.push(new URLSearchParams(await request.text()).get("token"));
        csrfHeaders.push(request.headers.get("X-XSRF-TOKEN"));
        return new HttpResponse(null, { status: 204 });
      }),
    );
    const { user, router } = renderApp("/entrar?retorno=%2Fperfil");
    await fillCredentials(user);
    expect(await screen.findByRole("heading", { name: "Confira seu e-mail" })).toBeInTheDocument();
    expect(loginBodies).toEqual([{ email: student.email, password: "senha-teste", retorno: "/perfil" }]);
    await user.click(screen.getByText("Usar código manualmente"));
    await user.type(screen.getByLabelText("Código de verificação"), "  codigo-teste  ");
    await user.click(screen.getByRole("button", { name: "Confirmar acesso" }));
    expect(await screen.findByRole("heading", { name: "Meu perfil" })).toBeInTheDocument();
    expect(tokens).toEqual(["codigo-teste"]);
    expect(csrfHeaders).toEqual(["csrf-test-token", "csrf-test-token"]);
    expect(router.state.location.pathname).toBe("/perfil");
  });

  it.each([
    [401, {}, "Não foi possível entrar. Confira o e-mail, a senha e a confirmação da conta."],
    [403, { detail: "Confirme seu e-mail." }, "Confirme seu e-mail."],
    [429, {}, "Muitas tentativas. Aguarde 45 s e tente novamente."],
  ])("exibe a falha HTTP %i e mantém o formulário", async (status, body, message) => {
    mockApi("post", "/auth/login", body, { status, headers: { "Retry-After": "45" } });
    const { user } = renderApp("/entrar");
    await fillCredentials(user);
    expect(await screen.findByRole("alert")).toHaveTextContent(message);
    expect(screen.getByRole("button", { name: "Continuar" })).toBeEnabled();
  });

  it("valida código obrigatório e permite corrigir código recusado", async () => {
    mockApi("post", "/auth/login", null, { status: 204 });
    mockApi("post", "/auth/verify-2fa", {}, { status: 401 });
    const { user } = renderApp("/entrar");
    await fillCredentials(user);
    await user.click(await screen.findByText("Usar código manualmente"));
    await user.click(screen.getByRole("button", { name: "Confirmar acesso" }));
    expect(await screen.findByText("Informe o código recebido.")).toBeInTheDocument();
    await user.type(screen.getByLabelText("Código de verificação"), "expirado");
    await user.click(screen.getByRole("button", { name: "Confirmar acesso" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Código inválido ou expirado.");
    await user.click(screen.getByRole("button", { name: "Voltar e usar outro e-mail" }));
    expect(screen.getByLabelText("Senha", { exact: true })).toHaveValue("");
    expect(screen.getByLabelText("E-mail")).toHaveValue(student.email);
  });

  it.each([
    ["//evil.example.test/perfil", "/"], ["https://evil.example.test", "/"],
    ["/sair", "/"], ["/entrar", "/"], ["/\\evil.example.test/perfil", "/"],
    ["/educacao?pagina=2#ignorar", "/educacao?pagina=2"], ["/", "/"],
  ])("restringe o retorno %s a uma rota interna permitida", async (retorno, expected) => {
    let sentReturn;
    server.use(http.post(origin + "/auth/login", async ({ request }) => {
      sentReturn = new URLSearchParams(await request.text()).get("retorno");
      return new HttpResponse(null, { status: 204 });
    }));
    const { user } = renderApp("/entrar?" + new URLSearchParams({ retorno }));
    await fillCredentials(user);
    await screen.findByRole("heading", { name: "Confira seu e-mail" });
    expect(sentReturn).toBe(expected);
  });
});

describe("link de acesso recebido por e-mail", () => {
  it("remove o token da URL e o consome uma única vez em StrictMode", async () => {
    const verify = vi.fn(async ({ request }) => {
      expect(window.location.hash).toBe("");
      expect(new URLSearchParams(await request.text()).get("token")).toBe("token-link");
      return new HttpResponse(null, { status: 204 });
    });
    server.use(http.post(origin + "/auth/verify-2fa", verify));
    const { router } = renderApp("/entrar/link?retorno=%2Fperfil#ott=token-link", { strict: true });
    expect(await screen.findByRole("heading", { name: "Meu perfil" })).toBeInTheDocument();
    expect(verify).toHaveBeenCalledOnce();
    expect(router.state.location.pathname).toBe("/perfil");
  });

  it("oferece novo login quando não há token", async () => {
    const verify = mockApi("post", "/auth/verify-2fa", null, { status: 204 });
    renderApp("/entrar/link?retorno=%2Fperfil");
    expect(screen.getByRole("alert")).toHaveTextContent("Abra o link enviado por e-mail ou entre novamente");
    expect(screen.getByRole("link", { name: "Voltar para entrar" })).toHaveAttribute("href", "/entrar?retorno=%2Fperfil");
    expect(verify).not.toHaveBeenCalled();
  });

  it("explica a expiração do link sem reenviar o token", async () => {
    mockApi("post", "/auth/verify-2fa", {}, { status: 401 });
    renderApp("/entrar/link#ott=expirado");
    expect(await screen.findByRole("alert")).toHaveTextContent("O link expirou ou não pertence a esta sessão");
    expect(window.location.hash).toBe("");
  });

});
