<template>
  <div class="farmer-ai">
    <h2>AI智能助手</h2>
    
    <el-card class="ai-card">
      <template #header>
        <div class="card-header">
          <span>智能问答</span>
        </div>
      </template>
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessages">
          <div v-for="(message, index) in messages" :key="index" :class="['message', message.role]">
            <div class="message-content">{{ message.content }}</div>
          </div>
        </div>
        <div class="chat-input">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题..."
            @keyup.enter.exact="sendMessage"
          />
          <el-button type="primary" @click="sendMessage">发送</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="ai-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>种植建议</span>
        </div>
      </template>
      <el-form :model="plantingForm" label-width="100px">
        <el-form-item label="作物">
          <el-input v-model="plantingForm.crop" placeholder="例如：莲藕" />
        </el-form-item>
        <el-form-item label="土壤类型">
          <el-select v-model="plantingForm.soilType" placeholder="请选择土壤类型">
            <el-option label="沙壤土" value="沙壤土" />
            <el-option label="粘土" value="粘土" />
            <el-option label="壤土" value="壤土" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="季节">
          <el-select v-model="plantingForm.season" placeholder="请选择季节">
            <el-option label="春季" value="春季" />
            <el-option label="夏季" value="夏季" />
            <el-option label="秋季" value="秋季" />
            <el-option label="冬季" value="冬季" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getPlantingSuggestion">获取种植建议</el-button>
        </el-form-item>
        <el-form-item v-if="plantingSuggestion">
          <div class="suggestion-content">{{ plantingSuggestion }}</div>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="ai-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>销售建议</span>
        </div>
      </template>
      <el-form :model="salesForm" label-width="100px">
        <el-form-item label="产品">
          <el-input v-model="salesForm.product" placeholder="例如：莲藕" />
        </el-form-item>
        <el-form-item label="目标市场">
          <el-select v-model="salesForm.targetMarket" placeholder="请选择目标市场">
            <el-option label="本地市场" value="本地市场" />
            <el-option label="全国市场" value="全国市场" />
            <el-option label="国际市场" value="国际市场" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getSalesSuggestion">获取销售建议</el-button>
        </el-form-item>
        <el-form-item v-if="salesSuggestion">
          <div class="suggestion-content">{{ salesSuggestion }}</div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../store'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()

const userStore = useUserStore()

// 智能问答
const messages = ref([
  { role: 'assistant', content: '您好！我是您的AI智能助手，有什么可以帮您的吗？' }
])
const inputMessage = ref('')
const chatMessages = ref(null)

// 种植建议
const plantingForm = ref({
  crop: '莲藕',
  soilType: '',
  season: ''
})
const plantingSuggestion = ref('')

// 销售建议
const salesForm = ref({
  product: '莲藕产品',
  targetMarket: ''
})
const salesSuggestion = ref('')

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})

const sendMessage = async () => {
  if (!inputMessage.value.trim()) {
    return
  }

  // 添加用户消息
  const userMessage = {
    role: 'user',
    content: inputMessage.value
  }
  messages.value.push(userMessage)

  const question = inputMessage.value
  inputMessage.value = ''

  // 添加AI回答的占位符
  const assistantMessage = {
    role: 'assistant',
    content: ''
  }
  const assistantIndex = messages.value.push(assistantMessage) - 1

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 调用AI流式接口
    const response = await fetch('/api/farmer/ai/chat/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify({ question })
    })
    
    if (!response.ok) {
      throw new Error('AI服务暂时不可用，请稍后再试。')
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6)
          if (data === '[DONE]') {
            break
          }
          // 更新AI回答
          messages.value[assistantIndex].content += data
          // 滚动到底部
          scrollToBottom()
        }
      }
    }
  } catch (error) {
    console.error('AI问答失败:', error)
    messages.value[assistantIndex].content = '抱歉，AI服务暂时不可用，请稍后再试。'
    // 滚动到底部
    scrollToBottom()
  } finally {
    // 滚动到底部
    scrollToBottom()
  }
}

const getPlantingSuggestion = async () => {
  try {
    // 清空之前的建议
    plantingSuggestion.value = ''
    
    // 调用AI流式接口
    const response = await fetch('/api/farmer/ai/planting-suggestion/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(plantingForm.value)
    })
    
    if (!response.ok) {
      throw new Error('AI服务暂时不可用，请稍后再试。')
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6)
          if (data === '[DONE]') {
            break
          }
          // 更新种植建议
          plantingSuggestion.value += data
        }
      }
    }
  } catch (error) {
    console.error('获取种植建议失败:', error)
    plantingSuggestion.value = '抱歉，AI服务暂时不可用，请稍后再试。'
    ElMessage.error('获取种植建议失败，请稍后再试')
  }
}

const getSalesSuggestion = async () => {
  try {
    // 清空之前的建议
    salesSuggestion.value = ''
    
    // 调用AI流式接口
    const response = await fetch('/api/farmer/ai/sales-suggestion/stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`,
        'Accept': 'text/event-stream'
      },
      body: JSON.stringify(salesForm.value)
    })
    
    if (!response.ok) {
      throw new Error('AI服务暂时不可用，请稍后再试。')
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value)
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const data = line.substring(6)
          if (data === '[DONE]') {
            break
          }
          // 更新销售建议
          salesSuggestion.value += data
        }
      }
    }
  } catch (error) {
    console.error('获取销售建议失败:', error)
    salesSuggestion.value = '抱歉，AI服务暂时不可用，请稍后再试。'
    ElMessage.error('获取销售建议失败，请稍后再试')
  }
}

const scrollToBottom = () => {
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}
</script>

<style scoped>
.farmer-ai {
  padding: 20px;
}

.farmer-ai h2 {
  margin-bottom: 30px;
  color: #333;
}

.ai-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-container {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
}

.message {
  margin-bottom: 10px;
  max-width: 80%;
  padding: 10px;
  border-radius: 8px;
}

.message.user {
  align-self: flex-end;
  background-color: #ecf5ff;
  margin-left: auto;
}

.message.assistant {
  align-self: flex-start;
  background-color: #f5f7fa;
  margin-right: auto;
}

.chat-input {
  display: flex;
  gap: 10px;
}

.chat-input .el-input {
  flex: 1;
}

.suggestion-content {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #f5f7fa;
  white-space: pre-wrap;
  line-height: 1.5;
}
</style>
