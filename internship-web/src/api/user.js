// 用户管理API
import request from '@/utils/request'

/**
 * 用户管理API
 */
export const userApi = {
  // 分页查询用户列表
  getUserPage(params) {
    return request.get('/user/page', { params })
  },
  // 根据ID查询用户详情
  getUserById(userId) {
    return request.get(`/user/${userId}`)
  },
  // 添加用户
  addUser(data) {
    return request.post('/user', data)
  },
  // 更新用户信息
  updateUser(data) {
    return request.put('/user', data)
  },
  // 停用用户（软删除）
  deleteUser(userId) {
    return request.delete(`/user/${userId}`)
  },
  // 重置用户密码
  resetPassword(userId, newPassword) {
    return request.post(`/user/${userId}/reset-password`, null, {
      params: { newPassword }
    })
  }
}
