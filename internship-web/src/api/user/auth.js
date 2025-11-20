// 认证授权API
import request from '@/utils/request'

/**
 * 认证授权API
 */
export const authApi = {
  // 用户登录
  login(data) {
    return request.post('/auth/login', data)
  },
  // 用户登出
  logout() {
    return request.post('/auth/logout')
  },
  // 刷新Token
  refreshToken(token) {
    return request.post('/auth/refresh', { token })
  }
}

