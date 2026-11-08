import { screen, waitFor, within } from "@testing-library/react";
import { http, HttpResponse } from "msw/http";
import { describe, expect, it, vi } from "vitest";
import { admin, course, lesson, otherLesson, paged, student, teacher } from "../fixtures.js";
import { renderApp } from "../renderApp.jsx";
import { origin, server } from "../server.js";

const coursePath = "/educacao/" + course.id;
function asUser(user) {
  server.use(http.get(origin + "/auth/me", () => HttpResponse.json(user)));
}

describe("catálogo e aulas", () => {
  it.each([[student, false], [teacher, true], [admin, true]])("mostra a criação de curso conforme o perfil %#", async (user, canCreate) => {
    asUser(user);
    renderApp("/educacao");
    await screen.findByRole("heading", { name: "Educação financeira" });
    expect(screen.getByRole("heading", { name: course.name })).toBeInTheDocument();
    if (canCreate) expect(screen.getByRole("button", { name: "Novo curso" })).toBeInTheDocument();
    else expect(screen.queryByRole("button", { name: "Novo curso" })).not.toBeInTheDocument();
  });

  it("carrega a próxima página do catálogo com os parâmetros do contrato", async () => {
    const pages = [];
    server.use(http.get(origin + "/api/courses", ({ request }) => {
      const query = new URL(request.url).searchParams;
      const number = Number(query.get("page"));
      pages.push(number);
      expect(query.get("size")).toBe("9");
      expect(query.getAll("sort")).toEqual(["name", "id"]);
      expect(query.has("sort[]")).toBe(false);
      return HttpResponse.json(paged([{ ...course, name: number === 0 ? "Renda fixa" : "Renda variável" }], { number, totalPages: 2, totalElements: 10 }));
    }));
    const { user, router } = renderApp("/educacao");
    await user.click(await screen.findByRole("link", { name: "Próxima" }));
    expect(await screen.findByRole("heading", { name: "Renda variável" })).toBeInTheDocument();
    expect(screen.getByText("Página 2 de 2")).toBeInTheDocument();
    expect(router.state.location.search).toBe("?pagina=2");
    expect(pages).toEqual([0, 1]);
  });

  it("corrige o endereço quando a página solicitada deixou de existir", async () => {
    const { router } = renderApp("/educacao?pagina=99");
    await screen.findByRole("heading", { name: "Educação financeira" });
    expect(router.state.location.search).toBe("");
    expect(router.state.historyAction).toBe("REPLACE");
  });

  it.each([[student, "Os cursos aparecerão aqui quando forem cadastrados."], [teacher, "Crie um curso para começar a adicionar aulas."]])("explica catálogo vazio ao perfil %#", async (user, message) => {
    asUser(user);
    server.use(http.get(origin + "/api/courses", () => HttpResponse.json(paged([]))));
    renderApp("/educacao");
    expect(await screen.findByRole("heading", { name: "O catálogo ainda está vazio" })).toBeInTheDocument();
    expect(screen.getByText(message)).toBeInTheDocument();
  });

  it("abre o player sob demanda e o reinicia ao selecionar outra aula", async () => {
    server.use(
      http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([lesson, otherLesson]))),
      http.get(origin + "/api/lessons/:id", ({ params }) => HttpResponse.json(params.id === otherLesson.id ? otherLesson : lesson)),
    );
    const { user, router } = renderApp(coursePath);
    await screen.findByRole("heading", { name: course.name });
    expect(screen.queryByTitle("Vídeo da aula " + lesson.title)).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Abrir no YouTube" })).toHaveAttribute("href", lesson.mediaUrl);
    await user.click(screen.getByRole("button", { name: "Abrir player" }));
    expect(screen.getByTitle("Vídeo da aula " + lesson.title)).toHaveAttribute("src", "https://www.youtube-nocookie.com/embed/dQw4w9WgXcQ");
    await user.click(screen.getByRole("link", { name: otherLesson.title }));
    expect(await screen.findByRole("heading", { name: otherLesson.title })).toBeInTheDocument();
    expect(screen.queryByTitle("Vídeo da aula " + otherLesson.title)).not.toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Abrir player" })).toBeInTheDocument();
    expect(router.state.location.pathname).toBe(coursePath + "/" + otherLesson.id);
  });

  it.each([[student, false], [{ ...teacher, id: "outro-professor" }, false], [teacher, true], [{ ...admin, id: "outro-admin" }, true]])("aplica propriedade do curso aos controles de gestão %#", async (user, canManage) => {
    asUser(user);
    renderApp(coursePath);
    await screen.findByRole("heading", { name: course.name });
    for (const name of ["Editar curso", "Excluir curso", "Nova aula", "Editar aula " + lesson.title, "Excluir aula " + lesson.title]) {
      if (canManage) expect(screen.getByRole("button", { name })).toBeInTheDocument();
      else expect(screen.queryByRole("button", { name })).not.toBeInTheDocument();
    }
  });

  it("recusa uma aula de outro curso mesmo com UUID válido", async () => {
    server.use(http.get(origin + "/api/lessons/:id", () => HttpResponse.json({ ...lesson, courseId: otherLesson.id })));
    renderApp(coursePath + "/" + lesson.id);
    expect(await screen.findByRole("heading", { name: "Página não encontrada" })).toBeInTheDocument();
  });

  it.each([
    ["https://videos.example.test/aula", true], ["javascript:alert(1)", false], [null, false],
  ])("exibe registros antigos sem incorporar endereço inseguro %#", async (mediaUrl, valid) => {
    server.use(http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([{ ...lesson, mediaUrl }]))));
    renderApp(coursePath);
    await screen.findByRole("heading", { name: lesson.title });
    expect(screen.queryByRole("button", { name: "Abrir player" })).not.toBeInTheDocument();
    if (valid) expect(screen.getByRole("link", { name: "Abrir vídeo" })).toHaveAttribute("href", mediaUrl);
    else expect(screen.getByText("O endereço do vídeo está indisponível.")).toBeInTheDocument();
  });

  it.each([[student, "Este curso ainda não tem aulas cadastradas."], [teacher, "Adicione uma videoaula para começar."]])("explica curso vazio ao perfil %#", async (user, message) => {
    asUser(user);
    server.use(http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([]))));
    renderApp(coursePath);
    expect(await screen.findByText(message)).toBeInTheDocument();
  });
});

