<template>
  <div class="register-container">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <div class="header-top">
            <el-link type="primary" @click="goBack" class="back-link">
              <el-icon><ArrowLeft /></el-icon>
              返回
            </el-link>
          </div>
          <h2>学生注册</h2>
          <p>请填写个人信息并输入班级分享码完成注册</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="120px"
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
            placeholder="请输入姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-select
            v-model="registerForm.gender"
            placeholder="请选择性别"
            style="width: 100%;"
            clearable
          >
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
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
            placeholder="请输入邮箱"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码（6-20位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-input
            v-model="registerForm.enrollmentYear"
            placeholder="请先验证班级分享码"
            :disabled="!shareCodeInfo"
            readonly
            style="width: 100%;"
          >
            <template #prefix>
              <el-icon><Calendar /></el-icon>
            </template>
          </el-input>
          <div v-if="!shareCodeInfo" class="form-tip">入学年份将根据班级分享码自动获取</div>
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
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Key, ArrowLeft, Calendar } from '@element-plus/icons-vue'
import { studentApi } from '@/api/user/student'
import { classApi } from '@/api/system'

export default {
  name: 'StudentRegister',
  components: {
    Key,
    Calendar
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const registering = ref(false)
    const shareCodeInfo = ref(null)
    const shareCodeError = ref('')

    const registerForm = reactive({
      shareCode: '',
      studentNo: '',
      realName: '',
      gender: '',
      idCard: '',
      phone: '',
      email: '',
      password: '',
      confirmPassword: '',
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
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ],
      enrollmentYear: [
        { required: true, message: '请先验证班级分享码以获取入学年份', trigger: 'blur' }
      ]
    }

    // 验证分享码
    const validateShareCode = async () => {
      if (!registerForm.shareCode) {
        shareCodeInfo.value = null
        shareCodeError.value = ''
        registerForm.enrollmentYear = null
        return
      }

      try {
        const res = await classApi.validateShareCode(registerForm.shareCode)
        if (res.code === 200 && res.data) {
          shareCodeInfo.value = res.data
          registerForm.classId = res.data.classId
          // 自动设置入学年份（从班级信息中获取）
          if (res.data.enrollmentYear) {
            registerForm.enrollmentYear = res.data.enrollmentYear
          }
          shareCodeError.value = ''
        } else {
          shareCodeError.value = res.message || '分享码无效或已过期'
          shareCodeInfo.value = null
          registerForm.enrollmentYear = null
        }
      } catch (error) {
        shareCodeError.value = '分享码验证失败：' + (error.message || '未知错误')
        shareCodeInfo.value = null
        registerForm.enrollmentYear = null
      }
    }

    // 提交注册
    const handleRegister = async () => {
      if (!registerFormRef.value) {
        ElMessage.warning('表单未初始化，请刷新页面重试')
        return
      }

      try {
        // 先验证表单
        const valid = await registerFormRef.value.validate()
        if (!valid) {
          ElMessage.warning('请检查表单填写是否正确')
          return
        }

        if (!shareCodeInfo.value) {
          ElMessage.warning('请先验证班级分享码')
          return
        }

        // 确保入学年份已设置
        if (!registerForm.enrollmentYear && shareCodeInfo.value?.enrollmentYear) {
          registerForm.enrollmentYear = shareCodeInfo.value.enrollmentYear
        }

        if (!registerForm.enrollmentYear) {
          ElMessage.warning('入学年份未设置，请先验证班级分享码')
          return
        }

        registering.value = true
        try {
          // 准备提交数据，不包含确认密码字段
          const submitData = {
            ...registerForm
          }
          delete submitData.confirmPassword
          
          console.log('准备提交注册数据:', submitData)
          console.log('分享码:', registerForm.shareCode)
          
          const res = await studentApi.registerStudent(submitData, registerForm.shareCode)
          console.log('注册API响应:', res)
          if (res.code === 200) {
            ElMessage.success('注册成功，请等待班主任审核')
            setTimeout(() => {
              router.push('/login')
            }, 2000)
          } else {
            ElMessage.error(res.message || '注册失败')
          }
        } catch (error) {
          console.error('注册失败:', error)
          ElMessage.error('注册失败：' + (error.response?.data?.message || error.message || '未知错误'))
        } finally {
          registering.value = false
        }
      } catch (error) {
        // 表单验证失败
        console.error('表单验证失败:', error)
        ElMessage.warning('请检查表单填写是否正确')
      }
    }

    const goBack = () => {
      router.push('/register')
    }

    return {
      registerFormRef,
      registerForm,
      registerRules,
      registering,
      shareCodeInfo,
      shareCodeError,
      validateShareCode,
      handleRegister,
      goBack
    }
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background: #f5f7fa;
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 680px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

.register-card :deep(.el-card__header) {
  background: #ffffff;
  padding: 24px 40px 20px;
  border-bottom: 1px solid #e4e7ed;
}

.register-card :deep(.el-card__body) {
  padding: 40px;
  background: #ffffff;
}

.card-header {
  text-align: center;
  position: relative;
}

.header-top {
  position: absolute;
  left: 0;
  top: 0;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #409eff !important;
  transition: all 0.3s;
  padding: 6px 12px;
  border-radius: 6px;
}

.back-link:hover {
  color: #66b1ff !important;
  background: #ecf5ff;
  transform: translateX(-2px);
}

.back-link .el-icon {
  font-size: 16px;
}

.card-header h2 {
  margin: 8px 0 12px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 500;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

:deep(.el-form) {
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
  font-size: 14px;
  padding-bottom: 8px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
  padding: 4px 12px;
  min-height: 26px;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #409eff inset;
}

:deep(.el-input--large) {
  font-size: 14px;
}

:deep(.el-select .el-input__wrapper) {
  padding: 4px 12px;
  min-height: 26px;
}

:deep(.el-date-editor) {
  width: 100%;
}

:deep(.el-date-editor .el-input__wrapper) {
  padding: 4px 12px;
  min-height: 26px;
}

:deep(.el-button--primary) {
  background-color: #409eff;
  border-color: #409eff;
  border-radius: 4px;
  height: 36px;
  font-size: 14px;
  transition: all 0.3s;
}

:deep(.el-button--primary:hover) {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

:deep(.el-button--primary:active) {
  background-color: #3a8ee6;
  border-color: #3a8ee6;
}

:deep(.el-divider) {
  margin: 24px 0;
  border-color: #e4e7ed;
}

.share-code-info,
.share-code-error {
  margin-top: 10px;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}

:deep(.el-alert) {
  border-radius: 8px;
}

@media (max-width: 768px) {
  .register-container {
    padding: 20px 16px;
  }
  
  .register-card :deep(.el-card__body) {
    padding: 24px 20px;
  }
  
  .register-card :deep(.el-card__header) {
    padding: 24px 20px 20px;
  }
  
  .card-header h2 {
    font-size: 24px;
  }
}
</style>


