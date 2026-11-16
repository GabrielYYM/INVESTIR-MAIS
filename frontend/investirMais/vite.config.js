import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig(({ mode }) => {
  const { VITE_DEV_API_TARGET = "http://localhost:8080" } = loadEnv(mode, process.cwd());
  const proxy = Object.fromEntries(["/api", "/auth", "/csrf"].map((path) => [path, VITE_DEV_API_TARGET]));

  return {
    plugins: [react(), tailwindcss()],
    build: { rolldownOptions: { output: { codeSplitting: {
      groups: [{ name: "framework", test: /node_modules[\\/](?:react|react-dom|react-router|react-router-dom|ra-core|@tanstack)[\\/]/, maxSize: 250_000 }],
    } } } },
    server: { port: 5173, proxy, watch: { ignored: ["**/reports/**"] } },
    preview: { proxy },
  };
});
