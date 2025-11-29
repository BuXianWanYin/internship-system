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
        <el-form-item label="评价状态">
          <el-select
            v-model="searchForm.evaluationStatus"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="待评价" :value="null" />
            <el-option label="草稿" :value="0" />
            <el-option label="已提交" :value="1" />
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
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="100" />
      <el-table-column prop="enterpriseName" label="企业" min-width="200" show-overflow-tooltip />
      <el-table-column label="实习时间" width="200">
        <template #default="{ row }">
          {{ formatDate(row.startDate) }} 至 {{ formatDate(row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="evaluationStatus" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.evaluationStatus === 1" type="success" size="small">已评价</el-tag>
          <el-tag v-else type="warning" size="small">待评价</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button
            v-if="row.evaluationStatus !== 1"
            link
            type="primary"
            size="small"
            @click="handleEvaluate(row)"
          >
            评价
          </el-button>
          <el-button
            v-else
            link
            type="primary"
            size="small"
            @click="handleView(row)"
          >
            查看
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

    <!-- 评价对话框 -->
    <el-dialog
      v-model="evaluationDialogVisible"
      :title="evaluationDialogTitle"
      width="900px"
      :close-on-click-modal="false"
    >
      <EnterpriseEvaluationForm
        v-if="currentStudent"
        :student="currentStudent"
        :evaluation="currentEvaluation"
        @save="handleSaveEvaluation"
        @submit="handleSubmitEvaluation"
      />
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import EnterpriseEvaluationForm from '@/components/business/EnterpriseEvaluationForm.vue'
import { enterpriseEvaluationApi } from '@/api/evaluation/enterprise'
import { applyApi } from '@/api/internship/apply'
import { formatDate } from '@/utils/dateUtils'
import { hasAnyRole } from '@/utils/permission'
import { computed } from 'vue'

// 根据角色动态显示页面标题
const pageTitle = computed(() => {
  if (hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])) {
    return '评价管理'
  } else if (hasAnyRole(['ROLE_ENTERPRISE_MENTOR'])) {
    return '学生评价'
  }
  return '学生评价'
})

const loading = ref(false)
const evaluationDialogVisible = ref(false)
const evaluationDialogTitle = ref('企业评价')
const currentStudent = ref(null)
const currentEvaluation = ref(null)

const searchForm = reactive({
  studentName: '',
  evaluationStatus: null
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
    const params = {
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      evaluationStatus: searchForm.evaluationStatus !== null ? searchForm.evaluationStatus : undefined
    }
    
    const res = await enterpriseEvaluationApi.getEvaluationPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 如果没有评价记录，从申请列表加载
      if (tableData.value.length === 0 && searchForm.evaluationStatus === null) {
        await loadStudentList()
      }
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载学生列表（实习结束但未评价的学生）
const loadStudentList = async () => {
  try {
    const res = await applyApi.getEnterpriseStudents({
      current: pagination.current,
      size: pagination.size,
      status: 7 // 实习结束
    })
    if (res.code === 200 && res.data && res.data.records) {
      const students = res.data.records.map(item => ({
        applyId: item.applyId,
        studentId: item.studentId,
        studentNo: item.studentNo,
        studentName: item.studentName,
        enterpriseName: item.enterpriseName,
        startDate: item.internshipStartDate,
        endDate: item.internshipEndDate,
        evaluationStatus: null
      }))
      
      // 过滤已有评价的学生
      for (const student of students) {
        const evalRes = await enterpriseEvaluationApi.getEvaluationByApplyId(student.applyId)
        if (evalRes.code === 200 && evalRes.data) {
          student.evaluationStatus = evalRes.data.evaluationStatus
          student.evaluationId = evalRes.data.evaluationId
        }
      }
      
      tableData.value = students
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
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
  searchForm.evaluationStatus = null
  handleSearch()
}

// 评价
const handleEvaluate = async (row) => {
  currentStudent.value = row
  evaluationDialogTitle.value = `企业评价 - ${row.studentName}（学号：${row.studentNo}）`
  
  // 加载已有评价（草稿）
  const res = await enterpriseEvaluationApi.getEvaluationByApplyId(row.applyId)
  if (res.code === 200 && res.data) {
    currentEvaluation.value = res.data
  } else {
    currentEvaluation.value = null
  }
  
  evaluationDialogVisible.value = true
}

// 查看
const handleView = async (row) => {
  currentStudent.value = row
  evaluationDialogTitle.value = `查看评价 - ${row.studentName}（学号：${row.studentNo}）`
  
  const res = await enterpriseEvaluationApi.getEvaluationByApplyId(row.applyId)
  if (res.code === 200 && res.data) {
    currentEvaluation.value = res.data
    evaluationDialogVisible.value = true
  } else {
    ElMessage.error('加载评价失败')
  }
}

// 保存评价
const handleSaveEvaluation = async (data) => {
  try {
    const res = await enterpriseEvaluationApi.saveOrUpdateEvaluation(data)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      currentEvaluation.value = res.data
      await loadData()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

// 提交评价
const handleSubmitEvaluation = async (evaluationId) => {
  try {
    const res = await enterpriseEvaluationApi.submitEvaluation(evaluationId)
    if (res.code === 200) {
      ElMessage.success('提交成功')
      evaluationDialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (error) {
    ElMessage.error('提交失败：' + (error.message || '未知错误'))
  }
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

