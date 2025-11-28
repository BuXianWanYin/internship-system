<template>
  <el-card class="statistics-filter" shadow="never">
    <el-form :inline="true" :model="filterForm" class="filter-form">
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="filterForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          clearable
          @change="handleFilterChange"
        />
      </el-form-item>
      
      <el-form-item v-if="showSchoolFilter" label="学校">
        <el-select
          v-model="filterForm.schoolId"
          placeholder="请选择学校"
          clearable
          style="width: 200px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="school in schoolList"
            :key="school.schoolId"
            :label="school.schoolName"
            :value="school.schoolId"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item v-if="showCollegeFilter" label="学院">
        <el-select
          v-model="filterForm.collegeId"
          placeholder="请选择学院"
          clearable
          style="width: 200px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="college in collegeList"
            :key="college.collegeId"
            :label="college.collegeName"
            :value="college.collegeId"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item v-if="showMajorFilter" label="专业">
        <el-select
          v-model="filterForm.majorId"
          placeholder="请选择专业"
          clearable
          style="width: 200px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="major in majorList"
            :key="major.majorId"
            :label="major.majorName"
            :value="major.majorId"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item v-if="showClassFilter" label="班级">
        <el-select
          v-model="filterForm.classId"
          placeholder="请选择班级"
          clearable
          style="width: 200px"
          @change="handleFilterChange"
        >
          <el-option
            v-for="classItem in classList"
            :key="classItem.classId"
            :label="classItem.className"
            :value="classItem.classId"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useAuthStore } from '@/store/modules/auth'
import { hasAnyRole } from '@/utils/permission'

const props = defineProps({
  showSchoolFilter: {
    type: Boolean,
    default: false
  },
  showCollegeFilter: {
    type: Boolean,
    default: false
  },
  showMajorFilter: {
    type: Boolean,
    default: false
  },
  showClassFilter: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['filter-change'])

const authStore = useAuthStore()
const userRoles = computed(() => authStore.roles || [])

const filterForm = reactive({
  dateRange: null,
  schoolId: null,
  collegeId: null,
  majorId: null,
  classId: null
})

const schoolList = ref([])
const collegeList = ref([])
const majorList = ref([])
const classList = ref([])

const handleFilterChange = () => {
  emit('filter-change', { ...filterForm })
}

const handleReset = () => {
  filterForm.dateRange = null
  filterForm.schoolId = null
  filterForm.collegeId = null
  filterForm.majorId = null
  filterForm.classId = null
  handleFilterChange()
}

// 初始化时触发一次
watch(() => props.showSchoolFilter, () => {
  handleFilterChange()
}, { immediate: true })
</script>

<style scoped>
.statistics-filter {
  margin-bottom: 20px;
  border-radius: 8px;
}

.filter-form {
  margin: 0;
}

:deep(.el-card__body) {
  padding: 16px 20px;
}
</style>

