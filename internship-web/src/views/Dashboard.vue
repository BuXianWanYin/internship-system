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
        />
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 饼图：实习进度统计 -->
        <el-card
          v-if="hasChart('progress')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">实习进度统计</span>
              <el-icon class="chart-icon" :size="16">
                <PieChartIcon />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <PieChart
              v-if="chartData.progress && chartData.progress.data"
              :data="chartData.progress.data"
              :title="''"
              :height="300"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 饼图：岗位类型分布 -->
        <el-card
          v-if="hasChart('postType')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">岗位类型分布</span>
              <el-icon class="chart-icon" :size="16">
                <PieChartIcon />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <PieChart
              v-if="chartData.postType && chartData.postType.data"
              :data="chartData.postType.data"
              :title="''"
              :height="300"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 柱状图：评价分数分布 -->
        <el-card
          v-if="hasChart('scoreDistribution')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">评价分数分布</span>
              <el-icon class="chart-icon" :size="16">
                <DataAnalysis />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <BarChart
              v-if="chartData.scoreDistribution && chartData.scoreDistribution.data"
              :data="chartData.scoreDistribution.data"
              :title="''"
              :height="300"
              x-axis-name="等级"
              y-axis-name="人数"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 柱状图：学生分数排行 -->
        <el-card
          v-if="hasChart('studentRanking')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">学生评价分数排行榜</span>
              <el-icon class="chart-icon" :size="16">
                <TrendCharts />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <BarChart
              v-if="chartData.studentRanking && chartData.studentRanking.data"
              :data="chartData.studentRanking.data"
              :title="''"
              :height="300"
              x-axis-name="学生"
              y-axis-name="分数"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 柱状图：各学校/学院/专业实习人数对比 -->
        <el-card
          v-if="hasChart('comparison')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ chartData.comparison?.title || '人数对比' }}</span>
              <el-icon class="chart-icon" :size="16">
                <TrendCharts />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <BarChart
              v-if="chartData.comparison && chartData.comparison.data"
              :data="chartData.comparison.data"
              :title="''"
              :height="300"
              x-axis-name=""
              y-axis-name="人数"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 折线图：实习时长分布 -->
        <el-card
          v-if="hasChart('duration')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">实习时长分布（按月份）</span>
              <el-icon class="chart-icon" :size="16">
                <TrendCharts />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <LineChart
              v-if="chartData.duration && chartData.duration.data"
              :data="chartData.duration.data"
              :title="''"
              :height="300"
              x-axis-name="月份"
              y-axis-name="平均天数"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 雷达图：个人评价分数 -->
        <el-card
          v-if="hasChart('personalScore')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">个人评价分数（雷达图）</span>
              <el-icon class="chart-icon" :size="16">
                <DataAnalysis />
              </el-icon>
            </div>
          </template>
          <div class="chart-content">
            <RadarChart
              v-if="chartData.personalScore && chartData.personalScore.data"
              :data="chartData.personalScore.data"
              :indicators="chartData.personalScore.indicators"
              :title="''"
              :height="350"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>

        <!-- 待批阅提醒（班主任和企业导师） -->
        <el-card
          v-if="(hasRole(['ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_MENTOR']) && pendingReviewData && (pendingReviewData.pendingLogCount > 0 || pendingReviewData.pendingReportCount > 0))"
          class="chart-card pending-review-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">待批阅提醒</span>
              <el-icon class="chart-icon" :size="16" style="color: #F7BA2A;">
                <Document />
              </el-icon>
            </div>
          </template>
          <div class="pending-review-content">
            <el-alert
              :title="`待批阅日志：${pendingReviewData.pendingLogCount} 篇`"
              type="warning"
              :closable="false"
              show-icon
              style="margin-bottom: 10px;"
            />
            <el-alert
              :title="`待批阅周报：${pendingReviewData.pendingReportCount} 篇`"
              type="warning"
              :closable="false"
              show-icon
            />
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
import PieChart from '@/components/charts/PieChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import { statisticsApi } from '@/api/statistics'
import {
  User,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  Document,
  Clock,
  TrendCharts,
  PieChart as PieChartIcon
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const userRoles = computed(() => authStore.roles || [])
const loading = ref(false)
const filterParams = ref({})

// 判断是否有指定角色
const hasRole = (roles) => {
  return hasAnyRole(roles)
}

// 判断是否显示筛选器（学生和企业导师不显示筛选器）
const showFilter = computed(() => {
  return !hasRole(['ROLE_STUDENT', 'ROLE_ENTERPRISE_MENTOR'])
})

// 统计卡片数据
const statisticsCards = ref([])

// 图表数据
const chartData = reactive({
  progress: null,
  postType: null,
  scoreDistribution: null,
  studentRanking: null,
  comparison: null,
  duration: null,
  personalScore: null
})

// 待批阅数据
const pendingReviewData = ref(null)

// 判断是否有某个图表
const hasChart = (key) => {
  if (key === 'progress') {
    return hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'])
  }
  if (key === 'postType') {
    return hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'])
  }
  if (key === 'scoreDistribution') {
    return hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
  }
  if (key === 'studentRanking') {
    return hasRole(['ROLE_CLASS_TEACHER'])
  }
  if (key === 'comparison') {
    return hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
  }
  if (key === 'duration') {
    return hasRole(['ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'])
  }
  if (key === 'personalScore') {
    return hasRole(['ROLE_STUDENT'])
  }
  return false
}

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
    } else if (hasRole(['ROLE_ENTERPRISE_MENTOR'])) {
      await loadEnterpriseMentorStatistics()
    } else if (hasRole(['ROLE_STUDENT'])) {
      await loadStudentStatistics()
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 构建查询参数
const buildQueryParams = () => {
  const params = {}
  if (filterParams.value.dateRange && filterParams.value.dateRange.length === 2) {
    params.startDate = filterParams.value.dateRange[0]
    params.endDate = filterParams.value.dateRange[1]
  }
  if (filterParams.value.schoolId) {
    params.schoolId = filterParams.value.schoolId
  }
  if (filterParams.value.collegeId) {
    params.collegeId = filterParams.value.collegeId
  }
  if (filterParams.value.majorId) {
    params.majorId = filterParams.value.majorId
  }
  if (filterParams.value.classId) {
    params.classId = filterParams.value.classId
  }
  return params
}

// 系统管理员统计数据
const loadSystemAdminStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '总学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        },
        {
          key: 'pending',
          title: '待开始',
          value: progressData.pendingCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#C0C4CC'
        }
      ]
      
      // 设置饼图数据
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载评价分数统计
    const scoreRes = await statisticsApi.getEvaluationScoreStatistics(params)
    if (scoreRes.code === 200 && scoreRes.data) {
      const scoreData = scoreRes.data
      if (scoreData.barChartData) {
        chartData.scoreDistribution = {
          data: scoreData.barChartData
        }
      }
    }
    
    // 加载岗位类型分布统计
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
    
    // TODO: 加载各学校实习人数对比（需要后端实现）
    chartData.comparison = null
  } catch (error) {
    console.error('加载系统管理员统计数据失败:', error)
  }
}

