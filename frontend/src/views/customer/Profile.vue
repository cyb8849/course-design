<template>
  <div class="profile-container">
    <div class="profile-header">
      <el-card class="user-info-card">
        <div class="user-info">
          <el-avatar :size="80" :src="userAvatar">
            {{ userStore.user?.username?.charAt(0).toUpperCase() }}
          </el-avatar>
          <div class="user-details">
            <h2>{{ userStore.user?.username }}</h2>
            <p class="user-role">{{ getRoleText(userStore.user?.role) }}</p>
            <p class="user-email">{{ userStore.user?.email || '暂无邮箱' }}</p>
          </div>
          <div class="user-actions">
            <el-button type="primary" @click="goToEditProfile">编辑资料</el-button>
            <el-button @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <div class="profile-content">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="orders-card">
            <template #header>
              <div class="card-header">
                <span>我的订单</span>
                <el-button type="primary" link @click="goToOrders">查看全部</el-button>
              </div>
            </template>

            <el-tabs v-model="activeTab" @tab-click="handleTabClick">
              <el-tab-pane label="全部" name="all" />
              <el-tab-pane label="待支付" name="PENDING" />
              <el-tab-pane label="待发货" name="PAID" />
              <el-tab-pane label="待收货" name="SHIPPED" />
              <el-tab-pane label="已完成" name="COMPLETED" />
            </el-tabs>

            <div v-if="loading" class="loading-state">
              <el-skeleton :rows="3" animated />
            </div>

            <div v-else-if="orders.length === 0" class="empty-state">
              <el-empty description="暂无订单" />
            </div>

            <div v-else class="orders-list">
              <div v-for="order in orders" :key="order.id" class="order-item" @click="goToOrderDetail(order.id)">
                <div class="order-header">
                  <span class="order-no">{{ order.orderNo }}</span>
                  <el-tag :type="getStatusType(order.status)" size="small">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
                <div class="order-info">
                  <span class="order-time">{{ formatTime(order.createTime) }}</span>
                  <span class="order-amount">¥{{ order.totalAmount }}</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="quick-links-card">
            <template #header>
              <span>快捷入口</span>
            </template>

            <div class="quick-links">
              <div class="quick-link-item" @click="goToCart">
                <el-icon size="32"><i-ep-shopping-cart /></el-icon>
                <span>购物车</span>
                <el-badge :value="cartCount" :hidden="cartCount === 0" />
              </div>
              <div class="quick-link-item" @click="goToAddresses">
                <el-icon size="32"><i-ep-location /></el-icon>
                <span>收货地址</span>
              </div>
              <div class="quick-link-item" @click="goToOrders">
                <el-icon size="32"><i-ep-document /></el-icon>
                <span>我的订单</span>
              </div>
              <div class="quick-link-item" @click="goToProducts">
                <el-icon size="32"><i-ep-shopping-bag /></el-icon>
                <span>继续购物</span>
              </div>
            </div>
          </el-card>

          <el-card class="statistics-card">
            <template #header>
              <span>消费统计</span>
            </template>

            <div class="statistics">
              <div class="stat-item">
                <span class="stat-value">{{ statistics.totalOrders }}</span>
                <span class="stat-label">订单总数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">¥{{ statistics.totalSpent }}</span>
                <span class="stat-label">消费总额</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ statistics.pendingPayments }}</span>
                <span class="stat-label">待支付</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useOrderStore, useCartStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const orderStore = useOrderStore()
const cartStore = useCartStore()

const loading = ref(false)
const orders = ref([])
const activeTab = ref('all')

const userAvatar = computed(() => {
  return userStore.user?.avatar || ''
})

const cartCount = computed(() => {
  return cartStore.items.length
})

const statistics = ref({
  totalOrders: 0,
  totalSpent: 0,
  pendingPayments: 0
})

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  await loadOrders()
  await loadStatistics()
  await loadCartCount()
})

const loadOrders = async () => {
  loading.value = true
  try {
    const status = activeTab.value === 'all' ? null : activeTab.value
    await orderStore.getOrders(userStore.user.id, 1, 5, status)
    orders.value = orderStore.orders
  } catch (error) {
    console.error('加载订单失败:', error)
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const allOrders = await orderStore.getOrders(userStore.user.id, 1, 100, null)
    const allRecords = allOrders.data.records || []

    statistics.value.totalOrders = allRecords.length
    statistics.value.totalSpent = allRecords
      .filter(o => o.status === 'PAID' || o.status === 'SHIPPED' || o.status === 'COMPLETED')
      .reduce((sum, o) => sum + Number(o.totalAmount), 0)
      .toFixed(2)
    statistics.value.pendingPayments = allRecords.filter(o => o.status === 'PENDING').length
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadCartCount = async () => {
  try {
    await cartStore.getCartItems(userStore.user.id)
  } catch (error) {
    console.error('加载购物车失败:', error)
  }
}

const handleTabClick = async () => {
  await loadOrders()
}

const getRoleText = (role) => {
  const roleMap = {
    'CUSTOMER': '普通用户',
    'FARMER': '农户',
    'ADMIN': '管理员'
  }
  return roleMap[role] || role
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PAID': 'success',
    'PROCESSING': 'primary',
    'SHIPPED': 'info',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'PROCESSING': '处理中',
    'SHIPPED': '待收货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleDateString('zh-CN')
}

const goToOrders = () => {
  router.push('/orders')
}

const goToOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

const goToCart = () => {
  router.push('/cart')
}

const goToAddresses = () => {
  router.push('/addresses')
}

const goToProducts = () => {
  router.push('/products')
}

const goToEditProfile = () => {
  ElMessage.info('功能开发中')
}

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 20px;
}

.user-info-card :deep(.el-card__body) {
  padding: 30px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-details {
  flex: 1;
}

.user-details h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.user-role {
  color: #409eff;
  margin: 5px 0;
  font-size: 14px;
}

.user-email {
  color: #999;
  margin: 5px 0;
  font-size: 14px;
}

.user-actions {
  display: flex;
  gap: 10px;
}

.profile-content {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.orders-card {
  margin-bottom: 20px;
}

.loading-state {
  padding: 20px 0;
}

.empty-state {
  padding: 40px 0;
}

.orders-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.order-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.order-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.order-no {
  color: #333;
  font-weight: bold;
}

.order-info {
  display: flex;
  justify-content: space-between;
  color: #999;
  font-size: 14px;
}

.order-amount {
  color: #ff4d4f;
  font-weight: bold;
}

.quick-links-card {
  margin-bottom: 20px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.quick-link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-link-item:hover {
  border-color: #409eff;
  background: #f5f7fa;
}

.quick-link-item span {
  margin-top: 10px;
  color: #333;
  font-size: 14px;
}

.statistics-card {
  margin-bottom: 20px;
}

.statistics {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-value {
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  color: #999;
  font-size: 14px;
}
</style>
