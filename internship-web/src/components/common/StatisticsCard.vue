<template>
  <el-card 
    class="statistics-card" 
    :class="{ 'clickable': clickable }"
    shadow="hover"
    @click="handleClick"
  >
    <div class="card-content">
      <div class="card-header">
        <el-icon v-if="icon" class="card-icon" :size="24" :style="{ color: color }">
          <component :is="icon" />
        </el-icon>
        <span class="card-title">{{ title }}</span>
      </div>
      <div class="card-value" :style="{ color: color || '#409EFF' }">
        {{ formattedValue }}
        <span v-if="unit" class="card-unit">{{ unit }}</span>
      </div>
      <div v-if="description" class="card-description">
        {{ description }}
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [Number, String],
    required: true
  },
  unit: {
    type: String,
    default: ''
  },
  icon: {
    type: String,
    default: null
  },
  color: {
    type: String,
    default: '#409EFF'
  },
  description: {
    type: String,
    default: ''
  },
  clickable: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const handleClick = () => {
  if (props.clickable) {
    emit('click')
  }
}
</script>

<style scoped>
.statistics-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
  height: 100%;
}

.statistics-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.15);
  border-color: #409eff;
}

.statistics-card.clickable {
  cursor: pointer;
}

.card-content {
  padding: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.card-icon {
  margin-right: 8px;
}

.card-title {
  font-size: 14px;
  color: #606266;
  font-weight: 400;
}

.card-value {
  font-size: 28px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.2;
}

.card-unit {
  font-size: 14px;
  font-weight: 400;
  margin-left: 4px;
}

.card-description {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>

