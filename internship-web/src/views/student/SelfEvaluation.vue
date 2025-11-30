<template>
  <PageLayout title="自我评价">
    <el-card v-loading="loading" shadow="never">
      <div v-if="!formData.applyId && !loading">
        <el-empty description="暂无评价信息，请在实习结束后填写自我评价" />
      </div>
      
      <div v-else-if="formData.applyId">
        <!-- 实习信息 -->
        <div class="info-section">
          <h3>实习信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="企业名称">{{ internshipInfo.enterpriseName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="实习时间">
              {{ internshipInfo.startDate || '-' }} 至 {{ internshipInfo.endDate || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="实习岗位">{{ internshipInfo.postName || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 自评表单 -->
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="150px"
          style="margin-top: 20px"
        >
          <el-form-item label="自我反思和总结" prop="reflectionSummary">
            <RichTextEditor
              v-model="formData.reflectionSummary"
              :disabled="isSubmitted"
              placeholder="请填写：&#10;1. 实习期间的主要工作内容和收获&#10;2. 遇到的困难和解决方法&#10;3. 对实习过程的反思和总结&#10;4. 未来的职业规划"
              :height="'400px'"
            />
          </el-form-item>

          <el-form-item label="自评分数" prop="selfScore">
            <el-input-number
              v-model="formData.selfScore"
              :min="0"
              :max="100"
              :precision="2"
              :controls="false"
              :disabled="isSubmitted"
              placeholder="请输入分数（0-100）"
              style="width: 100%"
            />
            <div style="margin-top: 5px; font-size: 12px; color: #909399;">
              说明：请根据自己在实习期间的表现，给自己打分（0-100分）
            </div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="saving" :disabled="isSubmitted" @click="handleSaveDraft">保存草稿</el-button>
            <el-button type="success" :loading="submitting" :disabled="isSubmitted" @click="handleSubmit">提交自评</el-button>
            <el-alert
              v-if="isSubmitted"
              type="success"
              :closable="false"
              show-icon
              style="margin-top: 10px"
            >
              <template #title>
                <span>评价已提交，无法修改</span>
              </template>
            </el-alert>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageLayout from '@/components/common/PageLayout.vue'
import RichTextEditor from '@/components/common/RichTextEditor.vue'
import { selfEvaluationApi } from '@/api/evaluation/self'
import { applyApi } from '@/api/internship/apply'
import { formatDate } from '@/utils/dateUtils'

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const evaluation = ref(null)
const internshipInfo = ref({
  enterpriseName: '',
  startDate: '',
  endDate: '',
  postName: ''
})

const formData = reactive({
  applyId: null,
  selfScore: null,
  reflectionSummary: ''
})

const formRules = {
  selfScore: [
    { required: true, message: '请输入自评分数', trigger: 'blur' },
    { type: 'number', min: 0, max: 100, message: '分数必须在0-100之间', trigger: 'blur' }
  ],
  reflectionSummary: [
    { required: true, message: '请填写自我反思和总结', trigger: 'blur' }
  ]
}

// 判断评价是否已提交（evaluationStatus === 1 表示已提交）
const isSubmitted = computed(() => {
  return evaluation.value && evaluation.value.evaluationStatus === 1
})

// 加载实习信息和评价
const loadData = async () => {
  loading.value = true
  try {
    // 先查询已结束的实习（合作企业：status=7，自主实习：status=13）
    const completedRes = await applyApi.getApplyPage({
      current: 1,
      size: 10
      // 不限制status，查询所有，然后过滤
    })
    
    let endedApply = null
    
    if (completedRes.code === 200 && completedRes.data && completedRes.data.records) {
      // 查找实习已结束的申请（status=7 或 status=13）
      endedApply = completedRes.data.records.find(apply => {
        // 合作企业：status=7
        if (apply.applyType === 1 && apply.status === 7) {
          return true
        }
        // 自主实习：status=13
        if (apply.applyType === 2 && apply.status === 13) {
          return true
        }
        // 如果没有标记为结束，检查结束日期（兼容旧逻辑）
        if (apply.internshipEndDate) {
          const today = new Date()
          today.setHours(0, 0, 0, 0)
          const endDate = new Date(apply.internshipEndDate)
          endDate.setHours(0, 0, 0, 0)
          if (endDate <= today && (apply.status === 3 || apply.status === 11)) {
            return true
          }
        }
        return false
      })
    }
    
    if (endedApply) {
      formData.applyId = endedApply.applyId
      
      // 填充实习信息
      internshipInfo.value = {
        enterpriseName: endedApply.enterpriseName || endedApply.selfEnterpriseName || '-',
        startDate: endedApply.internshipStartDate ? formatDate(endedApply.internshipStartDate) : (endedApply.selfStartDate ? formatDate(endedApply.selfStartDate) : '-'),
        endDate: endedApply.internshipEndDate ? formatDate(endedApply.internshipEndDate) : (endedApply.selfEndDate ? formatDate(endedApply.selfEndDate) : '-'),
        postName: endedApply.postName || endedApply.selfPostName || '-'
      }
      
      // 加载已有评价
      if (endedApply.applyId) {
        const evalRes = await selfEvaluationApi.getEvaluationByApplyId(endedApply.applyId)
        if (evalRes.code === 200 && evalRes.data) {
          evaluation.value = evalRes.data
          formData.selfScore = evalRes.data.selfScore
          formData.reflectionSummary = evalRes.data.reflectionSummary || ''
        }
      }
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    saving.value = true
    try {
      const data = {
        applyId: formData.applyId,
        selfScore: formData.selfScore,
        reflectionSummary: formData.reflectionSummary,
        evaluationStatus: 0 // 草稿
      }
      
      if (evaluation.value && evaluation.value.evaluationId) {
        data.evaluationId = evaluation.value.evaluationId
      }
      
      const res = await selfEvaluationApi.saveOrUpdateEvaluation(data)
      if (res.code === 200) {
        ElMessage.success('保存草稿成功')
        evaluation.value = res.data
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    } finally {
      saving.value = false
    }
  })
}

// 提交自评
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      await ElMessageBox.confirm('提交后将无法修改，确认提交吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      
      submitting.value = true
      try {
        // 先保存
        const data = {
          applyId: formData.applyId,
          selfScore: formData.selfScore,
          reflectionSummary: formData.reflectionSummary,
          evaluationStatus: 1 // 已提交
        }
        
        if (evaluation.value && evaluation.value.evaluationId) {
          data.evaluationId = evaluation.value.evaluationId
          const saveRes = await selfEvaluationApi.saveOrUpdateEvaluation(data)
          if (saveRes.code !== 200) {
            ElMessage.error(saveRes.message || '保存失败')
            return
          }
          evaluation.value = saveRes.data
        } else {
          const saveRes = await selfEvaluationApi.saveOrUpdateEvaluation(data)
          if (saveRes.code !== 200) {
            ElMessage.error(saveRes.message || '保存失败')
            return
          }
          evaluation.value = saveRes.data
        }
        
        // 提交
        const submitRes = await selfEvaluationApi.submitEvaluation(evaluation.value.evaluationId)
        if (submitRes.code === 200) {
          ElMessage.success('提交成功')
          await loadData()
        } else {
          ElMessage.error(submitRes.message || '提交失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('提交失败：' + (error.message || '未知错误'))
        }
      } finally {
        submitting.value = false
      }
    } catch (error) {
      // 用户取消
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.info-section {
  margin-bottom: 20px;
}

.info-section h3 {
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
</style>

