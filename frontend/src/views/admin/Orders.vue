<template>
  <div class="orders">
    <el-table :data="orders" style="width: 100%">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="totalAmount" label="订单金额" width="120">
        <template #default="scope">
          ¥{{ scope.row.totalAmount }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="订单状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" size="small" @click="viewOrder(scope.row)">查看</el-button>
          <el-button size="small" @click="updateOrderStatus(scope.row)">更新状态</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 更新订单状态对话框 -->
    <el-dialog v-model="statusDialogVisible" title="更新订单状态" width="400px">
      <el-form :model="statusForm" ref="statusFormRef">
        <el-form-item label="订单状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态">
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
          <el-button type="primary" @click="saveOrderStatus">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const router = useRouter()
const orders = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusDialogVisible = ref(false)
const statusForm = ref({
  orderId: null,
  status: ''
})
const statusFormRef = ref(null)

onMounted(async () => {
  await loadOrders()
})

const loadOrders = async () => {
  try {
    const response = await axios.get('/admin/orders', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    orders.value = response.data.data?.records || []
    total.value = response.data.data?.total || 0
  } catch (error) {
    console.error('获取订单失败:', error)
  }
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  await loadOrders()
}

const handleCurrentChange = async (current) => {
  currentPage.value = current
  await loadOrders()
}

const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'PAID': return 'primary'
    case 'SHIPPED': return 'info'
    case 'DELIVERED': return 'success'
    case 'CANCELLED': return 'danger'
    default: return ''
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '待支付'
    case 'PAID': return '已支付'
    case 'SHIPPED': return '已发货'
    case 'DELIVERED': return '已送达'
    case 'CANCELLED': return '已取消'
    default: return status
  }
}

const viewOrder = (order) => {
  // 查看订单详情
  console.log('查看订单:', order)
  router.push(`/admin/orders/${order.id}`)
}

const updateOrderStatus = (order) => {
  statusForm.value = {
    orderId: order.id,
    status: order.status
  }
  statusDialogVisible.value = true
}

const saveOrderStatus = async () => {
  try {
    await axios.put(`/admin/orders/${statusForm.value.orderId}/status`, {}, {
      params: {
        status: statusForm.value.status
      }
    })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    await loadOrders()
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
  }
}
</script>

<style scoped>
.orders {
  padding: 20px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>