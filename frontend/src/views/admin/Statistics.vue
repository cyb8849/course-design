<template>
  <div class="admin-statistics">
    <h2>大数据可视化</h2>
    
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
            <span>总用户数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalUsers }}</div>
        <div class="stat-desc">较上月增长 {{ usersGrowth }}%</div>
      </el-card>
      <el-card shadow="hover">
        <template #header>
          <div class="card-header">
            <span>总商品数</span>
          </div>
        </template>
        <div class="stat-value">{{ totalProducts }}</div>
        <div class="stat-desc">较上月增长 {{ productsGrowth }}%</div>
      </el-card>
    </div>
    
    <div class="charts-container">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>销量趋势折线图</span>
          </div>
        </template>
        <div ref="salesTrendRef" class="chart"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>莲藕品类占比饼图</span>
          </div>
        </template>
        <div ref="categoryPieRef" class="chart"></div>
      </el-card>
    </div>
    
    <div class="charts-container">
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>农户销售额排行柱状图</span>
          </div>
        </template>
        <div ref="farmerRankingRef" class="chart"></div>
      </el-card>
      
      <el-card shadow="hover" class="chart-card">
        <template #header>
          <div class="card-header">
            <span>订单状态分布图</span>
          </div>
        </template>
        <div ref="orderStatusRef" class="chart"></div>
      </el-card>
    </div>
    
    <div class="charts-container">
      <el-card shadow="hover" class="chart-card full-width">
        <template #header>
          <div class="card-header">
            <span>库存预警表格</span>
          </div>
        </template>
        <el-table :data="inventoryAlerts" style="width: 100%" border>
          <el-table-column prop="productName" label="商品名称" width="200" />
          <el-table-column prop="categoryName" label="分类" width="120" />
          <el-table-column prop="currentStock" label="当前库存" width="120" />
          <el-table-column prop="minStock" label="最小库存" width="120" />
          <el-table-column prop="farmerName" label="农户" width="150" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="primary">通知农户</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { useStatisticsStore, useUserStore } from '../../store'

// 响应式数据
const loading = ref(true)
const totalSales = ref(0)
const salesGrowth = ref(0)
const totalOrders = ref(0)
const ordersGrowth = ref(0)
const totalUsers = ref(0)
const usersGrowth = ref(0)
const totalProducts = ref(0)
const productsGrowth = ref(0)

const salesTrendRef = ref(null)
const categoryPieRef = ref(null)
const farmerRankingRef = ref(null)
const orderStatusRef = ref(null)

const inventoryAlerts = ref([])

const statisticsStore = useStatisticsStore()
const userStore = useUserStore()

onMounted(async () => {
  console.log('Statistics component mounted')
  console.log('Is logged in:', userStore.isLoggedIn)
  
  if (!userStore.isLoggedIn) {
    ElMessage.error('请先登录')
    return
  }
  
  await loadStatistics()
  await nextTick()
  await initCharts()
  loading.value = false
  console.log('Statistics component initialized')
})

// 加载统计数据
const loadStatistics = async () => {
  try {
    console.log('Loading statistics data...')
    
    // 获取概览数据
    const overviewData = await statisticsStore.getOverviewStatistics()
    console.log('Overview data:', overviewData)
    totalUsers.value = overviewData.totalUsers || 0
    totalProducts.value = overviewData.totalProducts || 0
    totalOrders.value = overviewData.totalOrders || 0
    totalSales.value = overviewData.totalSales || 0

    // 获取库存预警数据
    await loadInventoryAlerts()
    console.log('Statistics data loaded')
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

// 加载库存预警数据
const loadInventoryAlerts = async () => {
  try {
    const data = await statisticsStore.getInventoryStatistics()
    // 使用后端返回的真实数据
    inventoryAlerts.value = data
    console.log('Inventory alerts:', data)
  } catch (error) {
    console.error('加载库存预警数据失败:', error)
  }
}

const initCharts = async () => {
  console.log('Initializing charts...')
  await initSalesTrendChart()
  await initCategoryPieChart()
  await initFarmerRankingChart()
  await initOrderStatusChart()
  console.log('Charts initialized')
}

const initSalesTrendChart = async () => {
  try {
    console.log('Loading sales trend data...')
    const data = await statisticsStore.getSalesStatistics(30)
    console.log('Sales trend data:', data)
    
    if (!salesTrendRef.value) {
      console.error('salesTrendRef is null')
      return
    }
    
    const chart = echarts.init(salesTrendRef.value)
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
        data: data.dates
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
          data: data.amounts.map(amount => Number(amount)),
          smooth: true,
          lineStyle: {
            color: '#1890ff'
          }
        },
        {
          name: '订单数',
          type: 'line',
          yAxisIndex: 1,
          data: data.orderCounts || data.amounts.map(() => 0),
          smooth: true,
          lineStyle: {
            color: '#52c41a'
          }
        }
      ]
    }
    chart.setOption(option)
    console.log('Sales trend chart initialized')
  } catch (error) {
    console.error('加载销售趋势数据失败:', error)
    ElMessage.error('加载销售趋势数据失败')
  }
}

const initCategoryPieChart = async () => {
  try {
    console.log('Loading category data...')
    const data = await statisticsStore.getCategoryStatistics()
    console.log('Category data:', data)
    
    if (!categoryPieRef.value) {
      console.error('categoryPieRef is null')
      return
    }
    
    const chart = echarts.init(categoryPieRef.value)
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
          name: '莲藕品类',
          type: 'pie',
          radius: '60%',
          data: data.categories.map((category, index) => ({
            value: data.counts[index],
            name: category
          })),
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
    console.log('Category pie chart initialized')
  } catch (error) {
    console.error('加载分类占比数据失败:', error)
    ElMessage.error('加载分类占比数据失败')
  }
}

const initFarmerRankingChart = async () => {
  try {
    console.log('Loading farmer ranking data...')
    const data = await statisticsStore.getFarmerStatistics()
    console.log('Farmer ranking data:', data)
    
    if (!farmerRankingRef.value) {
      console.error('farmerRankingRef is null')
      return
    }
    
    const chart = echarts.init(farmerRankingRef.value)
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
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: data.farmers.reverse()
      },
      series: [
        {
          name: '销售额',
          type: 'bar',
          data: data.salesAmounts.map(amount => Number(amount)).reverse(),
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          }
        }
      ]
    }
    chart.setOption(option)
    console.log('Farmer ranking chart initialized')
  } catch (error) {
    console.error('加载农户排行数据失败:', error)
    ElMessage.error('加载农户排行数据失败')
  }
}

const initOrderStatusChart = async () => {
  try {
    console.log('Loading order status data...')
    const data = await statisticsStore.getOrderStatusStatistics()
    console.log('Order status data:', data)
    
    if (!orderStatusRef.value) {
      console.error('orderStatusRef is null')
      return
    }
    
    const chart = echarts.init(orderStatusRef.value)
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
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: data.statusNames.map((name, index) => ({
            value: data.counts[index],
            name: name
          }))
        }
      ]
    }
    chart.setOption(option)
    console.log('Order status chart initialized')
  } catch (error) {
    console.error('加载订单状态数据失败:', error)
    ElMessage.error('加载订单状态数据失败')
  }
}
</script>

<style scoped>
.admin-statistics {
  padding: 20px;
}

.admin-statistics h2 {
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

.full-width {
  grid-column: 1 / -1;
}

.chart-card {
  height: 400px;
}

.chart {
  width: 100%;
  height: 340px;
}
</style>