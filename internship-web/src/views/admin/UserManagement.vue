<template>
  <PageLayout title="用户管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加用户
      </el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入真实姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.roleCodes"
            placeholder="请选择角色"
            clearable
            style="width: 200px"
            multiple
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="role in filteredRoleListForSearch"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学校">
          <el-select
            v-model="searchForm.schoolId"
            placeholder="请选择学校"
            clearable
            style="width: 200px"
            :disabled="isSchoolDisabled"
            @change="handleSchoolChange"
          >
            <el-option
              v-for="school in schoolList"
              :key="school.schoolId"
              :label="school.schoolName"
              :value="school.schoolId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属学院">
          <el-select
            v-model="searchForm.collegeId"
            placeholder="请选择学院"
            clearable
            style="width: 200px"
            :disabled="isCollegeDisabled || !searchForm.schoolId"
            @change="handleCollegeChange"
          >
            <el-option
              v-for="college in collegeList"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属专业">
          <el-select
            v-model="searchForm.majorId"
            placeholder="请选择专业"
            clearable
            style="width: 200px"
            :disabled="!searchForm.collegeId"
            @change="handleMajorChange"
          >
            <el-option
              v-for="major in majorList"
              :key="major.majorId"
              :label="major.majorName"
              :value="major.majorId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="所属班级">
          <el-select
            v-model="searchForm.classId"
            placeholder="请选择班级"
            clearable
            style="width: 200px"
            :disabled="!searchForm.majorId"
            :multiple="isClassMultiple"
          >
            <el-option
              v-for="classItem in filteredClassList"
              :key="classItem.classId"
              :label="classItem.className"
              :value="classItem.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-select
            v-model="searchForm.enrollmentYear"
            placeholder="请选择入学年份"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="year in enrollmentYearOptions"
              :key="year"
              :label="`${year}年`"
              :value="year"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="realName" label="真实姓名" min-width="120" />
      <el-table-column prop="phone" label="手机号" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="240" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="primary" 
            size="small" 
            :disabled="!canEditUser(row.roles || [])"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="warning" 
            size="small" 
            @click="handleResetPassword(row)"
          >
            重置密码
          </el-button>
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="danger" 
            size="small" 
            :disabled="!canDeleteMap[row.userId]"
            @click="handleDelete(row)"
          >
            停用
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formData.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="roleCode" v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])">
          <el-select
            v-model="formData.roleCode"
            placeholder="请选择角色"
            style="width: 100%"
            @change="handleRoleChange"
          >
            <el-option
              v-for="role in filteredRoleList"
              :key="role.roleCode"
              :label="role.roleName"
              :value="role.roleCode"
            />
          </el-select>
          <div v-if="isEdit && rowRoles.length > 0" style="margin-top: 8px; font-size: 12px; color: #909399;">
            当前角色：{{ rowRoles.map(r => getRoleName(r)).join('、') }}
          </div>
        </el-form-item>
        
        <!-- 学生角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_STUDENT'">
          <el-form-item label="学号" prop="studentNo">
            <el-input v-model="formData.studentNo" placeholder="请输入学号" />
          </el-form-item>
          <el-form-item label="所属学校" prop="schoolId">
            <el-select
              v-model="formData.schoolId"
              placeholder="请选择学校"
              style="width: 100%"
              :disabled="isSchoolDisabledForAdd"
              @change="handleSchoolChangeForAdd"
            >
              <el-option
                v-for="school in schoolList"
                :key="school.schoolId"
                :label="school.schoolName"
                :value="school.schoolId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属学院" prop="collegeId">
            <el-select
              v-model="formData.collegeId"
              placeholder="请选择学院"
              style="width: 100%"
              :disabled="isCollegeDisabledForAdd || !formData.schoolId"
              @change="handleCollegeChangeForAdd"
            >
              <el-option
                v-for="college in collegeListForAdd"
                :key="college.collegeId"
                :label="college.collegeName"
                :value="college.collegeId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属专业" prop="majorId">
            <el-select
              v-model="formData.majorId"
              placeholder="请选择专业"
              style="width: 100%"
              :disabled="!formData.collegeId"
              @change="handleMajorChangeForAdd"
            >
              <el-option
                v-for="major in majorListForAdd"
                :key="major.majorId"
                :label="major.majorName"
                :value="major.majorId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属班级" prop="classId">
            <el-select
              v-model="formData.classId"
              placeholder="请选择班级"
              style="width: 100%"
              :disabled="!formData.majorId"
            >
              <el-option
                v-for="classItem in filteredClassListForAdd"
                :key="classItem.classId"
                :label="classItem.className"
                :value="classItem.classId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="入学年份" prop="enrollmentYear">
            <el-select
              v-model="formData.enrollmentYear"
              placeholder="请选择入学年份"
              style="width: 100%"
            >
              <el-option
                v-for="year in enrollmentYearOptions"
                :key="year"
                :label="`${year}年`"
                :value="year"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 学校管理员角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_SCHOOL_ADMIN'">
          <el-form-item label="所属学校" prop="schoolId">
            <el-select
              v-model="formData.schoolId"
              placeholder="请选择学校"
              style="width: 100%"
              :disabled="isSchoolDisabledForAdd"
              @change="handleSchoolChangeForAdd"
            >
              <el-option
                v-for="school in schoolList"
                :key="school.schoolId"
                :label="school.schoolName"
                :value="school.schoolId"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 学院负责人角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_COLLEGE_LEADER'">
          <el-form-item label="所属学校" prop="schoolId">
            <el-select
              v-model="formData.schoolId"
              placeholder="请选择学校"
              style="width: 100%"
              :disabled="isSchoolDisabledForAdd"
              @change="handleSchoolChangeForAdd"
            >
              <el-option
                v-for="school in schoolList"
                :key="school.schoolId"
                :label="school.schoolName"
                :value="school.schoolId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属学院" prop="collegeId">
            <el-select
              v-model="formData.collegeId"
              placeholder="请选择学院"
              style="width: 100%"
              :disabled="isCollegeDisabledForAdd || !formData.schoolId"
              @change="handleCollegeChangeForAdd"
            >
              <el-option
                v-for="college in collegeListForAdd"
                :key="college.collegeId"
                :label="college.collegeName"
                :value="college.collegeId"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 班主任角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_CLASS_TEACHER'">
          <el-form-item label="所属学校" prop="schoolId">
            <el-select
              v-model="formData.schoolId"
              placeholder="请选择学校"
              style="width: 100%"
              :disabled="isSchoolDisabledForAdd"
              @change="handleSchoolChangeForAdd"
            >
              <el-option
                v-for="school in schoolList"
                :key="school.schoolId"
                :label="school.schoolName"
                :value="school.schoolId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="所属学院" prop="collegeId">
            <el-select
              v-model="formData.collegeId"
              placeholder="请选择学院"
              style="width: 100%"
              :disabled="isCollegeDisabledForAdd || !formData.schoolId"
              @change="handleCollegeChangeForAdd"
            >
              <el-option
                v-for="college in collegeListForAdd"
                :key="college.collegeId"
                :label="college.collegeName"
                :value="college.collegeId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="管理班级" prop="classIds">
            <el-select
              v-model="formData.classIds"
              placeholder="请选择管理的班级（可多选）"
              style="width: 100%"
              multiple
              collapse-tags
              collapse-tags-tooltip
              :disabled="!formData.collegeId"
            >
              <el-option
                v-for="classItem in classListForAdd"
                :key="classItem.classId"
                :label="classItem.className"
                :value="classItem.classId"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 企业管理员角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_ENTERPRISE_ADMIN'">
          <el-form-item label="所属企业" prop="enterpriseId">
            <el-select
              v-model="formData.enterpriseId"
              placeholder="请选择企业"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="enterprise in enterpriseList"
                :key="enterprise.enterpriseId"
                :label="enterprise.enterpriseName"
                :value="enterprise.enterpriseId"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 企业导师角色字段 -->
        <template v-if="formData.roleCode === 'ROLE_ENTERPRISE_MENTOR'">
          <el-form-item label="所属企业" prop="enterpriseId">
            <el-select
              v-model="formData.enterpriseId"
              placeholder="请选择企业"
              style="width: 100%"
              filterable
            >
              <el-option
                v-for="enterprise in enterpriseList"
                :key="enterprise.enterpriseId"
                :label="enterprise.enterpriseName"
                :value="enterprise.enterpriseId"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole, canEditUser, canAssignRole } from '@/utils/permission'
