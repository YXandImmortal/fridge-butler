<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="visible" class="insufficient-data-overlay" @click.self="handleOverlayClick">
        <div class="insufficient-data-dialog">
          <!-- 头部 -->
          <div class="dialog-header">
            <div class="dialog-title-container">
              <i class="iconfont icon-chart dialog-icon"/>
              <h3 class="dialog-title">{{ title }}</h3>
            </div>
            <i v-if="showClose" class="iconfont icon-close dialog-close" @click="handleClose"/>
          </div>

          <!-- 内容区 -->
          <div class="dialog-content">
            <p class="reason-text">{{ message }}</p>

            <div class="data-requirements">
              <h4 class="requirements-title">
                <i class="iconfont icon-info-box"/>
                AI 推荐需要以下数据
              </h4>
              <ul class="requirements-list">
                <li>
                  <i class="iconfont icon-check requirements-check"/>
                  <span>库存记录 ≥ 3 条</span>
                </li>
                <li>
                  <i class="iconfont icon-check requirements-check"/>
                  <span>近 30 天入库记录 ≥ 3 条</span>
                </li>
                <li>
                  <i class="iconfont icon-check requirements-check"/>
                  <span>近 30 天取出记录 ≥ 3 条</span>
                </li>
              </ul>
            </div>

            <p class="guidance-text">
              你可以先手动创建采购计划，或继续在冰箱中录入物品及出入库记录。
            </p>
          </div>

          <!-- 底部 -->
          <div class="dialog-footer">
            <CustomButton v-if="showManualCreate" @click="handleManualCreate">
              手动创建计划
            </CustomButton>
            <CustomButton type="primary" @click="handleConfirm">
              {{ confirmText }}
            </CustomButton>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import CustomButton from './CustomButton.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '数据不足'
  },
  message: {
    type: String,
    default: ''
  },
  confirmText: {
    type: String,
    default: '我知道了'
  },
  showClose: {
    type: Boolean,
    default: true
  },
  showManualCreate: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'manual-create'])

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const handleConfirm = () => {
  emit('confirm')
  handleClose()
}

const handleManualCreate = () => {
  emit('manual-create')
}
</script>

<style scoped lang="scss">
.insufficient-data-overlay {
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

.insufficient-data-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 460px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6) 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-2) var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--warn-color);
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
  padding: var(--space-5) var(--space-6);
}

.reason-text {
  font-size: 15px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 var(--space-4);
}

.data-requirements {
  background: var(--primary-light);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  padding: var(--space-4) var(--space-5);
  margin-bottom: var(--space-4);
}

.requirements-title {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 var(--space-3);

  .iconfont {
    color: var(--primary-color);
    font-size: 16px;
  }
}

.requirements-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.requirements-list li {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  font-size: 14px;
  color: var(--text-secondary);
}

.requirements-check {
  font-size: 14px;
  color: var(--success-color);
}

.guidance-text {
  font-size: 13px;
  color: var(--text-tertiary);
  line-height: 1.5;
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
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

.dialog-fade-enter-from {
  opacity: 0;
}

.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .insufficient-data-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5) 0;
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: var(--space-4) var(--space-5);
  }

  .data-requirements {
    padding: var(--space-3) var(--space-4);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
