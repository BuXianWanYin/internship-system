<template>
  <PageLayout title="企业管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])" 
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
      <el-table-column prop="unifiedSocialCreditCode" label="统一社会信用代码" min-width="180" show-overflow-tooltip />
      <el-table-column prop="legalPerson" label="法人代表" min-width="120" />
      <el-table-column prop="industry" label="所属行业" min-width="120" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
      <el-table-column label="意向合作院校审核状态" min-width="300">
        <template #default="{ row }">
          <div v-if="row.registerSchools && row.registerSchools.length > 0" class="school-audit-status">
            <el-tag
              v-for="school in row.registerSchools"
              :key="school.id"
              :type="school.auditStatus === 1 ? 'success' : school.auditStatus === 2 ? 'danger' : 'warning'"
              size="small"
              style="margin-right: 8px; margin-bottom: 4px;"
            >
              {{ school.schoolName }}: {{ school.auditStatus === 0 ? '待审核' : school.auditStatus === 1 ? '已通过' : '已拒绝' }}
            </el-tag>
          </div>
          <span v-else style="color: #909399">暂无</span>
        </template>
      </el-table-column>
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
            type="info" 
            size="small" 
            @click="handleManageCooperation(row)"
          >
            管理合作关系
          </el-button>
          <el-button
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="primary"
            size="small"
            @click="handleAuditBySchool(row)"
          >
            按院校审核
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])" 
            link 
            type="danger" 
            size="small" 
            @click="handleDelete(row)"
          >
            停用
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
              <el-input v-model="formData.enterpriseScale" placeholder="请输入企业规模" />
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

    <!-- 按院校审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="按院校审核企业注册申请"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentEnterprise" class="enterprise-info">
        <h4>企业信息</h4>
        <p>企业名称：{{ currentEnterprise.enterpriseName }}</p>
        <p>企业代码：{{ currentEnterprise.enterpriseCode }}</p>
      </div>
      <el-divider />
      <div class="school-audit-list">
        <h4>意向合作院校审核</h4>
        <el-table
          :data="currentRegisterSchools"
          border
          style="width: 100%"
        >
          <el-table-column prop="schoolName" label="院校名称" width="200" />
          <el-table-column label="审核状态" width="120" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.auditStatus === 0" type="warning" size="small">待审核</el-tag>
              <el-tag v-else-if="row.auditStatus === 1" type="success" size="small">已通过</el-tag>
              <el-tag v-else-if="row.auditStatus === 2" type="danger" size="small">已拒绝</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditOpinion" label="审核意见" min-width="150" show-overflow-tooltip />
          <el-table-column label="操作" width="200" align="center">
            <template #default="{ row }">
              <el-button
                v-if="row.auditStatus === 0"
                link
                type="success"
                size="small"
                @click="handleAuditSchool(row, 1)"
              >
                通过
              </el-button>
              <el-button
                v-if="row.auditStatus === 0"
                link
                type="danger"
                size="small"
                @click="handleAuditSchool(row, 2)"
              >
                拒绝
              </el-button>
              <span v-else style="color: #909399">已审核</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 审核意见输入对话框 -->
      <el-dialog
        v-model="auditOpinionDialogVisible"
        :title="currentAuditSchool && currentAuditSchool.auditStatus === 1 ? '审核通过' : '审核拒绝'"
        width="500px"
        append-to-body
      >
        <el-form
          ref="auditFormRef"
          :model="auditForm"
          label-width="100px"
        >
          <el-form-item label="院校名称">
            <span>{{ currentAuditSchool && currentAuditSchool.schoolName }}</span>
          </el-form-item>
          <el-form-item
            label="审核意见"
            :prop="auditForm.auditStatus === 1 ? '' : 'auditOpinion'"
          >
            <el-input
              v-model="auditForm.auditOpinion"
              type="textarea"
              :rows="4"
              :placeholder="auditForm.auditStatus === 1 ? '审核意见（可选）' : '请输入拒绝原因'"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="auditOpinionDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="auditing"
            @click="handleConfirmSchoolAudit"
          >
            确认
          </el-button>
        </template>
      </el-dialog>
      
      <template #footer>
        <el-button @click="auditDialogVisible = false">关闭</el-button>
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
      >
        <el-table-column label="合作学校" min-width="150">
          <template #default="{ row }">
            {{ schoolMap[row.schoolId]?.schoolName || '-' }}
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
          <el-input
            v-model="cooperationFormData.cooperationType"
            placeholder="请输入合作类型（如：实习基地、校企合作、人才培养等）"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole } from '@/utils/permission'
