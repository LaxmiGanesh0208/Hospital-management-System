import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // Keep Vite's generated cache outside node_modules so Windows/OneDrive ACLs
  // on installed packages cannot prevent the dev server from starting.
  cacheDir: './.vite-cache',
  server: {
    proxy: {
      '/auth': 'http://localhost:4004',
      '/api': 'http://localhost:4004',
    },
  },
})
