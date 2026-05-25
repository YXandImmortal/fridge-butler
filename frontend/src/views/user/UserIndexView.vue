<template>
  <div v-loading="pageLoading" class="user-index-container">
    <!-- 欢迎区 -->
    <section class="welcome-section animate-in" style="animation-delay: 0s">
      <h1 class="welcome-title">
        <span class="greeting-emoji">{{ greetingEmoji }}</span>
        {{ greeting }}，{{ userStore.username || '用户' }}！
      </h1>
      <p class="welcome-subtitle">今天想怎么管理你的冰箱？</p>
    </section>

    <!-- 统计卡片 -->
    <StatsOverview
      :fridge-list="fridgeList"
      :item-list="itemList"
      :take-out-list="takeOutList"
      :expiring-summary="expiringSummary"
    />

    <!-- AI 聊天助手 -->
    <section class="ai-chat-section glass-card animate-in" style="animation-delay: 0.42s">
      <div class="chat-header">
        <div class="chat-header-left">
          <Logo class="ai-logo" />
          <div class="chat-title-info">
            <h3 class="chat-title">AI 冰箱管家</h3>
            <span class="chat-status">
              <span class="status-dot" />
              在线
            </span>
          </div>
        </div>
        <div class="chat-session-name">{{ currentSessionName }}</div>

        <div class="chat-header-actions">
          <button class="header-action-btn" title="新建会话" @click="createNewSession">
            <i class="iconfont icon-add-box" />
          </button>
          <button class="header-action-btn" title="会话列表" @click="drawerVisible = true">
            <i class="iconfont icon-list" />
          </button>
        </div>
      </div>

      <!-- 顶部向导面板（独立于消息列表） -->
      <transition name="wizard-panel">
        <div v-if="activeWizard && activeWizard.type === 'fridge_creation'" class="chat-wizard-panel">
          <div class="wizard-panel-header">
            <div class="wizard-panel-title">
              <i class="iconfont icon-fridge-line" />
              <span>创建冰箱向导</span>
            </div>
            <button class="wizard-panel-close" title="关闭向导" @click="handleWizardCancel">
              <i class="iconfont icon-close" />
            </button>
          </div>
          <FridgeCreationWizard
            :data="activeWizardData"
            @step-submit="handleWizardStepSubmit"
            @confirm="handleWizardConfirm"
            @cancel="handleWizardCancel"
            @skip="handleWizardSkip"
          />
        </div>
      </transition>

      <div ref="chatMessagesRef" class="chat-messages">
        <div
          v-for="(msg, idx) in messages"
          :key="msg.id"
          :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']"
        >
          <Logo v-if="msg.role === 'assistant'" class="ai-logo-sm" />
          <div v-else class="message-avatar">
            <span class="user-avatar-text">{{ userAvatarText }}</span>
          </div>
          <div :class="['message-bubble', { 'typing-bubble': aiTyping && msg.role === 'assistant' && !msg.content && !msg.data && idx === messages.length - 1 }]">
            <ChatStructuredMessage
              :msg="msg"
              :is-last="idx === messages.length - 1"
              :ai-typing="aiTyping"
              @action-confirm="handleActionConfirm"
              @action-cancel="handleActionCancel"
            />
          </div>
        </div>

      </div>

      <!-- 快捷指令（后端 suggestions + 固定快捷按钮） -->
      <div class="chat-quick-actions">
        <button
          v-for="action in defaultQuickActions"
          :key="action.text"
          class="quick-action-btn"
          @click="sendQuickMessage(action.text)"
        >
          {{ action.text }}
        </button>
        <transition name="el-zoom-in-bottom">
          <div v-show="suggestions.length > 0" class="suggestions-box">
            <span>猜你想：</span>
            <button
                v-for="text in suggestions"
                :key="text"
                class="quick-action-btn"
                @click="sendQuickMessage(text)"
            >
              {{ text }}
            </button>
          </div>
        </transition>

        <AttachmentSelector v-model="attachments" :fridge-list="fridgeList" :item-list="itemList" />
      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <input
          v-model="inputMessage"
          type="text"
          class="chat-input"
          :placeholder="aiTyping ? 'AI思考中(ง •_•)ง' : '输入你想问的问题，例如：我冰箱里还有什么鸡蛋？'"
          :disabled="aiTyping"
          @keydown.enter="sendMessage"
        />
        <button
          class="send-btn"
          :disabled="!inputMessage.trim() || aiTyping"
          @click="sendMessage"
        >
          <i class="iconfont icon-arrow-right-box" />
        </button>
      </div>
    </section>

    <!-- 快捷操作入口 -->
    <section class="quick-actions-row">
      <div
        v-for="(action, index) in navActions"
        :key="action.name"
        class="quick-action-card glass-card animate-in"
        :style="{ animationDelay: `${0.72 + index * 0.08}s` }"
        @click="$router.push(action.path)"
      >
        <div class="quick-action-icon-wrapper" :style="{ background: action.iconBg }">
          <i :class="['iconfont', action.icon, 'quick-action-icon']" :style="{ color: action.iconColor }" />
        </div>
        <div class="quick-action-name">{{ action.name }}</div>
        <div class="quick-action-desc">{{ action.desc }}</div>
      </div>
    </section>

    <!-- 数据趋势 -->
    <section class="trend-section glass-card animate-in" style="animation-delay: 1.04s">
      <div class="trend-header">
        <div class="trend-title-wrapper">
          <i class="iconfont icon-trending trend-title-icon" />
          <h3 class="trend-title">近7天取出/入库趋势</h3>
        </div>
      </div>
      <v-chart v-if="trendHasData" class="trend-chart" :option="trendOption" autoresize />
      <el-empty v-else description="近7天暂无数据" class="trend-empty" />
    </section>

    <!-- 会话列表 Drawer -->
    <ChatSessionDrawer
      v-model:visible="drawerVisible"
      :session-id="sessionId"
      :sessions="sessions"
      :session-loading="sessionLoading"
      @new-session="createNewSession"
      @switch-session="switchSession"
      @delete-session="handleDeleteSession"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import Logo from '@/components/Logo.vue'
