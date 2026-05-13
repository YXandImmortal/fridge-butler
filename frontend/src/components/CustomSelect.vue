<template>
  <div class="custom-select" ref="selectRef" :class="{ 'is-open': isOpen, 'is-grid': grid, 'is-full-width': fullWidth }">
    <div class="select-trigger" :class="{ 'is-disabled': disabled }" @click="toggleOpen">
      <span class="select-label" :class="{ 'is-placeholder': !selectedLabel }">
        {{ selectedLabel || placeholder }}
      </span>
      <i
        v-if="clearable && hasValue"
        class="iconfont icon-close select-clear"
        @click.stop="handleClear"
      />
      <i v-else class="iconfont icon-chevron-down select-arrow" />
    </div>
    <transition name="dropdown">
      <div v-show="isOpen" class="select-dropdown" :class="{ 'is-grid': grid, 'align-right': dropdownAlign === 'right' }">
        <div
          v-for="option in options"
          :key="option.value"
          class="select-option"
          :class="{ 'is-selected': String(modelValue) === String(option.value) }"
          @click.stop="handleSelect(option)"
        >
          <span class="option-label">{{ option.label }}</span>
          <i v-if="!grid && String(modelValue) === String(option.value)" class="iconfont icon-check" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number, null],
    default: null
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择'
  },
  clearable: {
    type: Boolean,
    default: false
  },
  clearValue: {
    type: [String, Number, null],
    default: null
  },
  disabled: {
    type: Boolean,
    default: false
  },
  grid: {
    type: Boolean,
    default: false
  },
  dropdownAlign: {
    type: String,
    default: 'left',
    validator: (val) => ['left', 'right'].includes(val)
  },
  fullWidth: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const selectRef = ref(null)

const hasValue = computed(() => {
  return props.modelValue !== '' && props.modelValue !== null && props.modelValue !== undefined
})

const selectedLabel = computed(() => {
  const found = props.options.find(opt => String(opt.value) === String(props.modelValue))
  return found ? found.label : ''
})

const toggleOpen = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

const handleSelect = (option) => {
  emit('update:modelValue', option.value)
  emit('change', option.value)
  isOpen.value = false
}

const handleClear = () => {
  emit('update:modelValue', props.clearValue)
  emit('change', props.clearValue)
}

const handleClickOutside = (event) => {
  if (selectRef.value && !selectRef.value.contains(event.target)) {
    isOpen.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped lang="scss">
.custom-select {
  position: relative;
  width: 140px;
  height: 40px;
  font-size: 14px;
  user-select: none;
}

.custom-select.is-full-width {
  width: 100%;
}

.select-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  padding: 0 12px;
  background: var(--card-bg);
  border: 1px solid var(--gray-40);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-trigger:hover:not(.is-disabled) {
  border-color: var(--color-primary-200);
  box-shadow: 0 4px 12px var(--primary-20);
}

.select-trigger.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
  background: var(--gray-20);
}

.custom-select.is-open .select-trigger {
  border-color: var(--primary-color);
  box-shadow: 0 4px 16px var(--primary-30);
}

.select-label {
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.select-label.is-placeholder {
  color: var(--text-tertiary);
}

.select-arrow {
  font-size: 14px;
  color: var(--text-tertiary);
  transition: transform 0.3s ease;
  margin-left: 4px;
}

.custom-select.is-open .select-arrow {
  transform: rotate(180deg);
  color: var(--primary-color);
}

.select-clear {
  font-size: 12px;
  color: var(--text-tertiary);
  padding: 2px;
  border-radius: 50%;
  transition: all 0.2s ease;
  margin-left: 4px;
}

.select-clear:hover {
  background: var(--gray-40);
  color: var(--text-primary);
}

.select-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  width: 100%;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--gray-40);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 100;
  overflow: hidden;
  padding: 6px;
}

.select-dropdown.align-right {
  left: auto;
  right: 0;
}

.select-dropdown.is-grid {
  width: auto;
  min-width: max(280px, 100%);
  max-width: 360px;
  max-height: 170px;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(72px, 1fr));
  gap: 8px;
  padding: 12px;
  z-index: 100;
}

.select-dropdown.is-grid .select-option {
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 8px 4px;
  text-align: center;
  border: 1px solid var(--gray-40);
  background: var(--card-bg);
  font-size: 13px;
  min-height: 44px;
}

.select-dropdown.is-grid .select-option:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px var(--primary-20);
}

.select-dropdown.is-grid .select-option.is-selected {
  background: var(--primary-color);
  color: var(--text-inverse);
  border-color: var(--primary-color);
  font-weight: 600;
}

.select-dropdown.is-grid .option-label {
  white-space: normal;
  line-height: 1.3;
}

.select-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s ease;
  color: var(--text-secondary);
  font-size: 14px;
}

.select-option:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.select-option.is-selected {
  background: var(--primary-10);
  color: var(--primary-color);
  font-weight: 500;
}

.option-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.select-option .icon-check {
  font-size: 12px;
  flex-shrink: 0;
  margin-left: 6px;
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
