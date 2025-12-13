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
          <h2>教师注册</h2>
          <p>请填写个人信息完成注册，注册后需等待管理员审核</p>
        </div>
      </template>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="120px"
      >
        <el-form-item label="工号" prop="teacherNo">
          <el-input
            v-model="registerForm.teacherNo"
            placeholder="请输入工号"
            clearable
          />
        </el-form-item>

        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名，用于登录"
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

        <el-form-item label="学校/学院" prop="cascaderValue">
          <el-cascader
            v-model="registerForm.cascaderValue"
            :options="cascaderOptions"
            :props="cascaderProps"
            placeholder="请先选择学校，再选择学院"
            style="width: 100%;"
            filterable
            @change="handleCascaderChange"
            clearable
          />
        </el-form-item>

        <el-form-item label="职称" prop="title">
          <el-select
            v-model="registerForm.title"
            placeholder="请选择或输入职称"
            style="width: 100%;"
            filterable
            allow-create
            default-first-option
            clearable
          >
            <el-option
              v-for="title in titleOptions"
              :key="title"
              :label="title"
              :value="title"
            />
          </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { teacherApi } from '@/api/user/teacher'
import { collegeApi } from '@/api/system/college'
import { schoolApi } from '@/api/system/school'

export default {
  name: 'TeacherRegister',
  components: {
    ArrowLeft
  },
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const registering = ref(false)
    const schoolList = ref([])
    const collegeList = ref([])
    const cascaderOptions = ref([])

    // 职称选项
    const titleOptions = [
      '教授',
      '副教授',
      '讲师',
      '助教',
      '研究员',
      '副研究员',
      '助理研究员',
      '高级工程师',
      '工程师',
      '助理工程师',
      '其他'
    ]

    const registerForm = reactive({
      teacherNo: '',
      username: '',
      realName: '',
      gender: '',
      idCard: '',
      phone: '',
      email: '',
      schoolId: null,
      collegeId: null,
      cascaderValue: [],
      title: '',
      password: '',
      confirmPassword: ''
    })

    // 级联选择器配置
    const cascaderProps = {
      value: 'id',
      label: 'name',
      children: 'children',
      emitPath: true
    }

    // 验证规则
    const registerRules = {
      teacherNo: [
        { required: true, message: '请输入工号', trigger: 'blur' }
      ],
      username: [
        { required: true, message: '请输入用户名，用于登录', trigger: 'blur' },
        { min: 3, max: 50, message: '用户名长度在3到50个字符', trigger: 'blur' }
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
      cascaderValue: [
        { 
          required: true, 
          validator: (rule, value, callback) => {
            if (!value || value.length !== 2) {
              callback(new Error('请选择学校和学院'))
            } else {
              callback()
            }
          },
          trigger: 'change' 
        }
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
      ]
    }

    // 加载学校和学院数据，构建级联选择器选项
    const loadCascaderData = async () => {
      try {
        // 加载学校列表（使用公开接口，不需要认证）
        const schoolRes = await schoolApi.getPublicSchoolList()
        if (schoolRes.code === 200) {
          schoolList.value = schoolRes.data || []
        }

        // 加载所有学院列表（使用公开接口，不需要认证）
        const collegeRes = await collegeApi.getPublicCollegeList()
        if (collegeRes.code === 200) {
          collegeList.value = collegeRes.data || []
        }

        // 构建级联选择器数据结构
        if (schoolList.value.length > 0 && collegeList.value.length > 0) {
          cascaderOptions.value = schoolList.value.map(school => {
            const colleges = collegeList.value
              .filter(college => college.schoolId === school.schoolId)
              .map(college => ({
                id: college.collegeId,
                name: college.collegeName
              }))

            return {
              id: school.schoolId,
              name: school.schoolName,
              children: colleges.length > 0 ? colleges : undefined
            }
          }).filter(school => school.children && school.children.length > 0)
        } else {
          cascaderOptions.value = []
          console.warn('学校或学院数据为空')
        }
      } catch (error) {
        console.error('加载数据失败:', error)
        ElMessage.error('加载学校和学院数据失败')
      }
    }

    // 级联选择器变化时的处理
    const handleCascaderChange = (value) => {
      if (value && value.length === 2) {
        registerForm.schoolId = value[0]
        registerForm.collegeId = value[1]
      } else {
        registerForm.schoolId = null
        registerForm.collegeId = null
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

        registering.value = true
        try {
          // 准备提交数据，不包含确认密码字段和级联选择器值
          const submitData = {
            teacherNo: registerForm.teacherNo,
            username: registerForm.username,
            realName: registerForm.realName,
            gender: registerForm.gender || null,
            idCard: registerForm.idCard || null,
            phone: registerForm.phone || null,
            email: registerForm.email || null,
            schoolId: registerForm.schoolId,
            collegeId: registerForm.collegeId,
            title: registerForm.title || null,
            password: registerForm.password
          }

          console.log('准备提交教师注册数据:', submitData)

          const res = await teacherApi.registerTeacher(submitData)
          console.log('教师注册API响应:', res)
          if (res.code === 200) {
            ElMessage.success('注册成功，请等待管理员审核')
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

    onMounted(() => {
      loadCascaderData()
    })

    return {
      registerFormRef,
      registerForm,
      registerRules,
      registering,
      schoolList,
      collegeList,
      cascaderOptions,
      cascaderProps,
      titleOptions,
      handleCascaderChange,
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

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
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

