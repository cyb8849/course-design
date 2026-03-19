<template>
  <div class="traceability-container">
    <div class="header">
      <h2>溯源信息管理</h2>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon> 添加溯源信息</el-button>
    </div>

    <el-card class="traceability-card">
      <div class="search-section">
        <el-select v-model="searchProductId" placeholder="选择商品" style="width: 200px; margin-right: 10px;">
          <el-option
            v-for="product in products"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
        <el-button type="primary" @click="loadTraceabilityList"><el-icon><Search /></el-icon> 查询</el-button>
      </div>

      <el-table :data="traceabilityList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="productId" label="商品ID" width="100" />
        <el-table-column label="商品名称" width="150">
          <template #default="scope">
            {{ getProductName(scope.row.productId) }}
          </template>
        </el-table-column>
        <el-table-column prop="seedDate" label="播种日期" width="120" />
        <el-table-column prop="harvestDate" label="收获日期" width="120" />
        <el-table-column prop="images" label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.images && scope.row.images.length > 0"
              :src="(scope.row.images.filter(img => img != null && typeof img === 'string' && img.trim() !== ''))[0]"
              :preview-src-list="scope.row.images.filter(img => img != null && typeof img === 'string' && img.trim() !== '')"
              style="width: 50px; height: 50px"
              fit="cover"
              :preview-teleported="true"
            />
            <span v-else>无</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)"><el-icon><Edit /></el-icon> 编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)"><el-icon><Delete /></el-icon> 删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" label-width="120px">
        <el-form-item label="商品">
          <el-select v-model="form.productId" placeholder="请选择商品" style="width: 100%">
            <el-option
              v-for="product in products"
              :key="product.id"
              :label="product.name"
              :value="product.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="播种日期">
          <el-date-picker
            v-model="form.seedDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="施肥记录">
          <el-input
            v-model="form.fertilizerRecord"
            type="textarea"
            placeholder="请输入施肥记录"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="农药使用记录">
          <el-input
            v-model="form.pesticideRecord"
            type="textarea"
            placeholder="请输入农药使用记录"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="收获日期">
          <el-date-picker
            v-model="form.harvestDate"
            type="date"
            placeholder="选择日期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="检测报告">
          <el-input
            v-model="form.inspectionReport"
            type="textarea"
            placeholder="请输入检测报告"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="图片上传">
          <el-upload
            class="upload-demo"
            :action="uploadUrl"
            :headers="{ Authorization: `Bearer ${token}` }"
            :on-success="handleImageUploadSuccess"
            :on-error="handleImageUploadError"
            :multiple="true"
            :limit="5"
            :file-list="fileList"
            list-type="picture"
          >
            <el-button type="primary"><el-icon><Upload /></el-icon> 上传图片</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传播种、施肥、收获等环节的照片，最多上传5张
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore, useProductStore } from '../../store'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'
import { Edit, Delete, Upload, Plus, Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const productStore = useProductStore()

const loading = ref(false)
const traceabilityList = ref([])
const products = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchProductId = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('添加溯源信息')
const form = ref({
  id: null,
  productId: null,
  seedDate: null,
  fertilizerRecord: '',
  pesticideRecord: '',
  harvestDate: null,
  inspectionReport: '',
  images: []
})

console.log('初始form.images:', form.value.images)

const fileList = ref([])
const uploadUrl = computed(() => {
  // 获取当前域名和端口，构建完整的上传URL
  const baseUrl = window.location.origin
  return `${baseUrl}/api/farmer/upload`
})
const token = computed(() => userStore.token)

const loadProducts = async () => {
  try {
    // 获取农户ID
    const farmerId = userStore.user?.id
    if (!farmerId) {
      ElMessage.error('请先登录')
      return
    }
    
    // 调用农户商品列表接口
    const response = await axios.get('/farmer/products', {
      params: {
        page: 1,
        size: 100,
        farmerId: farmerId
      },
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })
    
    // 转换商品数据格式
    console.log('后端返回的商品数据:', response.data.data.records || [])
    products.value = (response.data.data.records || []).map(product => ({
      id: product.id,
      name: product.productName || product.name,
      productName: product.productName,
      categoryId: product.categoryId
    }))
    console.log('转换后的商品数据:', products.value)
  } catch (error) {
    console.error('加载商品列表失败:', error)
    ElMessage.error('加载商品列表失败')
  }
}

const loadTraceabilityList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/farmer/traceability', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        productId: searchProductId.value
      },
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })
    
    // 检查响应数据结构
    if (response.data && response.data.code === 200 && response.data.data) {
      traceabilityList.value = response.data.data.records || []
      total.value = response.data.data.total || 0
    } else {
      traceabilityList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载溯源信息列表失败:', error)
    traceabilityList.value = []
    total.value = 0
    ElMessage.error('加载溯源信息列表失败')
  } finally {
    loading.value = false
  }
}

