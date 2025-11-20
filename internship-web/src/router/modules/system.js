/**
 * 系统管理模块路由
 */
export default [
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
    path: '/admin/system/class-teacher',
    name: 'ClassTeacherAppointment',
    component: () => import('@/views/admin/ClassTeacherAppointment.vue'),
    meta: {
      title: '班主任任命',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_COLLEGE_LEADER']
    }
  }
]

