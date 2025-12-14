<template>
  <PageLayout title="企业信息管理">
    <template #actions>
      <el-button 
        v-if="!isEditMode" 
        type="primary" 
        :icon="EditPen" 
        @click="handleEdit"
      >
        编辑信息
      </el-button>
      <el-button 
        v-if="isEditMode" 
        type="success" 
        :icon="Check" 
        :loading="submitting"
        @click="handleSave"
      >
        保存
      </el-button>
      <el-button 
        v-if="isEditMode" 
        :icon="Close" 
        @click="handleCancel"
      >
        取消
      </el-button>
    </template>

    <!-- 加载状态 -->
    <div v-if="loading" style="text-align: center; padding: 40px;">
      <el-icon class="is-loading" style="font-size: 40px;"><Loading /></el-icon>
      <p style="margin-top: 10px; color: #909399;">加载中...</p>
    </div>

    <!-- 企业信息表单 -->
    <el-form
      v-else
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="140px"
      :disabled="!isEditMode"
    >
      <!-- 基本信息 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <div style="display: flex; align-items: center;">
            <el-icon style="margin-right: 8px;"><InfoFilled /></el-icon>
            <span style="font-weight: 500;">基本信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业名称" prop="enterpriseName">
              <el-input 
                v-model="formData.enterpriseName" 
                placeholder="请输入企业名称"
                :disabled="true"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="企业代码" prop="enterpriseCode">
              <el-input 
                v-model="formData.enterpriseCode" 
                placeholder="企业代码"
                :disabled="true"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
              <el-input 
                v-model="formData.unifiedSocialCreditCode" 
                placeholder="请输入统一社会信用代码"
                maxlength="18"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="法人代表" prop="legalPerson">
              <el-input 
                v-model="formData.legalPerson" 
                placeholder="请输入法人代表"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="注册资本（万元）" prop="registeredCapital">
              <el-input-number
                v-model="formData.registeredCapital"
                :min="0"
                :precision="2"
                placeholder="请输入注册资本"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属行业" prop="industry">
              <el-input 
                v-model="formData.industry" 
                placeholder="请输入所属行业"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="企业地址" prop="address">
          <el-input 
            v-model="formData.address" 
            placeholder="请输入企业地址"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="企业规模" prop="enterpriseScale">
              <el-select 
                v-model="formData.enterpriseScale" 
                placeholder="请选择企业规模" 
                style="width: 100%"
              >
                <el-option label="大型企业（500人以上）" value="大型企业" />
                <el-option label="中型企业（100-500人）" value="中型企业" />
                <el-option label="小型企业（20-100人）" value="小型企业" />
                <el-option label="微型企业（20人以下）" value="微型企业" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="经营范围" prop="businessScope">
          <el-input
            v-model="formData.businessScope"
            type="textarea"
            :rows="3"
            placeholder="请输入经营范围"
          />
        </el-form-item>
      </el-card>

      <!-- 联系信息 -->
      <el-card shadow="never" style="margin-bottom: 20px;">
        <template #header>
          <div style="display: flex; align-items: center;">
            <el-icon style="margin-right: 8px;"><Phone /></el-icon>
            <span style="font-weight: 500;">联系信息</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系人" prop="contactPerson">
              <el-input 
                v-model="formData.contactPerson" 
                placeholder="请输入联系人"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input 
                v-model="formData.contactPhone" 
                placeholder="请输入联系电话"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="联系邮箱" prop="contactEmail">
          <el-input 
            v-model="formData.contactEmail" 
            placeholder="请输入联系邮箱"
          />
        </el-form-item>
      </el-card>
    </el-form>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  EditPen, 
  Check, 
  Close, 
  InfoFilled, 
  Phone, 
  DocumentChecked,
  Loading
} from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { enterpriseApi } from '@/api/user/enterprise'
import { userApi } from '@/api/user/user'
import { formatDateTime } from '@/utils/dateUtils'
import { useAuthStore } from '@/store/modules/auth'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)
const formData = reactive({
  enterpriseId: null,
  userId: null,
  enterpriseName: '',
  enterpriseCode: '',
  unifiedSocialCreditCode: '',
  legalPerson: '',
  registeredCapital: null,
  industry: '',
  address: '',
  contactPerson: '',
  contactPhone: '',
  contactEmail: '',
  enterpriseScale: '',
  businessScope: '',
  auditStatus: null,
  auditOpinion: '',
  auditTime: null,
  status: 1,
  createTime: null,
  updateTime: null
})

