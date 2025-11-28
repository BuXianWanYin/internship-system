<template>
  <PageLayout title="学校管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学校</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学校名称">
          <el-input
            v-model="searchForm.schoolName"
            placeholder="请输入学校名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学校代码">
          <el-input
            v-model="searchForm.schoolCode"
            placeholder="请输入学校代码"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
      <el-table-column prop="schoolName" label="学校名称" min-width="150" />
      <el-table-column prop="schoolCode" label="学校代码" min-width="120" />
      <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="managerName" label="负责人" min-width="100">
        <template #default="{ row }">
          {{ row.managerName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="managerPhone" label="负责人电话" min-width="120">
        <template #default="{ row }">
          {{ row.managerPhone || '-' }}
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
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" style="background: none; border: none; padding: 0;" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">停用</el-button>
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
        <el-form-item label="学校名称" prop="schoolName">
          <el-input v-model="formData.schoolName" placeholder="请输入学校名称" />
        </el-form-item>
        <el-form-item label="学校代码" prop="schoolCode">
          <el-input v-model="formData.schoolCode" placeholder="请输入学校代码" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="formData.address" type="textarea" :rows="2" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="学校管理员" prop="managerType">
          <el-radio-group v-model="formData.managerType" @change="handleManagerTypeChange">
            <el-radio label="existing">选择已有用户</el-radio>
            <el-radio label="new">创建新用户</el-radio>
            <el-radio label="none">暂不绑定</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 选择已有用户 -->
        <el-form-item v-if="formData.managerType === 'existing'" label="选择用户" prop="managerUserId">
          <el-select
            v-model="formData.managerUserId"
            placeholder="请输入用户名、姓名或手机号搜索用户"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            clearable
            style="width: 100%"
            @focus="handleUserSelectFocus"
          >
            <el-option
              v-for="user in availableUserList"
              :key="user.userId"
              :label="`${user.realName} (${user.username})${user.phone ? ' - ' + user.phone : ''}`"
              :value="user.userId"
            >
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ user.realName }} ({{ user.username }})</span>
                <span style="color: #909399; font-size: 12px;">{{ user.phone || '无手机号' }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <!-- 创建新用户 -->
        <template v-if="formData.managerType === 'new'">
          <el-form-item label="用户名" prop="newUserUsername">
            <el-input v-model="formData.newUserUsername" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="newUserPassword">
            <el-input
              v-model="formData.newUserPassword"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="真实姓名" prop="newUserRealName">
            <el-input v-model="formData.newUserRealName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="newUserPhone">
            <el-input v-model="formData.newUserPhone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="newUserEmail">
            <el-input v-model="formData.newUserEmail" placeholder="请输入邮箱（可选）" />
          </el-form-item>
        </template>
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
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Download } from '@element-plus/icons-vue'
import { schoolApi } from '@/api/system/school'
import { userApi } from '@/api/user/user'
import { exportExcel } from '@/utils/exportUtils'
import request from '@/utils/request'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加学校')
const formRef = ref(null)

