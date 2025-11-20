<template>
  <MainLayout v-if="showLayout" />
  <router-view v-else />
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/modules/auth'
import MainLayout from '@/components/common/MainLayout.vue'

const route = useRoute()
const authStore = useAuthStore()

// 登录页和错误页不显示布局
const showLayout = computed(() => {
  const noLayoutRoutes = ['/login', '/403', '/404']
  return !noLayoutRoutes.includes(route.path) && authStore.token
})
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
</style>
