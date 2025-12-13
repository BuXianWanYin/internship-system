<template>
  <PageLayout title="学生管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加学生</el-button>
      <el-button 
        v-if="hasAnyRole(['ROLE_CLASS_TEACHER'])" 
        type="primary" 
        :icon="Download" 
        @click="handleDownloadTemplate"
      >
        下载模板
      </el-button>
      <el-button 
        v-if="hasAnyRole(['ROLE_CLASS_TEACHER'])" 
        type="success" 
        :icon="Upload" 
        @click="handleOpenImportDialog"
      >
        批量导入
      </el-button>
    </template>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- Tab 1: 学生列表 -->
      <el-tab-pane label="学生列表" name="list">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="学号">
              <el-input
                v-model="searchForm.studentNo"
                placeholder="请输入学号"
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
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
                style="width: 150px"
              >
                <el-option label="已审核" :value="1" />
                <el-option label="待审核" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="入学年份">
              <el-input-number
                v-model="searchForm.enrollmentYear"
                placeholder="请输入年份"
                clearable
                style="width: 150px"
                :min="2000"
                :max="2100"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
              <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              <el-button 
                type="success" 
                :icon="Download" 
                @click="handleExportStudentList"
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
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column label="学生信息" min-width="150">
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
          <el-table-column prop="enrollmentYear" label="入学年份" min-width="100" align="center" />
          <el-table-column label="所属班级" min-width="150">
            <template #default="{ row }">
              <span v-if="classMap[row.classId]">{{ classMap[row.classId].className }}</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="所属专业" min-width="150">
            <template #default="{ row }">
              <span v-if="majorMap[row.majorId]">{{ majorMap[row.majorId].majorName }}</span>
              <span v-else style="color: #909399">加载中...</span>
            </template>
          </el-table-column>
          <el-table-column label="所属学院" min-width="150">
            <template #default="{ row }">
              <span v-if="majorMap[row.majorId] && collegeMap[majorMap[row.majorId].collegeId]">
                {{ collegeMap[majorMap[row.majorId].collegeId].collegeName }}
              </span>
              <span v-else style="color: #909399">加载中...</span>
            </template>
          </el-table-column>
          <el-table-column label="所属学校" min-width="150">
            <template #default="{ row }">
              <span v-if="majorMap[row.majorId] && 
                          collegeMap[majorMap[row.majorId].collegeId] && 
                          schoolMap[collegeMap[majorMap[row.majorId].collegeId].schoolId]">
                {{ schoolMap[collegeMap[majorMap[row.majorId].collegeId].schoolId].schoolName }}
              </span>
              <span v-else style="color: #909399">加载中...</span>
            </template>
          </el-table-column>
          <el-table-column label="实习企业" min-width="150">
            <template #default="{ row }">
              <span v-if="row.currentEnterpriseName">{{ row.currentEnterpriseName }}</span>
              <span v-else style="color: #909399">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" min-width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
                {{ row.status === 1 ? '已审核' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="180" />
          <el-table-column label="操作" min-width="280" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button 
                v-if="canDeleteStudentBtn" 
                link 
                type="danger" 
                size="small" 
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

      <!-- Tab 2: 待审核学生 -->
      <el-tab-pane label="待审核学生" name="approval">
        <!-- 搜索栏 -->
        <div class="search-bar">
          <el-form :inline="true" :model="approvalSearchForm" class="search-form">
            <el-form-item label="学号">
              <el-input
                v-model="approvalSearchForm.studentNo"
                placeholder="请输入学号"
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

        <!-- 数据表格 -->
        <el-table
          v-loading="approvalLoading"
          :data="approvalTableData"
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        >
          <el-table-column prop="studentNo" label="学号" min-width="120" />
          <el-table-column label="学生信息" min-width="150">
            <template #default="{ row }">
              <div v-if="userInfoMap[row.userId]">
                <div>{{ userInfoMap[row.userId].realName }}</div>
                <div style="font-size: 12px; color: #909399;">
                  {{ userInfoMap[row.userId].phone || '未填写' }}
                </div>
              </div>
              <span v-else>加载中...</span>
            </template>
          </el-table-column>
          <el-table-column prop="enrollmentYear" label="入学年份" width="100" align="center" />
          <el-table-column label="班级" min-width="150">
            <template #default="{ row }">
              <span v-if="classMap[row.classId]">{{ classMap[row.classId].className }}</span>
              <span v-else>加载中...</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180" />
          <el-table-column label="操作" width="250" fixed="right" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="handleApprovalView(row)">查看详情</el-button>
              <el-button link type="success" size="small" @click="handleApprove(row, true)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleApprove(row, false)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
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
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="formData.studentNo" placeholder="请输入学号" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="用户名" prop="username">
          <el-input 
            v-model="formData.username" 
            placeholder="请输入用户名（用于登录）" 
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="formData.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
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
        <el-form-item label="所属班级" prop="classId">
          <el-select v-model="formData.classId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="classItem in classList"
              :key="classItem.classId"
              :label="classItem.className"
              :value="classItem.classId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份" prop="enrollmentYear">
          <el-input-number
            v-model="formData.enrollmentYear"
            :min="2000"
            :max="2100"
            placeholder="请输入入学年份"
            style="width: 100%"
          />
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

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学生详情"
      width="600px"
    >
      <el-descriptions v-if="currentStudent" :column="2" border>
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentStudent.enrollmentYear }}</el-descriptions-item>
        <el-descriptions-item label="姓名" v-if="userInfoMap[currentStudent.userId]">
          {{ userInfoMap[currentStudent.userId].realName }}
        </el-descriptions-item>
        <el-descriptions-item label="性别" v-if="userInfoMap[currentStudent.userId]">
          {{ userInfoMap[currentStudent.userId].gender || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="身份证号" v-if="userInfoMap[currentStudent.userId]">
          {{ userInfoMap[currentStudent.userId].idCard || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="手机号" v-if="userInfoMap[currentStudent.userId]">
          {{ userInfoMap[currentStudent.userId].phone || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱" v-if="userInfoMap[currentStudent.userId]">
          {{ userInfoMap[currentStudent.userId].email || '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="班级" v-if="classMap[currentStudent.classId]">
          {{ classMap[currentStudent.classId].className }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentStudent.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

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
        <el-form-item label="学生信息">
          <div v-if="currentStudent">
            <div>学号：{{ currentStudent.studentNo }}</div>
            <div v-if="userInfoMap[currentStudent.userId]">
              姓名：{{ userInfoMap[currentStudent.userId].realName }}
            </div>
          </div>
        </el-form-item>
        <el-form-item
          label="审核意见"
          :prop="approveForm.approved ? '' : 'auditOpinion'"
        >
          <el-input
            v-model="approveForm.auditOpinion"
            type="textarea"
            :rows="4"
            :placeholder="approveForm.approved ? '审核意见' : '请输入拒绝原因'"
            :required="!approveForm.approved"
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
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 批量导入对话框 -->
    <el-dialog
      v-model="importDialogVisible"
      title="批量导入学生"
      width="700px"
      :close-on-click-modal="false"
      @close="handleImportDialogClose"
    >
      <div v-if="!importResult && importStatus !== 'uploading'">
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #title>
            <div>
              <div>请先下载模板，按照模板格式填写学生信息后上传。</div>
              <div style="margin-top: 8px; font-size: 12px; color: #909399;">
                模板列说明：学号*、姓名*、性别、身份证号、手机号、邮箱、入学年份*、初始密码*
              </div>
            </div>
          </template>
        </el-alert>
        
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :limit="1"
          accept=".xlsx,.xls"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传Excel文件（.xlsx或.xls），且不超过10MB
            </div>
          </template>
        </el-upload>

        <div class="class-select" style="margin-top: 20px;">
          <el-form-item label="学生班级*" required>
            <el-select
              v-model="selectedClassId"
              placeholder="请选择学生班级"
              style="width: 100%;"
            >
              <el-option
                v-for="classItem in classList"
                :key="classItem.classId"
                :label="classItem.className"
                :value="classItem.classId"
              />
            </el-select>
          </el-form-item>
        </div>
      </div>

      <!-- 导入进度 -->
      <div v-if="importStatus === 'uploading'">
        <el-alert
          type="info"
          :closable="false"
          style="margin-bottom: 20px"
        >
          <template #title>
            <div>正在导入学生数据，请稍候...</div>
          </template>
        </el-alert>
        <el-progress
          :percentage="importProgress"
          :status="importProgress === 100 ? 'success' : undefined"
          style="margin-bottom: 20px"
        />
      </div>

      <!-- 导入结果 -->
      <div v-if="importResult">
        <el-alert
          :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
          :type="importResult.failCount === 0 ? 'success' : 'warning'"
          :closable="false"
          show-icon
          style="margin-bottom: 20px"
        />

        <!-- 成功列表 -->
        <div v-if="importResult.successList && importResult.successList.length > 0" style="margin-bottom: 20px;">
          <h4 style="margin-bottom: 10px; color: #67c23a;">
            <el-icon><CircleCheck /></el-icon>
            成功导入（{{ importResult.successList.length }} 条）：
          </h4>
          <el-table :data="importResult.successList" border max-height="200" style="margin-bottom: 20px;">
            <el-table-column prop="rowNum" label="行号" width="80" align="center" />
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column label="状态" width="80" align="center">
              <template #default>
                <el-tag type="success" size="small">成功</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 失败列表 -->
        <div v-if="importResult.failList && importResult.failList.length > 0">
          <h4 style="margin-bottom: 10px; color: #f56c6c;">
            <el-icon><CircleClose /></el-icon>
            导入失败（{{ importResult.failList.length }} 条）：
          </h4>
          <el-table :data="importResult.failList" border max-height="300">
            <el-table-column prop="rowNum" label="行号" width="80" align="center" />
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="errorMessage" label="失败原因" min-width="200" />
            <el-table-column label="状态" width="80" align="center">
              <template #default>
                <el-tag type="danger" size="small">失败</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button
            v-if="!importResult"
            type="primary"
            :icon="Download"
            @click="handleDownloadTemplate"
          >
            下载模板
          </el-button>
          <div style="flex: 1"></div>
          <el-button @click="handleImportDialogClose">关闭</el-button>
          <el-button
            v-if="!importResult && fileList.length > 0"
            type="primary"
            :loading="uploading"
            @click="handleUpload"
          >
            开始导入
          </el-button>
        </div>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Upload, Download, UploadFilled, CircleCheck, CircleClose } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'

// 计算属性：是否可以删除学生（用于显示删除按钮）
const canDeleteStudentBtn = computed(() => {
  return hasAnyRole(['ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER'])
})
import { studentApi } from '@/api/user/student'
import { classApi } from '@/api/system/class'
import { schoolApi } from '@/api/system/school'
import { collegeApi } from '@/api/system/college'
import { majorApi } from '@/api/system/major'
import { userApi } from '@/api/user/user'
import { reportApi } from '@/api/report'
import { exportExcel } from '@/utils/exportUtils'
import request from '@/utils/request'

// 权限相关
const authStore = useAuthStore()
const userRoles = computed(() => authStore.roles || [])

// Tab切换
const activeTab = ref('list')

// ========== 学生列表相关 ==========
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const userInfoMap = ref({})
const classMap = ref({})
const classList = ref([])
const schoolList = ref([])
const schoolMap = ref({})
const collegeList = ref([])
const collegeMap = ref({})
const majorList = ref([])
const majorMap = ref({})

// 当前用户组织信息
const currentOrgInfo = ref({
  schoolId: null,
  schoolName: '',
  collegeId: null,
  collegeName: '',
  classIds: [],
  classNames: []
})

const searchForm = reactive({
  studentNo: '',
  schoolId: null,
  collegeId: null,
  majorId: null,
  classId: null,
  status: null,
  enrollmentYear: null
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// ========== 待审核学生相关 ==========
const approvalLoading = ref(false)
const approving = ref(false)
const approvalTableData = ref([])
const approvalSearchForm = reactive({
  studentNo: '',
  realName: ''
})
const approvalPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})
const approveDialogVisible = ref(false)
const approveFormRef = ref(null)
const approveForm = reactive({
  approved: true,
  auditOpinion: ''
})

// ========== 批量导入相关 ==========
const importDialogVisible = ref(false)
const uploadRef = ref(null)
const fileList = ref([])
const selectedClassId = ref(null)
const uploading = ref(false)
const importResult = ref(null)
const importProgress = ref(0)
const importStatus = ref(null) // 'uploading' | 'success' | 'error'

// ========== 添加/编辑相关 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('添加学生')
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const formData = reactive({
  studentId: null,
  studentNo: '',
  username: '',
  userId: null,
  realName: '',
  gender: '',
  idCard: '',
  phone: '',
  email: '',
  classId: null,
  enrollmentYear: new Date().getFullYear(),
  status: 1,
  password: ''
})
const formRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  enrollmentYear: [{ required: true, message: '请输入入学年份', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

// ========== 详情对话框 ==========
const detailDialogVisible = ref(false)
const currentStudent = ref(null)

// ========== Tab切换处理 ==========
const handleTabChange = (tabName) => {
  if (tabName === 'list') {
    loadStudentList()
  } else if (tabName === 'approval') {
    loadApprovalList()
  }
}

// ========== 学生列表功能 ==========
const loadStudentList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      studentNo: searchForm.studentNo || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      majorId: searchForm.majorId || undefined,
      classId: searchForm.classId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      enrollmentYear: searchForm.enrollmentYear || undefined
    }
    const res = await studentApi.getStudentPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0

      // 加载用户信息、班级信息、专业信息、学院信息、学校信息
      const userIds = [...new Set(tableData.value.map(item => item.userId))]
      const classIds = [...new Set(tableData.value.map(item => item.classId))]
      const majorIds = [...new Set(tableData.value.map(item => item.majorId))]

      await loadUserInfo(userIds)
      await loadClassInfo(classIds)
      await loadMajorInfo(majorIds)
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadStudentList()
}

const handleReset = () => {
  searchForm.studentNo = ''
  searchForm.status = null
  searchForm.enrollmentYear = null
  
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
  
  // 重新加载列表
  if (searchForm.schoolId) {
    loadCollegeList(searchForm.schoolId)
  } else {
    collegeList.value = []
  }
  
  if (searchForm.collegeId) {
    loadMajorList(searchForm.collegeId)
  } else {
    majorList.value = []
  }
  
  if (searchForm.majorId) {
    loadClassList(searchForm.majorId)
  } else {
    classList.value = []
  }
  
  pagination.current = 1
  loadStudentList()
}

// 学校变化时，清空下级选项并重新加载学院列表
const handleSchoolChange = (schoolId) => {
  searchForm.collegeId = null
  searchForm.majorId = null
  searchForm.classId = null
  majorList.value = []
  classList.value = []
  if (schoolId) {
    loadCollegeList(schoolId)
  } else {
    collegeList.value = []
  }
}

// 学院变化时，清空下级选项并重新加载专业列表
const handleCollegeChange = (collegeId) => {
  searchForm.majorId = null
  searchForm.classId = null
  classList.value = []
  if (collegeId) {
    loadMajorList(collegeId)
  } else {
    majorList.value = []
  }
}

// 专业变化时，清空下级选项并重新加载班级列表
const handleMajorChange = (majorId) => {
  searchForm.classId = null
  if (majorId) {
    loadClassListByMajor(majorId)
  } else {
    classList.value = []
  }
}

const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadStudentList()
}

const handlePageChange = (page) => {
  pagination.current = page
  loadStudentList()
}

const handleView = (row) => {
  currentStudent.value = row
  detailDialogVisible.value = true
}

const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '添加学生'
  resetFormData()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑学生'
  try {
    const res = await studentApi.getStudentById(row.studentId)
    if (res.code === 200) {
      const student = res.data
      // 加载用户信息
      if (student.userId) {
        const userRes = await userApi.getUserById(student.userId)
        if (userRes.code === 200 && userRes.data) {
          Object.assign(formData, {
            studentId: student.studentId,
            studentNo: student.studentNo,
            username: userRes.data.username || '',
            userId: student.userId,
            realName: userRes.data.realName,
            gender: userRes.data.gender || '',
            idCard: userRes.data.idCard || '',
            phone: userRes.data.phone || '',
            email: userRes.data.email || '',
            classId: student.classId,
            enrollmentYear: student.enrollmentYear,
            password: ''
            // 编辑时不显示和修改status
          })
        }
      }
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取学生详情失败')
  }
}

const resetFormData = () => {
  Object.assign(formData, {
    studentId: null,
    studentNo: '',
    username: '',
    userId: null,
    realName: '',
    gender: '',
    idCard: '',
    phone: '',
    email: '',
    classId: null,
    enrollmentYear: new Date().getFullYear(),
    status: 1,
    password: ''
  })
  formRef.value?.clearValidate()
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (isEdit.value) {
        // 编辑学生：先更新用户信息，再更新学生信息
        if (formData.userId) {
          // 更新用户信息
          const userUpdateData = {
            userId: formData.userId,
            realName: formData.realName,
            gender: formData.gender || null,
            idCard: formData.idCard,
            phone: formData.phone,
            email: formData.email
            // 编辑时不更新status，保持原有状态
          }
          await userApi.updateUser(userUpdateData)
        }
        
        // 更新学生信息
        const studentUpdateData = {
          studentId: formData.studentId,
          studentNo: formData.studentNo,
          classId: formData.classId,
          enrollmentYear: formData.enrollmentYear
          // 编辑时不更新status，保持原有状态
        }
        const res = await studentApi.updateStudent(studentUpdateData)
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadStudentList()
        }
      } else {
        // 添加学生：先创建用户，再创建学生
        // 检查学号是否已存在（通过查询学生列表检查）
        try {
          const checkRes = await studentApi.getStudentPage({ 
            current: 1, 
            size: 1, 
            studentNo: formData.studentNo 
          })
          if (checkRes.code === 200 && checkRes.data && checkRes.data.records && checkRes.data.records.length > 0) {
            ElMessage.error('学号已存在')
            return
          }
        } catch (error) {
          // 检查失败，继续创建
        }

        // 创建用户
        const userData = {
          username: formData.username || formData.studentNo,
          password: formData.password,
          realName: formData.realName,
          gender: formData.gender || null,
          idCard: formData.idCard,
          phone: formData.phone,
          email: formData.email,
          status: formData.status
        }
        const userRes = await userApi.addUser(userData)
        if (userRes.code !== 200 || !userRes.data) {
          ElMessage.error('创建用户失败')
          return
        }

        // 获取班级信息以获取专业、学院、学校ID
        const classRes = await classApi.getClassById(formData.classId)
        if (classRes.code !== 200 || !classRes.data) {
          ElMessage.error('班级不存在')
          return
        }
        const classInfo = classRes.data

        // 创建学生
        const studentData = {
          userId: userRes.data.userId,
          studentNo: formData.studentNo,
          classId: formData.classId,
          enrollmentYear: formData.enrollmentYear,
          majorId: classInfo.majorId,
          status: formData.status
        }
        const studentRes = await studentApi.addStudent(studentData)
        if (studentRes.code === 200) {
          // 分配学生角色
          try {
            await userApi.assignRoleToUser(userRes.data.userId, 'ROLE_STUDENT')
          } catch (error) {
            console.error('分配角色失败:', error)
          }
          
          ElMessage.success('添加成功')
          dialogVisible.value = false
          loadStudentList()
        } else {
          ElMessage.error('添加学生失败')
        }
      }
    } catch (error) {
      console.error('操作失败:', error)
      ElMessage.error('操作失败：' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除学生 "${row.studentNo}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await studentApi.deleteStudent(row.studentId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadStudentList()
      }
    } catch (error) {
      const errorMessage = error.response?.data?.message || error.message || '删除失败'
      ElMessage.error(errorMessage)
    }
  }).catch(() => {})
}

// ========== 待审核学生功能 ==========
const loadApprovalList = async () => {
  approvalLoading.value = true
  try {
    const params = {
      current: approvalPagination.current,
      size: approvalPagination.size,
      studentNo: approvalSearchForm.studentNo || undefined,
      realName: approvalSearchForm.realName || undefined
    }
    const res = await studentApi.getPendingApprovalStudentPage(params)
    if (res.code === 200) {
      approvalTableData.value = res.data.records || []
      approvalPagination.total = res.data.total || 0

      // 加载用户信息和班级信息
      const userIds = [...new Set(approvalTableData.value.map(item => item.userId))]
      const classIds = [...new Set(approvalTableData.value.map(item => item.classId))]

      await loadUserInfo(userIds)
      await loadClassInfo(classIds)
    }
  } catch (error) {
    ElMessage.error('加载失败：' + (error.message || '未知错误'))
  } finally {
    approvalLoading.value = false
  }
}

const handleApprovalSearch = () => {
  approvalPagination.current = 1
  loadApprovalList()
}

const handleApprovalReset = () => {
  approvalSearchForm.studentNo = ''
  approvalSearchForm.realName = ''
  approvalPagination.current = 1
  loadApprovalList()
}

const handleApprovalSizeChange = (size) => {
  approvalPagination.size = size
  approvalPagination.current = 1
  loadApprovalList()
}

const handleApprovalPageChange = (page) => {
  approvalPagination.current = page
  loadApprovalList()
}

const handleApprovalView = (row) => {
  currentStudent.value = row
  detailDialogVisible.value = true
}

const handleApprove = (row, approved) => {
  currentStudent.value = row
  approveForm.approved = approved
  approveForm.auditOpinion = ''
  approveDialogVisible.value = true
}

const handleConfirmApprove = async () => {
  if (!approveForm.approved && !approveForm.auditOpinion) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  approving.value = true
  try {
    const res = await studentApi.approveStudentRegistration(
      currentStudent.value.studentId,
      approveForm.approved,
      approveForm.auditOpinion
    )
    if (res.code === 200) {
      ElMessage.success(res.message || '审核成功')
      approveDialogVisible.value = false
      loadApprovalList()
      // 如果审核通过，刷新学生列表
      if (approveForm.approved && activeTab.value === 'list') {
        loadStudentList()
      }
    } else {
      ElMessage.error(res.message || '审核失败')
    }
  } catch (error) {
    ElMessage.error('审核失败：' + (error.message || '未知错误'))
  } finally {
    approving.value = false
  }
}

// ========== 批量导入功能 ==========
const handleOpenImportDialog = () => {
  importDialogVisible.value = true
  // 确保班级列表已加载
  if (classList.value.length === 0) {
    loadClassList()
  }
}

const handleDownloadTemplate = async () => {
  try {
    const response = await studentApi.downloadImportTemplate()
    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '学生导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败：' + (error.message || '未知错误'))
  }
}

const handleFileChange = (file) => {
  fileList.value = [file]
}

const handleFileRemove = () => {
  fileList.value = []
  selectedClassId.value = null
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择要上传的文件')
    return
  }

  if (!selectedClassId.value) {
    ElMessage.warning('请选择学生班级')
    return
  }

  const file = fileList.value[0].raw
  if (!file) {
    ElMessage.warning('文件无效')
    return
  }

  // 验证文件大小（10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error('文件大小不能超过10MB')
    return
  }

  uploading.value = true
  importStatus.value = 'uploading'
  importProgress.value = 0
  importResult.value = null

  // 模拟进度更新
  const progressInterval = setInterval(() => {
    if (importProgress.value < 90) {
      importProgress.value += 10
    }
  }, 200)

  try {
    const res = await studentApi.importStudents(file, selectedClassId.value)
    clearInterval(progressInterval)
    importProgress.value = 100
    
    if (res.code === 200) {
      importResult.value = res.data
      importStatus.value = 'success'
      ElMessage.success(res.message || '导入完成')
      // 导入成功后刷新学生列表和待审核列表
      if (activeTab.value === 'list') {
        loadStudentList()
      } else if (activeTab.value === 'approval') {
        loadApprovalList()
      }
    } else {
      importStatus.value = 'error'
      ElMessage.error(res.message || '导入失败')
    }
  } catch (error) {
    clearInterval(progressInterval)
    importProgress.value = 0
    importStatus.value = 'error'
    ElMessage.error('导入失败：' + (error.message || '未知错误'))
  } finally {
    uploading.value = false
    setTimeout(() => {
      if (importStatus.value === 'uploading') {
        importStatus.value = null
      }
    }, 500)
  }
}

