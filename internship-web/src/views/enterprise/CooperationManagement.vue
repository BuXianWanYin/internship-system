<template>
  <PageLayout title="合作管理">
    <template #actions>
      <el-button 
        type="primary" 
        :icon="Plus" 
        @click="handleApply"
      >
        申请合作
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item v-if="activeTab === 'cooperation'" label="学校名称">
          <el-input
            v-model="searchForm.schoolName"
            placeholder="请输入学校名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item v-if="activeTab === 'cooperation'" label="合作类型">
          <el-select
            v-model="searchForm.cooperationType"
            placeholder="请选择合作类型"
            clearable
            style="width: 150px"
          >
            <el-option label="实习基地" value="实习基地" />
            <el-option label="校企合作" value="校企合作" />
            <el-option label="人才培养" value="人才培养" />
            <el-option label="科研合作" value="科研合作" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="activeTab === 'apply'" label="学校名称">
          <el-input
            v-model="searchForm.schoolName"
            placeholder="请输入学校名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item v-if="activeTab === 'apply'" label="学校代码">
          <el-input
            v-model="searchForm.schoolCode"
            placeholder="请输入学校代码"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="已建立合作" name="cooperation">
        <el-table
          v-loading="loading"
          :data="filteredCooperationList"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          empty-text="暂无数据"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="schoolName" label="学校名称" min-width="200" />
          <el-table-column prop="cooperationType" label="合作类型" width="150" />
          <el-table-column label="合作时间" width="280">
            <template #default="{ row }">
              <div>{{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="cooperationStatus" label="合作状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.cooperationStatus === 1 ? 'success' : 'info'" size="small">
                {{ row.cooperationStatus === 1 ? '进行中' : '已结束' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="cooperationDesc" label="合作描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewDetail(row)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="合作申请" name="apply">
        <el-table
          v-loading="loading"
          :data="filteredSchoolList"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
          empty-text="暂无数据"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="schoolName" label="学校名称" min-width="200" />
          <el-table-column prop="schoolCode" label="学校代码" width="150" />
          <el-table-column prop="address" label="地址" min-width="200" show-overflow-tooltip />
          <el-table-column prop="contactPerson" label="负责人" width="120">
            <template #default="{ row }">
              {{ row.contactPerson || row.managerName || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="contactPhone" label="联系电话" width="150">
            <template #default="{ row }">
              {{ row.contactPhone || row.managerPhone || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleApplyToSchool(row)">
                申请合作
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 申请合作对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      title="申请合作"
      width="600px"
      append-to-body
    >
      <el-form
        ref="applyFormRef"
        :model="applyForm"
        :rules="applyFormRules"
        label-width="120px"
      >
        <el-form-item label="选择学校" prop="schoolId">
          <el-select
            v-model="applyForm.schoolId"
            placeholder="请选择学校"
            filterable
            style="width: 100%;"
            :loading="schoolListLoading"
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
          <el-select
            v-model="applyForm.cooperationType"
            placeholder="请选择合作类型"
            style="width: 100%;"
          >
            <el-option label="实习基地" value="实习基地" />
            <el-option label="校企合作" value="校企合作" />
            <el-option label="人才培养" value="人才培养" />
            <el-option label="科研合作" value="科研合作" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作开始时间" prop="startTime">
          <el-date-picker
            v-model="applyForm.startTime"
            type="datetime"
            placeholder="选择合作开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="合作结束时间" prop="endTime">
          <el-date-picker
            v-model="applyForm.endTime"
            type="datetime"
            placeholder="选择合作结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="合作描述" prop="cooperationDesc">
          <el-input
            v-model="applyForm.cooperationDesc"
            type="textarea"
            :rows="4"
            placeholder="请输入合作描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitApply">
          提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看合作详情对话框 -->
    <el-dialog
      v-model="cooperationDetailDialogVisible"
      title="合作详情"
      width="700px"
      append-to-body
    >
      <el-descriptions :column="2" border v-if="currentCooperation">
        <el-descriptions-item label="学校名称" :span="2">
          {{ currentCooperation.schoolName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="合作类型">
          {{ currentCooperation.cooperationType || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="合作状态">
          <el-tag :type="currentCooperation.cooperationStatus === 1 ? 'success' : 'info'" size="small">
            {{ currentCooperation.cooperationStatus === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="合作开始时间" :span="2">
          {{ formatDateTime(currentCooperation.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="合作结束时间" :span="2">
          {{ formatDateTime(currentCooperation.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="合作描述" :span="2">
          {{ currentCooperation.cooperationDesc || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDateTime(currentCooperation.createTime) }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="cooperationDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { cooperationApplyApi } from '@/api/cooperationApply'
import { formatDateTime } from '@/utils/dateUtils'

export default {
  name: 'CooperationManagement',
  setup() {
    const activeTab = ref('cooperation')
    const loading = ref(false)
    const submitting = ref(false)
    const schoolListLoading = ref(false)
    const applyDialogVisible = ref(false)
    const applyFormRef = ref(null)
    
    const cooperationList = ref([])
    const applyList = ref([])
    const schoolList = ref([])
    const cooperationDetailDialogVisible = ref(false)
    const currentCooperation = ref(null)

    const searchForm = reactive({
      schoolName: '',
      schoolCode: '',
      cooperationType: ''
    })

    const applyForm = reactive({
      schoolId: null,
      cooperationType: '',
      startTime: null,
      endTime: null,
      cooperationDesc: ''
    })

    const applyFormRules = {
      schoolId: [
        { required: true, message: '请选择学校', trigger: 'change' }
      ],
      cooperationType: [
        { required: true, message: '请选择合作类型', trigger: 'change' }
      ],
      startTime: [
        { required: true, message: '请选择合作开始时间', trigger: 'change' }
      ],
      endTime: [
        { required: true, message: '请选择合作结束时间', trigger: 'change' }
      ],
      cooperationDesc: [
        { required: true, message: '请输入合作描述', trigger: 'blur' }
      ]
    }

    // 加载数据
    const loadData = async () => {
      loading.value = true
      try {
        if (activeTab.value === 'cooperation') {
          await loadCooperationList()
        } else {
          await loadSchoolList()
        }
      } catch (error) {
        ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    // 加载合作关系列表
    const loadCooperationList = async () => {
      try {
        const res = await cooperationApplyApi.getCooperationList()
        if (res.code === 200) {
          cooperationList.value = res.data || []
          // 学校名称已由后端填充，无需前端再次请求
        }
      } catch (error) {
        console.error('加载合作关系列表失败:', error)
        throw error
      }
    }

    // 加载申请列表
    const loadApplyList = async () => {
      try {
        const res = await cooperationApplyApi.getApplyList()
        if (res.code === 200) {
          applyList.value = res.data || []
          // 学校名称已由后端填充，无需前端再次请求
        }
      } catch (error) {
        console.error('加载申请列表失败:', error)
        throw error
      }
    }

    // 加载可申请学校列表
    const loadSchoolList = async () => {
      schoolListLoading.value = true
      try {
        const res = await cooperationApplyApi.getAvailableSchoolList()
        if (res.code === 200) {
          schoolList.value = res.data || []
        }
      } catch (error) {
        console.error('加载学校列表失败:', error)
        ElMessage.error('加载学校列表失败')
      } finally {
        schoolListLoading.value = false
      }
    }

    // 标签页切换
    const handleTabChange = () => {
      // 重置搜索表单
      searchForm.schoolName = ''
      searchForm.schoolCode = ''
      searchForm.cooperationType = ''
      loadData()
    }

    // 搜索
    const handleSearch = () => {
      // 前端过滤，无需重新加载数据
    }

    // 重置
    const handleReset = () => {
      searchForm.schoolName = ''
      searchForm.schoolCode = ''
      searchForm.cooperationType = ''
    }

    // 过滤数据
    const filteredCooperationList = computed(() => {
      let data = cooperationList.value
      if (searchForm.schoolName) {
        data = data.filter(item => 
          item.schoolName && item.schoolName.includes(searchForm.schoolName)
        )
      }
      if (searchForm.cooperationType) {
        data = data.filter(item => item.cooperationType === searchForm.cooperationType)
      }
      return data
    })

    const filteredSchoolList = computed(() => {
      let data = schoolList.value
      if (searchForm.schoolName) {
        data = data.filter(item => 
          item.schoolName && item.schoolName.includes(searchForm.schoolName)
        )
      }
      if (searchForm.schoolCode) {
        data = data.filter(item => 
          item.schoolCode && item.schoolCode.includes(searchForm.schoolCode)
        )
      }
      return data
    })

    // 申请合作（从顶部按钮点击）
    const handleApply = () => {
      applyForm.schoolId = null
      applyForm.cooperationType = ''
      applyForm.startTime = null
      applyForm.endTime = null
      applyForm.cooperationDesc = ''
      loadSchoolList()
      applyDialogVisible.value = true
    }
    
    // 申请合作（从表格行点击，已预选学校）
    const handleApplyToSchool = (school) => {
      applyForm.schoolId = school.schoolId
      applyForm.cooperationType = ''
      applyForm.startTime = null
      applyForm.endTime = null
      applyForm.cooperationDesc = ''
      applyDialogVisible.value = true
    }

    // 提交申请
    const handleSubmitApply = async () => {
      if (!applyFormRef.value) return
      
      try {
        await applyFormRef.value.validate()
      } catch (error) {
        return
      }

      submitting.value = true
      try {
        const res = await cooperationApplyApi.applyCooperation(applyForm)
        if (res.code === 200) {
          ElMessage.success('申请提交成功，等待学校审核')
          applyDialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '申请提交失败')
        }
      } catch (error) {
        ElMessage.error('申请提交失败：' + (error.response?.data?.message || error.message || '未知错误'))
      } finally {
        submitting.value = false
      }
    }

    // 查看详情
    const handleViewDetail = (row) => {
      currentCooperation.value = row
      cooperationDetailDialogVisible.value = true
    }

    // 查看申请详情
    const handleViewApplyDetail = (row) => {
      ElMessage.info('查看申请详情功能待开发')
    }

    // 获取申请状态类型
    const getApplyStatusType = (status) => {
      const statusMap = {
        0: 'warning',  // 待审核
        1: 'success', // 已通过
        2: 'danger'   // 已拒绝
      }
      return statusMap[status] || 'info'
    }

    // 获取申请状态文本
    const getApplyStatusText = (status) => {
      const statusMap = {
        0: '待审核',
        1: '已通过',
        2: '已拒绝'
      }
      return statusMap[status] || '未知'
    }

    onMounted(() => {
      loadData()
    })

    return {
      activeTab,
      loading,
      submitting,
      schoolListLoading,
      applyDialogVisible,
      applyFormRef,
      cooperationList,
      applyList,
      schoolList,
      applyForm,
      applyFormRules,
      handleTabChange,
      handleApply,
      handleApplyToSchool,
      handleSubmitApply,
      handleViewDetail,
      cooperationDetailDialogVisible,
      currentCooperation,
      searchForm,
      handleSearch,
      handleReset,
      filteredCooperationList,
      filteredSchoolList,
      formatDateTime,
      Plus,
      Search,
      Refresh
    }
  }
}
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
</style>

