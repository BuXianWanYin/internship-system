<template>
  <PageLayout title="阶段性成果">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">提交成果</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
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
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="成果标题" prop="achievementTitle">
          <el-input
            v-model="formData.achievementTitle"
            placeholder="请输入成果标题"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="成果类型" prop="achievementType">
              <el-select
                v-model="formData.achievementType"
                placeholder="请选择类型"
                style="width: 100%"
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业名称">
              <el-input :value="formData.enterpriseName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成果描述" prop="achievementDescription">
          <RichTextEditor
            v-model="formData.achievementDescription"
            placeholder="请详细描述成果内容、技术要点、创新点等（支持富文本编辑，包括图片、文档、表格等）"
            height="400px"
          />
        </el-form-item>
        <el-form-item label="成果附件" prop="fileUrls">
          <el-upload
            ref="uploadRef"
            :action="uploadAction"
            :headers="uploadHeaders"
            :file-list="fileList"
            :before-upload="beforeUpload"
            :on-success="handleUploadSuccess"
            :on-remove="handleRemove"
            :on-error="handleUploadError"
            :limit="10"
            multiple
            :accept="allowedFileTypes"
          >
            <el-button type="primary" :icon="Plus">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                <div>支持上传的文件类型：{{ allowedFileTypesText }}</div>
                <div>单个文件大小不超过10MB，最多可上传10个文件</div>
              </div>
            </template>
          </el-upload>
          <div v-if="attachmentUrls.length > 0" class="attachment-preview">
            <div v-for="(url, index) in attachmentUrls" :key="index" class="attachment-item">
              <el-link type="primary" :icon="Document" @click="handleDownloadFile(url)">
                {{ getFileName(url) }}
              </el-link>
              <el-button
                link
                type="danger"
                size="small"
                @click="handleRemoveAttachment(index)"
              >
                删除
              </el-button>
            </div>
          </div>
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
      title="成果详情"
      width="900px"
    >
      <el-descriptions :column="2" border>
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
          <div 
            v-if="detailData.achievementDescription"
            v-html="detailData.achievementDescription"
            class="rich-content achievement-description"
          ></div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.fileUrls" label="成果附件" :span="2">
          <div class="attachment-list">
            <div v-for="(url, index) in (detailData.fileUrls || '').split(',').filter(u => u)" :key="index" class="attachment-item">
              <el-link type="primary" :icon="Document" @click="handleDownloadFile(url)">
                {{ getFileName(url) }}
              </el-link>
            </div>
          </div>
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
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Document } from '@element-plus/icons-vue'
import { achievementApi } from '@/api/internship/achievement'
import { applyApi } from '@/api/internship/apply'
import { fileApi } from '@/api/common/file'
import { formatDateTime } from '@/utils/dateUtils'
import { getToken } from '@/utils/auth'
import PageLayout from '@/components/common/PageLayout.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('提交成果')
const formRef = ref(null)

