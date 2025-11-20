/**
 * 权限工具函数
 */

/**
 * 检查是否有指定角色
 */
export function hasRole(roles, role) {
  if (!roles || !Array.isArray(roles)) {
    return false
  }
  return roles.includes(role)
}

/**
 * 检查是否有任一角色
 */
export function hasAnyRole(roles, requiredRoles) {
  if (!roles || !Array.isArray(roles)) {
    return false
  }
  if (!requiredRoles || !Array.isArray(requiredRoles)) {
    return false
  }
  return requiredRoles.some(role => roles.includes(role))
}

/**
 * 检查是否有所有角色
 */
export function hasAllRoles(roles, requiredRoles) {
  if (!roles || !Array.isArray(roles)) {
    return false
  }
  if (!requiredRoles || !Array.isArray(requiredRoles)) {
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
