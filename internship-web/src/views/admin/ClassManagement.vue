<template>
  <PageLayout :title="pageTitle">
    <template #actions>
      <el-button 
        v-if="canAddClass"
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加班级
      </el-button>
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
        <el-form-item label="所属学校">
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
        <el-form-item label="所属学院">
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
        <el-form-item label="所属专业">
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
      <el-table-column label="所属专业" min-width="150">
        <template #default="{ row }">
          <span v-if="majorMap[row.majorId]">{{ majorMap[row.majorId].majorName }}</span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学院" min-width="150">
        <template #default="{ row }">
          <span v-if="majorMap[row.majorId] && collegeMap[majorMap[row.majorId].collegeId]">
            {{ collegeMap[majorMap[row.majorId].collegeId].collegeName }}
          </span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="majorMap[row.majorId] && 
                      collegeMap[majorMap[row.majorId].collegeId] && 
                      schoolMap[collegeMap[majorMap[row.majorId].collegeId].schoolId]">
            {{ schoolMap[collegeMap[majorMap[row.majorId].collegeId].schoolId].schoolName }}
          </span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
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
          <el-button
            v-if="row.status === 1"
            link
            type="danger"
            size="small"
            @click="handleDisable(row)"
          >
            停用
          </el-button>
          <el-button
            v-if="row.status === 0"
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
        <el-form-item label="所属学校" prop="schoolId" v-if="!isSchoolDisabledForAdd">
          <el-select 
            v-model="formData.schoolId" 
            placeholder="请选择学校" 
            style="width: 100%"
            @change="handleSchoolChangeForAdd"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId" v-if="!isCollegeDisabledForAdd">
          <el-select 
            v-model="formData.collegeId" 
            placeholder="请选择学院" 
            style="width: 100%"
            :disabled="isCollegeDisabledForAdd || (formData.schoolId === null && !isSchoolDisabledForAdd)"
            @change="handleCollegeChangeForAdd"
          >
            <el-option
              v-for="college in collegeListForAdd"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
          <div v-if="formData.schoolId === null && !isSchoolDisabledForAdd" style="font-size: 12px; color: #909399; margin-top: 4px;">
            请先选择所属学校
          </div>
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select 
            v-model="formData.majorId" 
            placeholder="请选择专业" 
            style="width: 100%"
            :disabled="formData.collegeId === null && !isCollegeDisabledForAdd"
          >
            <el-option
              v-for="major in majorListForAdd"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
          <div v-if="formData.collegeId === null && !isCollegeDisabledForAdd" style="font-size: 12px; color: #909399; margin-top: 4px;">
            请先选择所属学院
          </div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, DocumentCopy } from '@element-plus/icons-vue'
import { classApi } from '@/api/system/class'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { majorApi } from '@/api/system/major'
import { userApi } from '@/api/user/user'
import PageLayout from '@/components/common/PageLayout.vue'
import { formatDateTime } from '@/utils/dateUtils'
import { hasAnyRole } from '@/utils/permission'
import { useAuthStore } from '@/store/modules/auth'

const loading = ref(false)
const submitLoading = ref(false)
const shareCodeLoading = ref(false)
const dialogVisible = ref(false)
const shareCodeDialogVisible = ref(false)
const dialogTitle = ref('添加班级')
const formRef = ref(null)
const currentClassId = ref(null)
const shareCodeInfo = ref(null)

const schoolList = ref([])
const schoolMap = ref({})
const collegeList = ref([])
const collegeMap = ref({})
const majorList = ref([])
const majorMap = ref({})
const collegeListForAdd = ref([]) // 添加表单用的学院列表
const majorListForAdd = ref([]) // 添加表单用的专业列表

// 页面标题（根据角色动态显示）
const pageTitle = computed(() => {
  const authStore = useAuthStore()
  const currentRoles = authStore.roles || []
  if (currentRoles.includes('ROLE_CLASS_TEACHER')) {
    return '我管理的班级'
  }
  return '班级管理'
})

// 是否可以添加班级（只有系统管理员、学校管理员、学院负责人可以添加）
const canAddClass = computed(() => {
  return hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
})

// 计算属性：添加表单中学校下拉框是否禁用
const isSchoolDisabledForAdd = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
})

// 计算属性：添加表单中学院下拉框是否禁用
const isCollegeDisabledForAdd = computed(() => {
  return hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
})

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: '',
  collegeId: null,
  collegeName: '',
  classIds: [],
  classNames: []
})

