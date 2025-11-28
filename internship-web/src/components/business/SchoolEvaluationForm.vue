<template>
  <div class="school-evaluation-form">
    <!-- 学生信息 -->
    <el-card class="student-info-card" shadow="never">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="学生姓名">{{ student?.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ student?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ student?.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习时间">
          {{ formatDate(student?.startDate) }} 至 {{ formatDate(student?.endDate) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 参考信息（可折叠） -->
    <el-card class="reference-info-card" shadow="never" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>参考信息</span>
          <el-button link type="primary" size="small" @click="loadSuggestedScore">一键填入建议分数</el-button>
        </div>
      </template>
      <el-collapse v-model="activeCollapse">
        <el-collapse-item title="日志周报情况" name="logs">
          <div v-if="referenceInfo.logs.length > 0">
            <p>日志：已提交{{ referenceInfo.logs.length }}篇</p>
            <p>平均批阅分数：{{ calculateAverageScore(referenceInfo.logs) }}分</p>
            <el-button link type="primary" size="small" @click="viewLogs">查看日志列表</el-button>
          </div>
          <div v-else style="color: #909399;">暂无日志数据</div>
        </el-collapse-item>
        <el-collapse-item title="周报情况" name="reports">
          <div v-if="referenceInfo.reports.length > 0">
            <p>周报：已提交{{ referenceInfo.reports.length }}篇</p>
            <p>平均批阅分数：{{ calculateAverageScore(referenceInfo.reports) }}分</p>
            <el-button link type="primary" size="small" @click="viewReports">查看周报列表</el-button>
          </div>
          <div v-else style="color: #909399;">暂无周报数据</div>
        </el-collapse-item>
        <el-collapse-item title="阶段性成果" name="achievements">
          <div v-if="referenceInfo.achievements.length > 0">
            <p>已提交{{ referenceInfo.achievements.length }}个成果</p>
            <el-button link type="primary" size="small" @click="viewAchievements">查看成果列表</el-button>
          </div>
          <div v-else style="color: #909399;">暂无成果数据</div>
        </el-collapse-item>
        <el-collapse-item title="考勤记录" name="attendance">
          <div v-if="referenceInfo.attendance">
            <p>出勤率：{{ referenceInfo.attendance.attendanceRate }}%</p>
            <p>迟到：{{ referenceInfo.attendance.lateCount }}次</p>
            <p>请假：{{ referenceInfo.attendance.leaveCount }}次</p>
            <el-button link type="primary" size="small" @click="viewAttendance">查看考勤记录</el-button>
          </div>
          <div v-else style="color: #909399;">暂无考勤数据</div>
        </el-collapse-item>
      </el-collapse>
    </el-card>

    <!-- 评价表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
      style="margin-top: 20px"
    >
      <el-form-item label="日志周报质量" prop="logWeeklyReportScore">
        <el-input-number
          v-model="formData.logWeeklyReportScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入日志周报质量评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          说明：可参考建议分数，日志平均分×50% + 周报平均分×50%
        </div>
      </el-form-item>

      <el-form-item label="过程表现" prop="processPerformanceScore">
        <el-input-number
          v-model="formData.processPerformanceScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入过程表现评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="成果展示" prop="achievementScore">
        <el-input-number
          v-model="formData.achievementScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入成果展示评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="总结反思" prop="summaryReflectionScore">
        <el-input-number
          v-model="formData.summaryReflectionScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入总结反思评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="总分">
        <el-input :value="formData.totalScore" disabled />
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          自动计算（4项指标的平均分）
        </div>
      </el-form-item>

      <el-form-item label="评价意见">
        <el-input
          v-model="formData.evaluationComment"
          type="textarea"
          :rows="4"
          placeholder="请输入评价意见（可选）"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存草稿</el-button>
        <el-button type="success" :loading="submitting" @click="handleSubmit">提交评价</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { schoolEvaluationApi } from '@/api/evaluation/school'
import { formatDate } from '@/utils/dateUtils'

const props = defineProps({
  student: {
    type: Object,
    default: null
  },
  evaluation: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['save', 'submit'])

const formRef = ref(null)
const saving = ref(false)
const submitting = ref(false)
const activeCollapse = ref([])

const formData = reactive({
  evaluationId: null,
  applyId: null,
  logWeeklyReportScore: null,
  processPerformanceScore: null,
  achievementScore: null,
  summaryReflectionScore: null,
  totalScore: null,
  evaluationComment: ''
})

const formRules = {
  logWeeklyReportScore: [
    { required: true, message: '请输入日志周报质量评分', trigger: 'blur' }
  ],
  processPerformanceScore: [
    { required: true, message: '请输入过程表现评分', trigger: 'blur' }
  ],
  achievementScore: [
    { required: true, message: '请输入成果展示评分', trigger: 'blur' }
  ],
  summaryReflectionScore: [
    { required: true, message: '请输入总结反思评分', trigger: 'blur' }
  ]
}

const referenceInfo = reactive({
  logs: [],
  reports: [],
  achievements: [],
  attendance: null
})

// 加载参考信息
const loadReferenceInfo = async () => {
  if (!props.student || !props.student.applyId) return
  
  try {
    // TODO: 加载日志、周报、成果、考勤数据
    // 这里需要调用相应的API
  } catch (error) {
    console.error('加载参考信息失败:', error)
  }
}

// 计算平均分数
const calculateAverageScore = (items) => {
  if (!items || items.length === 0) return 0
  const scores = items.filter(item => item.reviewScore != null).map(item => item.reviewScore)
  if (scores.length === 0) return 0
  const sum = scores.reduce((a, b) => a + b, 0)
  return (sum / scores.length).toFixed(2)
}

// 加载建议分数
const loadSuggestedScore = async () => {
  if (!props.student || !props.student.applyId) {
    ElMessage.warning('请先选择学生')
    return
  }
  
  try {
    const res = await schoolEvaluationApi.getSuggestedLogWeeklyReportScore(props.student.applyId)
    if (res.code === 200 && res.data) {
      formData.logWeeklyReportScore = res.data
      calculateTotalScore()
      ElMessage.success('已填入建议分数')
    } else {
      ElMessage.warning('暂无建议分数数据')
    }
  } catch (error) {
    ElMessage.error('获取建议分数失败：' + (error.message || '未知错误'))
  }
}

// 计算总分
const calculateTotalScore = () => {
  const scores = [
    formData.logWeeklyReportScore,
    formData.processPerformanceScore,
    formData.achievementScore,
    formData.summaryReflectionScore
  ].filter(score => score !== null && score !== undefined)
  
  if (scores.length === 4) {
    const sum = scores.reduce((a, b) => a + b, 0)
    formData.totalScore = (sum / 4).toFixed(2)
  }
}

// 保存草稿
const handleSave = async () => {
  if (!formRef.value) return
  
  // 草稿保存时不强制验证所有字段
  saving.value = true
  try {
    const data = {
      evaluationId: formData.evaluationId,
      applyId: formData.applyId,
      logWeeklyReportScore: formData.logWeeklyReportScore,
      processPerformanceScore: formData.processPerformanceScore,
      achievementScore: formData.achievementScore,
      summaryReflectionScore: formData.summaryReflectionScore,
      totalScore: formData.totalScore ? parseFloat(formData.totalScore) : null,
      evaluationComment: formData.evaluationComment,
      evaluationStatus: 0 // 草稿
    }
    emit('save', data)
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 提交评价
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await ElMessageBox.confirm('提交后将无法修改，确认提交吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      submitting.value = true
      try {
        const data = {
          evaluationId: formData.evaluationId,
          applyId: formData.applyId,
          logWeeklyReportScore: formData.logWeeklyReportScore,
          processPerformanceScore: formData.processPerformanceScore,
          achievementScore: formData.achievementScore,
          summaryReflectionScore: formData.summaryReflectionScore,
          totalScore: formData.totalScore ? parseFloat(formData.totalScore) : null,
          evaluationComment: formData.evaluationComment,
          evaluationStatus: 1 // 已提交
        }
        emit('save', data)
        // 等待保存完成后提交
        setTimeout(() => {
          if (formData.evaluationId) {
            emit('submit', formData.evaluationId)
          } else if (data.evaluationId) {
            emit('submit', data.evaluationId)
          }
        }, 500)
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('提交失败：' + (error.message || '未知错误'))
        }
      } finally {
        submitting.value = false
      }
    } catch (error) {
      // 用户取消
    }
  })
}

// 查看日志
const viewLogs = () => {
  // TODO: 跳转到日志列表页面
  ElMessage.info('功能开发中')
}

// 查看周报
const viewReports = () => {
  // TODO: 跳转到周报列表页面
  ElMessage.info('功能开发中')
}

// 查看成果
const viewAchievements = () => {
  // TODO: 跳转到成果列表页面
  ElMessage.info('功能开发中')
}

// 查看考勤
const viewAttendance = () => {
  // TODO: 跳转到考勤记录页面
  ElMessage.info('功能开发中')
}

// 监听评价变化，填充表单
watch(() => props.evaluation, (newVal) => {
  if (newVal) {
    formData.evaluationId = newVal.evaluationId
    formData.applyId = newVal.applyId
    formData.logWeeklyReportScore = newVal.logWeeklyReportScore
    formData.processPerformanceScore = newVal.processPerformanceScore
    formData.achievementScore = newVal.achievementScore
    formData.summaryReflectionScore = newVal.summaryReflectionScore
    formData.totalScore = newVal.totalScore
    formData.evaluationComment = newVal.evaluationComment || ''
  }
}, { immediate: true, deep: true })

// 监听学生变化
watch(() => props.student, (newVal) => {
  if (newVal) {
    formData.applyId = newVal.applyId
    loadReferenceInfo()
  }
}, { immediate: true })

onMounted(() => {
  if (props.student) {
    loadReferenceInfo()
  }
})
</script>

<style scoped>
.school-evaluation-form {
  padding: 20px 0;
}

.student-info-card,
.reference-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

