import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: Object.fromEntries(["/api", "/auth", "/csrf"].map((path) => [path, "http://localhost:8080"])),
  },
  test: { environment: "jsdom", restoreMocks: true, unstubGlobals: true },
});
