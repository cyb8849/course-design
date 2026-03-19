<template>
  <div class="farmer-orders">
    <h2>订单管理</h2>
    
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
    </div>
    
    <div v-else class="order-list">
      <el-table :data="orders" style="width: 100%" border>
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="receiverName" label="客户姓名" width="120" />
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
        <el-table-column prop="paymentTime" label="支付时间" width="180" />
        <el-table-column prop="shippingTime" label="发货时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewOrderDetail(scope.row.id)">查看</el-button>
            <el-button v-if="scope.row.status === 'PAID'" size="small" type="success" @click="openShipDialog(scope.row)">发货</el-button>
          </template>
        </el-table-column>
      </el-table>
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
    
    <!-- 发货对话框 -->
    <ShipDialog
      v-model="shipDialogVisible"
      :order-id="currentOrder?.id"
      :order-no="currentOrder?.orderNo"
      @success="handleShipSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store'
import { ElMessage } from 'element-plus'
import axios from 'axios'
import ShipDialog from './components/ShipDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const shipDialogVisible = ref(false)
const currentOrder = ref(null)

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await loadOrders()
  }
})

const loadOrders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/farmer/orders', {
      params: {
        farmerId: userStore.user.id,
        page: currentPage.value,
        size: pageSize.value,
        status: statusFilter.value
      }
    })
    
    orders.value = response.data.data?.records || []
    total.value = response.data.data?.total || 0
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('加载订单失败')
  } finally {
    loading.value = false
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

const viewOrderDetail = (orderId) => {
  router.push(`/farmer/order/${orderId}`)
}

const openShipDialog = (order) => {
  currentOrder.value = order
  shipDialogVisible.value = true
}

const handleShipSuccess = async () => {
  await loadOrders()
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
</script>

<style scoped>
.farmer-orders {
  padding: 20px;
}

.farmer-orders h2 {
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style>