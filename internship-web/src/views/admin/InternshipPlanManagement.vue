<template>
  <PageLayout title="实习计划管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        创建实习计划
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="计划名称">
          <el-input
            v-model="searchForm.planName"
            placeholder="请输入计划名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学期">
          <el-select
            v-model="searchForm.semesterId"
            placeholder="请选择学期"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="semester in semesterList"
              :key="semester.semesterId"
              :label="semester.semesterName"
              :value="semester.semesterId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学校">
          <el-select
            v-model="searchForm.schoolId"
            placeholder="请选择学校"
            clearable
            style="width: 200px"
            :disabled="isSchoolDisabled"
            @change="handleSchoolChange"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学院">
          <el-select
            v-model="searchForm.collegeId"
            placeholder="请选择学院"
            clearable
            style="width: 200px"
            :disabled="isCollegeDisabled || !searchForm.schoolId"
            @change="handleCollegeChange"
          >
            <el-option
              v-for="college in collegeList"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select
            v-model="searchForm.majorId"
            placeholder="请选择专业"
            clearable
            style="width: 200px"
            :disabled="!searchForm.collegeId"
          >
            <el-option
              v-for="major in majorList"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已通过" :value="2" />
            <el-option label="已拒绝" :value="3" />
            <el-option label="已发布" :value="4" />
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
      <el-table-column prop="planName" label="计划名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="planCode" label="计划编号" min-width="120" />
      <el-table-column prop="semesterName" label="学期" min-width="150" />
      <el-table-column prop="schoolName" label="所属学校" min-width="150" />
      <el-table-column prop="collegeName" label="所属学院" min-width="150" />
      <el-table-column prop="majorName" label="所属专业" min-width="150" />
      <el-table-column prop="planType" label="实习类型" min-width="100" />
      <el-table-column prop="startDate" label="开始日期" width="120">
        <template #default="{ row }">
          {{ formatDate(row.startDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="endDate" label="结束日期" width="120">
        <template #default="{ row }">
          {{ formatDate(row.endDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ getStatusText(row.status) }}
          </el-tag>
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
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'])" 
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
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.status === 0 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="success"
            size="small"
            @click="handleSubmit(row)"
          >
            提交审核
          </el-button>
          <el-button
            v-if="row.status === 1 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="success"
            size="small"
            @click="handleAudit(row)"
          >
            审核
          </el-button>
          <el-button
            v-if="row.status === 2 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
            link
            type="warning"
            size="small"
            @click="handlePublish(row)"
          >
            发布
          </el-button>
          <el-button
            v-if="row.status !== 4 && hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
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
            <el-form-item label="计划名称" prop="planName">
              <el-input v-model="formData.planName" placeholder="请输入计划名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划编号" prop="planCode">
              <el-input v-model="formData.planCode" placeholder="请输入计划编号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学期" prop="semesterId">
              <el-select
                v-model="formData.semesterId"
                placeholder="请选择学期"
                style="width: 100%"
              >
                <el-option
                  v-for="semester in semesterList"
                  :key="semester.semesterId"
                  :label="semester.semesterName"
                  :value="semester.semesterId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实习类型" prop="planType">
              <el-select v-model="formData.planType" placeholder="请选择实习类型" style="width: 100%">
                <el-option label="生产实习" value="生产实习" />
                <el-option label="毕业实习" value="毕业实习" />
                <el-option label="认知实习" value="认知实习" />
                <el-option label="专业实习" value="专业实习" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属学校" prop="schoolId">
              <el-select
                v-model="formData.schoolId"
                placeholder="请选择学校"
                style="width: 100%"
                @change="handleFormSchoolChange"
              >
                <el-option
                  v-for="school in schoolList"
                  :key="school.schoolId"
                  :label="school.schoolName"
                  :value="school.schoolId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select
                v-model="formData.collegeId"
                placeholder="请选择学院（可选）"
                clearable
                style="width: 100%"
                :disabled="!formData.schoolId"
                @change="handleFormCollegeChange"
              >
                <el-option
                  v-for="college in collegeList"
                  :key="college.collegeId"
                  :label="college.collegeName"
                  :value="college.collegeId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属专业" prop="majorId">
              <el-select
                v-model="formData.majorId"
                placeholder="请选择专业（可选）"
                clearable
                style="width: 100%"
                :disabled="!formData.collegeId"
              >
                <el-option
                  v-for="major in majorList"
                  :key="major.majorId"
                  :label="major.majorName"
                  :value="major.majorId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="formData.startDate"
                type="date"
                placeholder="请选择开始日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="formData.endDate"
                type="date"
                placeholder="请选择结束日期"
                style="width: 100%"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="实习大纲" prop="planOutline">
          <el-input
            v-model="formData.planOutline"
            type="textarea"
            :rows="4"
            placeholder="请输入实习大纲"
          />
        </el-form-item>
        <el-form-item label="任务要求" prop="taskRequirements">
          <el-input
            v-model="formData.taskRequirements"
            type="textarea"
            :rows="4"
            placeholder="请输入任务要求"
          />
        </el-form-item>
        <el-form-item label="考核标准" prop="assessmentStandards">
          <el-input
            v-model="formData.assessmentStandards"
            type="textarea"
            :rows="4"
            placeholder="请输入考核标准"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="实习计划详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划名称">{{ detailData.planName }}</el-descriptions-item>
        <el-descriptions-item label="计划编号">{{ detailData.planCode }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detailData.semesterName }}</el-descriptions-item>
        <el-descriptions-item label="实习类型">{{ detailData.planType }}</el-descriptions-item>
        <el-descriptions-item label="所属学校">{{ detailData.schoolName }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">{{ detailData.collegeName || '全校' }}</el-descriptions-item>
        <el-descriptions-item label="所属专业">{{ detailData.majorName || '全院' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detailData.status)" size="small">
            {{ getStatusText(detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ formatDate(detailData.startDate) }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ formatDate(detailData.endDate) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="实习大纲" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.planOutline }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="任务要求" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.taskRequirements }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="考核标准" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.assessmentStandards }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      title="审核实习计划"
      width="500px"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditFormRules"
        label-width="100px"
      >
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="auditForm.auditStatus">
            <el-radio :label="2">通过</el-radio>
            <el-radio :label="3">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="auditOpinion">
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="4"
            placeholder="请输入审核意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="handleAuditSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { hasAnyRole } from '@/utils/permission'
import { planApi } from '@/api/internship/plan'
import { semesterApi } from '@/api/system/semester'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { majorApi } from '@/api/system/major'
import { userApi } from '@/api/user/user'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const auditLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const auditDialogVisible = ref(false)
const dialogTitle = ref('创建实习计划')
const formRef = ref(null)
const auditFormRef = ref(null)

const searchForm = reactive({
  planName: '',
  semesterId: null,
  schoolId: null,
  collegeId: null,
  majorId: null,
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const semesterList = ref([])
const schoolList = ref([])
const collegeList = ref([])
const majorList = ref([])

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: '',
  collegeId: null,
  collegeName: ''
})

// 计算属性：学校下拉框是否禁用
const isSchoolDisabled = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
})

// 计算属性：学院下拉框是否禁用
const isCollegeDisabled = computed(() => {
  return hasAnyRole(['ROLE_COLLEGE_LEADER'])
})

const formData = reactive({
  planId: null,
  planName: '',
  planCode: '',
  semesterId: null,
  schoolId: null,
  collegeId: null,
  majorId: null,
  planType: '',
  startDate: '',
  endDate: '',
  planOutline: '',
  taskRequirements: '',
  assessmentStandards: '',
  status: 0
})

const detailData = ref({})

const auditForm = reactive({
  planId: null,
  auditStatus: 2,
  auditOpinion: ''
})

const formRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  planCode: [{ required: true, message: '请输入计划编号', trigger: 'blur' }],
  schoolId: [{ required: true, message: '请选择所属学校', trigger: 'change' }],
  planType: [{ required: true, message: '请输入实习类型', trigger: 'blur' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }]
}

const auditFormRules = {
  auditStatus: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await planApi.getPlanPage({
      current: pagination.current,
      size: pagination.size,
      planName: searchForm.planName || undefined,
      semesterId: searchForm.semesterId || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      majorId: searchForm.majorId || undefined,
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

// 加载学期列表
const loadSemesterList = async () => {
  try {
    const res = await semesterApi.getSemesterPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      semesterList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学期列表失败:', error)
  }
}

// 加载学校列表
const loadSchoolList = async () => {
  try {
    const res = await schoolApi.getSchoolPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      schoolList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学校列表失败:', error)
  }
}

// 加载学院列表
const loadCollegeList = async (schoolId) => {
  if (!schoolId) {
    collegeList.value = []
    return
  }
  try {
    const res = await collegeApi.getCollegePage({ current: 1, size: 1000, schoolId })
    if (res.code === 200) {
      collegeList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

// 加载专业列表
const loadMajorList = async (collegeId) => {
  if (!collegeId) {
    majorList.value = []
    return
  }
  try {
    const res = await majorApi.getMajorPage({ current: 1, size: 1000, collegeId })
    if (res.code === 200) {
      majorList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.planName = ''
  searchForm.semesterId = null
  searchForm.status = null
  
  // 根据角色重置筛选条件，保持组织信息的绑定
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])) {
    // 学校管理员、学院负责人：保持学校ID
    searchForm.schoolId = currentOrgInfo.value.schoolId || null
  } else {
    searchForm.schoolId = null
  }
  
  if (hasAnyRole(['ROLE_COLLEGE_LEADER'])) {
    // 学院负责人：保持学院ID
    searchForm.collegeId = currentOrgInfo.value.collegeId || null
  } else {
    searchForm.collegeId = null
  }
  
  searchForm.majorId = null
  
  // 重新加载列表
  if (searchForm.schoolId) {
    handleSchoolChange()
  } else {
    collegeList.value = []
    majorList.value = []
  }
  
  handleSearch()
}

// 学校变化
const handleSchoolChange = () => {
  searchForm.collegeId = null
  searchForm.majorId = null
  collegeList.value = []
  majorList.value = []
  if (searchForm.schoolId) {
    loadCollegeList(searchForm.schoolId)
  }
}

// 学院变化
const handleCollegeChange = () => {
  searchForm.majorId = null
  majorList.value = []
  if (searchForm.collegeId) {
    loadMajorList(searchForm.collegeId)
  }
}

// 表单学校变化
const handleFormSchoolChange = () => {
  formData.collegeId = null
  formData.majorId = null
  collegeList.value = []
  majorList.value = []
  if (formData.schoolId) {
    loadCollegeList(formData.schoolId)
  }
}

// 表单学院变化
const handleFormCollegeChange = () => {
  formData.majorId = null
  majorList.value = []
  if (formData.collegeId) {
    loadMajorList(formData.collegeId)
  }
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '创建实习计划'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  dialogTitle.value = '编辑实习计划'
  Object.assign(formData, {
    planId: row.planId,
    planName: row.planName,
    planCode: row.planCode,
    semesterId: row.semesterId,
    schoolId: row.schoolId,
    collegeId: row.collegeId,
    majorId: row.majorId,
    planType: row.planType,
    startDate: row.startDate,
    endDate: row.endDate,
    planOutline: row.planOutline || '',
    taskRequirements: row.taskRequirements || '',
    assessmentStandards: row.assessmentStandards || '',
    status: row.status
  })
  if (row.schoolId) {
    loadCollegeList(row.schoolId)
  }
  if (row.collegeId) {
    loadMajorList(row.collegeId)
  }
  dialogVisible.value = true
}

// 查看
const handleView = async (row) => {
  try {
    const res = await planApi.getPlanById(row.planId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 提交审核
const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('确定要提交审核吗？', '提示', {
      type: 'warning'
    })
    const res = await planApi.submitPlan(row.planId)
    if (res.code === 200) {
      ElMessage.success('提交审核成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交审核失败:', error)
      ElMessage.error(error.response?.data?.message || '提交审核失败')
    }
  }
}

// 审核
const handleAudit = (row) => {
  auditForm.planId = row.planId
  auditForm.auditStatus = 2
  auditForm.auditOpinion = ''
  auditDialogVisible.value = true
}

// 发布
const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm('确定要发布该实习计划吗？', '提示', {
      type: 'warning'
    })
    const res = await planApi.publishPlan(row.planId)
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

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该实习计划吗？', '提示', {
      type: 'warning'
    })
    const res = await planApi.deletePlan(row.planId)
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

// 提交表单
const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let res
        if (formData.planId) {
          res = await planApi.updatePlan(formData)
        } else {
          res = await planApi.addPlan(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.planId ? '更新成功' : '创建成功')
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

// 提交审核表单
const handleAuditSubmit = async () => {
  if (!auditFormRef.value) return
  await auditFormRef.value.validate(async (valid) => {
    if (valid) {
      auditLoading.value = true
      try {
        const res = await planApi.auditPlan(
          auditForm.planId,
          auditForm.auditStatus,
          auditForm.auditOpinion
        )
        if (res.code === 200) {
          ElMessage.success('审核成功')
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

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    planId: null,
    planName: '',
    planCode: '',
    semesterId: null,
    schoolId: null,
    collegeId: null,
    majorId: null,
    planType: '',
    startDate: '',
    endDate: '',
    planOutline: '',
    taskRequirements: '',
    assessmentStandards: '',
    status: 0
  })
  collegeList.value = []
  majorList.value = []
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
    0: '草稿',
    1: '待审核',
    2: '已通过',
    3: '已拒绝',
    4: '已发布'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger',
    4: 'success'
  }
  return typeMap[status] || 'info'
}

// 加载当前用户组织信息
const loadCurrentUserOrgInfo = async () => {
  try {
    const res = await userApi.getCurrentUserOrgInfo()
    if (res.code === 200 && res.data) {
      currentOrgInfo.value = {
        schoolId: res.data.schoolId || null,
        schoolName: res.data.schoolName || '',
        collegeId: res.data.collegeId || null,
        collegeName: res.data.collegeName || ''
      }
      
      // 根据角色设置筛选框默认值
      if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])) {
        if (currentOrgInfo.value.schoolId) {
          searchForm.schoolId = currentOrgInfo.value.schoolId
          // 加载该学校的学院列表
          await handleSchoolChange()
        }
      }
      
      if (hasAnyRole(['ROLE_COLLEGE_LEADER'])) {
        if (currentOrgInfo.value.collegeId) {
          searchForm.collegeId = currentOrgInfo.value.collegeId
        }
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

// 初始化
onMounted(async () => {
  loadSemesterList()
  loadSchoolList()
  await loadCurrentUserOrgInfo()
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

