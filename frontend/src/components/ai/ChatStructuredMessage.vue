<template>
  <div class="structured-message">
    <!-- 文本内容（Markdown 渲染） -->
    <div v-if="msg.content" class="message-content">
      <AiMessageContent
        :content="msg.content"
        :is-streaming="isStreaming"
      />
    </div>

    <!-- AI 打字中 -->
    <div v-if="isTypingIndicator" class="typing-indicator">
      <span />
      <span />
      <span />
    </div>

    <!-- 用户附件标签 -->
    <div v-if="msg.role === 'user' && msg.attachments && msg.attachments.length > 0" class="message-attachments">
      <span
        v-for="att in msg.attachments"
        :key="att.type + '-' + att.id"
        class="message-attach-tag"
        @click="$router.push(att.type === 'fridge' ? '/fridge/detail/' + att.id : '/fridge/items/' + att.fridgeId)"
      >
        <i class="iconfont" :class="att.type === 'fridge' ? 'icon-fridge-line' : 'icon-inbox'" />
        {{ att.name }}
      </span>
    </div>

    <!-- 结构化数据渲染 -->
    <div v-if="msg.messageType === 'fridge_list' && msg.data" class="struct-content">
      <div class="fridge-list-inline">
        <div
          v-for="fridge in msg.data.fridges"
          :key="fridge.id"
          class="fridge-mini-card"
          @click="$router.push('/fridge/detail/' + fridge.id)"
        >
          <i class="iconfont icon-fridge-line fridge-mini-icon" />
          <div class="fridge-mini-name">{{ fridge.name }}</div>
          <div class="fridge-mini-meta">
            {{ fridge.itemCount }} 件物品
            <span v-if="fridge.isDefault" class="default-badge">默认</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="msg.messageType === 'item_list' && msg.data" class="struct-content">
      <div class="item-list-inline">
        <div
          v-for="item in msg.data.items"
          :key="item.id"
          class="item-mini-card"
        >
          <div class="item-mini-header">
            <span class="item-mini-name">{{ item.name }}</span>
            <el-tag :type="item.freshnessType || 'info'" size="small" class="freshness-tag">
              {{ item.freshnessLabel || '-' }}
            </el-tag>
          </div>
          <div class="item-mini-meta">
            {{ item.num }}{{ item.unit }} · {{ item.fridgeName }}
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="msg.messageType === 'expiring_alert' && msg.data" class="struct-content">
      <div class="expiring-list-inline">
        <div
          v-for="item in msg.data.items"
          :key="item.id"
          class="expiring-mini-card"
          :class="'expiring-' + item.freshnessType"
        >
          <div class="expiring-mini-header">
            <span class="expiring-mini-name">{{ item.name }}</span>
            <span class="expiring-mini-days">
              {{ item.remainingDays >= 0 ? '剩' + item.remainingDays + '天' : '已过期' + Math.abs(item.remainingDays) + '天' }}
            </span>
          </div>
          <div class="expiring-mini-meta">
            {{ item.num }}{{ item.unit }} · {{ item.fridgeName }}
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="msg.messageType === 'recipe_recommend' && msg.data" class="struct-content">
      <div class="recipe-list-inline">
        <div
          v-for="recipe in msg.data.recipes"
          :key="recipe.name"
          class="recipe-mini-card"
        >
          <div class="recipe-mini-name">{{ recipe.name }}</div>
          <div class="recipe-mini-desc">{{ recipe.description }}</div>
          <div class="recipe-mini-meta">
            <span class="recipe-difficulty">{{ recipe.difficulty }}</span>
            <span class="recipe-time">{{ recipe.cookTime }}</span>
          </div>
          <div class="recipe-items">
            <el-tag v-for="m in recipe.matchedItems" :key="m" type="success" size="small" class="recipe-tag">
              {{ m }}
            </el-tag>
            <el-tag v-for="m in recipe.missingItems" :key="m" type="info" size="small" class="recipe-tag">
              缺{{ m }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="msg.messageType === 'trend_chart' && msg.data" class="struct-content">
      <v-chart class="chat-mini-chart" :option="buildChatChartOption(msg.data)" autoresize />
    </div>

    <div v-else-if="msg.messageType === 'action_confirm' && msg.data" class="struct-content">
      <div class="action-confirm-btns">
        <button class="confirm-btn cancel" @click="$emit('action-cancel', msg)">取消</button>
        <button class="confirm-btn confirm" @click="$emit('action-confirm', msg)">确认</button>
      </div>
    </div>

    <div v-else-if="msg.messageType === 'fridge_creation_wizard' && msg.data" class="struct-content">
      <div class="wizard-history-summary">
        <i class="iconfont icon-fridge-line" />
        <span>AI 正在引导您创建冰箱</span>
        <span v-if="msg.data.formData?.name" class="wizard-history-name">「{{ msg.data.formData.name }}」</span>
      </div>
    </div>

    <div v-if="msg.content || msg.data || msg.role === 'user'" class="message-time">{{ msg.time }}</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import AiMessageContent from './AiMessageContent.vue'
import { use, graphic } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { getChartThemeColors } from '@/utils/data-analysis'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const props = defineProps({
  msg: {
    type: Object,
    required: true
  },
  isLast: {
    type: Boolean,
    default: false
  },
  aiTyping: {
    type: Boolean,
    default: false
  }
})

defineEmits(['action-confirm', 'action-cancel'])

const router = useRouter()
const themeStore = useThemeStore()

const isStreaming = computed(() => {
  return props.aiTyping && props.msg.role === 'assistant' && props.isLast
})

const isTypingIndicator = computed(() => {
  return props.aiTyping && props.msg.role === 'assistant' && !props.msg.content && !props.msg.data && props.isLast
})

function buildChatChartOption(chartData) {
  const colors = getChartThemeColors(themeStore.theme === 'dark')
  const series = (chartData.series || []).map(s => ({
    name: s.name,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 5,
    showSymbol: false,
    lineStyle: { width: 2.5, color: s.color },
    areaStyle: {
      color: new graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: hexToRgba(s.color, 0.25) },
        { offset: 1, color: hexToRgba(s.color, 0.02) }
      ])
    },
    itemStyle: { color: s.color, borderColor: colors.tooltipBg, borderWidth: 2 },
    data: s.counts || []
  }))

  return {
    color: series.map(s => s.lineStyle.color),
    tooltip: {
      trigger: 'axis',
      backgroundColor: colors.tooltipBg,
      borderColor: colors.tooltipBorder,
      textStyle: { color: colors.textColor }
    },
    legend: {
      data: series.map(s => s.name),
      top: '2%',
      textStyle: { color: colors.subTextColor, fontSize: 11 },
      itemWidth: 10,
      itemHeight: 6
    },
    grid: {
      left: '2%',
      right: '4%',
      bottom: '2%',
      top: '22%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: chartData.dates || [],
      axisLine: { lineStyle: { color: colors.axisLineColor } },
      axisLabel: { color: colors.subTextColor, fontSize: 10 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: colors.splitLineColor, type: 'dashed' } },
      axisLabel: { color: colors.subTextColor, fontSize: 10 }
    },
    series
  }
}

