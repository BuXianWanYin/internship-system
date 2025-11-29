// 考勤组管理API
import request from '@/utils/request'

/**
 * 考勤组管理API
 */
export const attendanceGroupApi = {
  // 创建考勤组
  createAttendanceGroup(data) {
    return request.post('/internship/attendance-group', data)
  },
  // 更新考勤组
  updateAttendanceGroup(data) {
    return request.put('/internship/attendance-group', data)
  },
  // 删除考勤组
  deleteAttendanceGroup(groupId) {
    return request.delete(`/internship/attendance-group/${groupId}`)
  },
  // 分页查询考勤组列表
  getAttendanceGroupPage(params) {
    return request.get('/internship/attendance-group/page', { params })
  },
  // 查询考勤组详情
  getAttendanceGroupDetail(groupId) {
    return request.get(`/internship/attendance-group/${groupId}`)
  },
  // 添加考勤组规则
  addRule(groupId, data) {
    return request.post(`/internship/attendance-group/${groupId}/rule`, data)
  },
  // 删除考勤组规则
  deleteRule(ruleId) {
    return request.delete(`/internship/attendance-group/rule/${ruleId}`)
  },
  // 查询考勤组规则列表
  getRuleList(groupId) {
    return request.get(`/internship/attendance-group/${groupId}/rules`)
  },
  // 分配学生到考勤组
  assignStudentToGroup(groupId, applyId, effectiveStartDate, effectiveEndDate) {
    const params = { applyId, effectiveStartDate }
    if (effectiveEndDate) {
      params.effectiveEndDate = effectiveEndDate
    }
    return request.post(`/internship/attendance-group/${groupId}/assign-student`, null, { params })
  },
  // 批量分配学生到考勤组
  batchAssignStudentsToGroup(groupId, data) {
    return request.post(`/internship/attendance-group/${groupId}/assign-students`, data)
  },
  // 解除学生考勤组关联
  unassignStudentFromGroup(applyId) {
    return request.delete(`/internship/attendance-group/student/${applyId}`)
  },
  // 查询考勤组的学生列表
  getGroupStudents(groupId) {
    return request.get(`/internship/attendance-group/${groupId}/students`)
  },
  // 根据实习申请ID查询学生所属的考勤组
  getGroupByApplyId(applyId) {
    return request.get(`/internship/attendance-group/student/${applyId}`)
  },
  // 获取应出勤日期列表
  getExpectedDates(groupId, startDate, endDate) {
    return request.get('/internship/attendance-group/expected-dates', {
      params: { groupId, startDate, endDate }
    })
  },
  // 计算缺勤天数
  calculateAbsentDays(applyId, startDate, endDate) {
    return request.get('/internship/attendance-group/absent-days', {
      params: { applyId, startDate, endDate }
    })
  }
}