import FridgeCreationWizard from '@/components/ai/FridgeCreationWizard.vue'
import StatsOverview from '@/components/ai/StatsOverview.vue'
import ChatStructuredMessage from '@/components/ai/ChatStructuredMessage.vue'
import AttachmentSelector from '@/components/ai/AttachmentSelector.vue'
import ChatSessionDrawer from '@/components/ai/ChatSessionDrawer.vue'
import { getFridgeTypeById } from '@/utils/fridgeTypeMap.js'
import { listMyFridges } from '@/api/fridge'
import { searchItems, getRecent30DaysTakeOutStats, getRecent30DaysAddStats, getExpiringSummary } from '@/api/item'
import { sendChatMessage, sendChatMessageStream, getChatSessions, deleteChatSession, getChatSessionMessages } from '@/api/ai'
import { use, graphic } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getChartThemeColors } from '@/utils/data-analysis'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()

// ==================== 数据状态 ====================
const pageLoading = ref(false)
const fridgeList = ref([])
const itemList = ref([])
const takeOutList = ref([])
const addList = ref([])
const expiringSummary = ref({ expiringCount: 0, expiredCount: 0, totalExpiring: 0 })

// AI 聊天状态
const SESSION_STORAGE_KEY = 'ai_chat_session_id'
const sessionId = ref(localStorage.getItem(SESSION_STORAGE_KEY) || null)
const suggestions = ref([])
const drawerVisible = ref(false)
const sessions = ref([])
const sessionLoading = ref(false)

// 向导状态（当前激活的AI向导）
const activeWizard = ref(null) // { type: 'fridge_creation', currentStep, totalSteps, steps, formData, currentInput }
const wizardCompleted = ref(false)

const activeWizardData = computed(() => {
  if (!activeWizard.value) return null
  const { type, ...data } = activeWizard.value
  return data
})

// 是否处于冰箱创建向导的确认页（最后一步）
const isWizardConfirmStep = computed(() => {
  if (!activeWizard.value || activeWizard.value.type !== 'fridge_creation') return false
  const total = activeWizard.value.totalSteps || 3
  return activeWizard.value.currentStep >= total - 1
})

// ==================== 欢迎语 ====================
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 9) return '早上好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const greetingEmoji = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '🌙'
  if (hour < 9) return '🌅'
  if (hour < 12) return '☀️'
  if (hour < 14) return '🍱'
  if (hour < 18) return '🌤️'
  return '🌆'
})

const userAvatarText = computed(() => {
  const name = userStore.username || '我'
  return name.charAt(0).toUpperCase()
})

const currentSessionName = computed(() => {
  if (!sessionId.value) return '新对话'
  const session = sessions.value.find(s => s.sessionId === sessionId.value)
  return session?.title || '新对话'
})

// ==================== AI 聊天 ====================
function generateMsgId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

