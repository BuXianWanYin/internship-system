<template>
  <PageLayout title="我指导的学生">
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
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
            <el-option label="已录用" :value="3" />
            <el-option label="已拒绝录用" :value="4" />
          </el-select>
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
      <el-table-column prop="enterpriseName" label="企业名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="在职状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.unbindStatus === 2" type="success" size="small">已解绑</el-tag>
          <el-tag v-else type="info" size="small">在职</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="分配时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
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
      title="学生详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || detailData.selfEnterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || detailData.selfPostName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="在职状态">
          <el-tag v-if="detailData.unbindStatus === 2" type="success" size="small">已解绑</el-tag>
          <el-tag v-else type="info" size="small">在职</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="实习开始日期">
          {{ detailData.internshipStartDate ? formatDate(detailData.internshipStartDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束日期">
          {{ detailData.internshipEndDate ? formatDate(detailData.internshipEndDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="分配时间">
          {{ formatDateTime(detailData.createTime) }}
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
      </el-descriptions>
    </el-dialog>

  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  studentName: '',
  studentNo: '',
  status: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const detailDialogVisible = ref(false)
const detailData = ref({})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已录用',
    4: '已拒绝录用'
  }
  return statusMap[status] || '-'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await applyApi.getMentorStudents({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '加载数据失败')
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error(error.response?.data?.message || '加载数据失败')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.status = null
  handleSearch()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.current = 1
  loadData()
}

// 页码改变
const handlePageChange = () => {
  loadData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data || {}
      detailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取详情失败')
    }
  } catch (error) {
    console.error('获取详情失败:', error)
    ElMessage.error(error.response?.data?.message || '获取详情失败')
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

