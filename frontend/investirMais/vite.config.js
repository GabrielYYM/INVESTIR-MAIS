import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig(({ mode }) => {
  const { VITE_DEV_API_TARGET = "http://localhost:8080" } = loadEnv(mode, process.cwd());
  const proxy = Object.fromEntries(["/api", "/auth", "/csrf"].map((path) => [path, VITE_DEV_API_TARGET]));

  return {
    plugins: [react(), tailwindcss()],
    server: { port: 5173, proxy },
    preview: { proxy },
  };
});
