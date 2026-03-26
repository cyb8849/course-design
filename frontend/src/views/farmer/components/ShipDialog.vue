<template>
  <el-dialog
    v-model="visible"
    title="发货"
    width="500px"
    :close-on-click-modal="false"
  >
    <el-form :model="form" label-width="100px">
      <el-form-item label="订单号">
        <span>{{ orderNo }}</span>
      </el-form-item>
      
      <el-form-item label="快递公司" required>
        <el-select
          v-model="form.expressCompany"
          placeholder="请选择快递公司"
          style="width: 100%"
        >
          <el-option
            v-for="company in expressCompanies"
            :key="company.code"
            :label="company.name"
            :value="company.code"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="快递单号" required>
        <el-input
          v-model="form.trackingNo"
          placeholder="请输入快递单号"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="handleShip" :loading="loading">
          确认发货
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  modelValue: Boolean,
  orderId: Number,
  orderNo: String,
  subOrderId: Number
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const loading = ref(false)
const expressCompanies = ref([])

const form = reactive({
  expressCompany: '',
  trackingNo: ''
})

// 监听visible变化
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadExpressCompanies()
    // 重置表单
    form.expressCompany = ''
    form.trackingNo = ''
  }
})

watch(() => visible.value, (val) => {
  emit('update:modelValue', val)
})

// 加载快递公司列表
const loadExpressCompanies = async () => {
  try {
    const response = await axios.get('/logistics/companies')
    if (response.data.code === 200) {
      expressCompanies.value = response.data.data
    }
  } catch (error) {
    console.error('加载快递公司列表失败:', error)
  }
}

// 处理发货
const handleShip = async () => {
  if (!form.expressCompany) {
    ElMessage.warning('请选择快递公司')
    return
  }
  
  if (!form.trackingNo.trim()) {
    ElMessage.warning('请输入快递单号')
    return
  }
  
  loading.value = true
  
  try {
    // 1. 创建物流信息
    const logisticsResponse = await axios.post('/logistics/create', null, {
      params: {
        orderId: props.orderId,
        orderSubId: props.subOrderId,
        expressCompany: form.expressCompany,
        trackingNo: form.trackingNo
      }
    })
    
    if (logisticsResponse.data.code !== 200) {
      ElMessage.error(logisticsResponse.data.message || '创建物流信息失败')
      return
    }
    
    // 2. 更新子订单状态为已发货
    const shipResponse = await axios.put(`/farmer/sub-orders/${props.subOrderId}/ship`, null, {
      params: {
        logisticsId: logisticsResponse.data.data.id
      },
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}`
      }
    })
    
    if (shipResponse.data.code === 200) {
      ElMessage.success('发货成功')
      visible.value = false
      emit('success')
    } else {
      ElMessage.error(shipResponse.data.message || '发货失败')
    }
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
