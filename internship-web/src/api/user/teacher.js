// 教师管理API
import request from '@/utils/request'

/**
 * 教师管理API
 */
export const teacherApi = {
  // 分页查询教师列表
  getTeacherPage(params) {
    return request.get('/user/teacher/page', { params })
  },
  // 根据ID查询教师详情
  getTeacherById(teacherId) {
    return request.get(`/user/teacher/${teacherId}`)
  },
  // 根据用户ID查询教师信息
  getTeacherByUserId(userId) {
    return request.get(`/user/teacher/user/${userId}`)
  },
  // 添加教师（自动创建用户）
  addTeacher(data) {
    return request.post('/user/teacher', data)
  },
  // 更新教师信息
  updateTeacher(data) {
    return request.put('/user/teacher', data)
  },
  // 停用教师（软删除）
  deleteTeacher(teacherId) {
    return request.delete(`/user/teacher/${teacherId}`)
  },
  // 根据学校ID查询教师列表（用于下拉选择）
  getTeacherList(params) {
    return request.get('/user/teacher/list', { params })
  }
}

