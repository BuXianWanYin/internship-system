<template>
  <PageLayout title="合作申请审核">
    <!-- 搜索栏 -->
    <div class="search-bar" style="margin-bottom: 20px;">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="企业名称">
          <el-input
            v-model="searchForm.enterpriseName"
            placeholder="请输入企业名称"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="合作类型">
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
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="filteredTableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      empty-text="暂无数据"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="enterpriseName" label="企业名称" min-width="200" />
      <el-table-column prop="cooperationType" label="合作类型" width="150" />
      <el-table-column label="合作时间" width="280">
        <template #default="{ row }">
          <div>{{ formatDateTime(row.startTime) }} 至 {{ formatDateTime(row.endTime) }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="cooperationDesc" label="合作描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="申请时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleAudit(row, 1)">
            通过
          </el-button>
          <el-button link type="danger" size="small" @click="handleAudit(row, 2)">
            拒绝
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="auditDialogVisible"
      :title="auditForm.auditStatus === 1 ? '审核通过' : '审核拒绝'"
      width="500px"
      append-to-body
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditFormRules"
        label-width="100px"
      >
        <el-form-item label="企业名称">
          <span>{{ currentApply && currentApply.enterpriseName }}</span>
        </el-form-item>
        <el-form-item label="合作类型">
          <span>{{ currentApply && currentApply.cooperationType }}</span>
        </el-form-item>
        <el-form-item label="合作时间">
          <span v-if="currentApply">
            {{ formatDateTime(currentApply.startTime) }} 至 {{ formatDateTime(currentApply.endTime) }}
          </span>
        </el-form-item>
        <el-form-item label="合作描述">
          <span>{{ currentApply && currentApply.cooperationDesc }}</span>
        </el-form-item>
        <el-form-item
          v-if="auditForm.auditStatus === 2"
          label="拒绝原因"
          prop="auditOpinion"
        >
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
        <el-form-item
          v-else
          label="审核意见"
        >
          <el-input
            v-model="auditForm.auditOpinion"
            type="textarea"
            :rows="4"
            placeholder="审核意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="auditing"
          @click="handleConfirmAudit"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { cooperationApplyApi } from '@/api/cooperationApply'
import { formatDateTime } from '@/utils/dateUtils'

export default {
  name: 'CooperationAudit',
  components: {
    PageLayout
  },
  setup() {
    const loading = ref(false)
    const auditing = ref(false)
    const auditDialogVisible = ref(false)
    const auditFormRef = ref(null)
    const tableData = ref([])
    const currentApply = ref(null)

    const searchForm = reactive({
      enterpriseName: '',
      cooperationType: ''
    })

    const auditForm = reactive({
      auditStatus: null,
      auditOpinion: ''
    })

    const auditFormRules = {
      auditOpinion: [
        { required: true, message: '请输入拒绝原因', trigger: 'blur' }
      ]
    }

    // 加载数据
    const loadData = async () => {
      loading.value = true
      try {
        const res = await cooperationApplyApi.getPendingApplyList()
        if (res.code === 200) {
          tableData.value = res.data || []
          // 企业名称已由后端填充，无需前端再次请求
        }
      } catch (error) {
        ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      loadData()
    }

    // 重置
    const handleReset = () => {
      searchForm.enterpriseName = ''
      searchForm.cooperationType = ''
      loadData()
    }
    
    // 过滤数据（前端过滤，因为后端已经返回了所有数据）
    const filteredTableData = computed(() => {
      let data = tableData.value
      if (searchForm.enterpriseName) {
        data = data.filter(item => 
          item.enterpriseName && item.enterpriseName.includes(searchForm.enterpriseName)
        )
      }
      if (searchForm.cooperationType) {
        data = data.filter(item => item.cooperationType === searchForm.cooperationType)
      }
      return data
    })

    // 审核
    const handleAudit = (row, auditStatus) => {
      currentApply.value = row
      auditForm.auditStatus = auditStatus
      auditForm.auditOpinion = ''
      auditDialogVisible.value = true
    }

    // 确认审核
    const handleConfirmAudit = async () => {
      if (auditForm.auditStatus === 2 && !auditForm.auditOpinion) {
        if (!auditFormRef.value) return
        try {
          await auditFormRef.value.validate()
        } catch (error) {
          return
        }
      }

      auditing.value = true
      try {
        const res = await cooperationApplyApi.auditCooperationApply(
          currentApply.value.id,
          auditForm.auditStatus,
          auditForm.auditOpinion
        )
        if (res.code === 200) {
          ElMessage.success('审核成功')
          auditDialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '审核失败')
        }
      } catch (error) {
        ElMessage.error('审核失败：' + (error.response?.data?.message || error.message || '未知错误'))
      } finally {
        auditing.value = false
      }
    }

    onMounted(() => {
      loadData()
    })

    return {
      loading,
      auditing,
      auditDialogVisible,
      auditFormRef,
      tableData,
      currentApply,
      searchForm,
      auditForm,
      auditFormRules,
      handleSearch,
      handleReset,
      handleAudit,
      handleConfirmAudit,
      formatDateTime,
      filteredTableData,
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

