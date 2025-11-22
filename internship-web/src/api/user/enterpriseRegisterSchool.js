// 企业注册申请院校关联API
import request from '@/utils/request'

/**
 * 企业注册申请院校关联API
 */
export const enterpriseRegisterSchoolApi = {
  // 根据企业ID查询注册申请院校列表
  getByEnterpriseId(enterpriseId) {
    return request.get(`/user/enterprise-register-school/enterprise/${enterpriseId}`)
  },
  // 根据学校ID查询待审核的企业注册申请列表
  getPendingBySchoolId(schoolId) {
    return request.get(`/user/enterprise-register-school/school/${schoolId}/pending`)
  },
  // 审核企业注册申请（按院校）
  auditEnterpriseRegister(id, auditStatus, auditOpinion) {
    return request.post(`/user/enterprise-register-school/${id}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  }
}