function hexToRgba(hex, alpha) {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r},${g},${b},${alpha})`
}
</script>

<style scoped lang="scss">
.message-content {
  white-space: normal;
}

.typing-indicator {
  display: flex;
  gap: 4px;
  align-items: center;
}

.typing-indicator span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-tertiary);
  animation: typing-bounce 1.4s ease-in-out infinite both;
}

.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.message-time {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 4px;
  text-align: right;
}

.message-user {
  .message-time {
    color: var(--color-primary-50)
  }
}

/* 用户附件标签 */
.message-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: var(--space-2);
  padding-top: var(--space-2);
  border-top: 1px dashed rgba(255, 255, 255, 0.3);
}

.message-attach-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.95);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.35);
  }

  .iconfont {
    font-size: 11px;
  }
}

/* ==================== 结构化消息样式 ==================== */
.struct-content {
  margin-top: var(--space-3);
}

/* 冰箱列表卡片 */
.fridge-list-inline {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.fridge-mini-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--space-3);
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  min-width: 100px;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: var(--shadow-sm);
    transform: translateY(-2px);
  }
}

.fridge-mini-icon {
  font-size: 24px;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.fridge-mini-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.fridge-mini-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

.default-badge {
  display: inline-block;
  padding: 0 4px;
  border-radius: 4px;
  background: var(--primary-light);
  color: var(--primary-dark);
  font-size: 10px;
  margin-left: 4px;
}

/* 物品列表卡片 */
.item-list-inline {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.item-mini-card {
  padding: var(--space-2) var(--space-3);
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.item-mini-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.item-mini-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.freshness-tag {
  flex-shrink: 0;
}

.item-mini-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 临期提醒卡片 */
.expiring-list-inline {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.expiring-mini-card {
  padding: var(--space-2) var(--space-3);
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--text-tertiary);
}

.expiring-mini-card.expiring-warning {
  border-left-color: var(--warn-color);
  background: rgba(255, 183, 77, 0.08);
}

.expiring-mini-card.expiring-danger {
  border-left-color: var(--danger-color);
  background: rgba(248, 113, 113, 0.08);
}

.expiring-mini-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-2);
}

.expiring-mini-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.expiring-mini-days {
  font-size: 11px;
  font-weight: 600;
  color: var(--danger-color);
  flex-shrink: 0;
}

.expiring-mini-meta {
  font-size: 11px;
  color: var(--text-tertiary);
  margin-top: 2px;
}

/* 菜谱卡片 */
.recipe-list-inline {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.recipe-mini-card {
  padding: var(--space-3);
  background: var(--glass-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
}

.recipe-mini-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 2px;
}

.recipe-mini-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.recipe-mini-meta {
  display: flex;
  gap: var(--space-3);
  font-size: 11px;
  color: var(--text-tertiary);
  margin-bottom: var(--space-2);
}

.recipe-difficulty {
  color: var(--primary-color);
}

.recipe-items {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.recipe-tag {
  font-size: 11px;
}

/* 聊天内嵌图表 */
.chat-mini-chart {
  width: 100%;
  height: 180px;
}

/* 操作确认按钮 */
.action-confirm-btns {
  display: flex;
  gap: var(--space-3);
  margin-top: var(--space-3);
}

.confirm-btn {
  flex: 1;
  padding: 8px 16px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;

  &.cancel {
    background: var(--glass-bg);
    color: var(--text-secondary);

    &:hover {
      background: var(--input-bg);
    }
  }

  &.confirm {
    background: var(--danger-color);
    color: white;
    border-color: var(--danger-color);

    &:hover {
      background: var(--danger-dark);
    }
  }
}

/* 向导历史摘要 */
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
</style>
