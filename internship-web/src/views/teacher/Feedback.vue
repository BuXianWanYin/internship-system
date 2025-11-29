<template>
  <PageLayout :title="pageTitle">
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
            v-if="(row.feedbackStatus === 0 || row.feedbackStatus === 1) && canReview(row)"
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
      </el-descriptions>

      <!-- 反馈内容 -->
      <div class="content-section">
        <div class="content-title">反馈内容</div>
        <div 
          v-html="detailData.feedbackContent || '-'" 
          class="rich-content feedback-content"
        ></div>
      </div>

      <!-- 附件 -->
      <div v-if="detailData.attachmentUrls" class="content-section">
        <div class="content-title">附件</div>
        <div class="attachment-list">
          <div v-for="(url, index) in (detailData.attachmentUrls || '').split(',').filter(u => u)" :key="index" class="attachment-item">
            <el-link type="primary" :icon="Document" @click="handleDownloadFile(url)">
              {{ getFileName(url) }}
            </el-link>
          </div>
        </div>
      </div>

      <!-- 回复信息 -->
      <el-descriptions v-if="detailData.replyContent || detailData.replyTime || detailData.replyUserName" :column="2" border class="detail-info">
        <el-descriptions-item v-if="detailData.replyContent" label="回复内容" :span="2">
          <div 
            v-html="detailData.replyContent" 
            class="rich-content reply-content"
            style="color: #606266; background: #f5f7fa; padding: 10px; border-radius: 4px"
          ></div>
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
          v-if="(detailData.feedbackStatus === 0 || detailData.feedbackStatus === 1) && canReview(detailData)"
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
import { Search, Refresh, Document } from '@element-plus/icons-vue'
import { feedbackApi } from '@/api/internship/feedback'
import { fileApi } from '@/api/common/file'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'
import { computed } from 'vue'

// 根据角色动态显示页面标题
const pageTitle = computed(() => {
  if (hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])) {
    return '反馈管理'
  } else if (hasAnyRole(['ROLE_ENTERPRISE_MENTOR'])) {
    return '反馈处理'
  }
  return '问题反馈处理'
})

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

// 判断是否可以回复反馈
const canReview = (row) => {
  const authStore = useAuthStore()
  const roles = authStore.roles || []
  
  // 系统管理员和学校管理员可以回复所有
  if (roles.includes('ROLE_SYSTEM_ADMIN') || roles.includes('ROLE_SCHOOL_ADMIN')) {
    return true
  }
  
  // 根据申请类型判断
  if (row.applyType === 1) {
    // 合作企业实习：企业导师可以回复
    return roles.includes('ROLE_ENTERPRISE_MENTOR') || roles.includes('ROLE_ENTERPRISE_ADMIN')
  } else if (row.applyType === 2) {
    // 自主实习：班主任可以回复
    return roles.includes('ROLE_CLASS_TEACHER')
  }
  
  return false
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

// 获取文件名
const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

// 下载文件
const handleDownloadFile = async (filePath) => {
  if (!filePath) {
    ElMessage.warning('文件路径为空')
    return
  }
  try {
    await fileApi.downloadFile(filePath)
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败: ' + (error.message || '未知错误'))
  }
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

.content-section {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.content-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.rich-content {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  min-height: 100px;
  line-height: 1.8;
  color: #606266;
  word-wrap: break-word;
  word-break: break-all;
}

.feedback-content :deep(p),
.reply-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

.feedback-content :deep(img),
.reply-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.feedback-content :deep(table),
.reply-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.feedback-content :deep(table td),
.feedback-content :deep(table th),
.reply-content :deep(table td),
.reply-content :deep(table th) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.feedback-content :deep(table th),
.reply-content :deep(table th) {
  background-color: #f5f7fa;
  font-weight: bold;
}

.feedback-content :deep(ul),
.feedback-content :deep(ol),
.reply-content :deep(ul),
.reply-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.feedback-content :deep(li),
.reply-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.feedback-content :deep(h1),
.feedback-content :deep(h2),
.feedback-content :deep(h3),
.feedback-content :deep(h4),
.feedback-content :deep(h5),
.feedback-content :deep(h6),
.reply-content :deep(h1),
.reply-content :deep(h2),
.reply-content :deep(h3),
.reply-content :deep(h4),
.reply-content :deep(h5),
.reply-content :deep(h6) {
  margin: 16px 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
}

.feedback-content :deep(blockquote),
.reply-content :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 16px;
  border-left: 4px solid #409eff;
  background-color: #f5f7fa;
  color: #606266;
}

.feedback-content :deep(code),
.reply-content :deep(code) {
  padding: 2px 6px;
  background-color: #f5f7fa;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.feedback-content {
  min-height: 50px;
}

.reply-content {
  min-height: 50px;
}

.attachment-list {
  margin-top: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.detail-info {
  margin-top: 20px;
}
</style>

