// 面试管理API
import request from '@/utils/request'

/**
 * 面试管理API
 */
export const interviewApi = {
  // 安排面试
  addInterview(data) {
    return request.post('/internship/interview', data)
  },
  // 更新面试信息
  updateInterview(data) {
    return request.put('/internship/interview', data)
  },
  // 分页查询面试列表
  getInterviewPage(params) {
    return request.get('/internship/interview/page', { params })
  },
  // 查询面试详情
  getInterviewById(interviewId) {
    return request.get(`/internship/interview/${interviewId}`)
  },
  // 学生确认面试
  confirmInterview(interviewId, confirm) {
    return request.post(`/internship/interview/${interviewId}/confirm`, null, {
      params: { confirm }
    })
  },
  // 提交面试结果
  submitInterviewResult(interviewId, interviewResult, interviewComment) {
    return request.post(`/internship/interview/${interviewId}/result`, null, {
      params: { interviewResult, interviewComment }
    })
  },
  // 取消面试
  cancelInterview(interviewId) {
    return request.post(`/internship/interview/${interviewId}/cancel`)
  }
}

