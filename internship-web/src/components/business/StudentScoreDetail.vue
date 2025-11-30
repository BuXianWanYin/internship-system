<template>
  <div class="score-detail" v-loading="loading">
    <!-- 企业评价详情（仅合作企业实习显示） -->
    <el-card v-if="comprehensiveScore?.enterpriseScore !== null && comprehensiveScore?.enterpriseScore !== undefined && isCooperation" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>企业评价：{{ comprehensiveScore?.enterpriseScore }}分</span>
          <el-tag type="info" size="small">权重{{ getEnterpriseWeight() }}%</el-tag>
        </div>
      </template>
      <div v-if="enterpriseEvaluation">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="工作态度">{{ enterpriseEvaluation.workAttitudeScore }}分</el-descriptions-item>
          <el-descriptions-item label="专业知识应用">{{ enterpriseEvaluation.knowledgeApplicationScore }}分</el-descriptions-item>
          <el-descriptions-item label="专业技能">{{ enterpriseEvaluation.professionalSkillScore }}分</el-descriptions-item>
          <el-descriptions-item label="团队协作">{{ enterpriseEvaluation.teamworkScore }}分</el-descriptions-item>
          <el-descriptions-item label="创新意识">{{ enterpriseEvaluation.innovationScore }}分</el-descriptions-item>
          <el-descriptions-item label="日志周报质量">{{ enterpriseEvaluation.logWeeklyReportScore }}分</el-descriptions-item>
          <el-descriptions-item label="总分" :span="2">
            <strong style="color: #409eff; font-size: 16px;">{{ enterpriseEvaluation.totalScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="enterpriseEvaluation.evaluationComment" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
          <div style="color: #606266;">{{ enterpriseEvaluation.evaluationComment }}</div>
        </div>
      </div>
      <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无评价详情</div>
    </el-card>

    <!-- 学校评价详情 -->
    <el-card v-if="comprehensiveScore?.schoolScore !== null && comprehensiveScore?.schoolScore !== undefined" class="detail-card" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>学校评价：{{ comprehensiveScore?.schoolScore }}分</span>
          <el-tag type="info" size="small">权重{{ getSchoolWeight() }}%</el-tag>
        </div>
      </template>
      <div v-if="schoolEvaluation">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志周报质量">{{ schoolEvaluation.logWeeklyReportScore }}分</el-descriptions-item>
          <el-descriptions-item label="过程表现">{{ schoolEvaluation.processPerformanceScore }}分</el-descriptions-item>
          <el-descriptions-item label="成果展示">{{ schoolEvaluation.achievementScore }}分</el-descriptions-item>
          <el-descriptions-item label="总结反思">{{ schoolEvaluation.summaryReflectionScore }}分</el-descriptions-item>
          <el-descriptions-item label="总分" :span="2">
            <strong style="color: #409eff; font-size: 16px;">{{ schoolEvaluation.totalScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="schoolEvaluation.evaluationComment" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
          <div style="color: #606266;">{{ schoolEvaluation.evaluationComment }}</div>
        </div>
      </div>
      <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无评价详情</div>
    </el-card>

    <!-- 学生自评详情 -->
    <el-card v-if="comprehensiveScore?.selfScore !== null && comprehensiveScore?.selfScore !== undefined" class="detail-card" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>学生自评：{{ comprehensiveScore?.selfScore }}分</span>
          <el-tag type="info" size="small">权重{{ getSelfWeight() }}%</el-tag>
        </div>
      </template>
      <div v-if="selfEvaluation">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="自评分数">
            <strong style="color: #409eff; font-size: 16px;">{{ selfEvaluation.selfScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="selfEvaluation.reflectionSummary" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">自我反思和总结：</div>
          <div class="reflection-content" v-html="selfEvaluation.reflectionSummary"></div>
        </div>
      </div>
      <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无评价详情</div>
    </el-card>

    <!-- 综合成绩计算 -->
    <el-card v-if="comprehensiveScore" class="detail-card" style="margin-top: 15px;">
      <template #header>
        <span>综合成绩计算</span>
      </template>
      <div class="calculation-section">
        <div class="calculation-formula">
          {{ getCalculationFormula() }}
        </div>
        <div class="calculation-result">
          = {{ getCalculationResult() }} 
          = <strong style="color: #409eff; font-size: 18px;">{{ comprehensiveScore.comprehensiveScore }}分</strong>
        </div>
        <div style="margin-top: 15px;">
          <el-tag 
            :type="getGradeTagType(comprehensiveScore.gradeLevel)" 
            size="large"
          >
            {{ comprehensiveScore.gradeLevel }}
          </el-tag>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { comprehensiveScoreApi } from '@/api/evaluation/comprehensiveScore'
import { enterpriseEvaluationApi } from '@/api/evaluation/enterprise'
import { schoolEvaluationApi } from '@/api/evaluation/school'
import { selfEvaluationApi } from '@/api/evaluation/self'
import { applyApi } from '@/api/internship/apply'
import { getEvaluationWeights } from '@/utils/configUtils'

const props = defineProps({
  applyId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const comprehensiveScore = ref(null)
const enterpriseEvaluation = ref(null)
const schoolEvaluation = ref(null)
const selfEvaluation = ref(null)
const applyInfo = ref(null)
const weights = ref({
  enterprise: 0.4,
  school: 0.4,
  self: 0.2,
  selfInternshipSchool: 0.8,
  selfInternshipSelf: 0.2
})

// 判断是否为合作企业实习
const isCooperation = computed(() => {
  return applyInfo.value && applyInfo.value.applyType === 1
})

// 加载数据
const loadData = async () => {
  // 重置数据，避免缓存问题
  comprehensiveScore.value = null
  enterpriseEvaluation.value = null
  schoolEvaluation.value = null
  selfEvaluation.value = null
  applyInfo.value = null
  
  loading.value = true
  try {
    // 加载权重配置
    try {
      weights.value = await getEvaluationWeights()
    } catch (error) {
      console.warn('加载权重配置失败，使用默认值:', error)
    }
    
    // 并行加载所有数据
    const [scoreRes, applyRes] = await Promise.all([
      comprehensiveScoreApi.getScoreByApplyId(props.applyId),
      applyApi.getApplyById(props.applyId)
    ])
    
    if (scoreRes.code === 200 && scoreRes.data) {
      comprehensiveScore.value = scoreRes.data
    }
    
    if (applyRes.code === 200 && applyRes.data) {
      applyInfo.value = applyRes.data
    }
    
    // 加载详细评价信息
    await Promise.all([
      loadEnterpriseEvaluation(),
      loadSchoolEvaluation(),
      loadSelfEvaluation()
    ])
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载企业评价
const loadEnterpriseEvaluation = async () => {
  try {
    const res = await enterpriseEvaluationApi.getEvaluationByApplyId(props.applyId)
    if (res.code === 200 && res.data) {
      enterpriseEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载企业评价失败:', error)
  }
}

// 加载学校评价
const loadSchoolEvaluation = async () => {
  try {
    const res = await schoolEvaluationApi.getEvaluationByApplyId(props.applyId)
    if (res.code === 200 && res.data) {
      schoolEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载学校评价失败:', error)
  }
}

// 加载学生自评
const loadSelfEvaluation = async () => {
  try {
    const res = await selfEvaluationApi.getEvaluationByApplyId(props.applyId)
    if (res.code === 200 && res.data) {
      selfEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载学生自评失败:', error)
  }
}

// 获取企业权重
const getEnterpriseWeight = () => {
  if (!applyInfo.value) return (weights.value.enterprise * 100).toFixed(0)
  return applyInfo.value.applyType === 1 ? (weights.value.enterprise * 100).toFixed(0) : 0
}

// 获取学校权重
const getSchoolWeight = () => {
  if (!applyInfo.value) return (weights.value.school * 100).toFixed(0)
  return applyInfo.value.applyType === 1 ? (weights.value.school * 100).toFixed(0) : (weights.value.selfInternshipSchool * 100).toFixed(0)
}

// 获取自评权重
const getSelfWeight = () => {
  if (!applyInfo.value) return (weights.value.self * 100).toFixed(0)
  return applyInfo.value.applyType === 1 ? (weights.value.self * 100).toFixed(0) : (weights.value.selfInternshipSelf * 100).toFixed(0)
}

// 获取计算公式
const getCalculationFormula = () => {
  if (!comprehensiveScore.value || !applyInfo.value) return ''
  
  if (isCooperation.value) {
    return `综合成绩 = 企业评价${comprehensiveScore.value.enterpriseScore || 0}分 × ${(weights.value.enterprise * 100).toFixed(0)}% + 学校评价${comprehensiveScore.value.schoolScore || 0}分 × ${(weights.value.school * 100).toFixed(0)}% + 自评${comprehensiveScore.value.selfScore || 0}分 × ${(weights.value.self * 100).toFixed(0)}%`
  } else {
    return `综合成绩 = 学校评价${comprehensiveScore.value.schoolScore || 0}分 × ${(weights.value.selfInternshipSchool * 100).toFixed(0)}% + 自评${comprehensiveScore.value.selfScore || 0}分 × ${(weights.value.selfInternshipSelf * 100).toFixed(0)}%`
  }
}

// 获取计算结果
const getCalculationResult = () => {
  if (!comprehensiveScore.value || !applyInfo.value) return ''
  
  if (isCooperation.value) {
    const enterprisePart = calculatePart(comprehensiveScore.value.enterpriseScore, weights.value.enterprise)
    const schoolPart = calculatePart(comprehensiveScore.value.schoolScore, weights.value.school)
    const selfPart = calculatePart(comprehensiveScore.value.selfScore, weights.value.self)
    return `${enterprisePart} + ${schoolPart} + ${selfPart}`
  } else {
    const schoolPart = calculatePart(comprehensiveScore.value.schoolScore, weights.value.selfInternshipSchool)
    const selfPart = calculatePart(comprehensiveScore.value.selfScore, weights.value.selfInternshipSelf)
    return `${schoolPart} + ${selfPart}`
  }
}

// 计算部分分数
const calculatePart = (score, weight) => {
  if (!score) return '0.00'
  return (score * weight).toFixed(2)
}

// 获取等级标签类型
const getGradeTagType = (gradeLevel) => {
  if (!gradeLevel) return ''
  const typeMap = {
    '优秀': 'success',
    '良好': '',
    '中等': 'warning',
    '及格': 'info',
    '不及格': 'danger'
  }
  return typeMap[gradeLevel] || ''
}

// 监听 applyId 变化，重新加载数据
watch(() => props.applyId, (newId) => {
  if (newId) {
    loadData()
  }
}, { immediate: true })

onMounted(() => {
  if (props.applyId) {
    loadData()
  }
})
</script>

<style scoped>
.detail-card {
  margin-bottom: 15px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reflection-content {
  color: #606266;
  line-height: 1.8;
  max-width: 100%;
  word-wrap: break-word;
}

.calculation-section {
  padding: 10px 0;
}

.calculation-formula,
.calculation-result {
  font-size: 14px;
  color: #606266;
  line-height: 2;
  font-family: monospace;
  margin-bottom: 10px;
}
</style>

