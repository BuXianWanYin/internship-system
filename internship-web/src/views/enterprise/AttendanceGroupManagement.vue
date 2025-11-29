<template>
  <PageLayout title="考勤组管理">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增考勤组</el-button>
    </template>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="考勤组名称">
          <el-input
            v-model="searchForm.groupName"
            placeholder="请输入考勤组名称"
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

    <!-- 考勤组列表 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column type="index" label="序号" width="60" align="center" />
      <el-table-column prop="groupName" label="考勤组名称" min-width="150" />
      <el-table-column prop="groupDescription" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column label="工作日类型" width="120" align="center">
        <template #default="{ row }">
          {{ getWorkDaysTypeText(row.workDaysType) }}
        </template>
      </el-table-column>
      <el-table-column label="时间段数量" width="100" align="center">
        <template #default="{ row }">
          {{ row.timeSlotCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="学生数量" width="100" align="center">
        <template #default="{ row }">
          {{ row.studentCount || 0 }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="300" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="success" size="small" @click="handleManageRules(row)">规则管理</el-button>
          <el-button link type="warning" size="small" @click="handleManageStudents(row)">学生管理</el-button>
          <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
      width="900px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="考勤组名称" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="请输入考勤组名称" />
        </el-form-item>
        <el-form-item label="考勤组描述" prop="groupDescription">
          <el-input
            v-model="formData.groupDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入考勤组描述"
          />
        </el-form-item>
        <el-form-item label="工作日类型" prop="workDaysType">
          <el-radio-group v-model="formData.workDaysType" @change="handleWorkDaysTypeChange">
            <el-radio :label="1">周一到周五</el-radio>
            <el-radio :label="2">周一到周六</el-radio>
            <el-radio :label="3">周一到周日</el-radio>
            <el-radio :label="4">自定义</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="formData.workDaysType === 4"
          label="自定义工作日"
          prop="workDaysConfig"
        >
          <el-checkbox-group v-model="customWorkDays">
            <el-checkbox label="monday">周一</el-checkbox>
            <el-checkbox label="tuesday">周二</el-checkbox>
            <el-checkbox label="wednesday">周三</el-checkbox>
            <el-checkbox label="thursday">周四</el-checkbox>
            <el-checkbox label="friday">周五</el-checkbox>
            <el-checkbox label="saturday">周六</el-checkbox>
            <el-checkbox label="sunday">周日</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="时间段配置" prop="timeSlots">
          <div class="time-slots-container">
            <div
              v-for="(slot, index) in formData.timeSlots"
              :key="index"
              class="time-slot-item"
            >
              <el-row :gutter="10" align="middle">
                <el-col :span="4">
                  <el-input
                    v-model="slot.slotName"
                    placeholder="时间段名称"
                    size="small"
                  />
                </el-col>
                <el-col :span="5">
                  <el-time-picker
                    v-model="slot.startTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="上班时间"
                    size="small"
                    style="width: 100%"
                    @change="() => calculateWorkHours(slot, index)"
                  />
                </el-col>
                <el-col :span="5">
                  <el-time-picker
                    v-model="slot.endTime"
                    format="HH:mm"
                    value-format="HH:mm"
                    placeholder="下班时间"
                    size="small"
                    style="width: 100%"
                    @change="() => calculateWorkHours(slot, index)"
                  />
                </el-col>
                <el-col :span="4">
                  <el-input-number
                    v-model="slot.workHours"
                    :min="0"
                    :max="24"
                    :precision="2"
                    placeholder="工作时长"
                    size="small"
                    style="width: 100%"
                  />
                </el-col>
                <el-col :span="3">
                  <el-checkbox v-model="slot.isDefault" :true-label="1" :false-label="0">
                    默认
                  </el-checkbox>
                </el-col>
                <el-col :span="3">
                  <el-button
                    type="danger"
                    :icon="Delete"
                    size="small"
                    @click="handleRemoveTimeSlot(index)"
                    :disabled="formData.timeSlots.length === 1"
                  />
                </el-col>
              </el-row>
            </div>
            <el-button
              type="primary"
              :icon="Plus"
              size="small"
              @click="handleAddTimeSlot"
              style="margin-top: 10px"
            >
              添加时间段
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="考勤组详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="考勤组名称">{{ detailData.groupName }}</el-descriptions-item>
        <el-descriptions-item label="工作日类型">
          {{ getWorkDaysTypeText(detailData.workDaysType) }}
        </el-descriptions-item>
        <el-descriptions-item label="考勤组描述" :span="2">
          {{ detailData.groupDescription || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="时间段数量">{{ detailData.timeSlotCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="学生数量">{{ detailData.studentCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
            {{ detailData.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="时间段列表" :span="2">
          <el-table :data="detailData.timeSlots || []" size="small" border>
            <el-table-column prop="slotName" label="时间段名称" width="120" />
            <el-table-column prop="startTime" label="上班时间" width="100" />
            <el-table-column prop="endTime" label="下班时间" width="100" />
            <el-table-column prop="workHours" label="工作时长（小时）" width="120" />
            <el-table-column label="是否默认" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault === 1" type="success" size="small">是</el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 规则管理对话框 -->
    <el-dialog
      v-model="rulesDialogVisible"
      title="规则管理"
      width="900px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 20px">
        <el-button type="primary" :icon="Plus" @click="handleAddRule">添加规则</el-button>
      </div>
      <el-table :data="rulesData" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column label="规则类型" width="120" align="center">
          <template #default="{ row }">
            {{ getRuleTypeText(row.ruleType) }}
          </template>
        </el-table-column>
        <el-table-column label="日期" min-width="150">
          <template #default="{ row }">
            <span v-if="row.ruleDate">{{ formatDate(row.ruleDate) }}</span>
            <span v-else-if="row.ruleStartDate && row.ruleEndDate">
              {{ formatDate(row.ruleStartDate) }} 至 {{ formatDate(row.ruleEndDate) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDeleteRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="rulesDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 添加规则对话框 -->
    <el-dialog
      v-model="ruleDialogVisible"
      title="添加规则"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleFormRules"
        label-width="120px"
      >
        <el-form-item label="规则类型" prop="ruleType">
          <el-radio-group v-model="ruleForm.ruleType">
            <el-radio :label="2">节假日</el-radio>
            <el-radio :label="3">特殊日期</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="ruleForm.ruleType === 2"
          label="日期范围"
          prop="dateRange"
        >
          <el-date-picker
            v-model="ruleForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          v-if="ruleForm.ruleType === 3"
          label="日期"
          prop="ruleDate"
        >
          <el-date-picker
            v-model="ruleForm.ruleDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item
          v-if="ruleForm.ruleType === 3"
          label="是否工作日"
          prop="isWorkday"
        >
          <el-radio-group v-model="ruleForm.isWorkday">
            <el-radio :label="true">是</el-radio>
            <el-radio :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="ruleForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSubmitLoading" @click="handleSubmitRule">确定</el-button>
      </template>
    </el-dialog>

    <!-- 学生管理对话框 -->
    <el-dialog
      v-model="studentsDialogVisible"
      title="学生管理"
      width="1000px"
      :close-on-click-modal="false"
    >
      <div style="margin-bottom: 20px">
        <el-button type="primary" :icon="Plus" @click="handleAssignStudents">分配学生</el-button>
      </div>
      <el-table :data="studentsData" border>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column label="生效开始日期" width="150">
          <template #default="{ row }">
            {{ formatDate(row.effectiveStartDate) }}
          </template>
        </el-table-column>
        <el-table-column label="生效结束日期" width="150">
          <template #default="{ row }">
            {{ row.effectiveEndDate ? formatDate(row.effectiveEndDate) : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleUnassignStudent(row)">解除关联</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="studentsDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 分配学生对话框 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配学生"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="assignFormRef"
        :model="assignForm"
        :rules="assignFormRules"
        label-width="120px"
      >
        <el-form-item label="选择学生" prop="applyIds">
          <el-select
            v-model="assignForm.applyIds"
            multiple
            placeholder="请选择学生"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="student in availableStudents"
              :key="student.applyId"
              :label="`${student.studentName} (${student.studentNo})`"
              :value="student.applyId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="生效开始日期" prop="effectiveStartDate">
          <el-date-picker
            v-model="assignForm.effectiveStartDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="生效结束日期" prop="effectiveEndDate">
          <el-date-picker
            v-model="assignForm.effectiveEndDate"
            type="date"
            placeholder="选择结束日期（可选）"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitLoading" @click="handleSubmitAssign">确定</el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, Delete } from '@element-plus/icons-vue'
import { attendanceGroupApi } from '@/api/internship/attendanceGroup'
import { applyApi } from '@/api/internship/apply'
import { enterpriseApi } from '@/api/user/enterprise'
import { useAuthStore } from '@/store/modules/auth'
import { formatDate, formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

// 搜索表单
const searchForm = reactive({
  groupName: ''
})

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增考勤组')
const detailDialogVisible = ref(false)
const rulesDialogVisible = ref(false)
const ruleDialogVisible = ref(false)
const studentsDialogVisible = ref(false)
const assignDialogVisible = ref(false)

// 表单数据
const formData = reactive({
  groupId: null,
  groupName: '',
  groupDescription: '',
  workDaysType: 1,
  workDaysConfig: '',
  timeSlots: [
    {
      slotName: '标准班',
      startTime: '09:00',
      endTime: '18:00',
      workHours: 8,
      isDefault: 1
    }
  ]
})

// 自定义工作日
const customWorkDays = ref([])

// 规则数据
const rulesData = ref([])
const currentGroupId = ref(null)

// 规则表单
const ruleForm = reactive({
  ruleType: 2,
  dateRange: null,
  ruleDate: null,
  isWorkday: true,
  description: ''
})

// 学生数据
const studentsData = ref([])
const availableStudents = ref([])

// 分配表单
const assignForm = reactive({
  applyIds: [],
  effectiveStartDate: null,
  effectiveEndDate: null
})

// 详情数据
const detailData = ref({})

// 提交状态
const submitLoading = ref(false)
const ruleSubmitLoading = ref(false)
const assignSubmitLoading = ref(false)

// 表单引用
const formRef = ref(null)
const ruleFormRef = ref(null)
const assignFormRef = ref(null)

// 表单验证规则
const formRules = {
  groupName: [{ required: true, message: '请输入考勤组名称', trigger: 'blur' }],
  workDaysType: [{ required: true, message: '请选择工作日类型', trigger: 'change' }],
  timeSlots: [
    {
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('至少需要配置一个时间段'))
          return
        }
        const defaultCount = value.filter(slot => slot.isDefault === 1).length
        if (defaultCount === 0) {
          callback(new Error('至少需要设置一个默认时间段'))
          return
        }
        if (defaultCount > 1) {
          callback(new Error('只能设置一个默认时间段'))
          return
        }
        for (const slot of value) {
          if (!slot.slotName) {
            callback(new Error('时间段名称不能为空'))
            return
          }
          if (!slot.startTime) {
            callback(new Error('上班时间不能为空'))
            return
          }
          if (!slot.endTime) {
            callback(new Error('下班时间不能为空'))
            return
          }
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const ruleFormRules = {
  ruleType: [{ required: true, message: '请选择规则类型', trigger: 'change' }],
  dateRange: [
    {
      validator: (rule, value, callback) => {
        if (ruleForm.ruleType === 2 && (!value || value.length !== 2)) {
          callback(new Error('请选择日期范围'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  ruleDate: [
    {
      validator: (rule, value, callback) => {
        if (ruleForm.ruleType === 3 && !value) {
          callback(new Error('请选择日期'))
          return
        }
        callback()
      },
      trigger: 'change'
    }
  ]
}

const assignFormRules = {
  applyIds: [{ required: true, message: '请选择学生', trigger: 'change' }],
  effectiveStartDate: [{ required: true, message: '请选择生效开始日期', trigger: 'change' }]
}

// 获取工作日类型文本
const getWorkDaysTypeText = (type) => {
  const map = {
    1: '周一到周五',
    2: '周一到周六',
    3: '周一到周日',
    4: '自定义'
  }
  return map[type] || '-'
}

// 获取规则类型文本
const getRuleTypeText = (type) => {
  const map = {
    1: '工作日',
    2: '节假日',
    3: '特殊日期'
  }
  return map[type] || '-'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      groupName: searchForm.groupName || undefined
    }
    const res = await attendanceGroupApi.getAttendanceGroupPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  searchForm.groupName = ''
  handleSearch()
}

// 分页
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

const handlePageChange = (current) => {
  pagination.current = current
  loadData()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增考勤组'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = async (row) => {
  dialogTitle.value = '编辑考勤组'
  try {
    const res = await attendanceGroupApi.getAttendanceGroupDetail(row.groupId)
    if (res.code === 200) {
      // 处理时间段数据，确保时间格式为 HH:mm（如果后端返回的是 HH:mm:ss，截取前5位）
      const timeSlots = (res.data.timeSlots || []).map(slot => ({
        ...slot,
        startTime: slot.startTime ? (slot.startTime.length > 5 ? slot.startTime.substring(0, 5) : slot.startTime) : '',
        endTime: slot.endTime ? (slot.endTime.length > 5 ? slot.endTime.substring(0, 5) : slot.endTime) : ''
      }))
      
      Object.assign(formData, {
        groupId: res.data.groupId,
        groupName: res.data.groupName,
        groupDescription: res.data.groupDescription || '',
        workDaysType: res.data.workDaysType,
        workDaysConfig: res.data.workDaysConfig || '',
        timeSlots: timeSlots
      })
      
      // 解析自定义工作日
      if (res.data.workDaysType === 4 && res.data.workDaysConfig) {
        try {
          const config = JSON.parse(res.data.workDaysConfig)
          customWorkDays.value = Object.keys(config).filter(key => config[key] === true)
        } catch (e) {
          customWorkDays.value = []
        }
      } else {
        customWorkDays.value = []
      }
      
      // 重新计算所有时间段的工作时长
      if (formData.timeSlots && formData.timeSlots.length > 0) {
        formData.timeSlots.forEach((slot, index) => {
          calculateWorkHours(slot, index)
        })
      }
      
      dialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
  }
}

// 查看
const handleView = async (row) => {
  try {
    const res = await attendanceGroupApi.getAttendanceGroupDetail(row.groupId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
  }
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该考勤组吗？', '提示', {
      type: 'warning'
    })
    const res = await attendanceGroupApi.deleteAttendanceGroup(row.groupId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 规则管理
const handleManageRules = async (row) => {
  currentGroupId.value = row.groupId
  try {
    const res = await attendanceGroupApi.getRuleList(row.groupId)
    if (res.code === 200) {
      rulesData.value = res.data || []
      rulesDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载规则失败：' + (error.message || '未知错误'))
  }
}

// 添加规则
const handleAddRule = () => {
  resetRuleForm()
  ruleDialogVisible.value = true
}

// 删除规则
const handleDeleteRule = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该规则吗？', '提示', {
      type: 'warning'
    })
    const res = await attendanceGroupApi.deleteRule(row.ruleId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      handleManageRules({ groupId: currentGroupId.value })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 提交规则
const handleSubmitRule = async () => {
  if (!ruleFormRef.value) return
  await ruleFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    ruleSubmitLoading.value = true
    try {
      const ruleData = {
        groupId: currentGroupId.value,
        ruleType: ruleForm.ruleType,
        description: ruleForm.description || ''
      }
      
      if (ruleForm.ruleType === 2) {
        // 节假日
        ruleData.ruleStartDate = ruleForm.dateRange[0]
        ruleData.ruleEndDate = ruleForm.dateRange[1]
      } else if (ruleForm.ruleType === 3) {
        // 特殊日期
        ruleData.ruleDate = ruleForm.ruleDate
        ruleData.ruleValue = JSON.stringify({ is_workday: ruleForm.isWorkday })
      }
      
      const res = await attendanceGroupApi.addRule(currentGroupId.value, ruleData)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        ruleDialogVisible.value = false
        handleManageRules({ groupId: currentGroupId.value })
      }
    } catch (error) {
      ElMessage.error('添加失败：' + (error.message || '未知错误'))
    } finally {
      ruleSubmitLoading.value = false
    }
  })
}

// 学生管理
const handleManageStudents = async (row) => {
  currentGroupId.value = row.groupId
  try {
    const res = await attendanceGroupApi.getGroupStudents(row.groupId)
    if (res.code === 200) {
      studentsData.value = res.data || []
      studentsDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载学生列表失败：' + (error.message || '未知错误'))
  }
}

// 分配学生
const handleAssignStudents = async () => {
  try {
    // 加载可分配的学生列表（已确认上岗的实习申请）
    const res = await applyApi.getApplyPage({
      current: 1,
      size: 1000,
      status: 3 // 已录用
    })
    if (res.code === 200) {
      availableStudents.value = (res.data.records || []).map(item => ({
        applyId: item.applyId,
        studentId: item.studentId,
        studentName: item.studentName,
        studentNo: item.studentNo
      }))
      resetAssignForm()
      assignDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载学生列表失败：' + (error.message || '未知错误'))
  }
}

// 解除关联
const handleUnassignStudent = async (row) => {
  try {
    await ElMessageBox.confirm('确定要解除该学生的关联吗？', '提示', {
      type: 'warning'
    })
    const res = await attendanceGroupApi.unassignStudentFromGroup(row.applyId)
    if (res.code === 200) {
      ElMessage.success('解除关联成功')
      handleManageStudents({ groupId: currentGroupId.value })
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解除关联失败：' + (error.message || '未知错误'))
    }
  }
}

// 提交分配
const handleSubmitAssign = async () => {
  if (!assignFormRef.value) return
  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    assignSubmitLoading.value = true
    try {
      const res = await attendanceGroupApi.batchAssignStudentsToGroup(
        currentGroupId.value,
        {
          applyIds: assignForm.applyIds,
          effectiveStartDate: assignForm.effectiveStartDate,
          effectiveEndDate: assignForm.effectiveEndDate || null
        }
      )
      if (res.code === 200) {
        ElMessage.success('分配成功')
        assignDialogVisible.value = false
        handleManageStudents({ groupId: currentGroupId.value })
      }
    } catch (error) {
      ElMessage.error('分配失败：' + (error.message || '未知错误'))
    } finally {
      assignSubmitLoading.value = false
    }
  })
}

// 工作日类型变化
const handleWorkDaysTypeChange = () => {
  if (formData.workDaysType !== 4) {
    customWorkDays.value = []
    formData.workDaysConfig = ''
  }
}

// 添加时间段
const handleAddTimeSlot = () => {
  formData.timeSlots.push({
    slotName: '',
    startTime: '09:00',
    endTime: '18:00',
    workHours: 8,
    isDefault: 0
  })
}

// 删除时间段
const handleRemoveTimeSlot = (index) => {
  formData.timeSlots.splice(index, 1)
}

// 计算工作时长
const calculateWorkHours = (slot, index) => {
  if (slot.startTime && slot.endTime) {
    try {
      const [startHour, startMinute] = slot.startTime.split(':').map(Number)
      const [endHour, endMinute] = slot.endTime.split(':').map(Number)
      
      const startMinutes = startHour * 60 + startMinute
      const endMinutes = endHour * 60 + endMinute
      
      // 处理跨天的情况（如果结束时间小于开始时间，认为是第二天）
      let diffMinutes = endMinutes - startMinutes
      if (diffMinutes < 0) {
        diffMinutes += 24 * 60 // 加一天
      }
      
      // 转换为小时（保留2位小数）
      const workHours = Math.round((diffMinutes / 60) * 100) / 100
      slot.workHours = workHours
    } catch (error) {
      console.error('计算工作时长失败:', error)
    }
  }
}

// 提交表单
const handleSubmitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 处理自定义工作日配置
    if (formData.workDaysType === 4) {
      if (customWorkDays.value.length === 0) {
        ElMessage.warning('请至少选择一个工作日')
        return
      }
      const config = {}
      const dayMap = {
        monday: 'monday',
        tuesday: 'tuesday',
        wednesday: 'wednesday',
        thursday: 'thursday',
        friday: 'friday',
        saturday: 'saturday',
        sunday: 'sunday'
      }
      for (const day of ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday']) {
        config[day] = customWorkDays.value.includes(day)
      }
      formData.workDaysConfig = JSON.stringify(config)
    } else {
      formData.workDaysConfig = ''
    }
    
    // 提交前重新计算所有时间段的工作时长
    formData.timeSlots.forEach((slot, index) => {
      calculateWorkHours(slot, index)
    })
    
    submitLoading.value = true
    try {
      const groupData = {
        groupId: formData.groupId,
        groupName: formData.groupName,
        groupDescription: formData.groupDescription,
        workDaysType: formData.workDaysType,
        workDaysConfig: formData.workDaysConfig
      }
      
      const requestData = {
        group: groupData,
        timeSlots: formData.timeSlots.map(slot => ({
          slotName: slot.slotName,
          startTime: slot.startTime,
          endTime: slot.endTime,
          workHours: slot.workHours || 8,
          isDefault: slot.isDefault || 0
        }))
      }
      
      let res
      if (formData.groupId) {
        res = await attendanceGroupApi.updateAttendanceGroup(requestData)
      } else {
        res = await attendanceGroupApi.createAttendanceGroup(requestData)
      }
      
      if (res.code === 200) {
        ElMessage.success(formData.groupId ? '更新成功' : '创建成功')
        dialogVisible.value = false
        loadData()
      }
    } catch (error) {
      ElMessage.error((formData.groupId ? '更新' : '创建') + '失败：' + (error.message || '未知错误'))
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  Object.assign(formData, {
    groupId: null,
    groupName: '',
    groupDescription: '',
    workDaysType: 1,
    workDaysConfig: '',
    timeSlots: [
      {
        slotName: '标准班',
        startTime: '09:00',
        endTime: '18:00',
        workHours: 8,
        isDefault: 1
      }
    ]
  })
  customWorkDays.value = []
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 重置规则表单
const resetRuleForm = () => {
  Object.assign(ruleForm, {
    ruleType: 2,
    dateRange: null,
    ruleDate: null,
    isWorkday: true,
    description: ''
  })
  if (ruleFormRef.value) {
    ruleFormRef.value.resetFields()
  }
}

// 重置分配表单
const resetAssignForm = () => {
  Object.assign(assignForm, {
    applyIds: [],
    effectiveStartDate: null,
    effectiveEndDate: null
  })
  if (assignFormRef.value) {
    assignFormRef.value.resetFields()
  }
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
  margin: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.time-slots-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background: #fafafa;
}

.time-slot-item {
  margin-bottom: 10px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.time-slot-item:last-child {
  margin-bottom: 0;
}
</style>

