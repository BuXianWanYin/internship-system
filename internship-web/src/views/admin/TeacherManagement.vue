<template>
  <PageLayout title="教师管理">
    <template #actions>
      <el-button 
        v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
        type="primary" 
        :icon="Plus" 
        @click="handleAdd"
      >
        添加教师
      </el-button>
    </template>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="教师列表" name="list">
        <!-- 搜索栏 -->
        <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工号">
          <el-input
            v-model="searchForm.teacherNo"
            placeholder="请输入工号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
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
          >
            <el-option
              v-for="college in collegeList"
              :key="college.collegeId"
              :label="college.collegeName"
              :value="college.collegeId"
            />
          </el-select>
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
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button 
            type="success" 
            :icon="Download" 
            @click="handleExport"
            :loading="exportLoading"
          >
            导出Excel
          </el-button>
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
      <el-table-column prop="teacherNo" label="工号" min-width="120" />
      <el-table-column label="教师信息" min-width="150">
        <template #default="{ row }">
          <div v-if="userInfoMap[row.userId]">
            <div>{{ userInfoMap[row.userId].realName }}</div>
          </div>
          <span v-else>加载中...</span>
        </template>
      </el-table-column>
      <el-table-column label="性别" min-width="80" align="center">
        <template #default="{ row }">
          <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].gender">
            {{ userInfoMap[row.userId].gender }}
          </span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="手机号" min-width="120">
        <template #default="{ row }">
          <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].phone">
            {{ userInfoMap[row.userId].phone }}
          </span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="邮箱" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].email">
            {{ userInfoMap[row.userId].email }}
          </span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="职称" min-width="100">
        <template #default="{ row }">
          <span v-if="row.title">{{ row.title }}</span>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学院" min-width="150">
        <template #default="{ row }">
          <span v-if="collegeMap[row.collegeId]">{{ collegeMap[row.collegeId].collegeName }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="所属学校" min-width="150">
        <template #default="{ row }">
          <span v-if="schoolMap[row.schoolId]">{{ schoolMap[row.schoolId].schoolName }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" min-width="100" align="center">
        <template #default="{ row }">
          <el-tag 
            :type="getAuditStatusType(row.auditStatus)" 
            size="small"
          >
            {{ getAuditStatusText(row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" min-width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button 
            v-if="hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])" 
            link 
            type="primary" 
            size="small" 
            :disabled="!canEditTeacher(row)"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button 
            v-if="canDeleteTeacherBtn" 
            link 
            type="danger" 
            size="small" 
            :disabled="!canDeleteTeacher(row)"
            @click="handleDelete(row)"
          >
            删除
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
      </el-tab-pane>

      <el-tab-pane label="待审核" name="approval">
        <!-- 待审核搜索栏 -->
        <div class="search-bar">
          <el-form :inline="true" :model="approvalSearchForm" class="search-form">
            <el-form-item label="工号">
              <el-input
                v-model="approvalSearchForm.teacherNo"
                placeholder="请输入工号"
                clearable
                style="width: 200px"
                @keyup.enter="handleApprovalSearch"
              />
            </el-form-item>
            <el-form-item label="姓名">
              <el-input
                v-model="approvalSearchForm.realName"
                placeholder="请输入姓名"
                clearable
                style="width: 200px"
                @keyup.enter="handleApprovalSearch"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleApprovalSearch">查询</el-button>
              <el-button :icon="Refresh" @click="handleApprovalReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 待审核表格 -->
        <el-table
          v-loading="approvalLoading"
          :data="approvalTableData"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
          <el-table-column prop="teacherNo" label="工号" min-width="120" />
          <el-table-column label="教师信息" min-width="150">
            <template #default="{ row }">
              <div v-if="userInfoMap[row.userId]">
                <div>{{ userInfoMap[row.userId].realName }}</div>
              </div>
              <span v-else>加载中...</span>
            </template>
          </el-table-column>
          <el-table-column label="性别" min-width="80" align="center">
            <template #default="{ row }">
              <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].gender">
                {{ userInfoMap[row.userId].gender }}
              </span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="手机号" min-width="120">
            <template #default="{ row }">
              <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].phone">
                {{ userInfoMap[row.userId].phone }}
              </span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="邮箱" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="userInfoMap[row.userId] && userInfoMap[row.userId].email">
                {{ userInfoMap[row.userId].email }}
              </span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="职称" min-width="100">
            <template #default="{ row }">
              <span v-if="row.title">{{ row.title }}</span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column label="所属学院" min-width="150">
            <template #default="{ row }">
              <span v-if="collegeMap[row.collegeId]">{{ collegeMap[row.collegeId].collegeName }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="所属学校" min-width="150">
            <template #default="{ row }">
              <span v-if="schoolMap[row.schoolId]">{{ schoolMap[row.schoolId].schoolName }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" min-width="180" />
          <el-table-column label="操作" min-width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button 
                link 
                type="primary" 
                size="small" 
                @click="handleApprovalView(row)"
              >
                查看详情
              </el-button>
              <el-button 
                link 
                type="success" 
                size="small" 
                @click="handleApprove(row, true)"
              >
                通过
              </el-button>
              <el-button 
                link 
                type="danger" 
                size="small" 
                @click="handleApprove(row, false)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 待审核分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="approvalPagination.current"
            v-model:page-size="approvalPagination.size"
            :total="approvalPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleApprovalSizeChange"
            @current-change="handleApprovalPageChange"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 审核对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      :title="approveForm.approved ? '审核通过' : '审核拒绝'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="approveFormRef"
        :model="approveForm"
        label-width="100px"
      >
        <el-form-item label="工号">
          <span>{{ currentTeacher?.teacherNo || '-' }}</span>
        </el-form-item>
        <el-form-item label="姓名">
          <span v-if="currentTeacher && userInfoMap[currentTeacher.userId]">
            {{ userInfoMap[currentTeacher.userId].realName }}
          </span>
          <span v-else>-</span>
        </el-form-item>
        <el-form-item 
          label="审核意见" 
          :prop="approveForm.approved ? '' : 'auditOpinion'"
          :rules="approveForm.approved ? [] : [{ required: true, message: '请输入拒绝原因', trigger: 'blur' }]"
        >
          <el-input
            v-model="approveForm.auditOpinion"
            type="textarea"
            :rows="4"
            :placeholder="approveForm.approved ? '请输入审核意见（可选）' : '请输入拒绝原因（必填）'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="approving" 
          @click="handleConfirmApprove"
        >
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="教师详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="2" border v-if="currentTeacher && userInfoMap[currentTeacher.userId]">
        <el-descriptions-item label="工号">{{ currentTeacher.teacherNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfoMap[currentTeacher.userId].realName }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ userInfoMap[currentTeacher.userId].gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userInfoMap[currentTeacher.userId].phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfoMap[currentTeacher.userId].email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="身份证号">{{ userInfoMap[currentTeacher.userId].idCard || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentTeacher.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属学院">
          <span v-if="collegeMap[currentTeacher.collegeId]">
            {{ collegeMap[currentTeacher.collegeId].collegeName }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="所属学校">
          <span v-if="schoolMap[currentTeacher.schoolId]">
            {{ schoolMap[currentTeacher.schoolId].schoolName }}
          </span>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ currentTeacher.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div v-else style="padding: 20px; text-align: center; color: #909399;">
        加载中...
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="工号" prop="teacherNo">
              <el-input v-model="formData.teacherNo" placeholder="请输入工号" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="formData.username" 
                placeholder="请输入用户名（不填写则使用工号）" 
                :disabled="isEdit"
              />
              <div v-if="!isEdit" style="font-size: 12px; color: #909399; margin-top: 4px;">
                不填写则默认使用工号作为用户名
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属学院" prop="collegeId">
              <el-select
                v-model="formData.collegeId"
                placeholder="请选择学院"
                style="width: 100%"
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
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属学校" prop="schoolId">
              <el-select
                v-model="formData.schoolId"
                placeholder="请选择学校"
                style="width: 100%"
                :disabled="true"
              >
                <el-option
                  v-for="school in schoolList"
                  :key="school.schoolId"
                  :label="school.schoolName"
                  :value="school.schoolId"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="职称">
          <el-select
            v-model="formData.title"
            placeholder="请选择职称"
            style="width: 100%"
            filterable
            allow-create
            default-first-option
          >
            <el-option
              v-for="title in titleOptions"
              :key="title"
              :label="title"
              :value="title"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="初始密码" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="请输入初始密码（6-20个字符）"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Download } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { hasAnyRole, canEditUser } from '@/utils/permission'
import { useAuthStore } from '@/store/modules/auth'
import { teacherApi } from '@/api/user/teacher'
import { userApi } from '@/api/user/user'
import { collegeApi } from '@/api/system/college'
import { schoolApi } from '@/api/system/school'
import { exportExcel } from '@/utils/exportUtils'
import request from '@/utils/request'

// 加载状态
const loading = ref(false)
const submitting = ref(false)
const exportLoading = ref(false)
const approvalLoading = ref(false)
const approving = ref(false)

// 标签页
const activeTab = ref('list')

// 搜索表单
const searchForm = reactive({
  teacherNo: '',
  schoolId: null,
  collegeId: null,
  status: null
})

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 表格数据
const tableData = ref([])
const userInfoMap = ref({})
const collegeMap = ref({})
const schoolMap = ref({})

// 待审核相关
const approvalTableData = ref([])
const approvalSearchForm = reactive({
  teacherNo: '',
  realName: ''
})
const approvalPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const approveDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentTeacher = ref(null)
const approveFormRef = ref(null)
const approveForm = reactive({
  approved: true,
  auditOpinion: ''
})

// 学院列表和学校列表（用于下拉选择）
const collegeList = ref([])
const schoolList = ref([])

// 职称选项
const titleOptions = [
  '教授',
  '副教授',
  '讲师',
  '助教',
  '研究员',
  '副研究员',
  '助理研究员',
  '高级工程师',
  '工程师',
  '助理工程师',
  '其他'
]

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: '',
  collegeId: null,
  collegeName: ''
})

// 计算属性：学校下拉框是否禁用
const isSchoolDisabled = computed(() => {
  return hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
})

// 计算属性：学院下拉框是否禁用
const isCollegeDisabled = computed(() => {
  return hasAnyRole(['ROLE_COLLEGE_LEADER'])
})

// 计算属性：是否可以删除教师（用于显示删除按钮）
const canDeleteTeacherBtn = computed(() => {
  return hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
})

// 获取审核状态文本
const getAuditStatusText = (auditStatus) => {
  if (auditStatus === null || auditStatus === undefined) {
    return '-'
  }
  // 审核状态：0-待审核，1-已通过，2-已拒绝
  const statusMap = {
    0: '待审核',
    1: '已通过',
    2: '已拒绝'
  }
  return statusMap[auditStatus] || '-'
}

// 获取审核状态标签类型
const getAuditStatusType = (auditStatus) => {
  if (auditStatus === null || auditStatus === undefined) {
    return 'info'
  }
  // 审核状态：0-待审核（warning），1-已通过（success），2-已拒绝（danger）
  const typeMap = {
    0: 'warning', // 待审核 - 黄色
    1: 'success', // 已通过 - 绿色
    2: 'danger'   // 已拒绝 - 红色
  }
  return typeMap[auditStatus] || 'info'
}

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('添加教师')
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  teacherId: null,
  teacherNo: '',
  username: '',
  userId: null,
  realName: '',
  gender: '',
  idCard: '',
  phone: '',
  email: '',
  collegeId: null,
  schoolId: null,
  title: '',
  status: 1,
  password: ''
})

// 表单验证规则
const formRules = {
  teacherNo: [
    { required: true, message: '请输入工号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  idCard: [
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  collegeId: [
    { required: true, message: '请选择所属学院', trigger: 'change' }
  ],
  schoolId: [
    { required: true, message: '请选择所属学校', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 加载学院列表（所有）
const loadCollegeList = async () => {
  try {
    const res = await collegeApi.getCollegePage({ current: 1, size: 1000 })
    if (res.code === 200) {
      collegeList.value = res.data.records || []
      // 构建学院Map
      collegeList.value.forEach(college => {
        collegeMap.value[college.collegeId] = college
      })
    }
  } catch (error) {
    console.error('加载学院列表失败:', error)
  }
}

// 加载学校列表
const loadSchoolList = async () => {
  try {
    const res = await schoolApi.getSchoolPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      schoolList.value = res.data.records || []
      // 构建学校Map
      schoolList.value.forEach(school => {
        schoolMap.value[school.schoolId] = school
      })
    }
  } catch (error) {
    console.error('加载学校列表失败:', error)
  }
}

// 检查是否可以编辑该教师
const canEditTeacher = (row) => {
  if (!row || !row.userId) {
    return false
  }
  // 待审核状态的教师（auditStatus = 0）可能还没有角色，但仍然应该允许编辑
  // 审核状态：0-待审核，1-已通过，2-已拒绝
  const isPendingAudit = row.auditStatus !== null && row.auditStatus === 0
  if (isPendingAudit) {
    // 待审核的教师允许编辑（有权限的用户都可以编辑）
    return hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])
  }
  
  // 已审核通过的教师，使用原来的权限检查逻辑
  // 优先使用教师数据中的角色信息，如果没有则从用户信息中获取
  let roles = row.roles
  if (!roles || roles.length === 0) {
    const userInfo = userInfoMap.value[row.userId]
    if (userInfo && userInfo.roles) {
      roles = userInfo.roles
    } else {
      return false
    }
  }
  return canEditUser(roles)
}

// 检查是否可以删除该教师（删除权限与编辑权限相同）
const canDeleteTeacher = (row) => {
  return canEditTeacher(row)
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      teacherNo: searchForm.teacherNo || undefined,
      collegeId: searchForm.collegeId || undefined
    }
    const res = await teacherApi.getTeacherPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 加载用户信息
      const userIds = tableData.value.map(item => item.userId).filter(Boolean)
      if (userIds.length > 0) {
        await loadUserInfos(userIds)
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载用户信息
const loadUserInfos = async (userIds) => {
  try {
    const promises = userIds.map(userId => userApi.getUserById(userId))
    const results = await Promise.all(promises)
    results.forEach((res, index) => {
      if (res.code === 200 && res.data) {
        userInfoMap.value[userIds[index]] = res.data
        // 角色信息已经包含在返回的数据中，不需要额外请求
      }
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置搜索
const handleReset = () => {
  searchForm.teacherNo = ''
  searchForm.status = null
  
  // 根据角色重置筛选条件，保持组织信息的绑定
  if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])) {
    // 学校管理员、学院负责人：保持学校ID
    searchForm.schoolId = currentOrgInfo.value.schoolId || null
  } else {
    searchForm.schoolId = null
  }
  
  if (hasAnyRole(['ROLE_COLLEGE_LEADER'])) {
    // 学院负责人：保持学院ID
    searchForm.collegeId = currentOrgInfo.value.collegeId || null
  } else {
    searchForm.collegeId = null
  }
  
  // 重新加载学院列表
  if (searchForm.schoolId) {
    handleSchoolChange(searchForm.schoolId)
  } else {
    collegeList.value = []
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
  dialogTitle.value = '添加教师'
  resetFormData()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑教师'
  try {
    const res = await teacherApi.getTeacherById(row.teacherId)
    if (res.code === 200) {
      const teacher = res.data
      // 确保加载用户信息（如果userInfoMap中没有）
      if (teacher.userId && !userInfoMap.value[teacher.userId]) {
        try {
          const userRes = await userApi.getUserById(teacher.userId)
          if (userRes.code === 200 && userRes.data) {
            userInfoMap.value[teacher.userId] = userRes.data
          }
        } catch (error) {
          console.error('加载用户信息失败:', error)
        }
      }
      const userInfo = userInfoMap.value[teacher.userId] || {}
      Object.assign(formData, {
        teacherId: teacher.teacherId,
        teacherNo: teacher.teacherNo,
        username: userInfo.username || '',
        userId: teacher.userId,
        realName: userInfo.realName || '',
        gender: userInfo.gender || '',
        idCard: userInfo.idCard || '',
        phone: userInfo.phone || '',
        email: userInfo.email || '',
        collegeId: teacher.collegeId,
        schoolId: teacher.schoolId,
        title: teacher.title || '',
        status: teacher.status,
        password: ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取教师详情失败:', error)
    ElMessage.error('获取教师详情失败')
  }
}

// 重置表单数据
const resetFormData = () => {
  Object.assign(formData, {
    teacherId: null,
    teacherNo: '',
    username: '',
    userId: null,
    realName: '',
    gender: '',
    idCard: '',
    phone: '',
    email: '',
    collegeId: null,
    schoolId: null,
    title: '',
    status: 1,
    password: ''
  })
  formRef.value?.clearValidate()
}

// 学校变化时加载学院列表
const handleSchoolChange = async (schoolId) => {
  // 清空学院选择
  searchForm.collegeId = null
  
  if (schoolId) {
    // 加载该学校下的学院列表
    try {
      const res = await collegeApi.getCollegePage({ 
        current: 1, 
        size: 1000,
        schoolId: schoolId 
      })
      if (res.code === 200) {
        collegeList.value = res.data.records || []
        // 构建学院Map
        collegeList.value.forEach(college => {
          collegeMap.value[college.collegeId] = college
        })
      }
    } catch (error) {
      console.error('加载学院列表失败:', error)
    }
  } else {
    // 如果没有选择学校，加载所有学院
    await loadCollegeList()
  }
}

// 学院变化时更新学校
const handleCollegeChange = (collegeId) => {
  const college = collegeList.value.find(c => c.collegeId === collegeId)
  if (college) {
    formData.schoolId = college.schoolId
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const submitData = {
        teacherNo: formData.teacherNo,
        username: formData.username || undefined,
        realName: formData.realName,
        gender: formData.gender || null,
        idCard: formData.idCard,
        phone: formData.phone,
        email: formData.email,
        collegeId: formData.collegeId,
        schoolId: formData.schoolId,
        title: formData.title || undefined,
        status: formData.status
      }
      
      if (isEdit.value) {
        submitData.teacherId = formData.teacherId
        submitData.userId = formData.userId
        const res = await teacherApi.updateTeacher(submitData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        }
      } else {
        submitData.password = formData.password
        const res = await teacherApi.addTeacher(submitData)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadData()
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该教师吗？', '提示', {
      type: 'warning'
    })
    const res = await teacherApi.deleteTeacher(row.teacherId)
    if (res.code === 200) {
          ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 加载当前用户组织信息
const loadCurrentUserOrgInfo = async () => {
  try {
    const res = await userApi.getCurrentUserOrgInfo()
    if (res.code === 200 && res.data) {
      currentOrgInfo.value = {
        schoolId: res.data.schoolId || null,
        schoolName: res.data.schoolName || '',
        collegeId: res.data.collegeId || null,
        collegeName: res.data.collegeName || ''
      }
      
      // 根据角色设置筛选框默认值
      if (hasAnyRole(['ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER'])) {
        if (currentOrgInfo.value.schoolId) {
          searchForm.schoolId = currentOrgInfo.value.schoolId
          // 加载该学校的学院列表
          await handleSchoolChange(currentOrgInfo.value.schoolId)
        }
      }
      
      if (hasAnyRole(['ROLE_COLLEGE_LEADER'])) {
        if (currentOrgInfo.value.collegeId) {
          searchForm.collegeId = currentOrgInfo.value.collegeId
        }
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

// 导出教师列表
const handleExport = async () => {
  exportLoading.value = true
  try {
    const params = {
      teacherNo: searchForm.teacherNo || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined
    }
    
    // 注意：需要后端提供 /user/teacher/export 接口
    await exportExcel(
      (params) => request.get('/user/teacher/export', { params, responseType: 'blob' }),
      params,
      '教师列表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName) => {
  if (tabName === 'approval') {
    loadApprovalList()
  } else {
    loadData()
  }
}

// 加载待审核列表
const loadApprovalList = async () => {
  approvalLoading.value = true
  try {
    const params = {
      current: approvalPagination.current,
      size: approvalPagination.size,
      teacherNo: approvalSearchForm.teacherNo || undefined,
      realName: approvalSearchForm.realName || undefined
    }
    const res = await teacherApi.getPendingApprovalTeacherPage(params)
    if (res.code === 200) {
      approvalTableData.value = res.data.records || []
      approvalPagination.total = res.data.total || 0
      
      // 加载用户信息
      const userIds = approvalTableData.value.map(item => item.userId).filter(Boolean)
      if (userIds.length > 0) {
        await loadUserInfos(userIds)
      }
    }
  } catch (error) {
    console.error('加载待审核列表失败:', error)
    ElMessage.error('加载待审核列表失败')
  } finally {
    approvalLoading.value = false
  }
}

// 待审核搜索
const handleApprovalSearch = () => {
  approvalPagination.current = 1
  loadApprovalList()
}

// 待审核重置
const handleApprovalReset = () => {
  approvalSearchForm.teacherNo = ''
  approvalSearchForm.realName = ''
  approvalPagination.current = 1
  loadApprovalList()
}

// 待审核分页变化
const handleApprovalSizeChange = (size) => {
  approvalPagination.size = size
  approvalPagination.current = 1
  loadApprovalList()
}

const handleApprovalPageChange = (page) => {
  approvalPagination.current = page
  loadApprovalList()
}

// 查看详情
const handleApprovalView = async (row) => {
  currentTeacher.value = row
  
  // 确保加载用户信息（如果userInfoMap中没有）
  if (row.userId && !userInfoMap.value[row.userId]) {
    try {
      const userRes = await userApi.getUserById(row.userId)
      if (userRes.code === 200 && userRes.data) {
        userInfoMap.value[row.userId] = userRes.data
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  }
  
  detailDialogVisible.value = true
}

// 审核
const handleApprove = async (row, approved) => {
  currentTeacher.value = row
  approveForm.approved = approved
  approveForm.auditOpinion = ''
  
  // 确保加载用户信息（如果userInfoMap中没有）
  if (row.userId && !userInfoMap.value[row.userId]) {
    try {
      const userRes = await userApi.getUserById(row.userId)
      if (userRes.code === 200 && userRes.data) {
        userInfoMap.value[row.userId] = userRes.data
      }
    } catch (error) {
      console.error('加载用户信息失败:', error)
    }
  }
  
  approveDialogVisible.value = true
}

// 确认审核
const handleConfirmApprove = async () => {
  if (!approveFormRef.value) return
  
  await approveFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!approveForm.approved && !approveForm.auditOpinion) {
      ElMessage.warning('请输入拒绝原因')
      return
    }
    
    approving.value = true
    try {
      const res = await teacherApi.approveTeacherRegistration(
        currentTeacher.value.teacherId,
        approveForm.approved,
        approveForm.auditOpinion
      )
      if (res.code === 200) {
        ElMessage.success(res.message || '审核成功')
        approveDialogVisible.value = false
        loadApprovalList()
        // 如果审核通过，刷新教师列表
        if (approveForm.approved && activeTab.value === 'list') {
          loadData()
        }
      } else {
        ElMessage.error(res.message || '审核失败')
      }
    } catch (error) {
      console.error('审核失败:', error)
      ElMessage.error('审核失败：' + (error.response?.data?.message || error.message || '未知错误'))
    } finally {
      approving.value = false
    }
  })
}

// 初始化
onMounted(async () => {
  loadCollegeList()
  loadSchoolList()
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有学院
  if (!currentOrgInfo.value.schoolId) {
    loadCollegeList()
  }
  loadData()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

