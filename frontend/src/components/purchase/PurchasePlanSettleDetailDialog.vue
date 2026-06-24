<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="settle-detail-dialog-overlay" @click.self="handleClose">
      <div class="settle-detail-dialog">
        <!-- 头部 -->
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-list dialog-icon"/>
            <h3 class="dialog-title">结算明细</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>

        <!-- 内容 -->
        <div class="dialog-content">
          <template v-if="plan">
            <h4 class="plan-name">{{ plan.planName }}</h4>

            <div class="settle-stats">
              <span class="stat-stored">入库 {{ storedCount }}</span>
              <span class="stat-not-stored">不入库 {{ notStoredCount }}</span>
              <span class="stat-skipped">跳过 {{ skippedCount }}</span>
            </div>

            <ul class="settle-items">
              <li v-for="it in plan.items" :key="it.id" class="settle-item">
                <span class="item-name">{{ it.itemName }}</span>
                <span class="item-status" :class="statusClass(it.status)">
                  {{ statusText(it.status) }}
                </span>
                <span v-if="it.actualNum" class="item-actual">
                  {{ formatDecimal(it.actualNum) }} {{ it.itemUnitName || '' }}
                </span>
              </li>
            </ul>
          </template>
        </div>

        <!-- 底部 -->
        <div class="dialog-footer">
          <CustomButton type="primary" @click="handleClose">关闭</CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  plan: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible'])

const formatDecimal = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const num = parseFloat(value)
  if (Number.isNaN(num)) return value
  return Number(num.toFixed(2))
}

const statusText = (status) => {
  const map = {1: '待采购', 2: '已入库', 3: '跳过', 4: '已采购不入库'}
  return map[status] || '未知'
}

const statusClass = (status) => {
  const map = {1: 'status-pending', 2: 'status-stored', 3: 'status-skipped', 4: 'status-not-stored'}
  return map[status] || ''
}

const storedCount = computed(() => props.plan?.items?.filter(it => it.status === 2).length || 0)
const notStoredCount = computed(() => props.plan?.items?.filter(it => it.status === 4).length || 0)
const skippedCount = computed(() => props.plan?.items?.filter(it => it.status === 3).length || 0)

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}
</script>

<style scoped lang="scss">
.settle-detail-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.settle-detail-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 600px;
  width: 90%;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
    border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
  flex-shrink: 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-2) var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-close {
  font-size: var(--space-5);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-close:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.dialog-content {
  padding: 0 var(--space-6);
  overflow-y: auto;
  flex: 1;
  min-height: 0;
}

.plan-name {
  margin: 0 0 var(--space-4);
  font-size: 18px;
  color: var(--text-primary);
}

.settle-stats {
  display: flex;
  gap: var(--space-4);
  font-size: 14px;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px dashed var(--border-color);
}

.settle-items {
  list-style: none;
  margin: 0;
  padding: 0;
}

.settle-item {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  border-bottom: 1px solid var(--border-color);
  font-size: 14px;

  &:last-child {
    border-bottom: none;
  }
}

.item-name {
  flex: 1;
  font-weight: 500;
  color: var(--text-primary);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-status {
  padding: 1px 6px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
  flex-shrink: 0;
}

.item-actual {
  color: var(--text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}

.status-pending {
  background: var(--warning-light);
  color: var(--warning-color);
}

.status-stored {
  background: var(--success-light);
  color: var(--success-color);
}

.status-skipped {
  background: var(--warning-light);
  color: var(--warning-color);
}

.status-not-stored {
  background: var(--info-light);
  color: var(--info-color);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: var(--space-6);
  flex-shrink: 0;
}

@keyframes dialog-slide-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .settle-detail-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: 0 var(--space-5) var(--space-4);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
