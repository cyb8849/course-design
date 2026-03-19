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
          <span class="label">用户ID：</span>
          <span class="value">{{ order.userId }}</span>
        </div>
        <div class="info-row">
          <span class="label">下单时间：</span>
          <span class="value">{{ formatDate(order.createTime) }}</span>
        </div>
        <div class="info-row">
          <span class="label">支付时间：</span>
          <span class="value">{{ order.paymentTime ? formatDate(order.paymentTime) : '未支付' }}</span>
        </div>
        <div class="info-row">
          <span class="label">发货时间：</span>
          <span class="value">{{ order.shippingTime ? formatDate(order.shippingTime) : '未发货' }}</span>
        </div>
        <div class="info-row">
          <span class="label">完成时间：</span>
          <span class="value">{{ order.deliveryTime ? formatDate(order.deliveryTime) : '未完成' }}</span>
        </div>
        <div class="info-row">
          <span class="label">订单金额：</span>
          <span class="value">¥{{ order.totalAmount }}</span>
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
                <img :src="getProductImage(scope.row.productImage)" :alt="scope.row.productName" class="product-image" />
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
          <el-table-column prop="farmerId" label="农户ID" width="100" />
        </el-table>
        <div class="total-row">
          <span class="total-label">合计：</span>
          <span class="total-amount">¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
      </el-card>
    </div>
    
    <div class="order-actions">
      <el-button @click="goBack">返回订单列表</el-button>
      <el-button v-if="order.status === 'PAID'" type="primary" @click="openShipDialog">发货</el-button>
      <el-button type="warning" @click="openStatusDialog">更新状态</el-button>
    </div>
    
    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="发货"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="shipForm" label-width="100px">
        <el-form-item label="订单号">
          <span>{{ order.orderNo }}</span>
        </el-form-item>
        <el-form-item label="快递公司" required>
          <el-select
            v-model="shipForm.expressCompany"
            placeholder="请选择快递公司"
            style="width: 100%"
          >
            <el-option
              v-for="company in expressCompanies"
              :key="company.code"
              :label="company.name"
              :value="company.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" required>
          <el-input
            v-model="shipForm.trackingNo"
            placeholder="请输入快递单号"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="shipDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleShip" :loading="shipLoading">
            确认发货
          </el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 更新状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新订单状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="订单状态" required>
          <el-select
            v-model="statusForm.status"
            placeholder="请选择状态"
            style="width: 100%"
          >
            <el-option label="待支付" value="PENDING"></el-option>
            <el-option label="已支付" value="PAID"></el-option>
            <el-option label="已发货" value="SHIPPED"></el-option>
            <el-option label="已送达" value="DELIVERED"></el-option>
            <el-option label="已取消" value="CANCELLED"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="statusDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStatusUpdate" :loading="statusLoading">
            确认更新
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const shipDialogVisible = ref(false)
const statusDialogVisible = ref(false)
const shipLoading = ref(false)
const statusLoading = ref(false)

const order = ref({
  id: '',
  orderNo: '',
  userId: '',
  status: '',
  totalAmount: 0,
  createTime: '',
  paymentTime: '',
  shippingTime: '',
  deliveryTime: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  orderItems: []
})

const shipForm = ref({
  expressCompany: '',
  trackingNo: ''
})

const statusForm = ref({
  status: ''
})

const expressCompanies = ref([
  { code: 'SF', name: '顺丰速运' },
  { code: 'YT', name: '圆通速递' },
  { code: 'YD', name: '韵达快递' },
  { code: 'ZT', name: '中通快递' },
  { code: 'STO', name: '申通快递' },
  { code: 'EMS', name: 'EMS' },
  { code: 'JD', name: '京东物流' },
  { code: 'TT', name: '天天快递' }
])

onMounted(async () => {
  const orderId = route.params.id
  if (orderId) {
    await loadOrderDetail(orderId)
  }
})