// 原始数据（用于取消编辑时恢复）
const originalData = ref(null)

// 表单验证规则
const formRules = {
  enterpriseName: [
    { required: true, message: '请输入企业名称', trigger: 'blur' }
  ],
  enterpriseCode: [
    { required: true, message: '企业代码不能为空', trigger: 'blur' }
  ],
  contactPhone: [
    { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入正确的联系电话', trigger: 'blur' }
  ],
  contactEmail: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

// 加载企业信息
const loadEnterpriseInfo = async () => {
  loading.value = true
  try {
    // 获取当前用户信息
    const userRes = await userApi.getCurrentUser()
    if (userRes.code !== 200 || !userRes.data || !userRes.data.userId) {
      ElMessage.error('获取用户信息失败')
      return
    }

    const userId = userRes.data.userId

    // 根据用户ID获取企业信息
    const enterpriseRes = await enterpriseApi.getEnterpriseByUserId(userId)
    if (enterpriseRes.code !== 200 || !enterpriseRes.data) {
      ElMessage.error('获取企业信息失败')
      return
    }

    const enterprise = enterpriseRes.data

    // 填充表单数据
    Object.assign(formData, {
      enterpriseId: enterprise.enterpriseId,
      userId: enterprise.userId,
      enterpriseName: enterprise.enterpriseName || '',
      enterpriseCode: enterprise.enterpriseCode || '',
      unifiedSocialCreditCode: enterprise.unifiedSocialCreditCode || '',
      legalPerson: enterprise.legalPerson || '',
      registeredCapital: enterprise.registeredCapital || null,
      industry: enterprise.industry || '',
      address: enterprise.address || '',
      contactPerson: enterprise.contactPerson || '',
      contactPhone: enterprise.contactPhone || '',
      contactEmail: enterprise.contactEmail || '',
      enterpriseScale: enterprise.enterpriseScale || '',
      businessScope: enterprise.businessScope || '',
      auditStatus: enterprise.auditStatus,
      auditOpinion: enterprise.auditOpinion || '',
      auditTime: enterprise.auditTime,
      status: enterprise.status,
      createTime: enterprise.createTime,
      updateTime: enterprise.updateTime
    })

    // 保存原始数据
    originalData.value = JSON.parse(JSON.stringify(formData))
  } catch (error) {
    console.error('加载企业信息失败:', error)
    ElMessage.error('加载企业信息失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 进入编辑模式
const handleEdit = () => {
  isEditMode.value = true
  // 保存当前数据作为原始数据（如果还没有保存过）
  if (!originalData.value) {
    originalData.value = JSON.parse(JSON.stringify(formData))
  }
}

// 取消编辑
const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消编辑吗？未保存的修改将丢失。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 恢复原始数据
    if (originalData.value) {
      Object.assign(formData, originalData.value)
    }
    isEditMode.value = false

    // 清除表单验证状态
    if (formRef.value) {
      formRef.value.clearValidate()
    }
  } catch {
    // 用户取消
  }
}

// 保存企业信息
const handleSave = async () => {
  if (!formRef.value) {
    return
  }

  // 表单验证
  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.warning('请检查表单填写是否正确')
    return
  }

  submitting.value = true
  try {
    // 构建更新数据（只包含可修改的字段）
    const updateData = {
      enterpriseId: formData.enterpriseId,
      userId: formData.userId,
      enterpriseName: formData.enterpriseName,
      enterpriseCode: formData.enterpriseCode,
      unifiedSocialCreditCode: formData.unifiedSocialCreditCode,
      legalPerson: formData.legalPerson,
      registeredCapital: formData.registeredCapital,
      industry: formData.industry,
      address: formData.address,
      contactPerson: formData.contactPerson,
      contactPhone: formData.contactPhone,
      contactEmail: formData.contactEmail,
      enterpriseScale: formData.enterpriseScale,
      businessScope: formData.businessScope
    }

    const res = await enterpriseApi.updateEnterprise(updateData)
    if (res.code === 200) {
      ElMessage.success('企业信息更新成功')
      
      // 更新原始数据
      originalData.value = JSON.parse(JSON.stringify(formData))
      isEditMode.value = false

      // 重新加载数据以获取最新的更新时间等信息
      await loadEnterpriseInfo()
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新企业信息失败:', error)
    ElMessage.error('更新企业信息失败：' + (error.message || '未知错误'))
  } finally {
    submitting.value = false
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadEnterpriseInfo()
})
</script>

<style scoped>
:deep(.el-card__header) {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-descriptions) {
  margin-top: 0;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}
</style>

