<template>
  <PageLayout title="考勤管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">确认考勤</el-button>
      <el-button type="success" :icon="Plus" @click="handleBatchAdd">批量确认</el-button>
    </template>

    <!-- 搜索栏 -->
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
        <el-form-item label="考勤日期">
          <el-date-picker
            v-model="searchForm.attendanceDate"
            type="date"
            placeholder="请选择日期"
            clearable
            style="width: 200px"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="考勤类型">
          <el-select
            v-model="searchForm.attendanceType"
            placeholder="请选择类型"
            clearable
            style="width: 150px"
          >
            <el-option label="正常" :value="1" />
            <el-option label="迟到" :value="2" />
            <el-option label="早退" :value="3" />
            <el-option label="请假" :value="4" />
            <el-option label="缺勤" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 考勤列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="studentName" label="学生姓名" min-width="120" />
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="attendanceDate" label="考勤日期" width="120" align="center">
        <template #default="{ row }">
          {{ formatDate(row.attendanceDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="checkInTime" label="签到时间" width="180">
        <template #default="{ row }">
          {{ row.checkInTime ? formatDateTime(row.checkInTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="checkOutTime" label="签退时间" width="180">
        <template #default="{ row }">
          {{ row.checkOutTime ? formatDateTime(row.checkOutTime) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="attendanceType" label="考勤类型" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="getAttendanceTypeTagType(row.attendanceType)" size="small">
            {{ getAttendanceTypeText(row.attendanceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="workHours" label="工作时长" width="100" align="center">
        <template #default="{ row }">
          {{ row.workHours ? `${row.workHours}小时` : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="confirmStatus" label="确认状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.confirmStatus === 1 ? 'success' : 'warning'" size="small">
            {{ row.confirmStatus === 1 ? '已确认' : '待确认' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
          <el-button
            v-if="row.confirmStatus === 0"
            link
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            v-if="row.confirmStatus === 0"
            link
            type="success"
            size="small"
            @click="handleConfirm(row)"
          >
            确认
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
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
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
        <el-form-item label="签到时间" prop="checkInTime">
          <el-date-picker
            v-model="formData.checkInTime"
            type="datetime"
            placeholder="请选择签到时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="签退时间" prop="checkOutTime">
          <el-date-picker
            v-model="formData.checkOutTime"
            type="datetime"
            placeholder="请选择签退时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="考勤类型" prop="attendanceType">
          <el-radio-group v-model="formData.attendanceType">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="2">迟到</el-radio>
            <el-radio :label="3">早退</el-radio>
            <el-radio :label="4">请假</el-radio>
            <el-radio :label="5">缺勤</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="工作时长（小时）" prop="workHours">
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
            placeholder="请输入备注（可选）"
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
        :rules="confirmFormRules"
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
        <el-form-item label="签到时间" prop="checkInTime">
          <el-date-picker
            v-model="confirmForm.checkInTime"
            type="datetime"
            placeholder="请选择签到时间"
            style="width: 100%"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="签退时间" prop="checkOutTime">
          <el-date-picker
            v-model="confirmForm.checkOutTime"
            type="datetime"
            placeholder="请选择签退时间"
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
            placeholder="请输入确认意见（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="confirmLoading" @click="handleSubmitConfirm">确定</el-button>
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
        <el-descriptions-item label="签到时间">
          {{ detailData.checkInTime ? formatDateTime(detailData.checkInTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="签退时间">
          {{ detailData.checkOutTime ? formatDateTime(detailData.checkOutTime) : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="工作时长">
          {{ detailData.workHours ? `${detailData.workHours}小时` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="确认状态">
          <el-tag :type="detailData.confirmStatus === 1 ? 'success' : 'warning'" size="small">
            {{ detailData.confirmStatus === 1 ? '已确认' : '待确认' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.remark" label="备注" :span="2">
          <div style="white-space: pre-wrap">{{ detailData.remark }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detailData.updateTime" label="更新时间">
          {{ formatDateTime(detailData.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 批量确认对话框 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量确认考勤"
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="batchFormRef"
        :model="batchForm"
        :rules="batchFormRules"
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
          </el-radio-group>
        </el-form-item>
        <el-form-item label="工作时长（小时）" prop="workHours">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/internship/attendance'
import { applyApi } from '@/api/internship/apply'
import { formatDateTime, formatDate } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const submitLoading = ref(false)
const batchLoading = ref(false)
const confirmLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const batchDialogVisible = ref(false)
const confirmDialogVisible = ref(false)
const dialogTitle = ref('确认考勤')
const formRef = ref(null)
const batchFormRef = ref(null)
const confirmFormRef = ref(null)

const searchForm = reactive({
  studentName: '',
  studentNo: '',
  attendanceDate: null,
  attendanceType: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const tableData = ref([])
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
  checkInTime: '',
  checkOutTime: '',
  confirmStatus: 1,
  confirmComment: ''
})

const confirmFormRules = {
  checkInTime: [{ required: true, message: '请选择签到时间', trigger: 'change' }],
  checkOutTime: [{ required: true, message: '请选择签退时间', trigger: 'change' }],
  confirmStatus: [{ required: true, message: '请选择确认状态', trigger: 'change' }]
}

const formRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
  checkInTime: [{ required: true, message: '请选择签到时间', trigger: 'change' }],
  checkOutTime: [{ required: true, message: '请选择签退时间', trigger: 'change' }],
  attendanceType: [{ required: true, message: '请选择考勤类型', trigger: 'change' }],
  workHours: [{ required: true, message: '请输入工作时长', trigger: 'blur' }]
}

const batchFormRules = {
  attendanceDate: [{ required: true, message: '请选择考勤日期', trigger: 'change' }],
  studentIds: [{ required: true, message: '请选择学生', trigger: 'change' }],
  attendanceType: [{ required: true, message: '请选择考勤类型', trigger: 'change' }],
  workHours: [{ required: true, message: '请输入工作时长', trigger: 'blur' }]
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await attendanceApi.getAttendancePage({
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      attendanceDate: searchForm.attendanceDate || undefined,
      attendanceType: searchForm.attendanceType !== null ? searchForm.attendanceType : undefined
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

// 加载学生列表
const loadStudentList = async () => {
  try {
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 1000,
      status: 1 // 已通过的申请
    })
    if (res.code === 200) {
      // 去重学生
      const studentMap = new Map()
      res.data.records.forEach(apply => {
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
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.studentName = ''
  searchForm.studentNo = ''
  searchForm.attendanceDate = null
  searchForm.attendanceType = null
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

// 提交确认
const handleSubmitConfirm = async () => {
  if (!confirmFormRef.value) return
  await confirmFormRef.value.validate(async (valid) => {
    if (valid) {
      confirmLoading.value = true
      try {
        const res = await attendanceApi.confirmAttendance(
          confirmForm.attendanceId,
          confirmForm.confirmStatus,
          confirmForm.confirmComment || undefined,
          confirmForm.checkInTime || undefined,
          confirmForm.checkOutTime || undefined
        )
        if (res.code === 200) {
          ElMessage.success('确认成功')
          confirmDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('确认失败:', error)
        ElMessage.error(error.response?.data?.message || '确认失败')
      } finally {
        confirmLoading.value = false
      }
    }
  })
}

// 学生选择变化
const handleStudentChange = (studentId) => {
  const student = studentList.value.find(item => item.studentId === studentId)
  if (student) {
    formData.studentName = student.studentName || ''
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
        if (formData.attendanceId) {
          data.attendanceId = formData.attendanceId
          const res = await attendanceApi.updateAttendance(data)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          }
        } else {
          const res = await attendanceApi.addAttendance(data)
          if (res.code === 200) {
            ElMessage.success('确认成功')
            dialogVisible.value = false
            loadData()
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
        // 构建批量考勤数据
        const attendanceList = batchForm.studentIds.map(studentId => {
          const student = studentList.value.find(s => s.studentId === studentId)
          return {
            studentId,
            attendanceDate: batchForm.attendanceDate,
            attendanceType: batchForm.attendanceType,
            workHours: batchForm.workHours
          }
        })
        const res = await attendanceApi.batchAddAttendance({ attendanceList })
        if (res.code === 200) {
          ElMessage.success('批量确认成功')
          batchDialogVisible.value = false
          loadData()
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

// 分页处理
const handleSizeChange = () => {
  loadData()
}

const handlePageChange = () => {
  loadData()
}

// 获取考勤类型文本
const getAttendanceTypeText = (type) => {
  const typeMap = {
    1: '正常',
    2: '迟到',
    3: '早退',
    4: '请假',
    5: '缺勤'
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
    5: 'danger'
  }
  return typeMap[type] || 'info'
}

// 初始化
onMounted(() => {
  loadData()
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

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

