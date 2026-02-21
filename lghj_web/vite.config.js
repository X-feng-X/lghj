import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // Machine Learning Service (Port 8001)
      '/api/ml': {
        target: 'http://localhost:8001',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api\/ml/, '')
      },
      // Stock Data Service (Port 8002)
      '/api/user/stock/data': {
        target: 'http://localhost:8002',
        changeOrigin: true
      },
      // Main Service (Port 8080)
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})