<template>
  <div
      ref="selectRef"
      class="sticky-note-select"
      :class="[sizeClass, { 'is-open': isOpen, 'is-disabled': disabled }]"
  >
    <div class="sns-trigger" @click="toggleOpen">
      <span class="sns-label" :class="{ 'is-placeholder': !selectedLabel }">
        {{ selectedLabel || placeholder }}
      </span>
      <i class="iconfont icon-chevron-down sns-arrow"/>
    </div>
    <transition name="sns-dropdown">
      <div
          v-show="isOpen"
          class="sns-dropdown"
          :style="{ maxHeight: dropdownMaxHeight + 'px' }"
      >
        <template v-if="options.length > 0">
          <div
              v-for="(option, idx) in options"
              :key="option.value"
              class="sns-option"
              :class="{ 'is-selected': isSelected(option), 'is-disabled': option.disabled }"
              :style="{ '--option-rotate': getOptionRotate(idx), '--option-color': option.color || noteColor }"
              @click.stop="handleSelect(option)"
          >
            <span class="sns-option-label">{{ option.label }}</span>
            <i v-if="isSelected(option)" class="iconfont icon-check sns-check"/>
          </div>
        </template>
        <div v-else class="sns-empty">{{ emptyText }}</div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from 'vue'

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
  disabled: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (val) => ['default', 'small'].includes(val)
  },
  noteColor: {
    type: String,
    default: ''
  },
  dropdownMaxHeight: {
    type: Number,
    default: 200
  },
  seed: {
    type: [String, Number],
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const selectRef = ref(null)

const sizeClass = computed(() => props.size !== 'default' ? `sns-size--${props.size}` : '')

const selectedOption = computed(() => {
  return props.options.find(opt => String(opt.value) === String(props.modelValue))
})

const selectedLabel = computed(() => selectedOption.value?.label || '')

const isSelected = (option) => String(option.value) === String(props.modelValue)

const getOptionRotate = (index) => {
  const deg = (index % 3) - 1
  return `${deg}deg`
}

const toggleOpen = () => {
  if (props.disabled) return
  isOpen.value = !isOpen.value
}

const handleSelect = (option) => {
  if (option.disabled) return
  emit('update:modelValue', option.value)
  emit('change', option.value)
  isOpen.value = false
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
.sticky-note-select {
  position: relative;
  display: inline-flex;
  width: 100%;
  font-size: 14px;
  user-select: none;
}

.sns-trigger {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 4px 8px;
  background: var(--cork-note-yellow-field-bg);
  border: none;
  border-bottom: 1px dashed var(--cork-note-border);
  border-radius: 2px;
  cursor: pointer;
  transition: border-bottom-color 0.3s ease, background-color 0.3s ease;
  min-height: 28px;
}

.sns-trigger:hover {
  border-bottom-color: var(--cork-note-label);
  background: var(--cork-note-yellow-field-bg-focus);
}

.sticky-note-select.is-disabled .sns-trigger {
  cursor: not-allowed;
  opacity: 0.6;
}

.sns-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--cork-note-text);
  font-weight: 500;
  line-height: 1.4;
}

.sns-label.is-placeholder {
  color: var(--cork-note-label);
  opacity: 0.75;
}

.sns-arrow {
  font-size: 12px;
  color: var(--cork-note-label);
  margin-left: 4px;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.sticky-note-select.is-open .sns-arrow {
  transform: rotate(180deg);
}

.sns-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  left: 0;
  width: max-content;
  min-width: 100%;
  max-width: 200px;
  padding: 6px;
  background: var(--cork-select-dropdown-bg);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 4px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.22);
  z-index: 200;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sns-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  background: var(--option-color, var(--cork-note-yellow));
  border-radius: 2px;
  box-shadow: 1px 2px 4px rgba(0, 0, 0, 0.12);
  transform: rotate(var(--option-rotate, 0deg));
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background-color 0.3s ease;
  color: var(--cork-note-text);
  font-size: 13px;
}

.sns-option:hover:not(.is-disabled) {
  transform: rotate(0deg) translateY(-2px);
  box-shadow: 2px 4px 8px rgba(0, 0, 0, 0.18);
}

.sns-option.is-selected {
  background: var(--cork-select-selected);
  font-weight: 600;
}

.sns-option.is-disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.sns-option-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.sns-check {
  font-size: 12px;
  margin-left: 4px;
  flex-shrink: 0;
  color: var(--cork-note-text);
}

.sns-empty {
  padding: 8px;
  text-align: center;
  font-size: 12px;
  color: var(--cork-select-empty-text);
}

/* 小尺寸 */
.sns-size--small .sns-trigger {
  padding: 2px 6px;
  min-height: 24px;
  font-size: 13px;
}

.sns-size--small .sns-option {
  padding: 4px 6px;
  font-size: 12px;
}

/* 下拉动画 */
.sns-dropdown-enter-active,
.sns-dropdown-leave-active {
  transition: all 0.3s ease;
}

.sns-dropdown-enter-from,
.sns-dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
</style>
