<template>
  <div class="layout">
    <el-container>
      <el-header height="60px" class="header">
        <div class="logo">西胪莲藕电商 - 管理端</div>
        <div class="user-info">
          <span>{{ userInfo?.name }}</span>
          <el-button type="text" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu :default-active="activeMenu" class="menu" router>
            <el-menu-item index="/admin/home">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/products">
              <el-icon><Goods /></el-icon>
              <span>商品审核</span>
            </el-menu-item>
            <el-menu-item index="/admin/categories">
              <el-icon><Grid /></el-icon>
              <span>分类管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/orders">
              <el-icon><Document /></el-icon>
              <span>订单管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store'
import { HomeFilled, User, Goods, Document, DataAnalysis, Grid } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  return router.currentRoute.value.path
})

const userInfo = computed(() => {
  return userStore.userInfo
})

const logout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  background-color: #E6A23C;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.aside {
  background-color: #f5f5f5;
  border-right: 1px solid #e4e7ed;
}

.menu {
  height: 100%;
  border-right: none;
}

.main {
  background-color: white;
  padding: 20px;
}
</style>