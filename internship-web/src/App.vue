<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>实习管理系统</h1>
      </el-header>
      <el-main>
        <el-card>
          <template #header>
            <div class="card-header">
              <span>欢迎使用 Vue3 + Element Plus + Vite</span>
            </div>
          </template>
          <el-button type="primary">主要按钮</el-button>
          <el-button type="success">成功按钮</el-button>
          <el-button type="info">信息按钮</el-button>
          <el-button type="warning">警告按钮</el-button>
          <el-button type="danger">危险按钮</el-button>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
// Vue3 Composition API
</script>

<style scoped>
#app {
  width: 100%;
  min-height: 100vh;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

