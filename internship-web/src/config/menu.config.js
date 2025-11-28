/**
 * 菜单配置
 * 根据角色动态生成菜单
 */

import {
  House,
  Setting,
  School,
  OfficeBuilding,
  Reading,
  User,
  UserFilled,
  Calendar,
  Tools,
  Upload,
  DocumentChecked,
  Document,
  Briefcase,
  EditPen,
  Clock,
  Files,
  ChatLineRound,
  List,
  Star,
  DataAnalysis
} from '@element-plus/icons-vue'

/**
 * 菜单项定义
 */
export const menuItems = [
  // 仪表盘 - 所有角色可见
  {
    index: '/dashboard',
    title: '仪表盘',
    icon: House,
    roles: ['*'] // * 表示所有角色
  },
  
  // ========== 学校管理（系统管理员、学校管理员） ==========
  {
    index: 'school-management',
    title: '学校管理',
    icon: School,
    roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'],
    children: [
      {
        index: '/admin/system/school',
        title: '学校管理',
        icon: School,
        roles: ['ROLE_SYSTEM_ADMIN']
      },
      {
        index: '/admin/system/college',
        title: '学院管理',
        icon: OfficeBuilding,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']
      },
      {
        index: '/admin/system/major',
        title: '专业管理',
        icon: Reading,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      },
      {
        index: '/admin/system/class',
        title: '班级管理',
        icon: User,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'],
        titleMap: {
          'ROLE_SYSTEM_ADMIN': '班级管理',
          'ROLE_SCHOOL_ADMIN': '班级管理',
          'ROLE_COLLEGE_LEADER': '班级管理',
          'ROLE_CLASS_TEACHER': '我管理的班级'
        }
      },
      {
        index: '/admin/system/semester',
        title: '学期管理',
        icon: Calendar,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']
      },
      {
        index: '/admin/system/class-teacher',
        title: '班主任任命',
        icon: UserFilled,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_COLLEGE_LEADER']
      }
    ]
  },
  
  // ========== 系统管理（系统配置） ==========
  {
    index: 'system',
    title: '系统管理',
    icon: Setting,
    roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN'],
    children: [
      {
        index: '/admin/system/config',
        title: '系统配置',
        icon: Tools,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']
      }
    ]
  },
  
  // ========== 用户管理 ==========
  {
    index: 'user',
    title: '用户管理',
    icon: UserFilled,
    roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'],
    children: [
      {
        index: '/admin/user',
        title: '用户管理',
        icon: User,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
      },
      {
        index: '/admin/student',
        title: '学生管理',
        icon: User,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER']
      },
      {
        index: '/admin/teacher',
        title: '教师管理',
        icon: UserFilled,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      }
    ]
  },
  
  // ========== 企业管理（系统管理员） ==========
  {
    index: 'enterprise-management',
    title: '企业管理',
    icon: OfficeBuilding,
    roles: ['ROLE_SYSTEM_ADMIN'],
    children: [
      {
        index: '/admin/enterprise',
        title: '企业管理',
        icon: OfficeBuilding,
        roles: ['ROLE_SYSTEM_ADMIN']
      },
      {
        index: '/admin/enterprise-mentor',
        title: '企业导师管理',
        icon: User,
        roles: ['ROLE_SYSTEM_ADMIN']
      },
      {
        index: '/admin/internship/post',
        title: '岗位管理',
        icon: Briefcase,
        roles: ['ROLE_SYSTEM_ADMIN']
      }
    ]
  },
  
  // ========== 企业管理（学校管理员） ==========
  {
    index: 'enterprise-cooperation',
    title: '合作企业管理',
    icon: OfficeBuilding,
    roles: ['ROLE_SCHOOL_ADMIN'],
    children: [
      {
        index: '/admin/enterprise',
        title: '合作企业管理',
        icon: OfficeBuilding,
        roles: ['ROLE_SCHOOL_ADMIN']
      },
      {
        index: '/admin/enterprise-mentor',
        title: '企业导师详情',
        icon: User,
        roles: ['ROLE_SCHOOL_ADMIN']
      }
    ]
  },
  
  // ========== 企业管理（企业管理员） ==========
  {
    index: 'enterprise',
    title: '企业管理',
    icon: OfficeBuilding,
    roles: ['ROLE_ENTERPRISE_ADMIN'],
    children: [
      {
        index: '/admin/enterprise-mentor',
        title: '企业导师管理',
        icon: User,
        roles: ['ROLE_ENTERPRISE_ADMIN']
      }
    ]
  },
  
  // ========== 实习管理（管理员） ==========
  {
    index: 'internship-admin',
    title: '实习管理',
    icon: Document,
    roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'],
    children: [
      {
        index: '/admin/internship/plan',
        title: '实习计划管理',
        icon: Document,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN']
      },
      {
        index: '/admin/internship/post',
        title: '岗位管理',
        icon: Briefcase,
        roles: ['ROLE_SCHOOL_ADMIN']
      },
      {
        index: '/admin/internship/apply/audit',
        title: '实习申请审核',
        icon: DocumentChecked,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER']
      },
      {
        index: '/teacher/internship/log',
        title: '实习日志批阅',
        icon: Document,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/weekly-report',
        title: '周报批阅',
        icon: Files,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/achievement',
        title: '成果审核',
        icon: Files,
        roles: ['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      }
    ]
  },
  
  // ========== 实习管理（企业） ==========
  {
    index: 'internship-enterprise',
    title: '实习管理',
    icon: Briefcase,
    roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'],
    children: [
      {
        index: '/enterprise/internship/post',
        title: '岗位管理',
        icon: Briefcase,
        roles: ['ROLE_ENTERPRISE_ADMIN']
      },
      {
        index: '/enterprise/internship/apply',
        title: '申请管理',
        icon: DocumentChecked,
        roles: ['ROLE_ENTERPRISE_ADMIN']
      },
      {
        index: '/enterprise/internship/student',
        title: '实习学生管理',
        icon: User,
        roles: ['ROLE_ENTERPRISE_ADMIN']
      },
      {
        index: '/enterprise/internship/interview',
        title: '面试管理',
        icon: ChatLineRound,
        roles: ['ROLE_ENTERPRISE_ADMIN']
      },
      {
        index: '/enterprise/internship/attendance',
        title: '考勤管理',
        icon: Clock,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/log',
        title: '实习日志批阅',
        icon: Document,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/weekly-report',
        title: '周报批阅',
        icon: Files,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/achievement',
        title: '成果审核',
        icon: Files,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/internship/feedback',
        title: '问题反馈处理',
        icon: ChatLineRound,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/enterprise/evaluation/student',
        title: '学生评价',
        icon: Star,
        roles: ['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      }
    ]
  },
  
  // ========== 实习管理（学生） ==========
  {
    index: 'internship-student',
    title: '实习管理',
    icon: Document,
    roles: ['ROLE_STUDENT'],
    children: [
      {
        index: '/student/internship/post',
        title: '岗位列表',
        icon: List,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/apply',
        title: '实习申请',
        icon: EditPen,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/interview',
        title: '我的面试',
        icon: ChatLineRound,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/my',
        title: '我的实习',
        icon: Briefcase,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/log',
        title: '实习日志',
        icon: Document,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/weekly-report',
        title: '周报',
        icon: Files,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/attendance',
        title: '我的考勤',
        icon: Clock,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/achievement',
        title: '阶段性成果',
        icon: Files,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/internship/feedback',
        title: '问题反馈',
        icon: ChatLineRound,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/evaluation/self',
        title: '自我评价',
        icon: Star,
        roles: ['ROLE_STUDENT']
      },
      {
        index: '/student/evaluation/comprehensive-score',
        title: '综合成绩',
        icon: DataAnalysis,
        roles: ['ROLE_STUDENT']
      }
    ]
  },
  
  // ========== 实习管理（教师） ==========
  {
    index: 'internship-teacher',
    title: '实习管理',
    icon: Document,
    roles: ['ROLE_CLASS_TEACHER'],
    children: [
      {
        index: '/teacher/internship/log',
        title: '实习日志批阅',
        icon: Document,
        roles: ['ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      },
      {
        index: '/teacher/internship/weekly-report',
        title: '周报批阅',
        icon: Files,
        roles: ['ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      },
      {
        index: '/teacher/internship/achievement',
        title: '成果审核',
        icon: Files,
        roles: ['ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      },
      {
        index: '/teacher/internship/feedback',
        title: '问题反馈处理',
        icon: ChatLineRound,
        roles: ['ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR']
      },
      {
        index: '/teacher/evaluation/student',
        title: '学生评价',
        icon: Star,
        roles: ['ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER']
      }
    ]
  }
]

/**
 * 检查菜单项是否对当前用户可见
 * @param {Object} menuItem 菜单项
 * @param {string[]} userRoles 用户角色列表
 * @returns {boolean}
 */
export function isMenuVisible(menuItem, userRoles) {
  if (!menuItem.roles || menuItem.roles.length === 0) {
    return false
  }
  
  // * 表示所有角色可见
  if (menuItem.roles.includes('*')) {
    return true
  }
  
  // 检查用户是否有任一角色
  return menuItem.roles.some(role => userRoles.includes(role))
}

/**
 * 根据用户角色过滤菜单
 * @param {string[]} userRoles 用户角色列表
 * @returns {Array} 过滤后的菜单项
 */
export function filterMenuByRoles(userRoles) {
  if (!userRoles || userRoles.length === 0) {
    return []
  }
  
  // 先过滤并处理菜单项
  const filteredItems = menuItems
    .filter(item => isMenuVisible(item, userRoles))
    .map(item => {
      // 如果有子菜单，递归过滤
      if (item.children && item.children.length > 0) {
        const filteredChildren = item.children
          .filter(child => isMenuVisible(child, userRoles))
          .map(child => {
            // 处理动态标题（根据角色显示不同标题）
            if (child.titleMap) {
              const matchedRole = userRoles.find(role => child.titleMap[role])
              if (matchedRole) {
                child.title = child.titleMap[matchedRole]
              }
            }
            return child
          })
        
        // 如果子菜单全部被过滤掉，则隐藏父菜单
        if (filteredChildren.length === 0) {
          return null
        }
        
        return {
          ...item,
          children: filteredChildren
        }
      }
      
      return item
    })
    .filter(item => item !== null)
  
  // 合并相同标题的菜单项（避免显示重复的"实习管理"等）
  const mergedItems = []
  const titleIndexMap = new Map()
  
  for (const item of filteredItems) {
    if (titleIndexMap.has(item.title)) {
      // 如果已存在相同标题的菜单项，合并子菜单
      const existingIndex = titleIndexMap.get(item.title)
      const existingItem = mergedItems[existingIndex]
      
      if (item.children && item.children.length > 0) {
        if (!existingItem.children) {
          existingItem.children = []
        }
        
        // 合并子菜单（去重，基于 index）
        const existingChildrenIndexes = new Set(
          existingItem.children.map(child => child.index)
        )
        const newChildren = item.children.filter(
          child => !existingChildrenIndexes.has(child.index)
        )
        existingItem.children.push(...newChildren)
      }
    } else {
      // 新菜单项，直接添加
      titleIndexMap.set(item.title, mergedItems.length)
      mergedItems.push({ ...item })
    }
  }
  
  return mergedItems
}

