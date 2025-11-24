<template>
  <PageLayout title="我的实习">
    <div v-if="loading" style="text-align: center; padding: 50px">
      <el-icon class="is-loading" style="font-size: 30px"><Loading /></el-icon>
    </div>
    
    <div v-else-if="!currentInternship" class="no-internship">
      <el-empty description="您当前没有正在进行的实习">
        <el-button type="primary" @click="goToApply">去申请实习</el-button>
      </el-empty>
    </div>
    
    <div v-else class="internship-info">
      <!-- 实习信息卡片 -->
      <el-card class="internship-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="card-title">当前实习信息</span>
            <el-tag :type="getInternshipStatusType(currentInternship.internshipStatus)" size="large">
              {{ getInternshipStatusText(currentInternship.internshipStatus) }}
            </el-tag>
          </div>
        </template>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="企业名称" :span="2">
            <strong>{{ currentInternship.enterpriseName || currentInternship.selfEnterpriseName || '-' }}</strong>
          </el-descriptions-item>
          <el-descriptions-item label="岗位名称">
            {{ currentInternship.postName || currentInternship.selfPostName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="企业导师">
            {{ currentInternship.mentorName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实习开始日期">
            {{ formatDate(currentInternship.internshipStartDate) || formatDate(currentInternship.selfStartDate) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="实习结束日期">
            {{ formatDate(currentInternship.internshipEndDate) || formatDate(currentInternship.selfEndDate) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="确认上岗时间">
            {{ formatDateTime(currentInternship.studentConfirmTime) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.enterpriseAddress || currentInternship.selfEnterpriseAddress" label="企业地址" :span="2">
            {{ currentInternship.enterpriseAddress || currentInternship.selfEnterpriseAddress }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.contactPerson || currentInternship.selfContactPerson" label="联系人">
            {{ currentInternship.contactPerson || currentInternship.selfContactPerson || '-' }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.contactPhone || currentInternship.selfContactPhone" label="联系电话">
            {{ currentInternship.contactPhone || currentInternship.selfContactPhone || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="action-buttons" style="margin-top: 20px">
          <el-button
            v-if="currentInternship.unbindStatus === 0 || currentInternship.unbindStatus === null"
            type="danger"
            :loading="unbindLoading"
            @click="showUnbindDialog = true"
          >
            申请离职
          </el-button>
          <el-button
            v-else-if="currentInternship.unbindStatus === 1"
            type="warning"
            disabled
          >
            离职申请审核中
          </el-button>
          <el-button
            v-else-if="currentInternship.unbindStatus === 3"
            type="info"
            disabled
          >
            离职申请被拒绝
          </el-button>
        </div>
      </el-card>
      
      <!-- 解绑信息（如果有） -->
      <el-card v-if="currentInternship.unbindStatus !== 0 && currentInternship.unbindStatus !== null" class="unbind-info-card" shadow="hover">
        <template #header>
          <span class="card-title">离职申请信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="离职原因" :span="2">
            {{ currentInternship.unbindReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态">
            <el-tag :type="getUnbindStatusType(currentInternship.unbindStatus)" size="small">
              {{ getUnbindStatusText(currentInternship.unbindStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.unbindAuditTime" label="审核时间">
            {{ formatDateTime(currentInternship.unbindAuditTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.unbindAuditOpinion" label="审核意见" :span="2">
            {{ currentInternship.unbindAuditOpinion }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    
    <!-- 申请离职对话框 -->
    <el-dialog
      v-model="showUnbindDialog"
      title="申请离职"
      width="500px"
    >
      <el-form :model="unbindForm" :rules="unbindFormRules" ref="unbindFormRef" label-width="100px">
        <el-form-item label="离职原因" prop="reason">
          <el-input
            v-model="unbindForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入离职原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          style="margin-top: 10px"
        >
          <template #default>
            <div>离职申请提交后，需要班主任或学院负责人审核通过后才能解绑。</div>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="showUnbindDialog = false">取消</el-button>
        <el-button type="danger" :loading="unbindLoading" @click="handleSubmitUnbind">提交申请</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const router = useRouter()
const loading = ref(false)
const unbindLoading = ref(false)
const showUnbindDialog = ref(false)
const currentInternship = ref(null)
const unbindFormRef = ref(null)

const unbindForm = reactive({
  reason: ''
})

const unbindFormRules = {
  reason: [
    { required: true, message: '请输入离职原因', trigger: 'blur' },
    { min: 5, message: '离职原因至少5个字符', trigger: 'blur' }
  ]
}

// 加载当前实习信息
const loadCurrentInternship = async () => {
  loading.value = true
  try {
    const res = await applyApi.getCurrentInternship()
    if (res.code === 200) {
      currentInternship.value = res.data
    } else {
      currentInternship.value = null
    }
  } catch (error) {
    console.error('加载当前实习信息失败:', error)
    currentInternship.value = null
  } finally {
    loading.value = false
  }
}

// 获取实习状态文本
const getInternshipStatusText = (status) => {
  const statusMap = {
    0: '未实习',
    1: '实习中',
    2: '已离职',
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取实习状态类型
const getInternshipStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取解绑状态文本
const getUnbindStatusText = (status) => {
  const statusMap = {
    0: '未申请',
    1: '申请中',
    2: '已解绑',
    3: '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 获取解绑状态类型
const getUnbindStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return typeMap[status] || 'info'
}

// 提交离职申请
const handleSubmitUnbind = async () => {
  if (!unbindFormRef.value) return
  
  await unbindFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!currentInternship.value || !currentInternship.value.applyId) {
      ElMessage.error('申请ID不存在')
      return
    }
    
    try {
      unbindLoading.value = true
      const res = await applyApi.applyUnbind(currentInternship.value.applyId, unbindForm.reason)
      if (res.code === 200) {
        ElMessage.success('离职申请提交成功，等待审核')
        showUnbindDialog.value = false
        unbindForm.reason = ''
        // 重新加载数据
        await loadCurrentInternship()
      }
    } catch (error) {
      console.error('提交离职申请失败:', error)
      ElMessage.error(error.response?.data?.message || '提交离职申请失败')
    } finally {
      unbindLoading.value = false
    }
  })
}

// 去申请实习
const goToApply = () => {
  router.push('/student/internship/apply')
}

onMounted(() => {
  loadCurrentInternship()
})
</script>

<style scoped>
.no-internship {
  padding: 50px 0;
}

.internship-info {
  max-width: 1200px;
}

.internship-card {
  margin-bottom: 20px;
}

.unbind-info-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.action-buttons {
  text-align: right;
}
</style>

