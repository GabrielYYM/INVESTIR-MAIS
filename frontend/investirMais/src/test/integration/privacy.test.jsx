import { fireEvent, render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import TermsModal from "../../components/TermsModal.jsx";
import { renderApp } from "../renderApp.jsx";

describe("termos e privacidade", () => {
  it.each(["/termos", "/termos-de-uso", "/politica-de-privacidade"])("apresenta o documento da rota %s e permite alternar", async (path) => {
    const { user } = renderApp(path);
    await screen.findByRole("heading", { name: "Termos e Políticas de Privacidade" });
    expect(screen.getByRole("heading", { name: path.includes("privacidade") ? "Política de Privacidade e Proteção de Dados Pessoais" : "Termos e Condições Gerais de Uso", exact: true })).toBeInTheDocument();
    const page = within(screen.getByRole("heading", { name: "Termos e Políticas de Privacidade" }).closest("section"));
    await user.click(page.getByRole("button", { name: "Política de Privacidade (LGPD)" }));
    expect(page.getByRole("heading", { name: "Política de Privacidade e Proteção de Dados Pessoais", exact: true })).toBeInTheDocument();
    await user.click(page.getByRole("button", { name: "Termos de Uso" }));
    expect(page.getByRole("heading", { name: "Termos e Condições Gerais de Uso" })).toBeInTheDocument();
  });

  it.each(["Termos de Uso", "Política de Privacidade"])("abre %s pelo rodapé e fecha por botão ou Escape", async (label) => {
    const { user } = renderApp("/entrar");
    await user.click(screen.getByRole("button", { name: label }));
    const dialog = within(screen.getByRole("dialog"));
    await user.click(dialog.getByRole("button", { name: label === "Termos de Uso" ? "Política de Privacidade (LGPD)" : "Termos de Uso" }));
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    await user.keyboard("{Escape}"); expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: label }));
    await user.click(within(screen.getByRole("dialog")).getAllByRole("button", { name: "Fechar" })[0]);
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  it("fecha pelo fundo, preserva cliques internos e aceita troca de documento", async () => {
    const close = vi.fn(); const user = userEvent.setup();
    const { rerender } = render(<TermsModal initialTab="termos" onClose={close} />);
    await user.click(screen.getByRole("heading", { name: "Termos e Condições Gerais de Uso" })); expect(close).not.toHaveBeenCalled();
    rerender(<TermsModal initialTab="privacidade" onClose={close} />);
    expect(screen.getByRole("heading", { name: "Política de Privacidade e Proteção de Dados Pessoais", exact: true })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("dialog")); expect(close).toHaveBeenCalledOnce();
  });

  it("volta ao início ao abrir o documento sem uma página anterior", async () => {
    vi.spyOn(window.history, "length", "get").mockReturnValue(1);
    const { user, router } = renderApp("/termos");
    await user.click(screen.getByRole("button", { name: "Voltar", exact: true }));
    expect(await screen.findByRole("heading", { name: "Olá, Ana." })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe("/");
  });
});
