<template>
  <div class="enterprise-evaluation-form">
    <!-- 学生信息 -->
    <el-card class="student-info-card" shadow="never">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="学生姓名">{{ student?.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ student?.studentNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ student?.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习时间">
          {{ formatDate(student?.internshipStartDate || student?.startDate) }} 至 {{ formatDate(student?.internshipEndDate || student?.endDate) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 参考信息（Tab标签切换） -->
    <el-card class="reference-info-card" shadow="never" style="margin-top: 15px;">
      <template #header>
        <div class="card-header">
          <span>参考信息</span>
        </div>
      </template>
      <el-tabs v-model="activeReferenceTab" type="border-card">
        <!-- 日志周报情况 -->
        <el-tab-pane label="日志周报情况" name="logs">
          <div v-if="referenceInfo.logs.length > 0" style="padding: 10px 0;">
            <p>日志：已提交{{ referenceInfo.logs.length }}篇</p>
            <p>平均批阅分数：{{ calculateAverageScore(referenceInfo.logs) }}分</p>
            <el-button link type="primary" size="small" @click="viewLogs">查看日志列表</el-button>
            <!-- 日志列表 -->
            <el-table :data="referenceInfo.logs" border style="margin-top: 10px" max-height="300">
              <el-table-column prop="logTitle" label="日志标题" min-width="200" />
              <el-table-column prop="logDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.logDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewScore" label="批阅分数" width="100" align="center">
                <template #default="{ row }">
                  {{ row.reviewScore || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewStatus" label="批阅状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.reviewStatus === 1" type="success" size="small">已批阅</el-tag>
                  <el-tag v-else type="warning" size="small">未批阅</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无日志数据</div>
        </el-tab-pane>
        
        <!-- 周报情况 -->
        <el-tab-pane label="周报情况" name="reports">
          <div v-if="referenceInfo.reports.length > 0" style="padding: 10px 0;">
            <p>周报：已提交{{ referenceInfo.reports.length }}篇</p>
            <p>平均批阅分数：{{ calculateAverageScore(referenceInfo.reports) }}分</p>
            <el-button link type="primary" size="small" @click="viewReports">查看周报列表</el-button>
            <!-- 周报列表 -->
            <el-table :data="referenceInfo.reports" border style="margin-top: 10px" max-height="300">
              <el-table-column prop="reportTitle" label="周报标题" min-width="200" />
              <el-table-column prop="reportWeek" label="周次" width="100" align="center" />
              <el-table-column prop="reportDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.reportDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewScore" label="批阅分数" width="100" align="center">
                <template #default="{ row }">
                  {{ row.reviewScore || '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewStatus" label="批阅状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.reviewStatus === 1" type="success" size="small">已批阅</el-tag>
                  <el-tag v-else type="warning" size="small">未批阅</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无周报数据</div>
        </el-tab-pane>
        
        <!-- 阶段性成果 -->
        <el-tab-pane label="阶段性成果" name="achievements">
          <div v-if="referenceInfo.achievements.length > 0" style="padding: 10px 0;">
            <p>已提交{{ referenceInfo.achievements.length }}个成果</p>
            <el-button link type="primary" size="small" @click="viewAchievements">查看成果列表</el-button>
            <!-- 成果列表 -->
            <el-table :data="referenceInfo.achievements" border style="margin-top: 10px" max-height="300">
              <el-table-column prop="achievementTitle" label="成果标题" min-width="200" />
              <el-table-column prop="achievementType" label="成果类型" width="120" />
              <el-table-column prop="submitTime" label="提交时间" width="180">
                <template #default="{ row }">
                  {{ formatDateTime(row.submitTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="reviewStatus" label="审核状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.reviewStatus === 1" type="success" size="small">已通过</el-tag>
                  <el-tag v-else-if="row.reviewStatus === 2" type="danger" size="small">已拒绝</el-tag>
                  <el-tag v-else type="warning" size="small">待审核</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无成果数据</div>
        </el-tab-pane>
        
        <!-- 考勤记录 -->
        <el-tab-pane label="考勤记录" name="attendance">
          <div v-if="referenceInfo.attendance" style="padding: 10px 0;">
            <p>出勤率：{{ referenceInfo.attendance.attendanceRate || 0 }}%</p>
            <p>迟到：{{ referenceInfo.attendance.lateCount || 0 }}次</p>
            <p>请假：{{ referenceInfo.attendance.leaveCount || 0 }}次</p>
            <el-button link type="primary" size="small" @click="viewAttendance">查看考勤记录</el-button>
            <!-- 考勤记录列表 -->
            <el-table v-if="referenceInfo.attendanceRecords && referenceInfo.attendanceRecords.length > 0" 
                      :data="referenceInfo.attendanceRecords" border style="margin-top: 10px" max-height="300">
              <el-table-column prop="attendanceDate" label="日期" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.attendanceDate) }}
                </template>
              </el-table-column>
              <el-table-column prop="attendanceStatus" label="状态" width="100" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.attendanceStatus === 1" type="success" size="small">正常</el-tag>
                  <el-tag v-else-if="row.attendanceStatus === 2" type="warning" size="small">迟到</el-tag>
                  <el-tag v-else-if="row.attendanceStatus === 3" type="info" size="small">请假</el-tag>
                  <el-tag v-else type="danger" size="small">缺勤</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="checkInTime" label="签到时间" width="180">
                <template #default="{ row }">
                  {{ row.checkInTime ? formatDateTime(row.checkInTime) : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="checkOutTime" label="签退时间" width="180">
                <template #default="{ row }">
                  {{ row.checkOutTime ? formatDateTime(row.checkOutTime) : '-' }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无考勤数据</div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 评价表单 -->
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
      style="margin-top: 20px"
    >
      <!-- 评价指标 -->
      <el-form-item label="工作态度" prop="workAttitudeScore">
        <el-input-number
          v-model="formData.workAttitudeScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入工作态度评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="专业知识应用" prop="knowledgeApplicationScore">
        <el-input-number
          v-model="formData.knowledgeApplicationScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入专业知识应用评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="专业技能" prop="professionalSkillScore">
        <el-input-number
          v-model="formData.professionalSkillScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入专业技能评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="团队协作" prop="teamworkScore">
        <el-input-number
          v-model="formData.teamworkScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入团队协作评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="创新意识" prop="innovationScore">
        <el-input-number
          v-model="formData.innovationScore"
          :min="0"
          :max="100"
          :precision="2"
          placeholder="请输入创新意识评分（0-100分）"
          style="width: 100%"
          @change="calculateTotalScore"
        />
      </el-form-item>

      <el-form-item label="日志周报质量" prop="logWeeklyReportScore">
        <div style="display: flex; align-items: center; gap: 10px; width: 100%">
          <el-input-number
            v-model="formData.logWeeklyReportScore"
            :min="0"
            :max="100"
            :precision="2"
            placeholder="请输入日志周报质量评分（0-100分）"
            style="flex: 1"
            @change="calculateTotalScore"
          />
          <el-button
            v-if="formData.logWeeklyReportScoreAuto !== null && formData.logWeeklyReportScoreAuto !== undefined"
            type="info"
            size="small"
            @click="useAutoScore"
          >
            使用自动计算值 ({{ formData.logWeeklyReportScoreAuto }})
          </el-button>
        </div>
        <div v-if="formData.logWeeklyReportScoreAuto !== null && formData.logWeeklyReportScoreAuto !== undefined" 
             style="margin-top: 5px; font-size: 12px; color: #909399;">
          系统自动计算值：{{ formData.logWeeklyReportScoreAuto }} 分（基于日志和周报的平均分）
        </div>
      </el-form-item>

      <el-form-item label="总分">
        <el-input :value="formData.totalScore" disabled />
        <div style="margin-top: 5px; font-size: 12px; color: #909399;">
          自动计算（6项指标的平均分）
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
import { logApi } from '@/api/internship/log'
import { weeklyReportApi } from '@/api/internship/weeklyReport'
import { achievementApi } from '@/api/internship/achievement'
import { attendanceApi } from '@/api/internship/attendance'
import { formatDate, formatDateTime } from '@/utils/dateUtils'

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
const activeReferenceTab = ref('logs')

const referenceInfo = reactive({
  logs: [],
  reports: [],
  achievements: [],
  attendance: null,
  attendanceRecords: []
})

const formData = reactive({
  evaluationId: null,
  applyId: null,
  enterpriseId: null,
  workAttitudeScore: null,
  knowledgeApplicationScore: null,
  professionalSkillScore: null,
  teamworkScore: null,
  innovationScore: null,
  logWeeklyReportScore: null,
  logWeeklyReportScoreAuto: null,
  totalScore: null,
  evaluationComment: ''
})

const formRules = {
  workAttitudeScore: [
    { required: true, message: '请输入工作态度评分', trigger: 'blur' }
  ],
  knowledgeApplicationScore: [
    { required: true, message: '请输入专业知识应用评分', trigger: 'blur' }
  ],
  professionalSkillScore: [
    { required: true, message: '请输入专业技能评分', trigger: 'blur' }
  ],
  teamworkScore: [
    { required: true, message: '请输入团队协作评分', trigger: 'blur' }
  ],
  innovationScore: [
    { required: true, message: '请输入创新意识评分', trigger: 'blur' }
  ],
  logWeeklyReportScore: [
    { required: true, message: '请输入日志周报质量评分', trigger: 'blur' }
  ]
}

// 使用自动计算值
const useAutoScore = () => {
  if (formData.logWeeklyReportScoreAuto !== null && formData.logWeeklyReportScoreAuto !== undefined) {
    formData.logWeeklyReportScore = formData.logWeeklyReportScoreAuto
    calculateTotalScore()
  }
}

// 计算总分
const calculateTotalScore = () => {
  const scores = [
    formData.workAttitudeScore,
    formData.knowledgeApplicationScore,
    formData.professionalSkillScore,
    formData.teamworkScore,
    formData.innovationScore,
    formData.logWeeklyReportScore
  ].filter(score => score !== null && score !== undefined)
  
  if (scores.length === 6) {
    const sum = scores.reduce((a, b) => a + b, 0)
    formData.totalScore = (sum / 6).toFixed(2)
  } else {
    formData.totalScore = null
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
      enterpriseId: formData.enterpriseId,
      workAttitudeScore: formData.workAttitudeScore,
      knowledgeApplicationScore: formData.knowledgeApplicationScore,
      professionalSkillScore: formData.professionalSkillScore,
      teamworkScore: formData.teamworkScore,
      innovationScore: formData.innovationScore,
      logWeeklyReportScore: formData.logWeeklyReportScore,
      logWeeklyReportScoreAuto: formData.logWeeklyReportScoreAuto,
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
          enterpriseId: formData.enterpriseId,
          workAttitudeScore: formData.workAttitudeScore,
          knowledgeApplicationScore: formData.knowledgeApplicationScore,
          professionalSkillScore: formData.professionalSkillScore,
          teamworkScore: formData.teamworkScore,
          innovationScore: formData.innovationScore,
          logWeeklyReportScore: formData.logWeeklyReportScore,
          logWeeklyReportScoreAuto: formData.logWeeklyReportScoreAuto,
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

// 监听评价变化，填充表单
watch(() => props.evaluation, (newVal) => {
  if (newVal) {
    formData.evaluationId = newVal.evaluationId
    formData.applyId = newVal.applyId || formData.applyId
    // 优先使用评价中的企业ID，如果评价中没有，保持现有的企业ID
    if (newVal.enterpriseId) {
      formData.enterpriseId = newVal.enterpriseId
    } else if (!formData.enterpriseId && props.student?.enterpriseId) {
      formData.enterpriseId = props.student.enterpriseId
    }
    formData.workAttitudeScore = newVal.workAttitudeScore
    formData.knowledgeApplicationScore = newVal.knowledgeApplicationScore
    formData.professionalSkillScore = newVal.professionalSkillScore
    formData.teamworkScore = newVal.teamworkScore
    formData.innovationScore = newVal.innovationScore
    formData.logWeeklyReportScore = newVal.logWeeklyReportScore
    formData.logWeeklyReportScoreAuto = newVal.logWeeklyReportScoreAuto
    formData.totalScore = newVal.totalScore
    formData.evaluationComment = newVal.evaluationComment || ''
  } else {
    // 如果没有评价，重置表单（但保留申请ID和企业ID）
    formData.evaluationId = null
    formData.workAttitudeScore = null
    formData.knowledgeApplicationScore = null
    formData.professionalSkillScore = null
    formData.teamworkScore = null
    formData.innovationScore = null
    formData.logWeeklyReportScore = null
    formData.logWeeklyReportScoreAuto = null
    formData.totalScore = null
    formData.evaluationComment = ''
  }
}, { immediate: true, deep: true })

// 加载参考信息
const loadReferenceInfo = async () => {
  if (!props.student || !props.student.applyId) return
  
  try {
    const applyId = props.student.applyId
    
    // 并行加载所有参考信息
    const [logsRes, reportsRes, achievementsRes, attendanceData] = await Promise.all([
      // 加载日志列表
      logApi.getLogPage({ 
        current: 1, 
        size: 1000, 
        applyId: applyId 
      }).catch(() => ({ code: 200, data: { records: [] } })),
      
      // 加载周报列表
      weeklyReportApi.getReportPage({ 
        current: 1, 
        size: 1000, 
        applyId: applyId 
      }).catch(() => ({ code: 200, data: { records: [] } })),
      
      // 加载成果列表
      achievementApi.getAchievementPage({ 
        current: 1, 
        size: 1000, 
        applyId: applyId 
      }).catch(() => ({ code: 200, data: { records: [] } })),
      
      // 加载考勤统计和考勤记录
      Promise.all([
        attendanceApi.getAttendanceStatistics({ 
          applyId: applyId 
        }).catch(() => ({ code: 200, data: null })),
        attendanceApi.getAttendancePage({ 
          current: 1, 
          size: 1000, 
          applyId: applyId 
        }).catch(() => ({ code: 200, data: { records: [] } }))
      ])
    ])
    
    // 更新日志数据
    if (logsRes.code === 200 && logsRes.data && logsRes.data.records) {
      referenceInfo.logs = logsRes.data.records
    }
    
    // 更新周报数据
    if (reportsRes.code === 200 && reportsRes.data && reportsRes.data.records) {
      referenceInfo.reports = reportsRes.data.records
    }
    
    // 更新成果数据
    if (achievementsRes.code === 200 && achievementsRes.data && achievementsRes.data.records) {
      referenceInfo.achievements = achievementsRes.data.records
    }
    
    // 更新考勤数据
    if (attendanceData && attendanceData.length === 2) {
      const [attendanceStatsRes, attendanceRecordsRes] = attendanceData
      if (attendanceStatsRes.code === 200 && attendanceStatsRes.data) {
        referenceInfo.attendance = attendanceStatsRes.data
      }
      if (attendanceRecordsRes.code === 200 && attendanceRecordsRes.data && attendanceRecordsRes.data.records) {
        referenceInfo.attendanceRecords = attendanceRecordsRes.data.records
      }
    }
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

// 监听学生变化，初始化表单并加载自动计算值和参考信息
watch(() => props.student, async (newVal) => {
  if (newVal) {
    formData.applyId = newVal.applyId || formData.applyId
    // 优先使用学生对象中的企业ID，如果学生对象中没有，保持现有的企业ID
    if (newVal.enterpriseId) {
      formData.enterpriseId = newVal.enterpriseId
    } else if (!formData.enterpriseId && newVal.applyId) {
      // 如果学生对象中没有企业ID，尝试从申请中获取
      try {
        const { applyApi } = await import('@/api/internship/apply')
        const applyRes = await applyApi.getApplyById(newVal.applyId)
        if (applyRes.code === 200 && applyRes.data && applyRes.data.enterpriseId) {
          formData.enterpriseId = applyRes.data.enterpriseId
        }
      } catch (error) {
        console.error('获取申请详情失败:', error)
      }
    }
    
    // 加载参考信息
    loadReferenceInfo()
    
    // 如果有申请ID，尝试加载评价以获取自动计算值
    if (newVal.applyId && !formData.evaluationId) {
      try {
        const { enterpriseEvaluationApi } = await import('@/api/evaluation/enterprise')
        const res = await enterpriseEvaluationApi.getEvaluationByApplyId(newVal.applyId)
        if (res.code === 200 && res.data) {
          formData.logWeeklyReportScoreAuto = res.data.logWeeklyReportScoreAuto
          // 如果评价中有企业ID，也更新表单
          if (res.data.enterpriseId && !formData.enterpriseId) {
            formData.enterpriseId = res.data.enterpriseId
          }
          // 如果还没有填写日志周报质量评分，使用自动计算值
          if (formData.logWeeklyReportScore === null && res.data.logWeeklyReportScoreAuto !== null) {
            formData.logWeeklyReportScore = res.data.logWeeklyReportScoreAuto
            calculateTotalScore()
          }
        } else if (res.code === 200 && !res.data) {
          // 如果没有评价记录，也需要获取自动计算值
          // 可以通过调用后端接口获取，或者在前端页面加载时获取
        }
      } catch (error) {
        console.error('加载自动计算值失败:', error)
      }
    }
  }
}, { immediate: true })

onMounted(() => {
  if (props.student) {
    loadReferenceInfo()
  }
})
</script>

<style scoped>
.enterprise-evaluation-form {
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

