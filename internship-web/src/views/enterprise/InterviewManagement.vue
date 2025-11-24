<template>
  <PageLayout title="面试管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">安排面试</el-button>
    </template>

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
        <el-form-item label="岗位名称">
          <el-input
            v-model="searchForm.postName"
            placeholder="请输入岗位名称"
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
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 面试列表 -->
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
      <el-table-column prop="interviewTime" label="面试时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.interviewTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="interviewLocation" label="面试地点" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="studentConfirm" label="学生确认" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.studentConfirm === 1" type="success" size="small">已确认</el-tag>
          <el-tag v-else-if="row.studentConfirm === 2" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="info" size="small">待确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="interviewResult" label="面试结果" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.interviewResult === 1" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="row.interviewResult === 2" type="danger" size="small">不通过</el-tag>
          <el-tag v-else-if="row.interviewResult === 3" type="warning" size="small">待定</el-tag>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 1 && !row.interviewResult"
            link
            type="success"
            size="small"
            @click="handleSubmitResult(row)"
          >
            提交结果
          </el-button>
          <el-button
            v-if="row.status !== 4 && row.status !== 3"
            link
            type="danger"
            size="small"
            @click="handleCancel(row)"
          >
            取消
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

    <!-- 添加/编辑面试对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="申请信息">
          <el-select
            v-model="formData.applyId"
            placeholder="请选择申请"
            filterable
            style="width: 100%"
            :disabled="!!formData.interviewId"
            @change="handleApplyChange"
          >
            <el-option
              v-for="apply in applyList"
              :key="apply.applyId"
              :label="`${apply.studentName} - ${apply.postName || '自主实习'}`"
              :value="apply.applyId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.studentName" label="学生姓名">
          <el-input :value="formData.studentName" disabled />
        </el-form-item>
        <el-form-item v-if="formData.postName" label="岗位名称">
          <el-input :value="formData.postName" disabled />
        </el-form-item>
        <el-form-item label="面试时间" prop="interviewTime">
          <el-date-picker
            v-model="formData.interviewTime"
            type="datetime"
            placeholder="请选择面试时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="面试地点" prop="interviewLocation">
          <el-input
            v-model="formData.interviewLocation"
            placeholder="请输入面试地点"
          />
        </el-form-item>
        <el-form-item label="面试方式">
          <el-radio-group v-model="formData.interviewType">
            <el-radio :label="1">现场面试</el-radio>
            <el-radio :label="2">视频面试</el-radio>
            <el-radio :label="3">电话面试</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.interviewType === 2" label="视频链接">
          <el-input
            v-model="formData.videoLink"
            placeholder="请输入视频会议链接"
          />
        </el-form-item>
        <el-form-item label="面试说明" prop="interviewDescription">
          <el-input
            v-model="formData.interviewDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入面试说明（面试要求、注意事项等）"
          />
        </el-form-item>
        <el-form-item label="联系人">
          <el-input
            v-model="formData.contactPerson"
            placeholder="请输入联系人"
          />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="formData.contactPhone"
            placeholder="请输入联系电话"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="面试详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.postName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="面试时间">
          {{ formatDateTime(detailData.interviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="面试地点">{{ detailData.interviewLocation || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试方式">
          {{ getInterviewTypeText(detailData.interviewType) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.videoLink" label="视频链接">
          <el-link :href="detailData.videoLink" target="_blank" type="primary">{{ detailData.videoLink }}</el-link>
        </el-descriptions-item>
        <el-descriptions-item label="学生确认">
          <el-tag v-if="detailData.studentConfirm === 1" type="success" size="small">已确认</el-tag>
          <el-tag v-else-if="detailData.studentConfirm === 2" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="info" size="small">待确认</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="面试结果">
          <el-tag v-if="detailData.interviewResult === 1" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="detailData.interviewResult === 2" type="danger" size="small">不通过</el-tag>
          <el-tag v-else-if="detailData.interviewResult === 3" type="warning" size="small">待定</el-tag>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试说明" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.interviewDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.interviewComment" label="面试评价" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.interviewComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.updateTime" label="更新时间">
          {{ formatDateTime(detailData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 提交面试结果对话框 -->
    <el-dialog
      v-model="resultDialogVisible"
      title="提交面试结果"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resultFormRef"
        :model="resultForm"
        :rules="resultFormRules"
        label-width="100px"
      >
        <el-form-item label="面试信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentInterview.studentName }}（{{ currentInterview.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>岗位：</strong>{{ currentInterview.postName || '-' }}</div>
            <div style="margin-top: 5px"><strong>面试时间：</strong>{{ formatDateTime(currentInterview.interviewTime) }}</div>
          </div>
        </el-form-item>
        <el-form-item label="面试结果" prop="interviewResult">
          <el-radio-group v-model="resultForm.interviewResult">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">不通过</el-radio>
            <el-radio :label="3">待定</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面试评价" prop="interviewComment">
          <el-input
            v-model="resultForm.interviewComment"
            type="textarea"
            :rows="6"
            placeholder="请输入面试评价"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resultLoading" @click="handleSubmitResultForm">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { interviewApi } from '@/api/internship/interview'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const resultLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const resultDialogVisible = ref(false)
const dialogTitle = ref('安排面试')
const formRef = ref(null)
const resultFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
  postName: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentInterview = ref({})
const applyList = ref([])

const formData = reactive({
  interviewId: null,
  applyId: null,
  studentName: '',
  postName: '',
  interviewTime: '',
  interviewLocation: '',
  interviewType: 1,
  videoLink: '',
  interviewDescription: '',
  contactPerson: '',
  contactPhone: ''
})

const resultForm = reactive({
  interviewResult: null,
  interviewComment: ''
})

const formRules = {
  applyId: [{ required: true, message: '请选择申请', trigger: 'change' }],
  interviewTime: [{ required: true, message: '请选择面试时间', trigger: 'change' }],
  interviewLocation: [{ required: true, message: '请输入面试地点', trigger: 'blur' }],
  interviewDescription: [{ required: true, message: '请输入面试说明', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const resultFormRules = {
  interviewResult: [{ required: true, message: '请选择面试结果', trigger: 'change' }],
  interviewComment: [{ required: true, message: '请输入面试评价', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await interviewApi.getInterviewPage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      postName: searchForm.postName || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
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

// 加载申请列表（用于选择）
const loadApplyList = async () => {
  try {
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 1000,
      status: 1 // 只显示已通过的申请
    })
    if (res.code === 200) {
      applyList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
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
  searchForm.postName = ''
  searchForm.status = null
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadApplyList()
  dialogTitle.value = '安排面试'
  // 如果表单中已经有applyId（从路由参数填充的），保留它
  const savedApplyId = formData.applyId
  const savedStudentName = formData.studentName
  const savedPostName = formData.postName
  resetForm()
  // 恢复从路由参数填充的值
  if (savedApplyId) {
    formData.applyId = savedApplyId
    formData.studentName = savedStudentName
    formData.postName = savedPostName
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  await loadApplyList()
  dialogTitle.value = '编辑面试'
  try {
    const res = await interviewApi.getInterviewById(row.interviewId)
    if (res.code === 200) {
      Object.assign(formData, {
        interviewId: res.data.interviewId,
        applyId: res.data.applyId,
        studentName: res.data.studentName || '',
        postName: res.data.postName || '',
        interviewTime: res.data.interviewTime,
        interviewLocation: res.data.interviewLocation || '',
        interviewType: res.data.interviewType || 1,
        videoLink: res.data.videoLink || '',
        interviewDescription: res.data.interviewDescription || '',
        contactPerson: res.data.contactPerson || '',
        contactPhone: res.data.contactPhone || ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await interviewApi.getInterviewById(row.interviewId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 申请选择变化
const handleApplyChange = (applyId) => {
  const apply = applyList.value.find(item => item.applyId === applyId)
  if (apply) {
    formData.studentName = apply.studentName || ''
    formData.postName = apply.postName || ''
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = {
          ...formData,
          videoLink: formData.interviewType === 2 ? formData.videoLink : undefined
        }
        if (formData.interviewId) {
          data.interviewId = formData.interviewId
          const res = await interviewApi.updateInterview(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await interviewApi.addInterview(data)
          if (res.code === 200) {
            ElMessage.success('安排成功')
            dialogVisible.value = false
            loadData()
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交面试结果
const handleSubmitResult = (row) => {
  currentInterview.value = row
  resultForm.interviewResult = null
  resultForm.interviewComment = ''
  resultDialogVisible.value = true
}

// 提交面试结果表单
const handleSubmitResultForm = async () => {
  if (!resultFormRef.value) return
  await resultFormRef.value.validate(async (valid) => {
    if (valid) {
      resultLoading.value = true
      try {
        const res = await interviewApi.submitInterviewResult(
          currentInterview.value.interviewId,
          resultForm.interviewResult,
          resultForm.interviewComment
        )
        if (res.code === 200) {
          ElMessage.success('提交成功')
          resultDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        resultLoading.value = false
      }
    }
  })
}

// 取消面试
const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确定要取消该面试吗？', '提示', {
      type: 'warning'
    })
    const res = await interviewApi.cancelInterview(row.interviewId)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error(error.response?.data?.message || '取消失败')
    }
  }
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    interviewId: null,
    applyId: null,
    studentName: '',
    postName: '',
    interviewTime: '',
    interviewLocation: '',
    interviewType: 1,
    videoLink: '',
    interviewDescription: '',
    contactPerson: '',
    contactPhone: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已拒绝',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取面试方式文本
const getInterviewTypeText = (type) => {
  const typeMap = {
    1: '现场面试',
    2: '视频面试',
    3: '电话面试'
  }
  return typeMap[type] || '-'
}

const route = useRoute()

// 初始化
onMounted(() => {
  loadData()
  loadApplyList()
  
  // 如果从申请管理页面跳转过来，自动填充表单并打开对话框
  if (route.query.applyId) {
    const applyId = parseInt(route.query.applyId)
    const studentId = route.query.studentId ? parseInt(route.query.studentId) : null
    const studentName = route.query.studentName || ''
    const postId = route.query.postId ? parseInt(route.query.postId) : null
    const postName = route.query.postName || ''
    
    // 填充表单
    formData.applyId = applyId
    formData.studentName = studentName
    formData.postName = postName
    
    // 打开添加对话框
    handleAdd()
    
    // 清除路由参数（避免刷新时重复打开）
    if (window.history && window.history.replaceState) {
      const newQuery = { ...route.query }
      delete newQuery.applyId
      delete newQuery.studentId
      delete newQuery.studentName
      delete newQuery.postId
      delete newQuery.postName
      window.history.replaceState({}, '', {
        path: route.path,
        query: newQuery
      })
    }
  }
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

