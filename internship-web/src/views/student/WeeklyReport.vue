<template>
  <PageLayout title="周报">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">提交周报</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="周次">
          <el-input-number
            v-model="searchForm.weekNumber"
            :min="1"
            placeholder="请输入周次"
            clearable
            style="width: 150px"
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

    <!-- 周报列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="weekNumber" label="周次" width="80" align="center" />
      <el-table-column prop="weekStartDate" label="开始日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.weekStartDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="weekEndDate" label="结束日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.weekEndDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
       <el-table-column label="周报内容" min-width="300" show-overflow-tooltip>
         <template #default="{ row }">
           <div class="table-content-preview">{{ getContentPreview(row.workContent || row.reportContent) }}</div>
         </template>
       </el-table-column>
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
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周次" prop="weekNumber">
              <el-input-number
                v-model="formData.weekNumber"
                :min="1"
                style="width: 100%"
                placeholder="请输入周次"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业名称">
              <el-input :value="formData.enterpriseName" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="weekStartDate">
              <el-date-picker
                v-model="formData.weekStartDate"
                type="date"
                placeholder="请选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="weekEndDate">
              <el-date-picker
                v-model="formData.weekEndDate"
                type="date"
                placeholder="请选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="周报内容" prop="workContent">
          <RichTextEditor
            v-model="formData.workContent"
            placeholder="请编写本周的实习周报，包括工作内容、工作收获、遇到的问题、下周计划等。支持富文本格式、插入图片等。"
            :height="'500px'"
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            v-model:file-list="fileList"
            :action="''"
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            multiple
            :limit="5"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传Word文档、PDF、图片等，单个文件不超过10MB，最多上传5个文件
              </div>
            </template>
          </el-upload>
          <div v-if="attachmentUrls.length > 0" class="attachment-list">
            <div v-for="(url, index) in attachmentUrls" :key="index" class="attachment-item">
               <el-link type="primary" @click="handleDownloadFile(url)">{{ getFileName(url) }}</el-link>
              <el-button
                link
                type="danger"
                size="small"
                @click="removeAttachment(index)"
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
      title="周报详情"
      width="900px"
    >
      <!-- 基本信息 -->
      <el-descriptions :column="2" border class="detail-info">
        <el-descriptions-item label="周次">{{ detailData.weekNumber }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">
          {{ formatDate(detailData.weekStartDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束日期">
          {{ formatDate(detailData.weekEndDate) }}
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
      </el-descriptions>

      <!-- 周报内容 -->
      <div class="content-section">
        <div class="content-title">周报内容</div>
        <div 
          v-html="getMergedReportContent(detailData)" 
          class="rich-content report-content"
        ></div>
      </div>

      <!-- 附件 -->
      <div v-if="detailData.attachmentUrls" class="content-section">
        <div class="content-title">附件</div>
          <div class="attachment-list">
            <div v-for="(url, index) in (detailData.attachmentUrls || '').split(',').filter(u => u)" :key="index" class="attachment-item">
            <el-link type="primary" :icon="Document" @click="handleDownloadFile(url)">
              {{ getFileName(url) }}
            </el-link>
          </div>
            </div>
          </div>

      <!-- 批阅信息 -->
      <el-descriptions v-if="detailData.reviewComment || detailData.reviewTime || detailData.reviewerName" :column="2" border class="detail-info">
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
import { Plus, Search, Refresh, Document } from '@element-plus/icons-vue'
import { weeklyReportApi } from '@/api/internship/weeklyReport'
import { applyApi } from '@/api/internship/apply'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('提交周报')
const formRef = ref(null)

const searchForm = reactive({
  weekNumber: null,
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
  reportId: null,
  applyId: null,
  enterpriseName: '',
  weekNumber: null,
  weekStartDate: '',
  weekEndDate: '',
  reportTitle: '',
  workContent: '',
  attachmentUrls: ''
})

const formRules = {
  weekNumber: [{ required: true, message: '请输入周次', trigger: 'blur' }],
  weekStartDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  weekEndDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  workContent: [{ required: true, message: '请输入周报内容', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await weeklyReportApi.getReportPage({
      current: pagination.current,
      size: pagination.size,
      weekNumber: searchForm.weekNumber || undefined,
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
  searchForm.weekNumber = null
  searchForm.reviewStatus = null
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadCurrentApply()
  if (!currentApply.value) {
    ElMessage.warning('您还没有已通过的实习申请，无法提交周报')
    return
  }
  dialogTitle.value = '提交周报'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑周报'
  try {
    const res = await weeklyReportApi.getReportById(row.reportId)
    if (res.code === 200) {
      // 直接使用workContent
      const content = res.data.workContent || ''
      
      Object.assign(formData, {
        reportId: res.data.reportId,
        applyId: res.data.applyId,
        enterpriseName: res.data.enterpriseName || '',
        weekNumber: res.data.weekNumber,
        weekStartDate: res.data.weekStartDate,
        weekEndDate: res.data.weekEndDate,
        reportTitle: res.data.reportTitle || '',
        workContent: content,
        attachmentUrls: res.data.attachmentUrls || ''
      })
      // 加载附件列表
      if (res.data.attachmentUrls) {
        attachmentUrls.value = res.data.attachmentUrls.split(',').filter(u => u)
      } else {
        attachmentUrls.value = []
      }
      fileList.value = []
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
    const res = await weeklyReportApi.getReportById(row.reportId)
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
    await ElMessageBox.confirm('确定要删除该周报吗？', '提示', {
      type: 'warning'
    })
    const res = await weeklyReportApi.deleteReport(row.reportId)
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
  const isValidType = ['doc', 'docx', 'pdf', 'txt', 'jpg', 'jpeg', 'png', 'gif', 'xls', 'xlsx', 'zip', 'rar'].some(
    ext => file.name.toLowerCase().endsWith('.' + ext)
  )
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('不支持的文件类型！支持：Word、PDF、图片、Excel、压缩文件')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return false // 阻止自动上传
}

// 文件变化
const handleFileChange = (file, fileList) => {
  // 文件已添加到列表，稍后统一上传
}

// 移除文件
const handleFileRemove = (file, fileList) => {
  // 文件已从列表移除
}

// 上传附件
const uploadAttachments = async () => {
  if (fileList.value.length === 0) {
    return attachmentUrls.value.join(',')
  }
  
  const files = fileList.value.map(item => item.raw).filter(Boolean)
  if (files.length === 0) {
    return attachmentUrls.value.join(',')
  }
  
  try {
    const res = await fileApi.uploadFiles(files)
    if (res.code === 200 && res.data) {
      // 合并新上传的文件和已有附件
      const newUrls = [...attachmentUrls.value, ...res.data]
      return newUrls.join(',')
    }
  } catch (error) {
    console.error('附件上传失败:', error)
    ElMessage.error('附件上传失败: ' + (error.response?.data?.message || error.message))
    throw error
  }
  
  return attachmentUrls.value.join(',')
}

// 获取文件名
const getFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

// 下载文件
const handleDownloadFile = async (filePath) => {
  if (!filePath) {
    ElMessage.warning('文件路径为空')
    return
  }
  try {
    await fileApi.downloadFile(filePath)
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败: ' + (error.message || '未知错误'))
  }
}

// 移除附件
const removeAttachment = (index) => {
  attachmentUrls.value.splice(index, 1)
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 先上传附件
        let attachmentUrlsStr = ''
        try {
          attachmentUrlsStr = await uploadAttachments()
        } catch (error) {
          // 附件上传失败，不阻止提交
          console.error('附件上传失败，继续提交:', error)
        }
        
        // 如果没有提供标题，根据周次自动生成
        let reportTitle = formData.reportTitle
        if (!reportTitle && formData.weekNumber) {
          reportTitle = `第${formData.weekNumber}周实习周报`
        } else if (!reportTitle && formData.weekStartDate) {
          reportTitle = formData.weekStartDate + ' 实习周报'
        }
        
        const data = {
          ...formData,
          reportTitle: reportTitle || '实习周报',
          attachmentUrls: attachmentUrlsStr || undefined
        }
        if (formData.reportId) {
          data.reportId = formData.reportId
          const res = await weeklyReportApi.updateReport(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await weeklyReportApi.addReport(data)
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

 // 获取内容预览（用于表格显示，去除HTML标签，只显示纯文本）
 const getContentPreview = (htmlContent) => {
   if (!htmlContent) return '-'
   // 创建一个临时div来解析HTML
   const tempDiv = document.createElement('div')
   tempDiv.innerHTML = htmlContent
   // 获取纯文本内容
   let text = tempDiv.textContent || tempDiv.innerText || ''
   // 去除多余的空白字符
   text = text.replace(/\s+/g, ' ').trim()
   // 限制长度，最多显示100个字符
   if (text.length > 100) {
     text = text.substring(0, 100) + '...'
   }
   return text
 }

 // 合并周报内容用于显示
 const getMergedReportContent = (data) => {
  if (!data) return '-'
  // 直接使用workContent
  return data.workContent || '-'
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    reportId: null,
    applyId: currentApply.value?.applyId || null,
    enterpriseName: currentApply.value?.enterpriseName || '',
    weekNumber: null,
    weekStartDate: '',
    weekEndDate: '',
    reportTitle: '',
    workContent: '',
    attachmentUrls: ''
  })
  fileList.value = []
  attachmentUrls.value = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
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

.attachment-list {
  margin-top: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
  padding: 8px;
  background: #f5f7fa;
  border-radius: 4px;
}

.detail-info {
  margin-bottom: 20px;
}

.content-section {
  margin: 20px 0;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.content-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid #409eff;
}

.rich-content {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  min-height: 100px;
  line-height: 1.8;
  color: #606266;
  word-wrap: break-word;
  word-break: break-all;
}

.report-content :deep(h3) {
  margin-top: 24px;
  margin-bottom: 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.report-content :deep(h3:first-child) {
  margin-top: 0;
}

.report-content :deep(p) {
  margin: 12px 0;
  line-height: 1.8;
}

.report-content :deep(img) {
  max-width: 100%;
  height: auto;
  margin: 16px 0;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.report-content :deep(ul),
.report-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.report-content :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

.report-content :deep(blockquote) {
  margin: 12px 0;
  padding: 12px 16px;
  background: #f5f7fa;
  border-left: 4px solid #409eff;
  border-radius: 4px;
}

.report-content :deep(code) {
  padding: 2px 6px;
  background: #f5f7fa;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.report-content :deep(pre) {
  margin: 12px 0;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 6px;
  overflow-x: auto;
}

.report-content :deep(pre code) {
  background: transparent;
  padding: 0;
}

.table-content-preview {
  color: #606266;
  line-height: 1.5;
}
</style>

