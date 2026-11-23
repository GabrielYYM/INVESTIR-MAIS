import { describe, expect, it } from "vitest";
import { markdownToSections, sectionsToMarkdown } from "../../services/legalMarkdown.js";

describe("legalMarkdown", () => {
  it("converte seções existentes em markdown", () => {
    const sections = [
      { heading: "1. Termos de Uso", content: "Conteúdo dos termos." },
      { heading: "2. Isenção", content: "Isenção financeira." },
    ];
    const md = sectionsToMarkdown(sections);
    expect(md).toContain("## 1. Termos de Uso\n\nConteúdo dos termos.");
    expect(md).toContain("## 2. Isenção\n\nIsenção financeira.");
  });

  it("converte markdown estruturado em seções com heading e content", () => {
    const md = `
## 1. Aceitação dos Termos
Ao se cadastrar você aceita tudo.

## 2. Privacidade e Dados
Tratamos dados conforme a LGPD.
`;
    const sections = markdownToSections(md);
    expect(sections).toHaveLength(2);
    expect(sections[0]).toEqual({
      heading: "1. Aceitação dos Termos",
      content: "Ao se cadastrar você aceita tudo.",
    });
    expect(sections[1]).toEqual({
      heading: "2. Privacidade e Dados",
      content: "Tratamos dados conforme a LGPD.",
    });
  });

  it("lida com texto simples sem headers usando seção Geral", () => {
    const md = "Texto único sem títulos de seção.";
    const sections = markdownToSections(md);
    expect(sections).toEqual([
      { heading: "Geral", content: "Texto único sem títulos de seção." },
    ]);
  });
});
