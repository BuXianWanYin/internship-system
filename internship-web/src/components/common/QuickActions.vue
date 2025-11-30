<template>
  <el-card class="quick-actions-card" shadow="hover">
    <template #header>
      <div class="quick-actions-header">
        <span class="quick-actions-title">快捷操作</span>
        <el-icon v-if="icon" class="quick-actions-icon" :size="16">
          <component :is="getHeaderIcon()" />
        </el-icon>
      </div>
    </template>
    <div class="quick-actions-grid">
      <div
        v-for="action in actions"
        :key="action.key"
        class="quick-action-item"
        @click="handleAction(action)"
      >
        <div class="quick-action-icon" :style="{ backgroundColor: action.color + '15' }">
          <el-icon :size="24" :style="{ color: action.color }">
            <component :is="getIcon(action.icon)" />
          </el-icon>
        </div>
        <div class="quick-action-content">
          <div class="quick-action-title">{{ action.title }}</div>
          <div class="quick-action-desc" v-if="action.desc">{{ action.desc }}</div>
        </div>
        <el-icon class="quick-action-arrow" :size="16">
          <ArrowRight />
        </el-icon>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { useRouter } from 'vue-router'
import {
  ArrowRight,
  School,
  User,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  Document,
  Clock,
  EditPen,
  DocumentChecked,
  List,
  Setting,
  Briefcase,
  Files,
  Reading,
  Calendar,
  Star,
  Operation
} from '@element-plus/icons-vue'

const props = defineProps({
  actions: {
    type: Array,
    required: true,
    default: () => []
  },
  icon: {
    type: [String, Object],
    default: Operation
  }
})

// 图标映射
const iconMap = {
  School,
  User,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  Document,
  Clock,
  EditPen,
  DocumentChecked,
  List,
  Setting,
  Briefcase,
  Files,
  Reading,
  Calendar,
  Star,
  Operation
}

const router = useRouter()

const getIcon = (iconName) => {
  if (typeof iconName === 'string') {
    return iconMap[iconName] || Operation
  }
  return iconName || Operation
}

const getHeaderIcon = () => {
  if (!props.icon) return Operation
  if (typeof props.icon === 'string') {
    return iconMap[props.icon] || Operation
  }
  return props.icon || Operation
}

const handleAction = (action) => {
  if (action.path) {
    router.push(action.path)
  } else if (action.action) {
    action.action()
  }
}
</script>

<style scoped>
.quick-actions-card {
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
}

.quick-actions-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.quick-actions-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  letter-spacing: 0.3px;
}

.quick-actions-icon {
  color: #606266;
  opacity: 0.7;
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  flex: 1;
}

.quick-action-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #ffffff;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
  min-height: 80px;
}

.quick-action-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(64, 158, 255, 0.1), transparent);
  transition: left 0.5s;
}

.quick-action-item:hover::before {
  left: 100%;
}

.quick-action-item:hover {
  background: #f8f9fa;
  border-color: rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.quick-action-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  margin-right: 16px;
  flex-shrink: 0;
  transition: transform 0.3s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.quick-action-item:hover .quick-action-icon {
  transform: scale(1.08) rotate(3deg);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.quick-action-content {
  flex: 1;
  min-width: 0;
}

.quick-action-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  letter-spacing: 0.2px;
}

.quick-action-desc {
  font-size: 12px;
  color: #606266;
  opacity: 0.7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quick-action-arrow {
  color: #909399;
  opacity: 0.5;
  margin-left: 8px;
  flex-shrink: 0;
  transition: all 0.3s;
}

.quick-action-item:hover .quick-action-arrow {
  color: #606266;
  opacity: 0.8;
  transform: translateX(3px);
}


:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: transparent;
}

:deep(.el-card__body) {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}
</style>

