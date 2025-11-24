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
  ENTERPRISE_ADMIN: 'ROLE_ENTERPRISE_ADMIN',
  ENTERPRISE_MENTOR: 'ROLE_ENTERPRISE_MENTOR',
  STUDENT: 'ROLE_STUDENT'
}

/**
 * 检查当前用户是否可以编辑指定用户
 * 权限规则：
 * 1. 系统管理员可以编辑所有用户
 * 2. 学校管理员不能编辑系统管理员，可以编辑其他所有用户
 * 3. 学院负责人不能编辑系统管理员、学校管理员，可以编辑教师、学生等
 * 4. 班主任不能编辑系统管理员、学校管理员、学院负责人，可以编辑学生等
 * 
 * @param {string[]} targetUserRoles 目标用户的角色列表
 * @returns {boolean} true-可以编辑，false-不能编辑
 */
export function canEditUser(targetUserRoles) {
  if (!targetUserRoles || !Array.isArray(targetUserRoles) || targetUserRoles.length === 0) {
    return false
  }
  
  const currentRoles = getCurrentUserRoles()
  if (!currentRoles || currentRoles.length === 0) {
    return false
  }
  
  // 系统管理员可以编辑所有用户
  if (currentRoles.includes(ROLES.SYSTEM_ADMIN)) {
    return true
  }
  
  // 任何人都不能编辑系统管理员
  if (targetUserRoles.includes(ROLES.SYSTEM_ADMIN)) {
    return false
  }
  
  // 学校管理员不能编辑系统管理员（上面已处理），可以编辑其他所有用户
  if (currentRoles.includes(ROLES.SCHOOL_ADMIN)) {
    return true
  }
  
  // 学院负责人不能编辑系统管理员、学校管理员
  if (currentRoles.includes(ROLES.COLLEGE_LEADER)) {
    if (targetUserRoles.includes(ROLES.SCHOOL_ADMIN)) {
      return false
    }
    // 可以编辑教师、学生等
    return true
  }
  
  // 班主任不能编辑系统管理员、学校管理员、学院负责人
  if (currentRoles.includes(ROLES.CLASS_TEACHER)) {
    if (targetUserRoles.includes(ROLES.SCHOOL_ADMIN) || 
        targetUserRoles.includes(ROLES.COLLEGE_LEADER)) {
      return false
    }
    // 可以编辑学生等
    return true
  }
  
  // 其他角色默认不能编辑其他用户
  return false
}

/**
 * 检查当前用户是否可以分配指定角色
 * 权限规则：
 * 1. 系统管理员可以分配所有角色
 * 2. 学校管理员不能分配系统管理员角色，可以分配其他角色
 * 3. 学院负责人不能分配系统管理员、学校管理员角色，可以分配教师、学生等角色
 * 4. 班主任不能分配系统管理员、学校管理员、学院负责人角色，可以分配学生角色
 * 
 * @param {string} roleCode 角色代码
 * @returns {boolean} true-可以分配，false-不能分配
 */
export function canAssignRole(roleCode) {
  if (!roleCode || typeof roleCode !== 'string') {
    return false
  }
  
  const currentRoles = getCurrentUserRoles()
  if (!currentRoles || currentRoles.length === 0) {
    return false
  }
  
  // 系统管理员可以分配所有角色
  if (currentRoles.includes(ROLES.SYSTEM_ADMIN)) {
    return true
  }
  
  // 任何人都不能分配系统管理员角色
  if (roleCode === ROLES.SYSTEM_ADMIN) {
    return false
  }
  
  // 学校管理员不能分配系统管理员角色（上面已处理）
  // 可以分配学校相关角色（ROLE_SCHOOL_ADMIN、ROLE_COLLEGE_LEADER、ROLE_CLASS_TEACHER、ROLE_STUDENT）
  // 不能分配企业相关角色
  if (currentRoles.includes(ROLES.SCHOOL_ADMIN)) {
    // 不能分配企业相关角色
    if (roleCode === ROLES.ENTERPRISE_ADMIN || roleCode === ROLES.ENTERPRISE_MENTOR) {
      return false
    }
    // 可以分配学校相关角色
    return true
  }
  
  // 学院负责人不能分配系统管理员、学校管理员角色
  // 只能分配教师相关角色（ROLE_COLLEGE_LEADER、ROLE_CLASS_TEACHER）和学生角色
  if (currentRoles.includes(ROLES.COLLEGE_LEADER)) {
    if (roleCode === ROLES.SCHOOL_ADMIN) {
      return false
    }
    // 可以分配的教师相关角色和学生角色
    if (roleCode === ROLES.COLLEGE_LEADER || 
        roleCode === ROLES.CLASS_TEACHER ||
        roleCode === ROLES.STUDENT) {
      return true
    }
    // 不能分配企业相关角色等其他角色
    return false
  }
  
  // 班主任不能分配系统管理员、学校管理员、学院负责人角色
  if (currentRoles.includes(ROLES.CLASS_TEACHER)) {
    if (roleCode === ROLES.SCHOOL_ADMIN || roleCode === ROLES.COLLEGE_LEADER) {
      return false
    }
    // 只能分配学生角色
    if (roleCode === ROLES.STUDENT) {
      return true
    }
    return false
  }
  
  // 企业管理员只能分配企业导师角色
  if (currentRoles.includes(ROLES.ENTERPRISE_ADMIN)) {
    if (roleCode === ROLES.ENTERPRISE_MENTOR) {
      return true
    }
    return false
  }
  
  // 其他角色默认不能分配角色
  return false
}