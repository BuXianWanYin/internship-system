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
          // 先保存到本地存储（确保token立即可用）
          setToken(res.data.token, loginForm.remember)
          setUserInfo({
            username: res.data.username,
            userId: res.data.userInfo?.userId,
            realName: res.data.userInfo?.realName,
            phone: res.data.userInfo?.phone,
            email: res.data.userInfo?.email,
            avatar: res.data.userInfo?.avatar,
            roles: res.data.userInfo?.roles || []
          }, loginForm.remember)
          
          // 然后更新store（确保store和存储一致）
          this.token = res.data.token
          this.userInfo = {
            username: res.data.username,
            userId: res.data.userInfo?.userId,
            realName: res.data.userInfo?.realName,
            phone: res.data.userInfo?.phone,
            email: res.data.userInfo?.email,
            avatar: res.data.userInfo?.avatar,
            roles: res.data.userInfo?.roles || []
          }
          
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
          // 刷新token时，使用之前的remember设置（默认使用localStorage）
          const remember = !!localStorage.getItem('token')
          setToken(this.token, remember)
          return Promise.resolve(res)
        } else {
          return Promise.reject(new Error(res.message))
        }
      } catch (error) {
        // Token刷新失败，清除登录信息
        this.logout()
        return Promise.reject(error)
      }
    },
    
    /**
     * 从存储中同步token到store（用于确保store和存储的一致性）
     */
    syncTokenFromStorage() {
      const tokenFromStorage = getToken()
      if (tokenFromStorage && this.token !== tokenFromStorage) {
        this.token = tokenFromStorage
      }
    },
    
    /**
     * 更新用户信息
     */
    updateUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    }
  }
})

