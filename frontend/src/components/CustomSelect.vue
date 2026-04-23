<template>
  <div class="custom-select" ref="selectRef" :class="{ 'is-open': isOpen }">
    <div class="select-trigger" @click="toggleOpen">
      <span class="select-label" :class="{ 'is-placeholder': !selectedLabel }">
        {{ selectedLabel || placeholder }}
      </span>
      <i class="iconfont icon-chevron-down select-arrow" />
    </div>
    <transition name="dropdown">
      <div v-show="isOpen" class="select-dropdown">
        <div
          v-for="option in options"
          :key="option.value"
          class="select-option"
          :class="{ 'is-selected': String(modelValue) === String(option.value) }"
          @click.stop="handleSelect(option)"
        >
          <span class="option-label">{{ option.label }}</span>
          <i v-if="String(modelValue) === String(option.value)" class="iconfont icon-check" />
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: '请选择'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const isOpen = ref(false)
const selectRef = ref(null)

const selectedLabel = computed(() => {
  const found = props.options.find(opt => String(opt.value) === String(props.modelValue))
  return found ? found.label : ''
})

const toggleOpen = () => {
  isOpen.value = !isOpen.value
}

const handleSelect = (option) => {
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

<style scoped>
.custom-select {
  position: relative;
  width: 140px;
  height: 40px;
  font-size: 16px;
  user-select: none;
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

.select-trigger:hover {
  border-color: var(--color-primary-200);
  box-shadow: 0 4px 12px var(--primary-20);
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