const messages = ref([
  {
    id: generateMsgId(),
    role: 'assistant',
    content: '你好！我是你的 AI 冰箱管家 🎉\n我可以帮你：\n• 查询冰箱库存\n• 查看临期提醒\n• 推荐菜谱\n• 回答食材相关问题\n\n试试点击下方快捷按钮，或直接输入你想问的问题~',
    time: formatTime(new Date())
  }
])
const inputMessage = ref('')
const aiTyping = ref(false)
const chatMessagesRef = ref(null)
const abortController = ref(null)

// 附件状态
const attachments = ref([])

const defaultQuickActions = [
  { text: '查看冰箱' },
  { text: '有什么食材' },
  { text: '临期提醒' },
  { text: '推荐菜谱' }
]

const defaultQuickActionsTextArr = [
    '查看冰箱','有什么食材','临期提醒','推荐菜谱'
]

function formatTime(date) {
  const h = String(date.getHours()).padStart(2, '0')
  const m = String(date.getMinutes()).padStart(2, '0')
  return `${h}:${m}`
}

function scrollToBottom() {
  nextTick(() => {
    const el = chatMessagesRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

function sendQuickMessage(text) {
  inputMessage.value = text
  sendMessage()
}

async function doSendChat(text, currentAttachments) {
  // 用户消息
  messages.value.push({
    id: generateMsgId(),
    role: 'user',
    content: text,
    attachments: currentAttachments,
    time: formatTime(new Date())
  })
  scrollToBottom()

  // AI 回复占位
  aiTyping.value = true
  messages.value.push({
    id: generateMsgId(),
    role: 'assistant',
    content: '',
    messageType: 'text',
    data: null,
    time: formatTime(new Date())
  })
  const assistantMsg = messages.value[messages.value.length - 1]
  scrollToBottom()

  // 中断之前的流
  if (abortController.value) {
    abortController.value.abort()
  }
  abortController.value = new AbortController()

  let useFallback = false
  let scrollTimer = null

  const scheduleScroll = () => {
    if (!scrollTimer) {
      scrollTimer = setTimeout(() => {
        scrollTimer = null
        scrollToBottom()
      }, 50)
    }
  }

  const payload = {
    message: text,
    sessionId: sessionId.value,
    attachments: currentAttachments
  }

  // 如果处于向导模式，附加向导上下文
  if (activeWizard.value) {
    payload.wizardContext = {
      type: activeWizard.value.type,
      currentStep: activeWizard.value.currentStep,
      formData: activeWizard.value.formData
    }
  }

  // 先尝试 SSE 流式接口
  try {
    await sendChatMessageStream({
      ...payload,
      signal: abortController.value.signal,
      onText: (chunk) => {
        assistantMsg.content += chunk
        scheduleScroll()
      },
      onCard: (messageType, data) => {
        assistantMsg.messageType = messageType
        assistantMsg.data = data
        if (messageType === 'fridge_creation_wizard') {
          wizardCompleted.value = false
          activeWizard.value = {
            type: 'fridge_creation',
            currentStep: data.currentStep,
            totalSteps: data.totalSteps,
            steps: data.steps || [],
            formData: data.formData || {},
            currentInput: data.currentInput || null
          }
        } else {
          activeWizard.value = null
        }
        scrollToBottom()
      },
      onDone: (newSid, newSuggestions) => {
        if (scrollTimer) {
          clearTimeout(scrollTimer)
          scrollTimer = null
        }
        sessionId.value = newSid || sessionId.value
        if (sessionId.value) {
          localStorage.setItem(SESSION_STORAGE_KEY, sessionId.value)
        }
        suggestions.value = (newSuggestions || []).filter(
          item => !defaultQuickActionsTextArr.includes(item)
        )
        aiTyping.value = false
        abortController.value = null
        loadSessions()
        scrollToBottom()
      },
      onError: (msg) => {
        console.error('SSE 流式错误:', msg)
        useFallback = true
        const idx = messages.value.indexOf(assistantMsg)
        if (idx !== -1) {
          messages.value.splice(idx, 1)
        }
      }
    })
  } catch (err) {
    if (err.name !== 'AbortError') {
      console.error('SSE 请求失败:', err)
      useFallback = true
      const idx = messages.value.indexOf(assistantMsg)
      if (idx !== -1) {
        messages.value.splice(idx, 1)
      }
    } else {
      // 用户主动中断或组件卸载
      aiTyping.value = false
      abortController.value = null
      return
    }
  }

  // 兜底：如果 SSE 流正常结束但 onDone 未被触发（如连接静默关闭），强制重置状态
  if (!useFallback && aiTyping.value) {
    aiTyping.value = false
    abortController.value = null
  }

  // SSE 失败时降级到旧同步接口
  if (useFallback) {
    aiTyping.value = true
    try {
      const res = await sendChatMessage(payload)

      if (res.code === 200 && res.data) {
        const { sessionId: newSid, reply, suggestions: newSuggestions } = res.data
        sessionId.value = newSid || sessionId.value
        if (sessionId.value) {
          localStorage.setItem(SESSION_STORAGE_KEY, sessionId.value)
        }
        suggestions.value = newSuggestions || []

        //过滤已有快速操作
        suggestions.value = suggestions.value.filter(item => !defaultQuickActionsTextArr.includes(item))

        messages.value.push({
          id: generateMsgId(),
          role: 'assistant',
          content: reply.text || '',
          messageType: reply.messageType || 'text',
          data: reply.data || null,
          time: formatTime(new Date())
        })

        if (reply.messageType === 'fridge_creation_wizard') {
          wizardCompleted.value = false
          activeWizard.value = {
            type: 'fridge_creation',
            currentStep: reply.data.currentStep,
            totalSteps: reply.data.totalSteps,
            steps: reply.data.steps || [],
            formData: reply.data.formData || {},
            currentInput: reply.data.currentInput || null
          }
        } else {
          activeWizard.value = null
        }

        // 刷新会话列表（新会话可能已生成标题）
        loadSessions()
      } else {
        messages.value.push({
          id: generateMsgId(),
          role: 'assistant',
          content: '服务暂时不可用，请稍后再试。',
          messageType: 'text',
          data: null,
          time: formatTime(new Date())
        })
        suggestions.value = []
        activeWizard.value = null
      }
    } catch (err) {
      console.error('AI 聊天请求失败:', err)
      messages.value.push({
        id: generateMsgId(),
        role: 'assistant',
        content: '网络连接异常，请检查网络后重试。',
        messageType: 'text',
        data: null,
        time: formatTime(new Date())
      })
      suggestions.value = []
      activeWizard.value = null
    } finally {
      aiTyping.value = false
      scrollToBottom()
    }
  }
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || aiTyping.value) return
  const currentAttachments = attachments.value.map(a => ({ ...a }))
  inputMessage.value = ''
  attachments.value = []  // 发送后清空附件
  await doSendChat(text, currentAttachments)
}

// ==================== action_confirm 处理 ====================
async function handleActionConfirm(msg) {
  const actionData = msg.data
  if (!actionData) return

  // 标记该消息已处理
  msg.confirmed = true
  msg.content = `已确认：${actionData.targetName || '操作'}正在执行...`
  msg.messageType = 'text'
  msg.data = null

  try {
    switch (actionData.action) {
      case 'delete_fridge': {
        const { deleteFridge } = await import('@/api/fridge')
        const res = await deleteFridge(actionData.targetId)
        if (res.code === 200) {
          msg.content = `✅ 已删除「${actionData.targetName}」。`
          await fetchPageData()
        } else {
          msg.content = `❌ 删除失败：${res.message || '未知错误'}`
        }
        break
      }
      default:
        msg.content = `✅ 已确认执行「${actionData.action}」。`
    }
  } catch (err) {
    console.error('操作执行失败:', err)
    msg.content = '❌ 操作执行失败，请稍后重试。'
  }

  scrollToBottom()
}

function handleActionCancel(msg) {
  msg.confirmed = false
  msg.content = '已取消操作。'
  msg.messageType = 'text'
  msg.data = null
  scrollToBottom()
}

// ==================== fridge_creation_wizard 处理 ====================
function handleWizardStepSubmit({ field, value, formData }) {
  // 只更新 formData，不乐观更新 currentStep，避免后端根据 currentStep 推断下一步时跳过步骤
  activeWizard.value = {
    ...(activeWizard.value || {}),
    type: 'fridge_creation',
    formData
  }

  let messageText
  if (field === 'fridgeTypeId') {
    const type = getFridgeTypeById(Number(value))
    messageText = type ? `我选择${type.name}` : String(value)
  } else if (field === 'isDefault') {
    messageText = value ? '设为默认冰箱' : '不设为默认冰箱'
  } else if (field === 'totalCapacity') {
    messageText = value ? `${value}升` : '跳过容量'
  } else if (field === 'address') {
    const remark = formData.remark
    if (value && remark) {
      messageText = `地址：${value}，备注：${remark}`
    } else if (value) {
      messageText = value
    } else if (remark) {
      messageText = `备注：${remark}`
    } else {
      messageText = '跳过地址和备注'
    }
  } else {
    messageText = String(value || '')
  }

  doSendChat(messageText, [])
}

function handleWizardSkip({ field, formData }) {
  activeWizard.value = {
    ...(activeWizard.value || {}),
    type: 'fridge_creation',
    formData
  }

  let messageText
  if (field === 'fridgeTypeId') {
    messageText = '跳过冰箱类型'
  } else if (field === 'totalCapacity') {
    messageText = '跳过容量'
  } else if (field === 'isDefault') {
    messageText = '跳过默认设置'
  } else if (field === 'address') {
    messageText = '跳过地址和备注'
  } else {
    messageText = `跳过${field}`
  }

  doSendChat(messageText, [])
}

async function handleWizardConfirm(formData) {
  if (!formData.name || !formData.name.trim()) {
    messages.value.push({
      id: generateMsgId(),
      role: 'assistant',
      content: '❌ 冰箱名称不能为空，请重新输入。',
      messageType: 'text',
      data: null,
      time: formatTime(new Date())
    })
    scrollToBottom()
    return
  }
  // 验证通过后收起向导面板
  wizardCompleted.value = true
  activeWizard.value = null
  try {
    const { createFridge } = await import('@/api/fridge')
    const res = await createFridge({
      fridgeName: formData.name,
      fridgeTypeId: formData.fridgeTypeId || undefined,
      totalCapacity: formData.totalCapacity || undefined,
      isDefault: formData.isDefault || undefined,
      fridgeAddress: formData.address || undefined,
      remark: formData.remark || undefined
    })
    if (res.code === 200) {
      messages.value.push({
        id: generateMsgId(),
        role: 'assistant',
        content: `✅ 冰箱「${formData.name}」创建成功！现在你可以向里面添加食材了~`,
        messageType: 'text',
        data: null,
        time: formatTime(new Date())
      })
      await fetchPageData()
    } else {
      messages.value.push({
        id: generateMsgId(),
        role: 'assistant',
        content: `❌ 创建失败：${res.message || '未知错误'}`,
        messageType: 'text',
        data: null,
        time: formatTime(new Date())
      })
    }
  } catch (err) {
    console.error('创建冰箱失败:', err)
    messages.value.push({
      id: generateMsgId(),
      role: 'assistant',
      content: '❌ 创建冰箱失败，请稍后重试。',
      messageType: 'text',
      data: null,
      time: formatTime(new Date())
    })
  }
  scrollToBottom()
}

function handleWizardCancel() {
  activeWizard.value = null
  messages.value.push({
    id: generateMsgId(),
    role: 'assistant',
    content: '已取消创建冰箱。如需创建，请随时告诉我~',
    messageType: 'text',
    data: null,
    time: formatTime(new Date())
  })
  scrollToBottom()
}

// ==================== 会话管理 ====================
async function loadSessions() {
  try {
    sessionLoading.value = true
    const res = await getChatSessions()
    if (res.code === 200 && Array.isArray(res.data)) {
      sessions.value = res.data
    } else {
      sessions.value = []
    }
  } catch (err) {
    console.error('加载会话列表失败:', err)
    sessions.value = []
  } finally {
    sessionLoading.value = false
  }
}

async function switchSession(sid) {
  if (sid === sessionId.value) {
    drawerVisible.value = false
    return
  }
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
  aiTyping.value = false
  activeWizard.value = null
  sessionId.value = sid
  localStorage.setItem(SESSION_STORAGE_KEY, sid)
  messages.value = []
  suggestions.value = []
  drawerVisible.value = false

  // 尝试加载历史消息
  try {
    const res = await getChatSessionMessages(sid)
    if (res.code === 200 && Array.isArray(res.data)) {
      messages.value = res.data.map(m => ({
        id: m.id || generateMsgId(),
        role: m.role,
        content: m.content || '',
        messageType: m.messageType || 'text',
        data: m.data || null,
        time: m.createTime ? formatTime(new Date(m.createTime.replace(' ', 'T'))) : formatTime(new Date())
      }))
    }
  } catch (err) {
    console.error('加载历史消息失败:', err)
  } finally {
    scrollToBottom()
  }
}

function createNewSession() {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
  aiTyping.value = false
  sessionId.value = null
  activeWizard.value = null
  messages.value = [{
    id: generateMsgId(),
    role: 'assistant',
    content: '你好！我是你的 AI 冰箱管家 🎉\n我可以帮你：\n• 查询冰箱库存\n• 查看临期提醒\n• 推荐菜谱\n• 回答食材相关问题\n\n试试点击下方快捷按钮，或直接输入你想问的问题~',
    time: formatTime(new Date())
  }]
  suggestions.value = []
  localStorage.removeItem(SESSION_STORAGE_KEY)
  drawerVisible.value = false
}

async function handleDeleteSession(sid) {
  try {
    const res = await deleteChatSession(sid)
    if (res.code === 200) {
      sessions.value = sessions.value.filter(s => s.sessionId !== sid)
      if (sessionId.value === sid) {
        createNewSession()
      }
    }
  } catch (err) {
    console.error('删除会话失败:', err)
  }
}

// ==================== 快捷导航 ====================
const navActions = [
  {
    name: '冰箱管理',
    desc: '查看和管理你的冰箱',
    path: '/fridge/list',
    icon: 'icon-fridge-line',
    iconBg: 'linear-gradient(135deg, rgba(100,181,246,0.15) 0%, rgba(129,212,250,0.1) 100%)',
    iconColor: '#64B5F6'
  },
  {
    name: '物品管理',
    desc: '管理冰箱内的食材',
    path: '/fridge/items',
    icon: 'icon-inbox',
    iconBg: 'linear-gradient(135deg, rgba(129,199,132,0.15) 0%, rgba(165,214,167,0.1) 100%)',
    iconColor: '#81C784'
  },
  {
    name: '数据中心',
    desc: '查看统计与分析图表',
    path: '/data-center/index',
    icon: 'icon-chart',
    iconBg: 'linear-gradient(135deg, rgba(179,157,219,0.15) 0%, rgba(206,194,238,0.1) 100%)',
    iconColor: '#B39DDB'
  },
  {
    name: '个人中心',
    desc: '管理账号和个人信息',
    path: '/user/center',
    icon: 'icon-user',
    iconBg: 'linear-gradient(135deg, rgba(244,143,177,0.15) 0%, rgba(248,187,208,0.1) 100%)',
    iconColor: '#F48FB1'
  }
]

// ==================== 趋势图 ====================
const last7Days = computed(() => {
  const dates = []
  const now = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    dates.push(d.toISOString().split('T')[0])
  }
  return dates
})

const trendHasData = computed(() => {
  const dates = last7Days.value
  const hasTakeOut = dates.some(date => {
    const item = takeOutList.value.find(t => t.date === date)
    return item && (item.count || 0) > 0
  })
  const hasAdd = dates.some(date => {
    const item = addList.value.find(t => t.date === date)
    return item && (item.count || 0) > 0
  })
  return hasTakeOut || hasAdd
})

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}

