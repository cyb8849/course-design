<template>
  <div class="products">
    <el-table :data="products" style="width: 100%">
      <el-table-column prop="productName" label="商品名称" width="200" />
      <el-table-column prop="farmerId" label="农户ID" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">
          ¥{{ scope.row.price }}
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="100" />
      <el-table-column prop="origin" label="产地" width="150" />
      <el-table-column prop="harvestTime" label="采摘时间" width="150" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="viewProduct(scope.row)">查看</el-button>
          <el-button v-if="scope.row.status === 'PENDING'" type="success" size="small" @click="approveProduct(scope.row)">通过</el-button>
          <el-button v-if="scope.row.status === 'PENDING'" type="danger" size="small" @click="rejectProduct(scope.row)">拒绝</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 30, 40]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const products = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

onMounted(async () => {
  await loadProducts()
})

const loadProducts = async () => {
  try {
    const response = await axios.get('/admin/products/pending', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    })
    products.value = response.data.data?.records || []
    total.value = response.data.data?.total || 0
  } catch (error) {
    console.error('获取商品失败:', error)
  }
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  await loadProducts()
}

const handleCurrentChange = async (current) => {
  currentPage.value = current
  await loadProducts()
}

const getStatusType = (status) => {
  switch (status) {
    case 'PENDING': return 'warning'
    case 'APPROVED': return 'success'
    case 'REJECTED': return 'danger'
    case 'ON_SALE': return 'info'
    case 'OFF_SALE': return 'info'
    default: return ''
  }
}

const viewProduct = (product) => {
  // 查看商品详情
  console.log('查看商品:', product)
}

const approveProduct = async (product) => {
  try {
    await axios.put(`/admin/products/${product.id}/status`, {}, {
      params: {
        status: 'APPROVED'
      }
    })
    ElMessage.success('审核通过')
    await loadProducts()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}

const rejectProduct = async (product) => {
  try {
    await axios.put(`/admin/products/${product.id}/status`, {}, {
      params: {
        status: 'REJECTED'
      }
    })
    ElMessage.success('审核拒绝')
    await loadProducts()
  } catch (error) {
    console.error('审核失败:', error)
    ElMessage.error('审核失败')
  }
}
</script>

<style scoped>
.products {
  padding: 20px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>