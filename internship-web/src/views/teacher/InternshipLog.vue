<template>
  <PageLayout title="实习日志批阅">
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
        <el-form-item label="日志日期">
          <el-date-picker
            v-model="searchForm.logDate"
            type="date"
            placeholder="请选择日期"
            clearable
            style="width: 200px"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="批阅状态">
          <el-select
            v-model="searchForm.reviewStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="未批阅" :value="0" />
            <el-option label="已批阅" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日志列表 -->
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
      <el-table-column prop="logDate" label="日志日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.logDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column label="工作内容" min-width="300" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="table-content-preview">{{ getContentPreview(row.logContent) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="workHours" label="工作时长" width="100" align="center">
        <template #default="{ row }">
          {{ row.workHours ? `${row.workHours}小时` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="批阅状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.reviewStatus === 1 ? 'success' : 'warning'" size="small">
            {{ row.reviewStatus === 1 ? '已批阅' : '未批阅' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reviewScore" label="评分" width="80" align="center">
        <template #default="{ row }">
          <span v-if="row.reviewScore !== null && row.reviewScore !== undefined">
            {{ row.reviewScore }}分
          </span>
          <span v-else style="color: #909399">-</span>
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
            v-if="row.reviewStatus === 0 && canReview(row)"
            link
            type="success"
            size="small"
            @click="handleReview(row)"
          >
            批阅
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
      title="日志详情"
      width="900px"
    >
      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="日志日期">
          {{ formatDate(detailData.logDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作时长">
          {{ detailData.workHours ? `${detailData.workHours}小时` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="批阅状态">
          <el-tag :type="detailData.reviewStatus === 1 ? 'success' : 'warning'" size="small">
            {{ detailData.reviewStatus === 1 ? '已批阅' : '未批阅' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.reviewScore !== null && detailData.reviewScore !== undefined" label="评分">
          {{ detailData.reviewScore }}分
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 日志内容 -->
      <div class="content-section">
        <div class="content-title">日志内容</div>
        <div 
          v-html="getMergedLogContent(detailData)" 
          class="rich-content log-content"
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

      <!-- 批阅信息 -->
      <el-descriptions v-if="detailData.reviewComment || detailData.reviewTime || detailData.reviewerName" :column="2" border class="detail-info">
        <el-descriptions-item v-if="detailData.reviewComment" label="批阅意见" :span="2">
          <div style="white-space: pre-wrap; color: #606266">{{ detailData.reviewComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.reviewTime" label="批阅时间">
          {{ formatDateTime(detailData.reviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.reviewerName" label="批阅人">
          {{ detailData.reviewerName }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.reviewStatus === 0 && canReview(detailData)"
          type="primary"
          @click="handleReviewFromDetail"
        >
          批阅
        </el-button>
      </template>
    </el-dialog>

    <!-- 批阅对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      title="批阅日志"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewFormRules"
        label-width="100px"
      >
        <el-form-item label="日志信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentLog.studentName }}（{{ currentLog.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>日期：</strong>{{ formatDate(currentLog.logDate) }}</div>
            <div style="margin-top: 5px"><strong>企业：</strong>{{ currentLog.enterpriseName || '-' }}</div>
          </div>
        </el-form-item>
        <el-form-item label="评分" prop="reviewScore">
          <el-input-number
            v-model="reviewForm.reviewScore"
            :min="0"
            :max="100"
            :precision="0"
            style="width: 100%"
            placeholder="请输入评分（0-100分）"
          />
        </el-form-item>
        <el-form-item label="批阅意见" prop="reviewComment">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="6"
            placeholder="请输入批阅意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewLoading" @click="handleSubmitReview">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document } from '@element-plus/icons-vue'
import { logApi } from '@/api/internship/log'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'

const loading = ref(false)
const reviewLoading = ref(false)
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const reviewFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  logDate: null,
  reviewStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentLog = ref({})

const reviewForm = reactive({
  reviewScore: null,
  reviewComment: ''
})

const reviewFormRules = {
  reviewScore: [
    { required: true, message: '请输入评分', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '评分范围为0-100分', trigger: 'blur' }
  ],
  reviewComment: [{ required: true, message: '请输入批阅意见', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await logApi.getLogPage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      logDate: searchForm.logDate || undefined,
      reviewStatus: searchForm.reviewStatus !== null ? searchForm.reviewStatus : undefined
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
  searchForm.logDate = null
  searchForm.reviewStatus = null
  handleSearch()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await logApi.getLogById(row.logId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 批阅
const handleReview = (row) => {
  currentLog.value = row
  reviewForm.reviewScore = null
  reviewForm.reviewComment = ''
  reviewDialogVisible.value = true
}

// 从详情对话框批阅
const handleReviewFromDetail = () => {
  currentLog.value = detailData.value
  reviewForm.reviewScore = null
  reviewForm.reviewComment = ''
  detailDialogVisible.value = false
  reviewDialogVisible.value = true
}

// 提交批阅
const handleSubmitReview = async () => {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      reviewLoading.value = true
      try {
        const res = await logApi.reviewLog(
          currentLog.value.logId,
          reviewForm.reviewComment,
          reviewForm.reviewScore
        )
        if (res.code === 200) {
          ElMessage.success('批阅成功')
          reviewDialogVisible.value = false
          loadData()
          // 如果是从详情对话框打开的，重新加载详情
          if (detailDialogVisible.value === false && currentLog.value.logId === detailData.value.logId) {
            handleView(currentLog.value)
          }
        }
      } catch (error) {
        console.error('批阅失败:', error)
        ElMessage.error(error.response?.data?.message || '批阅失败')
      } finally {
        reviewLoading.value = false
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

// 判断是否可以批阅日志
const canReview = (row) => {
  const authStore = useAuthStore()
  const roles = authStore.roles || []
  
  // 系统管理员和学校管理员可以批阅所有
  if (roles.includes('ROLE_SYSTEM_ADMIN') || roles.includes('ROLE_SCHOOL_ADMIN')) {
    return true
  }
  
  // 根据申请类型判断
  if (row.applyType === 1) {
    // 合作企业实习：企业导师可以批阅
    return roles.includes('ROLE_ENTERPRISE_MENTOR') || roles.includes('ROLE_ENTERPRISE_ADMIN')
  } else if (row.applyType === 2) {
    // 自主实习：班主任可以批阅
    return roles.includes('ROLE_CLASS_TEACHER')
  }
  
  return false
}

// 获取内容预览（用于表格显示，去除HTML标签，只显示纯文本）
const getContentPreview = (htmlContent) => {
  if (!htmlContent) return '-'
  // 创建一个临时div来解析HTML
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = htmlContent
  // 获取纯文本内容
  let text = tempDiv.textContent || tempDiv.innerText || ''
  // 去除多余的空白字符
  text = text.replace(/\s+/g, ' ').trim()
  // 限制长度，最多显示100个字符
  if (text.length > 100) {
    text = text.substring(0, 100) + '...'
  }
  return text
}

// 合并日志内容用于显示
const getMergedLogContent = (data) => {
  if (!data) return '-'
  // 直接使用logContent
  return data.logContent || '-'
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

.detail-info {
  margin-bottom: 20px;
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

.log-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.log-content :deep(h3:first-child) {
  margin-top: 0;
}

.log-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

.log-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.log-content :deep(ul),
.log-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.log-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.log-content :deep(blockquote) {
  margin: 12px 0;
  padding: 12px 16px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.log-content :deep(code) {
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.log-content :deep(pre) {
  margin: 12px 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  overflow-x: auto;
}

.log-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

.table-content-preview {
  color: #606266;
  line-height: 1.5;
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
</style>

