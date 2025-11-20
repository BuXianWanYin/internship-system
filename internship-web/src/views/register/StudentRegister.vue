<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <h2>学生注册</h2>
          <p>请填写个人信息并输入班级分享码完成注册</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="120px"
        size="large"
      >
        <el-form-item label="班级分享码" prop="shareCode">
          <el-input
            v-model="registerForm.shareCode"
            placeholder="请输入班级分享码"
            @blur="validateShareCode"
            clearable
          >
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
          </el-input>
          <div v-if="shareCodeInfo" class="share-code-info">
            <el-alert
              :title="`班级：${shareCodeInfo.className}`"
              type="success"
              :closable="false"
              show-icon
            />
          </div>
          <div v-if="shareCodeError" class="share-code-error">
            <el-alert
              :title="shareCodeError"
              type="error"
              :closable="false"
              show-icon
            />
          </div>
        </el-form-item>

        <el-divider />

        <el-form-item label="学号" prop="studentNo">
          <el-input
            v-model="registerForm.studentNo"
            placeholder="请输入学号"
            clearable
          />
        </el-form-item>

        <el-form-item label="姓名" prop="realName">
          <el-input
            v-model="registerForm.realName"
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="身份证号" prop="idCard">
          <el-input
            v-model="registerForm.idCard"
            placeholder="请输入身份证号"
            clearable
            maxlength="18"
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号"
            clearable
            maxlength="11"
          />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（可选）"
            clearable
          />
        </el-form-item>

        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-date-picker
            v-model="enrollmentYearDate"
            type="year"
            placeholder="选择入学年份"
            format="YYYY"
            value-format="YYYY"
            @change="handleEnrollmentYearChange"
            style="width: 100%;"
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
                <p>1. 请向班主任获取班级分享码</p>
                <p>2. 填写完整信息后提交注册申请</p>
                <p>3. 注册后需要等待班主任审核通过后才能登录</p>
                <p>4. 初始密码将通过短信或邮件发送（如已填写）</p>
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
import { Key } from '@element-plus/icons-vue'
import { studentApi } from '@/api/user/student'
import { classApi } from '@/api/system'

export default {
  name: 'StudentRegister',
  components: {
    Key
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const registering = ref(false)
    const shareCodeInfo = ref(null)
    const shareCodeError = ref('')
    const enrollmentYearDate = ref(null)

    const registerForm = reactive({
      shareCode: '',
      studentNo: '',
      realName: '',
      idCard: '',
      phone: '',
      email: '',
      enrollmentYear: null,
      classId: null
    })

    // 验证规则
    const registerRules = {
      shareCode: [
        { required: true, message: '请输入班级分享码', trigger: 'blur' }
      ],
      studentNo: [
        { required: true, message: '请输入学号', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      idCard: [
        { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      enrollmentYear: [
        { required: true, message: '请选择入学年份', trigger: 'change' }
      ]
    }

    // 验证分享码
    const validateShareCode = async () => {
      if (!registerForm.shareCode) {
        shareCodeInfo.value = null
        shareCodeError.value = ''
        return
      }

      try {
        const res = await classApi.validateShareCode(registerForm.shareCode)
        if (res.code === 200 && res.data) {
          shareCodeInfo.value = res.data
          registerForm.classId = res.data.classId
          shareCodeError.value = ''
        } else {
          shareCodeError.value = res.message || '分享码无效或已过期'
          shareCodeInfo.value = null
        }
      } catch (error) {
        shareCodeError.value = '分享码验证失败：' + (error.message || '未知错误')
        shareCodeInfo.value = null
      }
    }

    // 处理入学年份变化
    const handleEnrollmentYearChange = (value) => {
      if (value) {
        registerForm.enrollmentYear = parseInt(value)
      } else {
        registerForm.enrollmentYear = null
      }
    }

    // 提交注册
    const handleRegister = async () => {
      if (!registerFormRef.value) return

      await registerFormRef.value.validate(async (valid) => {
        if (!valid) {
          return false
        }

        if (!shareCodeInfo.value) {
          ElMessage.warning('请先验证班级分享码')
          return
        }

        registering.value = true
        try {
          const res = await studentApi.registerStudent(registerForm, registerForm.shareCode)
          if (res.code === 200) {
            ElMessage.success('注册成功，请等待班主任审核')
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
      shareCodeInfo,
      shareCodeError,
      enrollmentYearDate,
      validateShareCode,
      handleEnrollmentYearChange,
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
  max-width: 600px;
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

.share-code-info,
.share-code-error {
  margin-top: 10px;
}

.register-tips {
  margin-top: 20px;
}

.register-tips p {
  margin: 5px 0;
  font-size: 13px;
}
</style>

