// 学校评价管理API
import request from '@/utils/request'

/**
 * 学校评价管理API
 */
export const schoolEvaluationApi = {
  // 添加或更新学校评价（支持草稿保存）
  saveOrUpdateEvaluation(data) {
    return request.post('/evaluation/school', data)
  },
  // 提交学校评价
  submitEvaluation(evaluationId) {
    return request.post(`/evaluation/school/${evaluationId}/submit`)
  },
  // 查询学校评价详情
  getEvaluationById(evaluationId) {
    return request.get(`/evaluation/school/${evaluationId}`)
  },
  // 根据申请ID查询学校评价
  getEvaluationByApplyId(applyId) {
    return request.get(`/evaluation/school/apply/${applyId}`)
  },
  // 计算日志周报质量建议分数
  getSuggestedLogWeeklyReportScore(applyId) {
    return request.get(`/evaluation/school/apply/${applyId}/suggested-score`)
  },
  // 分页查询学校评价列表
  getEvaluationPage(params) {
    return request.get('/evaluation/school/page', { params })
  }
}

