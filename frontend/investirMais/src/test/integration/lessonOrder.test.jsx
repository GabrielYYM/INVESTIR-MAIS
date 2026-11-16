import { screen, waitFor, within } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { course, lesson, otherLesson, paged, student, teacher, adminUser } from "../fixtures.js";
import { mockApi } from "../helpers.js";
import { renderApp } from "../renderApp.jsx";

const path = "/educacao/" + course.id;
const endpoint = `/api/lessons/course/${course.id}/order`;

describe("ordem didática das aulas", () => {
  it.each([teacher, adminUser])("permite ao professor responsável e ao admin salvar a ordem de todas as páginas %#", async session => {
    let items = Array.from({ length: 12 }, (_, index) => ({
      ...lesson, id: `11111111-1111-4111-8111-${String(index).padStart(12, "0")}`,
      title: `Vídeo ${index + 1}`, position: index + 1,
    }));
    mockApi("get", endpoint, () => items);
    mockApi("get", `/api/lessons/course/${course.id}`, () => paged(items.slice(0, 10), { totalPages: 2, totalElements: 12 }));
    const save = mockApi("put", endpoint, async ({ request }) => {
      const { lessonIds } = await request.json();
      expect(lessonIds).toHaveLength(12);
      expect(lessonIds[0]).toBe(items[1].id);
      items = lessonIds.map((id, index) => ({ ...items.find(item => item.id === id), position: index + 1 }));
      return null;
    }, { status: 204 });
    const view = renderApp(path, { session });
    await view.user.click(await screen.findByRole("button", { name: "Ordenar aulas" }));
    let dialog = within(await screen.findByRole("dialog"));
    await dialog.findByText("Vídeo 12");
    expect(dialog.getByRole("button", { name: "Subir aula Vídeo 1" })).toBeDisabled();
    expect(dialog.getByRole("button", { name: "Descer aula Vídeo 12" })).toBeDisabled();
    expect(dialog.getByRole("button", { name: "Salvar ordem" })).toBeDisabled();
    await view.user.click(dialog.getByRole("button", { name: "Subir aula Vídeo 2" }));
    await view.user.click(dialog.getByRole("button", { name: "Descer aula Vídeo 12" }));
    await view.user.click(dialog.getByRole("button", { name: "Salvar ordem" }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
    expect(save).toHaveBeenCalledOnce();
    await waitFor(() => expect(screen.getAllByRole("link", { name: /Vídeo/ })[0]).toHaveAccessibleName("Vídeo 2"));
    await view.user.click(screen.getByRole("button", { name: "Ordenar aulas" }));
    dialog = within(await screen.findByRole("dialog"));
    await waitFor(() => expect(within(dialog.getByRole("list", { name: "Ordem das aulas" })).getAllByRole("listitem")[0]).toHaveTextContent("Vídeo 2"));
    view.unmount();
    renderApp(path, { session: student });
    await screen.findByRole("heading", { name: course.name });
    expect(screen.getAllByRole("link", { name: /Vídeo/ })[0]).toHaveAccessibleName("Vídeo 2");
    expect(screen.queryByRole("button", { name: "Ordenar aulas" })).not.toBeInTheDocument();
  });

  it.each([student, { ...teacher, id: "outro-professor" }])("oculta a ordenação de alunos e de outros professores %#", async session => {
    renderApp(path, { session });
    await screen.findByRole("heading", { name: course.name });
    expect(screen.queryByRole("button", { name: "Ordenar aulas" })).not.toBeInTheDocument();
  });

  it("mantém a escolha ao falhar e permite recarregar uma lista desatualizada", async () => {
    mockApi("get", endpoint, [lesson, otherLesson]);
    mockApi("put", endpoint, { detail: "A lista de aulas foi alterada." }, { status: 409 });
    const { user } = renderApp(path, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Ordenar aulas" }));
    const dialog = within(await screen.findByRole("dialog"));
    await user.click(await dialog.findByRole("button", { name: "Subir aula " + otherLesson.title }));
    await user.click(dialog.getByRole("button", { name: "Salvar ordem" }));
    expect(await dialog.findByRole("alert")).toHaveTextContent("A lista de aulas foi alterada.");
    expect(dialog.getAllByRole("listitem")[0]).toHaveTextContent(otherLesson.title);
    await user.click(dialog.getByRole("button", { name: "Atualizar lista" }));
    await waitFor(() => expect(dialog.queryByRole("alert")).not.toBeInTheDocument());
    expect(dialog.getAllByRole("listitem")[0]).toHaveTextContent(lesson.title);
    expect(dialog.getByRole("button", { name: "Salvar ordem" })).toBeDisabled();
    await user.click(dialog.getByRole("button", { name: "Cancelar" }));
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("permite tentar novamente ao falhar a consulta", async () => {
    mockApi("get", endpoint, { detail: "Indisponível." }, { status: 503 });
    const { user } = renderApp(path, { session: teacher });
    await user.click(await screen.findByRole("button", { name: "Ordenar aulas" }));
    const dialog = within(await screen.findByRole("dialog"));
    expect(await dialog.findByRole("alert")).toHaveTextContent("Indisponível.");
    mockApi("get", endpoint, [lesson]);
    await user.click(dialog.getByRole("button", { name: "Atualizar lista" }));
    expect(await dialog.findByText(lesson.title)).toBeInTheDocument();
  });
});