const trendOption = computed(() => {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const dates = last7Days.value

  const takeOutCounts = dates.map(date => {
    const item = takeOutList.value.find(t => t.date === date)
    return item ? (item.count || 0) : 0
  })

  const addCounts = dates.map(date => {
    const item = addList.value.find(t => t.date === date)
    return item ? (item.count || 0) : 0
  })

  const takeOutColor = '#64B5F6'
  const addColor = '#81C784'

  return {
    color: [takeOutColor, addColor],
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor },
      axisPointer: {
        type: 'line',
        lineStyle: { color: colors.primaryColor, width: 1, type: 'dashed' }
      }
    },
    legend: {
      data: ['取出', '入库'],
      top: '2%',
      textStyle: { color: colors.subTextColor, fontSize: 12 },
      itemWidth: 12,
      itemHeight: 8
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '18%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.map(d => {
        const date = new Date(d)
        return `${date.getMonth() + 1}/${date.getDate()}`
      }),
      axisLine: { lineStyle: { color: colors.axisLineColor } },
      axisLabel: { color: colors.subTextColor, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: colors.splitLineColor, type: 'dashed' } },
      axisLabel: { color: colors.subTextColor }
    },
    series: [
      {
        name: '取出',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: { width: 3, color: takeOutColor },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: hexToRgba(takeOutColor, 0.3) },
            { offset: 1, color: hexToRgba(takeOutColor, 0.02) }
          ])
        },
        itemStyle: { color: takeOutColor, borderColor: colors.tooltipBg, borderWidth: 2 },
        data: takeOutCounts
      },
      {
        name: '入库',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: { width: 3, color: addColor },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: hexToRgba(addColor, 0.3) },
            { offset: 1, color: hexToRgba(addColor, 0.02) }
          ])
        },
        itemStyle: { color: addColor, borderColor: colors.tooltipBg, borderWidth: 2 },
        data: addCounts
      }
    ]
  }
})

