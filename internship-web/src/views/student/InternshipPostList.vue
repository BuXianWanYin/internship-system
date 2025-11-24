<template>
  <PageLayout title="岗位列表">
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
        <el-form-item label="工作地点">
          <el-input
            v-model="searchForm.workLocation"
            placeholder="请输入工作地点"
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
      <el-table-column prop="postName" label="岗位名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="workLocation" label="工作地点" min-width="150" />
      <el-table-column prop="recruitCount" label="招聘人数" width="100" align="center" />
      <el-table-column prop="appliedCount" label="已申请" width="100" align="center" />
      <el-table-column label="薪资范围" width="150" align="center">
        <template #default="{ row }">
          <span v-if="row.salaryMin && row.salaryMax">
            {{ row.salaryMin }}-{{ row.salaryMax }}元/{{ row.salaryType || '月' }}
          </span>
          <span v-else-if="row.salaryType === '面议'">面议</span>
          <span v-else style="color: #909399">未设置</span>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ row.publishTime ? formatDateTime(row.publishTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button link type="success" size="small" @click="handleApply(row)">申请</el-button>
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
      title="岗位详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ detailData.postName }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ detailData.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ detailData.workLocation }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ detailData.workAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ detailData.recruitCount }}</el-descriptions-item>
        <el-descriptions-item label="已申请人数">{{ detailData.appliedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">
          <span v-if="detailData.salaryMin && detailData.salaryMax">
            {{ detailData.salaryMin }}-{{ detailData.salaryMax }}元/{{ detailData.salaryType || '月' }}
          </span>
          <span v-else-if="detailData.salaryType === '面议'">面议</span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="工作时间">{{ detailData.workHours || '-' }}</el-descriptions-item>
        <el-descriptions-item label="实习开始日期">
          {{ detailData.internshipStartDate ? formatDate(detailData.internshipStartDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="实习结束日期">
          {{ detailData.internshipEndDate ? formatDate(detailData.internshipEndDate) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detailData.contactPerson || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detailData.contactPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ detailData.contactEmail || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.postDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.skillRequirements || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleApplyFromDetail">申请该岗位</el-button>
      </template>
    </el-dialog>

    <!-- 申请对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="提交实习申请"
      width="700px"
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyFormRules"
        label-width="120px"
      >
        <el-form-item label="申请岗位">
          <el-input :value="applyForm.postName" disabled />
        </el-form-item>
        <el-form-item label="申请企业">
          <el-input :value="applyForm.enterpriseName" disabled />
        </el-form-item>
        <el-form-item label="简历附件" prop="resumeAttachment">
          <el-upload
            ref="resumeUploadRef"
            v-model:file-list="resumeFileList"
            :auto-upload="false"
            :limit="5"
            :on-change="handleResumeFileChange"
            :on-remove="handleResumeFileRemove"
            :before-upload="beforeResumeUpload"
            accept=".doc,.docx,.pdf"
            multiple
          >
            <el-button type="primary" :icon="Upload">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传Word文档（.doc, .docx）和PDF文件（.pdf），最多5个文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
          <div v-if="resumeAttachmentUrls.length > 0" class="attachment-list" style="margin-top: 10px">
            <div v-for="(url, index) in resumeAttachmentUrls" :key="index" class="attachment-item" style="display: flex; align-items: center; margin-bottom: 8px">
              <el-icon style="margin-right: 8px"><Document /></el-icon>
              <span style="flex: 1; margin-right: 8px">{{ getResumeFileName(url) }}</span>
              <el-button link type="primary" size="small" @click="handleDownloadResume(url)">下载</el-button>
              <el-button link type="danger" size="small" @click="removeResumeAttachment(index)">删除</el-button>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="申请理由" prop="applyReason">
          <el-input
            v-model="applyForm.applyReason"
            type="textarea"
            :rows="4"
            placeholder="请输入申请理由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="handleSubmitApply">提交申请</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Upload, Document } from '@element-plus/icons-vue'
import { postApi } from '@/api/internship/post'
import { applyApi } from '@/api/internship/apply'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const applyLoading = ref(false)
const detailDialogVisible = ref(false)
const applyDialogVisible = ref(false)
const formRef = ref(null)
const applyFormRef = ref(null)

const searchForm = reactive({
  postName: '',
  workLocation: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})

const applyForm = reactive({
  enterpriseId: null,
  postId: null,
  enterpriseName: '',
  postName: '',
  applyReason: ''
})

const resumeUploadRef = ref(null)
const resumeFileList = ref([])
const resumeAttachmentUrls = ref([])

const applyFormRules = {
  resumeAttachment: [
    { 
      validator: (rule, value, callback) => {
        if (resumeAttachmentUrls.value.length === 0 && resumeFileList.value.length === 0) {
          callback(new Error('请上传简历附件'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  applyReason: [{ required: true, message: '请输入申请理由', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await postApi.getPostPage({
      current: pagination.current,
      size: pagination.size,
      postName: searchForm.postName || undefined,
      workLocation: searchForm.workLocation || undefined,
      status: 3, // 只显示已发布的岗位
      cooperationOnly: true // 只显示合作企业的岗位
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

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.postName = ''
  searchForm.workLocation = ''
  handleSearch()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await postApi.getPostById(row.postId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 申请
const handleApply = (row) => {
  applyForm.enterpriseId = row.enterpriseId
  applyForm.postId = row.postId
  applyForm.enterpriseName = row.enterpriseName || ''
  applyForm.postName = row.postName
  applyForm.applyReason = ''
  resumeFileList.value = []
  resumeAttachmentUrls.value = []
  applyDialogVisible.value = true
}

// 从详情页申请
const handleApplyFromDetail = () => {
  detailDialogVisible.value = false
  applyForm.enterpriseId = detailData.value.enterpriseId
  applyForm.postId = detailData.value.postId
  applyForm.enterpriseName = detailData.value.enterpriseName || ''
  applyForm.postName = detailData.value.postName
  applyForm.applyReason = ''
  resumeFileList.value = []
  resumeAttachmentUrls.value = []
  applyDialogVisible.value = true
}

// 简历文件上传前验证
const beforeResumeUpload = (file) => {
  const fileExtension = file.name.split('.').pop().toLowerCase()
  const allowedExtensions = ['pdf', 'doc', 'docx']
  
  if (!allowedExtensions.includes(fileExtension)) {
    ElMessage.error('只支持上传Word文档（.doc, .docx）和PDF文件（.pdf）')
    return false
  }
  
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过10MB')
    return false
  }
  
  return true
}

// 简历文件变化
const handleResumeFileChange = (file, fileList) => {
  // 文件已添加到列表，稍后统一上传
}

// 简历文件移除
const handleResumeFileRemove = (file, fileList) => {
  // 文件已从列表移除
}

// 上传简历附件
const uploadResumeAttachments = async () => {
  if (resumeFileList.value.length === 0) {
    return resumeAttachmentUrls.value.join(',')
  }
  
  const files = resumeFileList.value.map(item => item.raw).filter(Boolean)
  if (files.length === 0) {
    return resumeAttachmentUrls.value.join(',')
  }
  
  try {
    const res = await fileApi.uploadFiles(files)
    if (res.code === 200 && res.data) {
      // 合并新上传的文件和已有附件
      const newUrls = [...resumeAttachmentUrls.value, ...res.data]
      return newUrls.join(',')
    }
  } catch (error) {
    console.error('简历附件上传失败:', error)
    ElMessage.error('简历附件上传失败: ' + (error.response?.data?.message || error.message))
    throw error
  }
  
  return resumeAttachmentUrls.value.join(',')
}

// 获取简历文件名
const getResumeFileName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1] || url
}

// 下载简历文件
const handleDownloadResume = async (filePath) => {
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

// 移除简历附件
const removeResumeAttachment = (index) => {
  resumeAttachmentUrls.value.splice(index, 1)
}

// 提交申请
const handleSubmitApply = async () => {
  if (!applyFormRef.value) return
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      applyLoading.value = true
      try {
        // 先上传简历附件
        let resumeAttachmentUrlsStr = ''
        try {
          resumeAttachmentUrlsStr = await uploadResumeAttachments()
        } catch (error) {
          applyLoading.value = false
          return
        }
        
        const res = await applyApi.addCooperationApply({
          enterpriseId: applyForm.enterpriseId,
          postId: applyForm.postId,
          resumeAttachment: resumeAttachmentUrlsStr,
          applyReason: applyForm.applyReason
        })
        if (res.code === 200) {
          ElMessage.success('申请提交成功')
          applyDialogVisible.value = false
          resumeFileList.value = []
          resumeAttachmentUrls.value = []
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        ElMessage.error(error.response?.data?.message || '提交申请失败')
      } finally {
        applyLoading.value = false
      }
    }
  })
}

// 分页大小变化
const handleSizeChange = () => {
  loadData()
}

// 页码变化
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