// 学校管理员统计数据
const loadSchoolAdminStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '本学校实习学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        }
      ]
      
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载评价分数统计
    const scoreRes = await statisticsApi.getEvaluationScoreStatistics(params)
    if (scoreRes.code === 200 && scoreRes.data) {
      const scoreData = scoreRes.data
      statisticsCards.value.push({
        key: 'averageScore',
        title: '平均评价分数',
        value: scoreData.averageScore || 0,
        unit: '分',
        icon: 'DataAnalysis',
        color: '#409EFF'
      })
      
      if (scoreData.barChartData) {
        chartData.scoreDistribution = {
          data: scoreData.barChartData
        }
      }
    }
    
    // 加载专业维度统计（各学院实习人数对比）
    const majorRes = await statisticsApi.getMajorStatistics(params)
    if (majorRes.code === 200 && majorRes.data && majorRes.data.barChartData) {
      chartData.comparison = {
        title: '各专业实习人数对比',
        data: majorRes.data.barChartData
      }
    }
    
    // 加载岗位类型分布
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
    
    // TODO: 加载实习时长分布（按月份）
    chartData.duration = null
  } catch (error) {
    console.error('加载学校管理员统计数据失败:', error)
  }
}

// 学院负责人统计数据
const loadCollegeLeaderStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '本院实习学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        }
      ]
      
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载评价分数统计
    const scoreRes = await statisticsApi.getEvaluationScoreStatistics(params)
    if (scoreRes.code === 200 && scoreRes.data) {
      const scoreData = scoreRes.data
      statisticsCards.value.push({
        key: 'averageScore',
        title: '平均评价分数',
        value: scoreData.averageScore || 0,
        unit: '分',
        icon: 'DataAnalysis',
        color: '#409EFF'
      })
      
      if (scoreData.barChartData) {
        chartData.scoreDistribution = {
          data: scoreData.barChartData
        }
      }
    }
    
    // 加载专业维度统计
    const majorRes = await statisticsApi.getMajorStatistics(params)
    if (majorRes.code === 200 && majorRes.data && majorRes.data.barChartData) {
      chartData.comparison = {
        title: '各专业实习人数对比',
        data: majorRes.data.barChartData
      }
    }
    
    // 加载岗位类型分布
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
  } catch (error) {
    console.error('加载学院负责人统计数据失败:', error)
  }
}

