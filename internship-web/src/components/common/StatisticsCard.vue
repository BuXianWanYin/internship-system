<template>
  <div 
    class="statistics-card" 
    :class="{ 'clickable': clickable }"
    :style="{ '--card-color': color || '#409EFF' }"
    @click="handleClick"
  >
    <div class="card-background"></div>
    <div class="card-content">
      <div class="card-header">
        <div class="card-icon-wrapper">
          <el-icon v-if="icon" class="card-icon" :size="28">
            <component :is="icon" />
          </el-icon>
        </div>
        <span class="card-title">{{ title }}</span>
      </div>
      <div class="card-value">
        {{ formattedValue }}
        <span v-if="unit" class="card-unit">{{ unit }}</span>
      </div>
      <div v-if="description" class="card-description">
        {{ description }}
      </div>
    </div>
  </div>
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
  position: relative;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 100%;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.statistics-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, var(--card-color), rgba(64, 158, 255, 0.4));
  opacity: 0;
  transition: opacity 0.3s;
}

.statistics-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: rgba(0, 0, 0, 0.1);
}

.statistics-card:hover::before {
  opacity: 1;
}

.statistics-card.clickable {
  cursor: pointer;
}

.card-background {
  position: absolute;
  top: -50%;
  right: -20%;
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, var(--card-color)12, transparent);
  border-radius: 50%;
  opacity: 0.2;
  transition: transform 0.3s;
}

.statistics-card:hover .card-background {
  transform: scale(1.15);
}

.card-content {
  position: relative;
  padding: 24px;
  z-index: 1;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.card-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--card-color)15, var(--card-color)08);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s;
}

.statistics-card:hover .card-icon-wrapper {
  transform: scale(1.08) rotate(3deg);
}

.card-icon {
  color: var(--card-color);
}

.card-title {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
  flex: 1;
  text-align: right;
  opacity: 0.8;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  margin-bottom: 8px;
  line-height: 1.2;
  color: #303133;
  display: flex;
  align-items: baseline;
  gap: 4px;
  letter-spacing: -0.5px;
}

.statistics-card:hover .card-value {
  color: var(--card-color);
  transition: color 0.3s;
}

.card-unit {
  font-size: 16px;
  font-weight: 500;
  color: #909399;
  opacity: 0.7;
}

.card-description {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
  opacity: 0.7;
}
</style>