const loadOrderDetail = async (orderId) => {
  loading.value = true
  try {
    const response = await axios.get(`/admin/orders/${orderId}`)
    if (response.data.code === 200) {
      // 调整数据结构以匹配前端期望的格式
      const data = response.data.data
      console.log('后端返回的数据:', data)
      console.log('订单项数据:', data.items)
      order.value = {
        id: data.order.id,
        orderNo: data.order.orderNo,
        userId: data.order.userId,
        status: data.order.status,
        totalAmount: data.order.totalAmount,
        createTime: data.order.createTime,
        paymentTime: data.order.paymentTime,
        shippingTime: data.order.shippingTime,
        deliveryTime: data.order.deliveryTime,
        receiverName: data.order.receiverName,
        receiverPhone: data.order.receiverPhone,
        receiverAddress: data.order.receiverAddress,
        orderItems: data.items.map(item => {
          console.log('单个订单项:', item)
          console.log('订单项的productImage:', item.productImage)
          return {
            id: item.id,
            orderId: item.orderId,
            productId: item.productId,
            farmerId: item.farmerId,
            productName: item.productName,
            price: item.price,
            quantity: item.quantity,
            subtotal: item.subtotal,
            createTime: item.createTime,
            productImage: item.productImage || '/default-product.png'
          }
        })
      }
      console.log('转换后的订单数据:', order.value)
      statusForm.value.status = order.value.status
    } else {
      ElMessage.error(response.data.message || '加载订单详情失败')
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待支付',
    'PAID': '已支付',
    'SHIPPED': '已发货',
    'DELIVERED': '已送达',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

const getProductImage = (imageUrl) => {
  console.log('处理图片路径:', imageUrl)
  if (!imageUrl || imageUrl === null || imageUrl.trim() === '') {
    console.log('使用默认图片')
    return '/default-product.png'
  }
  // 检查图片路径是否完整，如果不是完整URL，添加基础路径
  if (!imageUrl.startsWith('http://') && !imageUrl.startsWith('https://')) {
    // 检查是否是相对路径
    if (!imageUrl.startsWith('/')) {
      console.log('添加斜杠前缀')
      return '/' + imageUrl
    }
    console.log('使用相对路径:', imageUrl)
    return imageUrl
  }
  console.log('使用完整URL:', imageUrl)
  return imageUrl
}

const goBack = () => {
  router.push('/admin/orders')
}

const openShipDialog = () => {
  shipForm.value = {
    expressCompany: '',
    trackingNo: ''
  }
  shipDialogVisible.value = true
}

const handleShip = async () => {
  if (!shipForm.value.expressCompany) {
    ElMessage.warning('请选择快递公司')
    return
  }
  if (!shipForm.value.trackingNo) {
    ElMessage.warning('请输入快递单号')
    return
  }
  
  shipLoading.value = true
  try {
    const response = await axios.post(`/admin/order/${order.value.id}/ship`, {
      expressCompany: shipForm.value.expressCompany,
      trackingNo: shipForm.value.trackingNo
    })
    
    if (response.data.code === 200) {
      ElMessage.success('发货成功')
      shipDialogVisible.value = false
      await loadOrderDetail(order.value.id)
    } else {
      ElMessage.error(response.data.message || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    shipLoading.value = false
  }
}

const openStatusDialog = () => {
  statusForm.value.status = order.value.status
  statusDialogVisible.value = true
}

const handleStatusUpdate = async () => {
  if (!statusForm.value.status) {
    ElMessage.warning('请选择订单状态')
    return
  }
  
  statusLoading.value = true
  try {
    const response = await axios.put(`/admin/orders/${order.value.id}/status`, {}, {
      params: {
        status: statusForm.value.status
      }
    })
    
    if (response.data.code === 200) {
      ElMessage.success('状态更新成功')
      statusDialogVisible.value = false
      await loadOrderDetail(order.value.id)
    } else {
      ElMessage.error(response.data.message || '状态更新失败')
    }
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  } finally {
    statusLoading.value = false
  }
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-status {
  font-size: 14px;
  font-weight: bold;
}

.status-PENDING {
  color: #faad14;
}

.status-PAID {
  color: #1890ff;
}

.status-SHIPPED {
  color: #52c41a;
}

.status-DELIVERED {
  color: #13c2c2;
}

.status-CANCELLED {
  color: #f5222d;
}

.info-row {
  margin-bottom: 15px;
  display: flex;
}

.label {
  width: 100px;
  color: #666;
}

.value {
  flex: 1;
  color: #333;
}

.order-items {
  margin-top: 30px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 10px;
}

.product-info {
  display: flex;
  align-items: center;
}

.total-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #e8e8e8;
}

.total-label {
  font-weight: bold;
  margin-right: 10px;
  color: #333;
}

.total-amount {
  font-weight: bold;
  color: #ff4d4f;
  font-size: 18px;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 10px;
}
</style>