const searchForm = reactive({
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
const currentApply = ref(null)
const uploadRef = ref(null)
const fileList = ref([])
const attachmentUrls = ref([])

const formData = reactive({
  achievementId: null,
  applyId: null,
  enterpriseName: '',
  achievementTitle: '',
  achievementType: '',
  achievementDescription: '',
  fileUrls: ''
})

// 文件上传配置
const uploadAction = computed(() => {
  // 使用相对路径，request会自动添加baseURL
  return '/file/upload'
})

const uploadHeaders = computed(() => {
  const token = getToken()
  return {
    Authorization: token ? `Bearer ${token}` : ''
  }
})

// 根据成果类型获取允许的文件类型
const allowedFileTypes = computed(() => {
  const type = formData.achievementType
  const allTypes = '.doc,.docx,.pdf,.txt,.rtf,.jpg,.jpeg,.png,.gif,.bmp,.xls,.xlsx,.zip,.rar,.7z'
  
  if (!type) {
    return allTypes
  }
  
  switch (type) {
    case '项目文档':
      return '.doc,.docx,.pdf,.txt,.rtf,.xls,.xlsx,.zip,.rar,.7z'
    case '技术文档':
      return '.doc,.docx,.pdf,.txt,.rtf,.xls,.xlsx'
    case '学习笔记':
      return '.doc,.docx,.pdf,.txt,.rtf,.xls,.xlsx,.jpg,.jpeg,.png,.gif'
    case '作品展示':
      return '.jpg,.jpeg,.png,.gif,.bmp,.pdf,.zip,.rar,.7z'
    case '项目成果':
    case '工作成果':
    case '其他':
    default:
      return allTypes
  }
})

const allowedFileTypesText = computed(() => {
  const type = formData.achievementType
  if (!type) {
    return '所有类型（文档、图片、表格、压缩文件）'
  }
  
  switch (type) {
    case '项目文档':
      return '文档、表格、压缩文件（doc, docx, pdf, txt, rtf, xls, xlsx, zip, rar, 7z）'
    case '技术文档':
      return '文档、表格（doc, docx, pdf, txt, rtf, xls, xlsx）'
    case '学习笔记':
      return '文档、表格、图片（doc, docx, pdf, txt, rtf, xls, xlsx, jpg, jpeg, png, gif）'
    case '作品展示':
      return '图片、PDF、压缩文件（jpg, jpeg, png, gif, bmp, pdf, zip, rar, 7z）'
    case '项目成果':
    case '工作成果':
    case '其他':
    default:
      return '所有类型（文档、图片、表格、压缩文件）'
  }
})

const formRules = {
  achievementTitle: [{ required: true, message: '请输入成果标题', trigger: 'blur' }],
  achievementType: [{ required: true, message: '请选择成果类型', trigger: 'change' }],
  achievementDescription: [{ required: true, message: '请输入成果描述', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await achievementApi.getAchievementPage({
      current: pagination.current,
      size: pagination.size,
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

// 加载当前学生的申请信息
const loadCurrentApply = async () => {
  try {
    // 使用getCurrentInternship接口获取已确认上岗的申请
    const res = await applyApi.getCurrentInternship()
    if (res.code === 200 && res.data) {
      currentApply.value = res.data
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
  searchForm.achievementType = ''
  searchForm.reviewStatus = null
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadCurrentApply()
  if (!currentApply.value) {
    ElMessage.warning('您还没有已通过的实习申请，无法提交成果')
    return
  }
  dialogTitle.value = '提交成果'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑成果'
  try {
    const res = await achievementApi.getAchievementById(row.achievementId)
    if (res.code === 200) {
      Object.assign(formData, {
        achievementId: res.data.achievementId,
        applyId: res.data.applyId,
        enterpriseName: res.data.enterpriseName || '',
        achievementTitle: res.data.achievementTitle,
        achievementType: res.data.achievementType,
        achievementDescription: res.data.achievementDescription || '',
        fileUrls: res.data.fileUrls || ''
      })
      
      // 加载附件列表
      if (res.data.fileUrls) {
        attachmentUrls.value = res.data.fileUrls.split(',').filter(url => url.trim())
        // 构建fileList用于显示
        fileList.value = attachmentUrls.value.map((url, index) => ({
          uid: index,
          name: getFileName(url),
          url: fileApi.getDownloadUrl(url),
          status: 'success'
        }))
      } else {
        attachmentUrls.value = []
        fileList.value = []
      }
      
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

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该成果吗？', '提示', {
      type: 'warning'
    })
    const res = await achievementApi.deleteAchievement(row.achievementId)
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

// 文件上传前验证
const beforeUpload = (file) => {
  // 检查文件类型
  const fileName = file.name
  const fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
  const allowedExtensions = allowedFileTypes.value.split(',').map(ext => ext.replace('.', '').toLowerCase())
  
  if (!allowedExtensions.includes(fileExtension)) {
    ElMessage.error(`不支持的文件类型：${fileExtension}，允许的类型：${allowedFileTypesText.value}`)
    return false
  }
  
  // 检查文件大小（10MB）
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 文件上传成功
const handleUploadSuccess = (response, file) => {
  if (response.code === 200) {
    if (Array.isArray(response.data)) {
      // 多个文件上传
      response.data.forEach(url => {
        if (!attachmentUrls.value.includes(url)) {
          attachmentUrls.value.push(url)
        }
      })
    } else if (typeof response.data === 'string') {
      // 单个文件上传
      if (!attachmentUrls.value.includes(response.data)) {
        attachmentUrls.value.push(response.data)
      }
    }
    ElMessage.success('文件上传成功')
  } else {
    ElMessage.error(response.message || '文件上传失败')
  }
}

// 文件上传失败
const handleUploadError = (error) => {
  console.error('文件上传失败:', error)
  ElMessage.error('文件上传失败')
}

// 移除文件
const handleRemove = (file) => {
  // 从attachmentUrls中移除对应的URL
  if (file.response && file.response.data) {
    const url = Array.isArray(file.response.data) ? file.response.data[0] : file.response.data
    const index = attachmentUrls.value.indexOf(url)
    if (index > -1) {
      attachmentUrls.value.splice(index, 1)
    }
  }
}

// 移除附件
const handleRemoveAttachment = (index) => {
  attachmentUrls.value.splice(index, 1)
  // 同时从fileList中移除
  if (uploadRef.value) {
    const fileList = uploadRef.value.fileList
    if (fileList[index]) {
      uploadRef.value.handleRemove(fileList[index])
    }
  }
}

// 下载文件
const handleDownloadFile = async (url) => {
  try {
    await fileApi.downloadFile(url)
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 获取文件名
const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 设置提交日期为当前日期
        const today = new Date()
        const submitDate = today.toISOString().split('T')[0] // 格式：YYYY-MM-DD
        
        // 合并附件URL（逗号分隔）
        const fileUrlsStr = attachmentUrls.value.length > 0 ? attachmentUrls.value.join(',') : ''
        
        const data = {
          ...formData,
          achievementName: formData.achievementTitle, // 将achievementTitle映射到achievementName
          submitDate: submitDate, // 设置提交日期
          fileUrls: fileUrlsStr || undefined // 文件URL（多个用逗号分隔）
        }
        // 删除achievementTitle，因为后端使用achievementName
        delete data.achievementTitle
        
        if (formData.achievementId) {
          data.achievementId = formData.achievementId
          const res = await achievementApi.updateAchievement(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await achievementApi.addAchievement(data)
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
    achievementId: null,
    applyId: currentApply.value?.applyId || null,
    enterpriseName: currentApply.value?.enterpriseName || '',
    achievementTitle: '',
    achievementType: '',
    achievementDescription: '',
    fileUrls: ''
  })
  fileList.value = []
  attachmentUrls.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
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

.attachment-preview {
  margin-top: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rich-content {
  max-width: 100%;
  word-wrap: break-word;
}

.achievement-description {
  line-height: 1.6;
}

.achievement-description :deep(img) {
  max-width: 100%;
  height: auto;
}

.achievement-description :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 10px 0;
}

.achievement-description :deep(table td),
.achievement-description :deep(table th) {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: left;
}

.achievement-description :deep(table th) {
  background-color: #f5f7fa;
  font-weight: bold;
}
</style>

