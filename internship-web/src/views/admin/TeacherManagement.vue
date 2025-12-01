<template>
  <PageLayout title="教师管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加教师
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工号">
          <el-input
            v-model="searchForm.teacherNo"
            placeholder="请输入工号"
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
          >
            <el-option
              v-for="college in collegeList"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
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
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button 
            type="success" 
            :icon="Download" 
            @click="handleExport"
            :loading="exportLoading"
          >
            导出Excel
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
      <el-table-column prop="teacherNo" label="工号" min-width="120" />
      <el-table-column label="教师信息" min-width="150">
        <template #default="{ row }">
          <div v-if="userInfoMap[row.userId]">
            <div>{{ userInfoMap[row.userId].realName }}</div>
            <div style="font-size: 12px; color: #909399;">
              {{ userInfoMap[row.userId].phone || '未填写' }}
            </div>
          </div>
          <span v-else>加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" width="80" align="center">
        <template #default="{ row }">
          <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].gender">
            {{ userInfoMap[row.userId].gender }}
          </span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学院" min-width="150">
        <template #default="{ row }">
          <span v-if="collegeMap[row.collegeId]">{{ collegeMap[row.collegeId].collegeName }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="schoolMap[row.schoolId]">{{ schoolMap[row.schoolId].schoolName }}</span>
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
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="primary" 
            size="small" 
            :disabled="!canEditTeacher(row)"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="teacherNo">
              <el-input v-model="formData.teacherNo" placeholder="请输入工号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select
                v-model="formData.collegeId"
                placeholder="请选择学院"
                style="width: 100%"
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
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属学校" prop="schoolId">
              <el-select
                v-model="formData.schoolId"
                placeholder="请选择学校"
                style="width: 100%"
                :disabled="true"
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
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="职称">
          <el-select
            v-model="formData.title"
            placeholder="请选择职称"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option
              v-for="title in titleOptions"
              :key="title"
              :label="title"
              :value="title"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入初始密码（6-20个字符）"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole, canEditUser } from '@/utils/permission'
import { teacherApi } from '@/api/user/teacher'
import { userApi } from '@/api/user/user'
import { collegeApi } from '@/api/system/college'
import { schoolApi } from '@/api/system/school'
import { exportExcel } from '@/utils/exportUtils'
import request from '@/utils/request'

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const exportLoading = ref(false)

// 搜索表单
const searchForm = reactive({
  teacherNo: '',
  schoolId: null,
  collegeId: null,
  status: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])
const userInfoMap = ref({})
const collegeMap = ref({})
const schoolMap = ref({})

// 学院列表和学校列表（用于下拉选择）
const collegeList = ref([])
const schoolList = ref([])

// 职称选项
const titleOptions = [
  '教授',
  '副教授',
  '讲师',
  '助教',
  '研究员',
  '副研究员',
  '助理研究员',
  '高级工程师',
  '工程师',
  '助理工程师',
  '其他'
]

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

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加教师')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  teacherId: null,
  teacherNo: '',
  userId: null,
  realName: '',
  gender: '',
  idCard: '',
  phone: '',
  email: '',
  collegeId: null,
  schoolId: null,
  title: '',
  status: 1,
  password: ''
})

