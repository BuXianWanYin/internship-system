/**
 * Excel导出工具函数
 */
import { ElMessage } from 'element-plus'
import { formatDate } from './dateUtils'

/**
 * 导出Excel文件
 * @param {Function} apiFunction - API函数
 * @param {Object} params - 导出参数
 * @param {String} filename - 文件名（不含扩展名）
 * @returns {Promise<Boolean>}
 */
export async function exportExcel(apiFunction, params, filename) {
  try {
    const response = await apiFunction(params)
    
    // 创建Blob对象
    const blob = new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${filename || '导出数据'}_${formatDate(new Date(), 'yyyyMMdd_HHmmss')}.xlsx`
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    return true
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error(error.response?.data?.message || '导出失败，请稍后重试')
    throw error
  }
}

