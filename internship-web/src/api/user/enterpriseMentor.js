// 企业导师管理API
import request from '@/utils/request'

/**
 * 企业导师管理API
 */
export const enterpriseMentorApi = {
  // 分页查询企业导师列表
  getEnterpriseMentorPage(params) {
    return request.get('/user/enterprise-mentor/page', { params })
  },
  // 根据ID查询企业导师详情
  getEnterpriseMentorById(mentorId) {
    return request.get(`/user/enterprise-mentor/${mentorId}`)
  },
  // 根据用户ID查询企业导师信息
  getEnterpriseMentorByUserId(userId) {
    return request.get(`/user/enterprise-mentor/user/${userId}`)
  },
  // 添加企业导师（自动创建用户）
  addEnterpriseMentor(data) {
    return request.post('/user/enterprise-mentor', data)
  },
  // 更新企业导师信息
  updateEnterpriseMentor(data) {
    return request.put('/user/enterprise-mentor', data)
  },
  // 停用企业导师（软删除）
  deleteEnterpriseMentor(mentorId) {
    return request.delete(`/user/enterprise-mentor/${mentorId}`)
  }
}

