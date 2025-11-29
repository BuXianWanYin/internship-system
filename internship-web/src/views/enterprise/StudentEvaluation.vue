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
          {{ formatDate(row.internshipStartDate || row.startDate) }} 至 {{ formatDate(row.internshipEndDate || row.endDate) }}
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
          <el-button
            v-if="canMarkAsCompleted(row)"
            link
            type="success"
            size="small"
            @click="handleMarkAsCompleted(row)"
          >
            结束实习
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

    <!-- 结束实习对话框 -->
    <el-dialog
      v-model="completeDialogVisible"
      title="结束实习"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="completeFormRef"
        :model="completeForm"
        :rules="completeFormRules"
        label-width="120px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentCompleteApply.studentName }}（{{ currentCompleteApply.studentNo }}）</div>
            <div style="margin-top: 5px">
              <strong>企业：</strong>
              {{ currentCompleteApply.enterpriseName || '-' }}
            </div>
            <div style="margin-top: 5px">
              <strong>岗位：</strong>
              {{ currentCompleteApply.postName || '-' }}
            </div>
          </div>
        </el-form-item>
        <el-form-item label="实习结束日期" prop="endDate">
          <el-date-picker
            v-model="completeForm.endDate"
            type="date"
            placeholder="请选择结束日期（不选则使用今天）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="completeForm.remark"
            type="textarea"
            :rows="4"
            placeholder="请输入备注（可选）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        >
          <template #default>
            <div>结束实习后，学生的实习状态将更新为"已结束"，可以进行评价。</div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="completeLoading" @click="handleSubmitComplete">确定</el-button>
      </template>
    </el-dialog>

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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import EnterpriseEvaluationForm from '@/components/business/EnterpriseEvaluationForm.vue'
import { enterpriseEvaluationApi } from '@/api/evaluation/enterprise'
import { applyApi } from '@/api/internship/apply'
import { formatDate } from '@/utils/dateUtils'
import { hasAnyRole } from '@/utils/permission'
import { isInternshipCompleted, isUnbound, getUnbindStatusText, getUnbindStatusType } from '@/utils/statusUtils'

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

// 结束实习相关
const completeDialogVisible = ref(false)
const completeFormRef = ref(null)
const currentCompleteApply = ref({})
const completeLoading = ref(false)

const completeForm = reactive({
  endDate: null,
  remark: ''
})

const completeFormRules = {
  endDate: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          const endDate = new Date(value)
          const startDate = currentCompleteApply.value.internshipStartDate || currentCompleteApply.value.startDate
          if (startDate && endDate < new Date(startDate)) {
            callback(new Error('实习结束日期不能早于开始日期'))
          } else {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

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
      const records = res.data.records || []
      // 确保每条记录都有实习时间字段
      tableData.value = records.map(item => ({
        ...item,
        startDate: item.internshipStartDate || item.startDate,
        endDate: item.internshipEndDate || item.endDate
      }))
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
        enterpriseId: item.enterpriseId, // 确保包含企业ID
        enterpriseName: item.enterpriseName,
        startDate: item.internshipStartDate || item.startDate,
        endDate: item.internshipEndDate || item.endDate,
        evaluationStatus: null
      }))
      
      // 过滤已有评价的学生
      for (const student of students) {
        const evalRes = await enterpriseEvaluationApi.getEvaluationByApplyId(student.applyId)
        if (evalRes.code === 200 && evalRes.data) {
          student.evaluationStatus = evalRes.data.evaluationStatus
          student.evaluationId = evalRes.data.evaluationId
          // 如果评价中有企业ID，使用评价中的企业ID
          if (evalRes.data.enterpriseId && !student.enterpriseId) {
            student.enterpriseId = evalRes.data.enterpriseId
          }
        }
        // 如果仍然没有企业ID，从申请详情中获取
        if (!student.enterpriseId && student.applyId) {
          try {
            const applyRes = await applyApi.getApplyById(student.applyId)
            if (applyRes.code === 200 && applyRes.data && applyRes.data.enterpriseId) {
              student.enterpriseId = applyRes.data.enterpriseId
            }
          } catch (error) {
            console.error('获取申请详情失败:', error)
          }
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
  // 确保学生对象包含企业ID
  let studentData = { ...row }
  
  // 如果没有企业ID，从申请详情中获取
  if (!studentData.enterpriseId && studentData.applyId) {
    try {
      const applyRes = await applyApi.getApplyById(studentData.applyId)
      if (applyRes.code === 200 && applyRes.data && applyRes.data.enterpriseId) {
        studentData.enterpriseId = applyRes.data.enterpriseId
      }
    } catch (error) {
      console.error('获取申请详情失败:', error)
    }
  }
  
  currentStudent.value = studentData
  evaluationDialogTitle.value = `企业评价 - ${row.studentName}（学号：${row.studentNo}）`
  
  // 加载已有评价（草稿）
  const res = await enterpriseEvaluationApi.getEvaluationByApplyId(row.applyId)
  if (res.code === 200 && res.data) {
    currentEvaluation.value = res.data
    // 如果评价中有企业ID，也更新到学生对象中
    if (res.data.enterpriseId && !studentData.enterpriseId) {
      studentData.enterpriseId = res.data.enterpriseId
      currentStudent.value = studentData
    }
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

// 判断是否可以标记为结束
const canMarkAsCompleted = (row) => {
  if (!row) return false
  // 不能标记已删除的
  if (row.deleteFlag === 1) return false
  // 不能标记已解绑的
  if (row.unbindStatus === 2) return false
  // 不能标记已结束的
  if (isInternshipCompleted(row)) return false
  // 只有合作企业的实习才能标记（企业评价页面只显示合作企业）
  if (row.applyType !== 1) return false
  // 只有已录用状态才能标记
  if (row.status !== 3) return false
  return true
}

// 处理结束实习
const handleMarkAsCompleted = (row) => {
  currentCompleteApply.value = { ...row }
  completeForm.endDate = null
  completeForm.remark = ''
  completeDialogVisible.value = true
}

// 提交结束实习
const handleSubmitComplete = async () => {
  if (!completeFormRef.value) return
  
  try {
    await completeFormRef.value.validate()
    
    completeLoading.value = true
    try {
      const res = await applyApi.completeInternship(
        currentCompleteApply.value.applyId,
        completeForm.endDate,
        completeForm.remark
      )
      if (res.code === 200) {
        ElMessage.success('结束实习成功')
        completeDialogVisible.value = false
        // 刷新列表
        await loadData()
      }
    } catch (error) {
      console.error('结束实习失败:', error)
      ElMessage.error(error.response?.data?.message || '结束实习失败')
    } finally {
      completeLoading.value = false
    }
  } catch (error) {
    // 表单验证失败
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

