<template>
  <div class="addresses-container">
    <h2>收货地址</h2>
    
    <div class="address-actions">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><i-ep-plus /></el-icon> 新增地址
      </el-button>
    </div>
    
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="addresses.length === 0" class="empty-state">
      <el-empty description="暂无地址" />
      <el-button type="primary" @click="showAddDialog = true">添加地址</el-button>
    </div>
    
    <div v-else class="address-list">
      <div v-for="address in addresses" :key="address.id" class="address-item">
        <div class="address-content">
          <div class="address-header">
            <span class="address-name">{{ address.consignee }}</span>
            <span class="address-phone">{{ address.phone }}</span>
            <el-tag v-if="address.isDefault === 1" size="small" type="success">默认</el-tag>
          </div>
          <div class="address-detail">
            {{ address.province }}{{ address.city }}{{ address.district }}{{ address.detailAddress }}
          </div>
        </div>
        <div class="address-actions">
          <el-button size="small" @click="setDefault(address.id)">设为默认</el-button>
          <el-button size="small" type="primary" @click="editAddress(address)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteAddress(address.id)">删除</el-button>
        </div>
      </div>
    </div>
    
    <!-- 结算按钮 -->
    <div class="checkout-section" v-if="addresses.length > 0">
      <el-button type="primary" size="large" @click="createOrder">
        确认订单
      </el-button>
    </div>

    <!-- 新增/编辑地址弹窗 -->
    <el-dialog v-model="showAddDialog" :title="isEditing ? '编辑地址' : '新增地址'" width="600px">
      <el-form :model="addressForm" :rules="rules" ref="addressFormRef" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="addressForm.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="addressForm.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="addressForm.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="addressForm.detail" placeholder="请输入详细地址" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveAddress">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAddressStore, useUserStore, useCartStore, useOrderStore } from '../../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const addressStore = useAddressStore()
const userStore = useUserStore()
const cartStore = useCartStore()
const orderStore = useOrderStore()

const loading = ref(false)
const addresses = ref([])
const showAddDialog = ref(false)
const isEditing = ref(false)
const addressFormRef = ref()

const addressForm = ref({
  id: 0,
  userId: 0,
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    addressForm.value.userId = userStore.user.id
    await loadAddresses()
  } else {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})

const loadAddresses = async () => {
  loading.value = true
  try {
    console.log('开始加载地址，userId:', userStore.user.id)
    const data = await addressStore.getAddresses(userStore.user.id)
    console.log('获取到的地址数据:', data)
    // 直接使用返回的数据，不进行字段转换
    addresses.value = data || []
    console.log('加载后的地址数据:', addresses.value)
  } catch (error) {
    console.error('加载地址失败:', error)
    ElMessage.error('加载地址失败：' + (error.response?.data?.message || error.message))
    addresses.value = []
  } finally {
    loading.value = false
  }
}

const saveAddress = async () => {
  try {
    await addressFormRef.value.validate()
    // 转换字段名称和类型以匹配后端实体类
    const addressData = {
      id: addressForm.value.id,
      userId: addressForm.value.userId,
      consignee: addressForm.value.name,
      phone: addressForm.value.phone,
      province: addressForm.value.province,
      city: addressForm.value.city,
      district: addressForm.value.district,
      detailAddress: addressForm.value.detail,
      isDefault: addressForm.value.isDefault ? 1 : 0
    }
    console.log('保存地址数据:', addressData)
    if (isEditing.value) {
      await addressStore.updateAddress(addressForm.value.id, addressData)
      ElMessage.success('更新成功')
    } else {
      await addressStore.addAddress(addressData)
      ElMessage.success('添加成功')
    }
    showAddDialog.value = false
    await loadAddresses()
  } catch (error) {
    console.error('保存地址失败:', error)
    ElMessage.error('保存地址失败')
  }
}

const editAddress = (address) => {
  // 转换字段名称以匹配前端表单
  addressForm.value = {
    id: address.id,
    userId: address.userId,
    name: address.consignee,
    phone: address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detailAddress,
    isDefault: address.isDefault === 1
  }
  isEditing.value = true
  showAddDialog.value = true
}

const deleteAddress = async (addressId) => {
  try {
    await addressStore.deleteAddress(addressId)
    ElMessage.success('删除成功')
    await loadAddresses()
  } catch (error) {
    console.error('删除地址失败:', error)
    ElMessage.error('删除地址失败')
  }
}

const setDefault = async (addressId) => {
  try {
    // 先将所有地址设为非默认
    for (const address of addresses.value) {
      if (address.isDefault === 1) {
        const addressData = {
          id: address.id,
          userId: address.userId,
          consignee: address.consignee,
          phone: address.phone,
          province: address.province,
          city: address.city,
          district: address.district,
          detailAddress: address.detailAddress,
          isDefault: 0
        }
        await addressStore.updateAddress(address.id, addressData)
      }
    }
    // 再将选中地址设为默认
    const address = addresses.value.find(a => a.id === addressId)
    const addressData = {
      id: address.id,
      userId: address.userId,
      consignee: address.consignee,
      phone: address.phone,
      province: address.province,
      city: address.city,
      district: address.district,
      detailAddress: address.detailAddress,
      isDefault: 1
    }
    await addressStore.updateAddress(addressId, addressData)
    ElMessage.success('设置成功')
    await loadAddresses()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置默认地址失败')
  }
}

const createOrder = async () => {
  // 检查购物车是否为空
  const cartItems = await cartStore.getCartItems(userStore.user.id)
  if (cartItems.length === 0) {
    ElMessage.warning('购物车为空')
    router.push('/cart')
    return
  }

  // 选择默认地址
  const defaultAddress = addresses.value.find(a => a.isDefault)
  if (!defaultAddress) {
    ElMessage.warning('请设置默认地址')
    return
  }

  // 创建订单数据
  const orderData = {
    userId: userStore.user.id,
    addressId: defaultAddress.id
  }

  try {
    const result = await orderStore.createOrder(orderData)
    ElMessage.success('订单创建成功')
    // 清空购物车
    await cartStore.clearCart(userStore.user.id)
    // 跳转到支付页面
    router.push(`/payment?orderId=${result.data.id}`)
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error('创建订单失败')
  }
}
</script>

<style scoped>
.addresses-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.addresses-container h2 {
  margin-bottom: 30px;
  color: #333;
}

.address-actions {
  margin-bottom: 30px;
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

.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.address-item {
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.address-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.address-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  gap: 15px;
}

.address-name {
  font-weight: bold;
  color: #333;
}

.address-phone {
  color: #666;
}

.address-detail {
  color: #333;
  line-height: 1.5;
  margin-bottom: 15px;
}

.address-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.checkout-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}
</style>