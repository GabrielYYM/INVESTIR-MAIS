import { defineConfig } from "orval";

export default defineConfig({
  account: {
    input: "./account.openapi.json",
    output: {
      target: "./src/generated/account.ts",
      client: "react-query",
      httpClient: "axios",
      override: {
        mutator: { path: "./src/services/apiRequest.ts", name: "apiRequest" },
      },
    },
  },
  education: {
    input: "./education.openapi.json",
    output: {
      target: "./src/generated/education.ts",
      client: "react-query",
      httpClient: "axios",
      override: {
        mutator: { path: "./src/services/apiRequest.ts", name: "apiRequest" },
      },
    },
  },
  accountValidation: {
    input: "./account.openapi.json",
    output: { target: "./src/generated/account.zod.ts", client: "zod", override: { zod: { version: 4 } } },
  },
  educationValidation: {
    input: "./education.openapi.json",
    output: { target: "./src/generated/education.zod.ts", client: "zod", override: { zod: { version: 4 } } },
  },
});
