<template>
  <PageLayout title="实习学生管理">
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
        <el-form-item label="岗位名称">
          <el-input
            v-model="searchForm.postName"
            placeholder="请输入岗位名称"
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

    <!-- 学生列表 -->
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
      <el-table-column prop="postName" label="岗位名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="mentorName" label="企业导师" min-width="120">
        <template #default="{ row }">
          {{ row.mentorName || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录用时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.acceptTime || row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button link type="success" size="small" @click="handleAssignMentor(row)">分配导师</el-button>
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

    <!-- 分配导师对话框 -->
    <el-dialog
      v-model="assignMentorDialogVisible"
      title="分配企业导师"
      width="500px"
    >
      <el-form
        ref="assignMentorFormRef"
        :model="assignMentorForm"
        :rules="assignMentorRules"
        label-width="100px"
      >
        <el-form-item label="学生姓名">
          <el-input :value="currentStudent?.studentName" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input :value="currentStudent?.studentNo" disabled />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input :value="currentStudent?.postName" disabled />
        </el-form-item>
        <el-form-item label="企业导师" prop="mentorId">
          <el-select
            v-model="assignMentorForm.mentorId"
            placeholder="请选择企业导师"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="mentor in mentorList"
              :key="mentor.mentorId"
              :label="mentor.mentorName"
              :value="mentor.mentorId"
            >
              <span>{{ mentor.mentorName }}</span>
              <span style="color: #8492a6; font-size: 13px; margin-left: 10px">
                {{ mentor.position || '' }} {{ mentor.department || '' }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignMentorDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignMentorLoading" @click="handleSubmitAssignMentor">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="学生详情"
      width="800px"
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生姓名">{{ detailData.studentName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ detailData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="企业名称" :span="2">{{ detailData.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位名称" :span="2">{{ detailData.postName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业导师">{{ detailData.mentorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="录用时间">
          {{ formatDateTime(detailData.acceptTime || detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ formatDateTime(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success" size="small">已录用</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageLayout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { applyApi } from '@/api/internship/apply'
import { enterpriseMentorApi } from '@/api/user/enterpriseMentor'
import { formatDateTime } from '@/utils/dateUtils'
import PageLayout from '@/components/common/PageLayout.vue'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({
  studentName: '',
  studentNo: '',
  postName: ''
})
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 分配导师相关
const assignMentorDialogVisible = ref(false)
const assignMentorLoading = ref(false)
const assignMentorFormRef = ref(null)
const currentStudent = ref(null)
const mentorList = ref([])
const assignMentorForm = reactive({
  mentorId: null
})
const assignMentorRules = {
  mentorId: [{ required: true, message: '请选择企业导师', trigger: 'change' }]
}

// 查看详情相关
const detailDialogVisible = ref(false)
const detailData = ref({})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      studentName: searchForm.studentName || undefined,
      studentNo: searchForm.studentNo || undefined,
      postName: searchForm.postName || undefined
    }
    const res = await applyApi.getEnterpriseStudents(params)
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

// 加载企业导师列表
const loadMentorList = async () => {
  try {
    const res = await enterpriseMentorApi.getEnterpriseMentorPage({
      current: 1,
      size: 1000,
      status: 1 // 只查询启用的导师
    })
    if (res.code === 200) {
      mentorList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载企业导师列表失败:', error)
    ElMessage.error('加载企业导师列表失败')
  }
}

// 查询
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    studentName: '',
    studentNo: '',
    postName: ''
  })
  pagination.current = 1
  loadData()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

// 页码变化
const handlePageChange = (current) => {
  pagination.current = current
  loadData()
}

// 查看详情
const handleView = async (row) => {
  try {
    const res = await applyApi.getApplyById(row.applyId)
    if (res.code === 200) {
      detailData.value = res.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    console.error('查询详情失败:', error)
    ElMessage.error('查询详情失败')
  }
}

// 分配导师
const handleAssignMentor = async (row) => {
  currentStudent.value = row
  assignMentorForm.mentorId = row.mentorId || null
  await loadMentorList()
  assignMentorDialogVisible.value = true
}

// 提交分配导师
const handleSubmitAssignMentor = async () => {
  if (!assignMentorFormRef.value) return
  await assignMentorFormRef.value.validate(async (valid) => {
    if (valid) {
      assignMentorLoading.value = true
      try {
        const res = await applyApi.assignMentor(
          currentStudent.value.applyId,
          assignMentorForm.mentorId
        )
        if (res.code === 200) {
          ElMessage.success('分配成功')
          assignMentorDialogVisible.value = false
          loadData()
        }
      } catch (error) {
        console.error('分配失败:', error)
        ElMessage.error(error.response?.data?.message || '分配失败')
      } finally {
        assignMentorLoading.value = false
      }
    }
  })
}

onMounted(() => {
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

