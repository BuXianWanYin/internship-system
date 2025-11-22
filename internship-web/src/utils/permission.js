/**
 * 权限工具函数
 */

import { useAuthStore } from '@/store/modules/auth'

/**
 * 获取当前用户角色列表
 * @returns {string[]} 角色列表
 */
function getCurrentUserRoles() {
  try {
    const authStore = useAuthStore()
    return authStore.roles || []
  } catch (error) {
    // 如果Store未初始化，返回空数组
    return []
  }
}

/**
 * 检查是否有指定角色
 * @param {string} role 角色代码
 * @returns {boolean}
 */
export function hasRole(role) {
  if (!role || typeof role !== 'string') {
    return false
  }
  const roles = getCurrentUserRoles()
  return roles.includes(role)
}

/**
 * 检查是否有任一角色
 * @param {string[]} requiredRoles 需要的角色代码数组
 * @returns {boolean}
 */
export function hasAnyRole(requiredRoles) {
  if (!requiredRoles || !Array.isArray(requiredRoles) || requiredRoles.length === 0) {
    return false
  }
  const roles = getCurrentUserRoles()
  if (!roles || roles.length === 0) {
    return false
  }
  return requiredRoles.some(role => roles.includes(role))
}

/**
 * 检查是否有所有角色
 * @param {string[]} requiredRoles 需要的角色代码数组
 * @returns {boolean}
 */
export function hasAllRoles(requiredRoles) {
  if (!requiredRoles || !Array.isArray(requiredRoles) || requiredRoles.length === 0) {
    return false
  }
  const roles = getCurrentUserRoles()
  if (!roles || roles.length === 0) {
    return false
  }
  return requiredRoles.every(role => roles.includes(role))
}

/**
 * 检查是否有指定权限
 */
export function hasPermission(permissions, permission) {
  if (!permissions || !Array.isArray(permissions)) {
    return false
  }
  return permissions.includes(permission)
}

/**
 * 检查是否有任一权限
 */
export function hasAnyPermission(permissions, requiredPermissions) {
  if (!permissions || !Array.isArray(permissions)) {
    return false
  }
  if (!requiredPermissions || !Array.isArray(requiredPermissions)) {
    return false
  }
  return requiredPermissions.some(permission => permissions.includes(permission))
}

/**
 * 角色常量
 */
export const ROLES = {
  SYSTEM_ADMIN: 'ROLE_SYSTEM_ADMIN',
  SCHOOL_ADMIN: 'ROLE_SCHOOL_ADMIN',
  COLLEGE_LEADER: 'ROLE_COLLEGE_LEADER',
  CLASS_TEACHER: 'ROLE_CLASS_TEACHER',
  INSTRUCTOR: 'ROLE_INSTRUCTOR',
  ENTERPRISE_ADMIN: 'ROLE_ENTERPRISE_ADMIN',
  ENTERPRISE_MENTOR: 'ROLE_ENTERPRISE_MENTOR',
  STUDENT: 'ROLE_STUDENT'
}
