import { writeFile } from "node:fs/promises";

// Export only the documented account/dashboard API; login/OTT remain Spring Security endpoints.
const response = await fetch(new URL("/v3/api-docs", process.env.API_DOCS_ORIGIN || "http://localhost:8080"));
if (!response.ok) throw new Error(`OpenAPI retornou HTTP ${response.status}`);
const { servers, ...document } = await response.json();
document.paths = Object.fromEntries(Object.entries(document.paths).filter(([path]) =>
  path.startsWith("/api/users") || path === "/auth/me" || path === "/api/teacher/dashboard"));
const schemas = document.components?.schemas || {};
const used = new Set();
function collect(value) {
  if (!value || typeof value !== "object") return;
  if (value.$ref?.startsWith("#/components/schemas/")) {
    const name = value.$ref.split("/").at(-1);
    if (!used.has(name)) { used.add(name); collect(schemas[name]); }
  }
  Object.values(value).forEach(collect);
}
collect(document.paths);
document.components.schemas = Object.fromEntries(Object.entries(schemas).filter(([name]) => used.has(name)));
await writeFile(new URL("../account.openapi.json", import.meta.url), JSON.stringify(document, null, 2) + "\n");
console.log("Contrato de conta e painel atualizado em account.openapi.json");
