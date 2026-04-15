import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000, // 前端启动端口，不冲突即可
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 后端SpringBoot端口（按你要求修改）
        changeOrigin: true
        
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src') // 路径别名@指向src
    }
  }
})