<template>
  <PageLayout title="班主任任命">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="班级名称">
          <el-input
            v-model="searchForm.className"
            placeholder="请输入班级名称"
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
      <el-table-column prop="className" label="班级名称" min-width="180" />
      <el-table-column prop="classCode" label="班级代码" min-width="120" />
      <el-table-column prop="enrollmentYear" label="入学年份" width="100" align="center" />
      <el-table-column label="当前班主任" min-width="150">
        <template #default="{ row }">
          <div v-if="row.classTeacherId && teacherMap[row.classTeacherId]">
            <div>{{ teacherMap[row.classTeacherId].teacherNo }}</div>
            <div v-if="userInfoMap[row.classTeacherId]" style="font-size: 12px; color: #909399;">
              {{ userInfoMap[row.classTeacherId].realName }}
            </div>
          </div>
          <el-tag v-else type="info" size="small">未任命</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right" align="center">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleAppoint(row)">任命</el-button>
          <el-button
            v-if="row.classTeacherId"
            link
            type="danger"
            size="small"
            @click="handleRemove(row)"
          >
            取消
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

    <!-- 任命对话框 -->
    <el-dialog
      v-model="appointDialogVisible"
      title="任命班主任"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="appointFormRef"
        :model="appointForm"
        label-width="100px"
      >
        <el-form-item label="班级信息">
          <div v-if="currentClass">
            <div>班级名称：{{ currentClass.className }}</div>
            <div>班级代码：{{ currentClass.classCode }}</div>
          </div>
        </el-form-item>
        <el-form-item label="选择教师" prop="teacherId">
          <el-select
            v-model="appointForm.teacherId"
            placeholder="请选择教师"
            filterable
            style="width: 100%"
            @change="handleTeacherChange"
          >
            <el-option
              v-for="teacher in teacherList"
              :key="teacher.teacherId"
              :label="`${teacher.teacherNo} - ${getTeacherName(teacher.userId)}`"
              :value="teacher.teacherId"
            />
          </el-select>
          <div v-if="selectedTeacher" style="margin-top: 10px; padding: 10px; background: #f5f7fa; border-radius: 4px;">
            <div>工号：{{ selectedTeacher.teacherNo }}</div>
            <div v-if="userInfoMap[selectedTeacher.userId]">
              姓名：{{ userInfoMap[selectedTeacher.userId].realName }}
            </div>
            <div v-if="selectedTeacher.title">职称：{{ selectedTeacher.title }}</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appointDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="appointing"
          @click="handleConfirmAppoint"
        >
          确认任命
        </el-button>
      </template>
    </el-dialog>
  </PageLayout>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageLayout from '@/components/common/PageLayout.vue'
import { classApi } from '@/api/system'
import { teacherApi } from '@/api/user/teacher'
import { userApi } from '@/api/user/user'

