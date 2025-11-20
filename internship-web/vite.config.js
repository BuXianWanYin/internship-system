import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd())
  
  // 从环境变量读取配置，必须从配置文件读取，无默认值
  if (!env.VITE_PORT) {
    throw new Error('VITE_PORT 未在 .env 文件中配置')
  }
  if (!env.VITE_API_BASE_URL) {
    throw new Error('VITE_API_BASE_URL 未在 .env 文件中配置')
  }

  const port = parseInt(env.VITE_PORT, 10)
  const apiBaseUrl = env.VITE_API_BASE_URL
  // API 代理路径前缀
  const apiPrefix = '/api'

  return {
    plugins: [vue()],
    resolve: {
      alias: {
        '@': resolve(__dirname, 'src')
      }
    },
    server: {
      port: port,
      proxy: {
        [apiPrefix]: {
          target: apiBaseUrl,
          changeOrigin: true,
          rewrite: (path) => path.replace(new RegExp(`^${apiPrefix}`), '')
        }
      }
    }
  }
})

