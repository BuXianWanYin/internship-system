<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <div class="profile-header">
            <!-- Banner区域 -->
            <div class="banner">
              <div class="banner-content">
                <h3>高校实习管理系统</h3>
              </div>
            </div>
            
            <!-- 头像区域 -->
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :show-file-list="false"
                name="file"
                :on-success="handleAvatarSuccess"
                :on-error="handleAvatarError"
                :before-upload="beforeAvatarUpload"
              >
                <el-avatar
                  v-if="profileForm.avatar"
                  :size="80"
                  :src="avatarUrl"
                  class="profile-avatar"
                />
                <el-avatar
                  v-else
                  :size="80"
                  :icon="User"
                  class="profile-avatar"
                />
                <div class="avatar-edit-overlay">
                  <el-icon><Camera /></el-icon>
                </div>
              </el-upload>
            </div>
            
            <!-- 用户信息区域 -->
            <div class="user-info-section">
              <h3 class="user-name">{{ profileForm.realName || profileForm.username || '用户' }}</h3>
              <p class="user-desc">{{ getUserRoleDesc() }}</p>
              
              <div class="user-details">
                <div class="detail-item">
                  <el-icon><Message /></el-icon>
                  <span>{{ profileForm.email || '暂无邮箱' }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><User /></el-icon>
                  <span>{{ profileForm.realName || '暂无姓名' }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ profileForm.address || '暂无地址' }}</span>
                </div>
                <div class="detail-item">
                  <el-icon><Phone /></el-icon>
                  <span>{{ profileForm.phone || '暂无手机号' }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：设置内容 -->
      <el-col :span="16">
        <!-- 基本设置 -->
        <el-card class="setting-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">基本设置</span>
              <el-button
                v-if="!isEditingProfile"
                type="primary"
                :icon="Edit"
                @click="startEditProfile"
              >
                编辑
              </el-button>
              <div v-else>
                <el-button @click="cancelEditProfile">取消</el-button>
                <el-button type="primary" :loading="savingProfile" @click="saveProfile">
                  保存
                </el-button>
              </div>
            </div>
          </template>

          <el-form
            ref="profileFormRef"
            :model="profileForm"
            :rules="profileRules"
            label-width="100px"
            :disabled="!isEditingProfile"
            class="profile-form"
          >
            <el-form-item label="姓名" prop="realName">
              <el-input 
                v-model="profileForm.realName" 
                placeholder="请输入姓名"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="性别" prop="gender">
              <el-select 
                v-model="profileForm.gender" 
                placeholder="请选择性别"
                class="form-input"
              >
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input 
                v-model="profileForm.nickname" 
                placeholder="请输入昵称"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input 
                v-model="profileForm.email" 
                placeholder="请输入邮箱"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="手机" prop="phone">
              <el-input 
                v-model="profileForm.phone" 
                placeholder="请输入手机号"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="地址" prop="address">
              <el-input 
                v-model="profileForm.address" 
                type="textarea"
                :rows="2"
                placeholder="请输入地址"
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="个人介绍" prop="introduction">
              <el-input 
                v-model="profileForm.introduction" 
                type="textarea"
                :rows="4"
                placeholder="请输入个人介绍"
                class="form-input"
              />
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 更改密码 -->
        <el-card class="setting-card" shadow="hover" style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span class="card-title">更改密码</span>
              <el-button
                v-if="!isEditingPassword"
                type="primary"
                :icon="Edit"
                @click="startEditPassword"
              >
                编辑
              </el-button>
              <div v-else>
                <el-button @click="cancelEditPassword">取消</el-button>
                <el-button type="primary" :loading="savingPassword" @click="savePassword">
                  保存
                </el-button>
              </div>
            </div>
          </template>

          <el-form
            ref="passwordFormRef"
            :model="passwordForm"
            :rules="passwordRules"
            label-width="100px"
            :disabled="!isEditingPassword"
            class="profile-form"
          >
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入当前密码"
                show-password
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20个字符）"
                show-password
                class="form-input"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
                class="form-input"
              />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Edit, Camera, Message, Phone, Location } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/modules/auth'
import { userApi } from '@/api/user/user'

const router = useRouter()

