<template>
  <div class="product-detail-container" v-loading="loading">
    <div class="product-basic-info">
      <div class="product-images">
        <el-carousel :interval="5000" type="card" height="400px">
          <el-carousel-item>
            <img :src="product.imageUrl || product.image" :alt="product.productName || product.name" class="main-image" />
          </el-carousel-item>
          <!-- 可以添加更多图片 -->
        </el-carousel>
      </div>
      <div class="product-info">
        <h1 class="product-name">{{ product.productName || product.name }}</h1>
        <div class="product-price">
          <span class="price">¥{{ product.price }}</span>
          <span class="unit">/{{ product.unit }}</span>
        </div>
        <div class="product-stock">
          <span class="label">库存：</span>
          <span class="value">{{ product.stock }} {{ product.unit }}</span>
        </div>
        <div class="product-sales">
          <span class="label">销量：</span>
          <span class="value">{{ product.sales || 0 }}</span>
        </div>
        <div class="product-category">
          <span class="label">分类：</span>
          <span class="value">{{ categoryName }}</span>
        </div>
        <div class="product-quantity">
          <span class="label">数量：</span>
          <el-input-number v-model="quantity" :min="1" :max="Math.max(1, product.stock || 999)" @change="handleQuantityChange" />
        </div>
        <div class="product-actions">
          <el-button type="primary" size="large" @click="addToCart" :disabled="product.stock <= 0">
            <el-icon><i-ep-shopping-cart /></el-icon> 加入购物车
          </el-button>
          <el-button type="success" size="large" @click="buyNow" :disabled="product.stock <= 0">
            <el-icon><i-ep-right /></el-icon> 立即购买
          </el-button>
          <el-button type="info" size="large" @click="viewTraceability">
            <el-icon><i-ep-search /></el-icon> 查看溯源信息
          </el-button>
        </div>
      </div>
    </div>

    <div class="product-details">
      <el-tabs type="border-card">
        <el-tab-pane label="商品详情">
          <div class="detail-content">
            <h3>商品描述</h3>
            <p>{{ product.description || '暂无描述' }}</p>
            <h3>产品特点</h3>
            <ul>
              <li>产地：{{ product.origin || '西胪镇' }}</li>
              <li>品质：优质</li>
              <li>新鲜度：当天采摘</li>
              <li>储存方式：冷藏保存</li>
            </ul>
          </div>
        </el-tab-pane>
        <el-tab-pane label="种植信息">
          <div class="detail-content">
            <template v-if="product.plantingInfo && product.plantingInfo.trim()">
              <ul class="info-list">
                <li v-for="(line, index) in formatInfo(product.plantingInfo)" :key="index">{{ line }}</li>
              </ul>
            </template>
            <template v-else>暂无描述</template>
          </div>
        </el-tab-pane>
        <el-tab-pane label="营养成分">
          <div class="detail-content">
            <template v-if="product.nutritionInfo && product.nutritionInfo.trim()">
              <ul class="info-list">
                <li v-for="(line, index) in formatInfo(product.nutritionInfo)" :key="index">{{ line }}</li>
              </ul>
            </template>
            <template v-else>暂无描述</template>
          </div>
        </el-tab-pane>
        <el-tab-pane label="烹饪建议">
          <div class="detail-content">
            <template v-if="product.cookingSuggestion && product.cookingSuggestion.trim()">
              <ul class="info-list">
                <li v-for="(line, index) in formatInfo(product.cookingSuggestion)" :key="index">{{ line }}</li>
              </ul>
            </template>
            <template v-else>暂无描述</template>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div class="related-products">
      <h3>相关推荐</h3>
      <div class="related-grid">
        <div v-for="item in relatedProducts" :key="item.id" class="related-item" @click="toProductDetail(item.id)">
          <img :src="item.imageUrl" :alt="item.productName || item.name" class="related-image" />
          <div class="related-info">
            <h4>{{ item.productName || item.name }}</h4>
            <div class="related-price">¥{{ item.price }}/{{ item.unit }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore, useCartStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const product = ref({
  id: 0,
  name: '',
  productName: '',
  description: '',
  price: 0,
  unit: '',
  stock: 0,
  sales: 0,
  image: '',
  imageUrl: '',
  categoryId: 0,
  origin: '',
  plantingInfo: '',
  nutritionInfo: '',
  cookingSuggestion: ''
})
const quantity = ref(1)
const relatedProducts = ref([])

const categoryName = computed(() => {
  const category = productStore.categories.find(c => c.id === product.value.categoryId)
  return category ? category.name : '未知分类'
})

onMounted(async () => {
  const productId = parseInt(route.params.id)
  if (productId) {
    await loadProductDetail(productId)
    await loadRelatedProducts(productId, product.value.categoryId)
  }
})

const loadProductDetail = async (productId) => {
  loading.value = true
  try {
    const result = await productStore.getProductDetail(productId)
    product.value = result
  } catch (error) {
    console.error('加载商品详情失败:', error)
    ElMessage.error('加载商品详情失败')
  } finally {
    loading.value = false
  }
}

const loadRelatedProducts = async (currentProductId, categoryId) => {
  try {
    const result = await productStore.getProducts(1, 4, categoryId)
    relatedProducts.value = result.data.records.filter(p => p.id !== currentProductId)
  } catch (error) {
    console.error('加载相关商品失败:', error)
  }
}

const handleQuantityChange = (value) => {
  quantity.value = value
}

const addToCart = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (product.value.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }

  try {
    await cartStore.addToCart(product.value.id, quantity.value, userStore.user.id)
    ElMessage.success('添加成功')
  } catch (error) {
    ElMessage.error('添加失败：' + (error.response?.data?.message || error.message))
  }
}

