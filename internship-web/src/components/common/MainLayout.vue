<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <h1 class="logo">高校实习管理系统</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ userInfo?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDownIcon /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
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
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>

          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/system/school">
              <el-icon><School /></el-icon>
              <template #title>学校管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/system/college">
              <el-icon><OfficeBuilding /></el-icon>
              <template #title>学院管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/system/major">
              <el-icon><Reading /></el-icon>
              <template #title>专业管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/system/class">
              <el-icon><User /></el-icon>
              <template #title>班级管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/system/semester">
              <el-icon><Calendar /></el-icon>
              <template #title>学期管理</template>
            </el-menu-item>
            <el-menu-item index="/admin/system/config">
              <el-icon><Tools /></el-icon>
              <template #title>系统配置</template>
            </el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="user">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/user">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
          </el-sub-menu>
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import {
  House,
  Setting,
  School,
  OfficeBuilding,
  Reading,
  User,
  UserFilled,
  Calendar,
  Tools,
  ArrowDown as ArrowDownIcon
} from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isCollapse = ref(false)
const userInfo = computed(() => authStore.userInfo)

// 当前激活的菜单
const activeMenu = computed(() => {
  const { path } = route
  return path
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
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
}

.user-info .el-icon {
  margin-right: 5px;
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

