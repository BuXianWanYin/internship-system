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
          <el-tag v-if="row.unbindStatus === 1" type="warning" size="small">待企业管理员审批</el-tag>
          <el-tag v-else-if="row.unbindStatus === 4" type="warning" size="small">待学校审批</el-tag>
          <el-tag v-else-if="row.unbindStatus === 2" type="success" size="small">离职审批通过</el-tag>
          <el-tag v-else-if="row.unbindStatus === 3" type="danger" size="small">已拒绝</el-tag>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button link type="success" size="small" @click="handleAssignMentor(row)">分配导师</el-button>
          <el-button
            v-if="row.unbindStatus === 1"
            link
            type="warning"
            size="small"
            @click="handleAuditUnbind(row)"
          >
            离职审批
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
          <el-tag v-if="detailData.unbindStatus === 1" type="warning" size="small">待企业管理员审批</el-tag>
          <el-tag v-else-if="detailData.unbindStatus === 4" type="warning" size="small">待学校审批</el-tag>
          <el-tag v-else-if="detailData.unbindStatus === 2" type="success" size="small">离职审批通过</el-tag>
          <el-tag v-else-if="detailData.unbindStatus === 3" type="danger" size="small">已拒绝</el-tag>
          <span v-else style="color: #909399">-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindReason" label="离职原因" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.unbindReason }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindAuditOpinion" label="审核意见" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.unbindAuditOpinion }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.unbindAuditTime" label="审核时间">
          {{ formatDateTime(detailData.unbindAuditTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核解绑对话框 -->
    <el-dialog
      v-model="unbindAuditDialogVisible"
      title="离职审批"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="unbindAuditFormRef"
        :model="unbindAuditForm"
        :rules="unbindAuditFormRules"
        label-width="100px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentUnbindApply.studentName }}（{{ currentUnbindApply.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>企业：</strong>{{ currentUnbindApply.enterpriseName || '-' }}</div>
            <div style="margin-top: 5px"><strong>岗位：</strong>{{ currentUnbindApply.postName || '-' }}</div>
            <div v-if="currentUnbindApply.unbindReason" style="margin-top: 10px; padding-top: 10px; border-top: 1px solid #dcdfe6">
              <strong>离职原因：</strong>
              <div style="margin-top: 5px; color: #606266">{{ currentUnbindApply.unbindReason }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="unbindAuditForm.auditStatus">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditOpinion">
          <el-input
            v-model="unbindAuditForm.auditOpinion"
            type="textarea"
            :rows="6"
            :placeholder="unbindAuditForm.auditStatus === 1 ? '请输入审核意见（可选）' : '请输入拒绝原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unbindAuditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="unbindAuditLoading" @click="handleSubmitUnbindAudit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { enterpriseMentorApi } from '@/api/user/enterpriseMentor'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
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

// 审核解绑相关
const unbindAuditDialogVisible = ref(false)
const unbindAuditLoading = ref(false)
const unbindAuditFormRef = ref(null)
const currentUnbindApply = ref({})
const unbindAuditForm = reactive({
  auditStatus: 1, // 1-已通过，2-已拒绝
  auditOpinion: ''
})
const unbindAuditFormRules = {
  auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  auditOpinion: [
    {
      validator: (rule, value, callback) => {
        if (unbindAuditForm.auditStatus === 2 && !value) {
          callback(new Error('拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
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

// 审核解绑
const handleAuditUnbind = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      currentUnbindApply.value = res.data
      unbindAuditForm.auditStatus = 1
      unbindAuditForm.auditOpinion = ''
      unbindAuditDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 提交解绑审核
const handleSubmitUnbindAudit = async () => {
  if (!unbindAuditFormRef.value) return
  await unbindAuditFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!currentUnbindApply.value || !currentUnbindApply.value.applyId) {
      ElMessage.error('申请ID不存在')
      return
    }
    
    try {
      unbindAuditLoading.value = true
      const res = await applyApi.auditUnbind(
        currentUnbindApply.value.applyId,
        unbindAuditForm.auditStatus,
        unbindAuditForm.auditOpinion || undefined
      )
      if (res.code === 200) {
        ElMessage.success('审核成功')
        unbindAuditDialogVisible.value = false
        loadData()
        // 如果详情对话框打开，刷新详情数据
        if (detailDialogVisible.value) {
          await handleView({ applyId: currentUnbindApply.value.applyId })
        }
      }
    } catch (error) {
      console.error('离职审批失败:', error)
      ElMessage.error(error.response?.data?.message || '离职审批失败')
    } finally {
      unbindAuditLoading.value = false
    }
  })
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

