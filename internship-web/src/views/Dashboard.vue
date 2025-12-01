<template>
  <div class="dashboard">
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

    <!-- 主要内容区域：快捷操作 + 图表 -->
    <div class="dashboard-main">
      <!-- 左侧：快捷操作 -->
      <div class="main-left">
        <QuickActions
          v-if="quickActions.length > 0"
          :actions="quickActions"
          icon="Operation"
        />
      </div>

      <!-- 右侧：图表区域 -->
      <div class="main-right">
        <div class="charts-grid">
        <!-- 饼图：实习进度统计/用户角色分布/性别分布等 -->
        <el-card
          v-if="hasChart('progress')"
          class="chart-card"
          shadow="hover"
        >
          <template #header>
            <div class="chart-header">
              <span class="chart-title">{{ getProgressChartTitle }}</span>
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
              :height="320"
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
              :height="320"
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
              :height="320"
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
              :height="320"
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
              :height="320"
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
              :height="320"
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
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'
import StatisticsCard from '@/components/common/StatisticsCard.vue'
import QuickActions from '@/components/common/QuickActions.vue'
import PieChart from '@/components/charts/PieChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import LineChart from '@/components/charts/LineChart.vue'
import RadarChart from '@/components/charts/RadarChart.vue'
import { statisticsApi } from '@/api/statistics'
import { useRouter } from 'vue-router'
import {
  User,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  Document,
  Clock,
  TrendCharts,
  PieChart as PieChartIcon,
  EditPen,
  DocumentChecked,
  List,
  Setting,
  School,
  Briefcase,
  Files,
  Reading,
  Calendar,
  Star,
  Operation
} from '@element-plus/icons-vue'

const authStore = useAuthStore()
const router = useRouter()
const userRoles = computed(() => authStore.roles || [])
const loading = ref(false)

// 判断是否有指定角色
const hasRole = (roles) => {
  return hasAnyRole(roles)
}

