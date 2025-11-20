// 专业管理API
import request from '@/utils/request'

/**
 * 专业管理API
 */
export const majorApi = {
  // 添加专业
  addMajor(data) {
    return request.post('/system/major', data)
  },
  // 更新专业
  updateMajor(id, data) {
    return request.put(`/system/major/${id}`, data)
  },
  // 查询专业详情
  getMajorById(id) {
    return request.get(`/system/major/${id}`)
  },
  // 分页查询专业列表
  getMajorPage(params) {
    return request.get('/system/major/page', { params })
  },
  // 停用专业
  deleteMajor(id) {
    return request.delete(`/system/major/${id}`)
  }
}

