// 角色管理API
import request from '@/utils/request'

/**
 * 角色管理API
 */
export const roleApi = {
  // 查询所有启用的角色列表
  getAllEnabledRoles() {
    return request.get('/user/role/list')
  }
}

