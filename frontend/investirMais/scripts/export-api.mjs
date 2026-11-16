import { readFile, writeFile } from "node:fs/promises";

let document;
if (process.env.API_DOCS_FILE) document = JSON.parse(await readFile(process.env.API_DOCS_FILE, "utf8"));
else {
  const response = await fetch(new URL("/v3/api-docs", process.env.API_DOCS_ORIGIN || "http://localhost:8080"));
  if (!response.ok) throw new Error(`OpenAPI retornou HTTP ${response.status}`);
  document = await response.json();
}
delete document.servers;
async function exportContract(name, include) {
  const paths = Object.fromEntries(Object.entries(document.paths).filter(([path]) => include(path)));
  const schemas = document.components?.schemas || {}, used = new Set();
  function collect(value) {
    if (!value || typeof value !== "object") return;
    if (value.$ref?.startsWith("#/components/schemas/")) {
      const name = value.$ref.split("/").at(-1);
      if (!used.has(name)) { used.add(name); collect(schemas[name]); }
    }
    Object.values(value).forEach(collect);
  }
  collect(paths);
  const output = { ...document, paths, components: { ...document.components,
    schemas: Object.fromEntries(Object.entries(schemas).filter(([name]) => used.has(name))),
  } };
  await writeFile(new URL(`../${name}.openapi.json`, import.meta.url), JSON.stringify(output, null, 2) + "\n");
}
await exportContract("account", path => path.startsWith("/api/users") || path.startsWith("/api/legal/") || path.startsWith("/api/teacher-applications") || path === "/api/audit" || path === "/auth/me" || path === "/api/teacher/dashboard");
await exportContract("education", path => path.startsWith("/api/courses") || path.startsWith("/api/lessons") || path.startsWith("/api/learning"));
console.log("Contratos de conta e educação atualizados.");
