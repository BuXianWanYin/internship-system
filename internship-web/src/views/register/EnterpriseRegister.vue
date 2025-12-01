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
          <h2>企业注册</h2>
          <p>请填写企业信息和管理员账号信息完成注册</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="140px"
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

        <el-form-item label="法人代表" prop="legalPerson">
          <el-input
            v-model="registerForm.legalPerson"
            placeholder="请输入法人代表姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="企业代码" prop="enterpriseCode">
          <el-input
            v-model="registerForm.enterpriseCode"
            placeholder="请输入企业代码"
            clearable
            maxlength="50"
          />
          <div class="form-tip">企业唯一标识码，用于系统内部识别，如：ENT001、COMPANY001</div>
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
            <el-option label="小型企业（20-100人）" value="小型企业" />
            <el-option label="微型企业（20人以下）" value="微型企业" />
          </el-select>
        </el-form-item>

        <el-form-item label="行业类别" prop="industryCategory">
          <el-select
            v-model="registerForm.industryCategory"
            placeholder="请选择或输入行业类别"
            filterable
            allow-create
            default-first-option
            clearable
            style="width: 100%;"
          >
            <el-option
              v-for="category in industryCategories"
              :key="category"
              :label="category"
              :value="category"
            />
            <el-option label="其他" value="其他" />
          </el-select>
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

        <el-divider>
          <span style="color: #909399; font-size: 14px;">管理员账号信息</span>
        </el-divider>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入登录用户名"
            clearable
          />
          <div class="form-tip">用于登录系统，建议使用手机号或邮箱</div>
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
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
import { ArrowLeft } from '@element-plus/icons-vue'
import { enterpriseApi } from '@/api/user/enterprise'

export default {
  name: 'EnterpriseRegister',
  components: {
    ArrowLeft
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const registering = ref(false)

    // 行业类别列表
    const industryCategories = [
      '互联网/电子商务',
      '计算机软件',
      '计算机硬件',
      'IT服务/系统集成',
      '电子技术/半导体/集成电路',
      '通信/电信/网络设备',
      '金融/投资/证券',
      '银行',
      '保险',
      '房地产/建筑/建材',
      '制造业',
      '汽车/摩托车',
      '机械/设备/重工',
      '化工/能源',
      '医疗/护理/卫生',
      '制药/生物工程',
      '教育/培训/院校',
      '广告/公关/媒体',
      '文化/娱乐/体育',
      '贸易/进出口',
      '物流/仓储',
      '餐饮/酒店/旅游',
      '零售/批发',
      '快速消费品',
      '服装/纺织/皮革',
      '家具/家电/玩具',
      '农业/林业/渔业',
      '环保',
      '政府/公共事业',
      '非营利组织',
      '其他'
    ]

    const registerForm = reactive({
      enterpriseName: '',
      unifiedSocialCreditCode: '',
      legalPerson: '',
      enterpriseCode: '',
      enterpriseScale: '',
      industryCategory: '',
      address: '',
      contactPerson: '',
      contactPhone: '',
      email: '',
      description: '',
      username: '',
      password: '',
      confirmPassword: ''
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
      legalPerson: [
        { required: true, message: '请输入法人代表', trigger: 'blur' }
      ],
      enterpriseCode: [
        { required: true, message: '请输入企业代码', trigger: 'blur' },
        { min: 2, max: 50, message: '企业代码长度在 2 到 50 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_-]+$/, message: '企业代码只能包含字母、数字、下划线和横线', trigger: 'blur' }
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
      ],
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_@.-]+$/, message: '用户名只能包含字母、数字、下划线、@、点、横线', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
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

        registering.value = true
        try {
          // 准备提交数据
          const submitData = {
            enterprise: {
              enterpriseName: registerForm.enterpriseName,
              unifiedSocialCreditCode: registerForm.unifiedSocialCreditCode,
              legalPerson: registerForm.legalPerson,
              enterpriseCode: registerForm.enterpriseCode,
              enterpriseScale: registerForm.enterpriseScale,
              industry: registerForm.industryCategory,
              address: registerForm.address,
              contactPerson: registerForm.contactPerson,
              contactPhone: registerForm.contactPhone,
              contactEmail: registerForm.email,
              businessScope: registerForm.description
            },
            username: registerForm.username,
            password: registerForm.password
          }
          
          console.log('准备提交企业注册数据:', submitData)
          
          const res = await enterpriseApi.registerEnterprise(submitData)
          console.log('企业注册API响应:', res)
          if (res.code === 200) {
            ElMessage.success('注册成功，请登录')
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
      industryCategories,
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
  max-width: 800px;
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

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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

:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  transition: all 0.3s;
  padding: 8px 12px;
}

:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 2px #409eff inset;
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

