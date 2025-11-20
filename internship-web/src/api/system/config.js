// 系统配置管理API
import request from '@/utils/request'

/**
 * 系统配置管理API
 */
export const systemConfigApi = {
  // 添加配置
  addConfig(data) {
    return request.post('/system/config', data)
  },
  // 更新配置
  updateConfig(id, data) {
    return request.put(`/system/config/${id}`, data)
  },
  // 查询配置详情
  getConfigById(id) {
    return request.get(`/system/config/${id}`)
  },
  // 根据配置键查询配置
  getConfigByKey(key) {
    return request.get(`/system/config/key/${key}`)
  },
  // 分页查询配置列表
  getConfigPage(params) {
    return request.get('/system/config/page', { params })
  },
  // 删除配置
  deleteConfig(id) {
    return request.delete(`/system/config/${id}`)
  }
}