// 班主任统计数据
const loadClassTeacherStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '本班级实习学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        }
      ]
      
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载评价分数统计
    const scoreRes = await statisticsApi.getEvaluationScoreStatistics(params)
    if (scoreRes.code === 200 && scoreRes.data) {
      const scoreData = scoreRes.data
      statisticsCards.value.push({
        key: 'averageScore',
        title: '平均评价分数',
        value: scoreData.averageScore || 0,
        unit: '分',
        icon: 'DataAnalysis',
        color: '#409EFF'
      })
      
      if (scoreData.barChartData) {
        chartData.scoreDistribution = {
          data: scoreData.barChartData
        }
      }
    }
    
    // 加载学生分数排行
    const rankingRes = await statisticsApi.getStudentScoreRanking(params)
    if (rankingRes.code === 200 && rankingRes.data && rankingRes.data.barChartData) {
      chartData.studentRanking = {
        data: rankingRes.data.barChartData.map(item => ({
          name: item.name,
          value: parseFloat(item.value) || 0,
          color: item.color || '#409EFF'
        }))
      }
    }
    
    // 加载岗位类型分布
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
    
    // 加载待批阅统计
    const pendingRes = await statisticsApi.getPendingReviewStatistics(params)
    if (pendingRes.code === 200 && pendingRes.data) {
      pendingReviewData.value = pendingRes.data
    }
  } catch (error) {
    console.error('加载班主任统计数据失败:', error)
  }
}

// 企业管理员统计数据
const loadEnterpriseAdminStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '本企业实习学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        },
        {
          key: 'pending',
          title: '待评价',
          value: progressData.pendingCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#F7BA2A'
        }
      ]
      
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载评价分数统计
    const scoreRes = await statisticsApi.getEvaluationScoreStatistics(params)
    if (scoreRes.code === 200 && scoreRes.data && scoreRes.data.barChartData) {
      chartData.scoreDistribution = {
        data: scoreRes.data.barChartData
      }
    }
    
    // 加载岗位类型分布
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
    
    // TODO: 加载实习时长统计
    chartData.duration = null
  } catch (error) {
    console.error('加载企业管理员统计数据失败:', error)
  }
}

// 企业导师统计数据
const loadEnterpriseMentorStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // 加载实习进度统计
    const progressRes = await statisticsApi.getInternshipProgressStatistics(params)
    if (progressRes.code === 200 && progressRes.data) {
      const progressData = progressRes.data
      statisticsCards.value = [
        {
          key: 'totalStudents',
          title: '所带实习学生数',
          value: progressData.totalCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'inProgress',
          title: '进行中',
          value: progressData.inProgressCount || 0,
          unit: '人',
          icon: 'Clock',
          color: '#409EFF'
        },
        {
          key: 'completed',
          title: '已完成',
          value: progressData.completedCount || 0,
          unit: '人',
          icon: 'Document',
          color: '#67C23A'
        }
      ]
      
      if (progressData.pieChartData) {
        chartData.progress = {
          data: [
            progressData.pieChartData.pending,
            progressData.pieChartData.inProgress,
            progressData.pieChartData.completed
          ].filter(item => item && item.value > 0)
        }
      }
    }
    
    // 加载岗位类型分布
    const postRes = await statisticsApi.getPostTypeDistributionStatistics(params)
    if (postRes.code === 200 && postRes.data && postRes.data.pieChartData) {
      chartData.postType = {
        data: postRes.data.pieChartData
      }
    }
    
    // 加载实习时长统计
    const durationRes = await statisticsApi.getInternshipDurationStatistics(params)
    if (durationRes.code === 200 && durationRes.data) {
      // TODO: 根据实际返回的数据格式处理
      chartData.duration = null
    }
    
    // 加载待批阅统计
    const pendingRes = await statisticsApi.getPendingReviewStatistics(params)
    if (pendingRes.code === 200 && pendingRes.data) {
      pendingReviewData.value = pendingRes.data
    }
  } catch (error) {
    console.error('加载企业导师统计数据失败:', error)
  }
}

// 学生统计数据
const loadStudentStatistics = async () => {
  try {
    const params = buildQueryParams()
    
    // TODO: 加载学生个人数据
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
        value: 0,
        unit: '分',
        icon: 'DataAnalysis',
        color: '#409EFF'
      },
      {
        key: 'logs',
        title: '已提交日志数',
        value: 0,
        unit: '篇',
        icon: 'Document',
        color: '#409EFF'
      },
      {
        key: 'reports',
        title: '已提交周报数',
        value: 0,
        unit: '篇',
        icon: 'Document',
        color: '#409EFF'
      }
    ]
    
    // TODO: 加载个人评价分数雷达图
    chartData.personalScore = null
    
    // TODO: 加载实习进度时间线
    // TODO: 加载日志/周报提交情况
  } catch (error) {
    console.error('加载学生统计数据失败:', error)
  }
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

.pending-review-card {
  border-color: #F7BA2A;
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
  min-height: 300px;
}

.pending-review-content {
  padding: 10px 0;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

:deep(.el-card__body) {
  padding: 20px;
}
</style>