const authStore = useAuthStore()

// 上传相关
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || ''}/file/upload/single`
})
const uploadHeaders = computed(() => {
  return {
    Authorization: `Bearer ${authStore.token}`
  }
})

// 用户信息
const profileForm = reactive({
  userId: null,
  username: '',
  realName: '',
  email: '',
  phone: '',
  avatar: '',
  gender: '',
  nickname: '',
  address: '',
  introduction: ''
})

const originalProfile = ref({})

const isEditingProfile = ref(false)
const savingProfile = ref(false)
const profileFormRef = ref(null)

const profileRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 密码相关
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const isEditingPassword = ref(false)
const savingPassword = ref(false)
const passwordFormRef = ref(null)

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度必须在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 头像URL
const avatarUrl = computed(() => {
  if (profileForm.avatar) {
    if (profileForm.avatar.startsWith('http://') || profileForm.avatar.startsWith('https://')) {
      return profileForm.avatar
    }
    return `${import.meta.env.VITE_API_BASE_URL || ''}${profileForm.avatar}`
  }
  return ''
})

// 获取用户角色描述
const getUserRoleDesc = () => {
  const roles = authStore.userInfo?.roles || []
  if (roles.length > 0) {
    const roleNames = {
      'ROLE_SYSTEM_ADMIN': '系统管理员',
      'ROLE_SCHOOL_ADMIN': '学校管理员',
      'ROLE_COLLEGE_LEADER': '学院负责人',
      'ROLE_CLASS_TEACHER': '班主任',
      'ROLE_STUDENT': '学生',
      'ROLE_ENTERPRISE_ADMIN': '企业管理员',
      'ROLE_ENTERPRISE_MENTOR': '企业导师'
    }
    return roleNames[roles[0]] || '用户'
  }
  return '用户'
}

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await userApi.getCurrentUser()
    if (res.code === 200) {
      Object.assign(profileForm, {
        userId: res.data.userId,
        username: res.data.username,
        realName: res.data.realName || '',
        email: res.data.email || '',
        phone: res.data.phone || '',
        avatar: res.data.avatar || '',
        gender: res.data.gender || '',
        nickname: res.data.nickname || '',
        address: res.data.address || '',
        introduction: res.data.introduction || ''
      })
      originalProfile.value = { ...profileForm }
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败：' + (error.message || '未知错误'))
  }
}

// 开始编辑个人信息
const startEditProfile = () => {
  isEditingProfile.value = true
  originalProfile.value = { ...profileForm }
}

// 取消编辑个人信息
const cancelEditProfile = () => {
  isEditingProfile.value = false
  Object.assign(profileForm, originalProfile.value)
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileFormRef.value) return
  
  try {
    await profileFormRef.value.validate()
    savingProfile.value = true
    
    const res = await userApi.updateCurrentUserProfile({
      realName: profileForm.realName,
      email: profileForm.email,
      phone: profileForm.phone,
      avatar: profileForm.avatar,
      gender: profileForm.gender,
      nickname: profileForm.nickname,
      address: profileForm.address,
      introduction: profileForm.introduction
    })
    
    if (res.code === 200) {
      ElMessage.success('保存成功')
      isEditingProfile.value = false
      // 更新 store 中的用户信息
      authStore.updateUserInfo({
        ...authStore.userInfo,
        realName: profileForm.realName,
        email: profileForm.email,
        phone: profileForm.phone,
        avatar: profileForm.avatar
      })
      originalProfile.value = { ...profileForm }
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    if (error.message && !error.message.includes('validation')) {
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    }
  } finally {
    savingProfile.value = false
  }
}

// 头像上传成功
const handleAvatarSuccess = async (response) => {
  try {
    // 如果 response 是字符串，尝试解析为 JSON
    let result = response
    if (typeof response === 'string') {
      try {
        result = JSON.parse(response)
      } catch (e) {
        // 如果不是 JSON，可能是直接的 URL 字符串
        result = { code: 200, data: response }
      }
    }
    
    if (result.code === 200 && result.data) {
      const avatarUrl = result.data.url || result.data
      profileForm.avatar = avatarUrl
      ElMessage.success('头像上传成功')
      
      // 自动保存头像
      try {
        const res = await userApi.updateCurrentUserProfile({
          avatar: avatarUrl
        })
        if (res.code === 200) {
          // 更新 store 中的用户信息
          authStore.updateUserInfo({
            ...authStore.userInfo,
            avatar: avatarUrl
          })
          originalProfile.value.avatar = avatarUrl
        }
      } catch (error) {
        ElMessage.warning('头像已上传，但保存失败，请稍后重试')
      }
    } else {
      ElMessage.error(result.message || '头像上传失败')
    }
  } catch (error) {
    ElMessage.error('头像上传失败：' + (error.message || '未知错误'))
  }
}

// 头像上传失败
const handleAvatarError = (error, file, fileList) => {
  console.error('头像上传错误：', error)
  let errorMessage = '头像上传失败'
  
  if (error && error.response) {
    // 处理 HTTP 错误响应
    const response = error.response
    if (response.data) {
      if (typeof response.data === 'string') {
        try {
          const errorData = JSON.parse(response.data)
          errorMessage = errorData.message || errorMessage
        } catch (e) {
          errorMessage = response.data || errorMessage
        }
      } else if (response.data.message) {
        errorMessage = response.data.message
      }
    } else if (response.status === 500) {
      errorMessage = '服务器错误，请稍后重试'
    } else if (response.status === 413) {
      errorMessage = '文件过大，请选择较小的图片'
    } else if (response.status === 415) {
      errorMessage = '不支持的文件类型'
    }
  } else if (error && error.message) {
    errorMessage = error.message
  }
  
  ElMessage.error(errorMessage)
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 开始编辑密码
const startEditPassword = () => {
  isEditingPassword.value = true
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 取消编辑密码
const cancelEditPassword = () => {
  isEditingPassword.value = false
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  if (passwordFormRef.value) {
    passwordFormRef.value.clearValidate()
  }
}

// 保存密码
const savePassword = async () => {
  if (!passwordFormRef.value) return
  
  try {
    await passwordFormRef.value.validate()
    savingPassword.value = true
    
    const res = await userApi.changePassword(
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    
    if (res.code === 200) {
      isEditingPassword.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      
      // 立即清除 token（无论用户是否点击确定，都要清除，防止刷新页面后还能使用旧token）
      // 使用 skipApiCall = true 跳过后端登出接口调用（因为密码已修改，旧 token 已失效）
      await authStore.logout(true)
      
      // 弹出确认对话框
      try {
        await ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
          confirmButtonText: '确定',
          type: 'success'
        })
      } catch {
        // token 已经清除，不需要额外处理
      } finally {
        // 无论用户是否点击确定，都跳转到登录页
        router.push('/login')
      }
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    if (error.message && !error.message.includes('validation')) {
      ElMessage.error('密码修改失败：' + (error.message || '未知错误'))
    }
  } finally {
    savingPassword.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.profile-card {
  margin-bottom: 0;
}

.profile-header {
  text-align: center;
}

.banner {
  height: 120px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border-radius: 8px 8px 0 0;
  margin: -20px -20px 0 -20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.banner-content h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.avatar-section {
  margin-top: -40px;
  margin-bottom: 20px;
  position: relative;
  display: inline-block;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.profile-avatar {
  border: 4px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.avatar-edit-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 28px;
  height: 28px;
  background: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  border: 2px solid #fff;
  transition: all 0.3s;
}

.avatar-edit-overlay:hover {
  background: #66b1ff;
  transform: scale(1.1);
}

.user-info-section {
  margin-top: 20px;
}

.user-name {
  font-size: 20px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.user-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 20px 0;
}

.user-details {
  text-align: left;
  padding: 0 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.detail-item .el-icon {
  margin-right: 8px;
  color: #909399;
  font-size: 16px;
}

.setting-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.profile-form {
  max-width: 800px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

.form-input {
  width: 100%;
  max-width: 600px;
}

:deep(.el-input__wrapper) {
  border-radius: 4px;
}

:deep(.el-textarea__inner) {
  border-radius: 4px;
}

:deep(.el-select) {
  width: 100%;
  max-width: 600px;
}
</style>
