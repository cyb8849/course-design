<template>
  <div class="traceability">
    <div class="search-section">
      <el-input v-model="productId" placeholder="请输入商品ID" style="width: 300px;">
        <template #append>
          <el-button @click="searchTraceability"><el-icon><Search /></el-icon></el-button>
        </template>
      </el-input>
    </div>
    
    <div v-if="traceability && traceability.length > 0" class="traceability-info">
      <el-card v-for="(item, index) in traceability" :key="index" shadow="hover" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header">
            <span>溯源信息 {{ index + 1 }}</span>
          </div>
        </template>
        <div class="info-item">
          <label>商品ID：</label>
          <span>{{ item.productId }}</span>
        </div>
        <div class="info-item">
          <label>播种日期：</label>
          <span>{{ item.seedDate }}</span>
        </div>
        <div class="info-item">
          <label>施肥记录：</label>
          <span>{{ item.fertilizerRecord || '无' }}</span>
        </div>
        <div class="info-item">
          <label>农药使用记录：</label>
          <span>{{ item.pesticideRecord || '无' }}</span>
        </div>
        <div class="info-item">
          <label>收获日期：</label>
          <span>{{ item.harvestDate }}</span>
        </div>
        <div class="info-item">
          <label>检测报告：</label>
          <span>{{ item.inspectionReport || '无' }}</span>
        </div>
        <div class="info-item">
          <label>图片：</label>
          <div class="images-container" v-if="getImagesArray(item.images).length > 0">
            <el-image
              v-for="(img, imgIndex) in getImagesArray(item.images)"
              :key="imgIndex"
              :src="img"
              :preview-src-list="getImagesArray(item.images)"
              style="width: 100px; height: 100px; margin-right: 10px"
              fit="cover"
              :preview-teleported="true"
            />
          </div>
          <span v-else>无</span>
        </div>
      </el-card>
    </div>
    
    <div v-else-if="searched" class="no-data">
      <el-empty description="未找到溯源信息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const route = useRoute()
const productId = ref('')
const traceability = ref(null)
const searched = ref(false)

// 处理图片数组，确保返回一个正确的数组
const getImagesArray = (images) => {
  if (!images) return []
  if (Array.isArray(images)) {
    // 过滤掉null值和空字符串
    return images.filter(img => img != null && typeof img === 'string' && img.trim() !== '')
  }
  if (typeof images === 'string') {
    try {
      // 尝试解析JSON字符串
      const parsed = JSON.parse(images)
      if (Array.isArray(parsed)) {
        // 过滤掉null值和空字符串
        return parsed.filter(img => img != null && typeof img === 'string' && img.trim() !== '')
      }
    } catch (e) {
      console.error('解析图片数组失败:', e)
    }
  }
  return []
}

const searchTraceability = async () => {
  if (!productId.value) {
    ElMessage.warning('请输入商品ID')
    return
  }
  
  try {
    const response = await axios.get(`/customer/traceability/${productId.value}`)
    traceability.value = response.data.data
    searched.value = true
  } catch (error) {
    console.error('查询溯源信息失败:', error)
    traceability.value = null
    searched.value = true
    ElMessage.error('查询失败，请检查商品ID是否正确')
  }
}

onMounted(() => {
  // 从路由参数中获取商品ID
  const paramProductId = route.params.productId
  if (paramProductId) {
    productId.value = paramProductId
    searchTraceability()
  }
})
</script>

<style scoped>
.traceability {
  padding: 20px;
}

.search-section {
  margin-bottom: 20px;
}

.traceability-info {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  margin-bottom: 15px;
  display: flex;
  align-items: flex-start;
}

.info-item label {
  width: 120px;
  font-weight: bold;
  color: #606266;
}

.info-item span {
  flex: 1;
  color: #303133;
}

.no-data {
  margin-top: 40px;
  text-align: center;
}
</style>