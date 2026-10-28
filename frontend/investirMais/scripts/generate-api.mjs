import { spawnSync } from "node:child_process";
import { fileURLToPath } from "node:url";
import { generate } from "orval";

const project = fileURLToPath(new URL("../../../", import.meta.url));
const parameters = ["-q", "-Dtest=OpenApiContractTest", "-Dopenapi.output=target/openapi.json", "test"];
const windows = process.platform === "win32";
const result = spawnSync(windows ? "cmd.exe" : "./mvnw", windows ? ["/d", "/c", "mvnw.cmd", ...parameters] : parameters, { cwd: project, stdio: "inherit" });
if (result.error) throw result.error;
if (result.status !== 0) process.exit(result.status || 1);
await generate("./orval.config.mjs");