const handleImportDialogClose = () => {
  // 重置导入相关状态
  fileList.value = []
  selectedClassId.value = null
  importResult.value = null
  importProgress.value = 0
  importStatus.value = null
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
  // 关闭对话框
  importDialogVisible.value = false
}

// ========== 公共功能 ==========
const loadUserInfo = async (userIds) => {
  try {
    for (const userId of userIds) {
      if (!userInfoMap.value[userId]) {
        const res = await userApi.getUserById(userId)
        if (res.code === 200 && res.data) {
          userInfoMap.value[userId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败：', error)
  }
}

const loadClassInfo = async (classIds) => {
  try {
    for (const classId of classIds) {
      if (!classMap.value[classId]) {
        const res = await classApi.getClassById(classId)
        if (res.code === 200 && res.data) {
          classMap.value[classId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载班级信息失败：', error)
  }
}

const loadClassList = async () => {
  try {
    const res = await classApi.getClassPage({ current: 1, size: 1000 })
    if (res.code === 200 && res.data) {
      classList.value = res.data.records || []
      // 构建班级Map
      classList.value.forEach(classItem => {
        classMap.value[classItem.classId] = classItem
      })
    }
  } catch (error) {
    console.error('加载班级列表失败：', error)
  }
}

const loadSchoolList = async () => {
  try {
    const res = await schoolApi.getSchoolPage({ current: 1, size: 1000 })
    if (res.code === 200) {
      schoolList.value = res.data.records || []
      schoolList.value.forEach(school => {
        schoolMap.value[school.schoolId] = school
      })
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
      collegeList.value.forEach(college => {
        collegeMap.value[college.collegeId] = college
        if (college.schoolId && !schoolMap.value[college.schoolId]) {
          loadSchoolInfo([college.schoolId])
        }
      })
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
      majorList.value.forEach(major => {
        majorMap.value[major.majorId] = major
        if (major.collegeId && !collegeMap.value[major.collegeId]) {
          loadCollegeInfo([major.collegeId])
        }
      })
    }
  } catch (error) {
    console.error('加载专业列表失败:', error)
  }
}

const loadClassListByMajor = async (majorId) => {
  try {
    const params = { current: 1, size: 1000 }
    if (majorId) {
      params.majorId = majorId
    }
    const res = await classApi.getClassPage(params)
    if (res.code === 200) {
      classList.value = res.data.records || []
      classList.value.forEach(classItem => {
        classMap.value[classItem.classId] = classItem
      })
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

const loadMajorInfo = async (majorIds) => {
  try {
    for (const majorId of majorIds) {
      if (!majorMap.value[majorId]) {
        const res = await majorApi.getMajorById(majorId)
        if (res.code === 200 && res.data) {
          majorMap.value[majorId] = res.data
          if (res.data.collegeId) {
            await loadCollegeInfo([res.data.collegeId])
          }
        }
      }
    }
  } catch (error) {
    console.error('加载专业信息失败:', error)
  }
}

const loadCollegeInfo = async (collegeIds) => {
  try {
    for (const collegeId of collegeIds) {
      if (!collegeMap.value[collegeId]) {
        const res = await collegeApi.getCollegeById(collegeId)
        if (res.code === 200 && res.data) {
          collegeMap.value[collegeId] = res.data
          if (res.data.schoolId && !schoolMap.value[res.data.schoolId]) {
            await loadSchoolInfo([res.data.schoolId])
          }
        }
      }
    }
  } catch (error) {
    console.error('加载学院信息失败:', error)
  }
}

const loadSchoolInfo = async (schoolIds) => {
  try {
    for (const schoolId of schoolIds) {
      if (!schoolMap.value[schoolId]) {
        const res = await schoolApi.getSchoolById(schoolId)
        if (res.code === 200 && res.data) {
          schoolMap.value[schoolId] = res.data
        }
      }
    }
  } catch (error) {
    console.error('加载学校信息失败:', error)
  }
}

// 初始化
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

// 计算属性：过滤后的班级列表（班主任只能看到管理的班级）
const filteredClassList = computed(() => {
  if (hasAnyRole(['ROLE_CLASS_TEACHER']) && currentOrgInfo.value.classIds && currentOrgInfo.value.classIds.length > 0) {
    return classList.value.filter(c => currentOrgInfo.value.classIds.includes(c.classId))
  }
  return classList.value
})

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
      }
    }
  } catch (error) {
    console.error('获取组织信息失败:', error)
  }
}

// 导出学生列表
const handleExportStudentList = async () => {
  exportLoading.value = true
  try {
    // 构建导出参数，使用当前页面的筛选条件
    const params = {
      studentNo: searchForm.studentNo || undefined,
      schoolId: searchForm.schoolId || undefined,
      collegeId: searchForm.collegeId || undefined,
      majorId: searchForm.majorId || undefined,
      classId: searchForm.classId || undefined,
      status: searchForm.status !== null ? searchForm.status : undefined,
      enrollmentYear: searchForm.enrollmentYear || undefined
    }
    
    await exportExcel(
      (params) => request.get('/user/student/export', { params, responseType: 'blob' }),
      params,
      '学生列表'
    )
    ElMessage.success('导出成功')
  } catch (error) {
    // 错误已在 exportExcel 中处理
  } finally {
    exportLoading.value = false
  }
}

// 导出学生个人实习报告
onMounted(async () => {
  loadSchoolList()
  await loadCurrentUserOrgInfo()
  // 如果没有组织信息，加载所有数据
  if (!currentOrgInfo.value.schoolId) {
    loadCollegeList(null)
  }
  if (!currentOrgInfo.value.collegeId) {
    loadMajorList(null)
  }
  loadClassList()
  loadStudentList()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.search-form {
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-section {
  margin-top: 40px;
}

.class-select {
  margin-top: 20px;
}

.upload-actions {
  margin-top: 20px;
  text-align: center;
}

.result-section {
  margin-top: 40px;
}

.fail-list {
  margin-top: 20px;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>

