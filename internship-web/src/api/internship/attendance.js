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
  confirmAttendance(attendanceId, confirmStatus, confirmComment, checkInTime, checkOutTime) {
    const params = { confirmStatus }
    if (confirmComment) {
      params.confirmComment = confirmComment
    }
    if (checkInTime) {
      params.checkInTime = checkInTime
    }
    if (checkOutTime) {
      params.checkOutTime = checkOutTime
    }
    return request.post(`/internship/attendance/${attendanceId}/confirm`, null, { params })
  },
  // 删除考勤
  deleteAttendance(attendanceId) {
    return request.delete(`/internship/attendance/${attendanceId}`)
  },
  // 考勤统计
  getAttendanceStatistics(params) {
    return request.get('/internship/attendance/statistics', { params })
  },
  // 学生签到
  studentCheckIn(attendanceDate, timeSlotId) {
    const params = {}
    if (attendanceDate) {
      params.attendanceDate = attendanceDate
    }
    if (timeSlotId) {
      params.timeSlotId = timeSlotId
    }
    return request.post('/internship/attendance/check-in', null, { params })
  },
  // 学生签退
  studentCheckOut(attendanceDate, timeSlotId) {
    const params = {}
    if (attendanceDate) {
      params.attendanceDate = attendanceDate
    }
    if (timeSlotId) {
      params.timeSlotId = timeSlotId
    }
    return request.post('/internship/attendance/check-out', null, { params })
  },
  // 学生申请请假
  studentApplyLeave(attendanceDate, leaveType, leaveReason) {
    return request.post('/internship/attendance/apply-leave', null, {
      params: { attendanceDate, leaveType, leaveReason }
    })
  },
  // 学生选择休息
  studentSelectRest(attendanceDate) {
    return request.post('/internship/attendance/select-rest', null, {
      params: { attendanceDate }
    })
  },
  // 获取今天的考勤记录
  getTodayAttendance() {
    return request.get('/internship/attendance/today')
  }
}

