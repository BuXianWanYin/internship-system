/**
 * 状态工具函数
 * 用于处理实习申请状态、在职状态等的显示和判断
 */

/**
 * 判断是否为实习结束
 * @param {Object} row - 申请记录对象
 * @returns {Boolean}
 */
export function isInternshipCompleted(row) {
  if (!row) return false
  // 合作企业：status=7
  if (row.applyType === 1) {
    return row.status === 7
  }
  // 自主实习：status=13
  if (row.applyType === 2) {
    return row.status === 13
  }
  return false
}

/**
 * 判断是否为提前离职（已解绑）
 * @param {Object} row - 申请记录对象
 * @returns {Boolean}
 */
export function isUnbound(row) {
  if (!row) return false
  // status=3（已录用）且 unbindStatus=2（已解绑），说明是提前离职
  return row.status === 3 && row.unbindStatus === 2
}

/**
 * 获取在职状态显示文本（区分提前离职和实习结束）
 * @param {Object} row - 申请记录对象
 * @returns {String} 状态文本
 */
export function getUnbindStatusText(row) {
  if (!row) return '在职'
  
  // 优先判断实习结束
  if (isInternshipCompleted(row)) {
    return '实习结束'
  }
  
  // 其次判断提前离职
  if (isUnbound(row)) {
    return '已离职'
  }
  
  // 其他解绑状态
  if (row.unbindStatus === 2) {
    return '已解绑'
  }
  if (row.unbindStatus === 1) {
    return '已申请解绑'
  }
  if (row.unbindStatus === 4) {
    return '企业已审批'
  }
  if (row.unbindStatus === 3) {
    return '解绑已拒绝'
  }
  
  return '在职'
}

/**
 * 获取在职状态标签类型
 * @param {Object} row - 申请记录对象
 * @returns {String} 标签类型（success/info/warning/danger）
 */
export function getUnbindStatusType(row) {
  if (!row) return 'success'
  
  // 实习结束：使用 info 类型（蓝色）
  if (isInternshipCompleted(row)) {
    return 'info'
  }
  
  // 提前离职：使用 danger 类型（红色）
  if (isUnbound(row)) {
    return 'danger'
  }
  
  // 其他解绑状态
  if (row.unbindStatus === 2) {
    return 'warning'
  }
  if (row.unbindStatus === 1 || row.unbindStatus === 4) {
    return 'warning'
  }
  
  return 'success'
}

/**
 * 获取申请状态显示文本
 * @param {Number} status - 状态码
 * @param {Number} applyType - 申请类型（1-合作企业，2-自主实习）
 * @returns {String} 状态文本
 */
export function getApplyStatusText(status, applyType) {
  if (status === null || status === undefined) return '-'
  
  // 合作企业
  if (applyType === 1) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      3: '已录用',
      4: '已拒绝录用',
      5: '已取消',
      7: '实习结束'
    }
    return statusMap[status] || '-'
  }
  
  // 自主实习
  if (applyType === 2) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      5: '已取消',
      11: '实习中',
      13: '实习结束'
    }
    return statusMap[status] || '-'
  }
  
  return '-'
}

/**
 * 获取申请状态标签类型
 * @param {Number} status - 状态码
 * @param {Number} applyType - 申请类型（1-合作企业，2-自主实习）
 * @returns {String} 标签类型
 */
export function getApplyStatusType(status, applyType) {
  if (status === null || status === undefined) return 'info'
  
  // 合作企业
  if (applyType === 1) {
    if (status === 7) return 'info' // 实习结束
    if (status === 3) return 'success' // 已录用
    if (status === 1) return 'success' // 已通过
    if (status === 2 || status === 4 || status === 5) return 'danger' // 已拒绝/已拒绝录用/已取消
    return 'warning' // 待审核
  }
  
  // 自主实习
  if (applyType === 2) {
    if (status === 13) return 'info' // 实习结束
    if (status === 11) return 'success' // 实习中
    if (status === 1) return 'success' // 已通过
    if (status === 2 || status === 5) return 'danger' // 已拒绝/已取消
    return 'warning' // 待审核
  }
  
  return 'info'
}

