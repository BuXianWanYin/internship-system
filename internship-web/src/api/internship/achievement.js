// 阶段性成果管理API
import request from '@/utils/request'

/**
 * 阶段性成果管理API
 */
export const achievementApi = {
  // 提交阶段性成果
  addAchievement(data) {
    return request.post('/internship/achievement', data)
  },
  // 更新阶段性成果
  updateAchievement(data) {
    return request.put('/internship/achievement', data)
  },
  // 分页查询阶段性成果列表
  getAchievementPage(params) {
    return request.get('/internship/achievement/page', { params })
  },
  // 查询阶段性成果详情
  getAchievementById(achievementId) {
    return request.get(`/internship/achievement/${achievementId}`)
  },
  // 审核成果
  reviewAchievement(achievementId, reviewStatus, reviewComment) {
    return request.post(`/internship/achievement/${achievementId}/review`, null, {
      params: { reviewStatus, reviewComment }
    })
  },
  // 删除成果
  deleteAchievement(achievementId) {
    return request.delete(`/internship/achievement/${achievementId}`)
  }
}

