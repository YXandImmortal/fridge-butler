<template>
  <div class="sort-control-wrapper">
    <div class="sort-control-group">
      <CustomSelect
          :model-value="field"
          @update:model-value="handleFieldChange"
          :options="fieldOptions"
          placeholder="排序条件"
          class="sort-select"
      />
      <CustomButton type="primary" @click="handleToggleOrder" class="sort-btn">
        {{ orderLabel }}
      </CustomButton>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import CustomSelect from '../ui/CustomSelect.vue'
import CustomButton from '../ui/CustomButton.vue'

const props = defineProps({
  field: {
    type: String,
    default: ''
  },
  order: {
    type: String,
    default: 'asc'
  },
  fieldOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:field', 'update:order', 'change'])

const orderLabel = computed(() => {
  return props.order === 'asc' ? '升序' : '降序'
})

const handleToggleOrder = () => {
  const newOrder = props.order === 'asc' ? 'desc' : 'asc'
  emit('update:order', newOrder)
  emit('change')
}

const handleFieldChange = (val) => {
  emit('update:field', val)
  emit('change')
}
</script>

<style scoped lang="scss">
.sort-control-wrapper {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.sort-control-group {
  display: flex;
  flex: 1;
  min-width: 180px;
  align-items: center;
}

/* 下拉列表：只保留左侧圆角，右侧贴合 */
.sort-control-group :deep(.sort-select .select-trigger) {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  border-right: none;
  height: 40px;
  font-size: 16px;
}

.sort-control-group :deep(.sort-select.is-open .select-trigger) {
  border-right: none;
}

/* 按钮：只保留右侧圆角，左侧贴合 */
.sort-control-group .sort-btn {
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  padding: 10px 16px;
  font-weight: 200;
  height: 40px;
  font-size: 16px;
  border: none;
}

/* 按钮悬浮时取消上浮效果，保持与下拉列表贴合 */
.sort-control-group .sort-btn:hover:not(:disabled) {
  transform: none;
  box-shadow: none;
}
</style>
