// 学生管理API
import request from '@/utils/request'

/**
 * 学生管理API
 */
export const studentApi = {
  // 分页查询学生列表
  getStudentPage(params) {
    return request.get('/user/student/page', { params })
  },
  // 根据ID查询学生详情
  getStudentById(studentId) {
    return request.get(`/user/student/${studentId}`)
  },
  // 根据用户ID查询学生信息
  getStudentByUserId(userId) {
    return request.get(`/user/student/user/${userId}`)
  },
  // 添加学生
  addStudent(data) {
    return request.post('/user/student', data)
  },
  // 更新学生信息
  updateStudent(data) {
    return request.put('/user/student', data)
  },
  // 停用学生（软删除）
  deleteStudent(studentId) {
    return request.delete(`/user/student/${studentId}`)
  },
  // 下载学生导入Excel模板
  downloadImportTemplate() {
    return request.get('/user/student/import/template', {
      responseType: 'blob'
    })
  },
  // Excel批量导入学生
  importStudents(file, classId) {
    const formData = new FormData()
    formData.append('file', file)
    // classId现在是必填的
    formData.append('classId', classId)
    return request.post('/user/student/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  // 学生自主注册（使用分享码）
  registerStudent(data, shareCode) {
    return request.post('/user/student/register', data, {
      params: { shareCode }
    })
  },
  // 分页查询待审核学生列表（班主任审核）
  getPendingApprovalStudentPage(params) {
    return request.get('/user/student/pending-approval/page', { params })
  },
  // 审核学生注册申请
  approveStudentRegistration(studentId, approved, auditOpinion) {
    return request.post(`/user/student/${studentId}/approve`, null, {
      params: {
        approved,
        auditOpinion
      }
    })
  },
  // 获取所有入学年份
  getEnrollmentYears() {
    return request.get('/user/student/enrollment-years')
  }
}

