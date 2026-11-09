import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router";
import { describe, expect, it } from "vitest";
import { Field } from "../../components/FormFields.jsx";
import { CourseCard, Pagination } from "../../components/EducationParts.jsx";
import { Notice, PendingPage } from "../../components/PageState.jsx";
import { course } from "../fixtures.js";

describe("campos de formulário", () => {
  it("associa rótulo e dica ao campo", () => {
    render(<Field label="E-mail" type="email" hint="Use seu endereço pessoal." />);
    const input = screen.getByRole("textbox", { name: "E-mail" });
    expect(input).toHaveAccessibleDescription("Use seu endereço pessoal.");
    expect(input).not.toHaveAttribute("aria-invalid");
  });

  it("prioriza a mensagem de validação e marca o campo inválido", () => {
    render(<Field label="Descrição" multiline hint="Uma dica" error="Informe uma descrição." />);
    const input = screen.getByRole("textbox", { name: "Descrição" });
    expect(input.tagName).toBe("TEXTAREA");
    expect(input).toHaveAttribute("aria-invalid", "true");
    expect(input).toHaveAccessibleDescription("Informe uma descrição.");
    expect(screen.queryByText("Uma dica")).not.toBeInTheDocument();
  });

  it("alterna a visibilidade da senha preservando seu valor", async () => {
    const user = userEvent.setup();
    render(<Field label="Senha" type="password" defaultValue="senha-teste" />);
    const input = screen.getByLabelText("Senha");
    expect(input).toHaveAttribute("type", "password");
    await user.click(screen.getByRole("button", { name: "Exibir senha" }));
    expect(input).toHaveAttribute("type", "text");
    expect(input).toHaveValue("senha-teste");
    await user.click(screen.getByRole("button", { name: "Ocultar senha" }));
    expect(input).toHaveAttribute("type", "password");
  });
});

describe("componentes de navegação e estado", () => {
  it("cria o link de detalhe com os dados do curso", () => {
    render(<MemoryRouter><CourseCard course={course} /></MemoryRouter>);
    expect(screen.getByRole("link", { name: /Renda fixa/ })).toHaveAttribute("href", "/educacao/" + course.id);
    expect(screen.getByText(course.description)).toBeInTheDocument();
  });

  it.each([0, 1])("oculta paginação com %i páginas", (totalPages) => {
    render(<MemoryRouter><Pagination page={{ totalPages, number: 0 }} pathname="/educacao" /></MemoryRouter>);
    expect(screen.queryByRole("navigation", { name: "Paginação" })).not.toBeInTheDocument();
  });

  it.each([
    [0, null, "/educacao?pagina=2"],
    [1, "/educacao", "/educacao?pagina=3"],
    [2, "/educacao?pagina=2", null],
  ])("limita os links à página disponível %i", (number, previous, next) => {
    render(<MemoryRouter><Pagination page={{ totalPages: 3, number }} pathname="/educacao" /></MemoryRouter>);
    expect(screen.getByText(`Página ${number + 1} de 3`)).toBeInTheDocument();
    if (previous) expect(screen.getByRole("link", { name: "Anterior" })).toHaveAttribute("href", previous);
    else expect(screen.queryByRole("link", { name: "Anterior" })).not.toBeInTheDocument();
    if (next) expect(screen.getByRole("link", { name: "Próxima" })).toHaveAttribute("href", next);
    else expect(screen.queryByRole("link", { name: "Próxima" })).not.toBeInTheDocument();
  });

  it("anuncia avisos, erros e carregamento com semântica acessível", () => {
    render(<><Notice>Salvo.</Notice><Notice error>Falhou.</Notice><PendingPage /></>);
    expect(screen.getByRole("status")).toHaveTextContent("Salvo.");
    expect(screen.getByRole("alert")).toHaveTextContent("Falhou.");
    expect(screen.getByLabelText("Carregando")).toBeInTheDocument();
  });
});
