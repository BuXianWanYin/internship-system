/**
 * 用户管理模块路由
 */
export default [
  {
    path: '/admin/user',
    name: 'UserManagement',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: {
      title: '用户管理',
      requiresAuth: true
    }
  },
  {
    path: '/admin/student/import',
    name: 'StudentImport',
    component: () => import('@/views/admin/StudentImport.vue'),
    meta: {
      title: '学生批量导入',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER']
    }
  },
  {
    path: '/admin/student/approval',
    name: 'StudentApproval',
    component: () => import('@/views/admin/StudentApproval.vue'),
    meta: {
      title: '学生注册审核',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER']
    }
  },
  {
    path: '/register/student',
    name: 'StudentRegister',
    component: () => import('@/views/register/StudentRegister.vue'),
    meta: {
      title: '学生注册',
      requiresAuth: false
    }
  },
  {
    path: '/register/enterprise',
    name: 'EnterpriseRegister',
    component: () => import('@/views/register/EnterpriseRegister.vue'),
    meta: {
      title: '企业注册',
      requiresAuth: false
    }
  },
  {
    path: '/admin/teacher',
    name: 'TeacherManagement',
    component: () => import('@/views/admin/TeacherManagement.vue'),
    meta: {
      title: '教师管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
    }
  }
]

