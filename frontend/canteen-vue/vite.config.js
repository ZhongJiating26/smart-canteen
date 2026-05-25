import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          isCustomElement: (tag) => tag.startsWith('md-')
        }
      }
    })
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@common': resolve(__dirname, 'src/common'),
      '@user': resolve(__dirname, 'src/user'),
      '@merchant': resolve(__dirname, 'src/merchant'),
      '@admin': resolve(__dirname, 'src/admin'),
      '@screen': resolve(__dirname, 'src/screen')
    }
  },
  build: {
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
        merchant: resolve(__dirname, 'merchant.html'),
        admin: resolve(__dirname, 'admin.html'),
        screen: resolve(__dirname, 'screen.html')
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:9000',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://127.0.0.1:9000',
        ws: true
      }
    }
  }
})
