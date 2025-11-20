<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>企业注册</h2>
          <p>请填写企业信息完成注册，注册后需要等待学校管理员审核</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="140px"
        size="large"
      >
        <el-form-item label="企业名称" prop="enterpriseName">
          <el-input
            v-model="registerForm.enterpriseName"
            placeholder="请输入企业名称"
            clearable
          />
        </el-form-item>

        <el-form-item label="统一社会信用代码" prop="unifiedSocialCreditCode">
          <el-input
            v-model="registerForm.unifiedSocialCreditCode"
            placeholder="请输入统一社会信用代码"
            clearable
            maxlength="18"
          />
        </el-form-item>

        <el-form-item label="企业代码" prop="enterpriseCode">
          <el-input
            v-model="registerForm.enterpriseCode"
            placeholder="请输入企业代码（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="企业规模" prop="enterpriseScale">
          <el-select
            v-model="registerForm.enterpriseScale"
            placeholder="请选择企业规模"
            clearable
            style="width: 100%;"
          >
            <el-option label="大型企业（500人以上）" value="大型企业" />
            <el-option label="中型企业（100-500人）" value="中型企业" />
            <el-option label="小型企业（10-100人）" value="小型企业" />
            <el-option label="微型企业（10人以下）" value="微型企业" />
          </el-select>
        </el-form-item>

        <el-form-item label="行业类别" prop="industryCategory">
          <el-input
            v-model="registerForm.industryCategory"
            placeholder="请输入行业类别"
            clearable
          />
        </el-form-item>

        <el-form-item label="企业地址" prop="address">
          <el-input
            v-model="registerForm.address"
            type="textarea"
            :rows="2"
            placeholder="请输入企业地址"
            clearable
          />
        </el-form-item>

        <el-form-item label="联系人" prop="contactPerson">
          <el-input
            v-model="registerForm.contactPerson"
            placeholder="请输入联系人姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="联系电话" prop="contactPhone">
          <el-input
            v-model="registerForm.contactPhone"
            placeholder="请输入联系电话"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入企业邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="企业简介" prop="description">
          <el-input
            v-model="registerForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入企业简介（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="registering"
            @click="handleRegister"
            style="width: 100%;"
          >
            提交注册
          </el-button>
        </el-form-item>

        <el-form-item>
          <div class="register-tips">
            <el-alert
              title="注册说明"
              type="info"
              :closable="false"
            >
              <template #default>
                <p>1. 请填写真实的企业信息</p>
                <p>2. 提交注册后需要等待学校管理员审核</p>
                <p>3. 审核通过后，系统将发送账号信息到您的邮箱</p>
                <p>4. 审核结果将通过邮件或短信通知</p>
              </template>
            </el-alert>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { enterpriseApi } from '@/api/user/enterprise'

export default {
  name: 'EnterpriseRegister',
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const registering = ref(false)

    const registerForm = reactive({
      enterpriseName: '',
      unifiedSocialCreditCode: '',
      enterpriseCode: '',
      enterpriseScale: '',
      industryCategory: '',
      address: '',
      contactPerson: '',
      contactPhone: '',
      email: '',
      description: ''
    })

    // 验证规则
    const registerRules = {
      enterpriseName: [
        { required: true, message: '请输入企业名称', trigger: 'blur' }
      ],
      unifiedSocialCreditCode: [
        { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
        { pattern: /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}$/, message: '请输入正确的统一社会信用代码', trigger: 'blur' }
      ],
      contactPerson: [
        { required: true, message: '请输入联系人', trigger: 'blur' }
      ],
      contactPhone: [
        { required: true, message: '请输入联系电话', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$|^0\d{2,3}-?\d{7,8}$/, message: '请输入正确的联系电话', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ]
    }

    // 提交注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return

      await registerFormRef.value.validate(async (valid) => {
        if (!valid) {
          return false
        }

        registering.value = true
        try {
          const res = await enterpriseApi.registerEnterprise(registerForm)
          if (res.code === 200) {
            ElMessage.success('注册成功，请等待学校管理员审核')
            setTimeout(() => {
              router.push('/login')
            }, 2000)
          } else {
            ElMessage.error(res.message || '注册失败')
          }
        } catch (error) {
          ElMessage.error('注册失败：' + (error.message || '未知错误'))
        } finally {
          registering.value = false
        }
      })
    }

    return {
      registerFormRef,
      registerForm,
      registerRules,
      registering,
      handleRegister
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 700px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-tips {
  margin-top: 20px;
}

.register-tips p {
  margin: 5px 0;
  font-size: 13px;
}
</style>

