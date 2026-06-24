<template>
  <div
      class="check-in-row step1-row"
      :class="{ 'is-skipped': !item.bought && !item.isNew, 'is-new': item.isNew }"
  >
    <!-- 物品信息 -->
    <div class="col col-info">
      <div class="info-top">
        <span class="status-tag" :class="statusClass">{{ statusText }}</span>
        <CustomInput v-model="nameModel" :disabled="isDisabled" placeholder="物品名称" width="100%"/>
      </div>
    </div>

    <!-- 分类 -->
    <div class="col col-category">
      <label class="field-label">分类</label>
      <CustomSelect
          v-model="categoryModel"
          :options="categoryOptions"
          :disabled="isDisabled"
          placeholder="分类"
          full-width
      />
    </div>

    <!-- 单位类型 -->
    <div class="col col-unit-type">
      <label class="field-label">单位类型</label>
      <CustomSelect
          v-model="unitTypeModel"
          :options="unitTypeOptions"
          :disabled="isDisabled"
          placeholder="类型"
          full-width
      />
    </div>

    <!-- 单位 -->
    <div class="col col-unit">
      <label class="field-label">单位</label>
      <CustomSelect
          v-model="unitModel"
          :options="currentUnitOptions"
          :disabled="isDisabled"
          placeholder="单位"
          full-width
      />
    </div>

    <!-- 计划数量 -->
    <div class="col col-planned">
      <label class="field-label">计划数量</label>
      <div class="planned-text">{{ formatDecimal(item.plannedNum) }}</div>
    </div>

    <!-- 实际数量 -->
    <div class="col col-actual">
      <label class="field-label">实际数量</label>
      <CustomInputNumber
          v-model="actualNumModel"
          :disabled="isDisabled"
          :min="0.01"
          :precision="2"
          controls-position="right"
          width="100%"
      />
    </div>

    <!-- 是否采购 -->
    <div class="col col-bought">
      <label class="field-label">采购</label>
      <CustomCheckbox v-model="boughtModel" :disabled="item.isNew" size="large"/>
    </div>

    <!-- 操作 -->
    <div class="col col-action">
      <CustomButton
          v-if="item.isNew"
          type="danger"
          size="small"
          @click="handleRemove"
      >
        删除
      </CustomButton>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomCheckbox from '@/components/ui/CustomCheckbox.vue'
import CustomButton from '@/components/ui/CustomButton.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  },
  categoryOptions: {
    type: Array,
    default: () => []
  },
  unitTypeOptions: {
    type: Array,
    default: () => []
  },
  unitOptions: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update', 'remove'])

const isDisabled = computed(() => !props.item.bought && !props.item.isNew)

const formatDecimal = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const num = parseFloat(value)
  if (Number.isNaN(num)) return value
  return Number(num.toFixed(2))
}

const statusMap = computed(() => {
  if (props.item.isNew) {
    return {text: '新增', class: 'status-new'}
  }
  if (!props.item.bought) {
    return {text: '跳过', class: 'status-skipped'}
  }
  return {text: '已采购', class: 'status-purchased'}
})

const statusText = computed(() => statusMap.value.text)
const statusClass = computed(() => statusMap.value.class)

const currentUnitOptions = computed(() => {
  if (props.item.unitTypeId) {
    return props.unitOptions.filter(o => o.unitTypeId === props.item.unitTypeId)
  }
  return props.unitOptions
})

const makeModel = (field) => computed({
  get: () => props.item[field],
  set: (value) => emit('update', {field, value})
})

const nameModel = makeModel('itemName')
const categoryModel = makeModel('categoryId')
const unitTypeModel = makeModel('unitTypeId')
const unitModel = makeModel('itemUnitId')
const actualNumModel = makeModel('actualNum')
const boughtModel = makeModel('bought')

const handleRemove = () => emit('remove')
</script>

<style scoped lang="scss">
.check-in-row {
  display: grid;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3) var(--space-4);
  border-bottom: 1px solid var(--border-color);
  transition: background-color 0.2s ease, opacity 0.2s ease;
}

.check-in-row:last-child {
  border-bottom: none;
}

.check-in-row:hover {
  background-color: var(--primary-10);
}

.step1-row {
  grid-template-columns:
    minmax(160px, 1.8fr)
    minmax(110px, 1.2fr)
    minmax(110px, 1.2fr)
    minmax(110px, 1.2fr)
    90px
    110px
    60px
    70px;
}

.is-skipped {
  background-color: var(--warning-light);
}

.is-new {
  background-color: var(--success-light);
}

.col {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
  min-width: 0;
}

.field-label {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
  display: none;
}

.info-top {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.status-tag {
  display: inline-flex;
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}

.status-new {
  background: var(--success-light);
  color: var(--success-color);
}

.status-purchased {
  background: var(--primary-10);
  color: var(--primary-color);
}

.status-skipped {
  background: var(--warning-light);
  color: var(--warning-color);
}

.planned-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 38px;
  text-align: center;
}

.col-bought {
  align-items: center;
  justify-content: center;
}

.col-action {
  align-items: center;
  justify-content: center;
}

/* 移动端：卡片式堆叠 */
@media (max-width: 768px) {
  .step1-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-3);
    padding: var(--space-4);
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: var(--space-3);
    box-shadow: var(--shadow-sm);
  }

  .step1-row:last-child {
    border-bottom: 1px solid var(--border-color);
  }

  .col-info {
    grid-column: 1 / -1;
  }

  .info-top {
    flex-direction: column;
    align-items: stretch;
    gap: var(--space-2);
  }

  .status-tag {
    align-self: flex-start;
  }

  .field-label {
    display: block;
  }

  .planned-text {
    text-align: left;
    line-height: 1.5;
  }
}
</style>
