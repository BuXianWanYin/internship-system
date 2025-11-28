// 企业评价管理API
import request from '@/utils/request'

/**
 * 企业评价管理API
 */
export const enterpriseEvaluationApi = {
  // 添加或更新企业评价（支持草稿保存）
  saveOrUpdateEvaluation(data) {
    return request.post('/evaluation/enterprise', data)
  },
  // 提交企业评价
  submitEvaluation(evaluationId) {
    return request.post(`/evaluation/enterprise/${evaluationId}/submit`)
  },
  // 查询企业评价详情
  getEvaluationById(evaluationId) {
    return request.get(`/evaluation/enterprise/${evaluationId}`)
  },
  // 根据申请ID查询企业评价
  getEvaluationByApplyId(applyId) {
    return request.get(`/evaluation/enterprise/apply/${applyId}`)
  },
  // 分页查询企业评价列表
  getEvaluationPage(params) {
    return request.get('/evaluation/enterprise/page', { params })
  }
}

