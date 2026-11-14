import { defineConfig } from "orval";

export default defineConfig({
  account: {
    input: "./account.openapi.json",
    output: {
      target: "./src/generated/account.ts",
      client: "axios-functions",
      override: {
        mutator: { path: "./src/services/educationHttp.ts", name: "educationRequest" },
      },
    },
  },
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
