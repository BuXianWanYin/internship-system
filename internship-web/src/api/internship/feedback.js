// 问题反馈管理API
import request from '@/utils/request'

/**
 * 问题反馈管理API
 */
export const feedbackApi = {
  // 提交问题反馈
  addFeedback(data) {
    return request.post('/internship/feedback', data)
  },
  // 更新问题反馈
  updateFeedback(data) {
    return request.put('/internship/feedback', data)
  },
  // 分页查询问题反馈列表
  getFeedbackPage(params) {
    return request.get('/internship/feedback/page', { params })
  },
  // 查询问题反馈详情
  getFeedbackById(feedbackId) {
    return request.get(`/internship/feedback/${feedbackId}`)
  },
  // 回复问题反馈
  replyFeedback(feedbackId, replyContent, replyUserType) {
    return request.post(`/internship/feedback/${feedbackId}/reply`, null, {
      params: { replyContent, replyUserType }
    })
  },
  // 标记问题已解决
  solveFeedback(feedbackId) {
    return request.post(`/internship/feedback/${feedbackId}/solve`)
  },
  // 关闭问题反馈
  closeFeedback(feedbackId) {
    return request.post(`/internship/feedback/${feedbackId}/close`)
  },
  // 删除问题反馈
  deleteFeedback(feedbackId) {
    return request.delete(`/internship/feedback/${feedbackId}`)
  }
}

