<template>
  <div class="score-detail" v-loading="loading">
    <!-- 企业评价详情 -->
    <el-card v-if="comprehensiveScore && comprehensiveScore.enterpriseScore !== null && comprehensiveScore.enterpriseScore !== undefined" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>企业评价：{{ comprehensiveScore.enterpriseScore }}分</span>
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
            <strong style="color: #409eff;">{{ enterpriseEvaluation.totalScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="enterpriseEvaluation.evaluationComment" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
          <div style="color: #606266;">{{ enterpriseEvaluation.evaluationComment }}</div>
        </div>
      </div>
      <div v-else style="color: #909399;">暂无评价详情</div>
    </el-card>

    <!-- 学校评价详情 -->
    <el-card v-if="comprehensiveScore && comprehensiveScore.schoolScore !== null && comprehensiveScore.schoolScore !== undefined" class="detail-card" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>学校评价：{{ comprehensiveScore.schoolScore }}分</span>
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
            <strong style="color: #409eff;">{{ schoolEvaluation.totalScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="schoolEvaluation.evaluationComment" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
          <div style="color: #606266;">{{ schoolEvaluation.evaluationComment }}</div>
        </div>
      </div>
      <div v-else style="color: #909399;">暂无评价详情</div>
    </el-card>

    <!-- 学生自评详情 -->
    <el-card v-if="comprehensiveScore && comprehensiveScore.selfScore !== null && comprehensiveScore.selfScore !== undefined" class="detail-card" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>学生自评：{{ comprehensiveScore.selfScore }}分</span>
          <el-tag type="info" size="small">权重{{ getSelfWeight() }}%</el-tag>
        </div>
      </template>
      <div v-if="selfEvaluation">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="自评分数">
            <strong style="color: #409eff;">{{ selfEvaluation.selfScore }}分</strong>
          </el-descriptions-item>
        </el-descriptions>
        <div v-if="selfEvaluation.reflectionSummary" style="margin-top: 15px;">
          <div style="font-weight: 600; margin-bottom: 5px;">自我反思和总结：</div>
          <div class="reflection-content" v-html="selfEvaluation.reflectionSummary"></div>
        </div>
      </div>
      <div v-else style="color: #909399;">暂无评价详情</div>
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
          = {{ calculatePart(comprehensiveScore.enterpriseScore, getEnterpriseWeight() / 100) }} 
          + {{ calculatePart(comprehensiveScore.schoolScore, getSchoolWeight() / 100) }} 
          + {{ calculatePart(comprehensiveScore.selfScore, getSelfWeight() / 100) }} 
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
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { comprehensiveScoreApi } from '@/api/evaluation/comprehensiveScore'
import { enterpriseEvaluationApi } from '@/api/evaluation/enterprise'
import { schoolEvaluationApi } from '@/api/evaluation/school'
import { selfEvaluationApi } from '@/api/evaluation/self'
import { applyApi } from '@/api/internship/apply'

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

// 加载数据
const loadData = async () => {
  if (!props.applyId) return
  
  loading.value = true
  try {
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
  if (!props.applyId) return
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
  if (!props.applyId) return
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
  if (!props.applyId) return
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
  if (!applyInfo.value) return 40
  // 合作企业：40%，自主实习：0%
  return applyInfo.value.applyType === 1 ? 40 : 0
}

// 获取学校权重
const getSchoolWeight = () => {
  if (!applyInfo.value) return 40
  // 合作企业：40%，自主实习：60%
  return applyInfo.value.applyType === 1 ? 40 : 60
}

// 获取自评权重
const getSelfWeight = () => {
  if (!applyInfo.value) return 20
  // 合作企业：20%，自主实习：40%
  return applyInfo.value.applyType === 1 ? 20 : 40
}

// 获取计算公式
const getCalculationFormula = () => {
  if (!comprehensiveScore.value || !applyInfo.value) return ''
  
  const isCooperation = applyInfo.value.applyType === 1
  
  if (isCooperation) {
    return `综合成绩 = 企业评价${comprehensiveScore.value.enterpriseScore || 0}分 × 40% + 学校评价${comprehensiveScore.value.schoolScore || 0}分 × 40% + 自评${comprehensiveScore.value.selfScore || 0}分 × 20%`
  } else {
    return `综合成绩 = 学校评价${comprehensiveScore.value.schoolScore || 0}分 × 60% + 自评${comprehensiveScore.value.selfScore || 0}分 × 40%`
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

// 监听 applyId 变化
watch(() => props.applyId, (newVal) => {
  if (newVal) {
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
.score-detail {
  padding: 0;
}

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
  padding: 0;
}

.calculation-formula,
.calculation-result {
  font-size: 14px;
  color: #606266;
  line-height: 2;
  font-family: monospace;
}
</style>

