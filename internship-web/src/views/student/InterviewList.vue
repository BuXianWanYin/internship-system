<template>
  <PageLayout title="我的面试">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
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
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="interviewTime" label="面试时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.interviewTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="interviewLocation" label="面试地点" min-width="200" show-overflow-tooltip />
      <el-table-column label="面试方式" width="120" align="center">
        <template #default="{ row }">
          {{ getInterviewTypeText(row.interviewType) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row)" size="small">
            {{ getStatusText(row) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="studentConfirm" label="我的确认" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.studentConfirm === 1" type="success" size="small">已确认</el-tag>
          <el-tag v-else-if="row.studentConfirm === 2" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="warning" size="small">待确认</el-tag>
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
      <el-table-column label="操作" width="300" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0 && !row.studentConfirm"
            link
            type="success"
            size="small"
            @click="handleConfirm(row, 1)"
          >
            确认
          </el-button>
          <el-button
            v-if="row.status === 0 && !row.studentConfirm"
            link
            type="danger"
            size="small"
            @click="handleConfirm(row, 2)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.canConfirmOnboard"
            link
            type="warning"
            size="small"
            :loading="confirmOnboardLoading === row.interviewId"
            @click="handleConfirmOnboard(row)"
          >
            确认上岗
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
      title="面试详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="企业名称">{{ detailData.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称">{{ detailData.postName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData)" size="small">
            {{ getStatusText(detailData) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="我的确认">
          <el-tag v-if="detailData.studentConfirm === 1" type="success" size="small">已确认</el-tag>
          <el-tag v-else-if="detailData.studentConfirm === 2" type="danger" size="small">已拒绝</el-tag>
          <el-tag v-else type="warning" size="small">待确认</el-tag>
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
        <el-descriptions-item label="联系人">{{ detailData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="面试说明" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.interviewDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="面试结果">
          <el-tag v-if="detailData.interviewResult === 1" type="success" size="small">通过</el-tag>
          <el-tag v-else-if="detailData.interviewResult === 2" type="danger" size="small">不通过</el-tag>
          <el-tag v-else-if="detailData.interviewResult === 3" type="warning" size="small">待定</el-tag>
          <span v-else>-</span>
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
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.status === 0 && !detailData.studentConfirm"
          type="success"
          @click="handleConfirmFromDetail(1)"
        >
          确认面试
        </el-button>
        <el-button
          v-if="detailData.status === 0 && !detailData.studentConfirm"
          type="danger"
          @click="handleConfirmFromDetail(2)"
        >
          拒绝面试
        </el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { interviewApi } from '@/api/internship/interview'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const confirmLoading = ref(false)
const confirmOnboardLoading = ref(null)
const detailDialogVisible = ref(false)

const searchForm = reactive({
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

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await interviewApi.getInterviewPage({
      current: pagination.current,
      size: pagination.size,
      postName: searchForm.postName || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 为每个面试记录查询申请状态，判断是否可以确认上岗
      for (const row of tableData.value) {
        if (row.applyId && row.interviewResult === 1) {
          try {
            const applyRes = await applyApi.getApplyById(row.applyId)
            if (applyRes.code === 200 && applyRes.data) {
              // 如果申请状态为已录用（status = 3）且学生确认状态为未确认（studentConfirmStatus = 0或null）
              if (applyRes.data.status === 3 && (applyRes.data.studentConfirmStatus === 0 || applyRes.data.studentConfirmStatus === null)) {
                row.canConfirmOnboard = true
                row.studentConfirmStatus = applyRes.data.studentConfirmStatus
              }
            }
          } catch (error) {
            console.error('查询申请状态失败:', error)
          }
        }
      }
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
  searchForm.postName = ''
  searchForm.status = null
  handleSearch()
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

// 确认面试
const handleConfirm = async (row, confirm) => {
  const action = confirm === 1 ? '确认' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该面试吗？`, '提示', {
      type: confirm === 1 ? 'info' : 'warning'
    })
    confirmLoading.value = true
    try {
      const res = await interviewApi.confirmInterview(row.interviewId, confirm)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        loadData()
      }
    } catch (error) {
      console.error(`${action}失败:`, error)
      ElMessage.error(error.response?.data?.message || `${action}失败`)
    } finally {
      confirmLoading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 从详情对话框确认
const handleConfirmFromDetail = async (confirm) => {
  const action = confirm === 1 ? '确认' : '拒绝'
  try {
    await ElMessageBox.confirm(`确定要${action}该面试吗？`, '提示', {
      type: confirm === 1 ? 'info' : 'warning'
    })
    confirmLoading.value = true
    try {
      const res = await interviewApi.confirmInterview(detailData.value.interviewId, confirm)
      if (res.code === 200) {
        ElMessage.success(`${action}成功`)
        detailDialogVisible.value = false
        loadData()
      }
    } catch (error) {
      console.error(`${action}失败:`, error)
      ElMessage.error(error.response?.data?.message || `${action}失败`)
    } finally {
      confirmLoading.value = false
    }
  } catch (error) {
    // 用户取消
  }
}

// 确认上岗
const handleConfirmOnboard = async (row) => {
  if (!row.applyId) {
    ElMessage.error('申请ID不存在，无法确认上岗')
    return
  }
  
  try {
    await ElMessageBox.confirm('确认上岗后，您将开始在该企业实习，可以开始考勤。确定要确认上岗吗？', '确认上岗', {
      type: 'warning'
    })
    confirmOnboardLoading.value = row.interviewId
    try {
      const res = await applyApi.confirmOnboard(row.applyId)
      if (res.code === 200) {
        ElMessage.success('确认上岗成功，可以开始实习了！')
        loadData()
      }
    } catch (error) {
      console.error('确认上岗失败:', error)
      ElMessage.error(error.response?.data?.message || '确认上岗失败')
    } finally {
      confirmOnboardLoading.value = null
    }
  } catch (error) {
    // 用户取消
  }
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 获取状态文本（综合考虑status、studentConfirm和interviewResult）
const getStatusText = (row) => {
  // 如果面试已取消
  if (row.status === 3) {
    return '已取消'
  }
  
  // 如果学生拒绝了面试
  if (row.studentConfirm === 2) {
    return '已拒绝'
  }
  
  // 如果面试已完成且有结果
  if (row.status === 2) {
    if (row.interviewResult === 1) {
      return '面试通过'
    } else if (row.interviewResult === 2) {
      return '面试未通过'
    } else if (row.interviewResult === 3) {
      return '面试待定'
    } else {
      return '已完成'
    }
  }
  
  // 如果学生已确认
  if (row.studentConfirm === 1) {
    return '已确认'
  }
  
  // 默认状态
  if (row.status === 0) {
    return '待确认'
  } else if (row.status === 1) {
    return '已确认'
  }
  
  return '未知'
}

// 获取状态类型（综合考虑status、studentConfirm和interviewResult）
const getStatusType = (row) => {
  // 如果面试已取消
  if (row.status === 3) {
    return 'info'
  }
  
  // 如果学生拒绝了面试
  if (row.studentConfirm === 2) {
    return 'danger'
  }
  
  // 如果面试已完成且有结果
  if (row.status === 2) {
    if (row.interviewResult === 1) {
      return 'success' // 面试通过
    } else if (row.interviewResult === 2) {
      return 'danger' // 面试未通过
    } else if (row.interviewResult === 3) {
      return 'warning' // 面试待定
    } else {
      return 'info' // 已完成但无结果
    }
  }
  
  // 如果学生已确认
  if (row.studentConfirm === 1) {
    return 'success'
  }
  
  // 默认状态
  if (row.status === 0) {
    return 'warning' // 待确认
  } else if (row.status === 1) {
    return 'success' // 已确认
  }
  
  return 'info'
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

