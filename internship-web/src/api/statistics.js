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
  // 获取学院维度统计
  getCollegeStatistics(params) {
    return request.get('/statistics/college', { params })
  },
  // 获取学生评价分数排行（班主任使用）
  getStudentScoreRanking(params) {
    return request.get('/statistics/student-score-ranking', { params })
  },
  // 获取待批阅统计（班主任使用）
  getPendingReviewStatistics(params) {
    return request.get('/statistics/pending-review', { params })
  },
  // 获取系统管理员仪表盘统计数据
  getSystemAdminDashboard() {
    return request.get('/statistics/system-admin-dashboard')
  },
  // 获取学校管理员仪表盘统计数据
  getSchoolAdminDashboard() {
    return request.get('/statistics/school-admin-dashboard')
  },
  // 获取学院负责人仪表盘统计数据
  getCollegeLeaderDashboard() {
    return request.get('/statistics/college-leader-dashboard')
  },
  // 获取班主任仪表盘统计数据
  getClassTeacherDashboard() {
    return request.get('/statistics/class-teacher-dashboard')
  }
}

