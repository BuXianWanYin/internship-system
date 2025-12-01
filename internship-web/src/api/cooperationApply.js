// 合作申请API
import request from '@/utils/request'

/**
 * 合作申请API
 */
export const cooperationApplyApi = {
  // 企业申请合作
  applyCooperation(data) {
    return request.post('/enterprise/cooperation/apply', data)
  },
  // 查询企业的合作申请列表
  getApplyList() {
    return request.get('/enterprise/cooperation/apply/list')
  },
  // 查询企业的合作关系列表
  getCooperationList() {
    return request.get('/enterprise/cooperation/list')
  },
  // 获取可申请合作的学校列表
  getAvailableSchoolList() {
    return request.get('/enterprise/cooperation/school/list')
  },
  // 学校管理员查询待审核申请列表
  getPendingApplyList(schoolId) {
    return request.get('/admin/cooperation/pending', {
      params: { schoolId }
    })
  },
  // 审核合作申请
  auditCooperationApply(id, auditStatus, auditOpinion) {
    return request.post(`/admin/cooperation/${id}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  }
}