// 快捷操作数据
const quickActions = computed(() => {
  const actions = []
  
  // 系统管理员快捷操作
  if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
    actions.push(
      { key: 'school', title: '学校管理', desc: '管理系统学校', icon: 'School', color: '#409EFF', path: '/admin/system/school' },
      { key: 'college', title: '学院管理', desc: '管理系统学院', icon: 'OfficeBuilding', color: '#67C23A', path: '/admin/system/college' },
      { key: 'user', title: '用户管理', desc: '管理系统用户', icon: 'User', color: '#F7BA2A', path: '/admin/system/user' },
      { key: 'enterprise', title: '企业管理', desc: '管理企业信息', icon: 'Briefcase', color: '#E6A23C', path: '/admin/enterprise' },
      { key: 'plan', title: '实习计划', desc: '查看实习计划', icon: 'Calendar', color: '#909399', path: '/admin/internship/plan' },
      { key: 'audit', title: '申请审核', desc: '审核实习申请', icon: 'DocumentChecked', color: '#409EFF', path: '/admin/internship/apply/audit' },
      { key: 'config', title: '系统配置', desc: '系统参数配置', icon: 'Setting', color: '#67C23A', path: '/admin/system/config' }
    )
  }
  
  // 学校管理员快捷操作
  if (hasRole(['ROLE_SCHOOL_ADMIN'])) {
    actions.push(
      { key: 'college', title: '学院管理', desc: '管理本学校学院', icon: 'OfficeBuilding', color: '#409EFF', path: '/admin/system/college' },
      { key: 'major', title: '专业管理', desc: '管理本学校专业', icon: 'Reading', color: '#67C23A', path: '/admin/system/major' },
      { key: 'class', title: '班级管理', desc: '管理本学校班级', icon: 'User', color: '#F7BA2A', path: '/admin/system/class' },
      { key: 'plan', title: '实习计划', desc: '查看实习计划', icon: 'Calendar', color: '#E6A23C', path: '/admin/internship/plan' },
      { key: 'audit', title: '申请审核', desc: '审核实习申请', icon: 'DocumentChecked', color: '#909399', path: '/admin/internship/apply/audit' },
      { key: 'evaluation', title: '学生评价', desc: '评价学生实习', icon: 'Star', color: '#409EFF', path: '/teacher/student/evaluation' }
    )
  }
  
  // 学院负责人快捷操作
  if (hasRole(['ROLE_COLLEGE_LEADER'])) {
    actions.push(
      { key: 'major', title: '专业管理', desc: '管理本院专业', icon: 'Reading', color: '#409EFF', path: '/admin/system/major' },
      { key: 'class', title: '班级管理', desc: '管理本院班级', icon: 'User', color: '#67C23A', path: '/admin/system/class' },
      { key: 'teacher', title: '班主任任命', desc: '任命班级班主任', icon: 'UserFilled', color: '#F7BA2A', path: '/admin/system/class-teacher' },
      { key: 'plan', title: '实习计划', desc: '查看实习计划', icon: 'Calendar', color: '#E6A23C', path: '/admin/internship/plan' },
      { key: 'audit', title: '申请审核', desc: '审核实习申请', icon: 'DocumentChecked', color: '#909399', path: '/admin/internship/apply/audit' },
      { key: 'evaluation', title: '学生评价', desc: '评价学生实习', icon: 'Star', color: '#409EFF', path: '/teacher/student/evaluation' }
    )
  }
  
  // 班主任快捷操作
  if (hasRole(['ROLE_CLASS_TEACHER'])) {
    actions.push(
      { key: 'class', title: '班级管理', desc: '管理本班级信息', icon: 'User', color: '#409EFF', path: '/admin/system/class' },
      { key: 'log', title: '日志批阅', desc: '批阅学生日志', icon: 'Document', color: '#67C23A', path: '/teacher/internship/log' },
      { key: 'report', title: '周报批阅', desc: '批阅学生周报', icon: 'Files', color: '#F7BA2A', path: '/teacher/internship/weekly-report' },
      { key: 'evaluation', title: '学生评价', desc: '评价学生实习', icon: 'Star', color: '#E6A23C', path: '/teacher/student/evaluation' },
      { key: 'audit', title: '申请审核', desc: '审核实习申请', icon: 'DocumentChecked', color: '#909399', path: '/admin/internship/apply/audit' }
    )
  }
  
  // 企业管理员快捷操作
  if (hasRole(['ROLE_ENTERPRISE_ADMIN'])) {
    actions.push(
      { key: 'post', title: '发布岗位', desc: '发布实习岗位', icon: 'Briefcase', color: '#409EFF', path: '/enterprise/internship/post' },
      { key: 'student', title: '学生管理', desc: '管理实习学生', icon: 'User', color: '#67C23A', path: '/enterprise/internship/student' },
      { key: 'application', title: '申请管理', desc: '处理学生申请', icon: 'List', color: '#F7BA2A', path: '/enterprise/internship/apply' },
      { key: 'evaluation', title: '学生评价', desc: '评价实习学生', icon: 'Star', color: '#E6A23C', path: '/enterprise/student/evaluation' },
      { key: 'attendance', title: '考勤管理', desc: '管理学生考勤', icon: 'Clock', color: '#909399', path: '/enterprise/internship/attendance' }
    )
  }
  
  // 企业导师快捷操作
  if (hasRole(['ROLE_ENTERPRISE_MENTOR'])) {
    actions.push(
      { key: 'log', title: '日志批阅', desc: '批阅学生日志', icon: 'Document', color: '#409EFF', path: '/teacher/internship/log' },
      { key: 'report', title: '周报批阅', desc: '批阅学生周报', icon: 'Files', color: '#67C23A', path: '/teacher/internship/weekly-report' },
      { key: 'attendance', title: '考勤管理', desc: '管理学生考勤', icon: 'Clock', color: '#F7BA2A', path: '/enterprise/internship/attendance' },
      { key: 'evaluation', title: '学生评价', desc: '评价实习学生', icon: 'Star', color: '#E6A23C', path: '/enterprise/student/evaluation' }
    )
  }
  
  // 学生快捷操作
  if (hasRole(['ROLE_STUDENT'])) {
    actions.push(
      { key: 'apply', title: '申请实习', desc: '申请实习岗位', icon: 'EditPen', color: '#409EFF', path: '/student/internship/apply' },
      { key: 'log', title: '实习日志', desc: '提交实习日志', icon: 'Document', color: '#67C23A', path: '/student/internship/log' },
      { key: 'report', title: '实习周报', desc: '提交实习周报', icon: 'Files', color: '#F7BA2A', path: '/student/internship/weekly-report' },
      { key: 'score', title: '查看成绩', desc: '查看评价成绩', icon: 'DataAnalysis', color: '#E6A23C', path: '/student/evaluation/score' }
    )
  }
  
  return actions
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

// 获取进度图表的标题
const getProgressChartTitle = computed(() => {
  if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
    return '用户角色分布'
  } else if (hasRole(['ROLE_SCHOOL_ADMIN'])) {
    return '学生性别分布'
  } else if (hasRole(['ROLE_CLASS_TEACHER'])) {
    return '评价分数分布'
  }
  return '实习进度统计'
})

