<template>
  <PageLayout title="考勤查看">
    <!-- 搜索筛选栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentName"
            placeholder="请输入学生姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentNo"
            placeholder="请输入学号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日历和统计面板 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <!-- 左侧：日历组件 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between">
              <span style="font-size: 14px; font-weight: 600">考勤日历</span>
              <div style="display: flex; gap: 10px; align-items: center">
                <div class="legend-item">
                  <span class="legend-dot normal"></span>
                  <span>正常</span>
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
                  <span class="legend-dot pending"></span>
                  <span>待确认</span>
                </div>
              </div>
            </div>
          </template>
          <el-calendar v-loading="loading" v-model="calendarDate">
            <template #date-cell="{ data }">
              <div class="calendar-cell" @click="handleDateClick(data.day)">
                <div class="calendar-date">{{ data.day.split('-').slice(2).join('-') }}</div>
                <div v-if="getDateAttendanceCount(data.day) > 0" class="calendar-content">
                  <div class="attendance-summary">
                    <span class="count-badge">{{ getDateAttendanceCount(data.day) }}人</span>
                    <span v-if="getPendingCount(data.day) > 0" class="pending-badge">
                      {{ getPendingCount(data.day) }}待确认
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
      <!-- 右侧：统计面板 -->
      <el-col :span="8">
        <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">待确认数量</div>
            <div class="stat-value" style="color: #e6a23c; cursor: pointer" @click="handleViewPending">
              {{ statistics.pendingCount || 0 }}
            </div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">异常考勤</div>
            <div class="stat-value" style="color: #f56c6c">{{ statistics.abnormalCount || 0 }}</div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">今日出勤率</div>
            <div class="stat-value" style="color: #409eff">
              {{ statistics.todayAttendanceRate ? statistics.todayAttendanceRate.toFixed(2) + '%' : '0%' }}
            </div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">本周出勤率</div>
            <div class="stat-value" style="color: #67c23a">
              {{ statistics.weekAttendanceRate ? statistics.weekAttendanceRate.toFixed(2) + '%' : '0%' }}
            </div>
          </div>
        </el-card>
        <el-card shadow="hover" style="margin-bottom: 12px">
          <div class="stat-item">
            <div class="stat-label">总考勤记录</div>
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
          </div>
        </el-card>
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">已确认记录</div>
            <div class="stat-value" style="color: #67c23a">{{ statistics.confirmedCount || 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日期考勤列表对话框 -->
    <el-dialog
      v-model="dateListDialogVisible"
      :title="`${selectedDate} 考勤记录`"
      width="900px"
    >
      <el-table
        :data="dateAttendanceList"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" min-width="120" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="attendanceType" label="考勤类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAttendanceTypeTagType(row.attendanceType)" size="small">
              {{ getAttendanceTypeText(row.attendanceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="上班打卡" width="150">
          <template #default="{ row }">
            {{ row.checkInTime ? formatTime(row.checkInTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="下班打卡" width="150">
          <template #default="{ row }">
            {{ row.checkOutTime ? formatTime(row.checkOutTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="确认状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getConfirmStatusTagType(row.confirmStatus)" size="small">
              {{ getConfirmStatusText(row.confirmStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.confirmStatus === 0"
              link
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.confirmStatus === 0"
              link
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
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
          <el-tag :type="getConfirmStatusTagType(detailData.confirmStatus)" size="small">
            {{ getConfirmStatusText(detailData.confirmStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.confirmComment" label="确认意见" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.confirmComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.remark" label="备注" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.remark }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.updateTime" label="更新时间">
          {{ formatDateTime(detailData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 确认/拒绝对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      :title="confirmAction === 'confirm' ? '确认考勤' : '拒绝考勤'"
      width="500px"
    >
      <el-form :model="confirmForm" label-width="100px">
        <el-form-item label="学生姓名">
          <el-input :value="confirmForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="考勤日期">
          <el-input :value="formatDate(confirmForm.attendanceDate)" disabled />
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input
            v-model="confirmForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="confirmAction === 'confirm' ? '请输入确认意见（可选）' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button
          :type="confirmAction === 'confirm' ? 'success' : 'danger'"
          @click="handleSubmitConfirm"
        >
          {{ confirmAction === 'confirm' ? '确认' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const detailDialogVisible = ref(false)
const dateListDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const confirmAction = ref('confirm') // 'confirm' or 'reject'

const searchForm = reactive({
  studentName: '',
  studentNo: ''
})

const calendarDate = ref(new Date())
const selectedDate = ref('')
const dateAttendanceList = ref([])
const allAttendanceData = ref([]) // 存储所有考勤数据
const attendanceMap = ref(new Map()) // 按日期组织的考勤数据

const detailData = ref({})
const confirmForm = reactive({
  attendanceId: null,
  studentName: '',
  attendanceDate: '',
  comment: ''
})

const statistics = ref({
  pendingCount: 0,
  abnormalCount: 0,
  todayAttendanceRate: 0,
  weekAttendanceRate: 0,
  totalCount: 0,
  confirmedCount: 0
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载所有考勤数据用于日历显示
    const res = await attendanceApi.getAttendancePage({
      current: 1,
      size: 1000,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined
    })
    if (res.code === 200) {
      allAttendanceData.value = res.data.records || []
      
      // 构建考勤数据映射（按日期）
      attendanceMap.value.clear()
      allAttendanceData.value.forEach(item => {
        if (item.attendanceDate) {
          const date = item.attendanceDate
          if (!attendanceMap.value.has(date)) {
            attendanceMap.value.set(date, [])
          }
          attendanceMap.value.get(date).push(item)
        }
      })
      
      // 计算统计数据
      calculateStatistics()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStatistics = () => {
  const today = new Date().toISOString().split('T')[0]
  const todayList = attendanceMap.value.get(today) || []
  
  // 待确认数量
  statistics.value.pendingCount = allAttendanceData.value.filter(item => item.confirmStatus === 0).length
  
  // 异常考勤（迟到、早退、缺勤）
  statistics.value.abnormalCount = allAttendanceData.value.filter(item => 
    item.attendanceType === 2 || item.attendanceType === 3 || item.attendanceType === 5
  ).length
  
  // 今日出勤率
  const todayTotal = todayList.length
  const todayNormal = todayList.filter(item => item.attendanceType === 1 && item.confirmStatus === 1).length
  statistics.value.todayAttendanceRate = todayTotal > 0 ? (todayNormal / todayTotal) * 100 : 0
  
  // 本周出勤率（简化计算，使用所有数据）
  const totalCount = allAttendanceData.value.length
  const normalCount = allAttendanceData.value.filter(item => 
    item.attendanceType === 1 && item.confirmStatus === 1
  ).length
  statistics.value.weekAttendanceRate = totalCount > 0 ? (normalCount / totalCount) * 100 : 0
  
  // 总记录数和已确认数
  statistics.value.totalCount = allAttendanceData.value.length
  statistics.value.confirmedCount = allAttendanceData.value.filter(item => item.confirmStatus === 1).length
}

// 获取日期考勤数量
const getDateAttendanceCount = (dateStr) => {
  if (!dateStr) return 0
  const dateOnly = dateStr.split(' ')[0]
  const list = attendanceMap.value.get(dateOnly) || []
  return list.length
}

// 获取日期待确认数量
const getPendingCount = (dateStr) => {
  if (!dateStr) return 0
  const dateOnly = dateStr.split(' ')[0]
  const list = attendanceMap.value.get(dateOnly) || []
  return list.filter(item => item.confirmStatus === 0).length
}

// 点击日期
const handleDateClick = (dateStr) => {
  if (!dateStr) return
  const dateOnly = dateStr.split(' ')[0]
  selectedDate.value = dateOnly
  dateAttendanceList.value = attendanceMap.value.get(dateOnly) || []
  dateListDialogVisible.value = true
}

// 查看待确认
const handleViewPending = () => {
  searchForm.confirmStatus = 0
  handleSearch()
  // 也可以直接显示待确认列表
  const pendingList = allAttendanceData.value.filter(item => item.confirmStatus === 0)
  if (pendingList.length > 0) {
    // 显示第一个待确认的日期
    const firstPending = pendingList[0]
    handleDateClick(firstPending.attendanceDate)
  } else {
    ElMessage.info('暂无待确认的考勤记录')
  }
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

// 确认考勤
const handleConfirm = (row) => {
  confirmAction.value = 'confirm'
  confirmForm.attendanceId = row.attendanceId
  confirmForm.studentName = row.studentName
  confirmForm.attendanceDate = row.attendanceDate
  confirmForm.comment = ''
  confirmDialogVisible.value = true
}

// 拒绝考勤
const handleReject = (row) => {
  confirmAction.value = 'reject'
  confirmForm.attendanceId = row.attendanceId
  confirmForm.studentName = row.studentName
  confirmForm.attendanceDate = row.attendanceDate
  confirmForm.comment = ''
  confirmDialogVisible.value = true
}

// 提交确认/拒绝
const handleSubmitConfirm = async () => {
  if (confirmAction.value === 'reject' && !confirmForm.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  try {
    const confirmStatus = confirmAction.value === 'confirm' ? 1 : 2
    const res = await attendanceApi.confirmAttendance(
      confirmForm.attendanceId,
      confirmStatus,
      confirmForm.comment || undefined
    )
    if (res.code === 200) {
      ElMessage.success(confirmAction.value === 'confirm' ? '确认成功' : '拒绝成功')
      confirmDialogVisible.value = false
      await loadData()
      // 刷新日期列表
      if (dateListDialogVisible.value) {
        handleDateClick(selectedDate.value)
      }
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

// 格式化时间（只显示时分）
const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 搜索
const handleSearch = () => {
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  handleSearch()
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

// 获取确认状态文本
const getConfirmStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已拒绝'
  }
  return statusMap[status] || '-'
}

// 获取确认状态标签类型
const getConfirmStatusTagType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return statusMap[status] || 'info'
}

// 初始化
onMounted(() => {
  loadData()
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
  padding: 1px 2px;
  cursor: pointer;
  transition: background-color 0.2s;
  overflow: hidden;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.calendar-date {
  font-size: 11px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.calendar-content {
  font-size: 10px;
}

.attendance-summary {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.count-badge {
  font-size: 10px;
  color: #409eff;
  font-weight: 500;
}

.pending-badge {
  font-size: 9px;
  color: #e6a23c;
  background-color: #fdf6ec;
  padding: 1px 4px;
  border-radius: 2px;
  display: inline-block;
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

.legend-dot.leave {
  background-color: #909399;
}

.legend-dot.rest {
  background-color: #409eff;
}

.legend-dot.pending {
  background-color: #e6a23c;
}

:deep(.el-calendar-day) {
  height: 50px;
  padding: 0;
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