import { useAuthStore } from '@/store/modules/auth'
import { userApi } from '@/api/user/user'
import { roleApi } from '@/api/user/role'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { majorApi } from '@/api/system/major'
import { classApi } from '@/api/system/class'
import { studentApi } from '@/api/user/student'
import { enterpriseApi } from '@/api/user/enterprise'
import request from '@/utils/request'

// 搜索表单
const searchForm = reactive({
  username: '',
  realName: '',
  phone: '',
  status: null,
  roleCodes: null,
  schoolId: null,
  collegeId: null,
  majorId: null,
  classId: null,
  enrollmentYear: null
})

// 筛选选项
const roleList = ref([])
const schoolList = ref([])
const collegeList = ref([])
const majorList = ref([])
const classList = ref([])

// 添加/编辑表单用的组织架构列表
const collegeListForAdd = ref([])
const majorListForAdd = ref([])
const classListForAdd = ref([])

// 企业列表（用于企业管理员和企业导师选择）
const enterpriseList = ref([])

// 入学年份选项（动态加载）
const enrollmentYearOptions = ref([])

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: '',
  collegeId: null,
  collegeName: '',
  classIds: [],
  classNames: []
})

// 表格数据
const tableData = ref([])
const loading = ref(false)
// 用户是否可以停用的映射表
const canDeleteMap = ref({})
// 编辑时保存的原始角色（用于显示）
const rowRoles = ref([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  idCard: '',
  phone: '',
  email: '',
  status: 1,
  roleCode: null,  // 改为单选，使用roleCode
  // 学生相关字段
  studentNo: '',
  schoolId: null,
  collegeId: null,
  majorId: null,
  classId: null,
  enrollmentYear: null,
  // 班主任相关字段
  classIds: [],  // 班主任可以管理多个班级
  // 企业相关字段
  enterpriseId: null  // 企业管理员和企业导师需要关联企业
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  roleCode: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  // 学生相关验证
  studentNo: [
    { required: true, message: '请输入学号', trigger: 'blur', validator: (rule, value, callback) => {
      if (formData.roleCode === 'ROLE_STUDENT' && !value) {
        callback(new Error('请输入学号'))
      } else {
        callback()
      }
    }}
  ],
  schoolId: [
    { required: true, message: '请选择学校', trigger: 'change', validator: (rule, value, callback) => {
      if (['ROLE_STUDENT', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'].includes(formData.roleCode) && !value) {
        callback(new Error('请选择学校'))
      } else {
        callback()
      }
    }}
  ],
  collegeId: [
    { required: true, message: '请选择学院', trigger: 'change', validator: (rule, value, callback) => {
      if (['ROLE_STUDENT', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'].includes(formData.roleCode) && !value) {
        callback(new Error('请选择学院'))
      } else {
        callback()
      }
    }}
  ],
  majorId: [
    { required: true, message: '请选择专业', trigger: 'change', validator: (rule, value, callback) => {
      if (formData.roleCode === 'ROLE_STUDENT' && !value) {
        callback(new Error('请选择专业'))
      } else {
        callback()
      }
    }}
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change', validator: (rule, value, callback) => {
      if (formData.roleCode === 'ROLE_STUDENT' && !value) {
        callback(new Error('请选择班级'))
      } else {
        callback()
      }
    }}
  ],
  enrollmentYear: [
    { required: true, message: '请选择入学年份', trigger: 'change', validator: (rule, value, callback) => {
      if (formData.roleCode === 'ROLE_STUDENT' && !value) {
        callback(new Error('请选择入学年份'))
      } else {
        callback()
      }
    }}
  ],
  enterpriseId: [
    { required: true, message: '请选择企业', trigger: 'change', validator: (rule, value, callback) => {
      if (['ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR'].includes(formData.roleCode) && !value) {
        callback(new Error('请选择企业'))
      } else {
        callback()
      }
    }}
  ]
}

// 重置密码对话框
const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

// 重置密码验证规则
const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性：学校下拉框是否禁用
const isSchoolDisabled = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
})

