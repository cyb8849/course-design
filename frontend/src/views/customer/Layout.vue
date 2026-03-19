<template>
  <div class="layout">
    <el-container>
      <el-header height="60px" class="header">
        <div class="header-left">
          <div class="logo">西胪莲藕电商</div>
          <el-menu 
            mode="horizontal" 
            :default-active="activeMenu" 
            class="header-menu"
            :ellipsis="false"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/home">
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/products">
              <span>商品浏览</span>
            </el-menu-item>
            <el-menu-item index="/traceability">
              <span>溯源查询</span>
            </el-menu-item>
            <el-menu-item index="/logistics">
              <span>物流追踪</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div class="header-right">
          <el-badge :value="cartTotal" :hidden="cartTotal === 0" class="cart-badge">
            <el-button circle @click="goToCart">
              <el-icon><ShoppingCart /></el-icon>
            </el-button>
          </el-badge>
          <el-dropdown @command="handleCommand">
            <div class="user-dropdown">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <span class="username">{{ userStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="/orders">
                  <el-icon><Document /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="/addresses">
                  <el-icon><Location /></el-icon>
                  收货地址
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useCartStore } from '../../store'
import { ElMessage } from 'element-plus'
import { ShoppingCart, ArrowDown, User, Document, Location, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const activeMenu = computed(() => {
  const path = router.currentRoute.value.path
  if (path === '/home' || path === '/') return '/home'
  if (path.startsWith('/products')) return '/products'
  if (path.startsWith('/traceability')) return '/traceability'
  if (path.startsWith('/logistics')) return '/logistics'
  return path
})

const cartTotal = computed(() => {
  return cartStore.items?.length || 0
})

onMounted(async () => {
  if (userStore.isLoggedIn && userStore.user?.id) {
    try {
      await cartStore.getCartItems(userStore.user.id)
    } catch (error) {
      console.error('加载购物车失败:', error)
    }
  }
})

const goToCart = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/cart')
}

const handleMenuSelect = (key) => {
  router.push(key)
}

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else {
    router.push(command)
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  color: #409EFF;
}

.header-menu {
  border-bottom: none;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-badge {
  margin-right: 10px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-dropdown:hover {
  background-color: #f5f7fa;
}

.user-avatar {
  background-color: #409EFF;
  color: white;
  font-size: 14px;
}

.username {
  color: #333;
  font-size: 14px;
}

.main {
  background-color: #f5f5f5;
  padding: 20px;
  min-height: calc(100vh - 60px);
}
</style>