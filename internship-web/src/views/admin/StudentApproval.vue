<template>
  <PageLayout title="学生注册审核">
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
        <el-form-item label="姓名">
          <el-input
            v-model="searchForm.realName"
            placeholder="请输入姓名"
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

    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      style="width: 100%"
      :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
    >
      <el-table-column prop="studentNo" label="学号" min-width="120" />
      <el-table-column prop="userId" label="用户信息" min-width="150">
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
      <el-table-column prop="classId" label="班级" min-width="150">
        <template #default="{ row }">
          <span v-if="classMap[row.classId]">{{ classMap[row.classId].className }}</span>
          <span v-else>加载中...</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="180" />
      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleView(row)">查看详情</el-button>
          <el-button link type="success" size="small" @click="handleApprove(row, true)">通过</el-button>
          <el-button link type="danger" size="small" @click="handleApprove(row, false)">拒绝</el-button>
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
        <el-descriptions-item label="注册时间">{{ currentStudent.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageLayout>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { studentApi } from '@/api/user/student'
import { classApi } from '@/api/system'
import { userApi } from '@/api/user/user'

export default {
  name: 'StudentApproval',
  components: {
    PageLayout,
    Search,
    Refresh
  },
  setup() {
    const loading = ref(false)
    const approving = ref(false)
    const tableData = ref([])
    const userInfoMap = ref({})
    const classMap = ref({})
    const approveDialogVisible = ref(false)
    const detailDialogVisible = ref(false)
    const currentStudent = ref(null)
    const approveFormRef = ref(null)

    const searchForm = reactive({
      studentNo: '',
      realName: ''
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const approveForm = reactive({
      approved: true,
      auditOpinion: ''
    })

    // 加载数据
    const loadData = async () => {
      loading.value = true
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          ...searchForm
        }
        const res = await studentApi.getPendingApprovalStudentPage(params)
        if (res.code === 200) {
          tableData.value = res.data.records || []
          pagination.total = res.data.total || 0

          // 加载用户信息和班级信息
          const userIds = [...new Set(tableData.value.map(item => item.userId))]
          const classIds = [...new Set(tableData.value.map(item => item.classId))]

          await loadUserInfo(userIds)
          await loadClassInfo(classIds)
        } else {
          ElMessage.error(res.message || '加载失败')
        }
      } catch (error) {
        ElMessage.error('加载失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    // 加载用户信息
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

    // 加载班级信息
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

    // 查询
    const handleSearch = () => {
      pagination.current = 1
      loadData()
    }

    // 重置
    const handleReset = () => {
      searchForm.studentNo = ''
      searchForm.realName = ''
      pagination.current = 1
      loadData()
    }

    // 查看详情
    const handleView = (row) => {
      currentStudent.value = row
      detailDialogVisible.value = true
    }

    // 审核
    const handleApprove = (row, approved) => {
      currentStudent.value = row
      approveForm.approved = approved
      approveForm.auditOpinion = ''
      approveDialogVisible.value = true
    }

    // 确认审核
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
          loadData()
        } else {
          ElMessage.error(res.message || '审核失败')
        }
      } catch (error) {
        ElMessage.error('审核失败：' + (error.message || '未知错误'))
      } finally {
        approving.value = false
      }
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

    onMounted(() => {
      loadData()
    })

    return {
      loading,
      approving,
      tableData,
      userInfoMap,
      classMap,
      searchForm,
      pagination,
      approveDialogVisible,
      detailDialogVisible,
      currentStudent,
      approveFormRef,
      approveForm,
      handleSearch,
      handleReset,
      handleView,
      handleApprove,
      handleConfirmApprove,
      handleSizeChange,
      handlePageChange
    }
  }
}
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

