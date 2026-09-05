import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Перенаправление `/api` на бэкенд (Spring Boot) — чтобы в разработке
// страница и данные шли с одного источника и не мешали ограничения браузера.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 8181,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8180',
        changeOrigin: true
      }
    }
  }
})
