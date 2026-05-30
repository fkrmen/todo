import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) {
            return
          }

          if (
            id.includes('element-plus') ||
            id.includes('@element-plus/icons-vue') ||
            id.includes('@floating-ui') ||
            id.includes('popperjs') ||
            id.includes('async-validator')
          ) {
            return 'element-plus'
          }

          if (id.includes('@vueuse')) {
            return 'vueuse'
          }

          if (id.includes('dayjs')) {
            return 'dayjs'
          }

          if (id.includes('lodash')) {
            return 'lodash'
          }

          if (id.includes('vue-router')) {
            return 'vue-router'
          }

          if (id.includes('/vue/')) {
            return 'vue'
          }

          if (id.includes('axios')) {
            return 'axios'
          }
        }
      }
    }
  }
})