describe("gestão de cursos", () => {
  it("volta à mesma aula e página após perder a sessão durante a edição", async () => {
    asUser(teacher);
    server.use(
      http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([lesson], { number: 1, totalPages: 2, totalElements: 11 }))),
      http.put(origin + "/api/courses/:id", () => HttpResponse.json({}, { status: 401 })),
      http.post(origin + "/auth/login", () => new HttpResponse(null, { status: 204 })),
      http.post(origin + "/auth/verify-2fa", () => new HttpResponse(null, { status: 204 })),
    );
    const destination = coursePath + "/" + lesson.id + "?pagina=2";
    const { user, router } = renderApp(destination);
    await user.click(await screen.findByRole("button", { name: "Editar curso" }));
    await user.click(screen.getByRole("button", { name: "Salvar curso" }));
    await screen.findByRole("heading", { name: "Bem-vindo de volta" });
    expect(new URLSearchParams(router.state.location.search).get("retorno")).toBe(destination);
    await user.type(screen.getByLabelText("E-mail"), teacher.email);
    await user.type(screen.getByLabelText("Senha", { exact: true }), "senha-teste");
    await user.click(screen.getByRole("button", { name: "Continuar" }));
    await user.click(await screen.findByText("Usar código manualmente"));
    await user.type(screen.getByLabelText("Código de verificação"), "codigo-teste");
    await user.click(screen.getByRole("button", { name: "Confirmar acesso" }));
    await screen.findByRole("heading", { name: course.name, level: 1 });
    expect(router.state.location.pathname + router.state.location.search).toBe(destination);
  });

  it("valida, cria o curso e navega para seu detalhe", async () => {
    asUser(teacher);
    const payloads = [];
    server.use(http.post(origin + "/api/courses", async ({ request }) => {
      payloads.push(await request.json());
      return HttpResponse.json(course, { status: 201 });
    }));
    const { user, router } = renderApp("/educacao");
    await user.click(await screen.findByRole("button", { name: "Novo curso" }));
    const dialog = within(screen.getByRole("dialog", { name: "Novo curso" }));
    await user.click(dialog.getByRole("button", { name: "Salvar curso" }));
    expect(await dialog.findByText("Informe um título.")).toBeInTheDocument();
    expect(dialog.getByText("Informe uma descrição.")).toBeInTheDocument();
    expect(payloads).toHaveLength(0);
    await user.type(dialog.getByLabelText("Nome do curso"), "  Renda fixa  ");
    await user.type(dialog.getByLabelText("Descrição"), "  Aprenda sobre investimentos.  ");
    await user.click(dialog.getByRole("button", { name: "Salvar curso" }));
    expect(await screen.findByRole("heading", { name: course.name, level: 1 })).toBeInTheDocument();
    expect(payloads).toEqual([{ name: "Renda fixa", description: "Aprenda sobre investimentos." }]);
    expect(router.state.location.pathname).toBe(coursePath);
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("fecha o formulário ao cancelar e pelo botão de fechar", async () => {
    asUser(teacher);
    const create = vi.fn();
    server.use(http.post(origin + "/api/courses", create));
    const { user } = renderApp("/educacao");
    await user.click(await screen.findByRole("button", { name: "Novo curso" }));
    await user.click(screen.getByRole("button", { name: "Cancelar" }));
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Novo curso" }));
    await user.click(screen.getByRole("button", { name: "Fechar" }));
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
    expect(create).not.toHaveBeenCalled();
  });

  it("mantém o diálogo e os dados quando a API recusa a criação", async () => {
    asUser(teacher);
    server.use(http.post(origin + "/api/courses", () => HttpResponse.json({ detail: "Sem permissão para criar." }, { status: 403 })));
    const { user } = renderApp("/educacao");
    await user.click(await screen.findByRole("button", { name: "Novo curso" }));
    await user.type(screen.getByLabelText("Nome do curso"), "Curso de teste");
    await user.type(screen.getByLabelText("Descrição"), "Descrição de teste");
    await user.click(screen.getByRole("button", { name: "Salvar curso" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("Sem permissão para criar.");
    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByLabelText("Nome do curso")).toHaveValue("Curso de teste");
  });

  it("edita o curso, revalida o detalhe e fecha o diálogo", async () => {
    asUser(teacher);
    let currentCourse = { ...course };
    server.use(
      http.get(origin + "/api/courses/:id", () => HttpResponse.json(currentCourse)),
      http.put(origin + "/api/courses/:id", async ({ params, request }) => {
        expect(params.id).toBe(course.id);
        currentCourse = { ...currentCourse, ...await request.json() };
        return HttpResponse.json(currentCourse);
      }),
    );
    const { user } = renderApp(coursePath);
    await user.click(await screen.findByRole("button", { name: "Editar curso" }));
    const field = screen.getByLabelText("Nome do curso");
    expect(field).toHaveValue(course.name);
    await user.clear(field);
    await user.type(field, "Renda fixa avançada");
    await user.click(screen.getByRole("button", { name: "Salvar curso" }));
    expect(await screen.findByRole("heading", { name: "Renda fixa avançada" })).toBeInTheDocument();
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("só exclui após confirmação e volta ao catálogo", async () => {
    asUser(teacher);
    let deleted = false;
    const remove = vi.fn(() => { deleted = true; return new HttpResponse(null, { status: 204 }); });
    server.use(
      http.delete(origin + "/api/courses/:id", remove),
      http.get(origin + "/api/courses", () => HttpResponse.json(paged(deleted ? [] : [course]))),
    );
    const { user, router } = renderApp(coursePath);
    await user.click(await screen.findByRole("button", { name: "Excluir curso" }));
    expect(remove).not.toHaveBeenCalled();
    expect(screen.getByRole("dialog", { name: "Excluir curso" })).toHaveTextContent("O curso e suas aulas deixarão de aparecer");
    await user.click(screen.getByRole("button", { name: "Confirmar exclusão" }));
    expect(await screen.findByRole("heading", { name: "O catálogo ainda está vazio" })).toBeInTheDocument();
    expect(remove).toHaveBeenCalledOnce();
    expect(router.state.location.pathname).toBe("/educacao");
  });
});

describe("gestão de aulas", () => {
  it("valida a URL e cria uma aula com vídeo e miniatura normalizados", async () => {
    asUser(teacher);
    const payloads = [];
    server.use(
      http.post(origin + "/api/lessons/course/:id", async ({ params, request }) => {
        expect(params.id).toBe(course.id);
        payloads.push(await request.json());
        return HttpResponse.json(otherLesson, { status: 201 });
      }),
      http.get(origin + "/api/lessons/:id", () => HttpResponse.json(otherLesson)),
    );
    const { user, router } = renderApp(coursePath);
    await user.click(await screen.findByRole("button", { name: "Nova aula" }));
    await user.type(screen.getByLabelText("Título da aula"), "  Reserva de emergência  ");
    await user.type(screen.getByLabelText("Descrição"), "  Aprenda a criar sua reserva.  ");
    await user.type(screen.getByLabelText("URL do vídeo no YouTube"), "https://evil.example.test/video");
    await user.click(screen.getByRole("button", { name: "Salvar aula" }));
    expect(await screen.findByText("Informe o link de um vídeo do YouTube.")).toBeInTheDocument();
    expect(payloads).toHaveLength(0);
    await user.clear(screen.getByLabelText("URL do vídeo no YouTube"));
    await user.type(screen.getByLabelText("URL do vídeo no YouTube"), "  https://youtu.be/dQw4w9WgXcQ?t=10  ");
    await user.click(screen.getByRole("button", { name: "Salvar aula" }));
    expect(await screen.findByRole("heading", { name: otherLesson.title })).toBeInTheDocument();
    expect(payloads).toEqual([{
      title: "Reserva de emergência", description: "Aprenda a criar sua reserva.",
      mediaUrl: lesson.mediaUrl, thumbnailUrl: lesson.thumbnailUrl,
    }]);
    expect(router.state.location.pathname).toBe(coursePath + "/" + otherLesson.id);
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("preserva mídia de registro antigo ao editar somente o título", async () => {
    asUser(teacher);
    let currentLesson = { ...lesson, mediaUrl: "https://videos.example.test/legado" };
    const payloads = [];
    server.use(
      http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged([currentLesson]))),
      http.put(origin + "/api/lessons/:id", async ({ params, request }) => {
        expect(params.id).toBe(lesson.id);
        const body = await request.json();
        payloads.push(body);
        currentLesson = { ...currentLesson, ...body };
        return HttpResponse.json(currentLesson);
      }),
    );
    const { user } = renderApp(coursePath);
    await user.click(await screen.findByRole("button", { name: "Editar aula " + lesson.title }));
    await user.clear(screen.getByLabelText("Título da aula"));
    await user.type(screen.getByLabelText("Título da aula"), "Aula atualizada");
    await user.click(screen.getByRole("button", { name: "Salvar aula" }));
    expect(await screen.findByRole("heading", { name: "Aula atualizada" })).toBeInTheDocument();
    expect(payloads).toEqual([{ title: "Aula atualizada", description: lesson.description, mediaUrl: "https://videos.example.test/legado", thumbnailUrl: lesson.thumbnailUrl }]);
    await waitFor(() => expect(screen.queryByRole("dialog")).not.toBeInTheDocument());
  });

  it("exclui a aula selecionada e exibe o estado vazio do curso", async () => {
    asUser(teacher);
    let deleted = false;
    server.use(
      http.get(origin + "/api/lessons/course/:id", () => HttpResponse.json(paged(deleted ? [] : [lesson]))),
      http.delete(origin + "/api/lessons/:id", ({ params }) => {
        expect(params.id).toBe(lesson.id);
        deleted = true;
        return new HttpResponse(null, { status: 204 });
      }),
    );
    const { user, router } = renderApp(coursePath + "/" + lesson.id);
    await user.click(await screen.findByRole("button", { name: "Excluir aula " + lesson.title }));
    expect(screen.getByRole("dialog", { name: "Excluir aula" })).toHaveTextContent("A aula deixará de aparecer no curso.");
    await user.click(screen.getByRole("button", { name: "Confirmar exclusão" }));
    expect(await screen.findByText("Adicione uma videoaula para começar.")).toBeInTheDocument();
    expect(router.state.location.pathname).toBe(coursePath);
  });
});
