<template>
  <PageLayout title="用户管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加用户
      </el-button>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
        type="success" 
        :icon="Upload" 
        @click="handleImport"
      >
        批量导入
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
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
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.roleCodes"
            placeholder="请选择角色"
            clearable
            style="width: 200px"
            multiple
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="role in roleList"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学校">
          <el-select
            v-model="searchForm.schoolId"
            placeholder="请选择学校"
            clearable
            style="width: 200px"
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
            :disabled="!searchForm.schoolId"
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
        <el-form-item label="所属班级">
          <el-select
            v-model="searchForm.classId"
            placeholder="请选择班级"
            clearable
            style="width: 200px"
            :disabled="!searchForm.collegeId"
          >
            <el-option
              v-for="classItem in classList"
              :key="classItem.classId"
              :label="classItem.className"
              :value="classItem.classId"
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
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="primary" 
            size="small" 
            :disabled="!canEditUser(row.roles || [])"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="warning" 
            size="small" 
            @click="handleResetPassword(row)"
          >
            重置密码
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="danger" 
            size="small" 
            :disabled="!canDeleteMap[row.userId]"
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
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
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
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole, canEditUser } from '@/utils/permission'
import { userApi } from '@/api/user/user'
import { roleApi } from '@/api/user/role'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { classApi } from '@/api/system/class'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  phone: '',
  status: null,
  roleCodes: null,
  schoolId: null,
  collegeId: null,
  classId: null
})

// 筛选选项
const roleList = ref([])
const schoolList = ref([])
const collegeList = ref([])
const classList = ref([])

// 表格数据
const tableData = ref([])
const loading = ref(false)
// 用户是否可以停用的映射表
const canDeleteMap = ref({})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  idCard: '',
  phone: '',
  email: '',
  status: 1
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

// 重置密码验证规则
const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
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
    const params = {
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      roleCodes: searchForm.roleCodes && searchForm.roleCodes.length > 0 ? searchForm.roleCodes.join(',') : undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      classId: searchForm.classId || undefined
    }
    const res = await userApi.getUserPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      
      // 为每个用户加载角色信息
      await loadUserRoles()
      
      // 检查每个用户是否可以停用
      await checkCanDeleteUsers()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载用户角色信息
const loadUserRoles = async () => {
  // 批量加载，使用 Promise.all 并行请求
  const rolePromises = tableData.value.map(async (user) => {
    try {
      const res = await userApi.getUserRoles(user.userId)
      return {
        userId: user.userId,
        roles: res.code === 200 && res.data ? res.data.map(r => r.roleCode) : []
      }
    } catch (error) {
      console.error(`加载用户 ${user.userId} 角色失败:`, error)
      return {
        userId: user.userId,
        roles: []
      }
    }
  })
  
  const results = await Promise.all(rolePromises)
  results.forEach(result => {
    const user = tableData.value.find(u => u.userId === result.userId)
    if (user) {
      user.roles = result.roles
    }
  })
}

// 检查用户是否可以停用
const checkCanDeleteUsers = async () => {
  canDeleteMap.value = {}
  // 批量检查，使用 Promise.all 并行请求
  const checkPromises = tableData.value.map(async (user) => {
    try {
      const res = await userApi.canDeleteUser(user.userId)
      return {
        userId: user.userId,
        canDelete: res.code === 200 ? res.data : false
      }
    } catch (error) {
      console.error(`检查用户 ${user.userId} 是否可以停用失败:`, error)
      return {
        userId: user.userId,
        canDelete: false
      }
    }
  })
  
  const results = await Promise.all(checkPromises)
  results.forEach(result => {
    canDeleteMap.value[result.userId] = result.canDelete
  })
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.phone = ''
  searchForm.status = null
  searchForm.roleCodes = null
  searchForm.schoolId = null
  searchForm.collegeId = null
  searchForm.classId = null
  collegeList.value = []
  classList.value = []
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
  dialogTitle.value = '添加用户'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  try {
    const res = await userApi.getUserById(row.userId)
    if (res.code === 200) {
      Object.assign(formData, {
        userId: res.data.userId,
        username: res.data.username,
        password: '',
        realName: res.data.realName || '',
        idCard: res.data.idCard || '',
        phone: res.data.phone || '',
        email: res.data.email || '',
        status: res.data.status
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    userId: null,
    username: '',
    password: '',
    realName: '',
    idCard: '',
    phone: '',
    email: '',
    status: 1
  })
  formRef.value?.clearValidate()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑时不需要密码
        const { password, ...updateData } = formData
        const res = await userApi.updateUser(updateData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        // 添加时需要密码
        const res = await userApi.addUser(formData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadData()
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.userId
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await userApi.resetPassword(
        resetPasswordForm.userId,
        resetPasswordForm.newPassword
      )
      if (res.code === 200) {
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
      }
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 批量导入
const router = useRouter()
const handleImport = () => {
  router.push('/admin/student/import')
}

// 删除
const handleDelete = (row) => {
  // 再次检查是否可以停用（防止状态变化）
  if (!canDeleteMap.value[row.userId]) {
    ElMessage.warning('该用户不能停用，系统至少需要保留一个启用的管理员')
    return
  }
  
  ElMessageBox.confirm(
    `确定要停用用户 "${row.username}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await userApi.deleteUser(row.userId)
      if (res.code === 200) {
        ElMessage.success('停用成功')
        loadData()
      }
    } catch (error) {
      // 显示后端返回的错误信息
      const errorMessage = error.response?.data?.message || error.message || '停用失败'
      ElMessage.error(errorMessage)
      // 重新检查是否可以停用
      await checkCanDeleteUsers()
    }
  }).catch(() => {})
}

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

const loadCollegeList = async (schoolId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await collegeApi.getCollegePage(params)
    if (res.code === 200) {
      collegeList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

const loadClassList = async (collegeId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (collegeId) {
      // 需要通过专业关联查询，这里简化处理，加载所有班级
      // 实际应该通过专业ID查询，但前端没有专业筛选，所以先加载所有
    }
    const res = await classApi.getClassPage(params)
    if (res.code === 200) {
      classList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await roleApi.getAllEnabledRoles()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadRoleList()
  loadSchoolList()
  loadCollegeList(null)
  loadClassList(null)
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-form {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