const getProductName = (productId) => {
  console.log('查找商品名称，productId:', productId, '类型:', typeof productId)
  const product = products.value.find(p => p.id == productId)
  console.log('找到的商品:', product)
  return product ? product.name : '未知商品'
}

const handleAdd = () => {
  dialogTitle.value = '添加溯源信息'
  form.value = {
    id: null,
    productId: null,
    seedDate: null,
    fertilizerRecord: '',
    pesticideRecord: '',
    harvestDate: null,
    inspectionReport: '',
    images: []
  }
  fileList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑溯源信息'
  // 处理日期类型，确保seedDate和harvestDate是字符串
  // 过滤图片数组中的null值和空字符串
  const filteredImages = (row.images || []).filter(img => img != null && typeof img === 'string' && img.trim() !== '')
  form.value = {
    ...row,
    seedDate: row.seedDate,
    harvestDate: row.harvestDate,
    images: filteredImages
  }
  // 处理图片列表
  fileList.value = filteredImages.map((url, index) => ({
    name: `image${index + 1}`,
    url: url
  }))
  console.log('编辑时的form.value:', form.value)
  console.log('编辑时seedDate类型:', typeof form.value.seedDate)
  console.log('编辑时harvestDate类型:', typeof form.value.harvestDate)
  console.log('编辑时过滤后的images:', filteredImages)
  dialogVisible.value = true
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条溯源信息吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await axios.delete(`/farmer/traceability/${id}`, {
      headers: {
        Authorization: `Bearer ${token.value}`
      }
    })
    
    ElMessage.success('删除成功')
    loadTraceabilityList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleImageUploadSuccess = (response, uploadFile) => {
  console.log('图片上传成功响应:', JSON.stringify(response))
  console.log('响应类型:', typeof response)
  console.log('响应是否有data属性:', 'data' in response)
  console.log('响应是否有message属性:', 'message' in response)
  if (response.message && typeof response.message === 'string') {
    console.log('使用response.message:', response.message)
    // 确保只添加非空的图片URL，并且不是错误信息
    if (response.message.trim() !== '' && !response.message.includes('上传失败') && response.message.startsWith('http')) {
      form.value.images.push(response.message)
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error('图片上传失败：无效的图片URL')
    }
  } else {
    ElMessage.error('图片上传失败')
  }
  console.log('上传后form.images:', form.value.images)
}

const handleImageUploadError = (error) => {
  console.error('图片上传失败:', error)
  ElMessage.error('图片上传失败')
}

const handleSave = async () => {
  try {
    if (!form.value.productId) {
      ElMessage.warning('请选择商品')
      return
    }
    
    if (!form.value.seedDate) {
      ElMessage.warning('请选择播种日期')
      return
    }
    
    if (!form.value.harvestDate) {
      ElMessage.warning('请选择收获日期')
      return
    }
    
    // 转换日期格式
    console.log('保存前的form.images:', form.value.images)
    console.log('seedDate类型:', typeof form.value.seedDate)
    console.log('seedDate值:', form.value.seedDate)
    console.log('harvestDate类型:', typeof form.value.harvestDate)
    console.log('harvestDate值:', form.value.harvestDate)
    // 过滤掉图片数组中的null值和空字符串，并转换为普通数组
    const filteredImages = Array.from(form.value.images).filter(img => {
      console.log('检查图片URL:', img, '类型:', typeof img, '是否为null:', img === null, '是否为空字符串:', img === '')
      return img != null && typeof img === 'string' && img.trim() !== ''
    })
    console.log('过滤后的图片数组:', filteredImages)
    const submitData = {
      ...form.value,
      seedDate: form.value.seedDate ? (typeof form.value.seedDate === 'string' ? form.value.seedDate : (form.value.seedDate instanceof Date ? form.value.seedDate.toISOString().split('T')[0] : form.value.seedDate)) : null,
      harvestDate: form.value.harvestDate ? (typeof form.value.harvestDate === 'string' ? form.value.harvestDate : (form.value.harvestDate instanceof Date ? form.value.harvestDate.toISOString().split('T')[0] : form.value.harvestDate)) : null,
      images: filteredImages
    }
    console.log('提交的数据:', submitData)
    
    let response
    if (form.value.id) {
      // 编辑
      response = await axios.put(`/farmer/traceability/${form.value.id}`, submitData, {
        headers: {
          Authorization: `Bearer ${token.value}`
        }
      })
    } else {
      // 添加
      response = await axios.post('/farmer/traceability', submitData, {
        headers: {
          Authorization: `Bearer ${token.value}`
        }
      })
    }
    
    if (response.data.code === 200) {
      ElMessage.success(form.value.id ? '编辑成功' : '添加成功')
      dialogVisible.value = false
      loadTraceabilityList()
    } else {
      ElMessage.error(form.value.id ? '编辑失败' : '添加失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败')
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadTraceabilityList()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadTraceabilityList()
}

onMounted(async () => {
  await loadProducts()
  await loadTraceabilityList()
})
</script>

<style scoped>
.traceability-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}

.traceability-card {
  margin-bottom: 20px;
}

.search-section {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>