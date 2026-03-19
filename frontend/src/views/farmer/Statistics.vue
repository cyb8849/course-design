<template>
  <div class="farmer-statistics">
    <h2>数据统计</h2>
    
    <div class="stats-grid">
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>总销售额</span>
          </div>
        </template>
        <div class="stat-value">¥{{ totalSales }}</div>
        <div class="stat-desc">较上月增长 {{ salesGrowth }}%</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>总订单数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalOrders }}</div>
        <div class="stat-desc">较上月增长 {{ ordersGrowth }}%</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>客单价</span>
          </div>
        </template>
        <div class="stat-value">¥{{ avgOrderValue }}</div>
        <div class="stat-desc">较上月增长 {{ avgOrderGrowth }}%</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>商品总数</span>
          </div>
        </template>
        <div class="stat-value">{{ productCount }}</div>
        <div class="stat-desc">在售商品 {{ activeProducts }} 件</div>
      </el-card>
    </div>
    
    <div class="charts-container">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>销售趋势</span>
          </div>
        </template>
        <div ref="salesChartRef" class="chart"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>商品销量排行</span>
          </div>
        </template>
        <div ref="productChartRef" class="chart"></div>
      </el-card>
    </div>
    
    <div class="charts-container">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>订单状态分布</span>
          </div>
        </template>
        <div ref="orderStatusChartRef" class="chart"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>库存预警</span>
          </div>
        </template>
        <el-table :data="lowStockProducts" style="width: 100%" border>
          <el-table-column prop="name" label="商品名称" />
          <el-table-column prop="stock" label="库存" />
          <el-table-column prop="threshold" label="预警阈值" />
          <el-table-column label="操作">
            <template #default="scope">
              <el-button size="small" type="primary" @click="openRestockDialog(scope.row)">补货</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
    
    <!-- 补货对话框 -->
    <el-dialog
      v-model="restockDialogVisible"
      title="补货"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="restockForm" label-width="100px">
        <el-form-item label="商品名称">
          <span>{{ restockForm.productName }}</span>
        </el-form-item>
        <el-form-item label="当前库存">
          <span>{{ restockForm.currentStock }}</span>
        </el-form-item>
        <el-form-item label="补货数量" required>
          <el-input-number
            v-model="restockForm.quantity"
            :min="1"
            :max="1000"
            :step="1"
            placeholder="请输入补货数量"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="restockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleRestock" :loading="restockLoading">
            确认补货
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'
import { useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

// 响应式数据
const userStore = useUserStore()
const loading = ref(false)
const totalSales = ref(0)
const salesGrowth = ref(0)
const totalOrders = ref(0)
const ordersGrowth = ref(0)
const avgOrderValue = ref(0)
const avgOrderGrowth = ref(0)
const productCount = ref(0)
const activeProducts = ref(0)

const salesChartRef = ref(null)
const productChartRef = ref(null)
const orderStatusChartRef = ref(null)

const lowStockProducts = ref([])

// 补货相关数据
const restockDialogVisible = ref(false)
const restockLoading = ref(false)
const restockForm = ref({
  productId: 0,
  productName: '',
  currentStock: 0,
  quantity: 1
})

// 图表数据
const salesData = ref([])
const orderData = ref([])
const productSalesData = ref([])
const productNames = ref([])
const orderStatusData = ref([])

// 存储从后端获取的日期数据
const chartDates = ref([])

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
      totalOrders.value = orders.length || 0
      
      // 计算总销售额（只计算该农户相关的商品金额）
      totalSales.value = orders.reduce((sum, order) => {
        const farmerOrderAmount = (order.orderItems || []).reduce((itemSum, item) => {
          return itemSum + (item.subtotal || 0)
        }, 0)
        return sum + farmerOrderAmount
      }, 0)
      
      // 计算客单价
      if (totalOrders.value > 0) {
        avgOrderValue.value = totalSales.value / totalOrders.value
      }
      
      // 计算订单状态分布
      const statusMap = {
        'PENDING': 0,
        'PAID': 0,
        'SHIPPED': 0,
        'DELIVERED': 0,
        'CANCELLED': 0
      }
      
      orders.forEach(order => {
        if (statusMap.hasOwnProperty(order.status)) {
          statusMap[order.status]++
        }
      })
      
      orderStatusData.value = [
        { value: statusMap.PENDING, name: '待支付' },
        { value: statusMap.PAID, name: '已支付' },
        { value: statusMap.SHIPPED, name: '已发货' },
        { value: statusMap.DELIVERED, name: '已完成' },
        { value: statusMap.CANCELLED, name: '已取消' }
      ]
    }
    
    // 加载低库存商品
    const productsResponse = await axios.get('/farmer/products', {
      params: {
        farmerId: userStore.user.id,
        page: 1,
        size: 100
      }
    })
    
    if (productsResponse.data.code === 200) {
      const products = productsResponse.data.data?.records || []
      // 假设库存阈值为20
      lowStockProducts.value = products
        .filter(product => (product.stock || 0) < 20)
        .map(product => ({
          id: product.id,
          name: product.productName,
          stock: product.stock || 0,
          threshold: 20
        }))
    }
    
    // 加载销售趋势数据
    const salesResponse = await axios.get(`/farmer/statistics/sales?farmerId=${userStore.user.id}&days=30`)
    if (salesResponse.data.code === 200) {
      const salesDataResult = salesResponse.data.data
      salesData.value = salesDataResult.amounts || [0, 0, 0, 0, 0, 0]
      orderData.value = salesDataResult.orderCounts || [0, 0, 0, 0, 0, 0]
      chartDates.value = salesDataResult.dates || []
    }
    
    // 加载商品销量排行数据
    const productSalesResponse = await axios.get(`/farmer/statistics/product-sales?farmerId=${userStore.user.id}&limit=6`)
    if (productSalesResponse.data.code === 200) {
      const productDataResult = productSalesResponse.data.data
      productNames.value = productDataResult.productNames || ['暂无数据']
      productSalesData.value = productDataResult.sales || [0]
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadData()
  await nextTick()
  initCharts()
})

