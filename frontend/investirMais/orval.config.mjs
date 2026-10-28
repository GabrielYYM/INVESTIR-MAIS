import { defineConfig } from "orval";

export default defineConfig({
  spring: {
    input: { target: "../../target/openapi.json" },
    output: {
      target: "./src/generated/spring.ts",
      client: "fetch",
      override: {
        mutator: { path: "./src/services/api.js", name: "apiRequest" },
        fetch: { includeHttpResponseReturnType: false },
      },
    },
  },
});
