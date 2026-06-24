<template>
  <div v-loading="pageLoading" class="user-index-container">
    <!-- 系统公告 -->
    <section v-if="announcement" class="announcement-bar animate-in" style="animation-delay: 0.05s">
      <div class="announcement-content">
        <i class="iconfont icon-megaphone announcement-icon"/>
        <span class="announcement-text" v-html="announcement"/>
      </div>
    </section>

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
          <Logo class="ai-logo"/>
          <div class="chat-title-info">
            <h3 class="chat-title">AI 冰箱管家</h3>
            <span class="chat-status">
              <span class="status-dot"/>
              在线
            </span>
          </div>
        </div>
        <div class="chat-session-name">{{ currentSessionName }}</div>

        <div class="chat-header-actions">
          <button class="header-action-btn" title="新建会话" @click="createNewSession">
            <i class="iconfont icon-add-box"/>
          </button>
          <button class="header-action-btn" title="会话列表" @click="drawerVisible = true">
            <i class="iconfont icon-list"/>
          </button>
        </div>
      </div>

      <!-- 顶部向导面板（独立于消息列表） -->
      <transition name="wizard-panel">
        <div v-if="activeWizard && activeWizard.type === 'fridge_creation'" class="chat-wizard-panel">
          <div class="wizard-panel-header">
            <div class="wizard-panel-title">
              <i class="iconfont icon-fridge-line"/>
              <span>创建冰箱向导</span>
            </div>
            <button class="wizard-panel-close" title="关闭向导" @click="handleWizardCancel">
              <i class="iconfont icon-close"/>
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

      <transition name="wizard-panel">
        <div v-if="activeWizard && activeWizard.type === 'item_creation'" class="chat-wizard-panel">
          <div class="wizard-panel-header">
            <div class="wizard-panel-title">
              <i class="iconfont icon-item"/>
              <span>添加物品向导</span>
            </div>
            <button class="wizard-panel-close" title="关闭向导" @click="handleWizardCancel">
              <i class="iconfont icon-close"/>
            </button>
          </div>
          <ItemCreationWizard
              :data="activeWizardData"
              :recommend-data="itemRecommendCache"
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
          <Logo v-if="msg.role === 'assistant'" class="ai-logo-sm"/>
          <div v-else class="message-avatar">
            <span class="user-avatar-text">{{ userAvatarText }}</span>
          </div>
          <div
              :class="['message-bubble', { 'typing-bubble': aiTyping && msg.role === 'assistant' && !msg.content && !msg.data && idx === messages.length - 1 }]">
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

        <AttachmentSelector v-model="attachments" :fridge-list="fridgeList" :item-list="itemList"/>
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
          <i class="iconfont icon-arrow-right-box"/>
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
          <i :class="['iconfont', action.icon, 'quick-action-icon']" :style="{ color: action.iconColor }"/>
        </div>
        <div class="quick-action-name">{{ action.name }}</div>
        <div class="quick-action-desc">{{ action.desc }}</div>
      </div>
    </section>

    <!-- 数据趋势 -->
    <section class="trend-section glass-card animate-in" style="animation-delay: 1.04s">
      <div class="trend-header">
        <div class="trend-title-wrapper">
          <i class="iconfont icon-trending trend-title-icon"/>
          <h3 class="trend-title">近7天取出/入库趋势</h3>
        </div>
      </div>
      <v-chart v-if="trendHasData" class="trend-chart" :option="trendOption" autoresize/>
      <el-empty v-else description="近7天暂无数据" class="trend-empty"/>
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
    <!-- 选择冰箱对话框（AI物品向导前置检查） -->
    <ConfirmDialog
        v-model:visible="showSelectFridgeDialog"
        v-model:select-value="selectedFridgeId"
        title="选择冰箱"
        message="请选择一个冰箱来添加物品："
        confirm-text="确定"
        cancel-text="取消"
        type="select"
        :persistent="true"
        :show-close="false"
        width="420px"
        :options="fridgeList"
        option-label="fridgeName"
        option-value="id"
        select-placeholder="请选择冰箱"
        :select-loading="fridgeListLoading"
        @confirm="handleSelectFridgeConfirm"
        @cancel="handleSelectFridgeCancel"
    />
    <UserIndexTour ref="tourRef"/>
  </div>
