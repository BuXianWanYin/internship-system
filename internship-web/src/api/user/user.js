// 用户基础管理API
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
  },
  // 根据用户名查询用户
  getUserByUsername(username) {
    return request.get(`/user/username/${username}`)
  },
  // 为用户分配角色
  assignRoleToUser(userId, roleCode) {
    return request.post(`/user/${userId}/assign-role`, null, {
      params: { roleCode }
    })
  },
  // 检查是否可以停用用户
  canDeleteUser(userId) {
    return request.get(`/user/${userId}/can-delete`)
  },
  // 获取用户角色列表
  getUserRoles(userId) {
    return request.get(`/user/${userId}/roles`)
  }
}

