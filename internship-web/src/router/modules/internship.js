/**
 * 实习管理模块路由
 */
export default [
  {
    path: '/admin/internship/plan',
    name: 'InternshipPlanManagement',
    component: () => import('@/views/admin/InternshipPlanManagement.vue'),
    meta: {
      title: '实习计划管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
    }
  },
  {
    path: '/enterprise/internship/post',
    name: 'InternshipPostManagement',
    component: () => import('@/views/enterprise/InternshipPostManagement.vue'),
    meta: {
      title: '岗位管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/admin/internship/post',
    name: 'AdminInternshipPostManagement',
    component: () => import('@/views/enterprise/InternshipPostManagement.vue'),
    meta: {
      title: '岗位管理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
    }
  },
  {
    path: '/student/internship/post',
    name: 'InternshipPostList',
    component: () => import('@/views/student/InternshipPostList.vue'),
    meta: {
      title: '岗位列表',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/internship/apply',
    name: 'InternshipApply',
    component: () => import('@/views/student/InternshipApply.vue'),
    meta: {
      title: '实习申请',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/internship/my',
    name: 'MyInternship',
    component: () => import('@/views/student/MyInternship.vue'),
    meta: {
      title: '我的实习',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/admin/internship/apply/audit',
    name: 'InternshipApplyAudit',
    component: () => import('@/views/admin/InternshipApplyAudit.vue'),
    meta: {
      title: '实习申请审核',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
    }
  },
  {
    path: '/enterprise/internship/apply',
    name: 'EnterpriseApplicationManagement',
    component: () => import('@/views/enterprise/ApplicationManagement.vue'),
    meta: {
      title: '申请管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/enterprise/internship/student',
    name: 'EnterpriseStudentManagement',
    component: () => import('@/views/enterprise/StudentManagement.vue'),
    meta: {
      title: '实习学生管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/enterprise/internship/interview',
    name: 'EnterpriseInterviewManagement',
    component: () => import('@/views/enterprise/InterviewManagement.vue'),
    meta: {
      title: '面试管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN']
    }
  },
  {
    path: '/student/internship/interview',
    name: 'StudentInterviewList',
    component: () => import('@/views/student/InterviewList.vue'),
    meta: {
      title: '我的面试',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/internship/log',
    name: 'StudentInternshipLog',
    component: () => import('@/views/student/InternshipLog.vue'),
    meta: {
      title: '实习日志',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/teacher/internship/log',
    name: 'TeacherInternshipLog',
    component: () => import('@/views/teacher/InternshipLog.vue'),
    meta: {
      title: '实习日志批阅',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  },
  {
    path: '/student/internship/weekly-report',
    name: 'StudentWeeklyReport',
    component: () => import('@/views/student/WeeklyReport.vue'),
    meta: {
      title: '周报',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/teacher/internship/weekly-report',
    name: 'TeacherWeeklyReport',
    component: () => import('@/views/teacher/WeeklyReport.vue'),
    meta: {
      title: '周报批阅',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  },
  {
    path: '/enterprise/internship/attendance',
    name: 'EnterpriseAttendance',
    component: () => import('@/views/enterprise/AttendanceManagement.vue'),
    meta: {
      title: '考勤管理',
      requiresAuth: true,
      roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  },
  {
    path: '/student/internship/attendance',
    name: 'StudentAttendance',
    component: () => import('@/views/student/Attendance.vue'),
    meta: {
      title: '我的考勤',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/student/internship/achievement',
    name: 'StudentAchievement',
    component: () => import('@/views/student/Achievement.vue'),
    meta: {
      title: '阶段性成果',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/teacher/internship/achievement',
    name: 'TeacherAchievement',
    component: () => import('@/views/teacher/Achievement.vue'),
    meta: {
      title: '成果审核',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  },
  {
    path: '/student/internship/feedback',
    name: 'StudentFeedback',
    component: () => import('@/views/student/Feedback.vue'),
    meta: {
      title: '问题反馈',
      requiresAuth: true,
      roles: ['ROLE_STUDENT']
    }
  },
  {
    path: '/teacher/internship/feedback',
    name: 'TeacherFeedback',
    component: () => import('@/views/teacher/Feedback.vue'),
    meta: {
      title: '问题反馈处理',
      requiresAuth: true,
      roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
    }
  }
]

