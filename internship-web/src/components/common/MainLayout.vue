<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <h1 class="logo">高校实习管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand" trigger="click" placement="bottom-end">
          <span class="user-info">
            <el-avatar
              :size="32"
              :src="userAvatar"
              :icon="User"
              class="user-avatar"
            />
            <div class="user-text">
              <div class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</div>
              <div class="user-username">{{ userInfo?.username || '' }}</div>
            </div>
            <el-icon class="el-icon--right"><ArrowDownIcon /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">
                <el-icon><User /></el-icon>
                个人中心
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <!-- 主体区域 -->
    <el-container class="main-container">
      <!-- 左侧菜单 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="sidebar-menu"
        >
          <!-- 动态渲染菜单 -->
          <template v-for="menuItem in filteredMenus" :key="menuItem.index">
            <!-- 有子菜单的情况 -->
            <el-sub-menu v-if="menuItem.children && menuItem.children.length > 0" :index="menuItem.index">
              <template #title>
                <el-icon><component :is="menuItem.icon" /></el-icon>
                <span>{{ menuItem.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menuItem.children"
                :key="child.index"
                :index="child.index"
              >
                <el-icon><component :is="child.icon" /></el-icon>
                <template #title>{{ child.title }}</template>
              </el-menu-item>
            </el-sub-menu>
            
            <!-- 无子菜单的情况 -->
            <el-menu-item v-else :index="menuItem.index">
              <el-icon><component :is="menuItem.icon" /></el-icon>
              <template #title>{{ menuItem.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import { filterMenuByRoles } from '@/config/menu.config'
import { ArrowDown as ArrowDownIcon, User } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapse = ref(false)
const userInfo = computed(() => authStore.userInfo)

// 用户头像
const userAvatar = computed(() => {
  if (userInfo.value?.avatar) {
    // 如果是完整URL，直接返回；否则拼接基础路径
    if (userInfo.value.avatar.startsWith('http://') || userInfo.value.avatar.startsWith('https://')) {
      return userInfo.value.avatar
    }
    return `${import.meta.env.VITE_API_BASE_URL || ''}${userInfo.value.avatar}`
  }
  return '' // 返回空字符串，使用默认头像
})

// 根据用户角色过滤菜单
const filteredMenus = computed(() => {
  const roles = authStore.roles || []
  return filterMenuByRoles(roles)
})

// 当前激活的菜单
const activeMenu = computed(() => {
  const { path } = route
  return path
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await authStore.logout()
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  height: 60px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0;
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  margin-right: 8px;
  flex-shrink: 0;
}

.user-text {
  display: flex;
  flex-direction: column;
  margin-right: 8px;
  line-height: 1.4;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.user-username {
  font-size: 12px;
  color: #909399;
}

.user-info .el-icon--right {
  margin-left: 0;
  font-size: 12px;
  color: #909399;
}

.main-container {
  flex: 1;
  overflow: hidden;
}

.aside {
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.main-content {
  background: #f5f7fa;
  padding: 0;
  overflow-y: auto;
}

/* 菜单样式优化 */
:deep(.el-menu-item) {
  color: #606266;
}

:deep(.el-menu-item:hover) {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
}

:deep(.el-sub-menu__title) {
  color: #606266;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #ecf5ff;
  color: #409eff;
}
</style>

