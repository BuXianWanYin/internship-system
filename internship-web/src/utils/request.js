import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { removeToken, removeUserInfo } from '@/utils/auth'
import router from '@/router'
import { isCanceledRequest } from '@/utils/request-helper'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000, // 30秒
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 全局标志：是否正在处理401错误
let isHandling401 = false
// 全局标志：是否正在退出登录
let isLoggingOut = false
// 用于存储所有待取消的请求
const pendingRequests = new Set()

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 检查是否是认证相关的请求（允许这些请求通过，即使正在退出登录）
    const isAuthRequest = config.url && (
      config.url.includes('/auth/login') ||
      config.url.includes('/auth/register') ||
      config.url.includes('/auth/refresh') ||
      config.url.includes('/auth/logout')
    )
    
    // 如果正在处理401错误，取消所有后续请求（除了认证相关请求）
    if (isHandling401 && !isAuthRequest) {
      const cancelToken = axios.CancelToken.source()
      config.cancelToken = cancelToken.token
      cancelToken.cancel('Token已过期，正在跳转登录页')
      // 注意：即使设置了 cancelToken 并 cancel，config 仍然会被返回
      // 请求会被取消，错误会在响应拦截器中处理
    }
    
    // 如果正在退出登录，取消所有后续请求（除了logout请求）
    if (isLoggingOut && !isAuthRequest) {
      const cancelToken = axios.CancelToken.source()
      config.cancelToken = cancelToken.token
      cancelToken.cancel('正在退出登录')
      // 注意：即使设置了 cancelToken 并 cancel，config 仍然会被返回
      // 请求会被取消，错误会在响应拦截器中处理
    }
    
    // 从store获取token
    const authStore = useAuthStore()
    const token = authStore.token
    
    // 如果不是登录相关请求且没有token，取消请求
    if (!isAuthRequest && !token) {
      const cancelToken = axios.CancelToken.source()
      config.cancelToken = cancelToken.token
      cancelToken.cancel('未登录，请先登录')
      // 注意：即使设置了 cancelToken 并 cancel，config 仍然会被返回
      // 请求会被取消，错误会在响应拦截器中处理
    }
    
    // 如果token存在，添加到请求头
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 记录待处理的请求
    pendingRequests.add(config)
    
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
    // 从待处理请求中移除
    pendingRequests.delete(response.config)
    
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
      // 如果是401，清除token并跳转到登录页
      if (res.code === 401) {
        handle401Error('未认证，请先登录')
      } else {
        ElMessage.error(res.message || '请求失败')
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    // 检查是否是取消的请求（使用工具函数）
    if (isCanceledRequest(error)) {
      // 从待处理请求中移除
      if (error.config) {
        pendingRequests.delete(error.config)
      }
      // 静默处理取消的请求
      // 返回一个永远不会 resolve 或 reject 的 Promise
      // 这样组件的 catch 块就不会执行，也不会继续执行后续代码
      return new Promise(() => {
        // 这个 Promise 永远不会 resolve 或 reject
        // 这样组件的 catch 块就不会执行
      })
    }
    
    // 从待处理请求中移除
    if (error.config) {
      pendingRequests.delete(error.config)
    }
    
    let message = '网络连接失败，请检查网络'
    let shouldShowError = true
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 401:
          // 处理401错误（只处理一次）
          handle401Error('未认证，请先登录')
          shouldShowError = false // 401错误已在handle401Error中处理，不再重复显示
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
      // 请求已发出但没有收到响应（可能是网络问题）
      message = '网络连接失败，请检查网络'
    } else {
      // 请求配置错误
      message = error.message || '请求失败'
    }
    
    // 只在需要时显示错误消息
    if (shouldShowError && !isHandling401 && !isLoggingOut) {
      ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

/**
 * 处理401错误（统一处理，避免重复）
 */
function handle401Error(message) {
  // 如果已经在处理401错误，直接返回
  if (isHandling401) {
    return
  }
  
  // 设置标志，防止重复处理
  isHandling401 = true
  
  // 取消所有待处理的请求
  const cancelTokenSource = axios.CancelToken.source()
  pendingRequests.forEach(config => {
    if (!config.cancelToken) {
      config.cancelToken = cancelTokenSource.token
    }
  })
  cancelTokenSource.cancel('Token已过期，正在跳转登录页')
  pendingRequests.clear()
  
  // 显示错误消息（只显示一次）
  ElMessage.error(message)
  
  // 清除token并跳转到登录页
  const authStore = useAuthStore()
  // 直接清除本地数据，不调用API（避免再次401）
  authStore.token = ''
  authStore.userInfo = null
  removeToken()
  removeUserInfo()
  
  // 跳转到登录页
  router.push('/login').finally(() => {
    // 延迟重置标志，确保跳转完成
    setTimeout(() => {
      isHandling401 = false
    }, 1000)
  })
}

/**
 * 设置退出登录标志（供外部调用）
 */
export function setLoggingOut(value) {
  isLoggingOut = value
  
  // 如果正在退出登录，取消所有待处理的请求
  if (value) {
    const cancelTokenSource = axios.CancelToken.source()
    pendingRequests.forEach(config => {
      if (!config.cancelToken) {
        config.cancelToken = cancelTokenSource.token
      }
    })
    cancelTokenSource.cancel('正在退出登录')
    pendingRequests.clear()
  }
}

export default service
