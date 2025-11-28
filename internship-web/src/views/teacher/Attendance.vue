<template>
  <PageLayout title="考勤查看">
    <!-- 搜索栏 -->
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
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="date"
            placeholder="请选择日期"
            clearable
            style="width: 200px"
            value-format="YYYY-MM-DD"
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
        <el-form-item label="确认状态">
          <el-select
            v-model="searchForm.confirmStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考勤列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="studentName" label="学生姓名" min-width="120" />
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="attendanceDate" label="考勤日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.attendanceDate) }}
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
      <el-table-column prop="attendanceType" label="考勤类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getAttendanceTypeTagType(row.attendanceType)" size="small">
            {{ getAttendanceTypeText(row.attendanceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workHours" label="工作时长" width="100" align="center">
        <template #default="{ row }">
          {{ row.workHours ? `${row.workHours}小时` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="confirmStatus" label="确认状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getConfirmStatusTagType(row.confirmStatus)" size="small">
            {{ getConfirmStatusText(row.confirmStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
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
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const detailDialogVisible = ref(false)

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  attendanceDate: null,
  attendanceType: null,
  confirmStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await attendanceApi.getAttendancePage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      attendanceDate: searchForm.attendanceDate || undefined,
      attendanceType: searchForm.attendanceType !== null ? searchForm.attendanceType : undefined,
      confirmStatus: searchForm.confirmStatus !== null ? searchForm.confirmStatus : undefined
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.attendanceDate = null
  searchForm.attendanceType = null
  searchForm.confirmStatus = null
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

