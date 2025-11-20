import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000, // 30秒
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从store获取token
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 如果token存在，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果是blob响应（文件下载），需要特殊处理
    if (response.config.responseType === 'blob') {
      // 检查HTTP状态码
      if (response.status === 200) {
        // 检查Content-Type，判断是否是错误响应（错误响应通常是JSON格式的blob）
        const contentType = response.headers['content-type'] || response.headers['Content-Type']
        if (contentType && contentType.includes('application/json')) {
          // 错误响应，需要解析JSON
          return response.data.text().then(text => {
            try {
              const errorData = JSON.parse(text)
              ElMessage.error(errorData.message || '下载失败')
              return Promise.reject(new Error(errorData.message || '下载失败'))
            } catch (e) {
              ElMessage.error('下载失败')
              return Promise.reject(new Error('下载失败'))
            }
          })
        }
        // 正常的文件响应，直接返回blob
        return response.data
      } else {
        // HTTP状态码不是200，说明有错误
        ElMessage.error('下载失败')
        return Promise.reject(new Error('下载失败'))
      }
    }
    
    const res = response.data
    
    // 如果响应码不是200，说明有错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      
      // 如果是401，清除token并跳转到登录页
      if (res.code === 401) {
        const authStore = useAuthStore()
        authStore.logout()
        router.push('/login')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    let message = '网络连接失败，请检查网络'
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          message = '未认证，请先登录'
          const authStore = useAuthStore()
          // 跳过API调用，直接清除本地数据（避免再次401错误）
          authStore.logout(true)
          router.push('/login')
          break
        case 403:
          message = '无权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误，请稍后重试'
          break
        default:
          message = data?.message || `请求失败: ${status}`
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    } else {
      message = error.message || '请求失败'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default service
