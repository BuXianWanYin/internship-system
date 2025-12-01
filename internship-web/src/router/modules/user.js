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
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
    }
  },
  {
    path: '/admin/student',
    name: 'StudentManagement',
    component: () => import('@/views/admin/StudentManagement.vue'),
    meta: {
      title: '学生管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
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
  },
  {
    path: '/admin/enterprise',
    name: 'EnterpriseManagement',
    component: () => import('@/views/admin/EnterpriseManagement.vue'),
    meta: {
      title: '企业管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
    }
  },
  {
    path: '/admin/enterprise-mentor',
    name: 'EnterpriseMentorManagement',
    component: () => import('@/views/admin/EnterpriseMentorManagement.vue'),
    meta: {
      title: '企业导师管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/enterprise/cooperation',
    name: 'CooperationManagement',
    component: () => import('@/views/enterprise/CooperationManagement.vue'),
    meta: {
      title: '合作管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/admin/cooperation/audit',
    name: 'CooperationAudit',
    component: () => import('@/views/admin/CooperationAudit.vue'),
    meta: {
      title: '合作申请审核',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']
    }
  }
]

