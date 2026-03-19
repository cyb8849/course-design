<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon user-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon product-icon">
            <el-icon><Goods /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.productCount }}</div>
            <div class="stat-label">商品总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon order-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon pending-icon">
            <el-icon><Time /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingProducts }}</div>
            <div class="stat-label">待审核商品</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>销量趋势</span>
              </div>
            </template>
            <div id="salesChart" class="chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>商品分类占比</span>
              </div>
            </template>
            <div id="categoryChart" class="chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const stats = ref({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  pendingProducts: 0
})

let salesChart = null
let categoryChart = null

onMounted(async () => {
  // 获取统计数据
  try {
    const response = await axios.get('/admin/statistics/overview')
    if (response.data && response.data.data) {
      stats.value = {
        userCount: response.data.data.totalUsers || 0,
        productCount: response.data.data.totalProducts || 0,
        orderCount: response.data.data.totalOrders || 0,
        pendingProducts: response.data.data.pendingProducts || 0
      }
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
  
  // 初始化图表
  initSalesChart()
  initCategoryChart()
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

const initSalesChart = () => {
  salesChart = echarts.init(document.getElementById('salesChart'))
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: ['1月', '2月', '3月', '4月', '5月', '6月']
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      data: [120, 200, 150, 80, 70, 110],
      type: 'line',
      smooth: true
    }]
  }
  salesChart.setOption(option)
}

const initCategoryChart = () => {
  categoryChart = echarts.init(document.getElementById('categoryChart'))
  const option = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '商品分类',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 30, name: '粉藕' },
        { value: 25, name: '脆藕' },
        { value: 20, name: '藕尖' },
        { value: 25, name: '其他' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }
  categoryChart.setOption(option)
}
</script>

<style scoped>
.home {
  padding: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  height: 120px;
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 300px;
  width: 100%;
}
</style>