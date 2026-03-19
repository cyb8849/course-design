<template>
  <div class="products-container">
    <div class="products-header">
      <h2>商品列表</h2>
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索商品"
          prefix-icon="el-icon-search"
          @keyup.enter="searchProducts"
        >
          <template #append>
            <el-button @click="searchProducts">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <div class="filter-section">
      <div class="category-filter">
        <span class="filter-label">分类：</span>
        <el-radio-group v-model="selectedCategory" @change="filterProducts">
          <el-radio-button label="0">全部</el-radio-button>
          <el-radio-button v-for="category in categories" :key="category.id" :label="category.id">
            {{ category.categoryName }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="sort-filter">
        <span class="filter-label">排序：</span>
        <el-select v-model="sortBy" @change="filterProducts" style="width: 120px">
          <el-option label="默认" value="default"></el-option>
          <el-option label="价格从低到高" value="price_asc"></el-option>
          <el-option label="价格从高到低" value="price_desc"></el-option>
          <el-option label="销量从高到低" value="sales_desc"></el-option>
        </el-select>
      </div>
    </div>

    <div class="products-grid" v-loading="loading">
      <div v-for="product in products" :key="product.id" class="product-item" @click="toProductDetail(product.id)">
        <el-card :body-style="{ padding: '0px' }">
          <img :src="product.imageUrl || product.image" :alt="product.productName || product.name" class="product-image" />
          <div class="product-info">
            <h3 class="product-name">{{ product.productName || product.name }}</h3>
            <p class="product-description">{{ product.description }}</p>
            <div class="product-price">
              <span class="price">¥{{ product.price }}</span>
              <span class="unit">/斤</span>
            </div>
            <div class="product-bottom">
              <span class="sales">销量: {{ product.sales }}</span>
              <el-button type="primary" size="small" @click.stop="addToCart(product.id)">加入购物车</el-button>
            </div>
          </div>
        </el-card>
      </div>
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

    <div v-if="products.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无商品" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useProductStore, useCartStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const route = useRoute()
const productStore = useProductStore()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const selectedCategory = ref(0)
const sortBy = ref('default')
const searchKeyword = ref('')

onMounted(async () => {
  // 从路由参数中获取分类ID
  const categoryId = route.query.categoryId
  if (categoryId) {
    selectedCategory.value = parseInt(categoryId)
  }
  
  await loadCategories()
  await loadProducts()
})

// 监听路由变化
watch(
  () => route.query.categoryId,
  (newCategoryId) => {
    if (newCategoryId) {
      selectedCategory.value = parseInt(newCategoryId)
    } else {
      selectedCategory.value = 0
    }
    loadProducts()
  }
)

const loadCategories = async () => {
  try {
    await productStore.getCategories()
    categories.value = productStore.categories
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    const categoryId = selectedCategory.value === 0 ? null : selectedCategory.value
    console.log('加载商品，分类ID:', categoryId, '排序:', sortBy.value)
    // 直接调用axios测试
    const response = await axios.get('/customer/products', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        categoryId: categoryId,
        keyword: searchKeyword.value,
        sortBy: sortBy.value
      }
    })
    console.log('直接API返回结果:', response.data)
    products.value = response.data?.data?.records || []
    total.value = response.data?.data?.total || 0
    console.log('商品列表:', products.value)
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const searchProducts = () => {
  currentPage.value = 1
  loadProducts()
}

const filterProducts = () => {
  currentPage.value = 1
  loadProducts()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadProducts()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadProducts()
}

const toProductDetail = (productId) => {
  router.push(`/product/${productId}`)
}

const addToCart = async (productId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    await cartStore.addToCart(productId, 1, userStore.user.id)
    ElMessage.success('添加成功')
  } catch (error) {
    ElMessage.error('添加失败：' + (error.response?.data?.message || error.message))
  }
}
</script>

<style scoped>
.products-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.products-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.products-header h2 {
  color: #333;
}

.search-box {
  width: 300px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.filter-label {
  font-weight: bold;
  margin-right: 10px;
  color: #333;
}

.category-filter {
  display: flex;
  align-items: center;
}

.sort-filter {
  display: flex;
  align-items: center;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.product-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.product-item:hover {
  transform: translateY(-5px);
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
}

.product-info {
  padding: 15px;
}

.product-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  color: #333;
}

.product-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 15px;
  height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.product-price {
  margin-bottom: 10px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.unit {
  font-size: 14px;
  color: #999;
  margin-left: 5px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.sales {
  font-size: 14px;
  color: #999;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}
</style>