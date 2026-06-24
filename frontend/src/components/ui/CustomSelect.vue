<template>
  <div class="custom-select" ref="selectRef"
       :class="[
         { 'is-open': isOpen, 'is-grid': grid, 'is-full-width': fullWidth },
         sizeClass,
         variantClass
       ]">
    <div class="select-trigger" :class="{ 'is-disabled': disabled }" @click="toggleOpen">
      <slot name="prefix" :selected="selectedOption"/>
      <span class="select-label" :class="{ 'is-placeholder': !selectedLabel }">
        {{ selectedLabel || placeholder }}
      </span>
      <i
          v-if="clearable && hasValue"
          class="iconfont icon-close select-clear"
          @click.stop="handleClear"
      />
      <i v-else class="iconfont icon-chevron-down select-arrow"/>
    </div>
    <transition name="dropdown">
      <div v-show="isOpen" class="select-dropdown"
           :class="{ 'is-grid': grid, 'align-right': dropdownAlign === 'right' }">
        <template v-if="options.length > 0">
          <div
              v-for="option in options"
              :key="option.value"
              class="select-option"
              :class="{ 'is-selected': String(modelValue) === String(option.value) }"
              @click.stop="handleSelect(option)"
          >
            <slot name="option" :option="option" :selected="String(modelValue) === String(option.value)">
              <span class="option-label">{{ option.label }}</span>
              <i v-if="!grid && String(modelValue) === String(option.value)" class="iconfont icon-check"/>
            </slot>
          </div>
        </template>
        <div v-else class="select-empty">
          <slot name="empty">{{ emptyText }}</slot>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, onUnmounted} from 'vue'

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
  emptyText: {
    type: String,
    default: '无数据'
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
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'large', 'small'].includes(val)
  },
  variant: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'search'].includes(val)
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const selectRef = ref(null)

const sizeClass = computed(() => props.size !== 'default' ? `cs-size--${props.size}` : '')
const variantClass = computed(() => props.variant !== 'default' ? `cs-variant--${props.variant}` : '')

const hasValue = computed(() => {
  return props.modelValue !== '' && props.modelValue !== null && props.modelValue !== undefined
})

const selectedOption = computed(() => {
  return props.options.find(opt => String(opt.value) === String(props.modelValue))
})

const selectedLabel = computed(() => {
  return selectedOption.value ? selectedOption.value.label : ''
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
  height: 38px;
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
  padding: 0 var(--space-3);
  background-color: var(--card-bg);
  box-shadow: var(--shadow-input);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.3s ease;
}

.select-trigger:hover:not(.is-disabled) {
  box-shadow: var(--shadow-input-hover);
  border-color: var(--border-color);
}

.select-trigger.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
  background-color: var(--disabled-bg);
  border: var(--disabled-border);
  box-shadow: none;
}

.custom-select.is-open .select-trigger {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-focus);
}

/* ---------- 搜索变体 ---------- */
.custom-select.cs-variant--search .select-trigger {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
  border-color: var(--border-color);
}

.custom-select.cs-variant--search .select-trigger:hover:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--gray-40) inset;
}

.custom-select.cs-variant--search.is-open .select-trigger {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
  border-color: var(--primary-color);
}

.custom-select.cs-variant--search.is-open .select-trigger:hover:not(.is-disabled) {
  box-shadow: 0 0 0 1px var(--primary-color) inset;
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
  border-radius: 50%;
  transition: all 0.3s ease;
  margin-left: 4px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.select-clear:hover {
  background: var(--primary-light);
  color: var(--primary-dark);
}

.select-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  width: 100%;
  background: var(--glass-bg);
  backdrop-filter: blur(10px);
  border: 1px solid var(--border-color);
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
  border: 1px solid var(--border-color);
  background: var(--card-bg);
  font-size: 13px;
  min-height: 44px;
}

.select-dropdown.is-grid .select-option:hover {
  border-color: var(--primary-color);
  box-shadow: 0 2px 8px var(--primary-20);
}

.select-dropdown.is-grid .select-option.is-selected {
  background: var(--primary-light);
  color: var(--primary-color);
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
  transition: all 0.3s ease;
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

/* ---------- 空状态 ---------- */
.select-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-4) var(--space-3);
  font-size: 13px;
  color: var(--text-tertiary);
  text-align: center;
}

/* ---------- 尺寸系统 ---------- */

/* large */
.cs-size--large {
  height: 42px;
  width: 160px;
}
.cs-size--large .select-trigger {
  padding: 0 var(--space-4);
  font-size: 16px;
}

/* small */
.cs-size--small {
  height: 28px;
  width: 120px;
}
.cs-size--small .select-trigger {
  padding: 0 var(--space-2);
  font-size: 12px;
}
.cs-size--small .select-dropdown {
  padding: 4px;
}
.cs-size--small .select-option {
  padding: 6px 8px;
  font-size: 12px;
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.3s ease;
}

.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
