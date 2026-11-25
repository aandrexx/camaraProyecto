import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // Backend de Spring Boot. Se puede cambiar con BACKEND_URL en frontend/.env.local
  const backend = env.BACKEND_URL || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      // En desarrollo, Vite reenvía las llamadas al backend: así no hace falta tocar CORS.
      proxy: {
        '/api': { target: backend, changeOrigin: true },
        '/auth': { target: backend, changeOrigin: true },
      },
    },
  }
})
