/**
 * 请求辅助工具函数
 */

/**
 * 检查是否是取消的请求（兼容新旧版本的 axios）
 * @param {Error} error - 错误对象
 * @returns {boolean} - 是否是取消的请求
 */
export function isCanceledRequest(error) {
  if (!error) {
    return false
  }
  
  // axios.isCancel 用于旧版本 (axios < 1.0)
  if (typeof axios !== 'undefined' && axios.isCancel && axios.isCancel(error)) {
    return true
  }
  
  // CanceledError 用于新版本 (axios >= 1.0)
  if (error.name === 'CanceledError' || error.code === 'ERR_CANCELED') {
    return true
  }
  
  // 检查错误消息（兼容处理）
  const errorMessage = error.message || ''
  const canceledKeywords = [
    'canceled',
    '取消',
    '未登录',
    '正在退出登录',
    'Token已过期',
    'Token已过期，正在跳转登录页'
  ]
  
  return canceledKeywords.some(keyword => errorMessage.toLowerCase().includes(keyword.toLowerCase()))
}

/**
 * 在 catch 块中处理错误，如果是取消的请求则静默处理
 * @param {Error} error - 错误对象
 * @param {Function} errorHandler - 错误处理函数
 * @returns {boolean} - 是否是取消的请求（如果是，则已静默处理）
 */
export function handleRequestError(error, errorHandler) {
  if (isCanceledRequest(error)) {
    // 静默处理取消的请求
    return true
  }
  
  // 如果不是取消的请求，调用错误处理函数
  if (errorHandler && typeof errorHandler === 'function') {
    errorHandler(error)
  }
  
  return false
}

