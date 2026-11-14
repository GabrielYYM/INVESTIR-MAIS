import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { student } from "../fixtures.js";
import { fillFields, mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

async function passwordForm() {
  return within((await screen.findByRole("heading", { name: "Segurança e Senha" })).closest("section"));
}
async function openDeletion(user) {
  await user.click(await screen.findByRole("button", { name: /Excluir minha conta/i }));
  const dialog = within(await screen.findByRole("dialog"));
  expect(dialog.getByRole("button", { name: /Prosseguir para confirmação/ })).toBeDisabled();
  await user.click(dialog.getByRole("checkbox"));
  await user.click(dialog.getByRole("button", { name: /Prosseguir para confirmação/ }));
  return dialog;
}

describe("segurança do perfil", () => {
  it("valida as senhas, atualiza a credencial e limpa os campos após sucesso", async () => {
    const update = mockApi("put", "/api/users/update-password", {}, { status: 204 });
    const { user } = renderApp("/perfil"); const form = await passwordForm();
    await user.click(form.getByRole("button", { name: "Atualizar senha" }));
    expect(form.getByText("Informe a senha atual.")).toBeInTheDocument(); expect(update).not.toHaveBeenCalled();
    await fillFields(user, { "Senha atual": "atual", "Nova senha": "senha-nova", "Confirme a nova senha": "diferente" }, form);
    await user.click(form.getByRole("button", { name: "Atualizar senha" }));
    expect(form.getByText("As senhas não coincidem.")).toBeInTheDocument();
    await fillFields(user, { "Confirme a nova senha": "senha-nova" }, form);
    await user.click(form.getByRole("button", { name: "Atualizar senha" }));
    expect(await form.findByRole("status")).toHaveTextContent("Senha atualizada com sucesso");
    expect(form.getByLabelText("Senha atual", { exact: true })).toHaveValue("");
    expect(update).toHaveBeenCalledOnce();
    expect(await update.mock.calls[0][0].request.clone().json()).toEqual({ currentPassword: "atual", newPassword: "senha-nova" });
  });

  it("preserva os campos após erro ao atualizar a senha", async () => {
    mockApi("put", "/api/users/update-password", { detail: "Senha atual incorreta." }, { status: 401 });
    const { user } = renderApp("/perfil"); const form = await passwordForm();
    await fillFields(user, { "Senha atual": "atual", "Nova senha": "senha-nova", "Confirme a nova senha": "senha-nova" }, form);
    await user.click(form.getByRole("button", { name: "Atualizar senha" }));
    expect(await form.findByRole("alert")).toHaveTextContent("Senha atual incorreta");
    expect(form.getByLabelText("Nova senha", { exact: true })).toHaveValue("senha-nova");
  });

  it.each([204, 503])("solicita recuperação e informa o resultado HTTP %i", async (status) => {
    const request = mockApi("post", "/api/users/forgot-password", status === 503 ? { detail: "E-mail indisponível." } : null, { status });
    const { user } = renderApp("/perfil");
    await user.click(await screen.findByRole("button", { name: "Enviar link de recuperação" }));
    expect(await screen.findByRole("status")).toHaveTextContent(status === 503 ? "E-mail indisponível" : student.email);
    expect(request).toHaveBeenCalledOnce();
    expect(await request.mock.calls[0][0].request.clone().json()).toEqual({ email: student.email });
    expect(screen.getByRole("link", { name: /Já recebeu o código/ })).toHaveAttribute("href", "/recuperar-senha");
  });

  it("exige dupla confirmação e senha válida para excluir e permite voltar/cancelar", async () => {
    const remove = mockApi("delete", `/api/users/${student.id}`, { detail: "Senha incorreta." }, { status: 401 });
    const { user } = renderApp("/perfil"); const dialog = await openDeletion(user);
    expect(dialog.getByRole("button", { name: "Confirmar e Desativar Conta" })).toBeDisabled();
    await fillFields(user, { "Senha atual da conta": "   " }, dialog);
    await user.click(dialog.getByRole("button", { name: "Confirmar e Desativar Conta" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("Informe sua senha atual"); expect(remove).not.toHaveBeenCalled();
    await fillFields(user, { "Senha atual da conta": "incorreta" }, dialog);
    await user.click(dialog.getByRole("button", { name: "Confirmar e Desativar Conta" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("Senha incorreta");
    expect(await remove.mock.calls[0][0].request.clone().json()).toEqual({ currentPassword: "incorreta" });
    await user.click(dialog.getByRole("button", { name: "Voltar", exact: true }));
    expect(dialog.getByRole("checkbox")).toBeChecked();
    await user.click(dialog.getByRole("button", { name: "Cancelar" })); expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /Excluir minha conta/i }));
    expect(screen.getByRole("checkbox")).not.toBeChecked();
    await user.click(screen.getByRole("button", { name: "Fechar modal" })); expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("envia a exclusão somente após confirmar e encerra a sessão", async () => {
    const remove = mockApi("delete", `/api/users/${student.id}`, null, { status: 204 });
    const logout = mockApi("post", "/auth/logout", null, { status: 204 });
    const { user } = renderApp("/perfil"); const dialog = await openDeletion(user);
    await fillFields(user, { "Senha atual da conta": "senha-atual" }, dialog);
    await user.click(dialog.getByRole("button", { name: "Confirmar e Desativar Conta" }));
    // jsdom does not navigate via location.href; verify the real HTTP effects instead.
    await screen.findByRole("button", { name: "Excluindo conta…" });
    await waitFor(() => expect(logout).toHaveBeenCalledOnce());
    expect(remove).toHaveBeenCalledOnce();
  });

  it("preserva os espaços da senha e impede fechar ou reenviar durante a exclusão", async () => {
    let release;
    const pending = new Promise((resolve) => { release = resolve; });
    const remove = mockApi("delete", `/api/users/${student.id}`, async () => {
      await pending; return { detail: "Senha incorreta." };
    }, { status: 401 });
    const { user } = renderApp("/perfil"); const dialog = await openDeletion(user);
    await fillFields(user, { "Senha atual da conta": " senha com espaços " }, dialog);
    await user.click(dialog.getByRole("button", { name: "Confirmar e Desativar Conta" }));
    expect(dialog.getByRole("button", { name: "Fechar modal" })).toBeDisabled();
    expect(dialog.getByRole("button", { name: "Cancelar" })).toBeDisabled();
    await user.keyboard("{Escape}"); expect(screen.getByRole("dialog")).toBeInTheDocument();
    await waitFor(() => expect(remove).toHaveBeenCalledOnce());
    expect(await remove.mock.calls[0][0].request.clone().json()).toEqual({ currentPassword: " senha com espaços " });
    release();
    expect(await dialog.findByRole("alert")).toHaveTextContent("Senha incorreta");
    expect(dialog.getByLabelText("Senha atual da conta")).toHaveValue(" senha com espaços ");
  });
});
