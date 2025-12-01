<template>
  <PageLayout title="企业管理">
    <template #actions>
      <el-button 
        v-if="hasRole('ROLE_SYSTEM_ADMIN')" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加企业
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="企业名称">
          <el-input
            v-model="searchForm.enterpriseName"
            placeholder="请输入企业名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="企业代码">
          <el-input
            v-model="searchForm.enterpriseCode"
            placeholder="请输入企业代码"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select
            v-model="searchForm.auditStatus"
            placeholder="请选择审核状态"
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
          <el-button 
            type="success" 
            :icon="Download" 
            @click="handleExportCooperationReport"
            :loading="exportLoading"
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN'])"
          >
            导出合作情况表
          </el-button>
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
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" />
      <el-table-column prop="enterpriseCode" label="企业代码" min-width="120" />
      <el-table-column label="企业类型" width="120" align="center">
        <template #default="{ row }">
          <el-tag v-if="isSelfEnterprise(row)" type="warning" size="small">自主实习</el-tag>
          <el-tag v-else type="success" size="small">合作企业</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" min-width="180" show-overflow-tooltip />
      <el-table-column prop="legalPerson" label="法人代表" min-width="120" />
      <el-table-column prop="industry" label="所属行业" min-width="120" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
      <el-table-column prop="auditStatus" label="审核状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.auditStatus === 0" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="row.auditStatus === 1" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="row.auditStatus === 2" type="danger" size="small">已拒绝</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="360" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN'])" 
            link 
            type="primary" 
            size="small" 
            @click="handleView(row)"
          >
            查看
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN'])" 
            link 
            type="primary" 
            size="small" 
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])" 
            link 
            type="primary" 
            size="small" 
            @click="handleManageCooperation(row)"
          >
            管理合作关系
          </el-button>
          <el-button
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']) && row.status === 1" 
            link
            type="danger" 
            size="small"
            @click="handleDelete(row)"
          >
            停用
          </el-button>
          <el-button
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']) && row.status === 0" 
            link
            type="success" 
            size="small"
            @click="handleEnable(row)"
          >
            启用
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input v-model="formData.enterpriseName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业代码" prop="enterpriseCode">
              <el-input v-model="formData.enterpriseCode" placeholder="请输入企业代码" :disabled="isEdit" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input v-model="formData.unifiedSocialCreditCode" placeholder="请输入统一社会信用代码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人代表" prop="legalPerson">
              <el-input v-model="formData.legalPerson" placeholder="请输入法人代表" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="注册资本（万元）" prop="registeredCapital">
              <el-input-number
                v-model="formData.registeredCapital"
                :min="0"
                :precision="2"
                placeholder="请输入注册资本"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-input v-model="formData.industry" placeholder="请输入所属行业" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="企业地址" prop="address">
          <el-input v-model="formData.address" placeholder="请输入企业地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input v-model="formData.contactPerson" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input v-model="formData.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业规模" prop="enterpriseScale">
              <el-select v-model="formData.enterpriseScale" placeholder="请选择企业规模" style="width: 100%">
                <el-option label="大型企业（500人以上）" value="大型企业" />
                <el-option label="中型企业（100-500人）" value="中型企业" />
                <el-option label="小型企业（20-100人）" value="小型企业" />
                <el-option label="微型企业（20人以下）" value="微型企业" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="经营范围" prop="businessScope">
          <el-input
            v-model="formData.businessScope"
            type="textarea"
            :rows="3"
            placeholder="请输入经营范围"
          />
        </el-form-item>
        <!-- 企业管理员信息（仅添加时显示） -->
        <template v-if="!isEdit">
          <el-divider content-position="left">企业管理员信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="管理员姓名" prop="adminName">
                <el-input v-model="formData.adminName" placeholder="请输入企业管理员姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="管理员手机号" prop="adminPhone">
                <el-input v-model="formData.adminPhone" placeholder="请输入企业管理员手机号" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="管理员邮箱" prop="adminEmail">
                <el-input v-model="formData.adminEmail" placeholder="请输入企业管理员邮箱" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="初始密码" prop="adminPassword">
                <el-input v-model="formData.adminPassword" type="password" placeholder="请输入企业管理员初始密码" show-password />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
        <el-form-item v-if="!isEdit" label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>


    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="企业详情"
      width="700px"
    >
      <el-descriptions v-if="currentEnterprise" :column="2" border>
        <el-descriptions-item label="企业名称">{{ currentEnterprise.enterpriseName }}</el-descriptions-item>
        <el-descriptions-item label="企业代码">{{ currentEnterprise.enterpriseCode }}</el-descriptions-item>
        <el-descriptions-item label="统一社会信用代码">{{ currentEnterprise.unifiedSocialCreditCode || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="法人代表">{{ currentEnterprise.legalPerson || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="注册资本">{{ currentEnterprise.registeredCapital ? currentEnterprise.registeredCapital + ' 万元' : '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="所属行业">{{ currentEnterprise.industry || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="企业地址" :span="2">{{ currentEnterprise.address || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ currentEnterprise.contactPerson || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentEnterprise.contactPhone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系邮箱">{{ currentEnterprise.contactEmail || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="企业规模">{{ currentEnterprise.enterpriseScale || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="经营范围" :span="2">{{ currentEnterprise.businessScope || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="审核状态">
          <el-tag v-if="currentEnterprise.auditStatus === 0" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="currentEnterprise.auditStatus === 1" type="success" size="small">已通过</el-tag>
          <el-tag v-else-if="currentEnterprise.auditStatus === 2" type="danger" size="small">已拒绝</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentEnterprise.status === 1 ? 'success' : 'info'" size="small">
            {{ currentEnterprise.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" :span="2">{{ currentEnterprise.auditOpinion || '无' }}</el-descriptions-item>
        <el-descriptions-item label="合作院校" :span="2">
          <span v-if="currentEnterprise.cooperationSchools && currentEnterprise.cooperationSchools.length > 0">
            {{ currentEnterprise.cooperationSchools.map(s => s.schoolName).join('、') }}
          </span>
          <span v-else style="color: #909399">暂无</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentEnterprise.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="审核时间">{{ currentEnterprise.auditTime ? formatDateTime(currentEnterprise.auditTime) : '未审核' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 合作关系管理对话框 -->
    <el-dialog
      v-model="cooperationDialogVisible"
      title="合作关系管理"
      width="900px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 20px">
        <el-button type="primary" :icon="Plus" @click="handleAddCooperation">添加合作关系</el-button>
      </div>
      
      <el-table
        v-loading="cooperationLoading"
        :data="cooperationList"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        empty-text="暂无数据"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="合作学校" min-width="150">
          <template #default="{ row }">
            {{ row.schoolName || schoolMap[row.schoolId]?.schoolName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="cooperationType" label="合作类型" min-width="120" />
        <el-table-column prop="cooperationStatus" label="合作状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.cooperationStatus === 1" type="success" size="small">进行中</el-tag>
            <el-tag v-else-if="row.cooperationStatus === 2" type="info" size="small">已结束</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" width="180">
          <template #default="{ row }">
            {{ row.startTime ? formatDateTime(row.startTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="cooperationDesc" label="合作描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEditCooperation(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDeleteCooperation(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加/编辑合作关系对话框 -->
    <el-dialog
      v-model="cooperationFormDialogVisible"
      :title="cooperationFormTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="cooperationFormRef"
        :model="cooperationFormData"
        :rules="cooperationFormRules"
        label-width="120px"
      >
        <el-form-item label="合作学校" prop="schoolId">
          <el-select
            v-model="cooperationFormData.schoolId"
            placeholder="请选择合作学校"
            style="width: 100%"
            :disabled="cooperationIsEdit"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="合作类型" prop="cooperationType">
          <el-select
            v-model="cooperationFormData.cooperationType"
            placeholder="请选择合作类型"
            style="width: 100%"
          >
            <el-option label="实习基地" value="实习基地" />
            <el-option label="校企合作" value="校企合作" />
            <el-option label="人才培养" value="人才培养" />
            <el-option label="产学研合作" value="产学研合作" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态" prop="cooperationStatus">
          <el-radio-group v-model="cooperationFormData.cooperationStatus">
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="2">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker
            v-model="cooperationFormData.startTime"
            type="datetime"
            placeholder="请选择开始时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker
            v-model="cooperationFormData.endTime"
            type="datetime"
            placeholder="请选择结束时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="合作描述" prop="cooperationDesc">
          <el-input
            v-model="cooperationFormData.cooperationDesc"
            type="textarea"
            :rows="4"
            placeholder="请输入合作描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cooperationFormDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="cooperationSubmitting" @click="handleSubmitCooperation">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole, hasRole } from '@/utils/permission'
import { enterpriseApi } from '@/api/user/enterprise'
import { cooperationApi } from '@/api/cooperation'
import { schoolApi } from '@/api/system/school'
import { reportApi } from '@/api/report'
import { formatDateTime } from '@/utils/dateUtils'
import { exportExcel } from '@/utils/exportUtils'

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const auditing = ref(false)
const exportLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  enterpriseName: '',
  enterpriseCode: '',
  auditStatus: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const dialogTitle = ref('添加企业')
const isEdit = ref(false)
const formRef = ref(null)
const currentEnterprise = ref(null)

// 表单数据
const formData = reactive({
  enterpriseId: null,
  enterpriseName: '',
  enterpriseCode: '',
  unifiedSocialCreditCode: '',
  legalPerson: '',
  registeredCapital: null,
  industry: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  enterpriseScale: '',
  businessScope: '',
  status: 1,
  // 企业管理员信息（仅添加时使用）
  adminName: '',
  adminPhone: '',
  adminEmail: '',
  adminPassword: ''
})

// 审核表单

// 表单验证规则（动态规则，根据isEdit判断）
const getFormRules = () => {
  const rules = {
    enterpriseName: [
      { required: true, message: '请输入企业名称', trigger: 'blur' }
    ],
    enterpriseCode: [
      { required: true, message: '请输入企业代码', trigger: 'blur' }
    ],
    contactEmail: [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ],
    status: [
      { required: true, message: '请选择状态', trigger: 'change' }
    ]
  }
  
  // 仅在添加模式下验证企业管理员信息
  if (!isEdit.value) {
    rules.adminName = [
      { required: true, message: '请输入企业管理员姓名', trigger: 'blur' }
    ]
    rules.adminPhone = [
      { required: true, message: '请输入企业管理员手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ]
    rules.adminEmail = [
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
    rules.adminPassword = [
      { required: true, message: '请输入企业管理员初始密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
    ]
  }
  
  return rules
}

const formRules = computed(() => getFormRules())

// 判断是否为自主实习企业（通过企业代码判断）
const isSelfEnterprise = (row) => {
  return row.enterpriseCode && row.enterpriseCode.startsWith('SELF_')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await enterpriseApi.getEnterprisePage({
      current: pagination.current,
      size: pagination.size,
      enterpriseName: searchForm.enterpriseName || undefined,
      enterpriseCode: searchForm.enterpriseCode || undefined,
      auditStatus: searchForm.auditStatus !== null ? searchForm.auditStatus : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 为每个企业加载注册申请的院校列表
      for (const enterprise of tableData.value) {
        try {
          // 加载合作院校信息
          const schoolRes = await enterpriseApi.getCooperationSchoolsByEnterpriseId(enterprise.enterpriseId)
          if (schoolRes.code === 200) {
            enterprise.cooperationSchools = schoolRes.data || []
          }
        } catch (error) {
          console.error(`加载企业 ${enterprise.enterpriseId} 的信息失败:`, error)
          enterprise.cooperationSchools = []
        }
      }
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
  searchForm.enterpriseName = ''
  searchForm.enterpriseCode = ''
  searchForm.auditStatus = null
  pagination.current = 1
  loadData()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await enterpriseApi.getEnterpriseById(row.enterpriseId)
    if (res.code === 200) {
      currentEnterprise.value = res.data
      
      // 加载合作院校信息（后端根据权限过滤）
      try {
        const schoolRes = await enterpriseApi.getCooperationSchoolsByEnterpriseId(row.enterpriseId)
        if (schoolRes.code === 200) {
          currentEnterprise.value.cooperationSchools = schoolRes.data || []
        }
      } catch (error) {
        console.error('加载合作院校失败:', error)
        currentEnterprise.value.cooperationSchools = []
      }
      
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取企业详情失败')
  }
}

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加企业'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑企业'
  try {
    const res = await enterpriseApi.getEnterpriseById(row.enterpriseId)
    if (res.code === 200) {
      const enterprise = res.data
      Object.assign(formData, {
        enterpriseId: enterprise.enterpriseId,
        enterpriseName: enterprise.enterpriseName,
        enterpriseCode: enterprise.enterpriseCode,
        unifiedSocialCreditCode: enterprise.unifiedSocialCreditCode || '',
        legalPerson: enterprise.legalPerson || '',
        registeredCapital: enterprise.registeredCapital,
        industry: enterprise.industry || '',
        address: enterprise.address || '',
        contactPerson: enterprise.contactPerson || '',
        contactPhone: enterprise.contactPhone || '',
        contactEmail: enterprise.contactEmail || '',
        enterpriseScale: enterprise.enterpriseScale || '',
        businessScope: enterprise.businessScope || '',
        status: enterprise.status
      })
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取企业详情失败')
  }
}

// 重置表单
const resetFormData = () => {
  Object.assign(formData, {
    enterpriseId: null,
    enterpriseName: '',
    enterpriseCode: '',
    unifiedSocialCreditCode: '',
    legalPerson: '',
    registeredCapital: null,
    industry: '',
    address: '',
    contactPerson: '',
    contactPhone: '',
    contactEmail: '',
    enterpriseScale: '',
    businessScope: '',
    status: 1,
    // 企业管理员信息
    adminName: '',
    adminPhone: '',
    adminEmail: '',
    adminPassword: ''
  })
  formRef.value?.clearValidate()
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑
        const res = await enterpriseApi.updateEnterprise(formData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        // 添加（使用新的DTO格式，包含企业管理员信息）
        const submitData = {
          enterprise: {
            enterpriseName: formData.enterpriseName,
            enterpriseCode: formData.enterpriseCode,
            unifiedSocialCreditCode: formData.unifiedSocialCreditCode,
            legalPerson: formData.legalPerson,
            registeredCapital: formData.registeredCapital,
            industry: formData.industry,
            address: formData.address,
            contactPerson: formData.contactPerson,
            contactPhone: formData.contactPhone,
            contactEmail: formData.contactEmail,
            enterpriseScale: formData.enterpriseScale,
            businessScope: formData.businessScope,
            status: formData.status
          },
          adminName: formData.adminName,
          adminPhone: formData.adminPhone,
          adminEmail: formData.adminEmail,
          adminPassword: formData.adminPassword
        }
        const res = await enterpriseApi.addEnterprise(submitData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadData()
        }
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      submitting.value = false
    }
  })
}


// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该企业吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await enterpriseApi.deleteEnterprise(row.enterpriseId)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      loadData()
    } else {
      ElMessage.error(res.message || '停用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停用失败：' + (error.message || '未知错误'))
    }
  }
}

// 启用
const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm('确定要启用该企业吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await enterpriseApi.enableEnterprise(row.enterpriseId)
    if (res.code === 200) {
      ElMessage.success('启用成功')
      loadData()
    } else {
      ElMessage.error(res.message || '启用失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('启用失败：' + (error.message || '未知错误'))
    }
  }
}

// 合作关系管理
const cooperationDialogVisible = ref(false)
const cooperationLoading = ref(false)
const cooperationList = ref([])
const cooperationFormDialogVisible = ref(false)
const cooperationFormTitle = ref('添加合作关系')
const cooperationIsEdit = ref(false)
const cooperationFormRef = ref(null)
const cooperationSubmitting = ref(false)
const schoolList = ref([])
const schoolMap = ref({})
const currentEnterpriseForCooperation = ref(null)

// 合作关系管理表单数据
const cooperationFormData = reactive({
  id: null,
  enterpriseId: null,
  schoolId: null,
  cooperationType: '',
  cooperationStatus: 1,
  startTime: null,
  endTime: null,
  cooperationDesc: ''
})

// 合作关系管理表单验证规则
const cooperationFormRules = {
  schoolId: [
    { required: true, message: '请选择合作学校', trigger: 'change' }
  ],
  cooperationType: [
    { required: true, message: '请选择合作类型', trigger: 'change' }
  ],
  cooperationStatus: [
    { required: true, message: '请选择合作状态', trigger: 'change' }
  ]
}

// 管理合作关系
const handleManageCooperation = async (row) => {
  currentEnterpriseForCooperation.value = row
  cooperationDialogVisible.value = true
  await loadCooperationList(row.enterpriseId)
  await loadSchoolList()
}

// 加载合作关系列表
const loadCooperationList = async (enterpriseId) => {
  cooperationLoading.value = true
  try {
    const res = await cooperationApi.getCooperationListByEnterpriseId(enterpriseId)
    if (res.code === 200) {
      cooperationList.value = res.data || []
      // 加载学校信息
      const schoolIds = cooperationList.value.map(item => item.schoolId).filter(Boolean)
      if (schoolIds.length > 0) {
        await loadSchoolInfo(schoolIds)
      }
    }
  } catch (error) {
    console.error('加载合作关系列表失败:', error)
    ElMessage.error('加载合作关系列表失败')
  } finally {
    cooperationLoading.value = false
  }
}

// 加载学校列表
const loadSchoolList = async () => {
  try {
    const res = await schoolApi.getSchoolPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      schoolList.value = res.data.records || []
      // 构建学校Map
      schoolList.value.forEach(school => {
        schoolMap.value[school.schoolId] = school
      })
    }
  } catch (error) {
    console.error('加载学校列表失败:', error)
  }
}

// 加载学校信息
const loadSchoolInfo = async (schoolIds) => {
  try {
    for (const schoolId of schoolIds) {
      if (!schoolMap.value[schoolId]) {
        const res = await schoolApi.getSchoolById(schoolId)
        if (res.code === 200 && res.data) {
          schoolMap.value[schoolId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载学校信息失败:', error)
  }
}

// 添加合作关系
const handleAddCooperation = () => {
  cooperationIsEdit.value = false
  cooperationFormTitle.value = '添加合作关系'
  resetCooperationForm()
  cooperationFormDialogVisible.value = true
}

// 编辑合作关系
const handleEditCooperation = (row) => {
  cooperationIsEdit.value = true
  cooperationFormTitle.value = '编辑合作关系'
  Object.assign(cooperationFormData, {
    id: row.id,
    enterpriseId: row.enterpriseId,
    schoolId: row.schoolId,
    cooperationType: row.cooperationType || '',
    cooperationStatus: row.cooperationStatus || 1,
    startTime: row.startTime || null,
    endTime: row.endTime || null,
    cooperationDesc: row.cooperationDesc || ''
  })
  cooperationFormDialogVisible.value = true
}

// 删除合作关系
const handleDeleteCooperation = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该合作关系吗？', '提示', {
      type: 'warning'
    })
    
    if (!row.id) {
      ElMessage.warning('合作关系信息不完整，无法删除')
      return
    }
    
    const res = await cooperationApi.deleteCooperation(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await loadCooperationList(currentEnterpriseForCooperation.value.enterpriseId)
      // 刷新企业列表的合作院校信息
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 提交合作关系表单
const handleSubmitCooperation = async () => {
  if (!cooperationFormRef.value) return
  
  await cooperationFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    cooperationSubmitting.value = true
    try {
      // 设置企业ID
      cooperationFormData.enterpriseId = currentEnterpriseForCooperation.value.enterpriseId
      
      if (cooperationIsEdit.value) {
        // 编辑
        const res = await cooperationApi.updateCooperation(cooperationFormData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          cooperationFormDialogVisible.value = false
          await loadCooperationList(currentEnterpriseForCooperation.value.enterpriseId)
          // 刷新企业列表的合作院校信息
          loadData()
        }
      } else {
        // 添加
        const res = await cooperationApi.addCooperation(cooperationFormData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          cooperationFormDialogVisible.value = false
          await loadCooperationList(currentEnterpriseForCooperation.value.enterpriseId)
          // 刷新企业列表的合作院校信息
          loadData()
        }
      }
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '操作失败')
    } finally {
      cooperationSubmitting.value = false
    }
  })
}

// 重置合作关系表单
const resetCooperationForm = () => {
  Object.assign(cooperationFormData, {
    id: null,
    enterpriseId: null,
    schoolId: null,
    cooperationType: '',
    cooperationStatus: 1,
    startTime: null,
    endTime: null,
    cooperationDesc: ''
  })
  cooperationFormRef.value?.clearValidate()
}

// 导出企业合作情况表
const handleExportCooperationReport = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数，使用当前页面的筛选条件
    const params = {
      enterpriseName: searchForm.enterpriseName || undefined,
      enterpriseCode: searchForm.enterpriseCode || undefined,
      auditStatus: searchForm.auditStatus !== null ? searchForm.auditStatus : undefined
    }
    
    await exportExcel(
      reportApi.exportEnterpriseCooperationReport,
      params,
      '企业合作情况表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
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
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.school-audit-status {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.enterprise-info {
  margin-bottom: 16px;
}

.enterprise-info h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.enterprise-info p {
  margin: 4px 0;
  color: #606266;
}

.school-audit-list h4 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
}
</style>