// ==================== 数据获取 ====================
async function fetchPageData() {
  pageLoading.value = true
  try {
    const [fridgeRes, itemRes, takeOutRes, addRes, expiringRes] = await Promise.all([
      listMyFridges(),
      searchItems({}),
      getRecent30DaysTakeOutStats().catch(err => {
        console.error('获取取出趋势失败:', err)
        return { code: -1, data: [] }
      }),
      getRecent30DaysAddStats().catch(err => {
        console.error('获取入库趋势失败:', err)
        return { code: -1, data: [] }
      }),
      getExpiringSummary().catch(err => {
        console.error('获取临期汇总失败:', err)
        return { code: -1, data: null }
      })
    ])

    if (fridgeRes.code === 200 && Array.isArray(fridgeRes.data)) {
      fridgeList.value = fridgeRes.data
    } else {
      fridgeList.value = []
    }

    if (itemRes.code === 200 && Array.isArray(itemRes.data)) {
      itemList.value = itemRes.data
    } else {
      itemList.value = []
    }

    if (takeOutRes.code === 200 && Array.isArray(takeOutRes.data)) {
      takeOutList.value = takeOutRes.data
    } else {
      takeOutList.value = []
    }

    if (addRes.code === 200 && Array.isArray(addRes.data)) {
      addList.value = addRes.data
    } else {
      addList.value = []
    }

    if (expiringRes.code === 200 && expiringRes.data) {
      expiringSummary.value = expiringRes.data
    } else {
      expiringSummary.value = { expiringCount: 0, expiredCount: 0, totalExpiring: 0 }
    }
  } catch (error) {
    console.error('获取首页数据失败:', error)
    fridgeList.value = []
    itemList.value = []
    takeOutList.value = []
    addList.value = []
  } finally {
    pageLoading.value = false
  }
}

