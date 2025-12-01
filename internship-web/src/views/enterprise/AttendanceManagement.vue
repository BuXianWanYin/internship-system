<template>
  <PageLayout :title="pageTitle">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">确认考勤</el-button>
      <el-button type="success" :icon="Plus" @click="handleBatchAdd">批量确认</el-button>
    </template>

    <!-- 待确认考勤提示 -->
    <el-alert
      v-if="statistics.pendingCount > 0"
      :title="`有 ${statistics.pendingCount} 条待确认的考勤记录，请及时处理`"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      <template #default>
        <span>待确认考勤：{{ statistics.pendingCount }} 条</span>
        <el-button
          type="primary"
          link
          size="small"
          style="margin-left: 10px"
          @click="handleViewPending"
        >
          立即查看
        </el-button>
      </template>
    </el-alert>

    <!-- 搜索筛选栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学生姓名">
          <el-input
            v-model="searchForm.studentName"
            placeholder="请输入学生姓名"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="学号">
          <el-input
            v-model="searchForm.studentNo"
            placeholder="请输入学号"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 日历和统计面板 -->
    <el-row :gutter="20" style="margin-bottom: 20px">
      <!-- 左侧：日历组件 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div style="display: flex; align-items: center; justify-content: space-between">
              <span style="font-size: 14px; font-weight: 600">考勤日历</span>
              <div style="display: flex; gap: 10px; align-items: center">
                <div class="legend-item">
                  <span class="legend-dot normal"></span>
                  <span>正常</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot leave"></span>
                  <span>请假</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot rest"></span>
                  <span>休息</span>
                </div>
                <div class="legend-item">
                  <span class="legend-dot pending"></span>
                  <span>待确认</span>
                </div>
              </div>
            </div>
          </template>
          <el-calendar v-loading="loading" v-model="calendarDate">
            <template #date-cell="{ data }">
              <div class="calendar-cell" @click="handleDateClick(data.day)">
                <div class="calendar-date">{{ data.day.split('-').slice(2).join('-') }}</div>
                <div v-if="getDateAttendanceCount(data.day) > 0" class="calendar-content">
                  <div class="attendance-summary">
                    <span class="count-badge">{{ getDateAttendanceCount(data.day) }}人</span>
                    <span v-if="getPendingCount(data.day) > 0" class="pending-badge">
                      {{ getPendingCount(data.day) }}待确认
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </el-calendar>
        </el-card>
      </el-col>
      <!-- 右侧：统计面板 -->
      <el-col :span="8">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
              <div class="stat-item">
                <div class="stat-label">待确认数量</div>
                <div class="stat-value" style="color: #e6a23c; cursor: pointer" @click="handleViewPending">
                  {{ statistics.pendingCount || 0 }}
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
              <div class="stat-item">
                <div class="stat-label">异常考勤</div>
                <div class="stat-value" style="color: #f56c6c">{{ statistics.abnormalCount || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
              <div class="stat-item">
                <div class="stat-label">今日出勤率</div>
                <div class="stat-value" style="color: #409eff">
                  {{ statistics.todayAttendanceRate ? statistics.todayAttendanceRate.toFixed(2) + '%' : '0%' }}
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
              <div class="stat-item">
                <div class="stat-label">本周出勤率</div>
                <div class="stat-value" style="color: #67c23a">
                  {{ statistics.weekAttendanceRate ? statistics.weekAttendanceRate.toFixed(2) + '%' : '0%' }}
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover" style="margin-bottom: 12px">
              <div class="stat-item">
                <div class="stat-label">总考勤记录</div>
                <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="hover">
              <div class="stat-item">
                <div class="stat-label">已确认记录</div>
                <div class="stat-value" style="color: #67c23a">{{ statistics.confirmedCount || 0 }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>

    <!-- 日期考勤列表对话框 -->
    <el-dialog
      v-model="dateListDialogVisible"
      :title="`${selectedDate} 考勤记录`"
      width="1000px"
    >
      <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center">
        <div>
          <el-checkbox v-model="selectAll" @change="handleSelectAll">全选</el-checkbox>
          <span style="margin-left: 12px; color: #606266; font-size: 14px">
            已选择 {{ selectedAttendanceIds.length }} 条
          </span>
        </div>
        <div>
          <el-button
            type="success"
            size="small"
            :disabled="selectedAttendanceIds.length === 0"
            @click="handleBatchConfirm"
          >
            批量确认
          </el-button>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedAttendanceIds.length === 0"
            @click="handleBatchReject"
          >
            批量拒绝
          </el-button>
        </div>
      </div>
      <el-table
        :data="dateAttendanceList"
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" min-width="120" />
        <el-table-column prop="studentNo" label="学号" min-width="120" />
        <el-table-column prop="attendanceType" label="考勤类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getAttendanceTypeTagType(row.attendanceType)" size="small">
              {{ getAttendanceTypeText(row.attendanceType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInTime" label="上班打卡" width="150">
          <template #default="{ row }">
            {{ row.checkInTime ? formatTime(row.checkInTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="checkOutTime" label="下班打卡" width="150">
          <template #default="{ row }">
            {{ row.checkOutTime ? formatTime(row.checkOutTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="workHours" label="工作时长" width="100" align="center">
          <template #default="{ row }">
            {{ row.workHours ? `${row.workHours}小时` : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="confirmStatus" label="确认状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getConfirmStatusTagType(row.confirmStatus)" size="small">
              {{ getConfirmStatusText(row.confirmStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button
              v-if="row.confirmStatus === 0 && canReview(row)"
              link
              type="primary"
              size="small"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>
            <el-button
              v-if="row.confirmStatus === 0 && canReview(row)"
              link
              type="success"
              size="small"
              @click="handleConfirm(row)"
            >
              确认
            </el-button>
            <el-button
              v-if="row.confirmStatus === 0 && canReview(row)"
              link
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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
        :rules="getFormRules()"
        label-width="120px"
      >
        <el-form-item label="学生">
          <el-select
            v-model="formData.studentId"
            placeholder="请选择学生"
            filterable
            style="width: 100%"
            :disabled="!!formData.attendanceId"
            @change="handleStudentChange"
          >
            <el-option
              v-for="student in studentList"
              :key="student.studentId"
              :label="`${student.studentName}（${student.studentNo}）`"
              :value="student.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.studentName" label="学生姓名">
          <el-input :value="formData.studentName" disabled />
        </el-form-item>
        <el-form-item label="考勤日期" prop="attendanceDate">
          <el-date-picker
            v-model="formData.attendanceDate"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="考勤类型" prop="attendanceType">
          <el-radio-group v-model="formData.attendanceType">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">迟到</el-radio>
            <el-radio :label="3">早退</el-radio>
            <el-radio :label="4">请假</el-radio>
            <el-radio :label="5">缺勤</el-radio>
            <el-radio :label="6">休息</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="formData.attendanceType !== 4 && formData.attendanceType !== 6" 
          label="上班打卡时间" 
          prop="checkInTime"
        >
          <el-date-picker
            v-model="formData.checkInTime"
            type="datetime"
            placeholder="请选择上班打卡时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item 
          v-if="formData.attendanceType !== 4 && formData.attendanceType !== 6" 
          label="下班打卡时间" 
          prop="checkOutTime"
        >
          <el-date-picker
            v-model="formData.checkOutTime"
            type="datetime"
            placeholder="请选择下班打卡时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item 
          v-if="formData.attendanceType !== 4 && formData.attendanceType !== 6" 
          label="工作时长（小时）" 
          prop="workHours"
        >
          <el-input-number
            v-model="formData.workHours"
            :min="0"
            :max="24"
            :precision="1"
            style="width: 100%"
            placeholder="请输入工作时长"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 确认考勤对话框 -->
    <el-dialog
      v-model="confirmDialogVisible"
      title="确认考勤"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="confirmFormRef"
        :model="confirmForm"
        :rules="getConfirmFormRules()"
        label-width="120px"
      >
        <el-form-item label="学生姓名">
          <el-input :value="confirmForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input :value="confirmForm.studentNo" disabled />
        </el-form-item>
        <el-form-item label="考勤日期">
          <el-input :value="formatDate(confirmForm.attendanceDate)" disabled />
        </el-form-item>
        <el-form-item label="考勤类型">
          <el-tag :type="getAttendanceTypeTagType(confirmForm.attendanceType)" size="small">
            {{ getAttendanceTypeText(confirmForm.attendanceType) }}
          </el-tag>
        </el-form-item>
        <el-form-item 
          v-if="confirmForm.attendanceType !== 4 && confirmForm.attendanceType !== 6" 
          label="上班打卡时间" 
          prop="checkInTime"
        >
          <el-date-picker
            v-model="confirmForm.checkInTime"
            type="datetime"
            placeholder="请选择上班打卡时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item 
          v-if="confirmForm.attendanceType !== 4 && confirmForm.attendanceType !== 6" 
          label="下班打卡时间" 
          prop="checkOutTime"
        >
          <el-date-picker
            v-model="confirmForm.checkOutTime"
            type="datetime"
            placeholder="请选择下班打卡时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="确认状态" prop="confirmStatus">
          <el-radio-group v-model="confirmForm.confirmStatus">
            <el-radio :label="1">已确认</el-radio>
            <el-radio :label="2">已拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input
            v-model="confirmForm.confirmComment"
            type="textarea"
            :rows="3"
            placeholder="请输入确认意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirmLoading" @click="handleSubmitConfirm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量确认/拒绝对话框 -->
    <el-dialog
      v-model="batchConfirmDialogVisible"
      :title="batchConfirmAction === 'confirm' ? '批量确认考勤' : '批量拒绝考勤'"
      width="500px"
    >
      <el-form :model="batchConfirmForm" label-width="100px">
        <el-form-item label="选择数量">
          <el-input :value="`${selectedAttendanceIds.length} 条记录`" disabled />
        </el-form-item>
        <el-form-item label="确认意见">
          <el-input
            v-model="batchConfirmForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="batchConfirmAction === 'confirm' ? '请输入确认意见' : '请输入拒绝原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchConfirmDialogVisible = false">取消</el-button>
        <el-button
          :type="batchConfirmAction === 'confirm' ? 'success' : 'danger'"
          :loading="batchConfirmLoading"
          @click="handleSubmitBatchConfirm"
        >
          {{ batchConfirmAction === 'confirm' ? '确认' : '拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤详情"
      width="700px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="考勤日期">
          {{ formatDate(detailData.attendanceDate) }}
        </el-descriptions-item>
        <el-descriptions-item label="考勤类型">
          <el-tag :type="getAttendanceTypeTagType(detailData.attendanceType)" size="small">
            {{ getAttendanceTypeText(detailData.attendanceType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上班打卡时间">
          {{ detailData.checkInTime ? formatDateTime(detailData.checkInTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="下班打卡时间">
          {{ detailData.checkOutTime ? formatDateTime(detailData.checkOutTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="工作时长">
          {{ detailData.workHours ? `${detailData.workHours}小时` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="确认状态">
          <el-tag :type="getConfirmStatusTagType(detailData.confirmStatus)" size="small">
            {{ getConfirmStatusText(detailData.confirmStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.remark" label="备注" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.remark }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.confirmComment" label="确认意见" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.confirmComment }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.updateTime" label="更新时间">
          {{ formatDateTime(detailData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 批量确认对话框（原有功能保留） -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量确认考勤"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="getBatchFormRules()"
        label-width="120px"
      >
        <el-form-item label="考勤日期" prop="attendanceDate">
          <el-date-picker
            v-model="batchForm.attendanceDate"
            type="date"
            placeholder="请选择日期"
            style="width: 100%"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="选择学生" prop="studentIds">
          <el-select
            v-model="batchForm.studentIds"
            multiple
            placeholder="请选择学生"
            style="width: 100%"
          >
            <el-option
              v-for="student in studentList"
              :key="student.studentId"
              :label="`${student.studentName}（${student.studentNo}）`"
              :value="student.studentId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="考勤类型" prop="attendanceType">
          <el-radio-group v-model="batchForm.attendanceType">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">迟到</el-radio>
            <el-radio :label="3">早退</el-radio>
            <el-radio :label="4">请假</el-radio>
            <el-radio :label="5">缺勤</el-radio>
            <el-radio :label="6">休息</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item 
          v-if="batchForm.attendanceType !== 4 && batchForm.attendanceType !== 6" 
          label="工作时长（小时）" 
          prop="workHours"
        >
          <el-input-number
            v-model="batchForm.workHours"
            :min="0"
            :max="24"
            :precision="1"
            style="width: 100%"
            placeholder="请输入工作时长"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="batchLoading" @click="handleSubmitBatch">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'

// 根据角色动态显示页面标题
const pageTitle = computed(() => {
  if (hasAnyRole(['ROLE_ENTERPRISE_ADMIN'])) {
    return '考勤管理'
  } else if (hasAnyRole(['ROLE_ENTERPRISE_MENTOR'])) {
    return '学生考勤'
  }
  return '考勤管理'
})

const loading = ref(false)
const submitLoading = ref(false)
const batchLoading = ref(false)
const confirmLoading = ref(false)
const batchConfirmLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const batchConfirmDialogVisible = ref(false)
const dateListDialogVisible = ref(false)
const dialogTitle = ref('确认考勤')
const formRef = ref(null)
const batchFormRef = ref(null)
const confirmFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
  studentNo: ''
})

const calendarDate = ref(new Date())
const selectedDate = ref('')
const dateAttendanceList = ref([])
const selectedAttendanceIds = ref([])
const selectAll = ref(false)
const allAttendanceData = ref([])
const attendanceMap = ref(new Map())

const detailData = ref({})
const studentList = ref([])

const formData = reactive({
  attendanceId: null,
  applyId: null,
  studentId: null,
  studentName: '',
  attendanceDate: '',
  checkInTime: '',
  checkOutTime: '',
  attendanceType: 1,
  workHours: 8,
  remark: ''
})

const batchForm = reactive({
  attendanceDate: '',
  studentIds: [],
  attendanceType: 1,
  workHours: 8
})

const confirmForm = reactive({
  attendanceId: null,
  studentName: '',
  studentNo: '',
  attendanceDate: '',
  attendanceType: null,
  checkInTime: '',
  checkOutTime: '',
  confirmStatus: 1,
  confirmComment: ''
})

const batchConfirmForm = reactive({
  comment: ''
})

const batchConfirmAction = ref('confirm') // 'confirm' or 'reject'

const statistics = ref({
  pendingCount: 0,
  abnormalCount: 0,
  todayAttendanceRate: 0,
  weekAttendanceRate: 0,
  totalCount: 0,
  confirmedCount: 0
})

// 动态验证规则：根据考勤类型决定是否需要签到签退时间
const getConfirmFormRules = () => {
  const rules = {
    confirmStatus: [{ required: true, message: '请选择确认状态', trigger: 'change' }]
  }
  // 请假(4)和休息(6)不需要签到签退时间
  if (confirmForm.attendanceType !== 4 && confirmForm.attendanceType !== 6) {
    rules.checkInTime = [{ required: true, message: '请选择上班打卡时间', trigger: 'change' }]
    rules.checkOutTime = [{ required: true, message: '请选择下班打卡时间', trigger: 'change' }]
  }
  return rules
}

// 动态验证规则：根据考勤类型决定是否需要签到签退时间和工作时长
const getFormRules = () => {
  const rules = {
    studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
    attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
    attendanceType: [{ required: true, message: '请选择考勤类型', trigger: 'change' }]
  }
  // 请假(4)和休息(6)不需要签到签退时间和工作时长
  if (formData.attendanceType !== 4 && formData.attendanceType !== 6) {
    rules.checkInTime = [{ required: true, message: '请选择上班打卡时间', trigger: 'change' }]
    rules.checkOutTime = [{ required: true, message: '请选择下班打卡时间', trigger: 'change' }]
    rules.workHours = [{ required: true, message: '请输入工作时长', trigger: 'blur' }]
  }
  return rules
}

// 动态验证规则：根据考勤类型决定是否需要工作时长
const getBatchFormRules = () => {
  const rules = {
    attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
    studentIds: [{ required: true, message: '请选择学生', trigger: 'change' }],
    attendanceType: [{ required: true, message: '请选择考勤类型', trigger: 'change' }]
  }
  // 请假(4)和休息(6)不需要工作时长
  if (batchForm.attendanceType !== 4 && batchForm.attendanceType !== 6) {
    rules.workHours = [{ required: true, message: '请输入工作时长', trigger: 'blur' }]
  }
  return rules
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    // 加载所有考勤数据用于日历显示
    const res = await attendanceApi.getAttendancePage({
      current: 1,
      size: 1000,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined
    })
    if (res.code === 200) {
      allAttendanceData.value = res.data.records || []
      
      // 构建考勤数据映射（按日期）
      attendanceMap.value.clear()
      allAttendanceData.value.forEach(item => {
        if (item.attendanceDate) {
          const date = item.attendanceDate
          if (!attendanceMap.value.has(date)) {
            attendanceMap.value.set(date, [])
          }
          attendanceMap.value.get(date).push(item)
        }
      })
      
      // 计算统计数据
      calculateStatistics()
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 计算统计数据
const calculateStatistics = () => {
  const today = new Date().toISOString().split('T')[0]
  const todayList = attendanceMap.value.get(today) || []
  
  // 待确认数量
  statistics.value.pendingCount = allAttendanceData.value.filter(item => item.confirmStatus === 0).length
  
  // 异常考勤（迟到、早退、缺勤）
  statistics.value.abnormalCount = allAttendanceData.value.filter(item => 
    item.attendanceType === 2 || item.attendanceType === 3 || item.attendanceType === 5
  ).length
  
  // 今日出勤率
  const todayTotal = todayList.length
  const todayNormal = todayList.filter(item => item.attendanceType === 1 && item.confirmStatus === 1).length
  statistics.value.todayAttendanceRate = todayTotal > 0 ? (todayNormal / todayTotal) * 100 : 0
  
  // 本周出勤率（简化计算，使用所有数据）
  const totalCount = allAttendanceData.value.length
  const normalCount = allAttendanceData.value.filter(item => 
    item.attendanceType === 1 && item.confirmStatus === 1
  ).length
  statistics.value.weekAttendanceRate = totalCount > 0 ? (normalCount / totalCount) * 100 : 0
  
  // 总记录数和已确认数
  statistics.value.totalCount = allAttendanceData.value.length
  statistics.value.confirmedCount = allAttendanceData.value.filter(item => item.confirmStatus === 1).length
}

// 获取日期考勤数量
const getDateAttendanceCount = (dateStr) => {
  if (!dateStr) return 0
  const dateOnly = dateStr.split(' ')[0]
  const list = attendanceMap.value.get(dateOnly) || []
  return list.length
}

// 获取日期待确认数量
const getPendingCount = (dateStr) => {
  if (!dateStr) return 0
  const dateOnly = dateStr.split(' ')[0]
  const list = attendanceMap.value.get(dateOnly) || []
  return list.filter(item => item.confirmStatus === 0).length
}

// 点击日期
const handleDateClick = (dateStr) => {
  if (!dateStr) return
  const dateOnly = dateStr.split(' ')[0]
  selectedDate.value = dateOnly
  dateAttendanceList.value = attendanceMap.value.get(dateOnly) || []
  selectedAttendanceIds.value = []
  selectAll.value = false
  dateListDialogVisible.value = true
}

// 全选/取消全选
const handleSelectAll = (checked) => {
  if (checked) {
    selectedAttendanceIds.value = dateAttendanceList.value
      .filter(item => item.confirmStatus === 0)
      .map(item => item.attendanceId)
  } else {
    selectedAttendanceIds.value = []
  }
}

// 选择变化
const handleSelectionChange = (selection) => {
  selectedAttendanceIds.value = selection.map(item => item.attendanceId)
  selectAll.value = selectedAttendanceIds.value.length > 0 && 
    selectedAttendanceIds.value.length === dateAttendanceList.value.filter(item => item.confirmStatus === 0).length
}

// 批量确认
const handleBatchConfirm = () => {
  if (selectedAttendanceIds.value.length === 0) {
    ElMessage.warning('请选择要确认的考勤记录')
    return
  }
  batchConfirmAction.value = 'confirm'
  batchConfirmForm.comment = ''
  batchConfirmDialogVisible.value = true
}

// 批量拒绝
const handleBatchReject = () => {
  if (selectedAttendanceIds.value.length === 0) {
    ElMessage.warning('请选择要拒绝的考勤记录')
    return
  }
  batchConfirmAction.value = 'reject'
  batchConfirmForm.comment = ''
  batchConfirmDialogVisible.value = true
}

// 提交批量确认/拒绝
const handleSubmitBatchConfirm = async () => {
  if (batchConfirmAction.value === 'reject' && !batchConfirmForm.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }
  
  batchConfirmLoading.value = true
  try {
    const confirmStatus = batchConfirmAction.value === 'confirm' ? 1 : 2
    let successCount = 0
    let failCount = 0
    
    for (const attendanceId of selectedAttendanceIds.value) {
      try {
        await attendanceApi.confirmAttendance(
          attendanceId,
          confirmStatus,
          batchConfirmForm.comment || undefined
        )
        successCount++
      } catch (error) {
        failCount++
        console.error(`确认考勤 ${attendanceId} 失败:`, error)
      }
    }
    
    if (successCount > 0) {
      ElMessage.success(`${batchConfirmAction.value === 'confirm' ? '确认' : '拒绝'}成功 ${successCount} 条`)
    }
    if (failCount > 0) {
      ElMessage.warning(`失败 ${failCount} 条`)
    }
    
    batchConfirmDialogVisible.value = false
    selectedAttendanceIds.value = []
    selectAll.value = false
    await loadData()
    // 刷新日期列表
    if (dateListDialogVisible.value) {
      handleDateClick(selectedDate.value)
    }
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    batchConfirmLoading.value = false
  }
}

// 查看待确认
const handleViewPending = () => {
  const pendingList = allAttendanceData.value.filter(item => item.confirmStatus === 0)
  if (pendingList.length > 0) {
    // 显示第一个待确认的日期
    const firstPending = pendingList[0]
    handleDateClick(firstPending.attendanceDate)
  } else {
    ElMessage.info('暂无待确认的考勤记录')
  }
}

// 加载学生列表
const loadStudentList = async () => {
  try {
    // 首先尝试从考勤数据中提取学生信息（所有角色都可以）
    const res = await attendanceApi.getAttendancePage({
      current: 1,
      size: 1000
    })
    if (res.code === 200) {
      // 去重学生
      const studentMap = new Map()
      res.data.records.forEach(attendance => {
        if (attendance.studentId && !studentMap.has(attendance.studentId)) {
          studentMap.set(attendance.studentId, {
            studentId: attendance.studentId,
            studentName: attendance.studentName || '',
            studentNo: attendance.studentNo || ''
          })
        }
      })
      studentList.value = Array.from(studentMap.values())
    }
    
    // 如果从考勤数据中没有获取到学生，尝试其他方式
    if (studentList.value.length === 0) {
      // 企业导师使用专门的API
      try {
        const mentorRes = await applyApi.getMentorStudents({
          current: 1,
          size: 1000
        })
        if (mentorRes.code === 200) {
          const mentorStudentMap = new Map()
          mentorRes.data.records.forEach(apply => {
            if (apply.studentId && !mentorStudentMap.has(apply.studentId)) {
              mentorStudentMap.set(apply.studentId, {
                studentId: apply.studentId,
                studentName: apply.studentName || '',
                studentNo: apply.studentNo || ''
              })
            }
          })
          studentList.value = Array.from(mentorStudentMap.values())
        }
      } catch (mentorError) {
        // 如果不是企业导师或API失败，尝试企业管理员的方式
        try {
          const applyRes = await applyApi.getApplyPage({
            current: 1,
            size: 1000,
            status: 1 // 已通过的申请
          })
          if (applyRes.code === 200) {
            const applyStudentMap = new Map()
            applyRes.data.records.forEach(apply => {
              if (apply.studentId && !applyStudentMap.has(apply.studentId)) {
                applyStudentMap.set(apply.studentId, {
                  studentId: apply.studentId,
                  studentName: apply.studentName,
                  studentNo: apply.studentNo
                })
              }
            })
            studentList.value = Array.from(applyStudentMap.values())
          }
        } catch (applyError) {
          console.warn('无法从申请接口获取学生列表（可能是权限问题）:', applyError)
        }
      }
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    // 如果考勤接口失败，尝试企业导师API
    try {
      const mentorRes = await applyApi.getMentorStudents({
        current: 1,
        size: 1000
      })
      if (mentorRes.code === 200) {
        const mentorStudentMap = new Map()
        mentorRes.data.records.forEach(apply => {
          if (apply.studentId && !mentorStudentMap.has(apply.studentId)) {
            mentorStudentMap.set(apply.studentId, {
              studentId: apply.studentId,
              studentName: apply.studentName || '',
              studentNo: apply.studentNo || ''
            })
          }
        })
        studentList.value = Array.from(mentorStudentMap.values())
      }
    } catch (mentorError) {
      // 最后尝试企业管理员的方式
      try {
        const applyRes = await applyApi.getApplyPage({
          current: 1,
          size: 1000,
          status: 1
        })
        if (applyRes.code === 200) {
          const studentMap = new Map()
          applyRes.data.records.forEach(apply => {
            if (apply.studentId && !studentMap.has(apply.studentId)) {
              studentMap.set(apply.studentId, {
                studentId: apply.studentId,
                studentName: apply.studentName,
                studentNo: apply.studentNo
              })
            }
          })
          studentList.value = Array.from(studentMap.values())
        }
      } catch (applyError) {
        console.error('从申请接口获取学生列表也失败:', applyError)
      }
    }
  }
}

// 搜索
const handleSearch = () => {
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  handleSearch()
}

// 添加
const handleAdd = async () => {
  await loadStudentList()
  dialogTitle.value = '确认考勤'
  resetForm()
  dialogVisible.value = true
}

// 批量添加
const handleBatchAdd = async () => {
  await loadStudentList()
  resetBatchForm()
  batchDialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  await loadStudentList()
  dialogTitle.value = '编辑考勤'
  try {
    const res = await attendanceApi.getAttendanceById(row.attendanceId)
    if (res.code === 200) {
      Object.assign(formData, {
        attendanceId: res.data.attendanceId,
        applyId: res.data.applyId,
        studentId: res.data.studentId,
        studentName: res.data.studentName || '',
        attendanceDate: res.data.attendanceDate,
        checkInTime: res.data.checkInTime || '',
        checkOutTime: res.data.checkOutTime || '',
        attendanceType: res.data.attendanceType || 1,
        workHours: res.data.workHours || 8,
        remark: res.data.remark || ''
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await attendanceApi.getAttendanceById(row.attendanceId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 确认考勤
const handleConfirm = async (row) => {
  try {
    const res = await attendanceApi.getAttendanceById(row.attendanceId)
    if (res.code === 200) {
      Object.assign(confirmForm, {
        attendanceId: res.data.attendanceId,
        studentName: res.data.studentName || '',
        studentNo: res.data.studentNo || '',
        attendanceDate: res.data.attendanceDate,
        attendanceType: res.data.attendanceType,
        checkInTime: res.data.checkInTime || '',
        checkOutTime: res.data.checkOutTime || '',
        confirmStatus: 1,
        confirmComment: ''
      })
      confirmDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 拒绝考勤
const handleReject = async (row) => {
  try {
    const res = await attendanceApi.getAttendanceById(row.attendanceId)
    if (res.code === 200) {
      Object.assign(confirmForm, {
        attendanceId: res.data.attendanceId,
        studentName: res.data.studentName || '',
        studentNo: res.data.studentNo || '',
        attendanceDate: res.data.attendanceDate,
        attendanceType: res.data.attendanceType,
        checkInTime: res.data.checkInTime || '',
        checkOutTime: res.data.checkOutTime || '',
        confirmStatus: 2,
        confirmComment: ''
      })
      confirmDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 提交确认
const handleSubmitConfirm = async () => {
  if (!confirmFormRef.value) return
  await confirmFormRef.value.validate(async (valid) => {
    if (valid) {
      confirmLoading.value = true
      try {
        // 请假和休息类型不需要传递签到签退时间
        const checkInTime = (confirmForm.attendanceType === 4 || confirmForm.attendanceType === 6) 
          ? undefined 
          : (confirmForm.checkInTime || undefined)
        const checkOutTime = (confirmForm.attendanceType === 4 || confirmForm.attendanceType === 6) 
          ? undefined 
          : (confirmForm.checkOutTime || undefined)
        
        const res = await attendanceApi.confirmAttendance(
          confirmForm.attendanceId,
          confirmForm.confirmStatus,
          confirmForm.confirmComment || undefined,
          checkInTime,
          checkOutTime
        )
        if (res.code === 200) {
          ElMessage.success('操作成功')
          confirmDialogVisible.value = false
          await loadData()
          // 刷新日期列表
          if (dateListDialogVisible.value) {
            handleDateClick(selectedDate.value)
          }
        }
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        confirmLoading.value = false
      }
    }
  })
}

// 学生选择变化
const handleStudentChange = async (studentId) => {
  const student = studentList.value.find(item => item.studentId === studentId)
  if (student) {
    formData.studentName = student.studentName || ''
    
    // 自动查询该学生的已通过申请，获取applyId
    if (studentId && !formData.attendanceId) {
      try {
        // 先从考勤数据中查找该学生的applyId
        const attendance = allAttendanceData.value.find(item => item.studentId === studentId)
        if (attendance && attendance.applyId) {
          formData.applyId = attendance.applyId
        } else {
          // 如果考勤数据中没有，尝试从申请接口获取（仅企业管理员）
          try {
            const res = await applyApi.getApplyPage({
              current: 1,
              size: 100,
              studentId: studentId,
              status: 1 // 已通过的申请
            })
            if (res.code === 200 && res.data.records && res.data.records.length > 0) {
              // 如果有多个申请，使用最新的一个
              const latestApply = res.data.records[0]
              formData.applyId = latestApply.applyId
            } else {
              ElMessage.warning('该学生没有已通过的实习申请，无法添加考勤')
              formData.applyId = null
            }
          } catch (applyError) {
            // 企业导师可能没有权限访问申请接口，忽略错误
            console.warn('无法从申请接口获取applyId（可能是权限问题）:', applyError)
            // 尝试从考勤数据中获取
            if (attendance && attendance.applyId) {
              formData.applyId = attendance.applyId
            } else {
              ElMessage.warning('无法获取该学生的申请信息，请手动选择')
              formData.applyId = null
            }
          }
        }
      } catch (error) {
        console.error('查询申请失败:', error)
        ElMessage.error('查询申请失败')
        formData.applyId = null
      }
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = {
          ...formData,
          remark: formData.remark || undefined
        }
        // 请假(4)和休息(6)不需要签到签退时间和工作时长
        if (formData.attendanceType === 4 || formData.attendanceType === 6) {
          data.checkInTime = undefined
          data.checkOutTime = undefined
          data.workHours = undefined
        }
        if (formData.attendanceId) {
          data.attendanceId = formData.attendanceId
          const res = await attendanceApi.updateAttendance(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            await loadData()
            // 刷新日期列表
            if (dateListDialogVisible.value) {
              handleDateClick(selectedDate.value)
            }
          }
        } else {
          const res = await attendanceApi.addAttendance(data)
          if (res.code === 200) {
            ElMessage.success('确认成功')
            dialogVisible.value = false
            await loadData()
          }
        }
      } catch (error) {
        console.error('提交失败:', error)
        ElMessage.error(error.response?.data?.message || '提交失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 提交批量确认
const handleSubmitBatch = async () => {
  if (!batchFormRef.value) return
  await batchFormRef.value.validate(async (valid) => {
    if (valid) {
      batchLoading.value = true
      try {
        // 为每个学生查询对应的申请ID
        const attendanceList = []
        for (const studentId of batchForm.studentIds) {
          let applyId = null
          
          // 先从考勤数据中查找该学生的applyId
          const attendance = allAttendanceData.value.find(item => item.studentId === studentId)
          if (attendance && attendance.applyId) {
            applyId = attendance.applyId
          } else {
            // 如果考勤数据中没有，尝试从申请接口获取（仅企业管理员）
            try {
              const res = await applyApi.getApplyPage({
                current: 1,
                size: 1,
                studentId: studentId,
                status: 1 // 已通过的申请
              })
              if (res.code === 200 && res.data.records && res.data.records.length > 0) {
                applyId = res.data.records[0].applyId
              }
            } catch (applyError) {
              // 企业导师可能没有权限访问申请接口，忽略错误
              console.warn(`无法从申请接口获取学生 ${studentId} 的applyId:`, applyError)
            }
          }
          
          if (applyId) {
            const attendanceItem = {
              studentId,
              applyId: applyId,
              attendanceDate: batchForm.attendanceDate,
              attendanceType: batchForm.attendanceType
            }
            // 请假(4)和休息(6)不需要工作时长
            if (batchForm.attendanceType !== 4 && batchForm.attendanceType !== 6) {
              attendanceItem.workHours = batchForm.workHours
            }
            attendanceList.push(attendanceItem)
          } else {
            ElMessage.warning(`学生ID ${studentId} 没有已通过的实习申请，已跳过`)
          }
        }
        
        if (attendanceList.length === 0) {
          ElMessage.warning('没有可添加的考勤记录')
          batchLoading.value = false
          return
        }
        
        const res = await attendanceApi.batchAddAttendance(attendanceList)
        if (res.code === 200) {
          ElMessage.success('批量确认成功')
          batchDialogVisible.value = false
          await loadData()
        }
      } catch (error) {
        console.error('批量确认失败:', error)
        ElMessage.error(error.response?.data?.message || '批量确认失败')
      } finally {
        batchLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    attendanceId: null,
    applyId: null,
    studentId: null,
    studentName: '',
    attendanceDate: '',
    checkInTime: '',
    checkOutTime: '',
    attendanceType: 1,
    workHours: 8,
    remark: ''
  })
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

// 重置批量表单
const resetBatchForm = () => {
  Object.assign(batchForm, {
    attendanceDate: '',
    studentIds: [],
    attendanceType: 1,
    workHours: 8
  })
  if (batchFormRef.value) {
    batchFormRef.value.clearValidate()
  }
}

// 格式化时间（只显示时分）
const formatTime = (dateTimeStr) => {
  if (!dateTimeStr) return ''
  const date = new Date(dateTimeStr)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 获取考勤类型文本
const getAttendanceTypeText = (type) => {
  const typeMap = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '请假',
    5: '缺勤',
    6: '休息'
  }
  return typeMap[type] || '-'
}

// 获取考勤类型标签类型
const getAttendanceTypeTagType = (type) => {
  const typeMap = {
    1: 'success',
    2: 'warning',
    3: 'warning',
    4: 'info',
    5: 'danger',
    6: 'info'
  }
  return typeMap[type] || 'info'
}

// 获取确认状态文本
const getConfirmStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '已拒绝'
  }
  return statusMap[status] || '-'
}

// 获取确认状态标签类型
const getConfirmStatusTagType = (status) => {
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'danger'
  }
  return statusMap[status] || 'info'
}

// 判断是否可以审核考勤
const canReview = (row) => {
  const authStore = useAuthStore()
  const roles = authStore.roles || []
  
  // 系统管理员和学校管理员可以审核所有
  if (roles.includes('ROLE_SYSTEM_ADMIN') || roles.includes('ROLE_SCHOOL_ADMIN')) {
    return true
  }
  
  // 如果没有申请类型信息，默认允许企业导师审核（兼容旧数据）
  if (row.applyType === null || row.applyType === undefined) {
    // 企业导师和管理员可以审核（可能是旧数据，没有申请类型）
    return roles.includes('ROLE_ENTERPRISE_MENTOR') || roles.includes('ROLE_ENTERPRISE_ADMIN')
  }
  
  // 根据申请类型判断
  if (row.applyType === 1) {
    // 合作企业实习：企业导师可以审核
    return roles.includes('ROLE_ENTERPRISE_MENTOR') || roles.includes('ROLE_ENTERPRISE_ADMIN')
  } else if (row.applyType === 2) {
    // 自主实习：班主任可以审核（企业导师不应该看到，但为了安全还是判断一下）
    return roles.includes('ROLE_CLASS_TEACHER')
  }
  
  return false
}

// 初始化
onMounted(() => {
  loadData()
  loadStudentList()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 20px;
}

.search-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
  padding: 6px 0;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
}

.calendar-cell {
  height: 100%;
  padding: 1px 2px;
  cursor: pointer;
  transition: background-color 0.2s;
  overflow: hidden;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.calendar-date {
  font-size: 11px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}

.calendar-content {
  font-size: 10px;
}

.attendance-summary {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.count-badge {
  font-size: 10px;
  color: #409eff;
  font-weight: 500;
}

.pending-badge {
  font-size: 9px;
  color: #e6a23c;
  background-color: #fdf6ec;
  padding: 1px 4px;
  border-radius: 2px;
  display: inline-block;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
}

.legend-dot.normal {
  background-color: #67c23a;
}

.legend-dot.leave {
  background-color: #909399;
}

.legend-dot.rest {
  background-color: #409eff;
}

.legend-dot.pending {
  background-color: #e6a23c;
}

:deep(.el-calendar-day) {
  height: 50px;
  padding: 0;
}

:deep(.el-calendar__header) {
  padding: 8px 12px;
}

:deep(.el-calendar__body) {
  padding: 8px 12px;
}

:deep(.el-calendar-table) {
  font-size: 12px;
}

:deep(.el-calendar-table thead th) {
  padding: 6px 0;
  font-size: 12px;
}

:deep(.el-calendar-table td) {
  padding: 2px;
}
</style>
