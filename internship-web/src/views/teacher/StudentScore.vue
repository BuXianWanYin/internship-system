<template>
  <PageLayout title="学生成绩">
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
        <el-form-item label="企业名称">
          <el-input
            v-model="searchForm.enterpriseName"
            placeholder="请输入企业名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="成绩等级">
          <el-select
            v-model="searchForm.gradeLevel"
            placeholder="请选择等级"
            clearable
            style="width: 150px"
          >
            <el-option label="优秀" value="优秀" />
            <el-option label="良好" value="良好" />
            <el-option label="中等" value="中等" />
            <el-option label="及格" value="及格" />
            <el-option label="不及格" value="不及格" />
          </el-select>
        </el-form-item>
        <el-form-item label="实习类型">
          <el-select
            v-model="searchForm.applyType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="合作企业" :value="1" />
            <el-option label="自主实习" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 成绩列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="enterpriseName" label="企业" min-width="200" show-overflow-tooltip />
      <el-table-column label="实习时间" width="200">
        <template #default="{ row }">
          {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column label="企业评分" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.enterpriseScore !== null && row.enterpriseScore !== undefined" style="font-weight: 500;">
            {{ row.enterpriseScore }}分
          </span>
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="班主任评分" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.schoolScore !== null && row.schoolScore !== undefined" style="font-weight: 500;">
            {{ row.schoolScore }}分
          </span>
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="学生自评" width="120" align="center">
        <template #default="{ row }">
          <span v-if="row.selfScore !== null && row.selfScore !== undefined" style="font-weight: 500;">
            {{ row.selfScore }}分
          </span>
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="综合成绩" width="140" align="center">
        <template #default="{ row }">
          <div v-if="row.comprehensiveScore !== null && row.comprehensiveScore !== undefined">
            <div style="font-weight: bold; color: #409eff; font-size: 14px;">
              {{ row.comprehensiveScore }}分
            </div>
            <el-tag 
              v-if="row.gradeLevel"
              :type="getGradeTagType(row.gradeLevel)" 
              size="small"
              style="margin-top: 4px;"
            >
              {{ row.gradeLevel }}
            </el-tag>
            <span v-else style="color: #909399; font-size: 12px;">未评级</span>
          </div>
          <span v-else style="color: #909399;">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            link
            type="primary"
            size="small"
            @click="handleViewDetail(row)"
          >
            查看详情
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

    <!-- 成绩详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="成绩详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <StudentScoreDetail
        v-if="currentApplyId"
        :apply-id="currentApplyId"
      />
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import StudentScoreDetail from '@/components/business/StudentScoreDetail.vue'
import { comprehensiveScoreApi } from '@/api/evaluation/comprehensiveScore'
import { formatDate } from '@/utils/dateUtils'

const loading = ref(false)
const detailDialogVisible = ref(false)
const currentApplyId = ref(null)

const searchForm = reactive({
  studentName: '',
  enterpriseName: '',
  gradeLevel: '',
  applyType: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await comprehensiveScoreApi.getScorePage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined
    })
    
    if (res.code === 200 && res.data) {
      let records = res.data.records.map(item => ({
        ...item,
        startDate: item.internshipStartDate || item.startDate,
        endDate: item.internshipEndDate || item.endDate
      }))
      
      // 前端筛选（后端暂不支持这些参数）
      if (searchForm.enterpriseName) {
        records = records.filter(item => 
          item.enterpriseName && item.enterpriseName.includes(searchForm.enterpriseName)
        )
      }
      if (searchForm.gradeLevel) {
        records = records.filter(item => item.gradeLevel === searchForm.gradeLevel)
      }
      if (searchForm.applyType !== null && searchForm.applyType !== undefined) {
        records = records.filter(item => item.applyType === searchForm.applyType)
      }
      
      tableData.value = records
      pagination.total = records.length // 注意：前端筛选后总数可能不准确
    } else {
      ElMessage.error(res.message || '加载失败')
      tableData.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
    tableData.value = []
    pagination.total = 0
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
  searchForm.enterpriseName = ''
  searchForm.gradeLevel = ''
  searchForm.applyType = null
  handleSearch()
}

// 查看详情
const handleViewDetail = (row) => {
  currentApplyId.value = row.applyId
  detailDialogVisible.value = true
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadData()
}

// 获取等级标签类型
const getGradeTagType = (gradeLevel) => {
  if (!gradeLevel) return ''
  const typeMap = {
    '优秀': 'success',
    '良好': '',
    '中等': 'warning',
    '及格': 'info',
    '不及格': 'danger'
  }
  return typeMap[gradeLevel] || ''
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

