<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="confirm-dialog-overlay" @click.self="handleOverlayClick">
      <div class="confirm-dialog" :style="{ maxWidth: width }">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-alert dialog-icon"></i>
            <h3 class="dialog-title">{{ title }}</h3>
          </div>
          <i v-if="showClose" class="iconfont icon-close dialog-close" @click="handleCancel"></i>
        </div>
        <div class="dialog-content">
          <p>{{ message }}</p>
          <div v-if="type === 'select'" class="dialog-select-wrapper">
            <CustomSelect
                :model-value="selectValue"
                :placeholder="selectPlaceholder"
                style="width: 100%"
                :options="mappedOptions"
                @update:model-value="handleSelectChange"
                empty-text="未找到可用冰箱"
            />
          </div>
          <slot/>
        </div>
        <div class="dialog-footer">
          <CustomButton v-if="showCancel" type="danger" @click="handleCancel">{{ cancelText }}</CustomButton>
          <CustomButton type="primary" @click="handleConfirm">{{ confirmText }}</CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed} from 'vue'
import CustomSelect from './CustomSelect.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '确认操作'
  },
  message: {
    type: String,
    default: '您确定要执行此操作吗？'
  },
  confirmText: {
    type: String,
    default: '确认'
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  persistent: {
    type: Boolean,
    default: false
  },
  showClose: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: true
  },
  width: {
    type: String,
    default: '300px'
  },
  type: {
    type: String,
    default: 'confirm'
  },
  selectValue: {
    type: [String, Number],
    default: null
  },
  options: {
    type: Array,
    default: () => []
  },
  optionLabel: {
    type: String,
    default: 'label'
  },
  optionValue: {
    type: String,
    default: 'value'
  },
  selectPlaceholder: {
    type: String,
    default: '请选择'
  },
  selectLoading: {
    type: Boolean,
    default: false
  },
  selectClearable: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'update:selectValue'])

const handleConfirm = () => {
  emit('confirm')
  if (props.type !== 'select') {
    emit('update:visible', false)
  }
}

const mappedOptions = computed(() => {
  return props.options.map(item => ({
    label: item[props.optionLabel],
    value: item[props.optionValue]
  }))
})

const handleSelectChange = (val) => {
  emit('update:selectValue', val)
}

const handleOverlayClick = () => {
  if (props.persistent) {
    return
  }
  handleCancel()
}

const handleCancel = () => {
  if (props.persistent) {
    emit('cancel')
    return
  }
  emit('cancel')
  emit('update:visible', false)
}
</script>

<style scoped lang="scss">
.confirm-dialog-overlay {
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

.confirm-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 500px;
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
  padding: var(--space-6);
}

.dialog-content p {
  margin: 0;
  font-size: 18px;
  color: var(--text-secondary);
  line-height: 1.5;
  text-align: center;
}

.dialog-select-wrapper {
  margin-top: var(--space-4);
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
  .confirm-dialog {
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
    padding: var(--space-5);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>