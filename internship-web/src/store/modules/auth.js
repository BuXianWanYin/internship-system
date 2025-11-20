import { defineStore } from 'pinia'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import request from '@/utils/request'

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
          this.token = res.data.token
          this.userInfo = {
            username: res.data.username,
            roles: [] // TODO: 后续从后端获取用户角色
          }
          
          // 保存到本地存储
          setToken(this.token, loginForm.remember)
          setUserInfo(this.userInfo, loginForm.remember)
          
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
     */
    async logout() {
      try {
        // 调用后端登出接口
        await request.post('/auth/logout')
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        // 清除本地数据
        this.token = ''
        this.userInfo = null
        removeToken()
        removeUserInfo()
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
          setToken(this.token)
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
     * 更新用户信息
     */
    updateUserInfo(userInfo) {
      this.userInfo = userInfo
      setUserInfo(userInfo)
    }
  }
})