export default {
  name: 'ClassTeacherAppointment',
  components: {
    PageLayout,
    Search,
    Refresh
  },
  setup() {
    const loading = ref(false)
    const appointing = ref(false)
    const tableData = ref([])
    const teacherList = ref([])
    const teacherMap = ref({})
    const userInfoMap = ref({})
    const appointDialogVisible = ref(false)
    const currentClass = ref(null)
    const selectedTeacher = ref(null)
    const appointFormRef = ref(null)

    const searchForm = reactive({
      className: ''
    })

    const pagination = reactive({
      current: 1,
      size: 10,
      total: 0
    })

    const appointForm = reactive({
      teacherId: null
    })

    // 加载班级列表
    const loadData = async () => {
      loading.value = true
      try {
        const params = {
          current: pagination.current,
          size: pagination.size,
          className: searchForm.className
        }
        const res = await classApi.getClassPage(params)
        if (res.code === 200) {
          tableData.value = res.data.records || []
          pagination.total = res.data.total || 0

          // 加载班主任信息
          const teacherUserIds = tableData.value
            .filter(item => item.classTeacherId)
            .map(item => item.classTeacherId)
          if (teacherUserIds.length > 0) {
            await loadTeacherInfo(teacherUserIds)
          }
        } else {
          ElMessage.error(res.message || '加载失败')
        }
      } catch (error) {
        ElMessage.error('加载失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    // 加载教师信息（通过userId）
    const loadTeacherInfo = async (userIds) => {
      try {
        // 加载用户信息
        for (const userId of userIds) {
          if (!userInfoMap.value[userId]) {
            const res = await userApi.getUserById(userId)
            if (res.code === 200 && res.data) {
              userInfoMap.value[userId] = res.data
            }
          }
        }
        
        // 加载教师信息（通过userId查询teacher_info表）
        for (const userId of userIds) {
          if (!teacherMap.value[userId]) {
            try {
              const res = await teacherApi.getTeacherByUserId(userId)
              if (res.code === 200 && res.data) {
                teacherMap.value[userId] = res.data
              }
            } catch (error) {
              // 如果查询失败，可能是该用户不是教师，忽略错误
              console.warn(`用户 ${userId} 不是教师或查询失败:`, error)
            }
          }
        }
      } catch (error) {
        console.error('加载教师信息失败：', error)
      }
    }

    // 加载教师列表（用于选择）
    const loadTeacherList = async () => {
      try {
        const res = await teacherApi.getTeacherPage({ current: 1, size: 1000 })
        if (res.code === 200) {
          teacherList.value = res.data.records || []
          
          // 加载教师对应的用户信息
          const userIds = [...new Set(teacherList.value.map(t => t.userId))]
          await loadTeacherInfo(userIds)
          
          // 构建teacherMap（以userId为key）
          teacherList.value.forEach(teacher => {
            teacherMap.value[teacher.userId] = teacher
          })
        }
      } catch (error) {
        console.error('加载教师列表失败：', error)
      }
    }

    // 获取教师姓名
    const getTeacherName = (userId) => {
      return userInfoMap.value[userId]?.realName || '未知'
    }

    // 查询
    const handleSearch = () => {
      pagination.current = 1
      loadData()
    }

    // 重置
    const handleReset = () => {
      searchForm.className = ''
      pagination.current = 1
      loadData()
    }

    // 任命
    const handleAppoint = async (row) => {
      currentClass.value = row
      appointForm.teacherId = null
      selectedTeacher.value = null
      
      // 加载教师列表
      await loadTeacherList()
      
      appointDialogVisible.value = true
    }

    // 教师选择变化
    const handleTeacherChange = (teacherId) => {
      selectedTeacher.value = teacherList.value.find(t => t.teacherId === teacherId) || null
    }

    // 确认任命
    const handleConfirmAppoint = async () => {
      if (!appointForm.teacherId) {
        ElMessage.warning('请选择教师')
        return
      }

      appointing.value = true
      try {
        const res = await classApi.appointClassTeacher(
          currentClass.value.classId,
          appointForm.teacherId
        )
        if (res.code === 200) {
          ElMessage.success(res.message || '任命成功')
          appointDialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '任命失败')
        }
      } catch (error) {
        ElMessage.error('任命失败：' + (error.message || '未知错误'))
      } finally {
        appointing.value = false
      }
    }

    // 取消任命
    const handleRemove = async (row) => {
      try {
        await ElMessageBox.confirm(
          `确定要取消班级"${row.className}"的班主任任命吗？`,
          '确认取消',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )

        const res = await classApi.removeClassTeacher(row.classId)
        if (res.code === 200) {
          ElMessage.success(res.message || '取消成功')
          loadData()
        } else {
          ElMessage.error(res.message || '取消失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('取消失败：' + (error.message || '未知错误'))
        }
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
      appointing,
      tableData,
      teacherList,
      teacherMap,
      userInfoMap,
      searchForm,
      pagination,
      appointDialogVisible,
      currentClass,
      selectedTeacher,
      appointFormRef,
      appointForm,
      handleSearch,
      handleReset,
      handleAppoint,
      handleTeacherChange,
      handleConfirmAppoint,
      handleRemove,
      handleSizeChange,
      handlePageChange,
      getTeacherName
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

