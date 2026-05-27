<template>
  <button
      :type="nativeType"
      :class="buttonClasses"
      :disabled="disabled || loading"
      @click="handleClick"
  >
    <i v-if="loading" class="iconfont icon-loader custom-button__loading-icon"></i>
    <span class="custom-button__content">
      <slot>{{ loading ? loadingText : '' }}</slot>
    </span>
  </button>
</template>

<script setup>
import {computed} from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'primary', 'danger', 'link', 'search-reset'].includes(val)
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  loading: {
    type: Boolean,
    default: false
  },
  loadingText: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  nativeType: {
    type: String,
    default: 'button',
    validator: (val) => ['button', 'submit', 'reset'].includes(val)
  }
})

const emit = defineEmits(['click'])

const buttonClasses = computed(() => {
  const classes = ['custom-button']
  classes.push(`custom-button--${props.type}`)
  if (props.size !== 'default') {
    classes.push(`custom-button--${props.size}`)
  }
  if (props.loading) {
    classes.push('is-loading')
  }
  if (props.disabled) {
    classes.push('is-disabled')
  }
  return classes
})

const handleClick = (event) => {
  if (props.loading || props.disabled) return
  emit('click', event)
}
</script>

<style scoped lang="scss">
.custom-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border-radius: var(--radius-md);
  padding: 10px 20px;
  font-weight: 200;
  font-size: 14px;
  line-height: 1;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.3s ease,
  box-shadow 0.3s ease, border-color 0.3s ease,
  filter 0.3s ease, opacity 0.3s ease;
  border: 1px solid transparent;
  outline: none;
  font-family: var(--el-font-family);
  white-space: nowrap;
  user-select: none;
}

.custom-button--default {
  background: transparent;
  color: var(--text-primary);
  border-color: var(--gray-40);
}

.custom-button--default:hover:not(:disabled) {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--gray-40);
}

.custom-button--primary {
  background: var(--primary-color);
  color: var(--text-inverse);
  border: none;
}

.custom-button--primary:hover:not(:disabled) {
  background: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--primary-40);
}

.custom-button--danger {
  background: var(--danger-color);
  color: var(--text-inverse);
  border: none;
}

.custom-button--danger:hover:not(:disabled) {
  filter: brightness(0.95);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--danger-40);
}

.custom-button--link {
  background: transparent;
  color: var(--text-secondary);
  border: none;
  padding: 4px 8px;
}

.custom-button--link:hover:not(:disabled) {
  color: var(--primary-color);
  transform: translateY(-1px);
}

.custom-button--search-reset {
  background: transparent;
  color: var(--text-primary);
  border-color: var(--gray-40);
  padding: 12px 20px;
}

.custom-button--search-reset:hover:not(:disabled) {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: var(--primary-dark);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px var(--gray-40);
}

.custom-button--large {
  padding: 14px 20px;
  font-size: 16px;
}

.custom-button--small {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: var(--radius-sm);
}

.custom-button.is-loading {
  cursor: default;
  opacity: 0.85;
}

.custom-button.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.custom-button__loading-icon {
  font-size: 1em;
  animation: spin 1.5s linear infinite;
}

.custom-button__content {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
