// 学校管理API
import request from '@/utils/request'

/**
 * 学校管理API
 */
export const schoolApi = {
  // 添加学校
  addSchool(data) {
    return request.post('/system/school', data)
  },
  // 更新学校
  updateSchool(id, data) {
    return request.put(`/system/school/${id}`, data)
  },
  // 查询学校详情
  getSchoolById(id) {
    return request.get(`/system/school/${id}`)
  },
  // 分页查询学校列表
  getSchoolPage(params) {
    return request.get('/system/school/page', { params })
  },
  // 公开获取学校列表（用于企业注册等公开场景）
  getPublicSchoolList() {
    return request.get('/system/school/public/list')
  },
  // 停用学校
  deleteSchool(id) {
    return request.delete(`/system/school/${id}`)
  }
}

