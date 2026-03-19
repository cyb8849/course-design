<template>
  <div class="farmer-home">
    <h2>农户管理中心</h2>
    
    <div class="stats-grid">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>总销售额</span>
          </div>
        </template>
        <div class="stat-value">¥{{ totalSales }}</div>
        <div class="stat-desc">本月销售额：¥{{ monthlySales }}</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>总订单数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalOrders }}</div>
        <div class="stat-desc">本月订单数：{{ monthlyOrders }}</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>商品数量</span>
          </div>
        </template>
        <div class="stat-value">{{ productCount }}</div>
        <div class="stat-desc">在售商品：{{ activeProducts }}</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>待处理订单</span>
          </div>
        </template>
        <div class="stat-value">{{ pendingOrders }}</div>
        <div class="stat-desc">待发货：{{ pendingShipOrders }}</div>
      </el-card>
    </div>
    
    <div class="recent-orders">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>最近订单</span>
            <el-button size="small" type="primary" @click="toOrders">查看全部</el-button>
          </div>
        </template>
        <el-table :data="recentOrders" style="width: 100%" border>
          <el-table-column prop="id" label="订单号" width="180" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="scope">
              ¥{{ scope.row.totalAmount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="下单时间" width="180" />
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button size="small" @click="viewOrderDetail(scope.row.id)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const totalSales = ref(0)
const monthlySales = ref(0)
const totalOrders = ref(0)
const monthlyOrders = ref(0)
const productCount = ref(0)
const activeProducts = ref(0)
const pendingOrders = ref(0)
const pendingShipOrders = ref(0)

const recentOrders = ref([])

// 加载数据
const loadData = async () => {
  if (!userStore.isLoggedIn) return
  
  loading.value = true
  try {
    // 加载商品统计数据
    const statsResponse = await axios.get(`/farmer/products/stats/${userStore.user.id}`)
    
    if (statsResponse.data.code === 200) {
      const stats = statsResponse.data.data || {}
      productCount.value = stats.total || 0
      activeProducts.value = stats.approved || 0
    }
    
    // 加载订单统计数据
    const ordersResponse = await axios.get('/farmer/orders', {
      params: {
        farmerId: userStore.user.id,
        page: 1,
        size: 100
      }
    })
    
    if (ordersResponse.data.code === 200) {
      const orderData = ordersResponse.data.data || {}
      const orders = orderData.records || []
      totalOrders.value = orderData.total || 0
      
      // 计算待处理订单和待发货订单数量
      pendingOrders.value = orders.filter(order => order.status === 'PENDING' || order.status === 'PAID').length
      pendingShipOrders.value = orders.filter(order => order.status === 'PAID').length
      
      // 计算总销售额
      totalSales.value = orders.reduce((sum, order) => sum + (order.totalAmount || 0), 0)
    }
    
    // 加载最近订单
    const recentOrdersResponse = await axios.get(`/farmer/orders/recent/${userStore.user.id}`, {
      params: { limit: 5 }
    })
    
    if (recentOrdersResponse.data.code === 200) {
      recentOrders.value = recentOrdersResponse.data.data || []
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'PENDING': 'warning',
    'PAID': 'primary',
    'SHIPPED': 'success',
    'COMPLETED': 'info',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const toOrders = () => {
  router.push('/farmer/orders')
}

const viewOrderDetail = (orderId) => {
  router.push(`/farmer/order/${orderId}`)
}
</script>

<style scoped>
.farmer-home {
  padding: 20px;
}

.farmer-home h2 {
  margin-bottom: 30px;
  color: #333;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #1890ff;
  margin: 20px 0;
}

.stat-desc {
  font-size: 14px;
  color: #666;
}

.recent-orders {
  margin-bottom: 30px;
}
</style>