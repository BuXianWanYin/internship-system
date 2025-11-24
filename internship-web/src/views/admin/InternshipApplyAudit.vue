<template>
  <PageLayout title="实习申请审核">
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
        <el-form-item label="申请类型">
          <el-select
            v-model="searchForm.applyType"
            placeholder="请选择申请类型"
            clearable
            style="width: 150px"
          >
            <el-option label="合作企业" :value="1" />
            <el-option label="自主实习" :value="2" />
          </el-select>
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
    0--0-0-0-0--090909999999999999999999999999999999999999999999999999909999999999999999999【=】】【【【【【【【【【【【【【-  </el-select>
        </el-form-item>
        <el-form-item label="解绑状态">
          <el-select
            v-model="searchForm.unbindStatus"
            placeholder="请选择解绑状态"
            clearable
            style="width: 150px"
          >
            <el-option label="未申请" :value="0" />
            <el-option label="申请中" :value="1" />
            <el-option label="已解绑" :value="2" />
            <el-option label="已拒绝" :value="3" />
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
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column label="申请类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="row.applyType === 1 ? 'primary' : 'warning'" size="small">
            {{ row.applyType === 1 ? '合作企业' : row.applyType === 2 ? '自主实习' : '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="150" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status, row.statusText)" size="small">
            {{ row.statusText || getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="解绑状态" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.unbindStatus === 1" type="warning" size="small">申请中</el-tag>
          <el-tag v-else-if="row.unbindStatus === 2" type="success" size="small">已解绑</el-tag>
          <el-tag v-else-if="row.unbindStatus === 3" type="danger" size="small">已拒绝</el-tag>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button
            v-if="row.status === 0"
            link
            type="success"
            size="small"
            @click="handleAudit(row, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0"
            link
            type="danger"
            size="small"
            @click="handleAudit(row, 2)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.unbindStatus === 1"
            link
            type="warning"
            size="small"
            @click="handleAuditUnbind(row)"
          >
            审核解绑
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

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="申请详情"
      width="1000px"
    >
      <!-- 基本信息 -->
      <el-descriptions :column="2" border style="margin-bottom: 20px">
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="申请类型">
          <el-tag :type="detailData.applyType === 1 ? 'primary' : 'warning'" size="small">
            {{ detailData.applyType === 1 ? '合作企业' : detailData.applyType === 2 ? '自主实习' : '未知' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(detailData.status, detailData.statusText)" size="small">
            {{ detailData.statusText || getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 标签页切换 -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 学生申请信息标签页 -->
        <el-tab-pane label="学生申请信息" name="student">
          <el-descriptions :column="2" border>
            <!-- 合作企业申请的企业信息 -->
            <template v-if="detailData.applyType === 1">
              <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || '-' }}</el-descriptions-item>
            </template>
            
            <!-- 自主实习申请的企业信息（学生填写） -->
            <template v-if="detailData.applyType === 2">
              <el-descriptions-item label="企业名称" :span="2">{{ detailData.selfEnterpriseName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业地址" :span="2">{{ detailData.selfEnterpriseAddress || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系人">{{ detailData.selfContactPerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ detailData.selfContactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="企业性质">{{ detailData.selfEnterpriseNature || '-' }}</el-descriptions-item>
              <el-descriptions-item label="岗位名称">{{ detailData.selfPostName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="实习开始日期">
                {{ detailData.selfStartDate ? formatDate(detailData.selfStartDate) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="实习结束日期">
                {{ detailData.selfEndDate ? formatDate(detailData.selfEndDate) : '-' }}
              </el-descriptions-item>
              <el-descriptions-item v-if="detailData.selfDescription" label="实习说明" :span="2">
                <div style="white-space: pre-wrap">{{ detailData.selfDescription }}</div>
              </el-descriptions-item>
            </template>
            
            <el-descriptions-item label="申请时间">
              {{ formatDateTime(detailData.createTime) }}
            </el-descriptions-item>
            <el-descriptions-item label="申请理由" :span="2">
              <div style="white-space: pre-wrap">{{ detailData.applyReason || '-' }}</div>
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

        <!-- 学校审核信息标签页（仅自主实习） -->
        <el-tab-pane v-if="detailData.applyType === 2" label="学校审核信息" name="school">
          <el-descriptions :column="2" border>
            <el-descriptions-item v-if="detailData.auditOpinion" label="审核意见" :span="2">
              <div style="white-space: pre-wrap; color: #606266">{{ detailData.auditOpinion }}</div>
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.auditTime" label="审核时间">
              {{ formatDateTime(detailData.auditTime) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detailData.auditorName" label="审核人">
              {{ detailData.auditorName }}
            </el-descriptions-item>
            <el-descriptions-item v-if="!detailData.auditOpinion && !detailData.auditTime" label="审核状态" :span="2">
              <el-tag type="info">待审核</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <!-- 企业审核信息标签页（仅合作企业） -->
        <el-tab-pane v-if="detailData.applyType === 1" label="企业审核信息" name="enterprise">
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
            <el-descriptions-item v-if="!detailData.interviewTime && !detailData.enterpriseFeedback" label="审核状态" :span="2">
              <el-tag type="info">待企业审核</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>
      </el-tabs>
      
      <!-- 状态流转历史 -->
      <el-divider content-position="left">状态流转历史</el-divider>
      <el-timeline v-if="detailData.statusHistory && detailData.statusHistory.length > 0">
        <el-timeline-item
          v-for="(item, index) in detailData.statusHistory"
          :key="index"
          :timestamp="formatDateTime(item.actionTime)"
          placement="top"
        >
          <el-card>
            <h4>{{ item.actionName }}</h4>
            <p><strong>操作人：</strong>{{ item.operator || '-' }}</p>
            <p><strong>说明：</strong>{{ item.description || '-' }}</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无状态流转记录" :image-size="80" />
      
      <!-- 下一步操作提示 -->
      <el-divider content-position="left">下一步操作</el-divider>
      <el-alert
        v-if="detailData.nextActionTip"
        :title="detailData.nextActionTip"
        type="info"
        :closable="false"
        show-icon
        style="margin-top: 10px"
      />
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.status === 0"
          type="success"
          @click="handleAuditFromDetail(1)"
        >
          通过
        </el-button>
        <el-button
          v-if="detailData.status === 0"
          type="danger"
          @click="handleAuditFromDetail(2)"
        >
          拒绝
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditDialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditFormRules"
        label-width="100px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentApply.studentName }}（{{ currentApply.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>企业：</strong>{{ currentApply.enterpriseName }}</div>
            <div style="margin-top: 5px"><strong>岗位：</strong>{{ currentApply.postName || '-' }}</div>
          </div>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditOpinion">
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="6"
            :placeholder="auditForm.auditStatus === 1 ? '请输入审核意见（可选）' : '请输入拒绝原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="handleSubmitAudit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 审核解绑对话框 -->
    <el-dialog
      v-model="unbindAuditDialogVisible"
      title="审核解绑申请"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="unbindAuditFormRef"
        :model="unbindAuditForm"
        :rules="unbindAuditFormRules"
        label-width="100px"
      >
        <el-form-item label="申请信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>学生：</strong>{{ currentUnbindApply.studentName }}（{{ currentUnbindApply.studentNo }}）</div>
            <div style="margin-top: 5px"><strong>企业：</strong>{{ currentUnbindApply.enterpriseName || currentUnbindApply.selfEnterpriseName || '-' }}</div>
            <div style="margin-top: 5px"><strong>岗位：</strong>{{ currentUnbindApply.postName || currentUnbindApply.selfPostName || '-' }}</div>
            <div v-if="currentUnbindApply.unbindReason" style="margin-top: 10px; padding-top: 10px; border-top: 1px solid #dcdfe6">
              <strong>离职原因：</strong>
              <div style="margin-top: 5px; color: #606266">{{ currentUnbindApply.unbindReason }}</div>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="unbindAuditForm.auditStatus">
            <el-radio :label="2">同意解绑</el-radio>
            <el-radio :label="3">拒绝解绑</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditOpinion">
          <el-input
            v-model="unbindAuditForm.auditOpinion"
            type="textarea"
            :rows="6"
            :placeholder="unbindAuditForm.auditStatus === 2 ? '请输入审核意见（可选）' : '请输入拒绝原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="unbindAuditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="unbindAuditLoading" @click="handleSubmitUnbindAudit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Document } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { fileApi } from '@/api/common/file'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const auditLoading = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const auditDialogTitle = ref('审核申请')
const auditFormRef = ref(null)
const activeTab = ref('student') // 标签页激活状态

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  applyType: null,
  status: null,
  unbindStatus: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const detailData = ref({})
const currentApply = ref({})

const auditForm = reactive({
  auditStatus: 1,
  auditOpinion: ''
})

const unbindAuditDialogVisible = ref(false)
const unbindAuditFormRef = ref(null)
const currentUnbindApply = ref({})
const unbindAuditLoading = ref(false)

const unbindAuditForm = reactive({
  auditStatus: 2, // 2-已解绑，3-解绑被拒绝
  auditOpinion: ''
})

const unbindAuditFormRules = {
  auditOpinion: [
    {
      validator: (rule, value, callback) => {
        if (unbindAuditForm.auditStatus === 3 && !value) {
          callback(new Error('拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const auditFormRules = {
  auditOpinion: [
    {
      validator: (rule, value, callback) => {
        if (auditForm.auditStatus === 2 && !value) {
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
      studentId: undefined, // 管理员查看所有学生申请
      enterpriseId: undefined,
      postId: undefined,
      applyType: searchForm.applyType !== null ? searchForm.applyType : undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    
    if (res.code === 200) {
      let records = res.data.records || []
      
      // 前端过滤学生姓名、学号和解绑状态（因为后端API不支持这些参数，需要在前端过滤）
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
      if (searchForm.unbindStatus !== null) {
        records = records.filter(item => 
          (item.unbindStatus || 0) === searchForm.unbindStatus
        )
      }
      
      tableData.value = records
      // 注意：由于前端过滤，总数可能不准确，但这是临时方案
      // 理想情况下应该在后端支持按学生姓名和学号查询
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
  searchForm.applyType = null
  searchForm.status = null
  searchForm.unbindStatus = null
  handleSearch()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data
      // 根据申请类型设置默认激活的标签页
      activeTab.value = 'student'
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 审核
const handleAudit = (row, auditStatus) => {
  currentApply.value = row
  auditForm.auditStatus = auditStatus
  auditForm.auditOpinion = ''
  auditDialogTitle.value = auditStatus === 1 ? '通过申请' : '拒绝申请'
  auditDialogVisible.value = true
}

// 从详情对话框审核
const handleAuditFromDetail = (auditStatus) => {
  currentApply.value = detailData.value
  auditForm.auditStatus = auditStatus
  auditForm.auditOpinion = ''
  auditDialogTitle.value = auditStatus === 1 ? '通过申请' : '拒绝申请'
  detailDialogVisible.value = false
  auditDialogVisible.value = true
}

// 提交审核
const handleSubmitAudit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      auditLoading.value = true
      try {
        const res = await applyApi.auditApply(
          currentApply.value.applyId,
          auditForm.auditStatus,
          auditForm.auditOpinion || undefined
        )
        if (res.code === 200) {
          ElMessage.success('审核成功')
          auditDialogVisible.value = false
          loadData()
          // 如果是从详情对话框打开的，重新加载详情
          if (detailDialogVisible.value === false && currentApply.value.applyId === detailData.value.applyId) {
            handleView(currentApply.value)
          }
        }
      } catch (error) {
        console.error('审核失败:', error)
        ElMessage.error(error.response?.data?.message || '审核失败')
      } finally {
        auditLoading.value = false
      }
    }
  })
}

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
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

// 获取状态类型（根据状态文本动态判断颜色）
const getStatusType = (status, statusText) => {
  // 如果有状态文本，优先根据状态文本判断
  if (statusText) {
    if (statusText.includes('等待') || statusText.includes('待')) {
      return 'warning' // 黄色 - 等待状态
    }
    if (statusText.includes('通过') || statusText.includes('已确认') || statusText.includes('已录用')) {
      return 'success' // 绿色 - 成功/通过状态
    }
    if (statusText.includes('拒绝') || statusText.includes('未通过') || statusText.includes('已取消')) {
      return 'danger' // 红色 - 拒绝/失败状态
    }
    if (statusText.includes('已完成') || statusText.includes('待定') || statusText.includes('处理')) {
      return 'info' // 灰色 - 信息状态
    }
  }
  
  // 如果没有状态文本，根据基础状态值判断
  const typeMap = {
    0: 'warning', // 待审核 - 黄色
    1: 'info',    // 已通过（等待企业处理）- 灰色，不是绿色
    2: 'danger',  // 已拒绝 - 红色
    3: 'success', // 已录用 - 绿色
    4: 'danger'   // 已拒绝录用 - 红色
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

// 审核解绑
const handleAuditUnbind = (row) => {
  currentUnbindApply.value = row
  unbindAuditForm.auditStatus = 2 // 默认通过
  unbindAuditForm.auditOpinion = ''
  unbindAuditDialogVisible.value = true
}

// 提交解绑审核
const handleSubmitUnbindAudit = async () => {
  if (!unbindAuditFormRef.value) return
  await unbindAuditFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!currentUnbindApply.value || !currentUnbindApply.value.applyId) {
      ElMessage.error('申请ID不存在')
      return
    }
    
    unbindAuditLoading.value = true
    try {
      const res = await applyApi.auditUnbind(
        currentUnbindApply.value.applyId,
        unbindAuditForm.auditStatus,
        unbindAuditForm.auditOpinion || undefined
      )
      if (res.code === 200) {
        ElMessage.success('审核成功')
        unbindAuditDialogVisible.value = false
        loadData()
      }
    } catch (error) {
      console.error('审核解绑失败:', error)
      ElMessage.error(error.response?.data?.message || '审核解绑失败')
    } finally {
      unbindAuditLoading.value = false
    }
  })
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

