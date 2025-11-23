<template>
  <PageLayout title="问题反馈处理">
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
      <el-table-column prop="studentName" label="学生姓名" min-width="120" />
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
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
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.feedbackStatus === 0 || row.feedbackStatus === 1"
            link
            type="success"
            size="small"
            @click="handleReply(row)"
          >
            回复
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
      title="反馈详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
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
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.feedbackStatus === 0 || detailData.feedbackStatus === 1"
          type="primary"
          @click="handleReplyFromDetail"
        >
          回复
        </el-button>
      </template>
    </el-dialog>

    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复反馈"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="replyFormRef"
        :model="replyForm"
        :rules="replyFormRules"
        label-width="100px"
      >
        <el-form-item label="反馈信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentFeedback.studentName }}（{{ currentFeedback.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>标题：</strong>{{ currentFeedback.feedbackTitle }}</div>
            <div style="margin-top: 5px"><strong>类型：</strong>{{ getFeedbackTypeText(currentFeedback.feedbackType) }}</div>
          </div>
        </el-form-item>
        <el-form-item label="回复内容" prop="replyContent">
          <el-input
            v-model="replyForm.replyContent"
            type="textarea"
            :rows="8"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="replyLoading" @click="handleSubmitReply">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { feedbackApi } from '@/api/internship/feedback'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const replyLoading = ref(false)
const detailDialogVisible = ref(false)
const replyDialogVisible = ref(false)
const replyFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
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
const currentFeedback = ref({})

const replyForm = reactive({
  replyContent: ''
})

const replyFormRules = {
  replyContent: [{ required: true, message: '请输入回复内容', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getFeedbackPage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.feedbackType = null
  searchForm.feedbackStatus = null
  handleSearch()
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

// 回复
const handleReply = (row) => {
  currentFeedback.value = row
  replyForm.replyContent = ''
  replyDialogVisible.value = true
}

// 从详情对话框回复
const handleReplyFromDetail = () => {
  currentFeedback.value = detailData.value
  replyForm.replyContent = ''
  detailDialogVisible.value = false
  replyDialogVisible.value = true
}

// 提交回复
const handleSubmitReply = async () => {
  if (!replyFormRef.value) return
  await replyFormRef.value.validate(async (valid) => {
    if (valid) {
      replyLoading.value = true
      try {
        const res = await feedbackApi.replyFeedback(
          currentFeedback.value.feedbackId,
          replyForm.replyContent,
          1 // 教师回复
        )
        if (res.code === 200) {
          ElMessage.success('回复成功')
          replyDialogVisible.value = false
          loadData()
          // 如果是从详情对话框打开的，重新加载详情
          if (detailDialogVisible.value === false && currentFeedback.value.feedbackId === detailData.value.feedbackId) {
            handleView(currentFeedback.value)
          }
        }
      } catch (error) {
        console.error('回复失败:', error)
        ElMessage.error(error.response?.data?.message || '回复失败')
      } finally {
        replyLoading.value = false
      }
    }
  })
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
    0: 'warning',  // 待处理 - 橙色
    1: 'primary',  // 处理中 - 蓝色
    2: 'success',  // 已解决 - 绿色
    3: 'info'      // 已关闭 - 灰色
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

