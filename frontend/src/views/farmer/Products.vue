<template>
  <div class="farmer-products">
    <h2>商品管理</h2>
    
    <div class="product-actions">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><i-ep-plus /></el-icon> 新增商品
      </el-button>
    </div>
    
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="products.length === 0" class="empty-state">
      <el-empty description="暂无商品" />
      <el-button type="primary" @click="showAddDialog = true">添加商品</el-button>
    </div>
    
    <div v-else class="product-list">
      <el-table :data="products" style="width: 100%" border>
        <el-table-column prop="id" label="商品ID" width="100" />
        <el-table-column prop="name" label="商品名称" width="200" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="sales" label="销量" width="100" />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ scope.row.status === 'ACTIVE' ? '在售' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="editProduct(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteProduct(scope.row.id)">删除</el-button>
            <el-button size="small" :type="scope.row.status === 'ACTIVE' ? 'warning' : 'success'" @click="toggleStatus(scope.row)">
              {{ scope.row.status === 'ACTIVE' ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
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

    <!-- 新增/编辑商品弹窗 -->
    <el-dialog v-model="showAddDialog" :title="isEditing ? '编辑商品' : '新增商品'" width="800px">
      <el-form :model="productForm" :rules="rules" ref="productFormRef" label-width="100px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="productForm.name" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="productForm.description" placeholder="请输入商品描述" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="productForm.price" :min="0" :step="0.01" placeholder="请输入价格" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="productForm.stock" :min="0" placeholder="请输入库存" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="productForm.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="productForm.categoryId" placeholder="请选择分类">
            <el-option v-for="category in categories" :key="category.id" :label="category.categoryName" :value="category.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品图片" prop="image">
          <el-upload
            class="avatar-uploader"
            action="/api/upload/image"
            :show-file-list="false"
            :on-success="handleImageUpload"
            :before-upload="beforeImageUpload"
          >
            <img v-if="productForm.image" :src="productForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><i-ep-plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="种植信息">
          <el-input v-model="productForm.plantingInfo" placeholder="请输入种植信息" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="营养成分">
          <el-input v-model="productForm.nutritionInfo" placeholder="请输入营养成分" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="烹饪建议">
          <el-input v-model="productForm.cookingSuggestion" placeholder="请输入烹饪建议" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="productForm.status">
            <el-radio label="ACTIVE">在售</el-radio>
            <el-radio label="INACTIVE">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showAddDialog = false">取消</el-button>
          <el-button type="primary" @click="saveProduct">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '../../store'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const categories = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const showAddDialog = ref(false)
const isEditing = ref(false)
const productFormRef = ref()

const productForm = ref({
  id: 0,
  name: '',
  description: '',
  price: 0,
  stock: 0,
  unit: '',
  categoryId: 0,
  image: '',
  status: 'ACTIVE',
  farmerId: 0,
  plantingInfo: '',
  nutritionInfo: '',
  cookingSuggestion: ''
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品描述', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }],
  unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'blur' }],
  image: [{ required: true, message: '请输入图片URL', trigger: 'blur' }]
}

onMounted(async () => {
  if (userStore.isLoggedIn) {
    productForm.value.farmerId = userStore.user.id
    await loadCategories()
    await loadProducts()
  }
})

const loadCategories = async () => {
  try {
    const response = await axios.get('/customer/categories')
    categories.value = response.data.data || []
    console.log('加载分类成功:', categories.value)
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
    // 加载失败时使用默认分类
    categories.value = [
      { id: 1, name: '粉藕' },
      { id: 2, name: '脆藕' },
      { id: 3, name: '藕带' },
      { id: 4, name: '藕粉' }
    ]
    console.log('使用默认分类:', categories.value)
  }
}

const loadProducts = async () => {
  loading.value = true
  try {
    console.log('加载商品前的分类数据:', categories.value)
    const response = await axios.get('/farmer/products', {
      params: {
        farmerId: userStore.user.id,
        page: currentPage.value,
        size: pageSize.value
      }
    })
    
    // 转换后端数据格式为前端期望的格式
    console.log('后端返回的商品数据:', response.data.data?.records || [])
    products.value = response.data.data?.records?.map(product => {
      console.log('商品数据:', product)
      console.log('商品categoryId:', product.categoryId, '类型:', typeof product.categoryId)
      const category = categories.value.find(c => c.id == product.categoryId)
      console.log('匹配的分类:', category)
      return {
        id: product.id,
        name: product.productName,
        description: product.description,
        price: product.price,
        stock: product.stock,
        sales: product.sales || 0,
        unit: product.unit || '斤',
        categoryId: product.categoryId,
        categoryName: category?.categoryName || '未分类',
        image: product.imageUrl || product.image,
        status: product.status === 'ON_SALE' ? 'ACTIVE' : 'INACTIVE',
        createdAt: product.createTime,
        plantingInfo: product.plantingInfo || '',
        nutritionInfo: product.nutritionInfo || '',
        cookingSuggestion: product.cookingSuggestion || ''
      }
    }) || []
    
    total.value = response.data.data?.total || 0
    console.log('转换后的商品数据:', products.value)
  } catch (error) {
    console.error('加载商品失败:', error)
    ElMessage.error('加载商品失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleImageUpload = (response) => {
  if (response.code === 200) {
    productForm.value.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    ElMessage.error('图片上传失败: ' + response.message)
  }
}

const beforeImageUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG、PNG 或 WebP 格式的图片!')
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
  }
  return isJpgOrPng && isLt5M
}

const saveProduct = async () => {
  try {
    await productFormRef.value.validate()
    
    // 转换前端数据格式为后端期望的格式
    const productData = {
      productName: productForm.value.name,
      description: productForm.value.description,
      price: productForm.value.price,
      stock: productForm.value.stock,
      unit: productForm.value.unit,
      categoryId: productForm.value.categoryId,
      imageUrl: productForm.value.image,
      farmerId: userStore.user.id,
      status: productForm.value.status === 'ACTIVE' ? 'ON_SALE' : 'OFF_SALE',
      origin: '汕头市潮阳区西胪镇',
      harvestTime: new Date().toISOString().split('T')[0],
      plantingInfo: productForm.value.plantingInfo,
      nutritionInfo: productForm.value.nutritionInfo,
      cookingSuggestion: productForm.value.cookingSuggestion
    }
    
    if (isEditing.value) {
      // 编辑商品
      await axios.put(`/farmer/products/${productForm.value.id}`, productData)
      ElMessage.success('更新成功，等待审核')
    } else {
      // 新增商品
      await axios.post('/farmer/products', productData)
      ElMessage.success('添加成功，等待审核')
    }
    
    showAddDialog.value = false
    await loadProducts()
  } catch (error) {
    console.error('保存商品失败:', error)
    ElMessage.error('保存商品失败: ' + (error.response?.data?.message || error.message))
  }
}

const editProduct = (product) => {
  productForm.value = { ...product }
  isEditing.value = true
  showAddDialog.value = true
}

const deleteProduct = async (productId) => {
  try {
    await axios.delete(`/farmer/products/${productId}`)
    ElMessage.success('删除成功')
    await loadProducts()
  } catch (error) {
    console.error('删除商品失败:', error)
    ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
  }
}

const toggleStatus = async (product) => {
  try {
    const newStatus = product.status === 'ACTIVE' ? 'OFF_SALE' : 'ON_SALE'
    await axios.put(`/farmer/products/${product.id}/status`, {}, {
      params: {
        status: newStatus
      }
    })
    product.status = product.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    ElMessage.success('操作成功')
  } catch (error) {
    console.error('更新商品状态失败:', error)
    ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
  }
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
</script>

<style scoped>
.farmer-products {
  padding: 20px;
}

.farmer-products h2 {
  margin-bottom: 30px;
  color: #333;
}

.product-actions {
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

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.avatar-uploader {
  width: 150px;
  height: 150px;
}

.avatar {
  width: 150px;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #999;
  width: 150px;
  height: 150px;
  line-height: 150px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 4px;
  background-color: #fafafa;
}
</style>