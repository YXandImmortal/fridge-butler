<template>
  <div
      class="check-in-row step2-row"
      :class="{ 'is-not-stored': item.bought && !item.storeInFridge }"
  >
    <!-- 物品信息 -->
    <div class="col col-info">
      <span class="status-tag" :class="statusClass">{{ statusText }}</span>
      <div class="item-name">{{ item.itemName }}</div>
      <div class="item-meta">
        {{ formatDecimal(item.actualNum) }} {{ item.itemUnitName || '' }}
        <span v-if="item.categoryName">· {{ item.categoryName }}</span>
      </div>
    </div>

    <!-- 实际数量 -->
    <div class="col col-actual">
      <label class="field-label">实际数量</label>
      <div class="actual-text">
        {{ formatDecimal(item.actualNum) }} {{ item.itemUnitName || '' }}
      </div>
    </div>

    <!-- 存入冰箱 -->
    <div class="col col-store">
      <label class="field-label">存入冰箱</label>
      <CustomCheckbox v-model="storeModel" size="large"/>
    </div>

    <!-- 生产日期 -->
    <div class="col col-date">
      <label class="field-label">生产日期</label>
      <CustomDatePicker v-model="productionDateModel" :disabled="!item.storeInFridge" clearable width="100%"/>
    </div>

    <!-- 保质期 -->
    <div class="col col-life">
      <label class="field-label">保质期(天)</label>
      <CustomInputNumber
          v-model="shelfLifeDaysModel"
          :disabled="!item.storeInFridge"
          :min="1"
          :precision="0"
          controls-position="right"
          width="100%"
      />
    </div>

    <!-- 存放位置 -->
    <div class="col col-location">
      <label class="field-label">存放位置</label>
      <CustomInput v-model="storageLocationModel" :disabled="!item.storeInFridge" :placeholder="item.storeInFridge ? '选填' : '无需填写'" width="100%"/>
    </div>

    <!-- 备注 -->
    <div class="col col-remark">
      <label class="field-label">备注</label>
      <CustomInput v-model="remarkModel" placeholder="备注" width="100%"/>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import CustomInputNumber from '@/components/ui/CustomInputNumber.vue'
import CustomDatePicker from '@/components/ui/CustomDatePicker.vue'
import CustomCheckbox from '@/components/ui/CustomCheckbox.vue'

const props = defineProps({
  item: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update'])

const formatDecimal = (value) => {
  if (value === null || value === undefined || value === '') return ''
  const num = parseFloat(value)
  if (Number.isNaN(num)) return value
  return Number(num.toFixed(2))
}

const statusMap = computed(() => {
  if (props.item.storeInFridge) {
    return {text: '确定入库', class: 'status-stored'}
  }
  return {text: '已采购不入库', class: 'status-not-stored'}
})

const statusText = computed(() => statusMap.value.text)
const statusClass = computed(() => statusMap.value.class)

const makeModel = (field) => computed({
  get: () => props.item[field],
  set: (value) => emit('update', {field, value})
})

const storeModel = makeModel('storeInFridge')
const productionDateModel = makeModel('productionDate')
const shelfLifeDaysModel = makeModel('shelfLifeDays')
const storageLocationModel = makeModel('storageLocation')
const remarkModel = makeModel('remark')
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

.step2-row {
  grid-template-columns:
    minmax(160px, 1.6fr)
    110px
    70px
    160px
    100px
    130px
    minmax(120px, 1fr);
}

.step2-row .custom-date-picker.is-open {
  z-index: 200;
}

.is-not-stored {
  background-color: var(--info-light);
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

.col-info {
  gap: var(--space-1);
}

.status-tag {
  display: inline-flex;
  align-self: flex-start;
  padding: 2px 8px;
  border-radius: var(--radius-full);
  font-size: 11px;
  font-weight: 600;
}

.status-stored {
  background: var(--success-light);
  color: var(--success-color);
}

.status-not-stored {
  background: var(--info-light);
  color: var(--info-color);
}

.item-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.3;
}

.item-meta {
  font-size: 12px;
  color: var(--text-secondary);
}

.actual-text {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 38px;
  text-align: center;
}

.col-store {
  align-items: center;
  justify-content: center;
}

/* 移动端：卡片式堆叠 */
@media (max-width: 768px) {
  .step2-row {
    grid-template-columns: 1fr 1fr;
    gap: var(--space-3);
    padding: var(--space-4);
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-md);
    margin-bottom: var(--space-3);
    box-shadow: var(--shadow-sm);
  }

  .step2-row:last-child {
    border-bottom: 1px solid var(--border-color);
  }

  .col-info {
    grid-column: 1 / -1;
  }

  .col-remark {
    grid-column: 1 / -1;
  }

  .field-label {
    display: block;
  }

  .actual-text {
    text-align: left;
    line-height: 1.5;
  }
}
</style>
