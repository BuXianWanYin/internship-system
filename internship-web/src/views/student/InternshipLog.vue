<template>
  <PageLayout title="实习日志">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">提交日志</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="日志日期">
          <el-date-picker
            v-model="searchForm.logDate"
            type="date"
            placeholder="请选择日期"
            clearable
            style="width: 200px"
            value-format="YYYY-MM-DD"
            @change="handleSearch"
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
      <el-table-column prop="logDate" label="日志日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.logDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="workContent" label="工作内容" min-width="300" show-overflow-tooltip />
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
          <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
          <el-button
            v-if="row.reviewStatus === 0"
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.reviewStatus === 0"
            link
            type="danger"
            size="small"
            @click="handleDelete(row)"
          >
            删除
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

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="日志日期" prop="logDate">
          <el-date-picker
            v-model="formData.logDate"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="企业名称">
          <el-input :value="formData.enterpriseName" disabled />
        </el-form-item>
        <el-form-item label="工作内容" prop="workContent">
          <el-input
            v-model="formData.workContent"
            type="textarea"
            :rows="8"
            placeholder="请详细描述当天的工作内容、遇到的问题、学到的知识等"
          />
        </el-form-item>
        <el-form-item label="工作收获" prop="workHarvest">
          <el-input
            v-model="formData.workHarvest"
            type="textarea"
            :rows="6"
            placeholder="请描述本次工作的收获和体会"
          />
        </el-form-item>
        <el-form-item label="遇到的问题" prop="problems">
          <el-input
            v-model="formData.problems"
            type="textarea"
            :rows="4"
            placeholder="请描述工作中遇到的问题（可选）"
          />
        </el-form-item>
        <el-form-item label="工作时长（小时）" prop="workHours">
          <el-input-number
            v-model="formData.workHours"
            :min="0"
            :max="24"
            :precision="1"
            style="width: 100%"
            placeholder="请输入工作时长"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
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
        <el-descriptions-item label="工作内容" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.workContent || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="工作收获" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.workHarvest || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.problems" label="遇到的问题" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.problems }}</div>
        </el-descriptions-item>
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
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { logApi } from '@/api/internship/log'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('提交日志')
const formRef = ref(null)

const searchForm = reactive({
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
const currentApply = ref(null)

const formData = reactive({
  logId: null,
  applyId: null,
  enterpriseName: '',
  logDate: '',
  workContent: '',
  workHarvest: '',
  problems: '',
  workHours: 8
})

const formRules = {
  logDate: [{ required: true, message: '请选择日志日期', trigger: 'change' }],
  workContent: [{ required: true, message: '请输入工作内容', trigger: 'blur' }],
  workHarvest: [{ required: true, message: '请输入工作收获', trigger: 'blur' }],
  workHours: [{ required: true, message: '请输入工作时长', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await logApi.getLogPage({
      current: pagination.current,
      size: pagination.size,
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

// 加载当前学生的申请信息
const loadCurrentApply = async () => {
  try {
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 1,
      status: 1 // 已通过的申请
    })
    if (res.code === 200 && res.data.records && res.data.records.length > 0) {
      currentApply.value = res.data.records[0]
      formData.applyId = currentApply.value.applyId
      formData.enterpriseName = currentApply.value.enterpriseName || ''
    }
  } catch (error) {
    console.error('加载申请信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.logDate = null
  searchForm.reviewStatus = null
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadCurrentApply()
  if (!currentApply.value) {
    ElMessage.warning('您还没有已通过的实习申请，无法提交日志')
    return
  }
  dialogTitle.value = '提交日志'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑日志'
  try {
    const res = await logApi.getLogById(row.logId)
    if (res.code === 200) {
      Object.assign(formData, {
        logId: res.data.logId,
        applyId: res.data.applyId,
        enterpriseName: res.data.enterpriseName || '',
        logDate: res.data.logDate,
        workContent: res.data.workContent || '',
        workHarvest: res.data.workHarvest || '',
        problems: res.data.problems || '',
        workHours: res.data.workHours || 8
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
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

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该日志吗？', '提示', {
      type: 'warning'
    })
    const res = await logApi.deleteLog(row.logId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = {
          ...formData,
          problems: formData.problems || undefined
        }
        if (formData.logId) {
          data.logId = formData.logId
          const res = await logApi.updateLog(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await logApi.addLog(data)
          if (res.code === 200) {
            ElMessage.success('提交成功')
            dialogVisible.value = false
            loadData()
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    logId: null,
    applyId: currentApply.value?.applyId || null,
    enterpriseName: currentApply.value?.enterpriseName || '',
    logDate: '',
    workContent: '',
    workHarvest: '',
    problems: '',
    workHours: 8
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 禁用未来日期
const disabledDate = (time) => {
  return time.getTime() > Date.now()
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
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

