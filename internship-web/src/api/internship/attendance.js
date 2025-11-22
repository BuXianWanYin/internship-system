// 考勤管理API
import request from '@/utils/request'

/**
 * 考勤管理API
 */
export const attendanceApi = {
  // 确认考勤
  addAttendance(data) {
    return request.post('/internship/attendance', data)
  },
  // 批量确认考勤
  batchAddAttendance(data) {
    return request.post('/internship/attendance/batch', data)
  },
  // 更新考勤
  updateAttendance(data) {
    return request.put('/internship/attendance', data)
  },
  // 分页查询考勤列表
  getAttendancePage(params) {
    return request.get('/internship/attendance/page', { params })
  },
  // 查询考勤详情
  getAttendanceById(attendanceId) {
    return request.get(`/internship/attendance/${attendanceId}`)
  },
  // 确认考勤
  confirmAttendance(attendanceId, confirmStatus, confirmComment) {
    return request.post(`/internship/attendance/${attendanceId}/confirm`, null, {
      params: { confirmStatus, confirmComment }
    })
  },
  // 删除考勤
  deleteAttendance(attendanceId) {
    return request.delete(`/internship/attendance/${attendanceId}`)
  },
  // 考勤统计
  getAttendanceStatistics(params) {
    return request.get('/internship/attendance/statistics', { params })
  }
}

