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

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="已建立合作" name="cooperation">
        <el-table
          v-loading="loading"
          :data="cooperationList"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
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
          :data="applyList"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
          <el-table-column prop="schoolName" label="学校名称" min-width="200" />
          <el-table-column prop="cooperationType" label="合作类型" width="150" />
          <el-table-column label="合作时间" width="280">
            <template #default="{ row }">
              <div>{{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column prop="applyStatus" label="申请状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getApplyStatusType(row.applyStatus)" size="small">
                {{ getApplyStatusText(row.applyStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditOpinion" label="审核意见" min-width="200" show-overflow-tooltip />
          <el-table-column prop="createTime" label="申请时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleViewApplyDetail(row)">
                查看详情
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
  </PageLayout>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
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
          await loadApplyList()
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
      loadData()
    }

    // 申请合作
    const handleApply = () => {
      applyForm.schoolId = null
      applyForm.cooperationType = ''
      applyForm.startTime = null
      applyForm.endTime = null
      applyForm.cooperationDesc = ''
      loadSchoolList()
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
      ElMessage.info('查看详情功能待开发')
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
      handleSubmitApply,
      handleViewDetail,
      handleViewApplyDetail,
      getApplyStatusType,
      getApplyStatusText,
      formatDateTime,
      Plus
    }
  }
}
</script>

<style scoped>
</style>