onMounted(() => {
  fetchPageData()
  loadSessions()

  // 如果本地有保存的 sessionId，自动恢复该会话的历史消息
  if (sessionId.value) {
    getChatSessionMessages(sessionId.value)
      .then(res => {
        if (res.code === 200 && Array.isArray(res.data) && res.data.length > 0) {
          messages.value = res.data.map(m => ({
            role: m.role,
            content: m.content || '',
            messageType: m.messageType || 'text',
            data: m.data || null,
            time: m.createTime ? formatTime(new Date(m.createTime.replace(' ', 'T'))) : formatTime(new Date())
          }))
        }
      })
      .catch(err => {
        console.error('恢复历史消息失败:', err)
      })
  }

  // 处理从其他页面跳转过来的 AI 快捷指令（如冰箱列表页的「AI帮我创建」）
  const aiMessage = route.query.aiMessage
  if (aiMessage && typeof aiMessage === 'string') {
    router.replace({ path: '/user/index' })
    doSendChat(aiMessage, [])
  }
})

onUnmounted(() => {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
})
</script>

<style scoped lang="scss">
.user-index-container {
  max-width: 1200px;
  margin: 0 auto;
  padding-bottom: var(--space-8);
}

/* ==================== 动画 ==================== */
.animate-in {
  opacity: 0;
  animation: fade-in-up 0.6s ease-out forwards;
}

