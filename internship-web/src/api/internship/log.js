// 实习日志管理API
import request from '@/utils/request'

/**
 * 实习日志管理API
 */
export const logApi = {
  // 提交实习日志
  addLog(data) {
    return request.post('/internship/log', data)
  },
  // 更新实习日志
  updateLog(data) {
    return request.put('/internship/log', data)
  },
  // 分页查询实习日志列表
  getLogPage(params) {
    return request.get('/internship/log/page', { params })
  },
  // 查询实习日志详情
  getLogById(logId) {
    return request.get(`/internship/log/${logId}`)
  },
  // 批阅日志
  reviewLog(logId, reviewComment, reviewScore) {
    return request.post(`/internship/log/${logId}/review`, null, {
      params: { reviewComment, reviewScore }
    })
  },
  // 删除日志
  deleteLog(logId) {
    return request.delete(`/internship/log/${logId}`)
  }
}

