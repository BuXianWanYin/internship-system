// 企业学校合作关系管理API
import request from '@/utils/request'

/**
 * 企业学校合作关系管理API
 */
export const cooperationApi = {
  // 添加合作关系
  addCooperation(data) {
    return request.post('/cooperation/enterprise-school', data)
  },
  // 更新合作关系
  updateCooperation(data) {
    return request.put('/cooperation/enterprise-school', data)
  },
  // 删除合作关系
  deleteCooperation(id) {
    return request.delete(`/cooperation/enterprise-school/${id}`)
  },
  // 根据企业ID查询合作学校列表
  getCooperationSchoolsByEnterpriseId(enterpriseId) {
    return request.get(`/cooperation/enterprise-school/enterprise/${enterpriseId}/schools`)
  },
  // 根据学校ID查询合作企业ID列表
  getCooperationEnterpriseIdsBySchoolId(schoolId) {
    return request.get(`/cooperation/enterprise-school/school/${schoolId}/enterprises`)
  },
  // 检查企业和学校是否有合作关系
  hasCooperation(enterpriseId, schoolId) {
    return request.get('/cooperation/enterprise-school/check', {
      params: { enterpriseId, schoolId }
    })
  },
  // 根据企业ID查询合作关系列表（包含完整信息）
  getCooperationListByEnterpriseId(enterpriseId) {
    return request.get(`/cooperation/enterprise-school/enterprise/${enterpriseId}/list`)
  }
}