@keyframes fade-in-up {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ==================== 欢迎区 ==================== */
.welcome-section {
  text-align: center;
  padding: var(--space-5);
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.greeting-emoji {
  font-size: 32px;
  line-height: 1;
}

.welcome-subtitle {
  font-size: 15px;
  color: var(--text-secondary);
  margin: 0;
}

/* ==================== AI 聊天 ==================== */
.ai-chat-section {
  display: flex;
  flex-direction: column;
  margin-bottom: var(--space-5);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-5) var(--space-5) var(--space-4);
  border-bottom: 1px solid var(--border-color);
}

.chat-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: white;
}

.ai-logo {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
}

.ai-logo-sm {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
}

.chat-title-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chat-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.chat-status {
  font-size: 12px;
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #81C784;
  animation: pulse-dot 2s ease-in-out infinite;
}

@keyframes pulse-dot {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chat-messages {
  min-height: 280px;
  max-height: 600px;
  padding: var(--space-4) var(--space-5);
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  overflow-y: auto;
}

.message {
  display: flex;
  gap: var(--space-3);
  align-items: flex-start;
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
}

.message-user .message-avatar {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.user-avatar-text {
  font-size: 14px;
  font-weight: 600;
}

.message-bubble {
  max-width: 70%;
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-primary);
  word-break: break-word;
}

.message-ai .message-bubble {
  background: var(--main-content-bg);
  border: 1px solid var(--border-color);
  border-top-left-radius: 4px;
}

.message-user .message-bubble {
  background: var(--user-message-bg);
  color: white;
  border-top-right-radius: 4px;
}

.typing-bubble {
  padding: 14px 18px;
}

/* 快捷指令 */
.chat-quick-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5) var(--space-5) var(--space-3);
  border-top: 1px solid var(--border-color);

  .suggestions-box {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: var(--space-2);

    span {
      margin-left: var(--space-6);
      font-size: 14px;
    }
  }
}

