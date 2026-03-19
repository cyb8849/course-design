<template>
  <div class="home-container">
    <!-- 轮播图 -->
    <el-carousel :interval="5000" type="card" height="400px" indicator-position="outside">
      <el-carousel-item v-for="(item, index) in carouselItems" :key="index">
        <img :src="item.image" :alt="item.title" class="carousel-image" />
        <div class="carousel-content">
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
          <el-button type="primary" size="large" @click="toProducts">立即购买</el-button>
        </div>
      </el-carousel-item>
    </el-carousel>

    <!-- 分类导航 -->
    <div class="categories-section">
      <h2>莲藕品类</h2>
      <div class="categories-grid">
        <div v-for="category in categories" :key="category.id" class="category-item" @click="toCategory(category.id)">
          <el-card :body-style="{ padding: '0px' }">
            <img :src="category.image" :alt="category.categoryName" class="category-image" />
            <div class="category-name">{{ category.categoryName }}</div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 推荐商品 -->
    <div class="products-section">
      <h2>推荐商品</h2>
      <div class="products-grid">
        <div v-for="product in recommendedProducts" :key="product.id" class="product-item" @click="toProductDetail(product.id)">
          <el-card :body-style="{ padding: '0px' }">
            <img :src="product.imageUrl || product.image" :alt="product.productName || product.name" class="product-image" />
            <div class="product-info">
              <h3 class="product-name">{{ product.productName || product.name }}</h3>
              <p class="product-description">{{ product.description }}</p>
              <div class="product-price">
                <span class="price">¥{{ product.price }}</span>
                <span class="unit">/{{ product.unit || '斤' }}</span>
              </div>
              <el-button type="primary" size="small" @click.stop="addToCart(product.id)">加入购物车</el-button>
            </div>
          </el-card>
        </div>
      </div>
    </div>

    <!-- 特色介绍 -->
    <div class="features-section">
      <h2>西胪莲藕特色</h2>
      <div class="features-grid">
        <div class="feature-item">
          <el-icon size="48"><i-ep-connection /></el-icon>
          <h3>产地直发</h3>
          <p>直接从农户手中采购，保证新鲜</p>
        </div>
        <div class="feature-item">
          <el-icon size="48"><i-ep-document /></el-icon>
          <h3>全程追溯</h3>
          <p>每批莲藕都有完整的种植和加工记录</p>
        </div>
        <div class="feature-item">
          <el-icon size="48"><i-ep-star /></el-icon>
          <h3>品质保证</h3>
          <p>严格的质量检测，确保莲藕品质</p>
        </div>
        <div class="feature-item">
          <el-icon size="48"><i-ep-truck /></el-icon>
          <h3>快速配送</h3>
          <p>当天发货，保证新鲜送达</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProductStore, useCartStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const productStore = useProductStore()
const cartStore = useCartStore()
const userStore = useUserStore()

const carouselItems = ref([
  {
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20roots%20in%20water%20with%20green%20leaves%2C%20high%20quality%20photography%2C%20bright%20colors&image_size=landscape_16_9',
    title: '西胪莲藕，天然美味',
    description: '来自潮汕地区的优质莲藕，口感清脆，营养丰富'
  },
  {
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20farm%20field%2C%20traditional%20agriculture%2C%20sunset%20view&image_size=landscape_16_9',
    title: '传统种植，绿色健康',
    description: '采用传统种植方式，不使用农药，绿色环保'
  },
  {
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20dishes%2C%20delicious%20chinese%20cuisine%2C%20restaurant%20style&image_size=landscape_16_9',
    title: '多种做法，美味无穷',
    description: '可炒、可炖、可汤，多种烹饪方式，满足不同口味'
  }
])

const categories = ref([])

const recommendedProducts = ref([
  {
    id: 1,
    name: '西胪粉藕',
    description: '口感粉糯，适合煲汤',
    price: 12.8,
    unit: '斤',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20with%20mud%2C%20natural%20and%20organic&image_size=square'
  },
  {
    id: 2,
    name: '西胪脆藕',
    description: '口感清脆，适合清炒',
    price: 10.8,
    unit: '斤',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=crispy%20lotus%20root%20slices%2C%20fresh%20and%20juicy&image_size=square'
  },
  {
    id: 3,
    name: '新鲜藕带',
    description: '鲜嫩可口，营养丰富',
    price: 18.8,
    unit: '斤',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20shoots%2C%20green%20and%20tender&image_size=square'
  },
  {
    id: 4,
    name: '纯藕粉',
    description: '无添加，营养健康',
    price: 29.8,
    unit: '袋',
    image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20starch%20in%20package%2C%20white%20and%20pure&image_size=square'
  }
])

