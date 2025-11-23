<template>
  <PageLayout title="成果审核">
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
            v-if="row.reviewStatus === 0"
            link
            type="success"
            size="small"
            @click="handleReview(row, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.reviewStatus === 0"
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
      <el-descriptions :column="2" border>
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
        <el-descriptions-item label="成果描述" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.achievementDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.achievementAttachment" label="成果附件" :span="2">
          <el-link :href="detailData.achievementAttachment" target="_blank" type="primary">
            {{ detailData.achievementAttachment }}
          </el-link>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.achievementLink" label="成果展示链接" :span="2">
          <el-link :href="detailData.achievementLink" target="_blank" type="primary">
            {{ detailData.achievementLink }}
          </el-link>
        </el-descriptions-item>
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
          v-if="detailData.reviewStatus === 0"
          type="success"
          @click="handleReviewFromDetail(1)"
        >
          通过
        </el-button>
        <el-button
          v-if="detailData.reviewStatus === 0"
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
            :placeholder="reviewForm.reviewStatus === 1 ? '请输入审核意见（可选）' : '请输入拒绝原因（必填）'"
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
import { Search, Refresh } from '@element-plus/icons-vue'
import { achievementApi } from '@/api/internship/achievement'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

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