// 表单验证规则
const formRules = {
  teacherNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  schoolId: [
    { required: true, message: '请选择所属学校', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载学院列表（所有）
const loadCollegeList = async () => {
  try {
    const res = await collegeApi.getCollegePage({ current: 1, size: 1000 })
    if (res.code === 200) {
      collegeList.value = res.data.records || []
      // 构建学院Map
      collegeList.value.forEach(college => {
        collegeMap.value[college.collegeId] = college
      })
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
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

// 检查是否可以编辑该教师
const canEditTeacher = (row) => {
  if (!row || !row.userId) {
    return false
  }
  // 优先使用教师数据中的角色信息，如果没有则从用户信息中获取
  let roles = row.roles
  if (!roles || roles.length === 0) {
    const userInfo = userInfoMap.value[row.userId]
    if (userInfo && userInfo.roles) {
      roles = userInfo.roles
    } else {
      return false
    }
  }
  return canEditUser(roles)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      teacherNo: searchForm.teacherNo || undefined,
      collegeId: searchForm.collegeId || undefined
    }
    const res = await teacherApi.getTeacherPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 加载用户信息
      const userIds = tableData.value.map(item => item.userId).filter(Boolean)
      if (userIds.length > 0) {
        await loadUserInfos(userIds)
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfos = async (userIds) => {
  try {
    const promises = userIds.map(userId => userApi.getUserById(userId))
    const results = await Promise.all(promises)
    results.forEach((res, index) => {
      if (res.code === 200 && res.data) {
        userInfoMap.value[userIds[index]] = res.data
        // 角色信息已经包含在返回的数据中，不需要额外请求
      }
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.teacherNo = ''
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
  
  // 重新加载学院列表
  if (searchForm.schoolId) {
    handleSchoolChange(searchForm.schoolId)
  } else {
    collegeList.value = []
  }
  
  handleSearch()
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

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加教师'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑教师'
  try {
    const res = await teacherApi.getTeacherById(row.teacherId)
    if (res.code === 200) {
      Object.assign(formData, {
        teacherId: res.data.teacherId,
        teacherNo: res.data.teacherNo,
        userId: res.data.userId,
        realName: userInfoMap.value[res.data.userId]?.realName || '',
        gender: userInfoMap.value[res.data.userId]?.gender || '',
        idCard: userInfoMap.value[res.data.userId]?.idCard || '',
        phone: userInfoMap.value[res.data.userId]?.phone || '',
        email: userInfoMap.value[res.data.userId]?.email || '',
        collegeId: res.data.collegeId,
        schoolId: res.data.schoolId,
        title: res.data.title || '',
        status: res.data.status,
        password: ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取教师详情失败:', error)
    ElMessage.error('获取教师详情失败')
  }
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    teacherId: null,
    teacherNo: '',
    userId: null,
    realName: '',
    gender: '',
    idCard: '',
    phone: '',
    email: '',
    collegeId: null,
    schoolId: null,
    title: '',
    status: 1,
    password: ''
  })
  formRef.value?.clearValidate()
}

// 学校变化时加载学院列表
const handleSchoolChange = async (schoolId) => {
  // 清空学院选择
  searchForm.collegeId = null
  
  if (schoolId) {
    // 加载该学校下的学院列表
    try {
      const res = await collegeApi.getCollegePage({ 
        current: 1, 
        size: 1000,
        schoolId: schoolId 
      })
      if (res.code === 200) {
        collegeList.value = res.data.records || []
        // 构建学院Map
        collegeList.value.forEach(college => {
          collegeMap.value[college.collegeId] = college
        })
      }
    } catch (error) {
      console.error('加载学院列表失败:', error)
    }
  } else {
    // 如果没有选择学校，加载所有学院
    await loadCollegeList()
  }
}

// 学院变化时更新学校
const handleCollegeChange = (collegeId) => {
  const college = collegeList.value.find(c => c.collegeId === collegeId)
  if (college) {
    formData.schoolId = college.schoolId
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const submitData = {
        teacherNo: formData.teacherNo,
        realName: formData.realName,
        gender: formData.gender || null,
        idCard: formData.idCard,
        phone: formData.phone,
        email: formData.email,
        collegeId: formData.collegeId,
        schoolId: formData.schoolId,
        title: formData.title || undefined,
        status: formData.status
      }
      
      if (isEdit.value) {
        submitData.teacherId = formData.teacherId
        submitData.userId = formData.userId
        const res = await teacherApi.updateTeacher(submitData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        submitData.password = formData.password
        const res = await teacherApi.addTeacher(submitData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadData()
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该教师吗？', '提示', {
      type: 'warning'
    })
    const res = await teacherApi.deleteTeacher(row.teacherId)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
      ElMessage.error('停用失败')
    }
  }
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
          await handleSchoolChange(currentOrgInfo.value.schoolId)
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

// 导出教师列表
const handleExport = async () => {
  exportLoading.value = true
  try {
    const params = {
      teacherNo: searchForm.teacherNo || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    
    // 注意：需要后端提供 /user/teacher/export 接口
    await exportExcel(
      (params) => request.get('/user/teacher/export', { params, responseType: 'blob' }),
      params,
      '教师列表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

// 初始化
onMounted(async () => {
  loadCollegeList()
  loadSchoolList()
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有学院
  if (!currentOrgInfo.value.schoolId) {
    loadCollegeList()
  }
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