</template>

<script setup>
import UserIndexTour from '@/components/tour/UserIndexTour.vue'
import {useTourStore, TOUR_SCENES} from '@/stores/tour'
import {ref, computed, onMounted, onUnmounted, nextTick, watch} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {useThemeStore} from '@/stores/theme'
import {useAiChatStore} from '@/stores/aiChat'
import {useGamificationStore} from '@/stores/gamification'

import Logo from '@/components/brand/Logo.vue'
import FridgeCreationWizard from '@/components/ai/FridgeCreationWizard.vue'
import ItemCreationWizard from '@/components/ai/ItemCreationWizard.vue'
import StatsOverview from '@/components/ai/StatsOverview.vue'
import ChatStructuredMessage from '@/components/ai/ChatStructuredMessage.vue'
import AttachmentSelector from '@/components/ai/AttachmentSelector.vue'
import ChatSessionDrawer from '@/components/ai/ChatSessionDrawer.vue'
import {getFridgeTypeById} from '@/utils/fridgeTypeMap.js'
import {listMyFridges} from '@/api/fridge'
import showMessage from '@/utils/message'
import notifyGamificationResult, {notifyGamificationReward, consumePendingRewards} from '@/utils/gamificationNotify'
import {searchItems, getRecent30DaysTakeOutStats, getRecent30DaysAddStats, getExpiringSummary, recommendItem} from '@/api/item'
import {getPublicConfig} from '@/api/system'
import {use, graphic} from 'echarts/core'
import {CanvasRenderer} from 'echarts/renderers'
import {LineChart} from 'echarts/charts'
import {GridComponent, TooltipComponent, LegendComponent} from 'echarts/components'
import VChart from 'vue-echarts'
import {getChartThemeColors} from '@/utils/data-analysis'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const aiChatStore = useAiChatStore()
const gamificationStore = useGamificationStore()

// ==================== 数据状态 ====================
const pageLoading = ref(false)
const announcement = ref('')
const fridgeList = ref([])
const itemList = ref([])
const takeOutList = ref([])
const addList = ref([])
const expiringSummary = ref({expiringCount: 0, expiredCount: 0, totalExpiring: 0})

// AI 聊天页面级 UI 状态
const drawerVisible = ref(false)
const showSelectFridgeDialog = ref(false)
const selectedFridgeId = ref(null)
const fridgeListLoading = ref(false)

// 物品创建向导的智能推荐缓存
const itemRecommendCache = ref(null)

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

const currentSessionName = computed(() => aiChatStore.currentSessionName)

// ==================== AI 聊天 ====================
const messages = computed(() => aiChatStore.messages)
const aiTyping = computed(() => aiChatStore.aiTyping)
const suggestions = computed(() => aiChatStore.suggestions)
const sessionId = computed(() => aiChatStore.sessionId)
const sessions = computed(() => aiChatStore.sessions)
const sessionLoading = computed(() => aiChatStore.sessionLoading)
const activeWizard = computed(() => aiChatStore.activeWizard)
const activeWizardData = computed(() => aiChatStore.activeWizardData)
const isWizardConfirmStep = computed(() => aiChatStore.isWizardConfirmStep)
const pendingWizardData = computed(() => aiChatStore.pendingWizardData)

const inputMessage = ref('')
const chatMessagesRef = ref(null)

// 附件状态
const attachments = ref([])

const defaultQuickActions = [
  {text: '查看冰箱'},
  {text: '有什么食材'},
  {text: '临期提醒'},
  {text: '推荐菜谱'}
]

