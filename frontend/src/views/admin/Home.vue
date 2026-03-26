<template>
  <div class="home">
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon user-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.userCount }}</div>
          <div class="stat-label">用户总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon product-icon">
          <el-icon><Goods /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.productCount }}</div>
          <div class="stat-label">商品总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon order-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.orderCount }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending-icon">
          <el-icon><Time /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.pendingProducts }}</div>
          <div class="stat-label">待审核商品</div>
        </div>
      </div>
    </div>
    
    <div class="charts-section">
      <div class="charts-grid">
        <div class="chart-card">
          <div class="card-header">
            <span>销量趋势</span>
          </div>
          <div id="salesChart" class="chart"></div>
        </div>
        <div class="chart-card">
          <div class="card-header">
            <span>商品分类占比</span>
          </div>
          <div id="categoryChart" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { useStatisticsStore } from '../../store'
import { ElMessage } from 'element-plus'

const stats = ref({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  pendingProducts: 0
})

let salesChart = null
let categoryChart = null

const statisticsStore = useStatisticsStore()

onMounted(async () => {
  // 获取统计数据
  try {
    const overviewData = await statisticsStore.getOverviewStatistics()
    stats.value = {
      userCount: overviewData.totalUsers || 0,
      productCount: overviewData.totalProducts || 0,
      orderCount: overviewData.totalOrders || 0,
      pendingProducts: overviewData.pendingProducts || 0
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
  
  // 初始化图表
  await initSalesChart()
  await initCategoryChart()
})

onUnmounted(() => {
  // 销毁图表
  if (salesChart) {
    salesChart.dispose()
  }
  if (categoryChart) {
    categoryChart.dispose()
  }
})

const initSalesChart = async () => {
  try {
    const data = await statisticsStore.getSalesStatistics(30)
    
    salesChart = echarts.init(document.getElementById('salesChart'))
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
    salesChart.setOption(option)
  } catch (error) {
    console.error('加载销售趋势数据失败:', error)
    ElMessage.error('加载销售趋势数据失败')
  }
}

const initCategoryChart = async () => {
  try {
    const data = await statisticsStore.getCategoryStatistics()
    
    categoryChart = echarts.init(document.getElementById('categoryChart'))
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
          name: '商品分类',
          type: 'pie',
          radius: '50%',
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
    categoryChart.setOption(option)
  } catch (error) {
    console.error('加载分类占比数据失败:', error)
    ElMessage.error('加载分类占比数据失败')
  }
}
</script>

<style scoped>
.home {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 120px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: box-shadow 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 20px;
  font-size: 24px;
}

.user-icon {
  background-color: #ECF5FF;
  color: #409EFF;
}

.product-icon {
  background-color: #F0F9EB;
  color: #67C23A;
}

.order-icon {
  background-color: #FEFCE8;
  color: #E6A23C;
}

.pending-icon {
  background-color: #FDF2F8;
  color: #F56C6C;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

.charts-section {
  margin-top: 20px;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-card {
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  font-weight: bold;
  color: #303133;
}

.chart {
  height: 300px;
  width: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .charts-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    height: 100px;
    padding: 15px;
  }
  
  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 20px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .chart {
    height: 250px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .home {
    padding: 10px;
  }
  
  .stat-card {
    height: 90px;
  }
  
  .chart {
    height: 200px;
  }
}
</style>
