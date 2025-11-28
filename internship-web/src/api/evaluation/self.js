// 学生自评管理API
import request from '@/utils/request'

/**
 * 学生自评管理API
 */
export const selfEvaluationApi = {
  // 添加或更新学生自评（支持草稿保存）
  saveOrUpdateEvaluation(data) {
    return request.post('/evaluation/self', data)
  },
  // 提交学生自评
  submitEvaluation(evaluationId) {
    return request.post(`/evaluation/self/${evaluationId}/submit`)
  },
  // 查询学生自评详情
  getEvaluationById(evaluationId) {
    return request.get(`/evaluation/self/${evaluationId}`)
  },
  // 根据申请ID查询学生自评
  getEvaluationByApplyId(applyId) {
    return request.get(`/evaluation/self/apply/${applyId}`)
  },
  // 分页查询学生自评列表
  getEvaluationPage(params) {
    return request.get('/evaluation/self/page', { params })
  }
}

