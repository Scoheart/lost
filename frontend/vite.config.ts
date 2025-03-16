import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 3333,
    host: true,
    proxy: {
      '/api': {
        target: 'http://192.168.10.56:8080',
        changeOrigin: true,
      },
    },
  },
  build: {
    // 更好的处理资源错误和警告
    reportCompressedSize: false,
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      // 优化静态资源分组
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          ui: ['element-plus', '@element-plus/icons-vue'],
        }
      }
    }
  },
  // 导入静态资源自动转换为模块
  assetsInclude: ['**/*.svg', '**/*.png', '**/*.jpg', '**/*.jpeg', '**/*.gif'],
})
