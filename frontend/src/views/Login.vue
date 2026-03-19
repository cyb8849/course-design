<template>
  <div class="login-container">
    <div class="login-box">
      <h2>西胪莲藕电商管理系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="loginForm.role">
            <el-radio label="CUSTOMER">消费者</el-radio>
            <el-radio label="FARMER">农户</el-radio>
            <el-radio label="ADMIN">管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
          <el-button type="info" class="register-btn" @click="showRegister = true">注册</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog v-model="showRegister" title="用户注册" width="500px">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="registerForm.role">
            <el-radio label="CUSTOMER">消费者</el-radio>
            <el-radio label="FARMER">农户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="registerForm.address" placeholder="请输入地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showRegister = false">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="registerLoading">注册</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../store'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const registerFormRef = ref()

const loading = ref(false)
const registerLoading = ref(false)
const showRegister = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'CUSTOMER'
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  role: 'CUSTOMER',
  name: '',
  phone: '',
  address: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }],
  address: [{ required: true, message: '请输入地址', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    const result = await userStore.login(loginForm.username, loginForm.password, loginForm.role)
    if (result.code === 200) {
      ElMessage.success('登录成功')
      // 根据角色跳转到对应首页
      if (result.data.user.role === 'CUSTOMER') {
        router.push('/home')
      } else if (result.data.user.role === 'FARMER') {
        router.push('/farmer/home')
      } else if (result.data.user.role === 'ADMIN') {
        router.push('/admin/home')
      }
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('登录失败：' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerFormRef.value.validate()
    registerLoading.value = true
    const result = await userStore.register(registerForm)
    if (result.code === 200) {
      ElMessage.success('注册成功')
      showRegister.value = false
      // 自动填充用户名和密码到登录表单
      loginForm.username = registerForm.username
      loginForm.password = registerForm.password
      loginForm.role = registerForm.role
    } else {
      ElMessage.error(result.message)
    }
  } catch (error) {
    ElMessage.error('注册失败：' + (error.response?.data?.message || error.message))
  } finally {
    registerLoading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.9) 0%, rgba(118, 75, 162, 0.9) 100%),
              url('https://images.unsplash.com/photo-1560493676-04071c5f467b?w=1920&h=1080&fit=crop');
  background-size: cover;
  background-position: center;
  position: relative;
}

.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1;
}

.login-box {
  background: rgba(255, 255, 255, 0.95);
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  width: 450px;
  z-index: 2;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.login-box:hover {
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.25);
  transform: translateY(-5px);
}

.login-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-btn {
  width: 100%;
  margin-bottom: 15px;
  padding: 12px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.register-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

/* 表单样式 */
.el-form-item {
  margin-bottom: 20px;
}

.el-input {
  border-radius: 8px;
  transition: all 0.3s ease;
}

.el-input:focus {
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.el-radio-group {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.el-radio {
  transition: all 0.3s ease;
}

.el-radio:hover {
  color: #667eea;
}

.el-radio__input.is-checked .el-radio__inner {
  background-color: #667eea;
  border-color: #667eea;
}

.el-radio__input.is-checked + .el-radio__label {
  color: #667eea;
}
</style>