// 判断是否有某个图表
const hasChart = (key) => {
  if (key === 'progress') {
    if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
      return chartData.progress && chartData.progress.data && chartData.progress.data.length > 0
    } else if (hasRole(['ROLE_SCHOOL_ADMIN'])) {
      return chartData.progress && chartData.progress.data && chartData.progress.data.length > 0
    } else if (hasRole(['ROLE_CLASS_TEACHER'])) {
      return chartData.progress && chartData.progress.data && chartData.progress.data.length > 0
    }
    return hasRole(['ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'])
  }
  if (key === 'postType') {
    if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
      return chartData.postType && chartData.postType.data && chartData.postType.data.length > 0
    }
    return hasRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'])
  }
  if (key === 'scoreDistribution') {
    return hasRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
  }
  if (key === 'studentRanking') {
    return hasRole(['ROLE_CLASS_TEACHER'])
  }
  if (key === 'comparison') {
    if (hasRole(['ROLE_SYSTEM_ADMIN'])) {
      return chartData.comparison && chartData.comparison.data && chartData.comparison.data.length > 0
    } else if (hasRole(['ROLE_SCHOOL_ADMIN'])) {
      return chartData.comparison && chartData.comparison.data && chartData.comparison.data.length > 0
    }
    return hasRole(['ROLE_COLLEGE_LEADER'])
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
  return {}
}

