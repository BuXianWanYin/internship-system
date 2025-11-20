<template>
  <PageLayout title="班级管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加班级</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="班级名称">
          <el-input
            v-model="searchForm.className"
            placeholder="请输入班级名称"
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
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column prop="classCode" label="班级代码" min-width="120" />
      <el-table-column prop="majorId" label="所属专业ID" width="120" />
      <el-table-column prop="enrollmentYear" label="入学年份" width="100" align="center" />
      <el-table-column prop="shareCode" label="分享码" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.shareCode" type="primary" size="small">{{ row.shareCode }}</el-tag>
          <span v-else style="color: #909399">未生成</span>
        </template>
      </el-table-column>
      <el-table-column prop="shareCodeUseCount" label="使用次数" width="100" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" size="small" @click="handleShareCode(row)">分享码</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">停用</el-button>
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
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="formData.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="班级代码" prop="classCode">
          <el-input v-model="formData.classCode" placeholder="请输入班级代码" />
        </el-form-item>
        <el-form-item label="所属专业ID" prop="majorId">
          <el-input-number v-model="formData.majorId" :min="1" placeholder="请输入专业ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-input-number v-model="formData.enrollmentYear" :min="2000" :max="2100" placeholder="请输入入学年份" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分享码对话框 -->
    <el-dialog
      v-model="shareCodeDialogVisible"
      title="班级分享码管理"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="shareCodeInfo" class="share-code-content">
        <div class="share-code-display">
          <div class="share-code-label">分享码</div>
          <div class="share-code-value">
            <el-tag type="primary" size="large" style="font-size: 20px; padding: 8px 16px">
              {{ shareCodeInfo.shareCode || '未生成' }}
            </el-tag>
            <el-button
              v-if="shareCodeInfo.shareCode"
              :icon="DocumentCopy"
              circle
              size="small"
              style="margin-left: 10px"
              @click="handleCopyShareCode"
            />
          </div>
        </div>
        <div class="share-code-info">
          <div class="info-item">
            <span class="info-label">生成时间：</span>
            <span class="info-value">{{ shareCodeInfo.generateTime ? formatDateTime(shareCodeInfo.generateTime) : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">过期时间：</span>
            <span class="info-value">{{ shareCodeInfo.expireTime ? formatDateTime(shareCodeInfo.expireTime) : '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">使用次数：</span>
            <span class="info-value">{{ shareCodeInfo.useCount || 0 }} 次</span>
          </div>
          <div class="info-item">
            <span class="info-label">已注册学生：</span>
            <span class="info-value">{{ shareCodeInfo.registeredStudentCount || 0 }} 人</span>
          </div>
        </div>
        <div class="share-code-actions">
          <el-button type="primary" :loading="shareCodeLoading" @click="handleGenerateShareCode">
            {{ shareCodeInfo.shareCode ? '重新生成' : '生成分享码' }}
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="shareCodeDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, DocumentCopy } from '@element-plus/icons-vue'
import { classApi } from '@/api/system/class'
import PageLayout from '@/components/common/PageLayout.vue'
import { formatDateTime } from '@/utils/dateUtils'

const loading = ref(false)
const submitLoading = ref(false)
const shareCodeLoading = ref(false)
const dialogVisible = ref(false)
const shareCodeDialogVisible = ref(false)
const dialogTitle = ref('添加班级')
const formRef = ref(null)
const currentClassId = ref(null)
const shareCodeInfo = ref(null)

const searchForm = reactive({
  className: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  classId: null,
  className: '',
  classCode: '',
  majorId: null,
  enrollmentYear: new Date().getFullYear(),
  status: 1
})

const formRules = {
  className: [
    { required: true, message: '请输入班级名称', trigger: 'blur' }
  ],
  classCode: [
    { required: true, message: '请输入班级代码', trigger: 'blur' }
  ],
  majorId: [
    { required: true, message: '请输入所属专业ID', trigger: 'blur' }
  ],
  enrollmentYear: [
    { required: true, message: '请输入入学年份', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await classApi.getClassPage({
      current: pagination.current,
      size: pagination.size,
      className: searchForm.className || undefined
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
  searchForm.className = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加班级'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑班级'
  Object.assign(formData, {
    classId: row.classId,
    className: row.className,
    classCode: row.classCode,
    majorId: row.majorId,
    enrollmentYear: row.enrollmentYear,
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该班级吗？', '提示', {
      type: 'warning'
    })
    const res = await classApi.deleteClass(row.classId)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
    }
  }
}

const handleShareCode = async (row) => {
  currentClassId.value = row.classId
  try {
    const res = await classApi.getShareCode(row.classId)
    if (res.code === 200) {
      shareCodeInfo.value = res.data
      shareCodeDialogVisible.value = true
    }
  } catch (error) {
    console.error('获取分享码失败:', error)
  }
}

const handleGenerateShareCode = async () => {
  if (!currentClassId.value) return
  
  try {
    await ElMessageBox.confirm(
      shareCodeInfo.value?.shareCode
        ? '重新生成分享码后，旧分享码将立即失效，确定要继续吗？'
        : '确定要生成分享码吗？',
      '提示',
      { type: 'warning' }
    )
    
    shareCodeLoading.value = true
    const res = await classApi.regenerateShareCode(currentClassId.value)
    if (res.code === 200) {
      shareCodeInfo.value = res.data
      ElMessage.success('分享码生成成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('生成分享码失败:', error)
    }
  } finally {
    shareCodeLoading.value = false
  }
}

const handleCopyShareCode = async () => {
  if (!shareCodeInfo.value?.shareCode) return
  
  try {
    await navigator.clipboard.writeText(shareCodeInfo.value.shareCode)
    ElMessage.success('分享码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (formData.classId) {
          res = await classApi.updateClass(formData.classId, formData)
        } else {
          res = await classApi.addClass(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.classId ? '更新成功' : '添加成功')
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
    classId: null,
    className: '',
    classCode: '',
    majorId: null,
    enrollmentYear: new Date().getFullYear(),
    status: 1
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

.share-code-content {
  padding: 20px 0;
}

.share-code-display {
  text-align: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.share-code-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 12px;
}

.share-code-value {
  display: flex;
  align-items: center;
  justify-content: center;
}

.share-code-info {
  margin-bottom: 20px;
  padding: 16px;
  background: #fafbfc;
  border-radius: 6px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 14px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-label {
  color: #606266;
}

.info-value {
  color: #303133;
  font-weight: 500;
}

.share-code-actions {
  text-align: center;
}
</style>

