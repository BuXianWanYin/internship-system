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
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentNo"
            placeholder="请输入学号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="成果类型">
          <el-select
            v-model="searchForm.achievementType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="项目文档" value="项目文档" />
            <el-option label="项目成果" value="项目成果" />
            <el-option label="工作成果" value="工作成果" />
            <el-option label="技术文档" value="技术文档" />
            <el-option label="学习笔记" value="学习笔记" />
            <el-option label="作品展示" value="作品展示" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.reviewStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成果列表 -->
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
      <el-table-column prop="achievementTitle" label="成果标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="achievementType" label="成果类型" width="120" align="center">
        <template #default="{ row }">
          {{ getAchievementTypeText(row.achievementType) }}
        </template>
      </el-table-column>
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="reviewStatus" label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getReviewStatusType(row.reviewStatus)" size="small">
            {{ getReviewStatusText(row.reviewStatus) }}
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
            v-if="row.reviewStatus === 0 && canReview(row)"
            link
            type="success"
            size="small"
            @click="handleReview(row, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.reviewStatus === 0 && canReview(row)"
            link
            type="danger"
            size="small"
            @click="handleReview(row, 2)"
          >
            拒绝
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
      title="成果详情"
      width="900px"
    >
      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="成果标题">{{ detailData.achievementTitle }}</el-descriptions-item>
        <el-descriptions-item label="成果类型">
          {{ getAchievementTypeText(detailData.achievementType) }}
        </el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag :type="getReviewStatusType(detailData.reviewStatus)" size="small">
            {{ getReviewStatusText(detailData.reviewStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="提交时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 成果描述 -->
      <div class="content-section">
        <div class="content-title">成果描述</div>
        <div 
          v-html="detailData.achievementDescription || '-'" 
          class="rich-content achievement-content"
        ></div>
      </div>

      <!-- 附件 -->
      <div v-if="detailData.fileUrls" class="content-section">
        <div class="content-title">成果附件</div>
        <div class="attachment-list">
          <div v-for="(url, index) in (detailData.fileUrls || '').split(',').filter(u => u)" :key="index" class="attachment-item">
            <el-link type="primary" :icon="Document" @click="handleDownloadFile(url)">
              {{ getFileName(url) }}
            </el-link>
          </div>
        </div>
      </div>

      <!-- 审核信息 -->
      <el-descriptions v-if="detailData.reviewComment || detailData.reviewTime || detailData.reviewerName" :column="2" border class="detail-info">
        <el-descriptions-item v-if="detailData.reviewComment" label="审核意见" :span="2">
          <div style="white-space: pre-wrap; color: #606266">{{ detailData.reviewComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.reviewTime" label="审核时间">
          {{ formatDateTime(detailData.reviewTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.reviewerName" label="审核人">
          {{ detailData.reviewerName }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.reviewStatus === 0 && canReview(detailData)"
          type="success"
          @click="handleReviewFromDetail(1)"
        >
          通过
        </el-button>
        <el-button
          v-if="detailData.reviewStatus === 0 && canReview(detailData)"
          type="danger"
          @click="handleReviewFromDetail(2)"
        >
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="reviewDialogVisible"
      :title="reviewDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="reviewFormRef"
        :model="reviewForm"
        :rules="reviewFormRules"
        label-width="100px"
      >
        <el-form-item label="成果信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentAchievement.studentName }}（{{ currentAchievement.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>成果：</strong>{{ currentAchievement.achievementTitle }}</div>
            <div style="margin-top: 5px"><strong>类型：</strong>{{ getAchievementTypeText(currentAchievement.achievementType) }}</div>
          </div>
        </el-form-item>
        <el-form-item label="审核意见" prop="reviewComment">
          <el-input
            v-model="reviewForm.reviewComment"
            type="textarea"
            :rows="6"
            :placeholder="reviewForm.reviewStatus === 1 ? '请输入审核意见' : '请输入拒绝原因（必填）'"
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
import { achievementApi } from '@/api/internship/achievement'
import { fileApi } from '@/api/common/file'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'
import { computed } from 'vue'

// 根据角色动态显示页面标题
const pageTitle = computed(() => {
  if (hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])) {
    return '成果查看'
  } else if (hasAnyRole(['ROLE_ENTERPRISE_MENTOR'])) {
    return '成果审核'
  }
  return '成果审核'
})

const loading = ref(false)
const reviewLoading = ref(false)
const detailDialogVisible = ref(false)
const reviewDialogVisible = ref(false)
const reviewDialogTitle = ref('审核成果')
const reviewFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  achievementType: '',
  reviewStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentAchievement = ref({})

const reviewForm = reactive({
  reviewStatus: 1,
  reviewComment: ''
})

const reviewFormRules = {
  reviewComment: [
    {
      validator: (rule, value, callback) => {
        if (reviewForm.reviewStatus === 2 && !value) {
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
    const res = await achievementApi.getAchievementPage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      achievementType: searchForm.achievementType || undefined,
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
  searchForm.achievementType = ''
  searchForm.reviewStatus = null
  handleSearch()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await achievementApi.getAchievementById(row.achievementId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 审核
const handleReview = (row, reviewStatus) => {
  currentAchievement.value = row
  reviewForm.reviewStatus = reviewStatus
  reviewForm.reviewComment = ''
  reviewDialogTitle.value = reviewStatus === 1 ? '通过审核' : '拒绝审核'
  reviewDialogVisible.value = true
}

// 从详情对话框审核
const handleReviewFromDetail = (reviewStatus) => {
  currentAchievement.value = detailData.value
  reviewForm.reviewStatus = reviewStatus
  reviewForm.reviewComment = ''
  reviewDialogTitle.value = reviewStatus === 1 ? '通过审核' : '拒绝审核'
  detailDialogVisible.value = false
  reviewDialogVisible.value = true
}

// 提交审核
const handleSubmitReview = async () => {
  if (!reviewFormRef.value) return
  await reviewFormRef.value.validate(async (valid) => {
    if (valid) {
      reviewLoading.value = true
      try {
        const res = await achievementApi.reviewAchievement(
          currentAchievement.value.achievementId,
          reviewForm.reviewStatus,
          reviewForm.reviewComment || undefined
        )
        if (res.code === 200) {
          ElMessage.success('审核成功')
          reviewDialogVisible.value = false
          loadData()
          // 如果是从详情对话框打开的，重新加载详情
          if (detailDialogVisible.value === false && currentAchievement.value.achievementId === detailData.value.achievementId) {
            handleView(currentAchievement.value)
          }
        }
      } catch (error) {
        console.error('审核失败:', error)
        ElMessage.error(error.response?.data?.message || '审核失败')
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

// 判断是否可以审核成果
const canReview = (row) => {
  const authStore = useAuthStore()
  const roles = authStore.roles || []
  
  // 系统管理员和学校管理员可以审核所有
  if (roles.includes('ROLE_SYSTEM_ADMIN') || roles.includes('ROLE_SCHOOL_ADMIN')) {
    return true
  }
  
  // 根据申请类型判断
  if (row.applyType === 1) {
    // 合作企业实习：企业导师可以审核
    return roles.includes('ROLE_ENTERPRISE_MENTOR') || roles.includes('ROLE_ENTERPRISE_ADMIN')
  } else if (row.applyType === 2) {
    // 自主实习：班主任可以审核
    return roles.includes('ROLE_CLASS_TEACHER')
  }
  
  return false
}

// 获取成果类型文本（直接返回字符串，如果为空则返回'-'）
const getAchievementTypeText = (type) => {
  return type || '-'
}

// 获取审核状态文本
const getReviewStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 获取审核状态类型
const getReviewStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
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

.achievement-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

.achievement-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.achievement-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.achievement-content :deep(table td),
.achievement-content :deep(table th) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.achievement-content :deep(table th) {
  background-color: #f5f7fa;
  font-weight: bold;
}

.achievement-content :deep(ul),
.achievement-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.achievement-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.achievement-content :deep(h1),
.achievement-content :deep(h2),
.achievement-content :deep(h3),
.achievement-content :deep(h4),
.achievement-content :deep(h5),
.achievement-content :deep(h6) {
  margin: 16px 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
}

.achievement-content :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 16px;
  border-left: 4px solid #409eff;
  background-color: #f5f7fa;
  color: #606266;
}

.achievement-content :deep(code) {
  padding: 2px 6px;
  background-color: #f5f7fa;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 0.9em;
}

.achievement-content :deep(pre) {
  margin: 16px 0;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
  overflow-x: auto;
}

.achievement-content :deep(pre code) {
  padding: 0;
  background-color: transparent;
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

