<template>
  <div class="dashboard">
    <!-- 页面标题 -->
    <div class="dashboard-header">
      <h1 class="dashboard-title">仪表盘</h1>
      <p class="dashboard-subtitle">数据统计与分析</p>
    </div>

    <!-- 筛选器 -->
    <StatisticsFilter
      v-if="showFilter"
      :show-school-filter="hasRole(['ROLE_SYSTEM_ADMIN'])"
      :show-college-filter="hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'])"
      :show-major-filter="hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])"
      :show-class-filter="hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])"
      @filter-change="handleFilterChange"
    />

    <!-- 统计卡片区域 -->
    <div class="statistics-cards">
      <div class="cards-grid">
        <StatisticsCard
          v-for="card in statisticsCards"
          :key="card.key"
          :title="card.title"
          :value="card.value"
          :unit="card.unit"
          :icon="card.icon"
          :color="card.color"
          :description="card.description"
          :clickable="card.clickable"
          @click="card.onClick"
        />
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 图表卡片将在这里动态渲染 -->
        <el-card
          v-for="chart in charts"
          :key="chart.key"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ chart.title }}</span>
              <el-icon class="chart-icon" :size="16">
                <component :is="chart.icon" />
              </el-icon>
            </div>
          </template>
          <div class="chart-content" :id="'chart-' + chart.key" :style="{ height: chart.height + 'px' }">
            <div v-if="loading" class="chart-loading">
              <el-skeleton :rows="5" animated />
            </div>
            <div v-else-if="!chart.data" class="chart-empty">
              <el-empty description="暂无数据" :image-size="80" />
            </div>
            <!-- 图表将在这里渲染（需要集成 ECharts） -->
            <div v-else class="chart-placeholder">
              {{ chart.title }} 图表（待集成 ECharts）
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'
import StatisticsCard from '@/components/common/StatisticsCard.vue'
import StatisticsFilter from '@/components/common/StatisticsFilter.vue'
import { statisticsApi } from '@/api/statistics'
import {
  User,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  Document,
  Clock,
  TrendCharts,
  PieChart
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const userRoles = computed(() => authStore.roles || [])
const loading = ref(false)
const filterParams = ref({})

// 判断是否有指定角色
const hasRole = (roles) => {
  return hasAnyRole(roles)
}

// 判断是否显示筛选器（学生不显示）
const showFilter = computed(() => {
  return !hasRole(['ROLE_STUDENT'])
})

// 统计卡片数据
const statisticsCards = ref([])

// 图表数据
const charts = ref([])

// 加载统计数据
const loadStatistics = async () => {
  loading.value = true
  try {
    // 根据角色加载不同的统计数据
    if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
      await loadSystemAdminStatistics()
    } else if (hasRole(['ROLE_SCHOOL_ADMIN'])) {
      await loadSchoolAdminStatistics()
    } else if (hasRole(['ROLE_COLLEGE_LEADER'])) {
      await loadCollegeLeaderStatistics()
    } else if (hasRole(['ROLE_CLASS_TEACHER'])) {
      await loadClassTeacherStatistics()
    } else if (hasRole(['ROLE_ENTERPRISE_ADMIN'])) {
      await loadEnterpriseAdminStatistics()
    } else if (hasRole(['ROLE_STUDENT'])) {
      await loadStudentStatistics()
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 系统管理员统计数据
const loadSystemAdminStatistics = async () => {
  try {
    // TODO: 调用实际API
    // const progressRes = await statisticsApi.getInternshipProgressStatistics(filterParams.value)
    
    // 模拟数据
    statisticsCards.value = [
      {
        key: 'totalStudents',
        title: '总学生数',
        value: 500,
        unit: '人',
        icon: 'User',
        color: '#409EFF'
      },
      {
        key: 'inProgress',
        title: '进行中',
        value: 320,
        unit: '人',
        icon: 'Clock',
        color: '#409EFF'
      },
      {
        key: 'completed',
        title: '已完成',
        value: 150,
        unit: '人',
        icon: 'Document',
        color: '#67C23A'
      },
      {
        key: 'enterprises',
        title: '合作企业',
        value: 25,
        unit: '家',
        icon: 'OfficeBuilding',
        color: '#409EFF'
      }
    ]

    charts.value = [
      {
        key: 'progress',
        title: '实习进度统计',
        icon: 'PieChart',
        height: 300,
        data: null // 待填充
      },
      {
        key: 'schoolComparison',
        title: '各学校实习人数对比',
        icon: 'TrendCharts',
        height: 300,
        data: null
      },
      {
        key: 'scoreDistribution',
        title: '评价分数分布',
        icon: 'DataAnalysis',
        height: 300,
        data: null
      },
      {
        key: 'postType',
        title: '岗位类型分布',
        icon: 'PieChart',
        height: 300,
        data: null
      }
    ]
  } catch (error) {
    console.error('加载系统管理员统计数据失败:', error)
  }
}

// 学校管理员统计数据
const loadSchoolAdminStatistics = async () => {
  statisticsCards.value = [
    {
      key: 'totalStudents',
      title: '本学校实习学生数',
      value: 120,
      unit: '人',
      icon: 'User',
      color: '#409EFF'
    },
    {
      key: 'inProgress',
      title: '进行中',
      value: 80,
      unit: '人',
      icon: 'Clock',
      color: '#409EFF'
    },
    {
      key: 'completed',
      title: '已完成',
      value: 35,
      unit: '人',
      icon: 'Document',
      color: '#67C23A'
    },
    {
      key: 'averageScore',
      title: '平均评价分数',
      value: 82.5,
      unit: '分',
      icon: 'DataAnalysis',
      color: '#409EFF'
    }
  ]

  charts.value = [
    {
      key: 'progress',
      title: '实习进度统计',
      icon: 'PieChart',
      height: 300,
      data: null
    },
    {
      key: 'collegeComparison',
      title: '各学院实习人数对比',
      icon: 'TrendCharts',
      height: 300,
      data: null
    },
    {
      key: 'scoreDistribution',
      title: '评价分数分布',
      icon: 'DataAnalysis',
      height: 300,
      data: null
    },
    {
      key: 'duration',
      title: '实习时长分布（按月份）',
      icon: 'TrendCharts',
      height: 300,
      data: null
    }
  ]
}

// 学院负责人统计数据
const loadCollegeLeaderStatistics = async () => {
  statisticsCards.value = [
    {
      key: 'totalStudents',
      title: '本院实习学生数',
      value: 60,
      unit: '人',
      icon: 'User',
      color: '#409EFF'
    },
    {
      key: 'inProgress',
      title: '进行中',
      value: 40,
      unit: '人',
      icon: 'Clock',
      color: '#409EFF'
    },
    {
      key: 'completed',
      title: '已完成',
      value: 18,
      unit: '人',
      icon: 'Document',
      color: '#67C23A'
    },
    {
      key: 'averageScore',
      title: '平均评价分数',
      value: 83.2,
      unit: '分',
      icon: 'DataAnalysis',
      color: '#409EFF'
    }
  ]

  charts.value = [
    {
      key: 'progress',
      title: '实习进度统计',
      icon: 'PieChart',
      height: 300,
      data: null
    },
    {
      key: 'majorComparison',
      title: '各专业实习人数对比',
      icon: 'TrendCharts',
      height: 300,
      data: null
    },
    {
      key: 'scoreDistribution',
      title: '评价分数分布',
      icon: 'DataAnalysis',
      height: 300,
      data: null
    },
    {
      key: 'postType',
      title: '岗位类型分布',
      icon: 'PieChart',
      height: 300,
      data: null
    }
  ]
}

// 班主任统计数据
const loadClassTeacherStatistics = async () => {
  statisticsCards.value = [
    {
      key: 'totalStudents',
      title: '本班级实习学生数',
      value: 35,
      unit: '人',
      icon: 'User',
      color: '#409EFF'
    },
    {
      key: 'inProgress',
      title: '进行中',
      value: 20,
      unit: '人',
      icon: 'Clock',
      color: '#409EFF'
    },
    {
      key: 'completed',
      title: '已完成',
      value: 12,
      unit: '人',
      icon: 'Document',
      color: '#67C23A'
    },
    {
      key: 'averageScore',
      title: '平均评价分数',
      value: 85.2,
      unit: '分',
      icon: 'DataAnalysis',
      color: '#409EFF'
    }
  ]

  charts.value = [
    {
      key: 'progress',
      title: '实习进度统计',
      icon: 'PieChart',
      height: 300,
      data: null
    },
    {
      key: 'studentRanking',
      title: '学生评价分数排行榜',
      icon: 'TrendCharts',
      height: 300,
      data: null
    },
    {
      key: 'scoreDistribution',
      title: '评价分数分布',
      icon: 'DataAnalysis',
      height: 300,
      data: null
    },
    {
      key: 'postType',
      title: '岗位类型分布',
      icon: 'PieChart',
      height: 300,
      data: null
    }
  ]
}

// 企业管理员统计数据
const loadEnterpriseAdminStatistics = async () => {
  statisticsCards.value = [
    {
      key: 'totalStudents',
      title: '本企业实习学生数',
      value: 15,
      unit: '人',
      icon: 'User',
      color: '#409EFF'
    },
    {
      key: 'inProgress',
      title: '进行中',
      value: 10,
      unit: '人',
      icon: 'Clock',
      color: '#409EFF'
    },
    {
      key: 'completed',
      title: '已完成',
      value: 5,
      unit: '人',
      icon: 'Document',
      color: '#67C23A'
    },
    {
      key: 'pending',
      title: '待评价',
      value: 3,
      unit: '人',
      icon: 'Document',
      color: '#F7BA2A'
    }
  ]

  charts.value = [
    {
      key: 'progress',
      title: '实习进度统计',
      icon: 'PieChart',
      height: 300,
      data: null
    },
    {
      key: 'scoreDistribution',
      title: '学生评价分数分布',
      icon: 'DataAnalysis',
      height: 300,
      data: null
    },
    {
      key: 'postType',
      title: '岗位类型分布',
      icon: 'PieChart',
      height: 300,
      data: null
    },
    {
      key: 'duration',
      title: '实习时长统计',
      icon: 'TrendCharts',
      height: 300,
      data: null
    }
  ]
}

// 学生统计数据
const loadStudentStatistics = async () => {
  statisticsCards.value = [
    {
      key: 'status',
      title: '实习状态',
      value: '进行中',
      icon: 'Clock',
      color: '#409EFF'
    },
    {
      key: 'score',
      title: '评价分数',
      value: 85.5,
      unit: '分',
      icon: 'DataAnalysis',
      color: '#409EFF'
    },
    {
      key: 'logs',
      title: '已提交日志数',
      value: 15,
      unit: '篇',
      icon: 'Document',
      color: '#409EFF'
    },
    {
      key: 'reports',
      title: '已提交周报数',
      value: 12,
      unit: '篇',
      icon: 'Document',
      color: '#409EFF'
    }
  ]

  charts.value = [
    {
      key: 'personalScore',
      title: '个人评价分数（雷达图）',
      icon: 'DataAnalysis',
      height: 350,
      data: null
    },
    {
      key: 'progressTimeline',
      title: '实习进度时间线',
      icon: 'TrendCharts',
      height: 250,
      data: null
    },
    {
      key: 'submission',
      title: '日志/周报提交情况',
      icon: 'TrendCharts',
      height: 300,
      data: null
    }
  ]
}

// 筛选器变化处理
const handleFilterChange = (params) => {
  filterParams.value = params
  loadStatistics()
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.dashboard-header {
  margin-bottom: 24px;
}

.dashboard-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.dashboard-subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.statistics-cards {
  margin-bottom: 24px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.charts-section {
  margin-top: 24px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

@media (max-width: 1200px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.chart-icon {
  color: #409eff;
}

.chart-content {
  position: relative;
}

.chart-loading,
.chart-empty,
.chart-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 200px;
}

.chart-placeholder {
  color: #909399;
  font-size: 14px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
