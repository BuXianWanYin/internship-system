// 班级管理API
import request from '@/utils/request'

/**
 * 班级管理API
 */
export const classApi = {
  // 添加班级
  addClass(data) {
    return request.post('/system/class', data)
  },
  // 更新班级
  updateClass(id, data) {
    return request.put(`/system/class/${id}`, data)
  },
  // 查询班级详情
  getClassById(id) {
    return request.get(`/system/class/${id}`)
  },
  // 分页查询班级列表
  getClassPage(params) {
    return request.get('/system/class/page', { params })
  },
  // 停用班级
  disableClass(id) {
    return request.put(`/system/class/${id}/disable`)
  },
  // 启用班级
  enableClass(id) {
    return request.put(`/system/class/${id}/enable`)
  },
  // 删除班级
  deleteClass(id) {
    return request.delete(`/system/class/${id}`)
  },
  // 生成分享码
  generateShareCode(id) {
    return request.post(`/system/class/${id}/share-code`)
  },
  // 重新生成分享码
  regenerateShareCode(id) {
    return request.post(`/system/class/${id}/share-code/regenerate`)
  },
  // 查看分享码
  getShareCode(id) {
    return request.get(`/system/class/${id}/share-code`)
  },
  // 验证分享码
  validateShareCode(shareCode) {
    return request.post('/system/class/share-code/validate', null, {
      params: { shareCode }
    })
  },
  // 任命班主任
  appointClassTeacher(classId, teacherId) {
    return request.post(`/system/class/${classId}/appoint-teacher`, null, {
      params: { teacherId }
    })
  },
  // 取消班主任任命
  removeClassTeacher(classId) {
    return request.post(`/system/class/${classId}/remove-teacher`)
  }
}

