<template>
  <div class="logistics-tracking-container">
    <h2>物流追踪</h2>
    
    <!-- 查询区域 -->
    <el-card class="search-card">
      <div class="search-form">
        <el-input
          v-model="searchOrderNo"
          placeholder="请输入订单号"
          style="width: 300px; margin-right: 10px;"
        />
        <el-button type="primary" @click="searchLogistics">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
      </div>
    </el-card>

    <!-- 物流信息展示 -->
    <el-card v-if="logisticsInfo" class="logistics-card">
      <template #header>
        <div class="card-header">
          <span>物流信息</span>
          <el-tag :type="getStatusType(logisticsInfo.status)">
            {{ getStatusText(logisticsInfo.status) }}
          </el-tag>
        </div>
      </template>
      
      <div class="logistics-info">
        <div class="info-row">
          <span class="label">快递公司：</span>
          <span class="value">{{ logisticsInfo.expressCompanyName }}</span>
        </div>
        <div class="info-row">
          <span class="label">快递单号：</span>
          <span class="value">{{ logisticsInfo.trackingNo }}</span>
          <el-button size="small" type="primary" @click="copyTrackingNo">
            复制
          </el-button>
        </div>
        <div class="info-row">
          <span class="label">最新状态：</span>
          <span class="value">{{ logisticsInfo.latestInfo || '暂无更新' }}</span>
        </div>
        <div class="info-row">
          <span class="label">更新时间：</span>
          <span class="value">{{ formatDate(logisticsInfo.latestTime) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 物流轨迹时间线 -->
    <el-card v-if="trackingList.length > 0" class="tracking-card">
      <template #header>
        <div class="card-header">
          <span>物流轨迹</span>
        </div>
      </template>
      
      <el-timeline>
        <el-timeline-item
          v-for="(item, index) in trackingList"
          :key="index"
          :type="index === 0 ? 'primary' : ''"
          :hollow="index !== 0"
        >
          <div class="tracking-item">
            <div class="tracking-time">{{ item.time }}</div>
            <div class="tracking-context">{{ item.context }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <!-- 无物流信息提示 -->
    <el-card v-else-if="searched" class="empty-card">
      <el-empty description="暂无物流信息">
        <template #description>
          <p>该订单暂无物流信息</p>
          <p class="sub-text">请确认订单已发货或联系客服</p>
        </template>
      </el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { useUserStore } from '../../store'

const route = useRoute()
const userStore = useUserStore()

const searchOrderNo = ref('')
const logisticsInfo = ref(null)
const trackingList = ref([])
const searched = ref(false)

// 页面加载时检查URL参数
onMounted(() => {
  if (route.query.orderNo) {
    searchOrderNo.value = route.query.orderNo
    searchLogistics()
  } else if (route.query.trackingNo && route.query.expressCompany) {
    // 直接通过快递单号和快递公司查询物流轨迹
    searchByTrackingNo(route.query.trackingNo, route.query.expressCompany)
  }
})

// 查询物流信息
const searchLogistics = async () => {
  if (!searchOrderNo.value.trim()) {
    ElMessage.warning('请输入订单号')
    return
  }

  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    // 直接根据订单号查询订单
    const orderResponse = await axios.get('/customer/order/by-order-no', {
      params: {
        orderNo: searchOrderNo.value
      }
    })
    
    if (orderResponse.data.code !== 200 || !orderResponse.data.data) {
      ElMessage.error('未找到该订单')
      searched.value = true
      logisticsInfo.value = null
      trackingList.value = []
      return
    }
    
    const order = orderResponse.data.data

    // 查询物流信息
    const logisticsResponse = await axios.get(`/logistics/order/${order.id}`)
    
    if (logisticsResponse.data.code === 200) {
      logisticsInfo.value = logisticsResponse.data.data
      
      // 查询物流轨迹
      const trackingResponse = await axios.get('/logistics/tracking', {
        params: {
          trackingNo: logisticsInfo.value.trackingNo,
          expressCompany: logisticsInfo.value.expressCompany
        }
      })
      
      if (trackingResponse.data.code === 200) {
        trackingList.value = trackingResponse.data.data
      }
    } else {
      logisticsInfo.value = null
      trackingList.value = []
      ElMessage.info(logisticsResponse.data.message || '暂无物流信息')
    }
    
    searched.value = true
  } catch (error) {
    console.error('查询物流失败:', error)
    ElMessage.error('查询物流失败')
    searched.value = true
    logisticsInfo.value = null
    trackingList.value = []
  }
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '无轨迹',
    1: '已揽收',
    2: '运输中',
    3: '已签收',
    4: '问题件'
  }
  return statusMap[status] || '未知'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 复制快递单号
const copyTrackingNo = () => {
  if (logisticsInfo.value?.trackingNo) {
    navigator.clipboard.writeText(logisticsInfo.value.trackingNo)
    ElMessage.success('快递单号已复制')
  }
}

// 通过快递单号和快递公司查询物流轨迹
const searchByTrackingNo = async (trackingNo, expressCompany) => {
  try {
    // 查询物流轨迹
    const trackingResponse = await axios.get('/logistics/tracking', {
      params: {
        trackingNo,
        expressCompany
      }
    })
    
    if (trackingResponse.data.code === 200) {
      trackingList.value = trackingResponse.data.data
      
      // 创建一个临时的物流信息对象
      logisticsInfo.value = {
        expressCompany,
        expressCompanyName: getExpressCompanyName(expressCompany),
        trackingNo,
        status: 0,
        latestInfo: trackingList.value.length > 0 ? trackingList.value[0].context : '暂无更新',
        latestTime: trackingList.value.length > 0 ? trackingList.value[0].time : null
      }
    } else {
      trackingList.value = []
      logisticsInfo.value = null
      ElMessage.info(trackingResponse.data.message || '暂无物流信息')
    }
    
    searched.value = true
  } catch (error) {
    console.error('查询物流失败:', error)
    ElMessage.error('查询物流失败')
    searched.value = true
    logisticsInfo.value = null
    trackingList.value = []
  }
}

// 获取快递公司名称
const getExpressCompanyName = (code) => {
  const companyMap = {
    'SF': '顺丰速运',
    'YTO': '圆通速递',
    'ZTO': '中通快递',
    'YUNDA': '韵达速递',
    'EMS': 'EMS',
    'JD': '京东物流',
    'STO': '申通快递',
    'HTKY': '百世快递',
    'UC': '邮政快递',
    'DBL': '德邦物流'
  }
  return companyMap[code] || '未知快递'
}
</script>

<style scoped>
.logistics-tracking-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
}

.logistics-card,
.tracking-card,
.empty-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logistics-info {
  padding: 10px 0;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.info-row .label {
  width: 100px;
  color: #666;
  font-weight: 500;
}

.info-row .value {
  flex: 1;
  color: #333;
}

.tracking-item {
  padding: 10px 0;
}

.tracking-time {
  color: #999;
  font-size: 14px;
  margin-bottom: 5px;
}

.tracking-context {
  color: #333;
  font-size: 14px;
  line-height: 1.5;
}

.sub-text {
  color: #999;
  font-size: 14px;
  margin-top: 10px;
}

:deep(.el-timeline-item__node) {
  background-color: #409eff;
}

:deep(.el-timeline-item__node.is-hollow) {
  background-color: #fff;
  border-color: #409eff;
}
</style>
