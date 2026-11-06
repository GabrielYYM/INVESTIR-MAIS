import { defineConfig } from "orval";

export default defineConfig({
  education: {
    input: "./education.openapi.json",
    output: {
      target: "./src/generated/education.ts",
      client: "axios-functions",
      override: {
        mutator: { path: "./src/services/educationHttp.ts", name: "educationRequest" },
      },
    },
  },
});
