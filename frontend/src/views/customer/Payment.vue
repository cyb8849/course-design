<template>
  <div class="payment-container">
    <h2>确认订单</h2>

    <el-card v-if="order" class="order-card">
      <template #header>
        <div class="card-header">
          <span>订单信息</span>
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
      </template>

      <div class="order-info">
        <div class="info-row">
          <span class="label">订单编号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间：</span>
          <span class="value">{{ formatTime(order.createTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">收货人：</span>
          <span class="value">{{ order.receiverName }}</span>
        </div>
        <div class="info-row">
          <span class="label">联系电话：</span>
          <span class="value">{{ order.receiverPhone }}</span>
        </div>
        <div class="info-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ order.receiverAddress }}</span>
        </div>
      </div>
    </el-card>

    <el-card class="items-card">
      <template #header>
        <span>商品信息</span>
      </template>

      <el-table :data="orderItems" style="width: 100%">
        <el-table-column label="商品图片" width="100">
          <template #default="scope">
            <img :src="scope.row.productImage" :alt="scope.row.productName" class="product-image" />
          </template>
        </el-table-column>
        <el-table-column prop="productName" label="商品名称" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="小计" width="100">
          <template #default="scope">
            ¥{{ scope.row.subtotal }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card class="summary-card">
      <div class="summary-content">
        <div class="summary-row">
          <span>商品总价：</span>
          <span>¥{{ order?.totalAmount || '0.00' }}</span>
        </div>
        <div class="summary-row">
          <span>运费：</span>
          <span>免运费</span>
        </div>
        <div class="summary-row total">
          <span>应付金额：</span>
          <span class="amount">¥{{ order?.totalAmount || '0.00' }}</span>
        </div>
      </div>
    </el-card>

    <div class="payment-method">
      <h3>支付方式</h3>
      <el-radio-group v-model="paymentMethod">
        <el-radio label="alipay">
          <el-icon><i-ep-wallet /></el-icon>
          支付宝
        </el-radio>
        <el-radio label="wechat">
          <el-icon><i-ep-chat-line-square /></el-icon>
          微信支付
        </el-radio>
        <el-radio label="bankcard">
          <el-icon><i-ep-credit-card /></el-icon>
          银行卡
        </el-radio>
      </el-radio-group>
    </div>

    <div class="actions">
      <el-button @click="goBack">返回</el-button>
      <el-button type="primary" size="large" @click="handlePayment" :loading="loading">
        立即支付
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()
const userStore = useUserStore()

const orderId = ref(route.params.orderId || route.query.orderId)
const order = ref(null)
const orderItems = ref([])
const paymentMethod = ref('alipay')
const loading = ref(false)

onMounted(async () => {
  if (!orderId.value) {
    ElMessage.error('订单不存在')
    router.push('/orders')
    return
  }

  await loadOrderDetail()
})

const loadOrderDetail = async () => {
  try {
    const data = await orderStore.getOrderDetail(orderId.value)
    order.value = data.order
    orderItems.value = data.items || []
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    router.push('/orders')
  }
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
    'SHIPPED': '已发货',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return textMap[status] || status
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const handlePayment = async () => {
  if (!order.value) {
    ElMessage.error('订单不存在')
    return
  }

  if (order.value.status !== 'PENDING') {
    ElMessage.error('订单状态不正确')
    return
  }

  loading.value = true
  try {
    await orderStore.payOrder(orderId.value)
    ElMessage.success('支付成功')
    router.push(`/order/${orderId.value}`)
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/orders')
}
</script>

<style scoped>
.payment-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.payment-container h2 {
  margin-bottom: 20px;
  color: #333;
}

.order-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
}

.info-row .label {
  width: 100px;
  color: #666;
}

.info-row .value {
  color: #333;
}

.items-card {
  margin-bottom: 20px;
}

.product-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.summary-card {
  margin-bottom: 20px;
}

.summary-content {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  color: #666;
}

.summary-row.total {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #eee;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.amount {
  color: #ff4d4f;
  font-size: 24px;
}

.payment-method {
  margin-bottom: 30px;
}

.payment-method h3 {
  margin-bottom: 15px;
  color: #333;
}

.el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}
</style>
