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
            <div style="display: flex; align-items: center; gap: 10px">
              <el-tag :type="getInternshipStatusType(currentInternship.studentInternshipStatus)" size="large">
                {{ getInternshipStatusText(currentInternship.studentInternshipStatus) }}
              </el-tag>
              <!-- 如果实习已结束，显示结束日期 -->
              <el-tag
                v-if="isInternshipCompleted(currentInternship)"
                type="info"
                size="small"
              >
                实习已结束（{{ formatDate(currentInternship.internshipEndDate) }}）
              </el-tag>
              <el-tag 
                v-else-if="currentInternship.studentConfirmStatus === 1 && (currentInternship.unbindStatus === null || currentInternship.unbindStatus === 0 || currentInternship.unbindStatus === 3)" 
                type="success" 
                size="small"
              >
                已确认上岗
              </el-tag>
              <el-tag 
                v-else-if="currentInternship.studentConfirmStatus === 0 && (currentInternship.unbindStatus === null || currentInternship.unbindStatus === 0)" 
                type="warning" 
                size="small"
              >
                待确认上岗
              </el-tag>
            </div>
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
          <!-- 如果是实习结束，显示结束日期和备注 -->
          <el-descriptions-item v-if="isInternshipCompleted(currentInternship)" label="实习结束日期">
            {{ formatDate(currentInternship.internshipEndDate) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="isInternshipCompleted(currentInternship) && currentInternship.unbindAuditOpinion" label="结束备注" :span="2">
            {{ currentInternship.unbindAuditOpinion }}
          </el-descriptions-item>
          <el-descriptions-item label="学生确认状态">
            <el-tag 
              v-if="currentInternship.unbindStatus === 2" 
              type="info" 
              size="small"
            >
              已解绑
            </el-tag>
            <el-tag 
              v-else-if="currentInternship.studentInternshipStatus === 1" 
              type="success" 
              size="small"
            >
              已确认上岗
            </el-tag>
            <el-tag 
              v-else 
              type="warning" 
              size="small"
            >
              待确认上岗
            </el-tag>
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
        
        <div class="action-buttons" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center">
          <div>
            <el-alert
              v-if="currentInternship.unbindStatus === 2"
              type="info"
              :closable="false"
              show-icon
            >
              <template #title>
                <span>您已与企业解绑，如需重新实习，请联系班主任</span>
              </template>
            </el-alert>
          </div>
          <div style="display: flex; gap: 10px">
            <el-button 
              v-if="currentInternship.applyId && (currentInternship.unbindStatus === null || currentInternship.unbindStatus === 0 || currentInternship.unbindStatus === 3)"
              type="danger" 
              @click="handleResign"
              :loading="resignLoading"
            >
              离职
            </el-button>
          </div>
        </div>
      </el-card>
      
      <!-- 解绑信息（如果有） -->
      <el-card v-if="currentInternship.unbindStatus === 2" class="unbind-info-card" shadow="hover">
        <template #header>
          <span class="card-title">解绑信息</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="解绑原因" :span="2">
            {{ currentInternship.unbindReason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="解绑状态">
            <el-tag :type="getUnbindStatusType(currentInternship.unbindStatus)" size="small">
              {{ getUnbindStatusText(currentInternship.unbindStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.unbindAuditTime" label="解绑时间">
            {{ formatDateTime(currentInternship.unbindAuditTime) }}
          </el-descriptions-item>
          <el-descriptions-item v-if="currentInternship.unbindAuditOpinion" label="解绑备注" :span="2">
            {{ currentInternship.unbindAuditOpinion }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
      
      <!-- 自我评价入口（实习结束时显示） -->
      <el-card v-if="isInternshipCompleted(currentInternship)" class="self-evaluation-card" shadow="hover">
        <template #header>
          <span class="card-title">自我评价</span>
        </template>
        <div class="self-evaluation-content">
          <el-alert
            type="info"
            :closable="false"
            show-icon
          >
            <template #title>
              <span>实习已结束，请填写自我评价，总结实习期间的收获和反思</span>
            </template>
          </el-alert>
          <div style="margin-top: 15px; text-align: right">
            <el-button type="primary" @click="goToSelfEvaluation">
              填写自我评价
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
  </PageLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Loading } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageLayout from '@/components/common/PageLayout.vue'

const router = useRouter()
const loading = ref(false)
const resignLoading = ref(false)
const currentInternship = ref(null)

// 加载当前实习信息（包括已结束的实习）
const loadCurrentInternship = async () => {
  loading.value = true
  try {
    // 先尝试获取当前实习（正在进行的）
    const res = await applyApi.getCurrentInternship()
    if (res.code === 200 && res.data) {
      currentInternship.value = res.data
    } else {
      // 如果当前实习不存在，查询已结束的实习
      const applyRes = await applyApi.getApplyPage({
        current: 1,
        size: 10
      })
      
      if (applyRes.code === 200 && applyRes.data && applyRes.data.records) {
        // 查找最近已结束的实习（status=7 或 status=13）
        const completedApply = applyRes.data.records.find(apply => {
          // 合作企业：status=7
          if (apply.applyType === 1 && apply.status === 7) {
            return true
          }
          // 自主实习：status=13
          if (apply.applyType === 2 && apply.status === 13) {
            return true
          }
          return false
        })
        
        if (completedApply) {
          currentInternship.value = completedApply
        } else {
          currentInternship.value = null
        }
      } else {
        currentInternship.value = null
      }
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
    3: '已结束'
  }
  return statusMap[status] || '未知'
}

// 获取实习状态类型
const getInternshipStatusType = (status) => {
  // 处理 null 或 undefined，默认为 info（未实习）
  if (status === null || status === undefined) {
    return 'info'
  }
  const typeMap = {
    0: 'info',
    1: 'success',
    3: 'info'
  }
  return typeMap[status] || 'info'
}

// 获取解绑状态文本
const getUnbindStatusText = (status) => {
  const statusMap = {
    0: '未解绑',
    2: '已解绑'
  }
  return statusMap[status] || '未知'
}

// 获取解绑状态类型
const getUnbindStatusType = (status) => {
  const typeMap = {
    0: 'info',
    2: 'success'
  }
  return typeMap[status] || 'info'
}

// 判断实习是否已结束
const isInternshipCompleted = (internship) => {
  if (!internship) return false
  // 合作企业：status=7
  if (internship.applyType === 1) {
    return internship.status === 7
  }
  // 自主实习：status=13
  if (internship.applyType === 2) {
    return internship.status === 13
  }
  return false
}

// 去申请实习
const goToApply = () => {
  router.push('/student/internship/apply')
}

// 去填写自我评价
const goToSelfEvaluation = () => {
  router.push('/student/evaluation/self')
}

// 离职
const handleResign = async () => {
  if (!currentInternship.value || !currentInternship.value.applyId) {
    ElMessage.warning('暂无实习申请信息')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确认已从该公司离职吗？',
      '离职确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    resignLoading.value = true
    try {
      const res = await applyApi.unbindInternship(
        currentInternship.value.applyId,
        '学生主动离职',
        undefined
      )
      if (res.code === 200) {
        ElMessage.success('离职成功')
        // 重新加载当前实习信息
        await loadCurrentInternship()
      }
    } catch (error) {
      console.error('离职失败:', error)
      ElMessage.error(error.response?.data?.message || '离职失败')
    } finally {
      resignLoading.value = false
    }
  } catch (error) {
    // 用户取消
  }
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

.self-evaluation-card {
  margin-top: 20px;
}

.self-evaluation-content {
  padding: 0;
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

