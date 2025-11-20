import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue'),
    meta: {
      title: '首页',
      requiresAuth: true
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限',
      requiresAuth: false
    }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false
    }
  },
  // 系统管理模块路由
  {
    path: '/admin/system/school',
    name: 'SchoolManagement',
    component: () => import('@/views/admin/SchoolManagement.vue'),
    meta: {
      title: '学校管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/system/college',
    name: 'CollegeManagement',
    component: () => import('@/views/admin/CollegeManagement.vue'),
    meta: {
      title: '学院管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/system/major',
    name: 'MajorManagement',
    component: () => import('@/views/admin/MajorManagement.vue'),
    meta: {
      title: '专业管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/system/class',
    name: 'ClassManagement',
    component: () => import('@/views/admin/ClassManagement.vue'),
    meta: {
      title: '班级管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/system/semester',
    name: 'SemesterManagement',
    component: () => import('@/views/admin/SemesterManagement.vue'),
    meta: {
      title: '学期管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/system/config',
    name: 'SystemConfigManagement',
    component: () => import('@/views/admin/SystemConfigManagement.vue'),
    meta: {
      title: '系统配置',
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 实习管理系统`
  }
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 检查是否已登录
    if (!authStore.token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
      return
    }
    
    // 检查Token是否有效（这里可以添加Token验证逻辑）
    // 如果Token无效，清除并跳转到登录页
    
    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
      const userRoles = authStore.userInfo?.roles || []
      if (!hasAnyRole(userRoles, to.meta.roles)) {
        next('/403')
        return
      }
    }
  }
  
  // 如果已登录，访问登录页则跳转到首页
  if (to.path === '/login' && authStore.token) {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