import { enterpriseApi } from '@/api/user/enterprise'
import { enterpriseRegisterSchoolApi } from '@/api/user/enterpriseRegisterSchool'
import { cooperationApi } from '@/api/cooperation'
import { schoolApi } from '@/api/system/school'
import { formatDateTime } from '@/utils/dateUtils'

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const auditing = ref(false)

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
const auditFormRef = ref(null)
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
  status: 1
})

// 审核表单
const auditForm = reactive({
  auditStatus: 1,
  auditOpinion: ''
})

// 表单验证规则
const formRules = {
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
          // 加载注册申请的院校列表
          const registerRes = await enterpriseRegisterSchoolApi.getByEnterpriseId(enterprise.enterpriseId)
          if (registerRes.code === 200) {
            const registerSchools = registerRes.data || []
            // 获取学校名称
            for (const school of registerSchools) {
              try {
                const schoolRes = await schoolApi.getSchoolById(school.schoolId)
                if (schoolRes.code === 200) {
                  school.schoolName = schoolRes.data.schoolName
                }
              } catch (error) {
                console.error(`加载学校 ${school.schoolId} 信息失败:`, error)
                school.schoolName = `学校ID: ${school.schoolId}`
              }
            }
            enterprise.registerSchools = registerSchools
          }
          
          // 加载合作院校信息
          const schoolRes = await enterpriseApi.getCooperationSchoolsByEnterpriseId(enterprise.enterpriseId)
          if (schoolRes.code === 200) {
            enterprise.cooperationSchools = schoolRes.data || []
          }
        } catch (error) {
          console.error(`加载企业 ${enterprise.enterpriseId} 的信息失败:`, error)
          enterprise.registerSchools = []
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
      
      // 加载合作院校信息
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
    status: 1
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
        // 添加
        const res = await enterpriseApi.addEnterprise(formData)
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

// 按院校审核
const currentRegisterSchools = ref([])
const auditOpinionDialogVisible = ref(false)
const currentAuditSchool = ref(null)

const handleAuditBySchool = async (row) => {
  currentEnterprise.value = row
  auditDialogVisible.value = true
  
  // 加载该企业的注册申请院校列表
  try {
    const res = await enterpriseRegisterSchoolApi.getByEnterpriseId(row.enterpriseId)
    if (res.code === 200) {
      // 获取学校名称
      const schools = res.data || []
      for (const school of schools) {
        try {
          const schoolRes = await schoolApi.getSchoolById(school.schoolId)
          if (schoolRes.code === 200) {
            school.schoolName = schoolRes.data.schoolName
          }
        } catch (error) {
          console.error(`加载学校 ${school.schoolId} 信息失败:`, error)
          school.schoolName = `学校ID: ${school.schoolId}`
        }
      }
      currentRegisterSchools.value = schools
    }
  } catch (error) {
    console.error('加载注册申请院校列表失败:', error)
    ElMessage.error('加载注册申请院校列表失败')
    currentRegisterSchools.value = []
  }
}

// 审核单个院校
const handleAuditSchool = (school, auditStatus) => {
  currentAuditSchool.value = school
  auditForm.auditStatus = auditStatus
  auditForm.auditOpinion = ''
  auditOpinionDialogVisible.value = true
}

// 确认院校审核
const handleConfirmSchoolAudit = async () => {
  if (auditForm.auditStatus === 2 && !auditForm.auditOpinion) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  auditing.value = true
  try {
    const res = await enterpriseRegisterSchoolApi.auditEnterpriseRegister(
      currentAuditSchool.value.id,
      auditForm.auditStatus,
      auditForm.auditOpinion
    )
    if (res.code === 200) {
      ElMessage.success('审核成功')
      auditOpinionDialogVisible.value = false
      // 刷新院校列表
      await handleAuditBySchool(currentEnterprise.value)
      // 刷新主列表
      loadData()
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败：' + (error.message || '未知错误'))
  } finally {
    auditing.value = false
  }
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

