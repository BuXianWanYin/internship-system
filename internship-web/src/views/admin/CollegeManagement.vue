<template>
  <PageLayout title="学院管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学院</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学院名称">
          <el-input
            v-model="searchForm.collegeName"
            placeholder="请输入学院名称"
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
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
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
      <el-table-column prop="collegeName" label="学院名称" min-width="150" />
      <el-table-column prop="collegeCode" label="学院代码" min-width="120" />
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="schoolMap[row.schoolId]">{{ schoolMap[row.schoolId].schoolName }}</span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column prop="deanName" label="院长" min-width="100" />
      <el-table-column prop="contactPhone" label="联系电话" min-width="120" />
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
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
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
        <el-form-item label="学院名称" prop="collegeName">
          <el-input v-model="formData.collegeName" placeholder="请输入学院名称" />
        </el-form-item>
        <el-form-item label="学院代码" prop="collegeCode">
          <el-input v-model="formData.collegeCode" placeholder="请输入学院代码" />
        </el-form-item>
        <el-form-item label="所属学校" prop="schoolId">
          <el-select v-model="formData.schoolId" placeholder="请选择学校" style="width: 100%">
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="院长" prop="deanUserId">
          <el-select 
            v-model="formData.deanUserId" 
            placeholder="请选择院长（需先添加教师）" 
            style="width: 100%"
            filterable
            clearable
            :disabled="!formData.schoolId"
          >
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.teacherId"
              :label="userInfoMap[teacher.userId] ? `${userInfoMap[teacher.userId].realName} (${teacher.teacherNo})` : teacher.teacherNo"
              :value="teacher.userId"
            />
          </el-select>
          <div v-if="!formData.schoolId" style="font-size: 12px; color: #909399; margin-top: 4px;">
            请先选择所属学校
          </div>
          <div v-if="formData.schoolId && teacherList.length === 0" style="font-size: 12px; color: #f56c6c; margin-top: 4px;">
            该学校暂无教师，请先添加教师
          </div>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
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
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { collegeApi } from '@/api/system/college'
import { schoolApi } from '@/api/system/school'
import { teacherApi } from '@/api/user/teacher'
import { userApi } from '@/api/user/user'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加学院')
const formRef = ref(null)

const schoolList = ref([])
const schoolMap = ref({})
const teacherList = ref([])
const userInfoMap = ref({})

const searchForm = reactive({
  collegeName: '',
  schoolId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  collegeId: null,
  collegeName: '',
  collegeCode: '',
  schoolId: null,
  deanUserId: null,
  deanName: '',
  contactPhone: '',
  status: 1
})

const formRules = {
  collegeName: [
    { required: true, message: '请输入学院名称', trigger: 'blur' }
  ],
  collegeCode: [
    { required: true, message: '请输入学院代码', trigger: 'blur' }
  ],
  schoolId: [
    { required: true, message: '请输入所属学校ID', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await collegeApi.getCollegePage({
      current: pagination.current,
      size: pagination.size,
      collegeName: searchForm.collegeName || undefined,
      schoolId: searchForm.schoolId || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 批量加载学校信息
      const schoolIds = [...new Set(tableData.value.map(item => item.schoolId))]
      await loadSchoolInfo(schoolIds)
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

// 加载教师列表（根据学校ID）
const loadTeacherList = async (schoolId) => {
  if (!schoolId) {
    teacherList.value = []
    userInfoMap.value = {}
    return
  }
  
  try {
    const res = await teacherApi.getTeacherList({ schoolId, status: 1 })
    if (res.code === 200) {
      teacherList.value = res.data || []
      
      // 批量加载用户信息
      const userIds = teacherList.value.map(t => t.userId).filter(id => id)
      if (userIds.length > 0) {
        for (const userId of userIds) {
          if (!userInfoMap.value[userId]) {
            try {
              const userRes = await userApi.getUserById(userId)
              if (userRes.code === 200 && userRes.data) {
                userInfoMap.value[userId] = userRes.data
              }
            } catch (error) {
              console.error(`加载用户 ${userId} 信息失败:`, error)
            }
          }
        }
      }
    }
  } catch (error) {
    console.error('加载教师列表失败:', error)
  }
}

// 监听学校ID变化，加载对应学校的教师列表
watch(() => formData.schoolId, (newSchoolId) => {
  if (newSchoolId) {
    loadTeacherList(newSchoolId)
  } else {
    teacherList.value = []
    formData.deanUserId = null
  }
})

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

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.collegeName = ''
  searchForm.schoolId = null
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加学院'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑学院'
  Object.assign(formData, {
    collegeId: row.collegeId,
    collegeName: row.collegeName,
    collegeCode: row.collegeCode,
    schoolId: row.schoolId,
    deanUserId: row.deanUserId || null,
    deanName: row.deanName || '',
    contactPhone: row.contactPhone || '',
    status: row.status
  })
  
  // 加载该学校的教师列表
  if (row.schoolId) {
    await loadTeacherList(row.schoolId)
  }
  
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该学院吗？', '提示', {
      type: 'warning'
    })
    const res = await collegeApi.deleteCollege(row.collegeId)
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

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 准备提交数据（只提交deanUserId，不提交deanName，后端会自动填充）
        const submitData = {
          ...formData,
          deanName: undefined // 不提交deanName，后端会根据deanUserId自动填充
        }
        
        let res
        if (formData.collegeId) {
          res = await collegeApi.updateCollege(formData.collegeId, submitData)
        } else {
          res = await collegeApi.addCollege(submitData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.collegeId ? '更新成功' : '添加成功')
          dialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const resetForm = () => {
  Object.assign(formData, {
    collegeId: null,
    collegeName: '',
    collegeCode: '',
    schoolId: null,
    deanUserId: null,
    deanName: '',
    contactPhone: '',
    status: 1
  })
  teacherList.value = []
  userInfoMap.value = {}
  formRef.value?.clearValidate()
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(() => {
  loadSchoolList()
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
</style>

