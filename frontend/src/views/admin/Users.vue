<template>
  <div class="users">
    <el-button type="primary" @click="addUser">添加用户</el-button>
    
    <div class="filter-section">
      <el-select v-model="userRole" placeholder="请选择角色" @change="loadUsers">
        <el-option label="全部" value=""></el-option>
        <el-option label="农户" value="FARMER"></el-option>
        <el-option label="客户" value="CUSTOMER"></el-option>
        <el-option label="管理员" value="ADMIN"></el-option>
      </el-select>
    </div>
    
    <el-table :data="users" style="width: 100%">
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机号" width="150" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="scope">
          <el-tag :type="getRoleType(scope.row.role)">{{ getRoleText(scope.row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-switch v-model="scope.row.status" @change="updateStatus(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" @click="editUser(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="deleteUser(scope.row)">删除</el-button>
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
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="农户" value="FARMER"></el-option>
            <el-option label="客户" value="CUSTOMER"></el-option>
            <el-option label="管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const users = ref([])
const userRole = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('添加用户')
const form = ref({
  id: null,
  username: '',
  password: '',
  name: '',
  phone: '',
  role: '',
  status: 1
})
const formRef = ref(null)

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'blur' }
  ]
}

onMounted(async () => {
  await loadUsers()
})

const loadUsers = async () => {
  try {
    const response = await axios.get('/admin/users', {
      params: {
        page: currentPage.value,
        size: pageSize.value,
        role: userRole.value
      }
    })
    users.value = response.data.data?.records || []
    total.value = response.data.data?.total || 0
  } catch (error) {
    console.error('获取用户失败:', error)
  }
}

const handleSizeChange = async (size) => {
  pageSize.value = size
  await loadUsers()
}

const handleCurrentChange = async (current) => {
  currentPage.value = current
  await loadUsers()
}

const getRoleType = (role) => {
  switch (role) {
    case 'FARMER': return 'success'
    case 'CUSTOMER': return 'primary'
    case 'ADMIN': return 'warning'
    default: return ''
  }
}

const getRoleText = (role) => {
  switch (role) {
    case 'FARMER': return '农户'
    case 'CUSTOMER': return '客户'
    case 'ADMIN': return '管理员'
    default: return role
  }
}

const addUser = () => {
  dialogTitle.value = '添加用户'
  form.value = {
    id: null,
    username: '',
    password: '',
    name: '',
    phone: '',
    role: '',
    status: 1
  }
  dialogVisible.value = true
}

const editUser = (user) => {
  dialogTitle.value = '编辑用户'
  form.value = { ...user }
  dialogVisible.value = true
}

const saveUser = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await axios.put(`/admin/users/${form.value.id}`, form.value)
        } else {
          await axios.post('/admin/users', form.value)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        await loadUsers()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      }
    }
  })
}

const updateStatus = async (user) => {
  try {
    await axios.put(`/admin/users/${user.id}`, user)
    ElMessage.success('状态更新成功')
  } catch (error) {
    console.error('状态更新失败:', error)
    ElMessage.error('状态更新失败')
    user.status = !user.status
  }
}

const deleteUser = async (user) => {
  try {
    await axios.delete(`/admin/users/${user.id}`)
    ElMessage.success('删除成功')
    await loadUsers()
  } catch (error) {
    console.error('删除失败:', error)
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.users {
  padding: 20px;
}

.filter-section {
  margin: 20px 0;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>