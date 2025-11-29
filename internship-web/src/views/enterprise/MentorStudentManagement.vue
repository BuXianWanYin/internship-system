<template>
  <PageLayout title="我指导的学生">
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
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已录用" :value="3" />
            <el-option label="已拒绝录用" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button 
            type="success" 
            :icon="Download" 
            @click="handleExportStudentList"
            :loading="exportLoading"
          >
            导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 学生列表 -->
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
      <el-table-column prop="enterpriseName" label="企业名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="考勤组" width="150" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.attendanceGroupName" type="success" size="small">
            {{ row.attendanceGroupName }}
          </el-tag>
          <el-tag v-else type="danger" size="small">未分配</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在职状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getUnbindStatusType(row)" size="small">
            {{ getUnbindStatusText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="分配时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="350" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button 
            link 
            type="warning" 
            size="small" 
            @click="handleAssignAttendanceGroup(row)"
            v-if="row.applyId && row.status === 3"
          >
            分配考勤组
          </el-button>
          <el-button 
            link 
            type="success" 
            size="small" 
            :icon="Download"
            @click="handleExportStudentReport(row)"
            v-if="row.applyId"
          >
            导出报告
          </el-button>
          <el-button
            v-if="canMarkAsCompleted(row)"
            link
            type="success"
            size="small"
            @click="handleMarkAsCompleted(row)"
          >
            结束实习
          </el-button>
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
      title="学生详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || detailData.selfEnterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || detailData.selfPostName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="在职状态">
          <el-tag :type="getUnbindStatusType(detailData)" size="small">
            {{ getUnbindStatusText(detailData) }}
          </el-tag>
        </el-descriptions-item>
        <!-- 如果是实习结束，显示结束日期和备注 -->
        <el-descriptions-item v-if="isInternshipCompleted(detailData)" label="实习结束日期">
          {{ formatDate(detailData.internshipEndDate) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="isInternshipCompleted(detailData) && detailData.unbindAuditOpinion" label="结束备注" :span="2">
          {{ detailData.unbindAuditOpinion }}
        </el-descriptions-item>
        <el-descriptions-item label="实习开始日期">
          {{ detailData.internshipStartDate ? formatDate(detailData.internshipStartDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束日期">
          {{ detailData.internshipEndDate ? formatDate(detailData.internshipEndDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="分配时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindReason" label="解绑原因" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.unbindReason }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindAuditOpinion" label="解绑备注" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.unbindAuditOpinion }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindAuditTime" label="解绑时间">
          {{ formatDateTime(detailData.unbindAuditTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="考勤组">
          <el-tag v-if="detailData.attendanceGroupName" type="success" size="small">
            {{ detailData.attendanceGroupName }}
          </el-tag>
          <el-tag v-else type="danger" size="small">未分配</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 分配考勤组对话框 -->
    <el-dialog
      v-model="assignGroupDialogVisible"
      title="分配考勤组"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="assignGroupFormRef"
        :model="assignGroupForm"
        :rules="assignGroupFormRules"
        label-width="120px"
      >
        <el-form-item label="学生信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生姓名：</strong>{{ currentStudent.studentName }}</div>
            <div style="margin-top: 5px"><strong>学号：</strong>{{ currentStudent.studentNo }}</div>
            <div style="margin-top: 5px" v-if="currentStudent.internshipStartDate && currentStudent.internshipEndDate">
              <strong>实习日期：</strong>{{ formatDate(currentStudent.internshipStartDate) }} 至 {{ formatDate(currentStudent.internshipEndDate) }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="选择考勤组" prop="groupId">
          <el-select
            v-model="assignGroupForm.groupId"
            placeholder="请选择考勤组"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="group in attendanceGroupList"
              :key="group.groupId"
              :label="`${group.groupName}（${getWorkDaysTypeText(group.workDaysType)}，${group.timeSlotCount || 0}个时间段）`"
              :value="group.groupId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生效开始日期" prop="effectiveStartDate">
          <el-date-picker
            v-model="assignGroupForm.effectiveStartDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生效结束日期" prop="effectiveEndDate">
          <el-date-picker
            v-model="assignGroupForm.effectiveEndDate"
            type="date"
            placeholder="选择结束日期（可选）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignGroupDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignGroupSubmitLoading" @click="handleSubmitAssignGroup">确定</el-button>
      </template>
    </el-dialog>

  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { attendanceGroupApi } from '@/api/internship/attendanceGroup'
import { reportApi } from '@/api/report'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import { exportExcel } from '@/utils/exportUtils'
import { isInternshipCompleted, isUnbound, getUnbindStatusText, getUnbindStatusType } from '@/utils/statusUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  studentName: '',
  studentNo: '',
  status: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const detailData = ref({})

// 结束实习相关
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const currentCompleteApply = ref({})
const completeLoading = ref(false)

const completeForm = reactive({
  endDate: null,
  remark: ''
})

const completeFormRules = {
  endDate: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          const endDate = new Date(value)
          const startDate = currentCompleteApply.value.internshipStartDate
          if (startDate && endDate < new Date(startDate)) {
            callback(new Error('实习结束日期不能早于开始日期'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 考勤组分配相关
const assignGroupDialogVisible = ref(false)
const assignGroupForm = reactive({
  groupId: null,
  effectiveStartDate: null,
  effectiveEndDate: null
})
const assignGroupFormRef = ref(null)
const assignGroupSubmitLoading = ref(false)
const currentStudent = ref({})
const attendanceGroupList = ref([])

const assignGroupFormRules = {
  groupId: [{ required: true, message: '请选择考勤组', trigger: 'change' }],
  effectiveStartDate: [{ required: true, message: '请选择生效开始日期', trigger: 'change' }]
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已录用',
    4: '已拒绝录用'
  }
  return statusMap[status] || '-'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await applyApi.getMentorStudents({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      const records = res.data.records || []
      // 为每个学生加载考勤组信息
      for (const record of records) {
        if (record.applyId) {
          try {
            const groupRes = await attendanceGroupApi.getGroupByApplyId(record.applyId)
            if (groupRes.code === 200 && groupRes.data) {
              record.attendanceGroupName = groupRes.data.groupName
              record.attendanceGroupId = groupRes.data.groupId
            }
          } catch (error) {
            // 忽略错误，可能学生未分配考勤组
          }
        }
      }
      tableData.value = records
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.status = null
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.current = 1
  loadData()
}

// 页码改变
const handlePageChange = () => {
  loadData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data || {}
      // 加载考勤组信息
      if (row.applyId) {
        try {
          const groupRes = await attendanceGroupApi.getGroupByApplyId(row.applyId)
          if (groupRes.code === 200 && groupRes.data) {
            detailData.value.attendanceGroupName = groupRes.data.groupName
          }
        } catch (error) {
          // 忽略错误
        }
      }
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取详情失败')
  }
}

// 分配考勤组
const handleAssignAttendanceGroup = async (row) => {
  currentStudent.value = row
  try {
    // 加载可用的考勤组列表（只显示已启用的）
    const groupRes = await attendanceGroupApi.getAttendanceGroupPage({
      current: 1,
      size: 1000
    })
    if (groupRes.code === 200) {
      attendanceGroupList.value = (groupRes.data.records || []).filter(g => g.status === 1)
      if (attendanceGroupList.value.length === 0) {
        ElMessage.warning('暂无可用的考勤组，请先创建考勤组')
        return
      }
      
      // 设置默认值
      assignGroupForm.groupId = row.attendanceGroupId || null
      assignGroupForm.effectiveStartDate = row.internshipStartDate || null
      assignGroupForm.effectiveEndDate = row.internshipEndDate || null
      
      assignGroupDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载考勤组列表失败：' + (error.message || '未知错误'))
  }
}

// 提交分配考勤组
const handleSubmitAssignGroup = async () => {
  if (!assignGroupFormRef.value) return
  await assignGroupFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    assignGroupSubmitLoading.value = true
    try {
      const res = await attendanceGroupApi.assignStudentToGroup(
        assignGroupForm.groupId,
        currentStudent.value.applyId,
        assignGroupForm.effectiveStartDate,
        assignGroupForm.effectiveEndDate || null
      )
      if (res.code === 200) {
        ElMessage.success('分配成功')
        assignGroupDialogVisible.value = false
        loadData() // 重新加载数据
      }
    } catch (error) {
      ElMessage.error('分配失败：' + (error.message || '未知错误'))
    } finally {
      assignGroupSubmitLoading.value = false
    }
  })
}

// 获取工作日类型文本
const getWorkDaysTypeText = (type) => {
  const map = {
    1: '周一到周五',
    2: '周一到周六',
    3: '周一到周日',
    4: '自定义'
  }
  return map[type] || '-'
}

// 导出学生列表
const handleExportStudentList = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数，使用当前页面的筛选条件
    const params = {
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    
    // 注意：这里需要后端提供导师学生列表导出接口
    await exportExcel(
      reportApi.exportInternshipSummaryReport,
      params,
      '我指导的学生列表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

// 导出学生个人实习报告
const handleExportStudentReport = async (row) => {
  if (!row.applyId) {
    ElMessage.warning('该学生暂无实习申请')
    return
  }
  
  exportLoading.value = true
  try {
    await exportExcel(
      () => reportApi.exportStudentInternshipReport(row.applyId),
      {},
      `学生实习报告_${row.studentNo}_${row.studentName}`
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