const initCharts = () => {
  initSalesChart()
  initProductChart()
  initOrderStatusChart()
}

const initSalesChart = () => {
  const chart = echarts.init(salesChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['销售额', '订单数']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartDates.value.length > 0 ? chartDates.value : ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        position: 'left'
      },
      {
        type: 'value',
        name: '订单数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'line',
        data: salesData.value.length > 0 ? salesData.value : [0, 0, 0, 0, 0, 0],
        smooth: true,
        lineStyle: {
          color: '#1890ff'
        }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: orderData.value.length > 0 ? orderData.value : [0, 0, 0, 0, 0, 0],
        smooth: true,
        lineStyle: {
          color: '#52c41a'
        }
      }
    ]
  }
  chart.setOption(option)
}

const initProductChart = () => {
  const chart = echarts.init(productChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: productNames.value.length > 0 ? productNames.value : ['暂无数据']
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '销量',
        type: 'bar',
        data: productSalesData.value.length > 0 ? productSalesData.value : [0],
        itemStyle: {
          color: '#1890ff'
        }
      }
    ]
  }
  chart.setOption(option)
}

const initOrderStatusChart = () => {
  const chart = echarts.init(orderStatusChartRef.value)
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '订单状态',
        type: 'pie',
        radius: '60%',
        data: orderStatusData.value.length > 0 ? orderStatusData.value : [{ value: 1, name: '暂无数据' }],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
}

// 打开补货对话框
const openRestockDialog = (product) => {
  restockForm.value = {
    productId: product.id,
    productName: product.name,
    currentStock: product.stock,
    quantity: 1
  }
  restockDialogVisible.value = true
}

// 处理补货
const handleRestock = async () => {
  if (!restockForm.value.quantity || restockForm.value.quantity <= 0) {
    ElMessage.warning('请输入有效的补货数量')
    return
  }
  
  restockLoading.value = true
  try {
    // 调用后端API进行补货
    const response = await axios.put(`/farmer/products/${restockForm.value.productId}/stock`, {
      quantity: restockForm.value.quantity
    })
    
    if (response.data.code === 200) {
      ElMessage.success('补货成功')
      restockDialogVisible.value = false
      // 重新加载数据
      await loadData()
    } else {
      ElMessage.error(response.data.message || '补货失败')
    }
  } catch (error) {
    console.error('补货失败:', error)
    ElMessage.error('补货失败')
  } finally {
    restockLoading.value = false
  }
}
</script>

<style scoped>
.farmer-statistics {
  padding: 20px;
}

.farmer-statistics h2 {
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

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  height: 400px;
}

.chart {
  width: 100%;
  height: 340px;
}
</style>