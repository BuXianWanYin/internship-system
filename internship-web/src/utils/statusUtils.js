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
  // 合作企业：status=7（实习结束）或 status=8（已评价，也视为实习结束）
  if (row.applyType === 1) {
    return row.status === 7 || row.status === 8
  }
  // 自主实习：status=13（实习结束）
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
 * 判断是否已录用（开始实习）
 * @param {Object} row - 申请记录对象
 * @returns {Boolean}
 */
export function isAccepted(row) {
  if (!row) return false
  // 合作企业：status=3（已录用）
  if (row.applyType === 1) {
    return row.status === 3
  }
  // 自主实习：status=11（实习中）
  if (row.applyType === 2) {
    return row.status === 11
  }
  return false
}

/**
 * 获取在职状态显示文本（区分提前离职和实习结束）
 * @param {Object} row - 申请记录对象
 * @returns {String} 状态文本
 */
export function getUnbindStatusText(row) {
  if (!row) return '-'
  
  // 优先判断实习结束（合作企业status=7或8，自主实习status=13）
  // 实习结束后，无论unbindStatus是什么，都应该显示"实习结束"
  if (isInternshipCompleted(row)) {
    return '实习结束'
  }
  
  // 其次判断提前离职（已解绑）
  // 提前离职：status=3（已录用）且 unbindStatus=2（已解绑）
  if (isUnbound(row)) {
    return '已离职'
  }
  
  // 解绑状态映射（只有在已录用的状态下才显示解绑相关状态）：
  // 1-申请解绑 -> 申请离职中
  // 2-已解绑 -> 已离职（但这种情况应该被isUnbound处理，这里作为兜底）
  // 3-解绑被拒绝 -> 离职申请被拒绝
  // 注意：如果status=3（已录用）且unbindStatus=2（已解绑），说明是提前离职
  if (row.status === 3 && row.unbindStatus === 2) {
    return '已离职'
  }
  if (row.unbindStatus === 1) {
    return '申请离职中'
  }
  if (row.unbindStatus === 3) {
    return '离职申请被拒绝'
  }
  if (row.unbindStatus === 4) {
    return '企业已审批'
  }
  
  // 只有在已录用（开始实习）的情况下，unbindStatus=0或null才显示"在职"
  if (isAccepted(row)) {
    return '在职'
  }
  
  // 未录用状态（待审核、已拒绝等），不显示在职状态
  return '-'
}

/**
 * 获取在职状态标签类型
 * @param {Object} row - 申请记录对象
 * @returns {String} 标签类型（success/info/warning/danger）
 */
export function getUnbindStatusType(row) {
  if (!row) return 'info'
  
  // 实习结束：使用 info 类型（蓝色）
  // 实习结束后，无论unbindStatus是什么，都应该显示为info类型
  if (isInternshipCompleted(row)) {
    return 'info'
  }
  
  // 提前离职：使用 danger 类型（红色）
  // 提前离职：status=3（已录用）且 unbindStatus=2（已解绑）
  if (isUnbound(row)) {
    return 'danger'
  }
  
  // 其他解绑状态
  // 注意：如果status=3（已录用）且unbindStatus=2（已解绑），说明是提前离职
  if (row.status === 3 && row.unbindStatus === 2) {
    return 'danger'
  }
  if (row.unbindStatus === 1 || row.unbindStatus === 4) {
    return 'warning'
  }
  if (row.unbindStatus === 3) {
    return 'danger'
  }
  
  // 已录用且未申请解绑：在职
  if (isAccepted(row)) {
    return 'success'
  }
  
  // 未录用状态，返回 info（灰色）
  return 'info'
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
      7: '实习结束',
      8: '实习结束' // 已评价状态也显示为实习结束
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
    if (status === 7 || status === 8) return 'info' // 实习结束、已评价
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

