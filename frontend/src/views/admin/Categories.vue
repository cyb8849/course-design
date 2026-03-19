<template>
  <div class="admin-categories">
    <h2>分类管理</h2>
    
    <div class="category-actions">
      <el-button type="primary" @click="openAddDialog">添加分类</el-button>
    </div>
    
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="categories.length === 0" class="empty-state">
      <el-empty description="暂无分类" />
    </div>
    
    <div v-else class="category-list">
      <el-table :data="categories" style="width: 100%" border>
        <el-table-column prop="id" label="分类ID" width="80" />
        <el-table-column label="分类图片" width="120">
          <template #default="scope">
            <el-image
              :src="scope.row.image"
              fit="cover"
              style="width: 80px; height: 80px"
              v-if="scope.row.image"
            />
            <div v-else class="no-image">无图片</div>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类名称" width="180" />
        <el-table-column prop="parentId" label="父分类ID" width="100" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteCategory(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 添加分类对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加分类"
      width="500px"
    >
      <el-form :model="addForm" :rules="rules" ref="addFormRef">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="addForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类ID" prop="parentId">
          <el-input v-model.number="addForm.parentId" placeholder="请输入父分类ID" type="number" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input v-model.number="addForm.sortOrder" placeholder="请输入排序" type="number" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="addForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类图片" prop="image">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleImageUpload"
            :before-upload="beforeUpload"
          >
            <img v-if="addForm.image" :src="addForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 编辑分类对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑分类"
      width="500px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef">
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="editForm.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父分类ID" prop="parentId">
          <el-input v-model.number="editForm.parentId" placeholder="请输入父分类ID" type="number" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input v-model.number="editForm.sortOrder" placeholder="请输入排序" type="number" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio label="1">启用</el-radio>
            <el-radio label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类图片" prop="image">
          <el-upload
            class="avatar-uploader"
            :action="uploadUrl"
            :show-file-list="false"
            :on-success="handleEditImageUpload"
            :before-upload="beforeUpload"
          >
            <img v-if="editForm.image" :src="editForm.image" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateCategory">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'

const loading = ref(false)
const categories = ref([])
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)
const addFormRef = ref(null)
const editFormRef = ref(null)
const uploadUrl = '/api/upload/image' // 上传接口地址

const addForm = ref({
  categoryName: '',
  parentId: 0,
  sortOrder: 0,
  status: 1,
  image: ''
})

const editForm = ref({
  id: '',
  categoryName: '',
  parentId: 0,
  sortOrder: 0,
  status: 1,
  image: ''
})

const rules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'blur' }]
}

// 加载分类列表
const loadCategories = async () => {
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    console.log('当前token:', token)
    
    const response = await axios.get('/admin/categories')
    console.log('分类API响应:', response.data)
    
    if (response.data.code === 200) {
      categories.value = response.data.data || []
    } else {
      console.error('API返回错误:', response.data)
      ElMessage.error(response.data.message || '加载分类失败')
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    console.error('错误详情:', error.response?.data)
    ElMessage.error('加载分类失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

// 打开添加对话框
const openAddDialog = () => {
  addForm.value = {
    categoryName: '',
    parentId: 0,
    sortOrder: 0,
    status: 1,
    image: ''
  }
  addDialogVisible.value = true
}

// 打开编辑对话框
const openEditDialog = (category) => {
  editForm.value = {
    id: category.id,
    categoryName: category.categoryName,
    parentId: category.parentId || 0,
    sortOrder: category.sortOrder || 0,
    status: category.status || 1,
    image: category.image || ''
  }
  editDialogVisible.value = true
}

// 添加分类
const addCategory = async () => {
  try {
    await addFormRef.value.validate()
    const response = await axios.post('/admin/categories', addForm.value)
    if (response.data.code === 200) {
      ElMessage.success('添加成功')
      addDialogVisible.value = false
      await loadCategories()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('添加分类失败:', error)
    ElMessage.error('添加分类失败')
  }
}

// 更新分类
const updateCategory = async () => {
  try {
    await editFormRef.value.validate()
    const response = await axios.put(`/admin/categories/${editForm.value.id}`, editForm.value)
    if (response.data.code === 200) {
      ElMessage.success('更新成功')
      editDialogVisible.value = false
      await loadCategories()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    console.error('更新分类失败:', error)
    ElMessage.error('更新分类失败')
  }
}

// 删除分类
const deleteCategory = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个分类吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await axios.delete(`/admin/categories/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('删除成功')
      await loadCategories()
    } else {
      ElMessage.error(response.data.message)
    }
  } catch (error) {
    if (error.message !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error('删除分类失败')
    }
  }
}

// 图片上传前验证
const beforeUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpgOrPng) {
    ElMessage.error('只能上传JPG或PNG图片!')
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB!')
  }
  return isJpgOrPng && isLt2M
}

// 图片上传成功处理
const handleImageUpload = (response) => {
  console.log('图片上传响应:', response)
  if (response.code === 200) {
    addForm.value.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    console.error('图片上传失败:', response)
    ElMessage.error('图片上传失败')
  }
}

// 编辑时图片上传成功处理
const handleEditImageUpload = (response) => {
  console.log('编辑图片上传响应:', response)
  if (response.code === 200) {
    editForm.value.image = response.data
    ElMessage.success('图片上传成功')
  } else {
    console.error('图片上传失败:', response)
    ElMessage.error('图片上传失败')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.admin-categories {
  padding: 20px;
}

.admin-categories h2 {
  margin-bottom: 30px;
  color: #333;
}

.category-actions {
  margin-bottom: 20px;
}

.loading-state {
  margin: 20px 0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.no-image {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border: 1px dashed #d9d9d9;
  color: #999;
  font-size: 12px;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.dialog-footer {
  text-align: right;
}
</style>