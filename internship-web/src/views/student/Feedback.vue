<template>
  <PageLayout title="问题反馈">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">提交反馈</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="反馈类型">
          <el-select
            v-model="searchForm.feedbackType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="工作问题" :value="1" />
            <el-option label="学习问题" :value="2" />
            <el-option label="生活问题" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈状态">
          <el-select
            v-model="searchForm.feedbackStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待处理" :value="0" />
            <el-option label="处理中" :value="1" />
            <el-option label="已解决" :value="2" />
            <el-option label="已关闭" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 反馈列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="feedbackTitle" label="反馈标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="feedbackType" label="反馈类型" width="120" align="center">
        <template #default="{ row }">
          {{ getFeedbackTypeText(row.feedbackType) }}
        </template>
      </el-table-column>
      <el-table-column prop="feedbackStatus" label="反馈状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getFeedbackStatusType(row.feedbackStatus)" size="small">
            {{ getFeedbackStatusText(row.feedbackStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="提交时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.feedbackStatus === 0"
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.feedbackStatus === 2"
            link
            type="success"
            size="small"
            @click="handleSolve(row)"
          >
            标记已解决
          </el-button>
          <el-button
            v-if="row.feedbackStatus !== 3"
            link
            type="info"
            size="small"
            @click="handleClose(row)"
          >
            关闭
          </el-button>
          <el-button
            v-if="row.feedbackStatus === 0"
            link
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
            删除
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="反馈标题" prop="feedbackTitle">
          <el-input
            v-model="formData.feedbackTitle"
            placeholder="请输入反馈标题"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="反馈类型" prop="feedbackType">
              <el-select
                v-model="formData.feedbackType"
                placeholder="请选择类型"
                style="width: 100%"
              >
                <el-option label="工作问题" :value="1" />
                <el-option label="学习问题" :value="2" />
                <el-option label="生活问题" :value="3" />
                <el-option label="其他" :value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业名称">
              <el-input :value="formData.enterpriseName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="反馈内容" prop="feedbackContent">
          <el-input
            v-model="formData.feedbackContent"
            type="textarea"
            :rows="8"
            placeholder="请详细描述遇到的问题、困难或建议"
          />
        </el-form-item>
        <el-form-item label="附件" prop="feedbackAttachment">
          <el-input
            v-model="formData.feedbackAttachment"
            placeholder="请输入附件URL或上传附件（可选）"
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
      title="反馈详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="反馈标题">{{ detailData.feedbackTitle }}</el-descriptions-item>
        <el-descriptions-item label="反馈类型">
          {{ getFeedbackTypeText(detailData.feedbackType) }}
        </el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="反馈状态">
          <el-tag :type="getFeedbackStatusType(detailData.feedbackStatus)" size="small">
            {{ getFeedbackStatusText(detailData.feedbackStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="反馈内容" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.feedbackContent || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.feedbackAttachment" label="附件" :span="2">
          <el-link :href="detailData.feedbackAttachment" target="_blank" type="primary">
            {{ detailData.feedbackAttachment }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.replyContent" label="回复内容" :span="2">
          <div style="white-space: pre-wrap; color: #606266; background: #f5f7fa; padding: 10px; border-radius: 4px">
            {{ detailData.replyContent }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.replyTime" label="回复时间">
          {{ formatDateTime(detailData.replyTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.replyUserName" label="回复人">
          {{ detailData.replyUserName }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { feedbackApi } from '@/api/internship/feedback'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('提交反馈')
const formRef = ref(null)

const searchForm = reactive({
  feedbackType: null,
  feedbackStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentApply = ref(null)

const formData = reactive({
  feedbackId: null,
  applyId: null,
  enterpriseName: '',
  feedbackTitle: '',
  feedbackType: null,
  feedbackContent: '',
  feedbackAttachment: ''
})

const formRules = {
  feedbackTitle: [{ required: true, message: '请输入反馈标题', trigger: 'blur' }],
  feedbackType: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  feedbackContent: [{ required: true, message: '请输入反馈内容', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getFeedbackPage({
      current: pagination.current,
      size: pagination.size,
      feedbackType: searchForm.feedbackType !== null ? searchForm.feedbackType : undefined,
      feedbackStatus: searchForm.feedbackStatus !== null ? searchForm.feedbackStatus : undefined
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

// 加载当前学生的申请信息
const loadCurrentApply = async () => {
  try {
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 1,
      status: 1 // 已通过的申请
    })
    if (res.code === 200 && res.data.records && res.data.records.length > 0) {
      currentApply.value = res.data.records[0]
      formData.applyId = currentApply.value.applyId
      formData.enterpriseName = currentApply.value.enterpriseName || ''
    }
  } catch (error) {
    console.error('加载申请信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.feedbackType = null
  searchForm.feedbackStatus = null
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadCurrentApply()
  if (!currentApply.value) {
    ElMessage.warning('您还没有已通过的实习申请，无法提交反馈')
    return
  }
  dialogTitle.value = '提交反馈'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑反馈'
  try {
    const res = await feedbackApi.getFeedbackById(row.feedbackId)
    if (res.code === 200) {
      Object.assign(formData, {
        feedbackId: res.data.feedbackId,
        applyId: res.data.applyId,
        enterpriseName: res.data.enterpriseName || '',
        feedbackTitle: res.data.feedbackTitle,
        feedbackType: res.data.feedbackType,
        feedbackContent: res.data.feedbackContent || '',
        feedbackAttachment: res.data.feedbackAttachment || ''
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
    const res = await feedbackApi.getFeedbackById(row.feedbackId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 标记已解决
const handleSolve = async (row) => {
  try {
    await ElMessageBox.confirm('确定要标记该问题为已解决吗？', '提示', {
      type: 'info'
    })
    const res = await feedbackApi.solveFeedback(row.feedbackId)
    if (res.code === 200) {
      ElMessage.success('标记成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记失败:', error)
      ElMessage.error(error.response?.data?.message || '标记失败')
    }
  }
}

// 关闭
const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭该反馈吗？', '提示', {
      type: 'warning'
    })
    const res = await feedbackApi.closeFeedback(row.feedbackId)
    if (res.code === 200) {
      ElMessage.success('关闭成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭失败:', error)
      ElMessage.error(error.response?.data?.message || '关闭失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该反馈吗？', '提示', {
      type: 'warning'
    })
    const res = await feedbackApi.deleteFeedback(row.feedbackId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
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
          feedbackAttachment: formData.feedbackAttachment || undefined
        }
        if (formData.feedbackId) {
          data.feedbackId = formData.feedbackId
          const res = await feedbackApi.updateFeedback(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await feedbackApi.addFeedback(data)
          if (res.code === 200) {
            ElMessage.success('提交成功')
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

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    feedbackId: null,
    applyId: currentApply.value?.applyId || null,
    enterpriseName: currentApply.value?.enterpriseName || '',
    feedbackTitle: '',
    feedbackType: null,
    feedbackContent: '',
    feedbackAttachment: ''
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

// 获取反馈类型文本
const getFeedbackTypeText = (type) => {
  const typeMap = {
    1: '工作问题',
    2: '学习问题',
    3: '生活问题',
    4: '其他'
  }
  return typeMap[type] || '-'
}

// 获取反馈状态文本
const getFeedbackStatusText = (status) => {
  const statusMap = {
    0: '待处理',
    1: '处理中',
    2: '已解决',
    3: '已关闭'
  }
  return statusMap[status] || '未知'
}

// 获取反馈状态类型
const getFeedbackStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'success',
    3: 'info'
  }
  return typeMap[status] || 'info'
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

