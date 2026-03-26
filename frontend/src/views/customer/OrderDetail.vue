<template>
  <div class="order-detail-container" v-loading="loading">
    <h2>订单详情</h2>
    
    <div class="order-info">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>订单信息</span>
            <span class="order-status" :class="`status-${order.status}`">
              {{ getStatusText(order.status) }}
            </span>
          </div>
        </template>
        <div class="info-row">
          <span class="label">订单号：</span>
          <span class="value">{{ order.orderNo }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间：</span>
          <span class="value">{{ formatDate(order.createdAt) }}</span>
        </div>
        <div class="info-row">
          <span class="label">支付时间：</span>
          <span class="value">{{ order.paidAt ? formatDate(order.paidAt) : '未支付' }}</span>
        </div>
        <div class="info-row">
          <span class="label">发货时间：</span>
          <span class="value">{{ order.shippedAt ? formatDate(order.shippedAt) : '未发货' }}</span>
        </div>
        <div class="info-row">
          <span class="label">完成时间：</span>
          <span class="value">{{ order.completedAt ? formatDate(order.completedAt) : '未完成' }}</span>
        </div>
      </el-card>
    </div>
    
    <div class="address-info">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>收货信息</span>
          </div>
        </template>
        <div class="info-row">
          <span class="label">收货人：</span>
          <span class="value">{{ order.address?.name }}</span>
        </div>
        <div class="info-row">
          <span class="label">联系电话：</span>
          <span class="value">{{ order.address?.phone }}</span>
        </div>
        <div class="info-row">
          <span class="label">收货地址：</span>
          <span class="value">{{ order.address?.province }}{{ order.address?.city }}{{ order.address?.district }}{{ order.address?.detail }}</span>
        </div>
      </el-card>
    </div>

    <!-- 物流信息 -->
    <div class="logistics-info" v-if="order.status === 'SHIPPED' || order.status === 'COMPLETED'">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>物流信息</span>
          </div>
        </template>
        <div v-if="logisticsInfoList && logisticsInfoList.length > 0">
          <div v-for="(logistics, index) in logisticsInfoList" :key="logistics.id" class="logistics-item">
            <div class="logistics-header">
              <h4>物流 {{ index + 1 }}</h4>
              <el-tag :type="getLogisticsStatusType(logistics.status)">
                {{ getLogisticsStatusText(logistics.status) }}
              </el-tag>
            </div>
            <div class="info-row">
              <span class="label">快递公司：</span>
              <span class="value">{{ logistics.expressCompanyName }}</span>
            </div>
            <div class="info-row">
              <span class="label">快递单号：</span>
              <span class="value">{{ logistics.trackingNo }}</span>
              <el-button size="small" type="primary" @click="copyTrackingNo(logistics.trackingNo)">复制</el-button>
            </div>
            <div class="info-row">
              <span class="label">最新状态：</span>
              <span class="value">{{ logistics.latestInfo || '暂无更新' }}</span>
            </div>
            <div class="info-row">
              <span class="label">更新时间：</span>
              <span class="value">{{ formatDate(logistics.latestTime) }}</span>
            </div>
            <div class="logistics-actions">
              <el-button type="primary" @click="viewLogisticsDetail(logistics.trackingNo, logistics.expressCompany)">查看物流详情</el-button>
            </div>
            <div v-if="index < logisticsInfoList.length - 1" class="logistics-divider"></div>
          </div>
        </div>
        <div v-else class="no-logistics">
          <el-empty description="暂无物流信息" />
        </div>
      </el-card>
    </div>
    
    <div class="order-items">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>商品信息</span>
          </div>
        </template>
        <el-table :data="order.orderItems" style="width: 100%" border>
          <el-table-column prop="productName" label="商品名称" width="300">
            <template #default="scope">
              <div class="product-info">
                <span>{{ scope.row.productName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="quantity" label="数量" width="100">
            <template #default="scope">
              {{ scope.row.quantity }}
            </template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="120">
            <template #default="scope">
              ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
        <div class="total-row">
          <span class="total-label">合计：</span>
          <span class="total-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
      </el-card>
    </div>
    
    <div class="order-actions">
      <el-button @click="goBack">返回订单列表</el-button>
      <el-button v-if="order.status === 'PENDING'" type="primary" @click="payOrder">支付</el-button>
      <el-button v-if="order.status === 'PENDING'" type="danger" @click="cancelOrder">取消</el-button>
      <el-button v-if="order.status === 'SHIPPED'" type="success" @click="confirmReceipt">确认收货</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useOrderStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const orderStore = useOrderStore()
const userStore = useUserStore()

const loading = ref(false)
const order = ref({
  id: '',
  status: '',
  totalAmount: 0,
  createdAt: '',
  paidAt: '',
  shippedAt: '',
  completedAt: '',
  address: null,
  orderItems: []
})

const logisticsInfoList = ref([])

onMounted(async () => {
  if (userStore.isLoggedIn) {
    const orderId = route.params.id
    if (orderId) {
      await loadOrderDetail(orderId)
    }
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

const loadOrderDetail = async (orderId) => {
  loading.value = true
  try {
    const result = await orderStore.getOrderDetail(orderId)
    order.value = {
      id: result.order.id,
      orderNo: result.order.orderNo,
      status: result.order.status,
      totalAmount: result.order.totalAmount,
      createdAt: result.order.createTime,
      paidAt: result.order.paymentTime,
      shippedAt: result.order.shippingTime,
      completedAt: result.order.deliveryTime,
      address: result.address ? {
        name: result.address.consignee,
        phone: result.address.phone,
        province: result.address.province,
        city: result.address.city,
        district: result.address.district,
        detail: result.address.detailAddress
      } : null,
      orderItems: result.items || []
    }
    
    // 加载物流信息
    if (order.value.status === 'SHIPPED' || order.value.status === 'COMPLETED') {
      await loadLogisticsInfo(orderId)
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

// 加载物流信息
const loadLogisticsInfo = async (orderId) => {
  try {
    const response = await axios.get(`/logistics/order/${orderId}`)
    if (response.data.code === 200) {
      logisticsInfoList.value = response.data.data
    }
  } catch (error) {
    console.error('加载物流信息失败:', error)
    logisticsInfoList.value = []
  }
}

// 获取物流状态类型
const getLogisticsStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取物流状态文本
const getLogisticsStatusText = (status) => {
  const statusMap = {
    0: '无轨迹',
    1: '已揽收',
    2: '运输中',
    3: '已签收',
    4: '问题件'
  }
  return statusMap[status] || '未知'
}

// 复制快递单号
const copyTrackingNo = (trackingNo) => {
  if (trackingNo) {
    navigator.clipboard.writeText(trackingNo)
    ElMessage.success('快递单号已复制')
  }
}

// 查看物流详情
const viewLogisticsDetail = (trackingNo, expressCompany) => {
  router.push({
    path: '/logistics',
    query: { trackingNo, expressCompany }
  })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
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

const payOrder = async () => {
  try {
    await orderStore.payOrder(order.value.id)
    ElMessage.success('支付成功')
    await loadOrderDetail(order.value.id)
  } catch (error) {
    ElMessage.error('支付失败：' + (error.response?.data?.message || error.message))
  }
}

const cancelOrder = async () => {
  try {
    await orderStore.cancelOrder(order.value.id)
    ElMessage.success('取消成功')
    await loadOrderDetail(order.value.id)
  } catch (error) {
    ElMessage.error('取消失败：' + (error.response?.data?.message || error.message))
  }
}

const confirmReceipt = async () => {
  try {
    await orderStore.confirmReceipt(order.value.id)
    ElMessage.success('确认收货成功')
    await loadOrderDetail(order.value.id)
  } catch (error) {
    ElMessage.error('确认收货失败：' + (error.response?.data?.message || error.message))
  }
}

const goBack = () => {
  router.push('/orders')
}
</script>

<style scoped>
.order-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.order-detail-container h2 {
  margin-bottom: 30px;
  color: #333;
}

.order-info,
.address-info,
.order-items {
  margin-bottom: 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-row {
  display: flex;
  margin-bottom: 15px;
}

.label {
  width: 100px;
  color: #666;
}

.value {
  flex: 1;
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

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.total-label {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.logistics-item {
  margin-bottom: 20px;
}

.logistics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e8e8e8;
}

.logistics-header h4 {
  margin: 0;
  color: #333;
  font-size: 16px;
}

.logistics-divider {
  height: 1px;
  background-color: #e8e8e8;
  margin: 20px 0;
}

.logistics-actions {
  margin-top: 15px;
  display: flex;
  justify-content: flex-start;
}
</style>