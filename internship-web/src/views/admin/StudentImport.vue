<template>
  <div class="student-import-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>学生批量导入</span>
          <el-button type="primary" @click="downloadTemplate">
            <el-icon><Download /></el-icon>
            下载模板
          </el-button>
        </div>
      </template>

      <el-steps :active="activeStep" finish-status="success" align-center>
        <el-step title="下载模板" description="下载Excel导入模板" />
        <el-step title="填写数据" description="按照模板格式填写学生信息" />
        <el-step title="上传文件" description="上传填写好的Excel文件" />
        <el-step title="导入结果" description="查看导入结果" />
      </el-steps>

      <div class="upload-section" v-if="activeStep >= 2">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :limit="1"
          accept=".xlsx,.xls"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              只能上传Excel文件（.xlsx或.xls），且不超过10MB
            </div>
          </template>
        </el-upload>

        <div class="class-select" v-if="showClassSelect">
          <el-select
            v-model="selectedClassId"
            placeholder="请选择班级（如果Excel中未指定班级ID）"
            clearable
            style="width: 100%; margin-top: 20px;"
          >
            <el-option
              v-for="classItem in classList"
              :key="classItem.classId"
              :label="classItem.className"
              :value="classItem.classId"
            />
          </el-select>
        </div>

        <div class="upload-actions" v-if="fileList.length > 0">
          <el-button type="primary" :loading="uploading" @click="handleUpload">
            开始导入
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <div class="result-section" v-if="activeStep === 3 && importResult">
        <el-alert
          :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failCount} 条`"
          :type="importResult.failCount === 0 ? 'success' : 'warning'"
          :closable="false"
          show-icon
        />

        <div v-if="importResult.failList && importResult.failList.length > 0" class="fail-list">
          <h3>失败详情：</h3>
          <el-table :data="importResult.failList" border style="width: 100%; margin-top: 20px;">
            <el-table-column prop="rowNum" label="行号" width="80" />
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="realName" label="姓名" width="100" />
            <el-table-column prop="errorMessage" label="错误信息" />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, UploadFilled } from '@element-plus/icons-vue'
import { studentApi } from '@/api/user/student'
import { classApi } from '@/api/system'

export default {
  name: 'StudentImport',
  components: {
    Download,
    UploadFilled
  },
  setup() {
    const activeStep = ref(0)
    const uploadRef = ref(null)
    const fileList = ref([])
    const selectedClassId = ref(null)
    const classList = ref([])
    const showClassSelect = ref(false)
    const uploading = ref(false)
    const importResult = ref(null)

    // 下载模板
    const downloadTemplate = async () => {
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
        activeStep.value = 1
      } catch (error) {
        ElMessage.error('模板下载失败：' + (error.message || '未知错误'))
      }
    }

    // 文件变化
    const handleFileChange = (file) => {
      fileList.value = [file]
      showClassSelect.value = true
      activeStep.value = 2
    }

    // 移除文件
    const handleFileRemove = () => {
      fileList.value = []
      showClassSelect.value = false
      selectedClassId.value = null
    }

    // 上传文件
    const handleUpload = async () => {
      if (fileList.value.length === 0) {
        ElMessage.warning('请先选择要上传的文件')
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
      try {
        const res = await studentApi.importStudents(file, selectedClassId.value)
        if (res.code === 200) {
          importResult.value = res.data
          activeStep.value = 3
          ElMessage.success(res.message || '导入完成')
        } else {
          ElMessage.error(res.message || '导入失败')
        }
      } catch (error) {
        ElMessage.error('导入失败：' + (error.message || '未知错误'))
      } finally {
        uploading.value = false
      }
    }

    // 重置
    const handleReset = () => {
      fileList.value = []
      selectedClassId.value = null
      showClassSelect.value = false
      importResult.value = null
      activeStep.value = 0
      if (uploadRef.value) {
        uploadRef.value.clearFiles()
      }
    }

    // 加载班级列表
    const loadClassList = async () => {
      try {
        const res = await classApi.getClassPage({ current: 1, size: 1000 })
        if (res.code === 200 && res.data) {
          classList.value = res.data.records || []
        }
      } catch (error) {
        console.error('加载班级列表失败：', error)
      }
    }

    onMounted(() => {
      loadClassList()
    })

    return {
      activeStep,
      uploadRef,
      fileList,
      selectedClassId,
      classList,
      showClassSelect,
      uploading,
      importResult,
      downloadTemplate,
      handleFileChange,
      handleFileRemove,
      handleUpload,
      handleReset
    }
  }
}
</script>

<style scoped>
.student-import-container {
  padding: 20px;
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

