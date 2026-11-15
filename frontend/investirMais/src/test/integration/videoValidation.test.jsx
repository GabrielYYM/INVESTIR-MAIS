import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { adminUser, teacherUser, student, course, lesson, paged } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

const channelId = "UCabcdefghijklmnopqrstuv";
const endpoint = `/api/teachers/${teacherUser.id}/youtube-channels`;
const approval = { id: "cccccccc-cccc-4ccc-8ccc-cccccccccccc", channelId, title: "Economia para todos", approvedAt: "2026-11-15T12:00:00Z" };
async function openChannels() {
  mockApi("get", "/api/users", [adminUser, teacherUser, student]);
  const view = renderApp("/admin", { session: adminUser });
  await view.user.click(await screen.findByRole("button", { name: "Canais de vídeo" }));
  return { ...view, dialog: within(await screen.findByRole("dialog", { name: "Canais de vídeo do professor" })) };
}

describe("validação e aprovação dos vídeos", () => {
  it("permite ao administrador aprovar um canal por professor e confirmar a revogação", async () => {
    let channels = [];
    mockApi("get", endpoint, () => channels);
    const add = mockApi("post", endpoint, async ({ request }) => { expect(await request.json()).toEqual({ channelId }); channels = [approval]; return approval; }, { status: 201 });
    const remove = mockApi("delete", endpoint + "/" + approval.id, () => { channels = []; return null; }, { status: 204 });
    const { user, dialog } = await openChannels();
    await dialog.findByText("Nenhum canal aprovado para este professor.");
    await user.click(dialog.getByRole("button", { name: "Aprovar canal" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("24 caracteres");
    expect(add).not.toHaveBeenCalled();
    await user.type(dialog.getByLabelText("ID do canal do YouTube"), channelId);
    await user.click(dialog.getByRole("button", { name: "Aprovar canal" }));
    expect(await dialog.findByRole("link", { name: approval.title })).toHaveAttribute("href", "https://www.youtube.com/channel/" + channelId);
    expect(add).toHaveBeenCalledOnce();
    await user.click(dialog.getByRole("button", { name: "Revogar canal " + approval.title }));
    expect(remove).not.toHaveBeenCalled();
    await user.click(dialog.getByRole("button", { name: "Cancelar revogação" }));
    expect(dialog.queryByRole("button", { name: "Confirmar revogação" })).not.toBeInTheDocument();
    await user.click(dialog.getByRole("button", { name: "Revogar canal " + approval.title }));
    await user.click(dialog.getByRole("button", { name: "Confirmar revogação" }));
    expect(await dialog.findByText("Nenhum canal aprovado para este professor.")).toBeInTheDocument();
    expect(remove).toHaveBeenCalledOnce();
    await user.click(dialog.getByRole("button", { name: "Fechar", exact: true }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("mostra a falha de configuração da validação e mantém o canal informado", async () => {
    mockApi("get", endpoint, []);
    mockApi("post", endpoint, { detail: "A validação do YouTube não está configurada." }, { status: 503 });
    const { user, dialog } = await openChannels();
    await user.type(dialog.getByLabelText("ID do canal do YouTube"), channelId);
    await user.click(dialog.getByRole("button", { name: "Aprovar canal" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("não está configurada");
    expect(dialog.getByLabelText("ID do canal do YouTube")).toHaveValue(channelId);
  });

  it("permite repetir a consulta dos canais quando a API está indisponível", async () => {
    mockApi("get", endpoint, { detail: "Consulta indisponível." }, { status: 503 });
    const { user, dialog } = await openChannels();
    expect(await dialog.findByRole("alert")).toHaveTextContent("Consulta indisponível.");
    mockApi("get", endpoint, [approval]);
    await user.click(dialog.getByRole("button", { name: "Tentar novamente" }));
    expect(await dialog.findByRole("link", { name: approval.title })).toBeInTheDocument();
  });

  it("não oferece reprodução ou conclusão de um vídeo não liberado e permite verificar novamente", async () => {
    mockApi("get", `/api/lessons/course/${course.id}`, paged([{ ...lesson, mediaUrl: null, thumbnailUrl: null, videoIssue: "Este vídeo está indisponível ou ainda não foi validado." }]));
    const { user } = renderApp("/educacao/" + course.id, { session: student });
    expect(await screen.findByRole("heading", { name: "Vídeo indisponível" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Abrir player" })).not.toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "Abrir no YouTube" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Marcar como assistida" })).not.toBeInTheDocument();
    mockApi("get", `/api/lessons/course/${course.id}`, paged([lesson]));
    await user.click(screen.getByRole("button", { name: "Tentar verificar novamente" }));
    expect(await screen.findByRole("button", { name: "Abrir player" })).toBeInTheDocument();
  });
});
