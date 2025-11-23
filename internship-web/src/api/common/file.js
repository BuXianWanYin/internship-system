// 文件上传API
import request from '@/utils/request'
import axios from 'axios'
import { getToken } from '@/utils/auth'

/**
 * 文件管理API
 */
export const fileApi = {
  // 上传单个文件
  uploadFile(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/file/upload/single', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 上传多个文件
  uploadFiles(files) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    return request.post('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  // 获取文件下载URL（用于显示，实际下载请使用downloadFile方法）
  getDownloadUrl(filePath) {
    // 移除开头的斜杠
    const cleanPath = filePath.replace(/^\/+/, '')
    return `/api/file/download?path=${encodeURIComponent(cleanPath)}`
  },

  // 下载文件（使用axios请求，自动携带token）
  async downloadFile(filePath) {
    // 移除开头的斜杠
    const cleanPath = filePath.replace(/^\/+/, '')
    
    // 使用原生axios请求以获取完整的响应对象（包括headers）
    const token = getToken()
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    
    const response = await axios.get(`${baseURL}/file/download`, {
      params: {
        path: cleanPath
      },
      responseType: 'blob', // 重要：设置为blob类型以接收文件数据
      headers: {
        'Authorization': token ? `Bearer ${token}` : ''
      }
    })
    
    // 检查是否是错误响应（错误响应通常是JSON格式的blob）
    const contentType = response.headers['content-type'] || response.headers['Content-Type']
    if (contentType && contentType.includes('application/json')) {
      // 错误响应，需要解析JSON
      const text = await response.data.text()
      try {
        const errorData = JSON.parse(text)
        throw new Error(errorData.message || '下载失败')
      } catch (e) {
        if (e.message && e.message !== '下载失败') {
          throw e
        }
        throw new Error('下载失败')
      }
    }
    
    // 从响应头中获取文件名
    const contentDisposition = response.headers['content-disposition'] || response.headers['Content-Disposition']
    let fileName = 'download'
    if (contentDisposition) {
      // 尝试匹配 filename*=UTF-8''格式
      const fileNameMatch = contentDisposition.match(/filename\*=UTF-8''(.+)/)
      if (fileNameMatch) {
        fileName = decodeURIComponent(fileNameMatch[1])
      } else {
        // 尝试匹配 filename="格式
        const fileNameMatch2 = contentDisposition.match(/filename="(.+)"/)
        if (fileNameMatch2) {
          fileName = fileNameMatch2[1]
        } else {
          // 尝试匹配 filename=格式（无引号）
          const fileNameMatch3 = contentDisposition.match(/filename=([^;]+)/)
          if (fileNameMatch3) {
            fileName = fileNameMatch3[1].trim()
          }
        }
      }
    } else {
      // 如果响应头中没有文件名，从路径中提取
      const pathParts = cleanPath.split('/')
      fileName = pathParts[pathParts.length - 1] || 'download'
    }
    
    // 创建blob URL并触发下载
    const blob = response.data
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }
}

