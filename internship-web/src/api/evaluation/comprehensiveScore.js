// 综合成绩管理API
import request from '@/utils/request'

/**
 * 综合成绩管理API
 */
export const comprehensiveScoreApi = {
  // 计算综合成绩
  calculateComprehensiveScore(applyId) {
    return request.post(`/evaluation/comprehensive-score/apply/${applyId}/calculate`)
  },
  // 查询综合成绩详情
  getScoreById(scoreId) {
    return request.get(`/evaluation/comprehensive-score/${scoreId}`)
  },
  // 根据申请ID查询综合成绩
  getScoreByApplyId(applyId) {
    return request.get(`/evaluation/comprehensive-score/apply/${applyId}`)
  },
  // 分页查询综合成绩列表
  getScorePage(params) {
    return request.get('/evaluation/comprehensive-score/page', { params })
  },
  // 检查三个评价是否都完成
  checkAllEvaluationsCompleted(applyId) {
    return request.get(`/evaluation/comprehensive-score/apply/${applyId}/check-completed`)
  }
}

