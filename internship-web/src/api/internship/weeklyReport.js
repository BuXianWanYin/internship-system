// 周报管理API
import request from '@/utils/request'

/**
 * 周报管理API
 */
export const weeklyReportApi = {
  // 提交周报
  addReport(data) {
    return request.post('/internship/weekly-report', data)
  },
  // 更新周报
  updateReport(data) {
    return request.put('/internship/weekly-report', data)
  },
  // 分页查询周报列表
  getReportPage(params) {
    return request.get('/internship/weekly-report/page', { params })
  },
  // 查询周报详情
  getReportById(reportId) {
    return request.get(`/internship/weekly-report/${reportId}`)
  },
  // 批阅周报
  reviewReport(reportId, reviewComment, reviewScore) {
    return request.post(`/internship/weekly-report/${reportId}/review`, null, {
      params: { reviewComment, reviewScore }
    })
  },
  // 删除周报
  deleteReport(reportId) {
    return request.delete(`/internship/weekly-report/${reportId}`)
  }
}

