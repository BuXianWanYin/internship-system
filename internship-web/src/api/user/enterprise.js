// 企业管理API
import request from '@/utils/request'

/**
 * 企业管理API
 */
export const enterpriseApi = {
  // 企业注册
  registerEnterprise(data) {
    return request.post('/user/enterprise/register', data)
  },
  // 分页查询企业列表
  getEnterprisePage(params) {
    return request.get('/user/enterprise/page', { params })
  },
  // 根据ID查询企业详情
  getEnterpriseById(enterpriseId) {
    return request.get(`/user/enterprise/${enterpriseId}`)
  },
  // 根据用户ID查询企业信息
  getEnterpriseByUserId(userId) {
    return request.get(`/user/enterprise/user/${userId}`)
  },
  // 添加企业
  addEnterprise(data) {
    return request.post('/user/enterprise', data)
  },
  // 更新企业信息
  updateEnterprise(data) {
    return request.put('/user/enterprise', data)
  },
  // 审核企业
  auditEnterprise(enterpriseId, auditStatus, auditOpinion) {
    return request.post(`/user/enterprise/${enterpriseId}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  },
  // 停用企业（软删除）
  deleteEnterprise(enterpriseId) {
    return request.delete(`/user/enterprise/${enterpriseId}`)
  },
  // 根据企业ID查询合作学校列表
  getCooperationSchoolsByEnterpriseId(enterpriseId) {
    return request.get(`/user/enterprise/${enterpriseId}/cooperation-schools`)
  }
}