function scrollToBottom() {
  nextTick(() => {
    const el = chatMessagesRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

let scrollTimer = null
function scheduleScroll() {
  if (!scrollTimer) {
    scrollTimer = setTimeout(() => {
      scrollTimer = null
      scrollToBottom()
    }, 50)
  }
}

function sendQuickMessage(text) {
  inputMessage.value = text
  sendMessage()
}

async function sendMessage() {
  const text = inputMessage.value.trim()
  if (!text || aiTyping.value) return
  const currentAttachments = attachments.value.map(a => ({...a}))
  inputMessage.value = ''
  attachments.value = []

  const wizardContext = activeWizard.value
      ? {
        type: activeWizard.value.type,
        currentStep: activeWizard.value.currentStep,
        formData: activeWizard.value.formData
      }
      : undefined

  const {reward} = await aiChatStore.sendMessage({
    text,
    attachments: currentAttachments,
    fridgeId: route.query.fridgeId,
    wizardContext
  })

  // 处理本次 AI 对话的 EXP/徽章/等级提升奖励
  if (reward) {
    await notifyGamificationReward(reward, '与 AI 对话')
  }
}

// ==================== action_confirm 处理 ====================
async function handleActionConfirm(msg) {
  const actionData = msg.data
  if (!actionData) return

  aiChatStore.updateMessageById(msg.id, {
    confirmed: true,
    content: `已确认：${actionData.targetName || '操作'}正在执行...`,
    messageType: 'text',
    data: null
  })

  try {
    switch (actionData.action) {
      case 'delete_fridge': {
        const {deleteFridge} = await import('@/api/fridge')
        const res = await deleteFridge(actionData.targetId)
        if (res.code === 200) {
          aiChatStore.updateMessageById(msg.id, {
            content: `✅ 已删除「${actionData.targetName}」。`
          })
          await fetchPageData()
        } else {
          aiChatStore.updateMessageById(msg.id, {
            content: `❌ 删除失败：${res.message || '未知错误'}`
          })
        }
        break
      }
      default:
        aiChatStore.updateMessageById(msg.id, {
          content: `✅ 已确认执行「${actionData.action}」。`
        })
    }
  } catch (err) {
    console.error('操作执行失败:', err)
    aiChatStore.updateMessageById(msg.id, {
      content: '❌ 操作执行失败，请稍后重试。'
    })
  }

  scrollToBottom()
}

function handleActionCancel(msg) {
  aiChatStore.updateMessageById(msg.id, {
    confirmed: false,
    content: '已取消操作。',
    messageType: 'text',
    data: null
  })
  scrollToBottom()
}

// ==================== wizard 处理（冰箱 + 物品）====================
async function handleWizardStepSubmit({field, value, formData}) {
  // 物品向导第一步输入名称后，请求智能推荐并暂存
  if (field === 'itemName' && aiChatStore.activeWizard?.type === 'item_creation') {
    try {
      const recRes = await recommendItem(String(value || '').trim(), Number(route.query.fridgeId) || undefined)
      if (recRes.code === 200 && recRes.data?.valid) {
        itemRecommendCache.value = recRes.data
      } else {
        itemRecommendCache.value = null
        if (recRes.data?.message) {
          showMessage.warning(recRes.data.message)
        }
      }
    } catch (error) {
      console.error('智能推荐失败:', error)
      itemRecommendCache.value = null
    }
  }

  const wizardType = aiChatStore.activeWizard?.type || 'fridge_creation'
  // 只更新 formData，不乐观更新 currentStep，避免后端根据 currentStep 推断下一步时跳过步骤
  aiChatStore.activeWizard = {
    ...(aiChatStore.activeWizard || {}),
    type: wizardType,
    formData
  }

  let messageText
  if (wizardType === 'fridge_creation') {
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
  } else if (wizardType === 'item_creation') {
    if (field === 'categoryId') {
      messageText = value ? `分类：${value}` : '跳过分类'
    } else if (field === 'itemUnitId') {
      messageText = value ? `数量${formData.itemNum || 1}，单位已选` : '跳过单位'
    } else if (field === 'productionDate') {
      const pd = formData.productionDate
      const sl = formData.shelfLifeDays
      if (pd && sl) {
        messageText = `生产日期：${pd}，保质期：${sl}天`
      } else if (pd) {
        messageText = `生产日期：${pd}`
      } else if (sl) {
        messageText = `保质期：${sl}天`
      } else {
        messageText = '跳过生产日期和保质期'
      }
    } else if (field === 'remark') {
      messageText = value ? `备注：${value}` : '跳过备注'
    } else if (field === 'storageLocation') {
      messageText = value ? `存放位置：${value}` : '跳过存放位置'
    } else {
      messageText = String(value || '')
    }
  }

  const wizardContext = aiChatStore.activeWizard
      ? {
        type: aiChatStore.activeWizard.type,
        currentStep: aiChatStore.activeWizard.currentStep,
        formData
      }
      : undefined

  const {reward} = await aiChatStore.sendMessage({
    text: messageText,
    attachments: [],
    fridgeId: route.query.fridgeId,
    wizardContext
  })

  // 处理本次 wizard 步骤的 EXP/徽章/等级提升奖励
  if (reward) {
    await notifyGamificationReward(reward, '与 AI 对话')
  }
}

async function handleWizardSkip({field, formData, messageText: customText}) {
  const wizardType = aiChatStore.activeWizard?.type || 'fridge_creation'
  aiChatStore.activeWizard = {
    ...(aiChatStore.activeWizard || {}),
    type: wizardType,
    formData
  }

  let messageText = customText
  if (!messageText) {
    if (wizardType === 'fridge_creation') {
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
    } else if (wizardType === 'item_creation') {
      if (field === 'productionDate') {
        messageText = '跳过生产日期和保质期'
      } else if (field === 'remark') {
        messageText = '跳过备注'
      } else {
        messageText = `跳过${field}`
      }
    }
  }

  const wizardContext = aiChatStore.activeWizard
      ? {
        type: aiChatStore.activeWizard.type,
        currentStep: aiChatStore.activeWizard.currentStep,
        formData
      }
      : undefined

  const {reward} = await aiChatStore.sendMessage({
    text: messageText,
    attachments: [],
    fridgeId: route.query.fridgeId,
    wizardContext
  })

  // 处理本次 wizard 跳过的 EXP/徽章/等级提升奖励
  if (reward) {
    await notifyGamificationReward(reward, '与 AI 对话')
  }
}

async function handleWizardConfirm(formData) {
  const wizardType = aiChatStore.activeWizard?.type || 'fridge_creation'

  if (wizardType === 'fridge_creation') {
    if (!formData.name || !formData.name.trim()) {
      aiChatStore.addAssistantMessage('❌ 冰箱名称不能为空，请重新输入。', 'text', null)
      scrollToBottom()
      return
    }
    aiChatStore.wizardCompleted = true
    aiChatStore.activeWizard = null
    try {
      const {createFridge} = await import('@/api/fridge')
      const res = await createFridge({
        fridgeName: formData.name,
        fridgeTypeId: formData.fridgeTypeId || undefined,
        totalCapacity: formData.totalCapacity || undefined,
        isDefault: formData.isDefault || undefined,
        fridgeAddress: formData.address || undefined,
        remark: formData.remark || undefined
      })
      if (res.code === 200) {
        aiChatStore.addAssistantMessage(`✅ 冰箱「${formData.name}」创建成功！现在你可以向里面添加食材了~`, 'text', null)
        notifyGamificationResult(res, '创建冰箱')
        await fetchPageData()
      } else {
        aiChatStore.addAssistantMessage(`❌ 创建失败：${res.message || '未知错误'}`, 'text', null)
      }
    } catch (err) {
      console.error('创建冰箱失败:', err)
      aiChatStore.addAssistantMessage('❌ 创建冰箱失败，请稍后重试。', 'text', null)
    }
  } else if (wizardType === 'item_creation') {
    // 兜底校验：确保有 fridgeId
    const currentFridgeId = route.query.fridgeId
    if (!currentFridgeId) {
      aiChatStore.addAssistantMessage('❌ 请先选择一个冰箱，才能添加物品。', 'text', null)
      scrollToBottom()
      return
    }
    if (!formData.itemName || !String(formData.itemName).trim()) {
      aiChatStore.addAssistantMessage('❌ 物品名称不能为空，请重新输入。', 'text', null)
      scrollToBottom()
      return
    }
    aiChatStore.wizardCompleted = true
    aiChatStore.activeWizard = null
    try {
      const {createItem} = await import('@/api/item')
      const res = await createItem({
        itemName: formData.itemName,
        categoryId: formData.categoryId || undefined,
        itemNum: formData.itemNum || 1,
        itemUnitId: formData.itemUnitId || undefined,
        fridgeId: Number(currentFridgeId) || undefined,
        storageLocation: formData.storageLocation || null,
        storedDate: formData.storedDate || new Date().toISOString().split('T')[0],
        productionDate: formData.productionDate || null,
        shelfLifeDays: formData.shelfLifeDays || null,
        remark: formData.remark || null
      })
      if (res.code === 200) {
        aiChatStore.addAssistantMessage(`✅ 物品「${formData.itemName}」添加成功！`, 'text', null)
        notifyGamificationResult(res, '添加食材')
        await fetchPageData()
      } else {
        aiChatStore.addAssistantMessage(`❌ 添加失败：${res.message || '未知错误'}`, 'text', null)
      }
    } catch (err) {
      console.error('添加物品失败:', err)
      aiChatStore.addAssistantMessage('❌ 添加物品失败，请稍后重试。', 'text', null)
    }
  }
  scrollToBottom()
}

function handleWizardCancel() {
  const wizardType = aiChatStore.activeWizard?.type || 'fridge_creation'
  aiChatStore.activeWizard = null
  itemRecommendCache.value = null
  const cancelText = wizardType === 'fridge_creation'
      ? '已取消创建冰箱。如需创建，请随时告诉我~'
      : '已取消添加物品。如需添加，请随时告诉我~'
  aiChatStore.addAssistantMessage(cancelText, 'text', null)
  scrollToBottom()
  itemRecommendCache.value = null
}

// ==================== 冰箱前置检查（物品向导）====================
async function checkFridgeBeforeItemWizard() {
  fridgeListLoading.value = true
  try {
    const res = await listMyFridges()
    let fridges = []
    if (res.code === 200 && Array.isArray(res.data)) {
      fridges = res.data
    }

    if (fridges.length === 0) {
      // 没有冰箱，把当前AI消息改为提示
      const lastAssistant = aiChatStore.getLastAssistantMessage()
      if (lastAssistant) {
        aiChatStore.updateMessageById(lastAssistant.id, {
          content: '我注意到您还没有创建冰箱，请先创建一个冰箱才能添加物品哦~',
          messageType: 'text',
          data: null
        })
      }
      scrollToBottom()
      aiChatStore.clearPendingWizardData()
    } else if (fridges.length === 1) {
      // 只有一个冰箱，自动选中
      const onlyFridge = fridges[0]
      await router.replace({
        query: {...route.query, fridgeId: String(onlyFridge.id)}
      })
      // 启动暂存的向导
      aiChatStore.startItemWizardFromPending()
    } else {
      // 多个冰箱，弹出选择对话框
      fridgeList.value = fridges
      selectedFridgeId.value = null
      showSelectFridgeDialog.value = true
    }
  } catch (error) {
    console.error('获取冰箱列表失败:', error)
    const lastAssistant = aiChatStore.getLastAssistantMessage()
    if (lastAssistant) {
      aiChatStore.updateMessageById(lastAssistant.id, {
        content: '获取冰箱列表失败，请稍后重试。',
        messageType: 'text',
        data: null
      })
    }
    scrollToBottom()
    aiChatStore.clearPendingWizardData()
  } finally {
    fridgeListLoading.value = false
  }
}

function handleSelectFridgeConfirm() {
  if (!selectedFridgeId.value) {
    showMessage.warning('请选择一个冰箱')
    return
  }
  showSelectFridgeDialog.value = false
  // 更新 URL query
  router.replace({
    query: {...route.query, fridgeId: String(selectedFridgeId.value)}
  })
  // 启动暂存的向导
  aiChatStore.startItemWizardFromPending()
}

function handleSelectFridgeCancel() {
  showSelectFridgeDialog.value = false
  selectedFridgeId.value = null
  aiChatStore.clearPendingWizardData()
  aiChatStore.addAssistantMessage('已取消选择冰箱，如需添加物品请重新发起对话~', 'text', null)
  scrollToBottom()
}

// ==================== 会话管理 ====================
async function loadSessions() {
  await aiChatStore.loadSessions()
}

async function switchSession(sid) {
  if (sid === sessionId.value) {
    drawerVisible.value = false
    return
  }
  await aiChatStore.switchSession(sid)
  drawerVisible.value = false
  // 切换会话时清除 fridgeId，避免历史会话受当前页面上下文影响
  if (route.query.fridgeId) {
    const query = {...route.query}
    delete query.fridgeId
    router.replace({path: '/user/index', query})
  }
  scrollToBottom()
}

function createNewSession() {
  aiChatStore.createNewSession()
  drawerVisible.value = false
  // 清除 URL 中的 fridgeId，避免新建会话后仍绑定到特定冰箱
  if (route.query.fridgeId) {
    const query = {...route.query}
    delete query.fridgeId
    router.replace({path: '/user/index', query})
  }
}

async function handleDeleteSession(sid) {
  try {
    await aiChatStore.deleteSession(sid)
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
    icon: 'icon-item',
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
      textStyle: {color: colors.textColor},
      axisPointer: {
        type: 'line',
        lineStyle: {color: colors.primaryColor, width: 1, type: 'dashed'}
      }
    },
    legend: {
      data: ['取出', '入库'],
      top: '2%',
      textStyle: {color: colors.subTextColor, fontSize: 12},
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
      axisLine: {lineStyle: {color: colors.axisLineColor}},
      axisLabel: {color: colors.subTextColor, fontSize: 11}
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: {show: false},
      splitLine: {lineStyle: {color: colors.splitLineColor, type: 'dashed'}},
      axisLabel: {color: colors.subTextColor}
    },
    series: [
      {
        name: '取出',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: {width: 3, color: takeOutColor},
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            {offset: 0, color: hexToRgba(takeOutColor, 0.3)},
            {offset: 1, color: hexToRgba(takeOutColor, 0.02)}
          ])
        },
        itemStyle: {color: takeOutColor, borderColor: colors.tooltipBg, borderWidth: 2},
        data: takeOutCounts
      },
      {
        name: '入库',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        showSymbol: false,
        lineStyle: {width: 3, color: addColor},
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            {offset: 0, color: hexToRgba(addColor, 0.3)},
            {offset: 1, color: hexToRgba(addColor, 0.02)}
          ])
        },
        itemStyle: {color: addColor, borderColor: colors.tooltipBg, borderWidth: 2},
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
        return {code: -1, data: []}
      }),
      getRecent30DaysAddStats().catch(err => {
        console.error('获取入库趋势失败:', err)
        return {code: -1, data: []}
      }),
      getExpiringSummary().catch(err => {
        console.error('获取临期汇总失败:', err)
        return {code: -1, data: null}
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
      expiringSummary.value = {expiringCount: 0, expiredCount: 0, totalExpiring: 0}
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

const fetchAnnouncement = async () => {
  try {
    const res = await getPublicConfig()
    if (res.code === 200 && res.data) {
      announcement.value = res.data.announcement || ''
    }
  } catch (error) {
    console.error('获取公告失败:', error)
  }
}

onMounted(async () => {
  fetchPageData()
  fetchAnnouncement()
  loadSessions()

  // 将登录时暂存的奖励转入 pendingRewards，由统一消费函数展示
  const pendingLoginExp = sessionStorage.getItem('pending_login_exp')
  if (pendingLoginExp) {
    try {
      const {exp, description, badges} = JSON.parse(pendingLoginExp)
      if (exp > 0) {
        gamificationStore.pushPendingReward({
          type: 'exp',
          exp,
          description: description || '每日登录',
          source: 'login'
        })
      }
      if (Array.isArray(badges) && badges.length > 0) {
        badges.forEach((badge) => {
          gamificationStore.pushPendingReward({
            type: 'badge',
            badge,
            source: 'login'
          })
        })
      }
    } catch (e) {
      console.error('解析 pending_login_exp 失败:', e)
    }
    sessionStorage.removeItem('pending_login_exp')
  }

  // 将登录时暂存的等级提升转入 pendingRewards
  const pendingLoginLevel = sessionStorage.getItem('pending_login_level')
  if (pendingLoginLevel) {
    try {
      const {leveledUp, level} = JSON.parse(pendingLoginLevel)
      if (level) {
        // 无论是否升级，都先记录当前等级，避免后续操作升级时重复弹窗
        sessionStorage.setItem('gamification_last_level', String(level.currentLevel))
        if (leveledUp) {
          gamificationStore.pushPendingReward({
            type: 'levelUp',
            levelInfo: level,
            source: 'login'
          })
        }
      }
    } catch (e) {
      console.error('解析 pending_login_level 失败:', e)
    }
    sessionStorage.removeItem('pending_login_level')
  }

  // 统一消费待展示奖励（包含登录奖励和 Header 已入队的 overview 奖励）
  await consumePendingRewards()

  // 如果本地有保存的 sessionId，自动恢复该会话的历史消息
  if (sessionId.value) {
    aiChatStore.loadSessionMessages(sessionId.value)
        .then(() => scrollToBottom())
        .catch(err => {
          console.error('恢复历史消息失败:', err)
        })
  }

  // 处理从其他页面跳转过来的 AI 快捷指令（如冰箱列表页的「AI帮我创建」、物品管理页的「AI帮我添加」）
  const aiMessage = route.query.aiMessage
  if (aiMessage && typeof aiMessage === 'string') {
    // 先保存 fridgeId，因为 createNewSession 会清除它
    const fridgeId = route.query.fridgeId

    // 保留 fridgeId query（物品向导场景需要），仅移除 aiMessage
    const query = {...route.query}
    delete query.aiMessage
    await router.replace({path: '/user/index', query})

    // "AI 帮我创建/添加"应视为新会话，避免携带旧 sessionId 导致冲突
    createNewSession()

    // 恢复 fridgeId，AI 帮我添加/创建流程需要绑定到具体冰箱
    if (fridgeId) {
      await router.replace({
        path: '/user/index',
        query: {...route.query, fridgeId: String(fridgeId)}
      })
    }

    const {reward} = await aiChatStore.sendMessage({
      text: aiMessage,
      attachments: [],
      fridgeId
    })

    // 处理本次 AI 快捷指令的 EXP/徽章/等级提升奖励
    if (reward) {
      await notifyGamificationReward(reward, '与 AI 对话')
    }
  }
})

onUnmounted(() => {
  aiChatStore.abortStream()
})

// 消息变化时自动滚动到底部（包含流式输出）
watch(() => aiChatStore.messages, () => {
  scheduleScroll()
}, {deep: true, flush: 'post'})

// 物品向导需要选择冰箱时触发前置检查
watch(() => aiChatStore.pendingWizardData, (data) => {
  if (data) {
    checkFridgeBeforeItemWizard()
  }
})

// 页面引导
const tourRef = ref(null)
const tourStore = useTourStore()

watch(() => tourStore.pendingStartScene, (scene) => {
  if (scene === TOUR_SCENES.USER_INDEX) {
    tourRef.value?.start()
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

/* ==================== 系统公告 ==================== */
.announcement-bar {
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--glass-bg) 100%);
  border: 1px solid var(--primary-30);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-5);
  backdrop-filter: blur(10px);
}

.announcement-content {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.announcement-icon {
  font-size: 18px;
  color: var(--primary-color);
  flex-shrink: 0;
}

.announcement-text {
  font-size: 14px;
  color: var(--text-primary);
  line-height: 1.6;
  word-break: break-all;
}

.announcement-text :deep(a) {
  color: var(--primary-color);
  text-decoration: none;
}

.announcement-text :deep(a:hover) {
  text-decoration: underline;
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
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.chat-messages {
  min-height: 280px;
  max-height: 420px;
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
  transition: all 0.3s ease;

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
  margin-right: 64px;
}

.chat-header-actions {
  display: flex;
  align-items: center;
  justify-content: end;
  gap: var(--space-2);
  margin-left: auto;
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
