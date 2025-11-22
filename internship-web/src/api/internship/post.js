// 实习岗位管理API
import request from '@/utils/request'

/**
 * 实习岗位管理API
 */
export const postApi = {
  // 发布岗位
  addPost(data) {
    return request.post('/internship/post', data)
  },
  // 更新岗位信息
  updatePost(data) {
    return request.put('/internship/post', data)
  },
  // 分页查询岗位列表
  getPostPage(params) {
    return request.get('/internship/post/page', { params })
  },
  // 查询岗位详情
  getPostById(postId) {
    return request.get(`/internship/post/${postId}`)
  },
  // 审核岗位
  auditPost(postId, auditStatus, auditOpinion) {
    return request.post(`/internship/post/${postId}/audit`, null, {
      params: { auditStatus, auditOpinion }
    })
  },
  // 发布岗位
  publishPost(postId) {
    return request.post(`/internship/post/${postId}/publish`)
  },
  // 关闭岗位
  closePost(postId) {
    return request.post(`/internship/post/${postId}/close`)
  },
  // 删除岗位
  deletePost(postId) {
    return request.delete(`/internship/post/${postId}`)
  },
  // 查询岗位申请情况
  getPostApplications(postId) {
    return request.get(`/internship/post/${postId}/applications`)
  }
}