const searchForm = reactive({
  schoolName: '',
  schoolCode: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
const availableUserList = ref([])
const userSearchLoading = ref(false)

const formData = reactive({
  schoolId: null,
  schoolName: '',
  schoolCode: '',
  address: '',
  status: 1,
  managerType: 'none', // 'existing', 'new', 'none'
  managerUserId: null,
  newUserUsername: '',
  newUserPassword: '',
  newUserRealName: '',
  newUserPhone: '',
  newUserEmail: ''
})

const formRules = {
  schoolName: [
    { required: true, message: '请输入学校名称', trigger: 'blur' }
  ],
  schoolCode: [
    { required: true, message: '请输入学校代码', trigger: 'blur' }
  ],
  managerUserId: [
    {
      validator: (rule, value, callback) => {
        if (formData.managerType === 'existing' && !value) {
          callback(new Error('请选择用户'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  newUserUsername: [
    {
      validator: (rule, value, callback) => {
        if (formData.managerType === 'new' && !value) {
          callback(new Error('请输入用户名'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  newUserPassword: [
    {
      validator: (rule, value, callback) => {
        if (formData.managerType === 'new' && !value) {
          callback(new Error('请输入密码'))
        } else if (formData.managerType === 'new' && value && value.length < 6) {
          callback(new Error('密码长度不能少于6位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  newUserRealName: [
    {
      validator: (rule, value, callback) => {
        if (formData.managerType === 'new' && !value) {
          callback(new Error('请输入真实姓名'))
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
    const res = await schoolApi.getSchoolPage({
      current: pagination.current,
      size: pagination.size,
      schoolName: searchForm.schoolName || undefined,
      schoolCode: searchForm.schoolCode || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
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
  searchForm.schoolName = ''
  searchForm.schoolCode = ''
  handleSearch()
}

// 添加
const handleAdd = () => {
  dialogTitle.value = '添加学校'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑学校'
  try {
    // 获取学校详情（包含当前绑定的管理员用户ID）
    const res = await schoolApi.getSchoolById(row.schoolId)
    if (res.code === 200 && res.data) {
      const school = res.data
      Object.assign(formData, {
        schoolId: school.schoolId,
        schoolName: school.schoolName,
        schoolCode: school.schoolCode,
        address: school.address || '',
        status: school.status,
        managerType: school.managerUserId ? 'existing' : 'none',
        managerUserId: school.managerUserId || null,
        newUserUsername: '',
        newUserPassword: '',
        newUserRealName: '',
        newUserPhone: '',
        newUserEmail: ''
      })
      // 如果已有绑定的管理员，加载用户信息到下拉列表
      if (school.managerUserId) {
        await loadUserById(school.managerUserId)
      } else {
        // 如果没有绑定的管理员，清空用户列表
        availableUserList.value = []
      }
    }
    dialogVisible.value = true
  } catch (error) {
    console.error('获取学校详情失败:', error)
    ElMessage.error('获取学校详情失败')
  }
}

// 加载用户信息
const loadUserById = async (userId) => {
  try {
    const res = await userApi.getUserById(userId)
    if (res.code === 200 && res.data) {
      const user = res.data
      // 检查用户是否已在列表中
      const existUser = availableUserList.value.find(u => u.userId === userId)
      if (!existUser) {
        availableUserList.value.push(user)
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 下拉框获得焦点时，如果没有输入，加载默认用户列表
const handleUserSelectFocus = () => {
  // 如果列表为空且没有正在加载，则加载默认列表
  if (availableUserList.value.length === 0 && !userSearchLoading.value) {
    loadDefaultUserList()
  }
}

// 加载默认用户列表（前20个启用的用户）
const loadDefaultUserList = async () => {
  userSearchLoading.value = true
  try {
    const res = await userApi.getUserPage({
      current: 1,
      size: 20,
      status: 1
    })
    if (res.code === 200 && res.data) {
      availableUserList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 搜索用户（支持按用户名、真实姓名、手机号搜索）
const searchUsers = async (query) => {
  if (!query || query.trim() === '') {
    // 如果查询为空，加载默认列表
    loadDefaultUserList()
    return
  }
  userSearchLoading.value = true
  try {
    // 尝试按真实姓名搜索
    let res = await userApi.getUserPage({
      current: 1,
      size: 20,
      realName: query.trim(),
      status: 1
    })
    
    // 如果按真实姓名没找到，尝试按用户名搜索
    if (res.code === 200 && res.data && (res.data.records || []).length === 0) {
      res = await userApi.getUserPage({
        current: 1,
        size: 20,
        username: query.trim(),
        status: 1
      })
    }
    
    // 如果按用户名也没找到，尝试按手机号搜索
    if (res.code === 200 && res.data && (res.data.records || []).length === 0) {
      res = await userApi.getUserPage({
        current: 1,
        size: 20,
        phone: query.trim(),
        status: 1
      })
    }
    
    if (res.code === 200 && res.data) {
      availableUserList.value = res.data.records || []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSearchLoading.value = false
  }
}

// 管理员类型变化
const handleManagerTypeChange = (value) => {
  if (value === 'existing') {
    // 清空新用户表单
    formData.newUserUsername = ''
    formData.newUserPassword = ''
    formData.newUserRealName = ''
    formData.newUserPhone = ''
    formData.newUserEmail = ''
  } else if (value === 'new') {
    // 清空已有用户选择
    formData.managerUserId = null
    availableUserList.value = []
  } else {
    // 清空所有
    formData.managerUserId = null
    formData.newUserUsername = ''
    formData.newUserPassword = ''
    formData.newUserRealName = ''
    formData.newUserPhone = ''
    formData.newUserEmail = ''
    availableUserList.value = []
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该学校吗？', '提示', {
      type: 'warning'
    })
    const res = await schoolApi.deleteSchool(row.schoolId)
    if (res.code === 200) {
      ElMessage.success('停用成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停用失败:', error)
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 构建提交数据
        const submitData = {
          schoolId: formData.schoolId,
          schoolName: formData.schoolName,
          schoolCode: formData.schoolCode,
          address: formData.address,
          status: formData.status
        }
        
        // 处理学校管理员绑定
        if (formData.managerType === 'existing' && formData.managerUserId) {
          submitData.managerUserId = formData.managerUserId
          submitData.createNewUser = false
        } else if (formData.managerType === 'new') {
          submitData.createNewUser = true
          submitData.newUserInfo = {
            username: formData.newUserUsername,
            password: formData.newUserPassword,
            realName: formData.newUserRealName,
            phone: formData.newUserPhone,
            email: formData.newUserEmail || undefined
          }
        } else {
          // 暂不绑定：编辑时解除绑定，新增时不绑定
          if (formData.schoolId) {
            // 编辑时，传递null表示解除绑定
            submitData.managerUserId = null
            submitData.createNewUser = false
          }
          // 新增时，不传递managerUserId和createNewUser，后端不会处理绑定
        }
        
        let res
        if (formData.schoolId) {
          res = await schoolApi.updateSchool(formData.schoolId, submitData)
        } else {
          res = await schoolApi.addSchool(submitData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.schoolId ? '更新成功' : '添加成功')
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

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    schoolId: null,
    schoolName: '',
    schoolCode: '',
    address: '',
    status: 1,
    managerType: 'none',
    managerUserId: null,
    newUserUsername: '',
    newUserPassword: '',
    newUserRealName: '',
    newUserPhone: '',
    newUserEmail: ''
  })
  availableUserList.value = []
  formRef.value?.clearValidate()
}

// 分页
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 导出学校列表
const handleExport = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数，使用当前页面的筛选条件
    const params = {
      schoolName: searchForm.schoolName || undefined,
      schoolCode: searchForm.schoolCode || undefined
    }
    
    // 注意：需要后端提供 /system/school/export 接口
    await exportExcel(
      (params) => request.get('/system/school/export', { params, responseType: 'blob' }),
      params,
      '学校列表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
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

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

/* 移除全局primary按钮背景色样式，使用Element Plus默认样式 */
</style>

