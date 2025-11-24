<template>
  <PageLayout title="我的考勤">
    <!-- 操作按钮区域 -->
    <div class="action-bar" style="margin-bottom: 20px">
      <el-card shadow="hover">
        <div style="display: flex; align-items: center; justify-content: space-between">
          <div style="display: flex; align-items: center; gap: 10px">
            <el-button 
              type="success" 
              :icon="Check" 
              :disabled="!canCheckIn"
              @click="handleCheckIn"
            >
              签到
            </el-button>
            <el-button 
              type="primary" 
              :icon="Close" 
              :disabled="!canCheckOut"
              @click="handleCheckOut"
            >
              签退
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
              已签到：{{ formatDateTime(todayAttendance.checkInTime) }}
            </span>
            <span v-if="todayAttendance.checkOutTime" style="margin-left: 20px">
              已签退：{{ formatDateTime(todayAttendance.checkOutTime) }}
            </span>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="date"
            placeholder="请选择日期"
            clearable
            style="width: 200px"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="考勤类型">
          <el-select
            v-model="searchForm.attendanceType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="早退" :value="3" />
            <el-option label="请假" :value="4" />
            <el-option label="缺勤" :value="5" />
            <el-option label="休息" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考勤统计卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">总出勤天数</div>
            <div class="stat-value">{{ statistics.totalDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">正常出勤</div>
            <div class="stat-value" style="color: #67c23a">{{ statistics.normalDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">请假天数</div>
            <div class="stat-value" style="color: #909399">{{ statistics.leaveDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">缺勤天数</div>
            <div class="stat-value" style="color: #f56c6c">{{ statistics.absentDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">休息天数</div>
            <div class="stat-value" style="color: #909399">{{ statistics.restDays || 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">出勤率</div>
            <div class="stat-value" style="color: #409eff">
              {{ statistics.attendanceRate ? statistics.attendanceRate.toFixed(2) + '%' : '0%' }}
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
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

    <!-- 考勤列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      table-layout="auto"
    >
      <el-table-column type="index" label="序号" min-width="60" align="center" />
      <el-table-column prop="attendanceDate" label="考勤日期" min-width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.attendanceDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="checkInTime" label="签到时间" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.checkInTime ? formatDateTime(row.checkInTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="checkOutTime" label="签退时间" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.checkOutTime ? formatDateTime(row.checkOutTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="attendanceType" label="考勤类型" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getAttendanceTypeTagType(row.attendanceType)" size="small">
            {{ getAttendanceTypeText(row.attendanceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workHours" label="工作时长" min-width="100" align="center">
        <template #default="{ row }">
          {{ row.workHours ? `${row.workHours}小时` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="confirmStatus" label="确认状态" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.confirmStatus === 1 ? 'success' : 'warning'" size="small">
            {{ row.confirmStatus === 1 ? '已确认' : '待确认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" min-width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

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
        <el-descriptions-item label="签到时间">
          {{ detailData.checkInTime ? formatDateTime(detailData.checkInTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="签退时间">
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
            :disabled-date="disabledDate"
            :default-value="new Date()"
          />
          <div style="color: #909399; font-size: 12px; margin-top: 5px">
            只能选择今天的日期
          </div>
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
import { Search, Refresh, Check, Close, Calendar, Sunny } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const detailDialogVisible = ref(false)
const showLeaveDialog = ref(false)
const showRestDialog = ref(false)
const todayAttendance = ref(null)
const restDate = ref(null)

const searchForm = reactive({
  attendanceDate: null,
  attendanceType: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
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

// 计算是否可以签到
const canCheckIn = computed(() => {
  if (!todayAttendance.value) return true
  return !todayAttendance.value.checkInTime
})

// 计算是否可以签退
const canCheckOut = computed(() => {
  if (!todayAttendance.value) return false
  return todayAttendance.value.checkInTime && !todayAttendance.value.checkOutTime
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await attendanceApi.getAttendancePage({
      current: pagination.current,
      size: pagination.size,
      attendanceDate: searchForm.attendanceDate || undefined,
      attendanceType: searchForm.attendanceType !== null ? searchForm.attendanceType : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
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

// 加载今天的考勤记录
const loadTodayAttendance = async () => {
  try {
    const res = await attendanceApi.getTodayAttendance()
    if (res.code === 200) {
      todayAttendance.value = res.data || null
    }
  } catch (error) {
    console.error('加载今天考勤记录失败:', error)
    todayAttendance.value = null
  }
}

// 签到
const handleCheckIn = async () => {
  try {
    const res = await attendanceApi.studentCheckIn()
    if (res.code === 200) {
      ElMessage.success('签到成功')
      await loadTodayAttendance()
      await loadData()
      await loadStatistics()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签到失败')
  }
}

// 签退
const handleCheckOut = async () => {
  try {
    const res = await attendanceApi.studentCheckOut()
    if (res.code === 200) {
      ElMessage.success('签退成功')
      await loadTodayAttendance()
      await loadData()
      await loadStatistics()
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '签退失败')
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.attendanceDate = null
  searchForm.attendanceType = null
  handleSearch()
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

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
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
onMounted(() => {
  loadData()
  loadStatistics()
  loadTodayAttendance()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.search-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

