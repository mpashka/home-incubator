import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // Proxy API calls to the Quarkus backend during development.
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});
