/**
 * 评价管理模块路由
 */
export default [
  // 企业评价
  {
    path: '/enterprise/evaluation/student',
    name: 'StudentEvaluation',
    component: () => import('@/views/enterprise/StudentEvaluation.vue'),
    meta: {
      title: '学生评价',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  },
  // 学校评价（教师端）
  {
    path: '/teacher/evaluation/student',
    name: 'TeacherStudentEvaluation',
    component: () => import('@/views/teacher/StudentEvaluation.vue'),
    meta: {
      title: '学生评价',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
    }
  },
  // 学生自评
  {
    path: '/student/evaluation/self',
    name: 'SelfEvaluation',
    component: () => import('@/views/student/SelfEvaluation.vue'),
    meta: {
      title: '自我评价',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  // 综合成绩
  {
    path: '/student/evaluation/comprehensive-score',
    name: 'ComprehensiveScore',
    component: () => import('@/views/student/ComprehensiveScore.vue'),
    meta: {
      title: '综合成绩',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  }
]

