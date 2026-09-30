import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// In dev the API is proxied, so the app and the API share an origin (no CORS, cookies just work).
// In prod the API lives on its own host, set via VITE_API_BASE_URL at build time.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/admin': 'http://localhost:8080',
    },
  },
})
