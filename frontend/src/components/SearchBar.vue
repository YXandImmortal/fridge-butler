<template>
  <div class="search-bar-wrapper">
    <div class="search-input-group">
      <EnhancedInput
        :model-value="modelValue"
        @update:model-value="$emit('update:modelValue', $event)"
        :placeholder="placeholder"
        variant="search"
        clearable
        @keyup.enter="handleSearch"
        @clear="handleClear"
        class="search-input"
        height="28px"
      />
      <CustomButton type="primary" @click="handleSearch" class="search-btn">
        <i class="iconfont icon-search" />
      </CustomButton>
    </div>
    <slot />
  </div>
</template>

<script setup>
import CustomButton from './CustomButton.vue'
import EnhancedInput from './EnhancedInput.vue'

defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索'
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear'])

const handleSearch = () => {
  emit('search')
}

const handleClear = () => {
  emit('clear')
}
</script>

<style scoped lang="scss">
.search-bar-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.search-input-group {
  display: flex;
  flex: 1;
  min-width: 180px;
  align-items: center;
}

/* 按钮：只保留右侧圆角，左侧贴合 */
.search-input-group .search-btn {
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  padding: 10px 20px;
  font-weight: 200;
  height: 40px;
  font-size: 16px;
  border: none;
}

/* 按钮悬浮时取消上浮效果，保持与输入框贴合 */
.search-input-group .search-btn:hover:not(:disabled) {
  transform: none;
  box-shadow: none;
}

.iconfont.icon-search {
  font-size: 24px;
}
</style>