.quick-action-btn {
  padding: 6px 14px;
  border-radius: 16px;
  border: 1px solid var(--border-light);
  background: var(--glass-bg);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--primary-light);
    color: var(--primary-dark);
    border-color: var(--primary-color);
  }
}

/* 输入区 */
.chat-input-area {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: 0 var(--space-5) var(--space-5);
}

.chat-input {
  flex: 1;
  height: 42px;
  padding: 0 var(--space-4);
  border-radius: 21px;
  border: 1px solid var(--border-color);
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;

  &::placeholder {
    color: var(--text-tertiary);
  }

  &:focus {
    border-color: var(--primary-color);
    box-shadow: var(--shadow-input-focus);
  }
}

.send-btn {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  border: none;
  background: var(--primary-color);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    transform: scale(1.05);
    box-shadow: var(--shadow-md);
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  .iconfont {
    font-size: 20px;
  }
}

/* ==================== 快捷导航 ==================== */
.quick-actions-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-5);
}

.quick-action-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: var(--space-6) var(--space-4);
  cursor: pointer;
  gap: var(--space-3);
}

.quick-action-icon-wrapper {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
}

.quick-action-card:hover .quick-action-icon-wrapper {
  transform: scale(1.1) rotate(-5deg);
}

.quick-action-icon {
  font-size: 28px;
}

.quick-action-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.quick-action-desc {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* ==================== 趋势图 ==================== */
.trend-section {
  display: flex;
  flex-direction: column;
  padding: var(--space-6);
}

.trend-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-5);
  padding-bottom: var(--space-4);
  border-bottom: 1px solid var(--gray-40);
}

.trend-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.trend-title-icon {
  font-size: 22px;
  color: var(--primary-color);
}

.trend-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.trend-chart {
  width: 100%;
  height: 260px;
}

.trend-empty {
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ==================== 响应式 ==================== */
@media (max-width: 992px) {
  .quick-actions-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .welcome-section {
    padding: var(--space-6) var(--space-4) var(--space-4);
  }

  .welcome-title {
    font-size: 22px;
  }

  .greeting-emoji {
    font-size: 26px;
  }

  .chat-messages {
    padding: var(--space-3) var(--space-4);
    min-height: 220px;
    max-height: 320px;
  }

  .message-bubble {
    max-width: 82%;
  }

  .quick-actions-row {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-3);
  }

  .quick-action-card {
    padding: var(--space-5) var(--space-3);
  }

  .trend-section {
    padding: var(--space-4);
  }

  .trend-chart {
    height: 220px;
  }
}

@media (max-width: 480px) {
  .quick-actions-row {
    grid-template-columns: 1fr 1fr;
  }

  .welcome-title {
    font-size: 20px;
  }

  .chat-input-area {
    padding: var(--space-3) var(--space-4) var(--space-4);
  }

  .chat-quick-actions {
    padding: 0 var(--space-4) var(--space-3);
  }
}

/* ==================== 向导面板 ==================== */
.chat-wizard-panel {
  background: var(--glass-bg);
  border-bottom: 1px solid var(--border-color);
  padding: var(--space-4) var(--space-5);
  animation: wizard-slide-down 0.3s ease-out;
}

@keyframes wizard-slide-down {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.wizard-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}

.wizard-panel-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);

  i {
    font-size: 18px;
    color: var(--primary-color);
  }
}

.wizard-panel-close {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: var(--input-bg);
  color: var(--text-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--danger-light);
    color: var(--danger-color);
  }

  i {
    font-size: 14px;
  }
}

.wizard-history-summary {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--primary-10);
  border-radius: var(--radius-sm);
  font-size: 13px;
  color: var(--text-secondary);

  i {
    font-size: 14px;
    color: var(--primary-color);
  }

  .wizard-history-name {
    font-weight: 600;
    color: var(--text-primary);
  }
}

/* ==================== 会话列表 Drawer ==================== */
.chat-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.chat-session-name {
  flex: 1;
  text-align: center;
  font-size: 18px;
  font-weight: 500;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0 var(--space-3);
}

.chat-header-actions {
  display: flex;
  align-items: center;
  justify-content: end;
  gap: var(--space-2);
  margin-left: auto;
  min-width: 144px;
}

.header-action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--input-bg);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--primary-light);
    color: var(--primary-dark);
  }

  .iconfont {
    font-size: 18px;
  }
}

</style>
