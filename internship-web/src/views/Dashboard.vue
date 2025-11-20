<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1 class="dashboard-title">高校实习管理系统</h1>
      <p class="dashboard-subtitle">欢迎使用，请选择功能模块</p>
    </div>

    <div class="dashboard-content">
      <!-- 系统管理模块 -->
      <div class="module-section">
        <h2 class="section-title">
          <el-icon class="section-icon"><Setting /></el-icon>
          系统管理
        </h2>
        <div class="module-grid">
          <el-card
            v-for="item in systemModules"
            :key="item.path"
            class="module-card"
            shadow="hover"
            @click="navigateTo(item.path)"
          >
            <div class="card-content">
              <el-icon class="card-icon" :size="32">
                <component :is="item.icon" />
              </el-icon>
              <h3 class="card-title">{{ item.title }}</h3>
              <p class="card-desc">{{ item.desc }}</p>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 用户管理模块 -->
      <div class="module-section">
        <h2 class="section-title">
          <el-icon class="section-icon"><UserFilled /></el-icon>
          用户管理
        </h2>
        <div class="module-grid">
          <el-card
            v-for="item in userModules"
            :key="item.path"
            class="module-card"
            shadow="hover"
            @click="navigateTo(item.path)"
          >
            <div class="card-content">
              <el-icon class="card-icon" :size="32">
                <component :is="item.icon" />
              </el-icon>
              <h3 class="card-title">{{ item.title }}</h3>
              <p class="card-desc">{{ item.desc }}</p>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Setting, School, OfficeBuilding, Reading, User, UserFilled, Calendar, Tools } from '@element-plus/icons-vue'

const router = useRouter()

const systemModules = [
  {
    title: '学校管理',
    desc: '管理学校基本信息',
    path: '/admin/system/school',
    icon: 'School'
  },
  {
    title: '学院管理',
    desc: '管理学院基本信息',
    path: '/admin/system/college',
    icon: 'OfficeBuilding'
  },
  {
    title: '专业管理',
    desc: '管理专业基本信息',
    path: '/admin/system/major',
    icon: 'Reading'
  },
  {
    title: '班级管理',
    desc: '管理班级信息及分享码',
    path: '/admin/system/class',
    icon: 'User'
  },
  {
    title: '学期管理',
    desc: '管理学期信息',
    path: '/admin/system/semester',
    icon: 'Calendar'
  },
  {
    title: '系统配置',
    desc: '管理系统配置参数',
    path: '/admin/system/config',
    icon: 'Tools'
  }
]

const userModules = [
  {
    title: '用户管理',
    desc: '管理系统用户信息',
    path: '/admin/user',
    icon: 'UserFilled'
  }
]

const navigateTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  margin-bottom: 32px;
  padding: 24px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.dashboard-title {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
}

.module-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 20px 0;
  padding: 0;
}

.section-icon {
  margin-right: 8px;
  color: #409eff;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.module-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  border-color: #409eff;
}

.card-content {
  text-align: center;
  padding: 8px;
}

.card-icon {
  color: #409eff;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0 0 8px 0;
}

.card-desc {
  font-size: 13px;
  color: #909399;
  margin: 0;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>
