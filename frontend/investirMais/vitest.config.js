import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  test: {
    environment: "jsdom",
    environmentOptions: { jsdom: { url: "http://localhost:5173" } },
    env: { VITE_API_BASE_URL: "" },
    include: ["src/test/{unit,integration}/**/*.test.{js,jsx}"],
    setupFiles: ["./src/test/setup.js"],
    clearMocks: true,
    restoreMocks: true,
    unstubEnvs: true,
    coverage: {
      provider: "v8",
      reportsDirectory: "./reports/coverage",
      include: ["src/**/*.{js,jsx,ts}"],
      exclude: ["src/main.jsx", "src/generated/**", "src/test/**"],
      reporter: ["text-summary", "html"],
      thresholds: { statements: 85, branches: 80, functions: 85, lines: 85 },
    },
  },
});
