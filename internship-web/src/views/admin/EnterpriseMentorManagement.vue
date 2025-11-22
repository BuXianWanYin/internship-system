<template>
  <PageLayout title="企业导师管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加企业导师
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="导师姓名">
          <el-input
            v-model="searchForm.mentorName"
            placeholder="请输入导师姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="所属企业">
          <el-select
            v-model="searchForm.enterpriseId"
            placeholder="请选择企业"
            clearable
            filterable
            style="width: 250px"
          >
            <el-option
              v-for="enterprise in enterpriseList"
              :key="enterprise.enterpriseId"
              :label="enterprise.enterpriseName"
              :value="enterprise.enterpriseId"
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
      <el-table-column prop="mentorName" label="导师姓名" min-width="120" />
      <el-table-column label="导师信息" min-width="180">
        <template #default="{ row }">
          <div v-if="userInfoMap[row.userId]">
            <div>{{ userInfoMap[row.userId].realName }}</div>
            <div style="font-size: 12px; color: #909399;">
              {{ userInfoMap[row.userId].phone || '未填写' }}
            </div>
            <div style="font-size: 12px; color: #909399;">
              {{ userInfoMap[row.userId].email || '未填写' }}
            </div>
          </div>
          <span v-else>加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="所属企业" min-width="200">
        <template #default="{ row }">
          <span v-if="enterpriseMap[row.enterpriseId]">{{ enterpriseMap[row.enterpriseId].enterpriseName }}</span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column prop="position" label="职位" min-width="120" />
      <el-table-column prop="department" label="部门" min-width="120" />
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
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN'])" 
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
            <el-form-item label="导师姓名" prop="mentorName">
              <el-input v-model="formData.mentorName" placeholder="请输入导师姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属企业" prop="enterpriseId">
              <el-select v-model="formData.enterpriseId" placeholder="请选择企业" style="width: 100%" filterable>
                <el-option
                  v-for="enterprise in enterpriseList"
                  :key="enterprise.enterpriseId"
                  :label="enterprise.enterpriseName"
                  :value="enterprise.enterpriseId"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="职位" prop="position">
              <el-input v-model="formData.position" placeholder="请输入职位" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门" prop="department">
              <el-input v-model="formData.department" placeholder="请输入部门" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入初始密码（6-20个字符）"
            show-password
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="企业导师详情"
      width="600px"
    >
      <el-descriptions v-if="currentMentor" :column="2" border>
        <el-descriptions-item label="导师姓名">{{ currentMentor.mentorName }}</el-descriptions-item>
        <el-descriptions-item label="所属企业" v-if="enterpriseMap[currentMentor.enterpriseId]">
          {{ enterpriseMap[currentMentor.enterpriseId].enterpriseName }}
        </el-descriptions-item>
        <el-descriptions-item label="职位">{{ currentMentor.position || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentMentor.department || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentMentor.phone || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentMentor.email || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentMentor.status === 1 ? 'success' : 'info'" size="small">
            {{ currentMentor.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentMentor.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="用户信息" v-if="userInfoMap[currentMentor.userId]" :span="2">
          <div>用户名：{{ userInfoMap[currentMentor.userId].username }}</div>
          <div>真实姓名：{{ userInfoMap[currentMentor.userId].realName }}</div>
          <div>手机号：{{ userInfoMap[currentMentor.userId].phone || '未填写' }}</div>
          <div>邮箱：{{ userInfoMap[currentMentor.userId].email || '未填写' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole } from '@/utils/permission'
import { enterpriseMentorApi } from '@/api/user/enterpriseMentor'
import { enterpriseApi } from '@/api/user/enterprise'
import { userApi } from '@/api/user/user'
import { formatDateTime } from '@/utils/dateUtils'

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 搜索表单
const searchForm = reactive({
  mentorName: '',
  enterpriseId: null,
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
const enterpriseMap = ref({})
const enterpriseList = ref([])

// 对话框
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('添加企业导师')
const isEdit = ref(false)
const formRef = ref(null)
const currentMentor = ref(null)

// 表单数据
const formData = reactive({
  mentorId: null,
  userId: null,
  mentorName: '',
  enterpriseId: null,
  position: '',
  department: '',
  phone: '',
  email: '',
  password: '',
  status: 1
})

// 表单验证规则
const formRules = {
  mentorName: [
    { required: true, message: '请输入导师姓名', trigger: 'blur' }
  ],
  enterpriseId: [
    { required: true, message: '请选择所属企业', trigger: 'change' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载企业列表
const loadEnterpriseList = async () => {
  try {
    const res = await enterpriseApi.getEnterprisePage({ current: 1, size: 1000, auditStatus: 1 })
    if (res.code === 200) {
      enterpriseList.value = res.data.records || []
      // 构建企业Map
      enterpriseList.value.forEach(enterprise => {
        enterpriseMap.value[enterprise.enterpriseId] = enterprise
      })
    }
  } catch (error) {
    console.error('加载企业列表失败:', error)
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await enterpriseMentorApi.getEnterpriseMentorPage({
      current: pagination.current,
      size: pagination.size,
      mentorName: searchForm.mentorName || undefined,
      enterpriseId: searchForm.enterpriseId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 加载用户信息和企业信息
      const userIds = [...new Set(tableData.value.map(item => item.userId))]
      const enterpriseIds = [...new Set(tableData.value.map(item => item.enterpriseId))]

      await loadUserInfo(userIds)
      await loadEnterpriseInfo(enterpriseIds)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfo = async (userIds) => {
  try {
    for (const userId of userIds) {
      if (!userInfoMap.value[userId]) {
        const res = await userApi.getUserById(userId)
        if (res.code === 200 && res.data) {
          userInfoMap.value[userId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败：', error)
  }
}

// 加载企业信息
const loadEnterpriseInfo = async (enterpriseIds) => {
  try {
    for (const enterpriseId of enterpriseIds) {
      if (!enterpriseMap.value[enterpriseId]) {
        const res = await enterpriseApi.getEnterpriseById(enterpriseId)
        if (res.code === 200 && res.data) {
          enterpriseMap.value[enterpriseId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载企业信息失败：', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.mentorName = ''
  searchForm.enterpriseId = null
  searchForm.status = null
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
    const res = await enterpriseMentorApi.getEnterpriseMentorById(row.mentorId)
    if (res.code === 200) {
      currentMentor.value = res.data
      // 加载用户信息
      if (currentMentor.value.userId) {
        await loadUserInfo([currentMentor.value.userId])
      }
      // 加载企业信息
      if (currentMentor.value.enterpriseId) {
        await loadEnterpriseInfo([currentMentor.value.enterpriseId])
      }
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取企业导师详情失败')
  }
}

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加企业导师'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑企业导师'
  try {
    const res = await enterpriseMentorApi.getEnterpriseMentorById(row.mentorId)
    if (res.code === 200) {
      const mentor = res.data
      // 加载用户信息
      if (mentor.userId) {
        const userRes = await userApi.getUserById(mentor.userId)
        if (userRes.code === 200 && userRes.data) {
          Object.assign(formData, {
            mentorId: mentor.mentorId,
            userId: mentor.userId,
            mentorName: mentor.mentorName,
            enterpriseId: mentor.enterpriseId,
            position: mentor.position || '',
            department: mentor.department || '',
            phone: mentor.phone || '',
            email: mentor.email || '',
            status: mentor.status,
            password: ''
          })
        }
      }
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取企业导师详情失败')
  }
}

// 重置表单
const resetFormData = () => {
  Object.assign(formData, {
    mentorId: null,
    userId: null,
    mentorName: '',
    enterpriseId: null,
    position: '',
    department: '',
    phone: '',
    email: '',
    password: '',
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
        const res = await enterpriseMentorApi.updateEnterpriseMentor(formData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        // 添加
        const res = await enterpriseMentorApi.addEnterpriseMentor(formData)
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
    await ElMessageBox.confirm('确定要停用该企业导师吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await enterpriseMentorApi.deleteEnterpriseMentor(row.mentorId)
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

// 初始化
onMounted(() => {
  loadEnterpriseList()
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
</style>

