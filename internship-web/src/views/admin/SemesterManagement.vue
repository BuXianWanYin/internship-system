<template>
  <PageLayout title="学期管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学期</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学期名称">
          <el-input
            v-model="searchForm.semesterName"
            placeholder="请输入学期名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="年份">
          <el-input-number
            v-model="searchForm.year"
            placeholder="请输入年份"
            clearable
            style="width: 200px"
            :min="2000"
            :max="2100"
          />
        </el-form-item>
        <el-form-item label="当前学期">
          <el-select
            v-model="searchForm.isCurrent"
            placeholder="请选择"
            clearable
            style="width: 150px"
          >
            <el-option label="是" :value="1" />
            <el-option label="否" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学校">
          <el-select
            v-model="searchForm.schoolId"
            placeholder="请选择学校"
            clearable
            style="width: 200px"
            :disabled="isSchoolDisabled"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
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
      <el-table-column prop="semesterName" label="学期名称" min-width="200" />
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="schoolMap[row.schoolId]">{{ schoolMap[row.schoolId].schoolName }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="开始日期" width="120" />
      <el-table-column prop="endDate" label="结束日期" width="120" />
      <el-table-column prop="isCurrent" label="当前学期" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isCurrent === 1" type="success" size="small">是</el-tag>
          <span v-else style="color: #909399">否</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button
            v-if="row.isCurrent !== 1"
            link
            type="success"
            size="small"
            @click="handleSetCurrent(row)"
          >
            设为当前
          </el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
        <el-form-item label="学期名称" prop="semesterName">
          <el-input v-model="formData.semesterName" placeholder="请输入学期名称" />
        </el-form-item>
        <el-form-item label="所属学校" prop="schoolId" v-if="!isSchoolDisabledForAdd">
          <el-select 
            v-model="formData.schoolId" 
            placeholder="请选择学校" 
            style="width: 100%"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="formData.startDate"
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="formData.endDate"
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="设为当前学期" prop="isCurrent">
          <el-switch v-model="formData.isCurrent" :active-value="1" :inactive-value="0" />
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
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { semesterApi } from '@/api/system/semester'
import { schoolApi } from '@/api/system/school'
import { userApi } from '@/api/user/user'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole } from '@/utils/permission'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加学期')
const formRef = ref(null)

const schoolList = ref([])
const schoolMap = ref({})

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: ''
})

// 计算属性：学校下拉框是否禁用
const isSchoolDisabled = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN'])
})

// 计算属性：添加表单中学校下拉框是否禁用
const isSchoolDisabledForAdd = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN'])
})

const searchForm = reactive({
  semesterName: '',
  year: null,
  isCurrent: null,
  dateRange: null,
  schoolId: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])

const formData = reactive({
  semesterId: null,
  semesterName: '',
  schoolId: null,
  startDate: '',
  endDate: '',
  isCurrent: 0
})

const formRules = {
  semesterName: [
    { required: true, message: '请输入学期名称', trigger: 'blur' }
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
  startDate: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      semesterName: searchForm.semesterName || undefined,
      year: searchForm.year || undefined,
      isCurrent: searchForm.isCurrent !== null ? searchForm.isCurrent : undefined,
      startDate: searchForm.dateRange && searchForm.dateRange[0] ? searchForm.dateRange[0] : undefined,
      endDate: searchForm.dateRange && searchForm.dateRange[1] ? searchForm.dateRange[1] : undefined,
      schoolId: searchForm.schoolId || undefined
    }
    const res = await semesterApi.getSemesterPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 批量加载学校信息
      const schoolIds = [...new Set(tableData.value.map(item => item.schoolId).filter(id => id))]
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
  searchForm.semesterName = ''
  searchForm.year = null
  searchForm.isCurrent = null
  searchForm.dateRange = null
  
  // 根据角色重置筛选条件，保持组织信息的绑定
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
    // 学校管理员：保持学校ID
    searchForm.schoolId = currentOrgInfo.value.schoolId || null
  } else {
    searchForm.schoolId = null
  }
  
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '添加学期'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑学期'
  Object.assign(formData, {
    semesterId: row.semesterId,
    semesterName: row.semesterName,
    schoolId: row.schoolId || null,
    startDate: row.startDate,
    endDate: row.endDate,
    isCurrent: row.isCurrent
  })
  dialogVisible.value = true
}

const handleSetCurrent = async (row) => {
  try {
    await ElMessageBox.confirm('确定要设置该学期为当前学期吗？', '提示', {
      type: 'warning'
    })
    const res = await semesterApi.setCurrentSemester(row.semesterId)
    if (res.code === 200) {
      ElMessage.success('设置成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('设置失败:', error)
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该学期吗？', '提示', {
      type: 'warning'
    })
    const res = await semesterApi.deleteSemester(row.semesterId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (formData.startDate && formData.endDate && formData.startDate > formData.endDate) {
        ElMessage.error('开始日期不能晚于结束日期')
        return
      }
      
      submitLoading.value = true
      try {
        let res
        if (formData.semesterId) {
          res = await semesterApi.updateSemester(formData.semesterId, formData)
        } else {
          res = await semesterApi.addSemester(formData)
        }
        if (res.code === 200) {
          ElMessage.success(formData.semesterId ? '更新成功' : '添加成功')
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
  
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
    defaultSchoolId = currentOrgInfo.value.schoolId || null
  }
  
  Object.assign(formData, {
    semesterId: null,
    semesterName: '',
    schoolId: defaultSchoolId,
    startDate: '',
    endDate: '',
    isCurrent: 0
  })
  formRef.value?.clearValidate()
}

// 加载当前用户组织信息
const loadCurrentUserOrgInfo = async () => {
  try {
    const res = await userApi.getCurrentUserOrgInfo()
    if (res.code === 200 && res.data) {
      currentOrgInfo.value = {
        schoolId: res.data.schoolId || null,
        schoolName: res.data.schoolName || ''
      }
      
      // 根据角色设置筛选框默认值
      if (hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
        if (currentOrgInfo.value.schoolId) {
          searchForm.schoolId = currentOrgInfo.value.schoolId
        }
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

onMounted(async () => {
  loadSchoolList()
  await loadCurrentUserOrgInfo()
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

