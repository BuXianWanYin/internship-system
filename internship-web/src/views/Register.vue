<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h2>高校实习管理系统</h2>
        <p>请选择注册类型</p>
      </div>
      
      <div class="register-options">
        <el-card 
          class="register-card" 
          shadow="hover"
          @click="goToStudentRegister"
        >
          <div class="card-content">
            <el-icon class="card-icon" :size="48" color="#409EFF">
              <User />
            </el-icon>
            <h3>学生注册</h3>
            <p>使用班级分享码完成注册</p>
          </div>
        </el-card>
        
        <el-card 
          class="register-card" 
          shadow="hover"
          @click="goToTeacherRegister"
        >
          <div class="card-content">
            <el-icon class="card-icon" :size="48" color="#E6A23C">
              <UserFilled />
            </el-icon>
            <h3>教师注册</h3>
            <p>填写个人信息完成注册</p>
          </div>
        </el-card>
        
        <el-card 
          class="register-card" 
          shadow="hover"
          @click="goToEnterpriseRegister"
        >
          <div class="card-content">
            <el-icon class="card-icon" :size="48" color="#67C23A">
              <OfficeBuilding />
            </el-icon>
            <h3>企业注册</h3>
            <p>填写企业信息完成注册</p>
          </div>
        </el-card>
      </div>
      
      <div class="back-login">
        <el-link type="primary" @click="goToLogin">
          <el-icon><ArrowLeft /></el-icon>
          返回登录
        </el-link>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'
import { User, UserFilled, OfficeBuilding } from '@element-plus/icons-vue'

export default {
  name: 'Register',
  components: {
    User,
    UserFilled,
    OfficeBuilding
  },
  setup() {
    const router = useRouter()
    
    const goToStudentRegister = () => {
      router.push('/register/student')
    }
    
    const goToTeacherRegister = () => {
      router.push('/register/teacher')
    }
    
    const goToEnterpriseRegister = () => {
      router.push('/register/enterprise')
    }
    
    const goToLogin = () => {
      router.push('/login')
    }
    
    return {
      goToStudentRegister,
      goToTeacherRegister,
      goToEnterpriseRegister,
      goToLogin
    }
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f5f7fa;
}

.register-box {
  width: 100%;
  max-width: 900px;
  padding: 48px;
  background: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h2 {
  margin: 0 0 12px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 500;
}

.register-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
  min-width: 800px;
}

.register-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.register-card:hover {
  border-color: #409EFF;
  transform: translateY(-4px);
  box-shadow: 0 4px 20px rgba(64, 158, 255, 0.2);
}

.card-content {
  text-align: center;
  padding: 20px;
}

.card-icon {
  margin-bottom: 16px;
}

.card-content h3 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 18px;
  font-weight: 500;
  white-space: nowrap;
}

.card-content p {
  margin: 0;
  color: #909399;
  font-size: 14px;
  white-space: nowrap;
}

.back-login {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid #EBEEF5;
}

.back-login .el-link {
  font-size: 14px;
}

.back-login .el-icon {
  margin-right: 4px;
}

@media (max-width: 768px) {
  .register-box {
    width: 90%;
    padding: 32px 24px;
  }
  
  .register-options {
    grid-template-columns: 1fr;
  }
}
</style>

