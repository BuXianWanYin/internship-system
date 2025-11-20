/**
 * 日期时间格式化工具函数
 */

/**
 * 格式化日期时间
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @param {string} pattern - 格式化模式，默认：'yyyy-MM-dd HH:mm:ss'
 * @returns {string} 格式化后的日期时间字符串
 * @example
 * formatDateTime(new Date()) // '2024-01-01 12:00:00'
 * formatDateTime(new Date(), 'yyyy-MM-dd') // '2024-01-01'
 * formatDateTime('2024-01-01T12:00:00') // '2024-01-01 12:00:00'
 */
export function formatDateTime(date, pattern = 'yyyy-MM-dd HH:mm:ss') {
  if (!date) return ''
  
  const d = new Date(date)
  if (isNaN(d.getTime())) return ''
  
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return pattern
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期（不包含时间）
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @param {string} pattern - 格式化模式，默认：'yyyy-MM-dd'
 * @returns {string} 格式化后的日期字符串
 * @example
 * formatDate(new Date()) // '2024-01-01'
 * formatDate(new Date(), 'yyyy年MM月dd日') // '2024年01月01日'
 */
export function formatDate(date, pattern = 'yyyy-MM-dd') {
  return formatDateTime(date, pattern)
}

/**
 * 格式化时间（不包含日期）
 * @param {Date|string|number} date - 日期对象、日期字符串或时间戳
 * @param {string} pattern - 格式化模式，默认：'HH:mm:ss'
 * @returns {string} 格式化后的时间字符串
 * @example
 * formatTime(new Date()) // '12:00:00'
 * formatTime(new Date(), 'HH:mm') // '12:00'
 */
export function formatTime(date, pattern = 'HH:mm:ss') {
  return formatDateTime(date, pattern)
}