const searchForm = reactive({
  className: '',
  schoolId: null,
  collegeId: null,
  majorId: null
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
  schoolId: null,
  collegeId: null,
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
  schoolId: [
    { required: true, message: '请选择所属学校', trigger: 'change', validator: (rule, value, callback) => {
      if (!isSchoolDisabledForAdd.value && !value) {
        callback(new Error('请选择所属学校'))
      } else {
        callback()
      }
    }}
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change', validator: (rule, value, callback) => {
      if (!isCollegeDisabledForAdd.value && !value) {
        callback(new Error('请选择所属学院'))
      } else {
        callback()
      }
    }}
  ],
  majorId: [
    { required: true, message: '请选择所属专业', trigger: 'change' }
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
      className: searchForm.className || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      majorId: searchForm.majorId || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 批量加载专业、学院和学校信息
      const majorIds = [...new Set(tableData.value.map(item => item.majorId))]
      await loadMajorInfo(majorIds)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

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

const loadCollegeList = async (schoolId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await collegeApi.getCollegePage(params)
    if (res.code === 200) {
      collegeList.value = res.data.records || []
      // 构建学院Map
      collegeList.value.forEach(college => {
        collegeMap.value[college.collegeId] = college
        // 同时加载学校信息
        if (college.schoolId && !schoolMap.value[college.schoolId]) {
          loadSchoolInfo([college.schoolId])
        }
      })
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

const loadMajorList = async (collegeId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (collegeId) {
      params.collegeId = collegeId
    }
    const res = await majorApi.getMajorPage(params)
    if (res.code === 200) {
      majorList.value = res.data.records || []
      // 构建专业Map
      majorList.value.forEach(major => {
        majorMap.value[major.majorId] = major
        // 同时加载学院和学校信息
        if (major.collegeId && !collegeMap.value[major.collegeId]) {
          loadCollegeInfo([major.collegeId])
        }
      })
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

const loadMajorInfo = async (majorIds) => {
  try {
    for (const majorId of majorIds) {
      if (!majorMap.value[majorId]) {
        const res = await majorApi.getMajorById(majorId)
        if (res.code === 200 && res.data) {
          majorMap.value[majorId] = res.data
          // 同时加载学院和学校信息
          if (res.data.collegeId) {
            await loadCollegeInfo([res.data.collegeId])
          }
        }
      }
    }
  } catch (error) {
    console.error('加载专业信息失败:', error)
  }
}

const loadCollegeInfo = async (collegeIds) => {
  try {
    for (const collegeId of collegeIds) {
      if (!collegeMap.value[collegeId]) {
        const res = await collegeApi.getCollegeById(collegeId)
        if (res.code === 200 && res.data) {
          collegeMap.value[collegeId] = res.data
          // 同时加载学校信息
          if (res.data.schoolId && !schoolMap.value[res.data.schoolId]) {
            await loadSchoolInfo([res.data.schoolId])
          }
        }
      }
    }
  } catch (error) {
    console.error('加载学院信息失败:', error)
  }
}

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

const handleSchoolChange = () => {
  // 清空学院和专业选择
  searchForm.collegeId = null
  searchForm.majorId = null
  majorList.value = []
  // 加载该学校下的学院列表
  loadCollegeList(searchForm.schoolId)
}

const handleCollegeChange = () => {
  // 清空专业选择
  searchForm.majorId = null
  // 加载该学院下的专业列表
  loadMajorList(searchForm.collegeId)
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.className = ''
  
  // 根据角色重置筛选条件，保持组织信息的绑定
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
    // 学校管理员、学院负责人、班主任：保持学校ID
    searchForm.schoolId = currentOrgInfo.value.schoolId || null
  } else {
    searchForm.schoolId = null
  }
  
  if (hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
    // 学院负责人、班主任：保持学院ID
    searchForm.collegeId = currentOrgInfo.value.collegeId || null
  } else {
    searchForm.collegeId = null
  }
  
  searchForm.majorId = null
  
  // 重新加载列表
  if (searchForm.schoolId) {
    handleSchoolChange(searchForm.schoolId)
  } else {
    collegeList.value = []
    majorList.value = []
  }
  
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加班级'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑班级'
  Object.assign(formData, {
    classId: row.classId,
    className: row.className,
    classCode: row.classCode,
    schoolId: null,
    collegeId: null,
    majorId: row.majorId,
    enrollmentYear: row.enrollmentYear,
    status: row.status
  })
  
  // 加载专业信息，获取学院和学校ID
  if (row.majorId) {
    const major = majorMap.value[row.majorId]
    if (major && major.collegeId) {
      const college = collegeMap.value[major.collegeId]
      if (college && college.schoolId) {
        formData.schoolId = college.schoolId
        formData.collegeId = major.collegeId
        // 加载该学校的学院列表
        await loadCollegeListForAdd(college.schoolId)
        // 加载该学院的专业列表
        await loadMajorListForAdd(major.collegeId)
      } else {
        // 如果缓存中没有，需要查询
        try {
          const collegeRes = await collegeApi.getCollegeById(major.collegeId)
          if (collegeRes.code === 200 && collegeRes.data) {
            formData.schoolId = collegeRes.data.schoolId
            formData.collegeId = major.collegeId
            await loadCollegeListForAdd(collegeRes.data.schoolId)
            await loadMajorListForAdd(major.collegeId)
          }
        } catch (error) {
          console.error('加载学院信息失败:', error)
        }
      }
    } else {
      // 如果缓存中没有专业信息，需要查询
      try {
        const majorRes = await majorApi.getMajorById(row.majorId)
        if (majorRes.code === 200 && majorRes.data && majorRes.data.collegeId) {
          const collegeRes = await collegeApi.getCollegeById(majorRes.data.collegeId)
          if (collegeRes.code === 200 && collegeRes.data) {
            formData.schoolId = collegeRes.data.schoolId
            formData.collegeId = majorRes.data.collegeId
            await loadCollegeListForAdd(collegeRes.data.schoolId)
            await loadMajorListForAdd(majorRes.data.collegeId)
          }
        }
      } catch (error) {
        console.error('加载专业信息失败:', error)
      }
    }
  }
  
  dialogVisible.value = true
}

const handleDisable = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要停用班级 "${row.className}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await classApi.disableClass(row.classId)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMessage = error.response?.data?.message || error.message || '停用失败'
      ElMessage.error(errorMessage)
    }
  }
}

const handleEnable = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要启用班级 "${row.className}" 吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const res = await classApi.enableClass(row.classId)
    if (res.code === 200) {
      ElMessage.success('启用成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      const errorMessage = error.response?.data?.message || error.message || '启用失败'
      ElMessage.error(errorMessage)
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
  // 根据角色设置默认值
  let defaultSchoolId = null
  let defaultCollegeId = null
  
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
    defaultSchoolId = currentOrgInfo.value.schoolId || null
  } else if (hasAnyRole(['ROLE_COLLEGE_LEADER'])) {
    defaultSchoolId = currentOrgInfo.value.schoolId || null
    defaultCollegeId = currentOrgInfo.value.collegeId || null
  }
  
  Object.assign(formData, {
    classId: null,
    className: '',
    classCode: '',
    schoolId: defaultSchoolId,
    collegeId: defaultCollegeId,
    majorId: null,
    enrollmentYear: new Date().getFullYear(),
    status: 1
  })
  
  // 如果有默认学校，加载学院列表
  if (defaultSchoolId) {
    loadCollegeListForAdd(defaultSchoolId)
    // 如果有默认学院，加载专业列表
    if (defaultCollegeId) {
      loadMajorListForAdd(defaultCollegeId)
    } else {
      majorListForAdd.value = []
    }
  } else {
    collegeListForAdd.value = []
    majorListForAdd.value = []
  }
  
  formRef.value?.clearValidate()
}

// 加载添加表单用的学院列表
const loadCollegeListForAdd = async (schoolId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await collegeApi.getCollegePage(params)
    if (res.code === 200) {
      collegeListForAdd.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

// 加载添加表单用的专业列表
const loadMajorListForAdd = async (collegeId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (collegeId) {
      params.collegeId = collegeId
    }
    const res = await majorApi.getMajorPage(params)
    if (res.code === 200) {
      majorListForAdd.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 学校改变时的处理（添加表单）
const handleSchoolChangeForAdd = () => {
  formData.collegeId = null
  formData.majorId = null
  majorListForAdd.value = []
  if (formData.schoolId) {
    loadCollegeListForAdd(formData.schoolId)
  } else {
    collegeListForAdd.value = []
  }
}

// 学院改变时的处理（添加表单）
const handleCollegeChangeForAdd = () => {
  formData.majorId = null
  if (formData.collegeId) {
    loadMajorListForAdd(formData.collegeId)
  } else {
    majorListForAdd.value = []
  }
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
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
        collegeName: res.data.collegeName || '',
        classIds: res.data.classIds || [],
        classNames: res.data.classNames || []
      }
      
      // 根据角色设置筛选框默认值
      if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
        if (currentOrgInfo.value.schoolId) {
          searchForm.schoolId = currentOrgInfo.value.schoolId
          // 加载该学校的学院列表
          await handleSchoolChange(currentOrgInfo.value.schoolId)
        }
      }
      
      if (hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
        if (currentOrgInfo.value.collegeId) {
          searchForm.collegeId = currentOrgInfo.value.collegeId
          // 加载该学院的专业列表
          await handleCollegeChange(currentOrgInfo.value.collegeId)
        }
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

onMounted(async () => {
  loadSchoolList()
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有数据
  if (!currentOrgInfo.value.schoolId) {
    loadCollegeList(null) // 加载所有学院
  }
  if (!currentOrgInfo.value.collegeId) {
    loadMajorList(null) // 加载所有专业
  }
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

