import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
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
  }
});
