<template>
  <PageLayout title="学期管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学期</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学期名称">
          <el-input
            v-model="searchForm.semesterName"
            placeholder="请输入学期名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column prop="semesterName" label="学期名称" min-width="200" />
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="isCurrent" label="当前学期" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isCurrent === 1" type="success" size="small">是</el-tag>
          <span v-else style="color: #909399">否</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.isCurrent !== 1"
            link
            type="success"
            size="small"
            @click="handleSetCurrent(row)"
          >
            设为当前
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="学期名称" prop="semesterName">
          <el-input v-model="formData.semesterName" placeholder="请输入学期名称" />
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="设为当前学期" prop="isCurrent">
          <el-switch v-model="formData.isCurrent" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { semesterApi } from '@/api/system/semester'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加学期')
const formRef = ref(null)

const searchForm = reactive({
  semesterName: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  semesterId: null,
  semesterName: '',
  startDate: '',
  endDate: '',
  isCurrent: 0
})

const formRules = {
  semesterName: [
    { required: true, message: '请输入学期名称', trigger: 'blur' }
  ],
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await semesterApi.getSemesterPage({
      current: pagination.current,
      size: pagination.size,
      semesterName: searchForm.semesterName || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.semesterName = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加学期'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学期'
  Object.assign(formData, {
    semesterId: row.semesterId,
    semesterName: row.semesterName,
    startDate: row.startDate,
    endDate: row.endDate,
    isCurrent: row.isCurrent
  })
  dialogVisible.value = true
}

const handleSetCurrent = async (row) => {
  try {
    await ElMessageBox.confirm('确定要设置该学期为当前学期吗？', '提示', {
      type: 'warning'
    })
    const res = await semesterApi.setCurrentSemester(row.semesterId)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置失败:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学期吗？', '提示', {
      type: 'warning'
    })
    const res = await semesterApi.deleteSemester(row.semesterId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (formData.startDate && formData.endDate && formData.startDate > formData.endDate) {
        ElMessage.error('开始日期不能晚于结束日期')
        return
      }
      
      submitLoading.value = true
      try {
        let res
        if (formData.semesterId) {
          res = await semesterApi.updateSemester(formData.semesterId, formData)
        } else {
          res = await semesterApi.addSemester(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.semesterId ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(formData, {
    semesterId: null,
    semesterName: '',
    startDate: '',
    endDate: '',
    isCurrent: 0
  })
  formRef.value?.clearValidate()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 6px;
}

.search-form {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

