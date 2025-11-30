// 配置工具函数
import { systemConfigApi } from '@/api/system/config'

// 默认权重值
const DEFAULT_WEIGHTS = {
  enterprise: 0.4,
  school: 0.4,
  self: 0.2,
  selfInternshipSchool: 0.8,
  selfInternshipSelf: 0.2
}

// 配置缓存
let weightsCache = null
let cacheTime = null
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

/**
 * 获取评价权重配置（使用公开接口，带缓存）
 * @returns {Promise<Object>}
 */
export async function getEvaluationWeights() {
  // 检查缓存
  if (weightsCache && cacheTime && Date.now() - cacheTime < CACHE_DURATION) {
    return weightsCache
  }

  try {
    const res = await systemConfigApi.getEvaluationWeights()
    if (res.code === 200 && res.data) {
      const weights = {
        enterprise: parseFloat(res.data.enterprise || DEFAULT_WEIGHTS.enterprise),
        school: parseFloat(res.data.school || DEFAULT_WEIGHTS.school),
        self: parseFloat(res.data.self || DEFAULT_WEIGHTS.self),
        selfInternshipSchool: parseFloat(res.data.selfInternshipSchool || DEFAULT_WEIGHTS.selfInternshipSchool),
        selfInternshipSelf: parseFloat(res.data.selfInternshipSelf || DEFAULT_WEIGHTS.selfInternshipSelf)
      }
      weightsCache = weights
      cacheTime = Date.now()
      return weights
    }
  } catch (error) {
    console.warn('获取评价权重配置失败，使用默认值:', error)
  }

  // 返回默认值
  weightsCache = DEFAULT_WEIGHTS
  cacheTime = Date.now()
  return DEFAULT_WEIGHTS
}

/**
 * 清除配置缓存
 */
export function clearConfigCache() {
  weightsCache = null
  cacheTime = null
}