// 计算属性：学院下拉框是否禁用
const isCollegeDisabled = computed(() => {
  return hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
})

// 计算属性：班级下拉框是否支持多选（班主任支持多选）
const isClassMultiple = computed(() => {
  return hasAnyRole(['ROLE_CLASS_TEACHER'])
})

// 计算属性：学校字段是否禁用（添加时）
const isSchoolDisabledForAdd = computed(() => {
  if (!formData.roleCode) return false
  const authStore = useAuthStore()
  const currentRoles = authStore.roles || []
  
  // 系统管理员：不禁用，可以自由选择
  if (currentRoles.includes('ROLE_SYSTEM_ADMIN')) {
    return false
  }
  
  // 学校管理员添加学校管理员、学院负责人、班主任：禁用（绑定自己的学校）
  if (currentRoles.includes('ROLE_SCHOOL_ADMIN') && 
      ['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(formData.roleCode)) {
    return true
  }
  
  // 学院负责人添加学院负责人、班主任、学生：禁用（绑定自己的学校）
  if (currentRoles.includes('ROLE_COLLEGE_LEADER') && 
      ['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(formData.roleCode)) {
    return true
  }
  
  // 班主任添加学生：禁用（绑定自己的学校）
  if (currentRoles.includes('ROLE_CLASS_TEACHER') && 
      formData.roleCode === 'ROLE_STUDENT') {
    return true
  }
  
  return false
})

// 计算属性：学院字段是否禁用（添加时）
const isCollegeDisabledForAdd = computed(() => {
  if (!formData.roleCode) return false
  const authStore = useAuthStore()
  const currentRoles = authStore.roles || []
  
  // 系统管理员：不禁用，可以自由选择
  if (currentRoles.includes('ROLE_SYSTEM_ADMIN')) {
    return false
  }
  
  // 学校管理员添加学院负责人、班主任、学生：不禁用，可以选择学院
  if (currentRoles.includes('ROLE_SCHOOL_ADMIN') && 
      ['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(formData.roleCode)) {
    return false
  }
  
  // 学院负责人添加学院负责人、班主任、学生：禁用（绑定自己的学院）
  if (currentRoles.includes('ROLE_COLLEGE_LEADER') && 
      ['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(formData.roleCode)) {
    return true
  }
  
  // 班主任添加学生：禁用（绑定自己的学院）
  if (currentRoles.includes('ROLE_CLASS_TEACHER') && 
      formData.roleCode === 'ROLE_STUDENT') {
    return true
  }
  
  return false
})

// 计算属性：过滤后的班级列表（班主任只能看到管理的班级）
const filteredClassList = computed(() => {
  if (hasAnyRole(['ROLE_CLASS_TEACHER']) && currentOrgInfo.value.classIds && currentOrgInfo.value.classIds.length > 0) {
    return classList.value.filter(c => currentOrgInfo.value.classIds.includes(c.classId))
  }
  return classList.value.filter(c => !searchForm.majorId || c.majorId === searchForm.majorId)
})

// 计算属性：添加表单中过滤后的班级列表（班主任只能选择管理的班级）
const filteredClassListForAdd = computed(() => {
  if (hasAnyRole(['ROLE_CLASS_TEACHER']) && currentOrgInfo.value.classIds && currentOrgInfo.value.classIds.length > 0) {
    // 班主任只能选择自己管理的班级
    return classListForAdd.value.filter(c => currentOrgInfo.value.classIds.includes(c.classId))
  }
  // 其他角色可以选择所有班级
  return classListForAdd.value
})

// 加载入学年份（方案A：前端兜底）
const loadEnrollmentYears = async () => {
  try {
    const res = await studentApi.getEnrollmentYears()
    const currentYear = new Date().getFullYear()
    
    if (res.code === 200) {
      let years = res.data || []
      
      // 如果数据库中没有年份，或者不包含当前年份，添加默认年份
      if (years.length === 0 || !years.includes(currentYear)) {
        const defaultYears = [currentYear, currentYear - 1, currentYear - 2]
        // 合并并去重
        years = [...new Set([...defaultYears, ...years])].sort((a, b) => b - a)
      } else {
        // 降序排列
        years = years.sort((a, b) => b - a)
      }
      
      enrollmentYearOptions.value = years
    }
  } catch (error) {
    console.error('加载入学年份失败:', error)
    // 失败时使用固定范围
    const currentYear = new Date().getFullYear()
    enrollmentYearOptions.value = []
    for (let i = currentYear; i >= currentYear - 10; i--) {
      enrollmentYearOptions.value.push(i)
    }
  }
}

// 计算属性：用于筛选的角色列表（排除不能筛选的角色）
const filteredRoleListForSearch = computed(() => {
  const authStore = useAuthStore()
  const currentRoles = authStore.roles || []
  if (!currentRoles || currentRoles.length === 0) {
    return []
  }
  
  // 系统管理员：可以筛选所有角色
  if (currentRoles.includes('ROLE_SYSTEM_ADMIN')) {
    return roleList.value
  }
  
  // 学校管理员：不能筛选系统管理员
  if (currentRoles.includes('ROLE_SCHOOL_ADMIN')) {
    return roleList.value.filter(role => role.roleCode !== 'ROLE_SYSTEM_ADMIN')
  }
  
  // 学院负责人：不能筛选系统管理员、学校管理员
  if (currentRoles.includes('ROLE_COLLEGE_LEADER')) {
    return roleList.value.filter(role => 
      role.roleCode !== 'ROLE_SYSTEM_ADMIN' && 
      role.roleCode !== 'ROLE_SCHOOL_ADMIN'
    )
  }
  
  // 班主任：只能添加学生角色
  if (currentRoles.includes('ROLE_CLASS_TEACHER')) {
    return roleList.value.filter(role => role.roleCode === 'ROLE_STUDENT')
  }
  
  // 其他角色：不能筛选任何角色
  return []
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      username: searchForm.username || undefined,
      realName: searchForm.realName || undefined,
      phone: searchForm.phone || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      roleCodes: searchForm.roleCodes && searchForm.roleCodes.length > 0 ? searchForm.roleCodes.join(',') : undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      majorId: searchForm.majorId || undefined,
      classId: searchForm.classId || undefined,
      enrollmentYear: searchForm.enrollmentYear || undefined
    }
    const res = await userApi.getUserPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records
      pagination.total = res.data.total
      
      // 为每个用户加载角色信息
      await loadUserRoles()
      
      // 检查每个用户是否可以停用
      await checkCanDeleteUsers()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 加载用户角色信息（现在角色信息已经包含在返回的数据中，此方法保留用于兼容）
const loadUserRoles = async () => {
  // 角色信息已经包含在用户数据中，不需要额外请求
  // 如果某些用户的角色信息缺失，可以在这里补充
  tableData.value.forEach(user => {
    if (!user.roles) {
      user.roles = []
    }
  })
}

// 检查用户是否可以停用
const checkCanDeleteUsers = async () => {
  canDeleteMap.value = {}
  // 批量检查，使用 Promise.all 并行请求
  const checkPromises = tableData.value.map(async (user) => {
    try {
      const res = await userApi.canDeleteUser(user.userId)
      return {
        userId: user.userId,
        canDelete: res.code === 200 ? res.data : false
      }
    } catch (error) {
      console.error(`检查用户 ${user.userId} 是否可以停用失败:`, error)
      return {
        userId: user.userId,
        canDelete: false
      }
    }
  })
  
  const results = await Promise.all(checkPromises)
  results.forEach(result => {
    canDeleteMap.value[result.userId] = result.canDelete
  })
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.phone = ''
  searchForm.status = null
  searchForm.roleCodes = null
  
  // 根据角色重置筛选条件，保持组织信息的绑定
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
    // 学校管理员、学院负责人、班主任：保持学校ID
    searchForm.schoolId = currentOrgInfo.value.schoolId || null
  } else {
    searchForm.schoolId = null
  }
  
  if (hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
    // 学院负责人、班主任：保持学院ID
    searchForm.collegeId = currentOrgInfo.value.collegeId || null
  } else {
    searchForm.collegeId = null
  }
  
  searchForm.majorId = null
  searchForm.classId = null
  searchForm.enrollmentYear = null
  
  // 重新加载学院、专业和班级列表
  if (searchForm.schoolId) {
    loadCollegeList(searchForm.schoolId)
  } else {
    collegeList.value = []
    majorList.value = []
    classList.value = []
  }
  
  if (searchForm.collegeId) {
    loadMajorList(searchForm.collegeId)
  } else {
    majorList.value = []
    classList.value = []
  }
  
  handleSearch()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadData()
}

// 添加
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加用户'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑用户'
  try {
    const res = await userApi.getUserById(row.userId)
    if (res.code === 200) {
      const roles = res.data.roles || []
      Object.assign(formData, {
        userId: res.data.userId,
        username: res.data.username,
        password: '',
        realName: res.data.realName || '',
        idCard: res.data.idCard || '',
        phone: res.data.phone || '',
        email: res.data.email || '',
        status: res.data.status,
        roleCode: roles.length > 0 ? roles[0] : null,  // 取第一个角色
        // 学生相关字段（需要从学生信息中获取）
        studentNo: '',
        schoolId: null,
        collegeId: null,
        majorId: null,
        classId: null,
        enrollmentYear: null,
        // 班主任相关字段
        classIds: [],
        // 企业相关字段
        enterpriseId: null
      })
      rowRoles.value = roles  // 保存原始角色用于显示
      
      // 如果用户是企业管理员，加载企业信息
      if (roles.includes('ROLE_ENTERPRISE_ADMIN')) {
        try {
          const enterpriseRes = await enterpriseApi.getEnterpriseByUserId(row.userId)
          if (enterpriseRes.code === 200 && enterpriseRes.data) {
            formData.enterpriseId = enterpriseRes.data.enterpriseId || null
          }
        } catch (error) {
          console.error('获取企业信息失败:', error)
        }
      }
      
      // 如果用户是企业导师，加载企业导师信息
      if (roles.includes('ROLE_ENTERPRISE_MENTOR')) {
        try {
          // 需要调用企业导师API获取信息
          const mentorRes = await request.get(`/user/enterprise-mentor/user/${row.userId}`)
          if (mentorRes.code === 200 && mentorRes.data) {
            formData.enterpriseId = mentorRes.data.enterpriseId || null
          }
        } catch (error) {
          console.error('获取企业导师信息失败:', error)
        }
      }
      
      // 如果用户是学生，加载学生信息
      if (roles.includes('ROLE_STUDENT')) {
        try {
          const studentRes = await studentApi.getStudentByUserId(row.userId)
          if (studentRes.code === 200 && studentRes.data) {
            formData.studentNo = studentRes.data.studentNo || ''
            formData.schoolId = studentRes.data.schoolId || null
            formData.collegeId = studentRes.data.collegeId || null
            formData.majorId = studentRes.data.majorId || null
            formData.classId = studentRes.data.classId || null
            formData.enrollmentYear = studentRes.data.enrollmentYear || null
            
            // 加载组织架构列表
            if (formData.schoolId) {
              await loadCollegeListForAdd(formData.schoolId)
            }
            if (formData.collegeId) {
              await loadMajorListForAdd(formData.collegeId)
              await loadClassListForAdd(null, null, null, formData.collegeId)
            }
            if (formData.majorId) {
              await loadClassListForAdd(null, null, formData.majorId, null)
            }
          }
        } catch (error) {
          console.error('获取学生信息失败:', error)
        }
      }
      
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    userId: null,
    username: '',
    password: '',
    realName: '',
    idCard: '',
    phone: '',
    email: '',
    status: 1,
    roleCode: null,
    // 学生相关字段
    studentNo: '',
    schoolId: null,
    collegeId: null,
    majorId: null,
    classId: null,
    enrollmentYear: null,
    // 班主任相关字段
    classIds: [],
    // 企业相关字段
    enterpriseId: null
  })
  rowRoles.value = []
  collegeListForAdd.value = []
  majorListForAdd.value = []
  classListForAdd.value = []
  formRef.value?.clearValidate()
}

// 角色改变时的处理
const handleRoleChange = (roleCode) => {
  // 清空角色相关的字段
  formData.studentNo = ''
  formData.schoolId = null
  formData.collegeId = null
  formData.majorId = null
  formData.classId = null
  formData.classIds = []
  formData.enrollmentYear = null
  formData.enterpriseId = null
  collegeListForAdd.value = []
  majorListForAdd.value = []
  classListForAdd.value = []
  
  const authStore = useAuthStore()
  const currentRoles = authStore.roles || []
  
  // 系统管理员：不设置默认值，可以自由选择
  if (currentRoles.includes('ROLE_SYSTEM_ADMIN')) {
    // 根据角色加载相应的列表
    if (roleCode === 'ROLE_COLLEGE_LEADER' || roleCode === 'ROLE_CLASS_TEACHER' || roleCode === 'ROLE_STUDENT') {
      // 需要选择学院的角色，加载所有学校列表（如果还没有加载）
      if (schoolList.value.length === 0) {
        loadSchoolList()
      }
    }
    if (roleCode === 'ROLE_ENTERPRISE_ADMIN' || roleCode === 'ROLE_ENTERPRISE_MENTOR') {
      loadEnterpriseList()
    }
    return
  }
  
  // 学校管理员：添加学校管理员、学院负责人、班主任、学生时，学校默认绑定
  if (currentRoles.includes('ROLE_SCHOOL_ADMIN')) {
    if (['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(roleCode)) {
      if (currentOrgInfo.value.schoolId) {
        formData.schoolId = currentOrgInfo.value.schoolId
        // 需要选择学院的角色，加载该学校的学院列表
        if (['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(roleCode)) {
          loadCollegeListForAdd(currentOrgInfo.value.schoolId)
        }
      }
    }
  }
  
  // 学院负责人：添加学院负责人、班主任、学生时，学校和学院都默认绑定
  if (currentRoles.includes('ROLE_COLLEGE_LEADER')) {
    if (['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT'].includes(roleCode)) {
      if (currentOrgInfo.value.schoolId) {
        formData.schoolId = currentOrgInfo.value.schoolId
        loadCollegeListForAdd(currentOrgInfo.value.schoolId)
      }
      if (currentOrgInfo.value.collegeId) {
        formData.collegeId = currentOrgInfo.value.collegeId
        // 需要选择专业/班级的角色，加载相应的列表
        if (roleCode === 'ROLE_STUDENT') {
          loadMajorListForAdd(currentOrgInfo.value.collegeId)
        } else if (roleCode === 'ROLE_CLASS_TEACHER') {
          loadClassListForAdd(null, null, null, currentOrgInfo.value.collegeId)
        }
      }
    }
  }
  
  // 班主任：只能添加学生，学校和学院都默认绑定，班级只能选择自己管理的班级
  if (currentRoles.includes('ROLE_CLASS_TEACHER')) {
    if (roleCode === 'ROLE_STUDENT') {
      if (currentOrgInfo.value.schoolId) {
        formData.schoolId = currentOrgInfo.value.schoolId
        loadCollegeListForAdd(currentOrgInfo.value.schoolId)
      }
      if (currentOrgInfo.value.collegeId) {
        formData.collegeId = currentOrgInfo.value.collegeId
        // 加载专业列表
        loadMajorListForAdd(currentOrgInfo.value.collegeId)
        // 加载班主任管理的班级列表
        if (currentOrgInfo.value.classIds && currentOrgInfo.value.classIds.length > 0) {
          loadClassListForAdd(null, null, null, currentOrgInfo.value.collegeId)
        }
      }
    }
  }
  
  // 企业管理员和企业导师：加载企业列表
  if (roleCode === 'ROLE_ENTERPRISE_ADMIN' || roleCode === 'ROLE_ENTERPRISE_MENTOR') {
    loadEnterpriseList()
  }
}

// 加载添加表单用的学院列表
const loadCollegeListForAdd = async (schoolId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await collegeApi.getCollegePage(params)
    if (res.code === 200) {
      collegeListForAdd.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

// 加载添加表单用的专业列表
const loadMajorListForAdd = async (collegeId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (collegeId) {
      params.collegeId = collegeId
    }
    const res = await majorApi.getMajorPage(params)
    if (res.code === 200) {
      majorListForAdd.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

// 加载添加表单用的班级列表
const loadClassListForAdd = async (collegeId, schoolId, majorId, collegeIdParam) => {
  try {
    const params = { current: 1, size: 1000 }
    // 优先级：majorId > collegeId > schoolId
    if (majorId) {
      params.majorId = majorId
    } else if (collegeIdParam || collegeId) {
      params.collegeId = collegeIdParam || collegeId
    } else if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await classApi.getClassPage(params)
    if (res.code === 200) {
      classListForAdd.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 学校改变时的处理（添加表单）
const handleSchoolChangeForAdd = () => {
  formData.collegeId = null
  formData.majorId = null
  formData.classId = null
  formData.classIds = []
  majorListForAdd.value = []
  classListForAdd.value = []
  if (formData.schoolId) {
    loadCollegeListForAdd(formData.schoolId)
  } else {
    collegeListForAdd.value = []
  }
}

// 学院改变时的处理（添加表单）
const handleCollegeChangeForAdd = () => {
  formData.majorId = null
  formData.classId = null
  formData.classIds = []
  classListForAdd.value = []
  if (formData.collegeId) {
    // 学生角色需要加载专业列表
    if (formData.roleCode === 'ROLE_STUDENT') {
      loadMajorListForAdd(formData.collegeId)
    }
    // 班主任角色需要加载班级列表
    if (formData.roleCode === 'ROLE_CLASS_TEACHER') {
      loadClassListForAdd(null, null, null, formData.collegeId)
    }
  } else {
    majorListForAdd.value = []
    classListForAdd.value = []
  }
}

// 专业改变时的处理（添加表单）
const handleMajorChangeForAdd = () => {
  formData.classId = null
  formData.classIds = []
  if (formData.majorId) {
    loadClassListForAdd(null, null, formData.majorId, null)
  } else if (formData.collegeId) {
    loadClassListForAdd(null, null, null, formData.collegeId)
  } else {
    classListForAdd.value = []
  }
}

// 加载企业列表
const loadEnterpriseList = async () => {
  try {
    const res = await enterpriseApi.getEnterprisePage({ current: 1, size: 1000, status: 1 })
    if (res.code === 200) {
      enterpriseList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载企业列表失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const userData = {
        username: formData.username,
        password: formData.password,
        realName: formData.realName,
        idCard: formData.idCard,
        phone: formData.phone,
        email: formData.email,
        status: formData.status,
        roles: formData.roleCode ? [formData.roleCode] : []  // 转换为数组格式
      }
      
      // 根据角色添加额外字段
      if (formData.roleCode === 'ROLE_STUDENT') {
        userData.studentNo = formData.studentNo
        userData.schoolId = formData.schoolId
        userData.collegeId = formData.collegeId
        userData.majorId = formData.majorId
        userData.classId = formData.classId
        userData.enrollmentYear = formData.enrollmentYear
      } else if (formData.roleCode === 'ROLE_SCHOOL_ADMIN') {
        userData.schoolId = formData.schoolId
      } else if (formData.roleCode === 'ROLE_COLLEGE_LEADER') {
        userData.schoolId = formData.schoolId
        userData.collegeId = formData.collegeId
      } else if (formData.roleCode === 'ROLE_CLASS_TEACHER') {
        userData.schoolId = formData.schoolId
        userData.collegeId = formData.collegeId
        userData.classIds = formData.classIds
      } else if (formData.roleCode === 'ROLE_ENTERPRISE_ADMIN') {
        userData.enterpriseId = formData.enterpriseId
      } else if (formData.roleCode === 'ROLE_ENTERPRISE_MENTOR') {
        userData.enterpriseId = formData.enterpriseId
      }
      
      if (isEdit.value) {
        // 编辑时不需要密码
        const { password, ...updateData } = userData
        updateData.userId = formData.userId
        const res = await userApi.updateUser(updateData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        // 添加时需要密码
        const res = await userApi.addUser(userData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadData()
          // 重新加载入学年份列表（可能新增了年份）
          await loadEnrollmentYears()
        } else {
          ElMessage.error(res.message || '添加失败')
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
      const errorMessage = error.response?.data?.message || error.message || '操作失败'
      ElMessage.error(errorMessage)
    } finally {
      submitting.value = false
    }
  })
}

// 重置密码
const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.userId
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const res = await userApi.resetPassword(
        resetPasswordForm.userId,
        resetPasswordForm.newPassword
      )
      if (res.code === 200) {
        ElMessage.success('密码重置成功')
        resetPasswordDialogVisible.value = false
      }
    } catch (error) {
      console.error('重置密码失败:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 批量导入
const router = useRouter()
const handleImport = () => {
  router.push('/admin/student/import')
}

// 删除
const handleDelete = (row) => {
  // 再次检查是否可以停用（防止状态变化）
  if (!canDeleteMap.value[row.userId]) {
    ElMessage.warning('该用户不能停用，系统至少需要保留一个启用的管理员')
    return
  }
  
  ElMessageBox.confirm(
    `确定要停用用户 "${row.username}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await userApi.deleteUser(row.userId)
      if (res.code === 200) {
        ElMessage.success('停用成功')
        loadData()
      }
    } catch (error) {
      // 显示后端返回的错误信息
      const errorMessage = error.response?.data?.message || error.message || '停用失败'
      ElMessage.error(errorMessage)
      // 重新检查是否可以停用
      await checkCanDeleteUsers()
    }
  }).catch(() => {})
}

const loadSchoolList = async () => {
  try {
    const res = await schoolApi.getSchoolPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      schoolList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学校列表失败:', error)
  }
}

const loadCollegeList = async (schoolId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await collegeApi.getCollegePage(params)
    if (res.code === 200) {
      collegeList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

const loadMajorList = async (collegeId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (collegeId) {
      params.collegeId = collegeId
    }
    const res = await majorApi.getMajorPage(params)
    if (res.code === 200) {
      majorList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

const loadClassList = async (collegeId, schoolId, majorId) => {
  try {
    const params = { current: 1, size: 1000 }
    // 优先级：majorId > collegeId > schoolId
    if (majorId) {
      params.majorId = majorId
    } else if (collegeId) {
      params.collegeId = collegeId
    } else if (schoolId) {
      params.schoolId = schoolId
    }
    const res = await classApi.getClassPage(params)
    if (res.code === 200) {
      classList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 学校改变时的处理
const handleSchoolChange = () => {
  // 清空学院、专业和班级选择
  searchForm.collegeId = null
  searchForm.majorId = null
  searchForm.classId = null
  majorList.value = []
  classList.value = []
  // 加载该学校下的学院列表
  if (searchForm.schoolId) {
    loadCollegeList(searchForm.schoolId)
  } else {
    collegeList.value = []
  }
}

// 学院改变时的处理
const handleCollegeChange = () => {
  // 清空专业和班级选择
  searchForm.majorId = null
  searchForm.classId = null
  classList.value = []
  // 加载该学院下的专业列表
  if (searchForm.collegeId) {
    loadMajorList(searchForm.collegeId)
  } else {
    majorList.value = []
  }
}

// 专业改变时的处理
const handleMajorChange = () => {
  // 清空班级选择
  searchForm.classId = null
  // 加载该专业下的班级列表
  if (searchForm.majorId) {
    loadClassList(null, null, searchForm.majorId)
  } else if (searchForm.collegeId) {
    loadClassList(searchForm.collegeId, null, null)
  } else if (searchForm.schoolId && hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
    // 学校管理员：即使没有选择学院，也可以根据学校加载班级
    loadClassList(null, searchForm.schoolId, null)
  } else {
    classList.value = []
  }
}

// 加载角色列表
const loadRoleList = async () => {
  try {
    const res = await roleApi.getAllEnabledRoles()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 过滤后的角色列表（根据当前用户权限）
const filteredRoleList = computed(() => {
  return roleList.value.filter(role => {
    // 系统管理员不能分配系统管理员角色
    if (role.roleCode === 'ROLE_SYSTEM_ADMIN') {
      return false
    }
    return canAssignRole(role.roleCode)
  })
})

// 获取角色名称
const getRoleName = (roleCode) => {
  const role = roleList.value.find(r => r.roleCode === roleCode)
  return role ? role.roleName : roleCode
}

// 初始化
// 加载当前用户组织信息
const loadCurrentUserOrgInfo = async () => {
  try {
    const res = await userApi.getCurrentUserOrgInfo()
    if (res.code === 200 && res.data) {
      currentOrgInfo.value = {
        schoolId: res.data.schoolId || null,
        schoolName: res.data.schoolName || '',
        collegeId: res.data.collegeId || null,
        collegeName: res.data.collegeName || '',
        classIds: res.data.classIds || [],
        classNames: res.data.classNames || []
      }
      
      // 根据角色设置筛选框默认值
      if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
        if (currentOrgInfo.value.schoolId) {
          searchForm.schoolId = currentOrgInfo.value.schoolId
          // 加载该学校的学院列表
          await loadCollegeList(currentOrgInfo.value.schoolId)
        }
      }
      
      if (hasAnyRole(['ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])) {
        if (currentOrgInfo.value.collegeId) {
          searchForm.collegeId = currentOrgInfo.value.collegeId
          // 加载该学院的专业列表
          await loadMajorList(currentOrgInfo.value.collegeId)
        }
      } else if (hasAnyRole(['ROLE_SCHOOL_ADMIN'])) {
        // 学校管理员：加载该学校的班级列表
        if (currentOrgInfo.value.schoolId) {
          await loadClassList(null, currentOrgInfo.value.schoolId, null)
        }
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

onMounted(async () => {
  loadRoleList()
  loadSchoolList()
  await loadEnrollmentYears()  // 加载入学年份
  await loadEnterpriseList()  // 加载企业列表
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有学院和班级（仅系统管理员）
  if (!currentOrgInfo.value.schoolId && hasAnyRole(['ROLE_SYSTEM_ADMIN'])) {
    loadCollegeList(null)
  }
  if (!currentOrgInfo.value.collegeId && !currentOrgInfo.value.schoolId && hasAnyRole(['ROLE_SYSTEM_ADMIN'])) {
    loadClassList(null, null, null)
  }
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-form {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

