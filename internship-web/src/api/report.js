// 报表生成管理API
import request from '@/utils/request'

/**
 * 报表生成管理API
 */
export const reportApi = {
  // 导出实习情况汇总表
  exportInternshipSummaryReport(params) {
    return request.get('/report/internship-summary/export', {
      params,
      responseType: 'blob'
    })
  },
  // 导出评价成绩统计表
  exportEvaluationScoreReport(params) {
    return request.get('/report/evaluation-score/export', {
      params,
      responseType: 'blob'
    })
  },
  // 导出企业合作情况表
  exportEnterpriseCooperationReport(params) {
    return request.get('/report/enterprise-cooperation/export', {
      params,
      responseType: 'blob'
    })
  },
  // 导出学生个人实习报告
  exportStudentInternshipReport(applyId) {
    return request.get(`/report/student-internship/${applyId}/export`, {
      responseType: 'blob'
    })
  }
}

