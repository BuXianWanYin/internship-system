<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <!-- 左侧：用户信息卡片 -->
      <el-col :span="6">
        <el-card class="profile-card" shadow="hover">
          <div class="profile-header">
            <div class="banner">
              <div class="banner-content">
                <h3>高校实习管理系统</h3>
              </div>
            </div>
            <div class="avatar-section">
              <el-upload
                class="avatar-uploader"
                :action="uploadAction"
                :headers="uploadHeaders"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :disabled="!isEditingProfile"
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
                <div v-if="isEditingProfile" class="avatar-edit-overlay">
                  <el-icon><Camera /></el-icon>
                </div>
              </el-upload>
            </div>
            <div class="user-info-section">
              <h3 class="user-name">{{ profileForm.realName || profileForm.username || '用户' }}</h3>
              <p class="user-desc">{{ profileForm.email || '暂无邮箱' }}</p>
              <div class="user-details">
                <div class="detail-item">
                  <el-icon><Message /></el-icon>
                  <span>{{ profileForm.email || '暂无邮箱' }}</span>
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
      <el-col :span="18">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>个人设置</span>
            </div>
          </template>

          <!-- 基本设置 -->
          <div class="setting-section">
            <div class="section-header">
              <h3>基本设置</h3>
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

            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              :disabled="!isEditingProfile"
            >
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item label="手机" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-form>
          </div>

          <!-- 更改密码 -->
          <el-divider />
          <div class="setting-section">
            <div class="section-header">
              <h3>更改密码</h3>
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

            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              :disabled="!isEditingPassword"
            >
              <el-form-item label="当前密码" prop="oldPassword">
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（6-20个字符）"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>
            </el-form>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Edit, Camera, Message, Phone } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/modules/auth'
import { userApi } from '@/api/user/user'
import request from '@/utils/request'

const authStore = useAuthStore()

// 上传相关
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || ''}/file/upload`
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
  avatar: ''
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
        avatar: res.data.avatar || ''
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
      avatar: profileForm.avatar
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
const handleAvatarSuccess = (response) => {
  if (response.code === 200 && response.data) {
    profileForm.avatar = response.data.url || response.data
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
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
      ElMessage.success('密码修改成功，请重新登录')
      isEditingPassword.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      // 延迟跳转到登录页
      setTimeout(() => {
        authStore.logout()
        window.location.href = '/login'
      }, 1500)
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
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
}

.user-info-section {
  margin-top: 20px;
}

.user-name {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.user-desc {
  font-size: 14px;
  color: #909399;
  margin: 0 0 16px 0;
}

.user-details {
  text-align: left;
  padding: 0 20px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.detail-item .el-icon {
  margin-right: 8px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.setting-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input),
:deep(.el-textarea) {
  max-width: 500px;
}
</style>

