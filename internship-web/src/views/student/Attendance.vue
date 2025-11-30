<template>
  <PageLayout title="我的考勤">
    <!-- 操作按钮区域 -->
    <div class="action-bar" style="margin-bottom: 20px">
      <el-card shadow="hover">
        <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px">
          <div style="display: flex; align-items: center; gap: 10px; flex-wrap: wrap">
            <!-- 时间段选择（仅合作企业实习显示） -->
            <div v-if="currentApplyType === 1 && attendanceGroup && attendanceGroup.timeSlots && attendanceGroup.timeSlots.length > 0" style="display: flex; align-items: center; gap: 8px; margin-right: 8px">
              <span style="font-size: 14px; color: #606266; white-space: nowrap">时间段：</span>
              <el-select
                v-model="selectedTimeSlotId"
                placeholder="请选择时间段"
                style="width: 200px"
                size="default"
              >
                <el-option
                  v-for="slot in attendanceGroup.timeSlots"
                  :key="slot.slotId"
                  :label="`${slot.slotName}（${slot.startTime} - ${slot.endTime}）`"
                  :value="slot.slotId"
                />
              </el-select>
            </div>
            <div v-else-if="currentApplyType === 1 && attendanceGroup && attendanceGroup.timeSlots && attendanceGroup.timeSlots.length === 1" style="display: flex; align-items: center; gap: 8px; margin-right: 8px">
              <span style="font-size: 14px; color: #606266; white-space: nowrap">时间段：</span>
              <span style="font-size: 14px; color: #303133; padding: 0 12px; background: #f5f7fa; border-radius: 4px; white-space: nowrap">
                {{ attendanceGroup.timeSlots[0].slotName }}（{{ attendanceGroup.timeSlots[0].startTime }} - {{ attendanceGroup.timeSlots[0].endTime }}）
              </span>
            </div>
            <el-button 
              type="success" 
              :icon="Check" 
              :disabled="!canCheckIn || (currentApplyType === 1 && attendanceGroup && attendanceGroup.timeSlots && attendanceGroup.timeSlots.length > 1 && !selectedTimeSlotId)"
              @click="handleCheckIn"
            >
              上班打卡
            </el-button>
            <el-button 
              type="primary" 
              :icon="Close" 
              :disabled="!canCheckOut || (currentApplyType === 1 && attendanceGroup && attendanceGroup.timeSlots && attendanceGroup.timeSlots.length > 1 && !selectedTimeSlotId)"
              @click="handleCheckOut"
            >
              下班打卡
            </el-button>
            <el-button 
              type="warning" 
              :icon="Calendar"
              @click="showLeaveDialog = true"
            >
              申请请假
            </el-button>
            <el-button 
              type="info" 
              :icon="Sunny"
              @click="showRestDialog = true"
            >
              选择休息
            </el-button>
          </div>
          <div v-if="todayAttendance" style="font-size: 14px; color: #606266">
            <span v-if="todayAttendance.checkInTime">
              已上班打卡：{{ formatDateTime(todayAttendance.checkInTime) }}
            </span>
            <span v-if="todayAttendance.checkOutTime" style="margin-left: 20px">
              已下班打卡：{{ formatDateTime(todayAttendance.checkOutTime) }}
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 日历和统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <!-- 左侧：日历组件 -->
      <el-col :span="16">
        <el-card shadow="hover">
      <template #header>
        <div style="display: flex; align-items: center; justify-content: space-between">
          <span style="font-size: 16px; font-weight: 600">考勤日历</span>
          <div style="display: flex; gap: 12px; align-items: center; flex-wrap: wrap">
            <div class="legend-item">
              <span class="legend-dot normal"></span>
              <span>正常</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot late"></span>
              <span>迟到</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot early"></span>
              <span>早退</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot leave"></span>
              <span>请假</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot rest"></span>
              <span>休息</span>
            </div>
            <div class="legend-item">
              <span class="legend-dot absent"></span>
              <span>缺勤</span>
            </div>
          </div>
        </div>
      </template>
      <el-calendar v-loading="loading" v-model="calendarDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell" @click="handleDateClick(data.day)">
            <div class="calendar-date">{{ data.day.split('-').slice(2).join('-') }}</div>
            <div v-if="getAttendanceByDate(data.day)" class="calendar-content">
              <div v-if="getAttendanceByDate(data.day).attendanceType === 1" class="attendance-info normal">
                <div v-if="getAttendanceByDate(data.day).checkInTime" class="time-info">
                  <span class="time-label">上班:</span>
                  <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkInTime) }}</span>
                </div>
                <div v-if="getAttendanceByDate(data.day).checkOutTime" class="time-info">
                  <span class="time-label">下班:</span>
                  <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkOutTime) }}</span>
                </div>
              </div>
              <div v-else-if="getAttendanceByDate(data.day).attendanceType === 2" class="attendance-info late">
                <div class="attendance-content-row">
                  <div class="time-info-wrapper">
                    <div v-if="getAttendanceByDate(data.day).checkInTime" class="time-info">
                      <span class="time-label">上班:</span>
                      <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkInTime) }}</span>
                    </div>
                    <div v-if="getAttendanceByDate(data.day).checkOutTime" class="time-info">
                      <span class="time-label">下班:</span>
                      <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkOutTime) }}</span>
                    </div>
                  </div>
                  <div class="type-tag">迟到</div>
                </div>
              </div>
              <div v-else-if="getAttendanceByDate(data.day).attendanceType === 3" class="attendance-info early">
                <div class="attendance-content-row">
                  <div class="time-info-wrapper">
                    <div v-if="getAttendanceByDate(data.day).checkInTime" class="time-info">
                      <span class="time-label">上班:</span>
                      <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkInTime) }}</span>
                    </div>
                    <div v-if="getAttendanceByDate(data.day).checkOutTime" class="time-info">
                      <span class="time-label">下班:</span>
                      <span class="time-value">{{ formatTime(getAttendanceByDate(data.day).checkOutTime) }}</span>
                    </div>
                  </div>
                  <div class="type-tag">早退</div>
                </div>
              </div>
              <div v-else-if="getAttendanceByDate(data.day).attendanceType === 4" class="attendance-info leave">
                <div class="type-tag">请假</div>
              </div>
              <div v-else-if="getAttendanceByDate(data.day).attendanceType === 6" class="attendance-info rest">
                <div class="type-tag">休息</div>
              </div>
              <div v-else-if="getAttendanceByDate(data.day).attendanceType === 5" class="attendance-info absent">
                <div class="type-tag">缺勤</div>
              </div>
            </div>
          </div>
        </template>
          </el-calendar>
        </el-card>
      </el-col>
      <!-- 右侧：统计卡片 -->
      <el-col :span="8">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">总出勤天数</div>
            <div class="stat-value">{{ statistics.totalDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">正常出勤</div>
            <div class="stat-value" style="color: #67c23a">{{ statistics.normalDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">请假天数</div>
            <div class="stat-value" style="color: #909399">{{ statistics.leaveDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">缺勤天数</div>
            <div class="stat-value" style="color: #f56c6c">{{ statistics.absentDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">休息天数</div>
            <div class="stat-value" style="color: #909399">{{ statistics.restDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">出勤率</div>
            <div class="stat-value" style="color: #409eff">
              {{ statistics.attendanceRate ? statistics.attendanceRate.toFixed(2) + '%' : '0%' }}
            </div>
          </div>
        </el-card>
      </el-col>
          <el-col :span="24">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总工作时长</div>
            <div class="stat-value" style="color: #67c23a">
              {{ statistics.totalWorkHours ? statistics.totalWorkHours.toFixed(2) + '小时' : '0小时' }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
      </el-col>
    </el-row>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="考勤日期">
          {{ formatDate(detailData.attendanceDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="考勤类型">
          <el-tag :type="getAttendanceTypeTagType(detailData.attendanceType)" size="small">
            {{ getAttendanceTypeText(detailData.attendanceType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上班打卡时间">
          {{ detailData.checkInTime ? formatDateTime(detailData.checkInTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="下班打卡时间">
          {{ detailData.checkOutTime ? formatDateTime(detailData.checkOutTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="工作时长">
          {{ detailData.workHours ? `${detailData.workHours}小时` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="确认状态">
          <el-tag :type="detailData.confirmStatus === 1 ? 'success' : 'warning'" size="small">
            {{ detailData.confirmStatus === 1 ? '已确认' : '待确认' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.leaveType" label="请假类型" :span="2">
          {{ detailData.leaveType }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.leaveReason" label="请假原因" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.leaveReason }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.confirmComment" label="确认意见" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.confirmComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.updateTime" label="更新时间">
          {{ formatDateTime(detailData.updateTime) }}
        </el-descriptions-item>
        </el-descriptions>
    </el-dialog>

    <!-- 请假申请对话框 -->
    <el-dialog
      v-model="showLeaveDialog"
      title="申请请假"
      width="600px"
    >
      <el-form
        ref="leaveFormRef"
        :model="leaveForm"
        :rules="leaveFormRules"
        label-width="100px"
      >
        <el-form-item label="请假日期" prop="attendanceDate">
          <el-date-picker
            v-model="leaveForm.attendanceDate"
            type="date"
            placeholder="请选择请假日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="请假类型" prop="leaveType">
          <el-select
            v-model="leaveForm.leaveType"
            placeholder="请选择请假类型"
            style="width: 100%"
          >
            <el-option label="事假" value="事假" />
            <el-option label="病假" value="病假" />
            <el-option label="调休" value="调休" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="请假原因" prop="leaveReason">
          <el-input
            v-model="leaveForm.leaveReason"
            type="textarea"
            :rows="4"
            placeholder="请输入请假原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLeaveDialog = false">取消</el-button>
        <el-button type="primary" @click="handleApplyLeave">提交</el-button>
      </template>
    </el-dialog>

    <!-- 选择休息对话框 -->
    <el-dialog
      v-model="showRestDialog"
      title="选择休息"
      width="500px"
    >
      <el-form label-width="100px">
        <el-form-item label="休息日期" required>
          <el-date-picker
            v-model="restDate"
            type="date"
            placeholder="请选择休息日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRestDialog = false; restDate = null">取消</el-button>
        <el-button type="primary" @click="handleSelectRest">确定</el-button>
      </template>
    </el-dialog>

  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close, Calendar, Sunny } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { attendanceGroupApi } from '@/api/internship/attendanceGroup'
import { applyApi } from '@/api/internship/apply'
import { useAuthStore } from '@/store/modules/auth'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const detailDialogVisible = ref(false)
const showLeaveDialog = ref(false)
const showRestDialog = ref(false)
const todayAttendance = ref(null)
const restDate = ref(null)
const attendanceGroup = ref(null)
const selectedTimeSlotId = ref(null)
const currentApplyId = ref(null)
const currentApplyType = ref(null) // 当前实习申请类型：1-合作企业，2-自主实习


const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const calendarDate = ref(new Date())
const attendanceMap = ref(new Map()) // 用于存储按日期组织的考勤数据
const statistics = ref({
  totalDays: 0,
  normalDays: 0,
  leaveDays: 0,
  absentDays: 0,
  restDays: 0,
  attendanceRate: 0,
  totalWorkHours: 0
})

const leaveForm = reactive({
  attendanceDate: null,
  leaveType: '',
  leaveReason: ''
})

const leaveFormRules = {
  attendanceDate: [{ required: true, message: '请选择请假日期', trigger: 'change' }],
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  leaveReason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

const leaveFormRef = ref(null)

// 计算是否可以上班打卡
const canCheckIn = computed(() => {
  if (!todayAttendance.value) return true
  return !todayAttendance.value.checkInTime
})

// 计算是否可以下班打卡
const canCheckOut = computed(() => {
  if (!todayAttendance.value) return false
  return todayAttendance.value.checkInTime && !todayAttendance.value.checkOutTime
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
      // 加载所有考勤数据用于日历显示（不限制分页）
    const res = await attendanceApi.getAttendancePage({
        current: 1,
        size: 1000 // 加载足够多的数据
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 构建考勤数据映射（按日期）
      attendanceMap.value.clear()
      tableData.value.forEach(item => {
        if (item.attendanceDate) {
          attendanceMap.value.set(item.attendanceDate, item)
        }
      })
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 根据日期获取考勤信息
const getAttendanceByDate = (dateStr) => {
  // 处理日期格式，确保格式一致（YYYY-MM-DD）
  if (!dateStr) return null
  // 如果日期字符串包含时间部分，只取日期部分
  const dateOnly = dateStr.split(' ')[0]
  return attendanceMap.value.get(dateOnly) || null
}

// 格式化时间（只显示时分）
const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 点击日期
const handleDateClick = (dateStr) => {
  const attendance = getAttendanceByDate(dateStr)
  if (attendance) {
    handleView(attendance)
  }
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await attendanceApi.getAttendanceStatistics({})
    if (res.code === 200) {
      statistics.value = res.data || {
        totalDays: 0,
        normalDays: 0,
        leaveDays: 0,
        absentDays: 0,
        restDays: 0,
        attendanceRate: 0,
        totalWorkHours: 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 加载当前实习申请信息
const loadCurrentApply = async () => {
  try {
    const res = await applyApi.getCurrentInternship()
    if (res.code === 200 && res.data) {
      currentApplyId.value = res.data.applyId
      currentApplyType.value = res.data.applyType
      // 如果是合作企业实习，加载考勤组
      if (res.data.applyType === 1) {
        await loadAttendanceGroup()
      }
    }
  } catch (error) {
    console.error('加载当前实习申请失败:', error)
  }
}

// 加载今天的考勤记录
const loadTodayAttendance = async () => {
  try {
    const res = await attendanceApi.getTodayAttendance()
    if (res.code === 200) {
      todayAttendance.value = res.data || null
      // 如果获取到今天的考勤记录，更新申请ID
      if (res.data && res.data.applyId) {
        currentApplyId.value = res.data.applyId
        // 如果还没有获取到申请类型，尝试获取申请详情
        if (!currentApplyType.value) {
          try {
            const applyRes = await applyApi.getApplyById(res.data.applyId)
            if (applyRes.code === 200 && applyRes.data) {
              currentApplyType.value = applyRes.data.applyType
              if (applyRes.data.applyType === 1) {
                await loadAttendanceGroup()
              }
            }
          } catch (error) {
            console.error('获取申请详情失败:', error)
          }
        }
      }
    }
  } catch (error) {
    console.error('加载今天考勤记录失败:', error)
    todayAttendance.value = null
  }
}

// 加载考勤组信息（仅合作企业实习需要）
const loadAttendanceGroup = async () => {
  // 只有合作企业实习才需要考勤组
  if (currentApplyType.value !== 1) {
    attendanceGroup.value = null
    return
  }
  
  try {
    // 获取当前学生的实习申请ID
    if (!currentApplyId.value) {
      // 优先从用户信息中获取
      const authStore = useAuthStore()
      if (authStore.userInfo?.currentApplyId) {
        currentApplyId.value = authStore.userInfo.currentApplyId
      } else if (todayAttendance.value && todayAttendance.value.applyId) {
        // 从今天的考勤记录中获取
        currentApplyId.value = todayAttendance.value.applyId
      } else {
        // 从申请列表获取已录用的申请
        const applyRes = await applyApi.getApplyPage({
          current: 1,
          size: 1,
          status: 3 // 已录用
        })
        if (applyRes.code === 200 && applyRes.data.records && applyRes.data.records.length > 0) {
          currentApplyId.value = applyRes.data.records[0].applyId
        }
      }
    }
    
    if (currentApplyId.value) {
      const groupRes = await attendanceGroupApi.getGroupByApplyId(currentApplyId.value)
      if (groupRes.code === 200 && groupRes.data) {
        attendanceGroup.value = groupRes.data
        // 如果只有一个时间段，自动选择
        if (groupRes.data.timeSlots && groupRes.data.timeSlots.length === 1) {
          selectedTimeSlotId.value = groupRes.data.timeSlots[0].slotId
        }
      } else {
        attendanceGroup.value = null
      }
    } else {
      attendanceGroup.value = null
    }
  } catch (error) {
    console.error('加载考勤组失败:', error)
    attendanceGroup.value = null
  }
}

// 上班打卡
const handleCheckIn = async () => {
  // 如果是合作企业实习，需要考勤组
  if (currentApplyType.value === 1) {
    // 确保考勤组已加载
    if (!attendanceGroup.value) {
      await loadAttendanceGroup()
    }
    
    // 如果没有考勤组，提示用户
    if (!attendanceGroup.value) {
      ElMessage.warning('您尚未分配到考勤组，请联系企业导师')
      return
    }
    
    // 如果只有一个时间段，自动选择
    if (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length === 1) {
      selectedTimeSlotId.value = attendanceGroup.value.timeSlots[0].slotId
    }
    
    // 如果有多个时间段但未选择，提示用户
    if (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length > 1 && !selectedTimeSlotId.value) {
      ElMessage.warning('请先选择时间段')
      return
    }
  }
  
  try {
    // 合作企业实习需要传递时间段ID，自主实习传null
    const timeSlotId = currentApplyType.value === 1 && attendanceGroup.value
      ? (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length === 1
        ? attendanceGroup.value.timeSlots[0].slotId
        : selectedTimeSlotId.value)
      : null
    
    const res = await attendanceApi.studentCheckIn(null, timeSlotId)
    if (res.code === 200) {
      ElMessage.success('上班打卡成功')
      await loadTodayAttendance()
      await loadData()
      await loadStatistics()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '上班打卡失败')
  }
}

// 下班打卡
const handleCheckOut = async () => {
  // 如果是合作企业实习，需要考勤组
  if (currentApplyType.value === 1) {
    // 确保考勤组已加载
    if (!attendanceGroup.value) {
      await loadAttendanceGroup()
    }
    
    // 如果没有考勤组，提示用户
    if (!attendanceGroup.value) {
      ElMessage.warning('您尚未分配到考勤组，请联系企业导师')
      return
    }
    
    // 如果只有一个时间段，自动选择
    if (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length === 1) {
      selectedTimeSlotId.value = attendanceGroup.value.timeSlots[0].slotId
    }
    
    // 如果有多个时间段但未选择，提示用户
    if (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length > 1 && !selectedTimeSlotId.value) {
      ElMessage.warning('请先选择时间段')
      return
    }
  }
  
  try {
    // 合作企业实习需要传递时间段ID，自主实习传null
    const timeSlotId = currentApplyType.value === 1 && attendanceGroup.value
      ? (attendanceGroup.value.timeSlots && attendanceGroup.value.timeSlots.length === 1
        ? attendanceGroup.value.timeSlots[0].slotId
        : selectedTimeSlotId.value)
      : null
    
    const res = await attendanceApi.studentCheckOut(null, timeSlotId)
    if (res.code === 200) {
      ElMessage.success('下班打卡成功')
      await loadTodayAttendance()
      await loadData()
      await loadStatistics()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '下班打卡失败')
  }
}

// 申请请假
const handleApplyLeave = async () => {
  if (!leaveFormRef.value) return
  await leaveFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await attendanceApi.studentApplyLeave(
          leaveForm.attendanceDate,
          leaveForm.leaveType,
          leaveForm.leaveReason
        )
        if (res.code === 200) {
          ElMessage.success('请假申请提交成功，等待企业导师确认')
          showLeaveDialog.value = false
          leaveForm.attendanceDate = null
          leaveForm.leaveType = ''
          leaveForm.leaveReason = ''
          await loadData()
          await loadStatistics()
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '请假申请失败')
      }
    }
  })
}

// 选择休息
const handleSelectRest = async () => {
  if (!restDate.value) {
    ElMessage.warning('请选择休息日期')
    return
  }
  
  try {
    const res = await attendanceApi.studentSelectRest(restDate.value)
    if (res.code === 200) {
      ElMessage.success('休息申请提交成功，等待企业导师确认')
      showRestDialog.value = false
      restDate.value = null
      await loadData()
      await loadStatistics()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '休息申请失败')
  }
}

// 获取今天的日期字符串（YYYY-MM-DD格式）
const getTodayDateString = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 禁用非今天的日期
const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const timeDate = new Date(time)
  timeDate.setHours(0, 0, 0, 0)
  return timeDate.getTime() !== today.getTime()
}


// 查看详情
const handleView = async (row) => {
  try {
    const res = await attendanceApi.getAttendanceById(row.attendanceId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}


// 获取考勤类型文本
const getAttendanceTypeText = (type) => {
  const typeMap = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '请假',
    5: '缺勤',
    6: '休息'
  }
  return typeMap[type] || '-'
}

// 获取考勤类型标签类型
const getAttendanceTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'warning',
    3: 'warning',
    4: 'info',
    5: 'danger',
    6: 'info'
  }
  return typeMap[type] || 'info'
}

// 初始化
onMounted(async () => {
  // 先加载当前实习申请信息
  await loadCurrentApply()
  // 并行加载其他数据，提高效率
  await Promise.all([
    loadTodayAttendance(), // 加载今天的考勤记录
    loadData(), // 加载考勤列表
    loadStatistics() // 加载统计数据
  ])
  // 如果没有获取到申请类型，再次尝试加载
  if (!currentApplyType.value && currentApplyId.value) {
    try {
      const applyRes = await applyApi.getApplyById(currentApplyId.value)
      if (applyRes.code === 200 && applyRes.data) {
        currentApplyType.value = applyRes.data.applyType
        if (applyRes.data.applyType === 1) {
          await loadAttendanceGroup()
        }
      }
    } catch (error) {
      console.error('获取申请详情失败:', error)
    }
  }
})
</script>

<style scoped>

.stat-item {
  text-align: center;
  padding: 6px 0;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.calendar-cell {
  height: 100%;
  padding: 2px;
  cursor: pointer;
  transition: background-color 0.2s;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.calendar-date {
  font-size: 11px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
  line-height: 1.2;
  flex-shrink: 0;
}

.calendar-content {
  font-size: 10px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
  min-height: 0;
}

.attendance-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.attendance-content-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 4px;
  width: 100%;
}

.time-info-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.attendance-info.normal {
  color: #67c23a;
}

.attendance-info.leave {
  color: #909399;
}

.attendance-info.rest {
  color: #409eff;
}

.attendance-info.absent {
  color: #f56c6c;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 2px;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time-label {
  font-size: 9px;
  opacity: 0.8;
  flex-shrink: 0;
}

.time-value {
  font-size: 10px;
  font-weight: 500;
  flex-shrink: 0;
}

.type-tag {
  font-size: 9px;
  padding: 2px 4px;
  border-radius: 2px;
  display: inline-block;
  width: fit-content;
  line-height: 1.2;
  white-space: nowrap;
  flex-shrink: 0;
  align-self: flex-start;
}

.attendance-info.normal .type-tag {
  background-color: #f0f9ff;
  color: #67c23a;
}

.attendance-info.late .type-tag {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.attendance-info.early .type-tag {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.attendance-info.leave .type-tag {
  background-color: #f5f7fa;
  color: #909399;
}

.attendance-info.rest .type-tag {
  background-color: #ecf5ff;
  color: #409eff;
}

.attendance-info.absent .type-tag {
  background-color: #fef0f0;
  color: #f56c6c;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.legend-dot.normal {
  background-color: #67c23a;
}

.legend-dot.late {
  background-color: #e6a23c;
}

.legend-dot.early {
  background-color: #e6a23c;
}

.legend-dot.leave {
  background-color: #909399;
}

.legend-dot.rest {
  background-color: #409eff;
}

.legend-dot.absent {
  background-color: #f56c6c;
}

:deep(.el-calendar-day) {
  height: 60px;
  padding: 0;
  overflow: hidden;
}

:deep(.el-calendar__header) {
  padding: 8px 12px;
}

:deep(.el-calendar__body) {
  padding: 8px 12px;
}

:deep(.el-calendar-table) {
  font-size: 12px;
}

:deep(.el-calendar-table thead th) {
  padding: 6px 0;
  font-size: 12px;
}

:deep(.el-calendar-table td) {
  padding: 2px;
}
</style>