// 系统管理员统计数据
const loadSystemAdminStatistics = async () => {
  try {
    // 加载系统管理员仪表盘数据
    const dashboardRes = await statisticsApi.getSystemAdminDashboard()
    if (dashboardRes.code === 200 && dashboardRes.data) {
      const dashboard = dashboardRes.data
      
      // 设置统计卡片
      statisticsCards.value = [
        {
          key: 'schoolCount',
          title: '学校总数',
          value: dashboard.schoolCount || 0,
          unit: '所',
          icon: 'School',
          color: '#409EFF'
        },
        {
          key: 'userCount',
          title: '用户总数',
          value: dashboard.userCount || 0,
          unit: '人',
          icon: 'User',
          color: '#67C23A'
        },
        {
          key: 'postCount',
          title: '实习岗位数',
          value: dashboard.postCount || 0,
          unit: '个',
          icon: 'Briefcase',
          color: '#F7BA2A'
        },
        {
          key: 'internshipStudentCount',
          title: '实习学生数',
          value: dashboard.internshipStudentCount || 0,
          unit: '人',
          icon: 'User',
          color: '#E6A23C'
        }
      ]
      
      // 设置用户角色分布饼图
      if (dashboard.userRoleDistribution && dashboard.userRoleDistribution.length > 0) {
        chartData.progress = {
          data: dashboard.userRoleDistribution
        }
      }
      
      // 设置各学校实习人数对比柱状图
      if (dashboard.schoolInternshipComparison && dashboard.schoolInternshipComparison.length > 0) {
        chartData.comparison = {
          title: '各学校实习人数对比',
          data: dashboard.schoolInternshipComparison
        }
      }
      
      // 设置实习岗位类型分布饼图
      if (dashboard.postTypeDistribution && dashboard.postTypeDistribution.length > 0) {
        chartData.postType = {
          data: dashboard.postTypeDistribution
        }
      }
    }
  } catch (error) {
    console.error('加载系统管理员统计数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  }
}

// 学校管理员统计数据
const loadSchoolAdminStatistics = async () => {
  try {
    // 加载学校管理员仪表盘数据
    const dashboardRes = await statisticsApi.getSchoolAdminDashboard()
    if (dashboardRes.code === 200 && dashboardRes.data) {
      const dashboard = dashboardRes.data
      
      // 设置统计卡片
      statisticsCards.value = [
        {
          key: 'collegeCount',
          title: '学院总数',
          value: dashboard.collegeCount || 0,
          unit: '个',
          icon: 'OfficeBuilding',
          color: '#409EFF'
        },
        {
          key: 'majorCount',
          title: '专业总数',
          value: dashboard.majorCount || 0,
          unit: '个',
          icon: 'Reading',
          color: '#67C23A'
        },
        {
          key: 'studentCount',
          title: '学生总数',
          value: dashboard.studentCount || 0,
          unit: '人',
          icon: 'User',
          color: '#F7BA2A'
        },
        {
          key: 'teacherCount',
          title: '教师总数',
          value: dashboard.teacherCount || 0,
          unit: '人',
          icon: 'UserFilled',
          color: '#E6A23C'
        },
        {
          key: 'maleCount',
          title: '男生人数',
          value: dashboard.maleCount || 0,
          unit: '人',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'femaleCount',
          title: '女生人数',
          value: dashboard.femaleCount || 0,
          unit: '人',
          icon: 'User',
          color: '#F56C6C'
        }
      ]
      
      // 设置不同专业实习人数对比柱状图
      if (dashboard.majorInternshipComparison && dashboard.majorInternshipComparison.length > 0) {
        chartData.comparison = {
          title: '不同专业实习人数对比',
          data: dashboard.majorInternshipComparison
        }
      }
      
      // 设置学生性别分布饼图
      if (dashboard.genderDistribution && dashboard.genderDistribution.length > 0) {
        chartData.progress = {
          data: dashboard.genderDistribution
        }
      }
    }
  } catch (error) {
    console.error('加载学校管理员统计数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  }
}

// 学院负责人统计数据
const loadCollegeLeaderStatistics = async () => {
  try {
    // 加载学院负责人仪表盘数据
    const dashboardRes = await statisticsApi.getCollegeLeaderDashboard()
    if (dashboardRes.code === 200 && dashboardRes.data) {
      const dashboard = dashboardRes.data
      
      // 设置统计卡片
      statisticsCards.value = [
        {
          key: 'majorCount',
          title: '专业总数',
          value: dashboard.majorCount || 0,
          unit: '个',
          icon: 'Reading',
          color: '#409EFF'
        },
        {
          key: 'studentCount',
          title: '学生总数',
          value: dashboard.studentCount || 0,
          unit: '人',
          icon: 'User',
          color: '#67C23A'
        },
        {
          key: 'teacherCount',
          title: '教师总数',
          value: dashboard.teacherCount || 0,
          unit: '人',
          icon: 'UserFilled',
          color: '#F7BA2A'
        },
        {
          key: 'classCount',
          title: '班级总数',
          value: dashboard.classCount || 0,
          unit: '个',
          icon: 'User',
          color: '#E6A23C'
        }
      ]
    }
  } catch (error) {
    console.error('加载学院负责人统计数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
  }
}

// 班主任统计数据
const loadClassTeacherStatistics = async () => {
  try {
    // 加载班主任仪表盘数据
    const dashboardRes = await statisticsApi.getClassTeacherDashboard()
    if (dashboardRes.code === 200 && dashboardRes.data) {
      const dashboard = dashboardRes.data
      
      // 设置统计卡片
      statisticsCards.value = [
        {
          key: 'classCount',
          title: '管理班级数',
          value: dashboard.classCount || 0,
          unit: '个',
          icon: 'User',
          color: '#409EFF'
        },
        {
          key: 'studentCount',
          title: '学生总数',
          value: dashboard.studentCount || 0,
          unit: '人',
          icon: 'User',
          color: '#67C23A'
        },
        {
          key: 'postCount',
          title: '实习岗位数',
          value: dashboard.postCount || 0,
          unit: '个',
          icon: 'Briefcase',
          color: '#F7BA2A'
        },
        {
          key: 'averageScore',
          title: '平均评价分数',
          value: dashboard.averageScore || 0,
          unit: '分',
          icon: 'DataAnalysis',
          color: '#E6A23C'
        }
      ]
      
      // 设置评价分数分布饼图
      if (dashboard.scoreDistribution && dashboard.scoreDistribution.length > 0) {
        chartData.progress = {
          data: dashboard.scoreDistribution
        }
      }
    }
    
    // 继续加载其他数据（学生分数排行、待批阅等）
    const params = buildQueryParams()
    
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
    
    // 加载待批阅统计
    const pendingRes = await statisticsApi.getPendingReviewStatistics(params)
    if (pendingRes.code === 200 && pendingRes.data) {
      pendingReviewData.value = pendingRes.data
    }
  } catch (error) {
    console.error('加载班主任统计数据失败:', error)
    ElMessage.error('加载仪表盘数据失败')
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


onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 20px 24px 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
  overflow-x: hidden;
  display: flex;
  flex-direction: column;
}

.statistics-cards {
  margin-bottom: 20px;
  flex-shrink: 0;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

/* 主要内容区域布局 */
.dashboard-main {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
  align-items: start;
  flex: 1;
  min-height: 0;
}

.main-left {
  position: sticky;
  top: 16px;
  height: fit-content;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

.main-right {
  min-width: 0; /* 防止网格溢出 */
  display: flex;
  flex-direction: column;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  flex: 1;
  align-content: start;
  grid-auto-rows: minmax(380px, auto);
}

/* 响应式布局 */
@media (max-width: 1600px) {
  .cards-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1400px) {
  .dashboard-main {
    grid-template-columns: 280px 1fr;
  }
  
  .cards-grid {
    grid-template-columns: repeat(4, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 1200px) {
  .dashboard-main {
    grid-template-columns: 1fr;
  }
  
  .main-left {
    position: static;
    margin-bottom: 20px;
    max-height: none;
  }
  
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 16px;
  }
  
  .statistics-cards {
    margin-bottom: 16px;
  }
  
  .cards-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  
  .charts-grid {
    gap: 16px;
  }
  
  .dashboard-main {
    gap: 16px;
  }
  
  .chart-card {
    min-height: 320px;
  }
}

@media (max-width: 480px) {
  .cards-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  overflow: hidden;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 380px;
}

.chart-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.pending-review-card {
  border-color: rgba(247, 186, 42, 0.3);
  background: linear-gradient(135deg, #fffbf0 0%, #ffffff 100%);
}

.pending-review-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #F7BA2A, #ffc868);
  z-index: 1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-title {
  font-size: 15px;
  font-weight: 500;
  color: #303133;
  letter-spacing: 0.3px;
}

.chart-icon {
  color: #606266;
  opacity: 0.7;
}

.chart-content {
  position: relative;
  min-height: 320px;
  height: 100%;
  overflow: hidden;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pending-review-content {
  padding: 12px 0;
}

.pending-review-content :deep(.el-alert) {
  border-radius: 8px;
  border: none;
  box-shadow: 0 2px 8px rgba(247, 186, 42, 0.1);
  background: #fffbf0;
}

:deep(.el-card__header) {
  padding: 18px 20px;
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

.chart-card :deep(.el-card__header) {
  background: transparent;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

/* 防止内容溢出 */
:deep(.el-card) {
  overflow: hidden;
}

:deep(.el-card__body) {
  overflow: hidden;
}
</style>
