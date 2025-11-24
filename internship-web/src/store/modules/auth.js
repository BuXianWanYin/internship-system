import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import request, { setLoggingOut } from '@/utils/request'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken() || '',
    userInfo: getUserInfo() || null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    roles: (state) => state.userInfo?.roles || []
  },

  actions: {
    /**
     * 登录
     */
    async login(loginForm) {
      try {
        const res = await request.post('/auth/login', {
          username: loginForm.username,
          password: loginForm.password
        })
        if (res.code === 200) {
          const userInfo = {
            username: res.data.username,
            userId: res.data.userInfo?.userId,
            realName: res.data.userInfo?.realName,
            phone: res.data.userInfo?.phone,
            email: res.data.userInfo?.email,
            avatar: res.data.userInfo?.avatar,
            roles: res.data.userInfo?.roles || []
          }
          
          // 更新store（单一数据源）
          this.token = res.data.token
          this.userInfo = userInfo
          
          // 持久化到本地存储
          setToken(res.data.token, loginForm.remember)
          setUserInfo(userInfo, loginForm.remember)
          
          return Promise.resolve(res)
        } else {
          return Promise.reject(new Error(res.message))
        }
      } catch (error) {
        return Promise.reject(error)
      }
    },

    /**
     * 登出
     * @param {boolean} skipApiCall - 是否跳过API调用（当Token失效时使用）
     */
    async logout(skipApiCall = false) {
      try {
        // 如果Token存在且未失效，先调用后端登出接口（在设置标志之前）
        if (!skipApiCall && this.token) {
          await request.post('/auth/logout')
        }
      } catch (error) {
        // 如果是401错误（Token失效）或取消的请求，静默处理，不打印错误
        if (error.response && error.response.status === 401) {
          // Token已失效，直接清除本地数据
        } else if (error.message && error.message.includes('正在退出登录')) {
          // 请求被取消，静默处理
        } else {
          console.error('登出失败:', error)
        }
      } finally {
        // 设置退出登录标志，阻止后续请求
        setLoggingOut(true)
        
        // 清除本地数据
        this.token = ''
        this.userInfo = null
        removeToken()
        removeUserInfo()
        
        // 延迟重置标志，确保所有请求都被取消
        setTimeout(() => {
          setLoggingOut(false)
        }, 1000)
      }
    },

    /**
     * 刷新Token
     */
    async refreshToken() {
      try {
        const res = await request.post('/auth/refresh')
        if (res.code === 200) {
          this.token = res.data.token
          // 刷新token时，使用之前的remember设置
          const remember = !!localStorage.getItem('token')
          setToken(this.token, remember)
          return Promise.resolve(res)
        } else {
          return Promise.reject(new Error(res.message))
        }
      } catch (error) {
        this.logout()
        return Promise.reject(error)
      }
    },
    
    /**
     * 更新用户信息
     */
    updateUserInfo(userInfo) {
      this.userInfo = userInfo
      const remember = !!localStorage.getItem('userInfo')
      setUserInfo(userInfo, remember)
    }
  }
})