onMounted(async () => {
  // 加载分类数据
  try {
    await productStore.getCategories()
    if (productStore.categories.length > 0) {
      categories.value = productStore.categories
    } else {
      // 备用分类数据
      categories.value = [
        { id: 1, categoryName: '莲藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20with%20mud%2C%20natural%20and%20organic&image_size=square' },
        { id: 2, categoryName: '粉藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=starchy%20lotus%20root%2C%20thick%20and%20white%2C%20sliced&image_size=square' },
        { id: 3, categoryName: '新鲜莲藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20slices%2C%20clean%20and%20white&image_size=square' },
        { id: 4, categoryName: '脆藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=crispy%20lotus%20root%2C%20fresh%20and%20juicy%2C%20sliced&image_size=square' },
        { id: 5, categoryName: '莲藕制品', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20products%2C%20various%20processed%20items&image_size=square' },
        { id: 6, categoryName: '藕尖', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20shoots%2C%20fresh%20and%20tender%2C%20green&image_size=square' },
        { id: 7, categoryName: '特色农产品', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=special%20agricultural%20products%2C%20local%20delicacies&image_size=square' }
      ]
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    // 错误时使用备用分类数据
    categories.value = [
      { id: 1, categoryName: '莲藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20with%20mud%2C%20natural%20and%20organic&image_size=square' },
      { id: 2, categoryName: '粉藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=starchy%20lotus%20root%2C%20thick%20and%20white%2C%20sliced&image_size=square' },
      { id: 3, categoryName: '新鲜莲藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=fresh%20lotus%20root%20slices%2C%20clean%20and%20white&image_size=square' },
      { id: 4, categoryName: '脆藕', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=crispy%20lotus%20root%2C%20fresh%20and%20juicy%2C%20sliced&image_size=square' },
      { id: 5, categoryName: '莲藕制品', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20products%2C%20various%20processed%20items&image_size=square' },
      { id: 6, categoryName: '藕尖', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=lotus%20root%20shoots%2C%20fresh%20and%20tender%2C%20green&image_size=square' },
      { id: 7, categoryName: '特色农产品', image: 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=special%20agricultural%20products%2C%20local%20delicacies&image_size=square' }
    ]
  }

  // 加载推荐商品
  try {
    const result = await productStore.getProducts(1, 4)
    if (result.data.records.length > 0) {
      recommendedProducts.value = result.data.records
    }
  } catch (error) {
    console.error('加载商品失败:', error)
  }
})

const toProducts = () => {
  router.push('/products')
}

const toCategory = (categoryId) => {
  router.push(`/products?categoryId=${categoryId}`)
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
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 轮播图样式 */
.el-carousel {
  margin-bottom: 40px;
}

.carousel-image {
  width: 100%;
  height: 400px;
  object-fit: cover;
  border-radius: 8px;
}

.carousel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: white;
  padding: 30px;
  border-radius: 0 0 8px 8px;
}

.carousel-content h3 {
  font-size: 24px;
  margin-bottom: 10px;
}

.carousel-content p {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 分类样式 */
.categories-section {
  margin-bottom: 40px;
}

.categories-section h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.category-item {
  cursor: pointer;
  transition: transform 0.3s ease;
}

.category-item:hover {
  transform: translateY(-5px);
}

.category-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
}

.category-name {
  text-align: center;
  padding: 15px;
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

/* 商品样式 */
.products-section {
  margin-bottom: 40px;
}

.products-section h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
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
  margin-bottom: 15px;
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

/* 特色介绍样式 */
.features-section {
  margin-bottom: 40px;
}

.features-section h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  text-align: center;
}

.feature-item {
  padding: 30px;
  background: #f5f5f5;
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-5px);
  background: #e6f7ff;
}

.feature-item h3 {
  margin-top: 15px;
  margin-bottom: 10px;
  color: #333;
}

.feature-item p {
  color: #666;
  font-size: 14px;
}
</style>