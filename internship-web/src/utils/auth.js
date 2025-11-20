/**
 * 认证工具函数
 */

/**
 * 获取Token
 */
export function getToken() {
  return localStorage.getItem('token') || sessionStorage.getItem('token')
}

/**
 * 设置Token
 */
export function setToken(token, remember = false) {
  if (remember) {
    localStorage.setItem('token', token)
  } else {
    sessionStorage.setItem('token', token)
  }
}

/**
 * 移除Token
 */
export function removeToken() {
  localStorage.removeItem('token')
  sessionStorage.removeItem('token')
}

/**
 * 获取用户信息
 */
export function getUserInfo() {
  const userInfo = localStorage.getItem('userInfo') || sessionStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo) : null
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo, remember = false) {
  const userInfoStr = JSON.stringify(userInfo)
  if (remember) {
    localStorage.setItem('userInfo', userInfoStr)
  } else {
    sessionStorage.setItem('userInfo', userInfoStr)
  }
}

/**
 * 移除用户信息
 */
export function removeUserInfo() {
  localStorage.removeItem('userInfo')
  sessionStorage.removeItem('userInfo')
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeUserInfo()
}