const buyNow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (product.value.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }

  // 先添加到购物车，然后跳转到购物车页面
  try {
    await cartStore.addToCart(product.value.id, quantity.value, userStore.user.id)
    ElMessage.success('已添加到购物车')
    router.push('/cart')
  } catch (error) {
    ElMessage.error('操作失败：' + (error.response?.data?.message || error.message))
  }
}

const toProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const viewTraceability = () => {
  router.push(`/traceability/${product.value.id}`)
}

// 将信息文本按换行符分割成数组
const formatInfo = (text) => {
  if (!text) return []
  return text.split('\n').filter(line => line.trim() !== '')
}
</script>

<style scoped>
.product-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.product-basic-info {
  display: flex;
  gap: 40px;
  margin-bottom: 40px;
}

.product-images {
  flex: 1;
  min-width: 400px;
}

.main-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.product-info {
  flex: 1;
  min-width: 400px;
}

.product-name {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 20px;
  color: #333;
}

.product-price {
  margin-bottom: 20px;
}

.price {
  font-size: 28px;
  font-weight: bold;
  color: #ff4d4f;
}

.unit {
  font-size: 16px;
  color: #999;
  margin-left: 5px;
}

.product-stock,
.product-sales,
.product-category {
  margin-bottom: 15px;
  font-size: 16px;
}

.label {
  color: #666;
  margin-right: 10px;
}

.value {
  color: #333;
  font-weight: 500;
}

.product-quantity {
  margin-bottom: 30px;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.product-quantity .label {
  margin-right: 20px;
}

.product-actions {
  display: flex;
  gap: 20px;
}

.product-actions .el-button {
  flex: 1;
}

.product-details {
  margin-bottom: 40px;
}

.detail-content {
  padding: 20px;
}

.detail-content h3 {
  margin-bottom: 15px;
  color: #333;
}

.detail-content p,
.detail-content ul {
  margin-bottom: 20px;
  color: #666;
  line-height: 1.6;
}

.info-list {
  list-style: disc;
  padding-left: 20px;
  margin: 0;
}

.info-list li {
  margin-bottom: 10px;
  line-height: 1.8;
  color: #333;
}

.info-list li:last-child {
  margin-bottom: 0;
}

.detail-content ul {
  padding-left: 20px;
}

.related-products {
  margin-bottom: 40px;
}

.related-products h3 {
  margin-bottom: 20px;
  color: #333;
}

.related-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.related-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.related-item:hover {
  transform: translateY(-5px);
}

.related-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 10px;
}

.related-info h4 {
  font-size: 14px;
  margin-bottom: 5px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.related-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff4d4f;
}
</style>