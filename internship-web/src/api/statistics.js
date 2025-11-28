// 数据统计管理API
import request from '@/utils/request'

/**
 * 数据统计管理API
 */
export const statisticsApi = {
  // 获取实习进度统计
  getInternshipProgressStatistics(params) {
    return request.get('/statistics/internship-progress', { params })
  },
  // 获取评价分数统计
  getEvaluationScoreStatistics(params) {
    return request.get('/statistics/evaluation-score', { params })
  },
  // 获取实习时长统计
  getInternshipDurationStatistics(params) {
    return request.get('/statistics/internship-duration', { params })
  },
  // 获取岗位类型分布统计
  getPostTypeDistributionStatistics(params) {
    return request.get('/statistics/post-type-distribution', { params })
  },
  // 获取专业维度统计
  getMajorStatistics(params) {
    return request.get('/statistics/major', { params })
  },
  // 获取班级维度统计
  getClassStatistics(params) {
    return request.get('/statistics/class', { params })
  },
  // 获取企业维度统计
  getEnterpriseStatistics(params) {
    return request.get('/statistics/enterprise', { params })
  },
  // 获取学生评价分数排行（班主任使用）
  getStudentScoreRanking(params) {
    return request.get('/statistics/student-score-ranking', { params })
  },
  // 获取待批阅统计（班主任使用）
  getPendingReviewStatistics(params) {
    return request.get('/statistics/pending-review', { params })
  }
}

