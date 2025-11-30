<template>
  <div class="school-evaluation-form">
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

    <!-- 主标签页：评分填写和参考信息 -->
    <el-tabs v-model="activeMainTab" type="border-card" style="margin-top: 15px;">
      <!-- 标签页1: 评分填写 -->
      <el-tab-pane label="评分填写" name="evaluation">
        <div style="padding: 20px;">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="150px"
    >
      <el-form-item label="日志周报质量" prop="logWeeklyReportScore">
        <el-input-number
          v-model="formData.logWeeklyReportScore"
          :min="0"
          :max="100"
          :precision="2"
          :disabled="isSubmitted"
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
          :disabled="isSubmitted"
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
          :disabled="isSubmitted"
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
          :disabled="isSubmitted"
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
          :disabled="isSubmitted"
          placeholder="请输入评价意见（可选）"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" :disabled="isSubmitted" @click="handleSave">保存草稿</el-button>
        <el-button type="success" :loading="submitting" :disabled="isSubmitted" @click="handleSubmit">提交评价</el-button>
        <el-alert
          v-if="isSubmitted"
          type="success"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        >
          <template #title>
            <span>评价已提交，无法修改</span>
          </template>
        </el-alert>
      </el-form-item>
    </el-form>
  </div>
      </el-tab-pane>
      
      <!-- 标签页2: 参考信息 -->
      <el-tab-pane label="参考信息" name="reference">
        <div style="padding: 20px;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
            <h3 style="margin: 0;">参考信息</h3>
            <el-button type="primary" size="small" @click="loadSuggestedScore">一键填入建议分数</el-button>
          </div>
          
          <el-tabs v-model="activeReferenceTab" type="border-card">
            <!-- 日志情况 -->
            <el-tab-pane label="日志情况" name="logs">
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
                  <el-table-column prop="weekNumber" label="周次" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.weekNumber ? `第${row.weekNumber}周` : '-' }}
                    </template>
                  </el-table-column>
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
                  <el-table-column prop="achievementName" label="成果名称" min-width="200" />
                  <el-table-column prop="achievementType" label="成果类型" width="120" />
                  <el-table-column prop="submitDate" label="提交时间" width="180">
                    <template #default="{ row }">
                      {{ row.submitDate ? formatDate(row.submitDate) : (row.createTime ? formatDateTime(row.createTime) : '-') }}
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
                <!-- 考勤统计卡片 -->
                <el-row :gutter="12" style="margin-bottom: 15px;">
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">总出勤天数</div>
                        <div class="stat-value">{{ referenceInfo.attendance.totalDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">正常出勤</div>
                        <div class="stat-value" style="color: #67c23a">{{ referenceInfo.attendance.normalDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">迟到</div>
                        <div class="stat-value" style="color: #e6a23c">{{ referenceInfo.attendance.lateDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">早退</div>
                        <div class="stat-value" style="color: #e6a23c">{{ referenceInfo.attendance.earlyLeaveDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">请假</div>
                        <div class="stat-value" style="color: #909399">{{ referenceInfo.attendance.leaveDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">缺勤</div>
                        <div class="stat-value" style="color: #f56c6c">{{ referenceInfo.attendance.absentDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">休息</div>
                        <div class="stat-value" style="color: #909399">{{ referenceInfo.attendance.restDays || 0 }}</div>
                      </div>
                    </el-card>
                  </el-col>
                  <el-col :span="6">
                    <el-card shadow="hover">
                      <div class="stat-item">
                        <div class="stat-label">出勤率</div>
                        <div class="stat-value" style="color: #409eff">
                          {{ referenceInfo.attendance.attendanceRate ? referenceInfo.attendance.attendanceRate.toFixed(2) + '%' : '0%' }}
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>
                
                <!-- 考勤记录列表 -->
                <el-table v-if="referenceInfo.attendanceRecords && referenceInfo.attendanceRecords.length > 0" 
                          :data="referenceInfo.attendanceRecords" border style="margin-top: 10px" max-height="300">
                  <el-table-column prop="attendanceDate" label="日期" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.attendanceDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="attendanceType" label="状态" width="100" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="row.attendanceType === 1" type="success" size="small">正常</el-tag>
                      <el-tag v-else-if="row.attendanceType === 2" type="warning" size="small">迟到</el-tag>
                      <el-tag v-else-if="row.attendanceType === 3" type="warning" size="small">早退</el-tag>
                      <el-tag v-else-if="row.attendanceType === 4" type="info" size="small">请假</el-tag>
                      <el-tag v-else-if="row.attendanceType === 5" type="danger" size="small">缺勤</el-tag>
                      <el-tag v-else-if="row.attendanceType === 6" type="info" size="small">休息</el-tag>
                      <el-tag v-else size="small">未知</el-tag>
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
                  <el-table-column prop="workHours" label="工作时长" width="100" align="center">
                    <template #default="{ row }">
                      {{ row.workHours ? row.workHours + '小时' : '-' }}
                    </template>
                  </el-table-column>
                </el-table>
                <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无考勤记录</div>
              </div>
              <div v-else style="color: #909399; padding: 20px; text-align: center;">暂无考勤数据</div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>

  <!-- 日志详情对话框 -->
  <el-dialog
    v-model="logDialogVisible"
    title="日志列表"
    width="80%"
    :close-on-click-modal="false"
  >
    <el-table :data="referenceInfo.logs" border max-height="500">
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
      <el-table-column prop="reviewComment" label="批阅意见" min-width="200" show-overflow-tooltip />
    </el-table>
    <template #footer>
      <el-button @click="logDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 周报详情对话框 -->
  <el-dialog
    v-model="reportDialogVisible"
    title="周报列表"
    width="80%"
    :close-on-click-modal="false"
  >
    <el-table :data="referenceInfo.reports" border max-height="500">
      <el-table-column prop="reportTitle" label="周报标题" min-width="200" />
      <el-table-column prop="weekNumber" label="周次" width="100" align="center">
        <template #default="{ row }">
          {{ row.weekNumber ? `第${row.weekNumber}周` : '-' }}
        </template>
      </el-table-column>
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
      <el-table-column prop="reviewComment" label="批阅意见" min-width="200" show-overflow-tooltip />
    </el-table>
    <template #footer>
      <el-button @click="reportDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 成果详情对话框 -->
  <el-dialog
    v-model="achievementDialogVisible"
    title="成果列表"
    width="80%"
    :close-on-click-modal="false"
  >
    <el-table :data="referenceInfo.achievements" border max-height="500">
      <el-table-column prop="achievementName" label="成果名称" min-width="200" />
      <el-table-column prop="achievementType" label="成果类型" width="120" />
      <el-table-column prop="submitDate" label="提交时间" width="180">
        <template #default="{ row }">
          {{ row.submitDate ? formatDate(row.submitDate) : (row.createTime ? formatDateTime(row.createTime) : '-') }}
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.reviewStatus === 1" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="row.reviewStatus === 2" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="warning" size="small">待审核</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewComment" label="审核意见" min-width="200" show-overflow-tooltip />
    </el-table>
    <template #footer>
      <el-button @click="achievementDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { schoolEvaluationApi } from '@/api/evaluation/school'
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
const activeMainTab = ref('evaluation') // 主标签页：评分填写/参考信息，默认显示评分填写
const activeReferenceTab = ref('logs') // 参考信息内部标签页

// 对话框显示状态
const logDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const achievementDialogVisible = ref(false)

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

// 判断评价是否已提交（evaluationStatus === 1 表示已提交）
const isSubmitted = computed(() => {
  return props.evaluation && props.evaluation.evaluationStatus === 1
})

const referenceInfo = reactive({
  logs: [],
  reports: [],
  achievements: [],
  attendance: null,
  attendanceRecords: []
})

// 加载参考信息
const loadReferenceInfo = async () => {
  if (!props.student || !props.student.applyId) return
  
  try {
    const applyId = props.student.applyId
    const studentId = props.student.studentId
    
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
  logDialogVisible.value = true
}

// 查看周报
const viewReports = () => {
  reportDialogVisible.value = true
}

// 查看成果
const viewAchievements = () => {
  achievementDialogVisible.value = true
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

.student-info-card {
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
</style>

