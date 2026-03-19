<template>
  <div class="orders-container">
    <h2>我的订单</h2>
    
    <div class="order-filters">
      <el-select v-model="statusFilter" @change="loadOrders" placeholder="订单状态">
        <el-option label="全部" value="" />
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
    </div>
    
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="orders.length === 0" class="empty-state">
      <el-empty description="暂无订单" />
      <el-button type="primary" @click="toProducts">去购物</el-button>
    </div>
    
    <div v-else class="order-list">
      <div v-for="order in orders" :key="order.id" class="order-item">
        <div class="order-header">
          <span class="order-id">订单号：{{ order.id }}</span>
          <span class="order-status" :class="`status-${order.status}`">
            {{ getStatusText(order.status) }}
          </span>
        </div>
        <div class="order-items">
          <div v-for="item in (order.orderItems || [])" :key="item.id" class="order-product">
            <img :src="item.productImage || '/default-product.png'" :alt="item.productName" class="product-image" />
            <div class="product-info">
              <span class="product-name">{{ item.productName }}</span>
              <span class="product-price">¥{{ item.price }}</span>
              <span class="product-quantity">x{{ item.quantity }}</span>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <div class="order-total">
            共 {{ (order.orderItems || []).length }} 件商品，合计：¥{{ (order.totalAmount || 0).toFixed(2) }}
          </div>
          <div class="order-actions">
            <el-button size="small" @click="viewOrderDetail(order.id)">查看详情</el-button>
            <el-button v-if="order.status === 'PENDING'" size="small" type="primary" @click="payOrder(order.id)">支付</el-button>
            <el-button v-if="order.status === 'PENDING'" size="small" type="danger" @click="cancelOrder(order.id)">取消</el-button>
            <el-button v-if="order.status === 'SHIPPED'" size="small" type="success" @click="confirmReceipt(order.id)">确认收货</el-button>
            <el-button size="small" type="danger" plain :disabled="order.status !== 'CANCELLED'" @click="deleteOrder(order.id)">删除</el-button>
          </div>
        </div>
      </div>
    </div>
    
    <div class="pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useOrderStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orderStore = useOrderStore()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await loadOrders()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

const loadOrders = async () => {
  loading.value = true
  try {
    const result = await orderStore.getOrders(userStore.user.id, currentPage.value, pageSize.value, statusFilter.value)
    orders.value = result.data?.records || []
    total.value = result.data?.total || 0
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadOrders()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadOrders()
}

const viewOrderDetail = (orderId) => {
  router.push(`/order/${orderId}`)
}

const payOrder = async (orderId) => {
  try {
    await orderStore.payOrder(orderId)
    ElMessage.success('支付成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error('支付失败：' + (error.response?.data?.message || error.message))
  }
}

const cancelOrder = async (orderId) => {
  try {
    await orderStore.cancelOrder(orderId)
    ElMessage.success('取消成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error('取消失败：' + (error.response?.data?.message || error.message))
  }
}

const confirmReceipt = async (orderId) => {
  try {
    await orderStore.confirmReceipt(orderId)
    ElMessage.success('确认收货成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error('确认收货失败：' + (error.response?.data?.message || error.message))
  }
}

const deleteOrder = async (orderId) => {
  try {
    await orderStore.deleteOrder(orderId)
    ElMessage.success('删除成功')
    await loadOrders()
  } catch (error) {
    ElMessage.error('删除失败：' + (error.response?.data?.message || error.message))
  }
}

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

const toProducts = () => {
  router.push('/products')
}
</script>

<style scoped>
.orders-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.orders-container h2 {
  margin-bottom: 30px;
  color: #333;
}

.order-filters {
  margin-bottom: 30px;
}

.loading-state {
  margin: 20px 0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state .el-button {
  margin-top: 20px;
}

.order-list {
  margin-bottom: 30px;
}

.order-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 20px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.order-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #f5f5f5;
  border-bottom: 1px solid #e4e7ed;
}

.order-id {
  font-weight: bold;
  color: #333;
}

.order-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
}

.status-PENDING {
  background: #fdf6ec;
  color: #fa8c16;
}

.status-PAID {
  background: #f0f5ff;
  color: #1890ff;
}

.status-SHIPPED {
  background: #f6ffed;
  color: #52c41a;
}

.status-COMPLETED {
  background: #f0f0f0;
  color: #8c8c8c;
}

.status-CANCELLED {
  background: #fff2f0;
  color: #ff4d4f;
}

.order-items {
  padding: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.order-product {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #f0f0f0;
}

.order-product:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}

.product-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.product-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-name {
  flex: 1;
  color: #333;
  margin-right: 20px;
}

.product-price {
  color: #333;
  margin-right: 20px;
}

.product-quantity {
  color: #666;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
}

.order-total {
  font-weight: bold;
  color: #333;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>