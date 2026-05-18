import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
    plugins: [react()],
    // base: '/' asegura que los assets se referencien desde la raíz
    base: '/',
    server: {
        // Solo aplica en desarrollo local (npm run dev)
        // En producción Spring Boot sirve todo, no hay proxy
        proxy: {
            '/api': {
                target: 'http://localhost:8081',
                changeOrigin: true,
            },
        },
    },
    build: {
        // Donde Gradle va a buscar el resultado del build
        outDir: 'dist',
        emptyOutDir: true,
    },
})