// 学期管理API
import request from '@/utils/request'

/**
 * 学期管理API
 */
export const semesterApi = {
  // 添加学期
  addSemester(data) {
    return request.post('/system/semester', data)
  },
  // 更新学期
  updateSemester(id, data) {
    return request.put(`/system/semester/${id}`, data)
  },
  // 查询学期详情
  getSemesterById(id) {
    return request.get(`/system/semester/${id}`)
  },
  // 分页查询学期列表
  getSemesterPage(params) {
    return request.get('/system/semester/page', { params })
  },
  // 设置当前学期
  setCurrentSemester(id) {
    return request.put(`/system/semester/${id}/current`)
  },
  // 获取当前学期
  getCurrentSemester() {
    return request.get('/system/semester/current')
  },
  // 删除学期
  deleteSemester(id) {
    return request.delete(`/system/semester/${id}`)
  }
}

