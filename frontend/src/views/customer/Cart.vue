<template>
  <div class="cart-container">
    <h2>购物车</h2>
    
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="!cartItems || cartItems.length === 0" class="empty-state">
      <el-empty description="购物车为空" />
      <el-button type="primary" @click="toProducts">去购物</el-button>
    </div>
    
    <div v-else class="cart-content">
      <el-table :data="cartItems" style="width: 100%" border>
        <el-table-column prop="productName" label="商品名称" width="200">
          <template #default="scope">
            <div class="product-info">
              <img :src="scope.row.productImage" :alt="scope.row.productName" class="product-image" />
              <span>{{ scope.row.productName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="180">
          <template #default="scope">
            <el-input-number 
              v-model="scope.row.quantity" 
              :min="1" 
              :max="Math.max(1, scope.row.stock || 999)"
              @change="updateQuantity(scope.row.id, scope.row.quantity)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120">
          <template #default="scope">
            ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="scope">
            <el-button type="danger" size="small" @click="removeItem(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="cart-footer">
        <div class="total-info">
          <span class="total-label">合计：</span>
          <span class="total-amount">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="cart-actions">
          <el-button @click="clearCart">清空购物车</el-button>
          <el-button type="primary" size="large" @click="goToCheckout" :disabled="!cartItems || cartItems.length === 0">
            去结算
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore, useUserStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(false)

const cartItems = computed(() => {
  return cartStore.items
})

const totalAmount = computed(() => {
  return cartItems.value.reduce((total, item) => total + item.price * item.quantity, 0)
})

onMounted(async () => {
  if (userStore.isLoggedIn) {
    await loadCartItems()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

const loadCartItems = async () => {
  loading.value = true
  try {
    if (!userStore.user) {
      ElMessage.warning('用户信息不存在')
      router.push('/login')
      return
    }
    await cartStore.getCartItems(userStore.user.id)
  } catch (error) {
    console.error('加载购物车失败:', error)
    ElMessage.error('加载购物车失败')
  } finally {
    loading.value = false
  }
}

const updateQuantity = async (itemId, quantity) => {
  try {
    await cartStore.updateCartItem(itemId, quantity)
  } catch (error) {
    console.error('更新数量失败:', error)
    ElMessage.error('更新数量失败')
    // 重新加载购物车
    await loadCartItems()
  }
}

const removeItem = async (itemId) => {
  try {
    await cartStore.removeFromCart(itemId)
    ElMessage.success('删除成功')
    await loadCartItems()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}

const clearCart = async () => {
  try {
    if (!userStore.user) {
      ElMessage.warning('用户信息不存在')
      router.push('/login')
      return
    }
    await cartStore.clearCart(userStore.user.id)
    ElMessage.success('清空成功')
    cartItems.value = []
  } catch (error) {
    console.error('清空失败:', error)
    ElMessage.error('清空失败')
  }
}

const goToCheckout = () => {
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  // 跳转到地址选择页面
  router.push('/addresses')
}

const toProducts = () => {
  router.push('/products')
}
</script>

<style scoped>
.cart-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-container h2 {
  margin-bottom: 30px;
  color: #333;
}

.loading-state {
  margin: 20px 0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.empty-state .el-button {
  margin-top: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-image {
  width: 50px;
  height: 50px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 30px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 8px;
}

.total-info {
  font-size: 18px;
  font-weight: bold;
}

.total-label {
  color: #333;
  margin-right: 10px;
}

.total-amount {
  color: #ff4d4f;
  font-size: 24px;
}

.cart-actions {
  display: flex;
  gap: 10px;
}
</style>