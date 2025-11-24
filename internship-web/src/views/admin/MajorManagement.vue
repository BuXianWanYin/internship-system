<template>
  <PageLayout title="专业管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加专业</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="专业名称">
          <el-input
            v-model="searchForm.majorName"
            placeholder="请输入专业名称"
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
      <el-table-column prop="majorName" label="专业名称" min-width="150" />
      <el-table-column prop="majorCode" label="专业代码" min-width="120" />
      <el-table-column label="所属学院" min-width="150">
        <template #default="{ row }">
          <span v-if="collegeMap[row.collegeId]">{{ collegeMap[row.collegeId].collegeName }}</span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="collegeMap[row.collegeId] && schoolMap[collegeMap[row.collegeId].schoolId]">
            {{ schoolMap[collegeMap[row.collegeId].schoolId].schoolName }}
          </span>
          <span v-else style="color: #909399">加载中...</span>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="学制年限" width="100" align="center" />
      <el-table-column prop="trainingObjective" label="培养目标" min-width="200" show-overflow-tooltip />
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
        <el-form-item label="专业名称" prop="majorName">
          <el-input v-model="formData.majorName" placeholder="请输入专业名称" />
        </el-form-item>
        <el-form-item label="专业代码" prop="majorCode">
          <el-input v-model="formData.majorCode" placeholder="请输入专业代码" />
        </el-form-item>
        <el-form-item label="所属学院" prop="collegeId">
          <el-select v-model="formData.collegeId" placeholder="请选择学院" style="width: 100%">
            <el-option
              v-for="college in collegeList"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学制年限" prop="duration">
          <el-input-number v-model="formData.duration" :min="1" :max="10" placeholder="请输入学制年限" style="width: 100%" />
        </el-form-item>
        <el-form-item label="培养目标" prop="trainingObjective">
          <el-input v-model="formData.trainingObjective" type="textarea" :rows="3" placeholder="请输入培养目标" />
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
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { majorApi } from '@/api/system/major'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { userApi } from '@/api/user/user'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole } from '@/utils/permission'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加专业')
const formRef = ref(null)

const schoolList = ref([])
const schoolMap = ref({})
const collegeList = ref([])
const collegeMap = ref({})

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

const searchForm = reactive({
  majorName: '',
  schoolId: null,
  collegeId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  majorId: null,
  majorName: '',
  majorCode: '',
  collegeId: null,
  duration: 4,
  trainingObjective: '',
  status: 1
})

const formRules = {
  majorName: [
    { required: true, message: '请输入专业名称', trigger: 'blur' }
  ],
  majorCode: [
    { required: true, message: '请输入专业代码', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请输入所属学院ID', trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await majorApi.getMajorPage({
      current: pagination.current,
      size: pagination.size,
      majorName: searchForm.majorName || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 批量加载学院和学校信息
      const collegeIds = [...new Set(tableData.value.map(item => item.collegeId))]
      await loadCollegeInfo(collegeIds)
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
  // 清空学院选择
  searchForm.collegeId = null
  // 加载该学校下的学院列表
  loadCollegeList(searchForm.schoolId)
}

const handleSearch = () => {
  pagination.current = 1
  loadData()
}

const handleReset = () => {
  searchForm.majorName = ''
  
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
  
  // 重新加载列表
  if (searchForm.schoolId) {
    handleSchoolChange()
  } else {
    collegeList.value = []
  }
  
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加专业'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑专业'
  Object.assign(formData, {
    majorId: row.majorId,
    majorName: row.majorName,
    majorCode: row.majorCode,
    collegeId: row.collegeId,
    duration: row.duration || 4,
    trainingObjective: row.trainingObjective || '',
    status: row.status
  })
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要停用该专业吗？', '提示', {
      type: 'warning'
    })
    const res = await majorApi.deleteMajor(row.majorId)
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
        let res
        if (formData.majorId) {
          res = await majorApi.updateMajor(formData.majorId, formData)
        } else {
          res = await majorApi.addMajor(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.majorId ? '更新成功' : '添加成功')
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
  Object.assign(formData, {
    majorId: null,
    majorName: '',
    majorCode: '',
    collegeId: null,
    duration: 4,
    trainingObjective: '',
    status: 1
  })
  formRef.value?.clearValidate()
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

onMounted(async () => {
  loadSchoolList()
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有学院
  if (!currentOrgInfo.value.schoolId) {
    loadCollegeList(null) // 加载所有学院
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
</style>

