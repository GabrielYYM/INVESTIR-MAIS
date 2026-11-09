import { fireEvent, screen, waitFor } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { course, lesson, teacher } from "../fixtures.js";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";

describe("limites de entrada compatíveis com a API", () => {
  it.each([
    ["curso", "Editar curso", "/api/courses/" + course.id, course],
    ["aula", "Editar aula " + lesson.title, "/api/lessons/" + lesson.id, lesson],
  ])("valida a descrição de %s sem perder o texto e aceita 10.000 caracteres", async (kind, edit, path, item) => {
    const save = vi.fn(async ({ request }) => HttpResponse.json({ ...item, ...await request.json() }));
    server.use(
      http.get(origin + "/auth/me", () => HttpResponse.json(teacher)),
      http.put(origin + path, save),
    );
    const { user } = renderApp("/educacao/" + course.id);
    await user.click(await screen.findByRole("button", { name: edit, exact: true }));
    const description = screen.getByRole("textbox", { name: "Descrição", exact: true });
    fireEvent.change(description, { target: { value: "x".repeat(10001) } });
    await user.click(screen.getByRole("button", { name: "Salvar " + kind }));
    expect(await screen.findByText("A descrição deve ter no máximo 10.000 caracteres.")).toBeInTheDocument();
    expect(description).toHaveValue("x".repeat(10001));
    expect(save).not.toHaveBeenCalled();

    fireEvent.change(description, { target: { value: "x".repeat(10000) } });
    await user.click(screen.getByRole("button", { name: "Salvar " + kind }));
    await waitFor(() => expect(save).toHaveBeenCalledOnce());
  });

  it("valida o limite de e-mail e aceita um endereço de 254 caracteres", async () => {
    const send = vi.fn(() => HttpResponse.json({}));
    server.use(http.post(origin + "/api/users/forgot-password", send));
    const { user } = renderApp("/recuperar-senha");
    const email = screen.getByRole("textbox", { name: "E-mail", exact: true });
    const address = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + "." + "d".repeat(56) + ".test";
    fireEvent.change(email, { target: { value: "a" + address } });
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByText("O e-mail deve ter no máximo 254 caracteres.")).toBeInTheDocument();
    expect(send).not.toHaveBeenCalled();

    fireEvent.change(email, { target: { value: address } });
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    expect(await screen.findByRole("heading", { name: "Redefinir senha" })).toBeInTheDocument();
    expect(send).toHaveBeenCalledOnce();
  });

  it("valida o limite da nova senha e aceita 4096 caracteres", async () => {
    const reset = vi.fn(() => HttpResponse.json({}));
    server.use(
      http.post(origin + "/api/users/forgot-password", () => HttpResponse.json({})),
      http.post(origin + "/api/users/reset-password", reset),
    );
    const { user } = renderApp("/recuperar-senha");
    await user.type(screen.getByLabelText("E-mail"), "test@example.test");
    await user.click(screen.getByRole("button", { name: "Enviar código" }));
    await user.type(await screen.findByLabelText("Código de recuperação"), "codigo-teste");
    const password = screen.getByLabelText("Nova senha", { exact: true });
    const confirmation = screen.getByLabelText("Confirme a nova senha");
    fireEvent.change(password, { target: { value: "x".repeat(4097) } });
    fireEvent.change(confirmation, { target: { value: "x".repeat(4097) } });
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByText("A senha deve ter no máximo 4096 caracteres.")).toBeInTheDocument();
    expect(reset).not.toHaveBeenCalled();

    fireEvent.change(password, { target: { value: "x".repeat(4096) } });
    fireEvent.change(confirmation, { target: { value: "x".repeat(4096) } });
    await user.click(screen.getByRole("button", { name: "Redefinir senha" }));
    expect(await screen.findByRole("heading", { name: "Senha atualizada" })).toBeInTheDocument();
    expect(reset).toHaveBeenCalledOnce();
  });
});
