<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #545c64">
      <el-menu
        :default-active="activeIndex"
        class="el-menu-vertical-demo"
        @select="handleSelect"
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
      >
        <el-menu-item index="/farmer/home">
          <el-icon><i-ep-home /></el-icon>
          <span>首页</span>
        </el-menu-item>
        <el-menu-item index="/farmer/products">
          <el-icon><i-ep-goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/farmer/traceability">
          <el-icon><i-ep-search /></el-icon>
          <span>溯源信息管理</span>
        </el-menu-item>
        <el-menu-item index="/farmer/orders">
          <el-icon><i-ep-order /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/farmer/statistics">
          <el-icon><i-ep-data-analysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="/login" @click="handleLogout">
          <el-icon><i-ep-switch-button /></el-icon>
          <span>退出登录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #f5f5f5; padding: 0 20px; display: flex; align-items: center; justify-content: space-between;">
        <h1 style="margin: 0; font-size: 20px; color: #333;">农户管理后台</h1>
        <div class="user-info">
          <span>欢迎，{{ user?.name }}</span>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../store'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeIndex = computed(() => route.path)
const user = computed(() => userStore.user)

const handleSelect = (key) => {
  router.push(key)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.el-aside {
  overflow: auto;
}

.user-info {
  font-size: 14px;
  color: #333;
}
</style>