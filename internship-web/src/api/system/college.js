// 学院管理API
import request from '@/utils/request'

/**
 * 学院管理API
 */
export const collegeApi = {
  // 添加学院
  addCollege(data) {
    return request.post('/system/college', data)
  },
  // 更新学院
  updateCollege(id, data) {
    return request.put(`/system/college/${id}`, data)
  },
  // 查询学院详情
  getCollegeById(id) {
    return request.get(`/system/college/${id}`)
  },
  // 分页查询学院列表
  getCollegePage(params) {
    return request.get('/system/college/page', { params })
  },
  // 停用学院
  deleteCollege(id) {
    return request.delete(`/system/college/${id}`)
  }
}

