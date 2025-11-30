<template>
  <PageLayout title="我的综合成绩">
    <el-card v-loading="loading" shadow="never">
      <div v-if="!score && !loading && !enterpriseEvaluation && !schoolEvaluation && !selfEvaluation">
        <el-empty description="暂无综合成绩信息，请等待所有评价完成后计算综合成绩" />
      </div>
      <div v-else-if="!score && (enterpriseEvaluation || schoolEvaluation || selfEvaluation) && !loading">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #title>
            <span>评价已完成，但综合成绩尚未计算。请联系班主任计算综合成绩。</span>
          </template>
        </el-alert>
        
        <!-- 显示已完成的评价信息 -->
        <div class="evaluation-details">
          <h3>评价详情</h3>
          
          <!-- 企业评价 -->
          <el-card v-if="enterpriseEvaluation" class="evaluation-card">
            <template #header>
              <div class="card-header">
                <span>企业评价：{{ enterpriseEvaluation.totalScore }}分</span>
                <el-tag v-if="enterpriseEvaluation.evaluationStatus === 1" type="success" size="small">已提交</el-tag>
                <el-tag v-else type="warning" size="small">草稿</el-tag>
              </div>
            </template>
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
          </el-card>

          <!-- 学校评价 -->
          <el-card v-if="schoolEvaluation" class="evaluation-card" style="margin-top: 15px;">
            <template #header>
              <div class="card-header">
                <span>学校评价：{{ schoolEvaluation.totalScore }}分</span>
                <el-tag v-if="schoolEvaluation.evaluationStatus === 1" type="success" size="small">已提交</el-tag>
                <el-tag v-else type="warning" size="small">草稿</el-tag>
              </div>
            </template>
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
          </el-card>

          <!-- 学生自评 -->
          <el-card v-if="selfEvaluation" class="evaluation-card" style="margin-top: 15px;">
            <template #header>
              <div class="card-header">
                <span>学生自评：{{ selfEvaluation.selfScore }}分</span>
                <el-tag v-if="selfEvaluation.evaluationStatus === 1" type="success" size="small">已提交</el-tag>
                <el-tag v-else type="warning" size="small">草稿</el-tag>
              </div>
            </template>
            <el-descriptions :column="1" border>
              <el-descriptions-item label="自评分数">
                <strong style="color: #409eff;">{{ selfEvaluation.selfScore }}分</strong>
              </el-descriptions-item>
            </el-descriptions>
            <div v-if="selfEvaluation.reflectionSummary" style="margin-top: 15px;">
              <div style="font-weight: 600; margin-bottom: 5px;">自我反思和总结：</div>
              <div class="reflection-content" v-html="selfEvaluation.reflectionSummary"></div>
            </div>
          </el-card>
        </div>
      </div>
      
      <div v-else-if="score">
        <!-- 实习信息 -->
        <div class="info-section">
          <h3>实习信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="企业名称">{{ score.enterpriseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="计算时间">{{ formatDateTime(score.calculateTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 综合成绩 -->
        <div class="score-section">
          <h3>综合成绩</h3>
          <el-card class="score-card" :class="getGradeClass(score.gradeLevel)">
            <div class="score-main">
              <div class="score-value">{{ score.comprehensiveScore }}分</div>
              <div class="score-grade">{{ score.gradeLevel }}</div>
            </div>
            <div class="score-time">计算时间：{{ formatDateTime(score.calculateTime) }}</div>
          </el-card>
        </div>

        <!-- 评价详情 -->
        <div class="evaluation-details">
          <h3>评价详情</h3>
          
          <!-- 企业评价 -->
          <el-card v-if="score.enterpriseScore" class="evaluation-card">
            <template #header>
              <div class="card-header">
                <span>企业评价：{{ score.enterpriseScore }}分</span>
                <el-tag type="info" size="small">权重40%</el-tag>
              </div>
            </template>
            <div v-if="enterpriseEvaluation">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="工作态度">{{ enterpriseEvaluation.workAttitudeScore }}分</el-descriptions-item>
                <el-descriptions-item label="专业知识应用">{{ enterpriseEvaluation.knowledgeApplicationScore }}分</el-descriptions-item>
                <el-descriptions-item label="专业技能">{{ enterpriseEvaluation.professionalSkillScore }}分</el-descriptions-item>
                <el-descriptions-item label="团队协作">{{ enterpriseEvaluation.teamworkScore }}分</el-descriptions-item>
                <el-descriptions-item label="创新意识">{{ enterpriseEvaluation.innovationScore }}分</el-descriptions-item>
                <el-descriptions-item label="总分">{{ enterpriseEvaluation.totalScore }}分</el-descriptions-item>
              </el-descriptions>
              <div v-if="enterpriseEvaluation.evaluationComment" style="margin-top: 15px;">
                <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
                <div style="color: #606266;">{{ enterpriseEvaluation.evaluationComment }}</div>
              </div>
            </div>
            <div v-else style="color: #909399;">暂无评价详情</div>
          </el-card>

          <!-- 学校评价 -->
          <el-card v-if="score.schoolScore" class="evaluation-card" style="margin-top: 15px;">
            <template #header>
              <div class="card-header">
                <span>学校评价：{{ score.schoolScore }}分</span>
                <el-tag type="info" size="small">权重40%</el-tag>
              </div>
            </template>
            <div v-if="schoolEvaluation">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="日志周报质量">{{ schoolEvaluation.logWeeklyReportScore }}分</el-descriptions-item>
                <el-descriptions-item label="过程表现">{{ schoolEvaluation.processPerformanceScore }}分</el-descriptions-item>
                <el-descriptions-item label="成果展示">{{ schoolEvaluation.achievementScore }}分</el-descriptions-item>
                <el-descriptions-item label="总结反思">{{ schoolEvaluation.summaryReflectionScore }}分</el-descriptions-item>
                <el-descriptions-item label="总分">{{ schoolEvaluation.totalScore }}分</el-descriptions-item>
              </el-descriptions>
              <div v-if="schoolEvaluation.evaluationComment" style="margin-top: 15px;">
                <div style="font-weight: 600; margin-bottom: 5px;">评价意见：</div>
                <div style="color: #606266;">{{ schoolEvaluation.evaluationComment }}</div>
              </div>
            </div>
            <div v-else style="color: #909399;">暂无评价详情</div>
          </el-card>

          <!-- 学生自评 -->
          <el-card v-if="score.selfScore" class="evaluation-card" style="margin-top: 15px;">
            <template #header>
              <div class="card-header">
                <span>学生自评：{{ score.selfScore }}分</span>
                <el-tag type="info" size="small">权重20%</el-tag>
              </div>
            </template>
            <div v-if="selfEvaluation">
              <el-descriptions :column="1" border>
                <el-descriptions-item label="自评分数">{{ selfEvaluation.selfScore }}分</el-descriptions-item>
              </el-descriptions>
              <div v-if="selfEvaluation.reflectionSummary" style="margin-top: 15px;">
                <div style="font-weight: 600; margin-bottom: 5px;">自我反思和总结：</div>
                <div class="reflection-content" v-html="selfEvaluation.reflectionSummary"></div>
              </div>
            </div>
            <div v-else style="color: #909399;">暂无评价详情</div>
          </el-card>
        </div>

        <!-- 成绩计算说明 -->
        <div class="calculation-section">
          <h3>成绩计算说明</h3>
          <el-card>
            <div class="calculation-formula">
              综合成绩 = 企业评价{{ score.enterpriseScore || 0 }}分 × 40% + 学校评价{{ score.schoolScore || 0 }}分 × 40% + 自评{{ score.selfScore || 0 }}分 × 20%
            </div>
            <div class="calculation-result">
              = {{ calculatePart(score.enterpriseScore, 0.4) }} + {{ calculatePart(score.schoolScore, 0.4) }} + {{ calculatePart(score.selfScore, 0.2) }} = {{ score.comprehensiveScore }}分
            </div>
          </el-card>
        </div>
      </div>
    </el-card>
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageLayout from '@/components/common/PageLayout.vue'
import { comprehensiveScoreApi } from '@/api/evaluation/comprehensiveScore'
import { enterpriseEvaluationApi } from '@/api/evaluation/enterprise'
import { schoolEvaluationApi } from '@/api/evaluation/school'
import { selfEvaluationApi } from '@/api/evaluation/self'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const score = ref(null)
const enterpriseEvaluation = ref(null)
const schoolEvaluation = ref(null)
const selfEvaluation = ref(null)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 获取当前学生的实习申请（查询所有，然后过滤出实习结束的）
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 10
    })
    
    if (res.code === 200 && res.data && res.data.records) {
      // 查找实习已结束的申请（合作企业：status=7，自主实习：status=13）
      const endedApply = res.data.records.find(apply => {
        // 合作企业：status=7
        if (apply.applyType === 1 && apply.status === 7) {
          return true
        }
        // 自主实习：status=13
        if (apply.applyType === 2 && apply.status === 13) {
          return true
        }
        return false
      })
      
      if (endedApply && endedApply.applyId) {
        // 加载综合成绩（可能还未计算）
        const scoreRes = await comprehensiveScoreApi.getScoreByApplyId(endedApply.applyId)
        if (scoreRes.code === 200 && scoreRes.data) {
          score.value = scoreRes.data
          
          // 加载详细评价信息
          await Promise.all([
            loadEnterpriseEvaluation(endedApply.applyId),
            loadSchoolEvaluation(endedApply.applyId),
            loadSelfEvaluation(endedApply.applyId)
          ])
        } else {
          // 如果综合成绩未计算，仍然加载评价信息（用于查看评价详情）
          await Promise.all([
            loadEnterpriseEvaluation(endedApply.applyId),
            loadSchoolEvaluation(endedApply.applyId),
            loadSelfEvaluation(endedApply.applyId)
          ])
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载企业评价
const loadEnterpriseEvaluation = async (applyId) => {
  try {
    const res = await enterpriseEvaluationApi.getEvaluationByApplyId(applyId)
    if (res.code === 200 && res.data) {
      enterpriseEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载企业评价失败:', error)
  }
}

// 加载学校评价
const loadSchoolEvaluation = async (applyId) => {
  try {
    const res = await schoolEvaluationApi.getEvaluationByApplyId(applyId)
    if (res.code === 200 && res.data) {
      schoolEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载学校评价失败:', error)
  }
}

// 加载学生自评
const loadSelfEvaluation = async (applyId) => {
  try {
    const res = await selfEvaluationApi.getEvaluationByApplyId(applyId)
    if (res.code === 200 && res.data) {
      selfEvaluation.value = res.data
    }
  } catch (error) {
    console.error('加载学生自评失败:', error)
  }
}

// 计算部分分数
const calculatePart = (score, weight) => {
  if (!score) return '0.00'
  return (score * weight).toFixed(2)
}

// 获取等级样式类
const getGradeClass = (grade) => {
  const gradeMap = {
    '优秀': 'grade-excellent',
    '良好': 'grade-good',
    '中等': 'grade-medium',
    '及格': 'grade-pass',
    '不及格': 'grade-fail'
  }
  return gradeMap[grade] || 'grade-medium'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.info-section,
.score-section,
.evaluation-details,
.calculation-section {
  margin-bottom: 30px;
}

.info-section h3,
.score-section h3,
.evaluation-details h3,
.calculation-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.score-card {
  text-align: center;
  padding: 40px;
}

.score-main {
  margin-bottom: 15px;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.score-grade {
  font-size: 24px;
  color: #909399;
}

.score-time {
  font-size: 14px;
  color: #909399;
}

.grade-excellent .score-value {
  color: #67c23a;
}

.grade-good .score-value {
  color: #409eff;
}

.grade-medium .score-value {
  color: #e6a23c;
}

.grade-pass .score-value {
  color: #f56c6c;
}

.grade-fail .score-value {
  color: #909399;
}

.evaluation-card {
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

.calculation-formula,
.calculation-result {
  font-size: 14px;
  color: #606266;
  line-height: 2;
  font-family: monospace;
}
</style>

