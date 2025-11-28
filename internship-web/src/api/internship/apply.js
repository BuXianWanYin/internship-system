// 实习申请管理API
import request from '@/utils/request'

/**
 * 实习申请管理API
 */
export const applyApi = {
  // 提交实习申请（选择合作企业）
  addCooperationApply(data) {
    return request.post('/internship/apply/cooperation', data)
  },
  // 提交实习申请（自主实习）
  addSelfApply(data) {
    return request.post('/internship/apply/self', data)
  },
  // 更新实习申请
  updateApply(data) {
    return request.put('/internship/apply', data)
  },
  // 分页查询实习申请列表
  getApplyPage(params) {
    return request.get('/internship/apply/page', { params })
  },
  // 查询实习申请详情
  getApplyById(applyId) {
    return request.get(`/internship/apply/${applyId}`)
  },
  // 审核实习申请（自主实习）
  auditApply(applyId, auditStatus, auditOpinion) {
    return request.post(`/internship/apply/${applyId}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  },
  // 企业筛选操作
  filterApply(applyId, action, comment) {
    return request.post(`/internship/apply/${applyId}/filter`, null, {
      params: { action, comment }
    })
  },
  // 取消申请
  cancelApply(applyId) {
    return request.post(`/internship/apply/${applyId}/cancel`)
  },
  // 删除申请
  deleteApply(applyId) {
    return request.delete(`/internship/apply/${applyId}`)
  },
  // 查询企业实习学生列表（仅显示已录用的学生）
  getEnterpriseStudents(params) {
    return request.get('/internship/apply/enterprise/students', { params })
  },
  // 给学生分配企业导师
  assignMentor(applyId, mentorId) {
    return request.post(`/internship/apply/${applyId}/assign-mentor`, null, {
      params: { mentorId }
    })
  },
  // 学生确认上岗
  confirmOnboard(applyId) {
    return request.post(`/internship/apply/${applyId}/confirm-onboard`)
  },
  // 解绑企业（班主任/管理员）
  unbindInternship(applyId, reason, remark) {
    return request.post(`/internship/apply/${applyId}/unbind`, null, {
      params: { reason, remark }
    })
  },
  // 获取当前学生的实习申请（已确认上岗的）
  getCurrentInternship() {
    return request.get('/internship/apply/current')
  }
}

