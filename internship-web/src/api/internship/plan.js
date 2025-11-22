// 实习计划管理API
import request from '@/utils/request'

/**
 * 实习计划管理API
 */
export const planApi = {
  // 创建实习计划
  addPlan(data) {
    return request.post('/internship/plan', data)
  },
  // 更新实习计划
  updatePlan(data) {
    return request.put('/internship/plan', data)
  },
  // 分页查询实习计划列表
  getPlanPage(params) {
    return request.get('/internship/plan/page', { params })
  },
  // 查询实习计划详情
  getPlanById(planId) {
    return request.get(`/internship/plan/${planId}`)
  },
  // 提交审核
  submitPlan(planId) {
    return request.post(`/internship/plan/${planId}/submit`)
  },
  // 审核实习计划
  auditPlan(planId, auditStatus, auditOpinion) {
    return request.post(`/internship/plan/${planId}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  },
  // 发布实习计划
  publishPlan(planId) {
    return request.post(`/internship/plan/${planId}/publish`)
  },
  // 删除实习计划
  deletePlan(planId) {
    return request.delete(`/internship/plan/${planId}`)
  }
}

