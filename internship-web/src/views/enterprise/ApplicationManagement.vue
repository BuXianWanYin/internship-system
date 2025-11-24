<template>
  <PageLayout title="申请管理">
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
        <el-form-item label="岗位名称">
          <el-input
            v-model="searchForm.postName"
            placeholder="请输入岗位名称"
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

    <!-- 申请列表 -->
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
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            link
            type="success"
            size="small"
            @click="handleFilter(row, 3)"
          >
            录用
          </el-button>
          <el-button
            v-if="row.status === 0 || row.status === 1"
            link
            type="danger"
            size="small"
            @click="handleFilter(row, 4)"
          >
            拒绝
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

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="800px"
    >
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 学生申请信息标签页 -->
        <el-tab-pane label="学生申请信息" name="student">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
            <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
            <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="申请时间">
              {{ formatDateTime(detailData.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="getStatusType(detailData.status)" size="small">
                {{ getStatusText(detailData.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请理由" :span="2">
              <div style="white-space: pre-wrap">{{ detailData.applyReason || '-' }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.resumeContent" label="简历内容" :span="2">
              <div style="white-space: pre-wrap">{{ detailData.resumeContent }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.resumeAttachment" label="简历附件" :span="2">
              <div class="attachment-list">
                <div v-for="(url, index) in (detailData.resumeAttachment || '').split(',').filter(u => u)" :key="index" class="attachment-item" style="display: flex; align-items: center; margin-bottom: 8px">
                  <el-icon style="margin-right: 8px"><Document /></el-icon>
                  <span style="flex: 1; margin-right: 8px">{{ getResumeFileName(url) }}</span>
                  <el-button link type="primary" size="small" @click="handleDownloadResume(url)">下载</el-button>
                </div>
              </div>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 企业审核信息标签页 -->
        <el-tab-pane label="企业审核信息" name="enterprise">
          <el-descriptions :column="2" border>
            <el-descriptions-item v-if="detailData.interviewTime" label="面试时间">
              {{ formatDateTime(detailData.interviewTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.interviewLocation" label="面试地点" :span="2">
              {{ detailData.interviewLocation }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.interviewResult !== null" label="面试结果">
              <el-tag :type="detailData.interviewResult === 1 ? 'success' : 'danger'" size="small">
                {{ detailData.interviewResult === 1 ? '通过' : detailData.interviewResult === 2 ? '不通过' : '待定' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.interviewComment" label="面试评价" :span="2">
              <div style="white-space: pre-wrap">{{ detailData.interviewComment }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.enterpriseFeedback" label="企业反馈" :span="2">
              <div style="white-space: pre-wrap; color: #606266">{{ detailData.enterpriseFeedback }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.enterpriseFeedbackTime" label="企业反馈时间">
              {{ formatDateTime(detailData.enterpriseFeedbackTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.acceptTime" label="录用时间">
              {{ formatDateTime(detailData.acceptTime) }}
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.status === 0 || detailData.status === 1"
          type="success"
          @click="handleFilterFromDetail(3)"
        >
          录用
        </el-button>
        <el-button
          v-if="detailData.status === 0 || detailData.status === 1"
          type="danger"
          @click="handleFilterFromDetail(4)"
        >
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 筛选操作对话框 -->
    <el-dialog
      v-model="filterDialogVisible"
      :title="filterDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="filterFormRef"
        :model="filterForm"
        :rules="filterFormRules"
        label-width="100px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentApply.studentName }}（{{ currentApply.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>岗位：</strong>{{ currentApply.postName || '-' }}</div>
          </div>
        </el-form-item>
        <el-form-item label="备注" prop="comment">
          <el-input
            v-model="filterForm.comment"
            type="textarea"
            :rows="6"
            :placeholder="filterForm.action === 3 ? '请输入录用备注（可选）' : '请输入拒绝原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="filterDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="filterLoading" @click="handleSubmitFilter">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Document } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const filterLoading = ref(false)
const detailDialogVisible = ref(false)
const filterDialogVisible = ref(false)
const filterDialogTitle = ref('筛选操作')
const filterFormRef = ref(null)
const activeTab = ref('student')

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  postName: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentApply = ref({})

const filterForm = reactive({
  action: 3,
  comment: ''
})

const filterFormRules = {
  comment: [
    {
      validator: (rule, value, callback) => {
        if (filterForm.action === 4 && !value) {
          callback(new Error('拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await applyApi.getApplyPage({
      current: pagination.current,
      size: pagination.size,
      applyType: 1, // 只显示合作企业申请
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    
    if (res.code === 200) {
      let records = res.data.records || []
      
      // 前端过滤（因为后端可能不支持按学生姓名、学号、岗位名称搜索）
      if (searchForm.studentName) {
        records = records.filter(item => 
          item.studentName && item.studentName.includes(searchForm.studentName)
        )
      }
      if (searchForm.studentNo) {
        records = records.filter(item => 
          item.studentNo && item.studentNo.includes(searchForm.studentNo)
        )
      }
      if (searchForm.postName) {
        records = records.filter(item => 
          item.postName && item.postName.includes(searchForm.postName)
        )
      }
      
      tableData.value = records
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
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.postName = ''
  searchForm.status = null
  handleSearch()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 页码变化
const handlePageChange = (current) => {
  pagination.current = current
  loadData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data
      activeTab.value = 'student'
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 筛选操作
const handleFilter = (row, action) => {
  currentApply.value = row
  filterForm.action = action
  filterForm.comment = ''
  filterDialogTitle.value = action === 3 ? '录用申请' : '拒绝申请'
  filterDialogVisible.value = true
}

// 从详情对话框筛选
const handleFilterFromDetail = (action) => {
  currentApply.value = detailData.value
  filterForm.action = action
  filterForm.comment = ''
  filterDialogTitle.value = action === 3 ? '录用申请' : '拒绝申请'
  detailDialogVisible.value = false
  filterDialogVisible.value = true
}

// 提交筛选操作
const handleSubmitFilter = async () => {
  if (!filterFormRef.value) return
  await filterFormRef.value.validate(async (valid) => {
    if (valid) {
      filterLoading.value = true
      try {
        const res = await applyApi.filterApply(
          currentApply.value.applyId,
          filterForm.action,
          filterForm.comment
        )
        if (res.code === 200) {
          ElMessage.success('操作成功')
          filterDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        filterLoading.value = false
      }
    }
  })
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已录用',
    4: '已拒绝录用'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'success',
    4: 'danger'
  }
  return typeMap[status] || 'info'
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

