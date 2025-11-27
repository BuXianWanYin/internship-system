<template>
  <PageLayout title="岗位管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        发布岗位
      </el-button>
    </template>

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
            <el-option label="已发布" :value="3" />
            <el-option label="已关闭" :value="4" />
          </el-select>
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
      <el-table-column prop="postCode" label="岗位编号" min-width="120" />
      <el-table-column prop="workLocation" label="工作地点" min-width="150" />
      <el-table-column prop="recruitCount" label="招聘人数" width="100" align="center" />
      <el-table-column prop="appliedCount" label="已申请" width="100" align="center" />
      <el-table-column prop="acceptedCount" label="已录用" width="100" align="center" />
      <el-table-column label="薪资范围" width="150" align="center">
        <template #default="{ row }">
          <span v-if="row.salaryMin && row.salaryMax">
            {{ row.salaryMin }}-{{ row.salaryMax }}元/{{ row.salaryType || '月' }}
          </span>
          <span v-else-if="row.salaryType === '面议'">面议</span>
          <span v-else style="color: #909399">未设置</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="180">
        <template #default="{ row }">
          {{ row.publishTime ? formatDateTime(row.publishTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT'])" 
            link 
            type="primary" 
            size="small" 
            @click="handleView(row)"
          >
            查看
          </el-button>
          <el-button
            v-if="row.status === 0 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="success"
            size="small"
            @click="handleAudit(row, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="row.status === 0 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="danger"
            size="small"
            @click="handleAudit(row, 2)"
          >
            拒绝
          </el-button>
          <el-button
            v-if="row.status !== 3 && row.status !== 4 && hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])"
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 1 && hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])"
            link
            type="warning"
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="row.status === 3 && hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])"
            link
            type="info"
            size="small"
            @click="handleClose(row)"
          >
            关闭
          </el-button>
          <el-button
            v-if="row.status !== 3 && hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])"
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
            <el-form-item label="岗位名称" prop="postName">
              <el-input v-model="formData.postName" placeholder="请输入岗位名称" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="岗位编号" prop="postCode">
              <el-input v-model="formData.postCode" placeholder="请输入岗位编号" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="岗位描述" prop="postDescription">
          <el-input
            v-model="formData.postDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入岗位描述"
          />
        </el-form-item>
        <el-form-item label="技能要求" prop="skillRequirements">
          <el-input
            v-model="formData.skillRequirements"
            type="textarea"
            :rows="4"
            placeholder="请输入技能要求"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工作地点" prop="workLocation">
              <el-input v-model="formData.workLocation" placeholder="请输入工作地点" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="详细地址" prop="workAddress">
              <el-input v-model="formData.workAddress" placeholder="请输入详细地址" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="招聘人数" prop="recruitCount">
              <el-input-number
                v-model="formData.recruitCount"
                :min="1"
                :max="1000"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资类型" prop="salaryType">
              <el-select v-model="formData.salaryType" placeholder="请选择薪资类型" style="width: 100%">
                <el-option label="月薪" value="月薪" />
                <el-option label="日薪" value="日薪" />
                <el-option label="时薪" value="时薪" />
                <el-option label="面议" value="面议" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="formData.salaryType && formData.salaryType !== '面议'">
          <el-col :span="24">
            <el-form-item label="薪资范围">
              <div style="display: flex; align-items: center; gap: 12px; width: 100%">
                <el-input-number
                  v-model="formData.salaryMin"
                  :min="0"
                  :precision="2"
                  placeholder="最低薪资"
                  style="flex: 1; max-width: 300px"
                />
                <span style="color: #909399; font-size: 14px; white-space: nowrap">-</span>
                <el-input-number
                  v-model="formData.salaryMax"
                  :min="0"
                  :precision="2"
                  placeholder="最高薪资"
                  style="flex: 1; max-width: 300px"
                />
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="实习开始日期" prop="internshipStartDate">
              <el-date-picker
                v-model="formData.internshipStartDate"
                type="date"
                placeholder="请选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实习结束日期" prop="internshipEndDate">
              <el-date-picker
                v-model="formData.internshipEndDate"
                type="date"
                placeholder="请选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工作时间" prop="workHours">
          <el-input v-model="formData.workHours" placeholder="如：周一至周五 9:00-18:00" style="width: 100%" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="formData.contactPerson" placeholder="请输入联系人" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="岗位详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="岗位名称">{{ detailData.postName }}</el-descriptions-item>
        <el-descriptions-item label="岗位编号">{{ detailData.postCode }}</el-descriptions-item>
        <el-descriptions-item label="工作地点">{{ detailData.workLocation }}</el-descriptions-item>
        <el-descriptions-item label="详细地址">{{ detailData.workAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ detailData.recruitCount }}</el-descriptions-item>
        <el-descriptions-item label="已申请人数">{{ detailData.appliedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="已录用人数">{{ detailData.acceptedCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
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
        <el-descriptions-item label="发布时间" :span="2">
          {{ detailData.publishTime ? formatDateTime(detailData.publishTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="岗位描述" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.postDescription || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="技能要求" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.skillRequirements || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="detailData.status === 0 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
          type="success"
          @click="handleAuditFromDetail(1)"
        >
          通过
        </el-button>
        <el-button
          v-if="detailData.status === 0 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
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
        <el-form-item label="岗位信息">
          <div style="padding: 10px; background: #f5f7fa; border-radius: 4px">
            <div><strong>岗位名称：</strong>{{ currentPost.postName }}</div>
            <div style="margin-top: 5px"><strong>岗位编号：</strong>{{ currentPost.postCode }}</div>
            <div style="margin-top: 5px"><strong>企业：</strong>{{ currentPost.enterpriseName || '-' }}</div>
            <div style="margin-top: 5px"><strong>工作地点：</strong>{{ currentPost.workLocation || '-' }}</div>
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
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { hasAnyRole } from '@/utils/permission'
import { postApi } from '@/api/internship/post'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'
import { enterpriseApi } from '@/api/user/enterprise'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('发布岗位')
const formRef = ref(null)

const searchForm = reactive({
  postName: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  postId: null,
  enterpriseId: null,
  postName: '',
  postCode: '',
  postDescription: '',
  skillRequirements: '',
  workLocation: '',
  workAddress: '',
  recruitCount: 1,
  salaryMin: null,
  salaryMax: null,
  salaryType: '月薪',
  internshipStartDate: '',
  internshipEndDate: '',
  workHours: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: ''
})

const detailData = ref({})

// 审核相关
const auditDialogVisible = ref(false)
const auditDialogTitle = ref('审核岗位')
const auditLoading = ref(false)
const auditFormRef = ref(null)
const currentPost = ref({})

const auditForm = reactive({
  auditStatus: 1, // 1-通过，2-拒绝
  auditOpinion: ''
})

const auditFormRules = {
  auditOpinion: [
    {
      validator: (rule, value, callback) => {
        if (auditForm.auditStatus === 2 && (!value || value.trim() === '')) {
          callback(new Error('拒绝时必须填写拒绝原因'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formRules = {
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  postCode: [{ required: true, message: '请输入岗位编号', trigger: 'blur' }],
  workLocation: [{ required: true, message: '请输入工作地点', trigger: 'blur' }],
  recruitCount: [{ required: true, message: '请输入招聘人数', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await postApi.getPostPage({
      current: pagination.current,
      size: pagination.size,
      postName: searchForm.postName || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
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
  searchForm.status = null
  handleSearch()
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '发布岗位'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑岗位'
  Object.assign(formData, {
    postId: row.postId,
    enterpriseId: row.enterpriseId || null,
    postName: row.postName,
    postCode: row.postCode,
    postDescription: row.postDescription || '',
    skillRequirements: row.skillRequirements || '',
    workLocation: row.workLocation,
    workAddress: row.workAddress || '',
    recruitCount: row.recruitCount || 1,
    salaryMin: row.salaryMin || null,
    salaryMax: row.salaryMax || null,
    salaryType: row.salaryType || '月薪',
    internshipStartDate: row.internshipStartDate || '',
    internshipEndDate: row.internshipEndDate || '',
    workHours: row.workHours || '',
    contactPerson: row.contactPerson || '',
    contactPhone: row.contactPhone || '',
    contactEmail: row.contactEmail || ''
  })
  dialogVisible.value = true
}

// 查看
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

// 发布
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该岗位吗？', '提示', {
      type: 'warning'
    })
    const res = await postApi.publishPost(row.postId)
    if (res.code === 200) {
      ElMessage.success('发布成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布失败:', error)
      ElMessage.error(error.response?.data?.message || '发布失败')
    }
  }
}

// 关闭
const handleClose = async (row) => {
  try {
    await ElMessageBox.confirm('确定要关闭该岗位吗？', '提示', {
      type: 'warning'
    })
    const res = await postApi.closePost(row.postId)
    if (res.code === 200) {
      ElMessage.success('关闭成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭失败:', error)
      ElMessage.error(error.response?.data?.message || '关闭失败')
    }
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该岗位吗？', '提示', {
      type: 'warning'
    })
    const res = await postApi.deletePost(row.postId)
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

// 审核岗位
const handleAudit = (row, auditStatus) => {
  currentPost.value = { ...row }
  auditForm.auditStatus = auditStatus
  auditForm.auditOpinion = ''
  auditDialogTitle.value = auditStatus === 1 ? '审核通过' : '审核拒绝'
  auditDialogVisible.value = true
}

// 从详情对话框审核
const handleAuditFromDetail = (auditStatus) => {
  currentPost.value = { ...detailData.value }
  auditForm.auditStatus = auditStatus
  auditForm.auditOpinion = ''
  auditDialogTitle.value = auditStatus === 1 ? '审核通过' : '审核拒绝'
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
        const res = await postApi.auditPost(
          currentPost.value.postId,
          auditForm.auditStatus,
          auditForm.auditOpinion || undefined
        )
        if (res.code === 200) {
          ElMessage.success(auditForm.auditStatus === 1 ? '审核通过' : '审核拒绝')
          auditDialogVisible.value = false
          loadData()
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

// 提交表单
const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 如果是新增岗位，需要获取当前用户的企业ID
        if (!formData.postId) {
          const authStore = useAuthStore()
          const userId = authStore.userInfo?.userId
          if (!userId) {
            ElMessage.error('无法获取当前用户信息')
            submitLoading.value = false
            return
          }
          
          // 获取当前用户的企业信息
          const enterpriseRes = await enterpriseApi.getEnterpriseByUserId(userId)
          if (enterpriseRes.code === 200 && enterpriseRes.data) {
            formData.enterpriseId = enterpriseRes.data.enterpriseId
          } else {
            ElMessage.error('无法获取企业信息，请确认您已关联企业')
            submitLoading.value = false
            return
          }
        }
        
        let res
        if (formData.postId) {
          res = await postApi.updatePost(formData)
        } else {
          res = await postApi.addPost(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.postId ? '更新成功' : '发布成功')
          dialogVisible.value = false
          loadData()
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

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    postId: null,
    enterpriseId: null,
    postName: '',
    postCode: '',
    postDescription: '',
    skillRequirements: '',
    workLocation: '',
    workAddress: '',
    recruitCount: 1,
    salaryMin: null,
    salaryMax: null,
    salaryType: '月薪',
    internshipStartDate: '',
    internshipEndDate: '',
    workHours: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 分页大小变化
const handleSizeChange = () => {
  loadData()
}

// 页码变化
const handlePageChange = () => {
  loadData()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝',
    3: '已发布',
    4: '已关闭'
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
    4: 'info'
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
</style>

