/**
 * Converte um texto em formato Markdown em seções compatíveis com a estrutura
 * de Documentos Legais da plataforma (heading e content).
 *
 * Suporta:
 * # Título Principal (opcional, pode atualizar o título do documento)
 * ## Título de Seção (ou ###)
 * Conteúdo em texto / parágrafos
 */
export function markdownToSections(mdText) {
  if (!mdText || !mdText.trim()) {
    return [{ heading: "Geral", content: "—" }];
  }

  const lines = mdText.split(/\r?\n/);
  const sections = [];
  let currentHeading = "";
  let currentContent = [];

  for (const line of lines) {
    const headingMatch = line.match(/^#{1,4}\s+(.+)$/);

    if (headingMatch) {
      if (currentHeading || currentContent.length > 0) {
        const text = currentContent.join("\n").trim();
        if (text || currentHeading) {
          sections.push({
            heading: currentHeading || "Introdução",
            content: text || "—",
          });
        }
        currentContent = [];
      }
      currentHeading = headingMatch[1].trim();
    } else {
      currentContent.push(line);
    }
  }

  if (currentHeading || currentContent.length > 0) {
    const text = currentContent.join("\n").trim();
    if (text || currentHeading) {
      sections.push({
        heading: currentHeading || "Geral",
        content: text || "—",
      });
    }
  }

  return sections.length > 0 ? sections : [{ heading: "Geral", content: mdText.trim() || "—" }];
}

/**
 * Converte a lista de seções de um documento legal em texto Markdown (.md).
 */
export function sectionsToMarkdown(sections = []) {
  if (!Array.isArray(sections) || sections.length === 0) return "";
  return sections
    .map((sec) => `## ${sec.heading || "Seção"}\n\n${sec.content || ""}`)
    .join("\n\n")
    .trim();
}

