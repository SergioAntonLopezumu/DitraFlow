import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Esto hace que escuche en la red local (0.0.0.0)
    allowedHosts: true // Permite que CUALQUIER dominio o host se conecte
  }
})