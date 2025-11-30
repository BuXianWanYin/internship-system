<template>
  <PageLayout title="实习学生管理">
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
        <el-form-item label="岗位名称">
          <el-input
            v-model="searchForm.postName"
            placeholder="请输入岗位名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="mentorName" label="企业导师" min-width="120">
        <template #default="{ row }">
          {{ row.mentorName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录用时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.acceptTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="在职状态" width="150" align="center">
        <template #default="{ row }">
          <el-tag :type="getUnbindStatusType(row)" size="small">
            {{ getUnbindStatusText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button link type="success" size="small" @click="handleAssignMentor(row)">分配导师</el-button>
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

    <!-- 分配导师对话框 -->
    <el-dialog
      v-model="assignMentorDialogVisible"
      title="分配企业导师"
      width="500px"
    >
      <el-form
        ref="assignMentorFormRef"
        :model="assignMentorForm"
        :rules="assignMentorRules"
        label-width="100px"
      >
        <el-form-item label="学生姓名">
          <el-input :value="currentStudent?.studentName" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input :value="currentStudent?.studentNo" disabled />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input :value="currentStudent?.postName" disabled />
        </el-form-item>
        <el-form-item label="企业导师" prop="mentorId">
          <el-select
            v-model="assignMentorForm.mentorId"
            placeholder="请选择企业导师"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="mentor in mentorList"
              :key="mentor.mentorId"
              :label="mentor.mentorName"
              :value="mentor.mentorId"
            >
              <span>{{ mentor.mentorName }}</span>
              <span style="color: #8492a6; font-size: 13px; margin-left: 10px">
                {{ mentor.position || '' }} {{ mentor.department || '' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignMentorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignMentorLoading" @click="handleSubmitAssignMentor">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学生详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业导师">{{ detailData.mentorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="录用时间">
          {{ formatDateTime(detailData.acceptTime || detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success" size="small">已录用</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="在职状态">
          <el-tag v-if="detailData.unbindStatus === 2" type="success" size="small">已解绑</el-tag>
          <el-tag v-else type="info" size="small">未解绑</el-tag>
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
        <!-- 如果是实习结束，显示结束日期和备注 -->
        <el-descriptions-item v-if="isInternshipCompleted(detailData)" label="实习结束日期">
          {{ formatDate(detailData.internshipEndDate) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="isInternshipCompleted(detailData) && detailData.unbindAuditOpinion" label="结束备注" :span="2">
          {{ detailData.unbindAuditOpinion }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="canMarkAsCompleted(detailData)"
          type="success"
          @click="handleMarkAsCompletedFromDetail"
        >
          结束实习
        </el-button>
      </template>
    </el-dialog>

    <!-- 结束实习对话框 -->
    <el-dialog
      v-model="completeDialogVisible"
      title="结束实习"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="completeFormRef"
        :model="completeForm"
        :rules="completeFormRules"
        label-width="120px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentCompleteApply.studentName }}（{{ currentCompleteApply.studentNo }}）</div>
            <div style="margin-top: 5px">
              <strong>企业：</strong>
              {{ currentCompleteApply.enterpriseName || '-' }}
            </div>
            <div style="margin-top: 5px">
              <strong>岗位：</strong>
              {{ currentCompleteApply.postName || '-' }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="实习结束日期" prop="endDate">
          <el-date-picker
            v-model="completeForm.endDate"
            type="date"
            placeholder="请选择结束日期（不选则使用今天）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="completeForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        >
          <template #default>
            <div>结束实习后，学生的实习状态将更新为"已结束"，可以进行评价。</div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleSubmitComplete">确定</el-button>
      </template>
    </el-dialog>

  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Download } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { enterpriseMentorApi } from '@/api/user/enterpriseMentor'
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
  postName: ''
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 分配导师相关
const assignMentorDialogVisible = ref(false)
const assignMentorLoading = ref(false)
const assignMentorFormRef = ref(null)
const currentStudent = ref(null)
const mentorList = ref([])
const assignMentorForm = reactive({
  mentorId: null
})
const assignMentorRules = {
  mentorId: [{ required: true, message: '请选择企业导师', trigger: 'change' }]
}

// 查看详情相关
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


// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      postName: searchForm.postName || undefined
    }
    const res = await applyApi.getEnterpriseStudents(params)
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

// 加载企业导师列表
const loadMentorList = async () => {
  try {
    const res = await enterpriseMentorApi.getEnterpriseMentorPage({
      current: 1,
      size: 1000,
      status: 1 // 只查询启用的导师
    })
    if (res.code === 200) {
      mentorList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载企业导师列表失败:', error)
    ElMessage.error('加载企业导师列表失败')
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    studentName: '',
    studentNo: '',
    postName: ''
  })
  pagination.current = 1
  loadData()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 页码变化
const handlePageChange = (current) => {
  pagination.current = current
  loadData()
}

// 判断是否可以标记为结束（仅合作企业）
const canMarkAsCompleted = (row) => {
  if (!row) return false
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  // 仅合作企业：status=3（已录用）且未标记结束（status!=7）
  return row.applyType === 1 && row.status === 3 && row.status !== 7
}

// 获取状态文本
const getStatusText = (status, applyType) => {
  if (status === null || status === undefined) return '-'
  if (applyType === 1) {
    const statusMap = {
      0: '待审核',
      1: '已通过',
      2: '已拒绝',
      3: '已录用',
      4: '已拒绝录用',
      5: '已取消',
      7: '实习结束',
      8: '已评价'
    }
    return statusMap[status] || '-'
  }
  return '-'
}

// 获取状态标签类型
const getStatusType = (status, applyType) => {
  if (status === null || status === undefined) return 'info'
  if (applyType === 1) {
    if (status === 7 || status === 8) return 'info'
    if (status === 3) return 'success'
    if (status === 1) return 'success'
    if (status === 2 || status === 4 || status === 5) return 'danger'
    return 'warning'
  }
  return 'info'
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 处理结束实习
const handleMarkAsCompleted = (row) => {
  currentCompleteApply.value = { ...row }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 从详情对话框处理结束实习
const handleMarkAsCompletedFromDetail = () => {
  currentCompleteApply.value = { ...detailData.value }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 提交结束实习
const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  
  try {
    await completeFormRef.value.validate()
    
    completeLoading.value = true
    try {
      const res = await applyApi.completeInternship(
        currentCompleteApply.value.applyId,
        completeForm.endDate,
        completeForm.remark
      )
      if (res.code === 200) {
        ElMessage.success('结束实习成功')
        completeDialogVisible.value = false
        // 刷新列表
        await loadData()
        // 如果详情对话框打开，刷新详情
        if (detailDialogVisible.value) {
          await handleView(currentCompleteApply.value)
        }
      }
    } catch (error) {
      console.error('结束实习失败:', error)
      ElMessage.error(error.response?.data?.message || '结束实习失败')
    } finally {
      completeLoading.value = false
    }
  } catch (error) {
    // 表单验证失败
  }
}

// 分配导师
const handleAssignMentor = async (row) => {
  currentStudent.value = row
  assignMentorForm.mentorId = row.mentorId || null
  await loadMentorList()
  assignMentorDialogVisible.value = true
}

// 提交分配导师
const handleSubmitAssignMentor = async () => {
  if (!assignMentorFormRef.value) return
  await assignMentorFormRef.value.validate(async (valid) => {
    if (valid) {
      assignMentorLoading.value = true
      try {
        const res = await applyApi.assignMentor(
          currentStudent.value.applyId,
          assignMentorForm.mentorId
        )
        if (res.code === 200) {
          ElMessage.success('分配成功')
          assignMentorDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('分配失败:', error)
        ElMessage.error(error.response?.data?.message || '分配失败')
      } finally {
        assignMentorLoading.value = false
      }
    }
  })
}
// 导出学生列表
const handleExportStudentList = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数，使用当前页面的筛选条件
    const params = {
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      postName: searchForm.postName || undefined
    }
    
    // 注意：这里需要后端提供企业学生列表导出接口
    // 暂时使用实习情况汇总表接口，或者需要创建新的接口
    await exportExcel(
      reportApi.exportInternshipSummaryReport,
      params,
      '实习学生列表'
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

