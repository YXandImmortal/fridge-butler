<template>
  <div class="item-creation-wizard">
    <!-- 步骤条 -->
    <el-steps :active="data.currentStep" finish-status="success" simple class="wizard-steps">
      <el-step
          v-for="(step, index) in data.steps"
          :key="index"
          :title="step.title"
      />
    </el-steps>

    <!-- 当前步骤描述 -->
    <div class="wizard-description">
      <p>{{ currentStepDescription }}</p>
    </div>

    <!-- 已收集的表单数据摘要 -->
    <div v-if="hasFormData" class="wizard-summary">
      <div v-if="data.formData.itemName" class="summary-item">
        <span class="summary-label">名称：</span>
        <span class="summary-value">{{ data.formData.itemName }}</span>
      </div>
      <div v-if="data.formData.categoryId" class="summary-item">
        <span class="summary-label">分类：</span>
        <span class="summary-value">{{ categoryName }}</span>
      </div>
      <div v-if="data.formData.itemNum != null && data.formData.itemNum !== ''" class="summary-item">
        <span class="summary-label">数量：</span>
        <span class="summary-value">{{ data.formData.itemNum }} {{ unitName }}</span>
      </div>
      <div v-if="data.formData.productionDate" class="summary-item">
        <span class="summary-label">生产日期：</span>
        <span class="summary-value">{{ data.formData.productionDate }}</span>
      </div>
      <div v-if="data.formData.shelfLifeDays" class="summary-item">
        <span class="summary-label">保质期：</span>
        <span class="summary-value">{{ data.formData.shelfLifeDays }} 天</span>
      </div>
      <div v-if="data.formData.remark" class="summary-item">
        <span class="summary-label">备注：</span>
        <span class="summary-value">{{ data.formData.remark }}</span>
      </div>
    </div>

    <!-- 输入区（非最后一步时显示） -->
    <div v-if="!isLastStep && currentInputConfig" class="wizard-input-area">
      <!-- 文本输入 -->
      <template v-if="inputComponentType === 'text'">
        <EnhancedInput
            v-model="inputValue"
            :placeholder="currentInputConfig.placeholder || `请输入${currentInputConfig.label}`"
            type="text"
            clearable
            maxlength="50"
            show-word-limit
            :class="{ 'is-error': currentInputConfig.required && !isInputValid }"
        />
      </template>

      <!-- 文本域 -->
      <template v-else-if="inputComponentType === 'textarea'">
        <EnhancedInput
            v-model="inputValue"
            :placeholder="currentInputConfig.placeholder || `请输入${currentInputConfig.label}`"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            :class="{ 'is-error': currentInputConfig.required && !isInputValid }"
        />
      </template>

      <!-- 选择器 -->
      <template v-else-if="inputComponentType === 'select'">
        <CustomSelect
            v-model="inputValue"
            :options="selectOptions"
            :placeholder="currentInputConfig.placeholder || `请选择${currentInputConfig.label}`"
            :grid="true"
            :full-width="true"
            class="wizard-custom-select"
        />
      </template>

      <!-- 数字输入（数量） -->
      <template v-else-if="inputComponentType === 'number'">
        <el-input-number
            v-model="inputValueNum"
            :min="0"
            :precision="2"
            :step="1"
            :placeholder="currentInputConfig.placeholder || `请输入${currentInputConfig.label}`"
            style="width: 100%; --el-border-radius-base: var(--radius-md);"
        />
      </template>

      <!-- 日期输入 -->
      <template v-else-if="inputComponentType === 'date'">
        <el-date-picker
            v-model="inputValue"
            type="date"
            :placeholder="currentInputConfig.placeholder || `请选择${currentInputConfig.label}`"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            style="width: 100%; --el-border-radius-base: var(--radius-md); --el-input-height: 40px;"
            clearable
            :default-value="new Date()"
        />
      </template>

      <!-- 组合输入：数量 + 单位类型 + 单位 -->
      <template v-else-if="inputComponentType === 'combined_unit'">
        <div class="combined-unit-row">
          <el-input-number
              v-model="inputValueNum"
              :min="1"
              :precision="2"
              :step="1"
              placeholder="数量"
              class="unit-num-input"
              size="large"
              style="--el-border-radius-base: var(--radius-md);"
          />
          <CustomSelect
              v-model="unitTypeId"
              :options="unitTypeOptions"
              placeholder="单位类型"
              grid
              class="unit-type-select"
              @change="handleUnitTypeChange"
          />
          <CustomSelect
              v-model="unitId"
              :options="filteredUnitOptions"
              placeholder="单位"
              grid
              :disabled="!unitTypeId"
              class="unit-select"
          />
        </div>
      </template>

      <!-- 组合输入：生产日期 + 保质期 -->
      <template v-else-if="inputComponentType === 'combined_date_number'">

        <div class="shelf-life-row">
          <el-date-picker
              v-model="dateValue"
              type="date"
              placeholder="请选择生产日期（选填）"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
              style="--el-border-radius-base: var(--radius-md); flex: 1 1 0;"
              clearable
              :default-value="new Date()"
              size="large"
          />
          <el-input-number
              v-model="shelfLifeDays"
              :min="1"
              :step="1"
              placeholder="保质期天数（选填）"
              style="--el-border-radius-base: var(--radius-md); flex: 1 1 0;"
              size="large"
          />
          <span class="shelf-life-hint">天</span>
        </div>
      </template>

      <div
          v-if="currentInputConfig.required && !isInputValid"
          class="wizard-input-hint is-error"
      >
        <i class="iconfont icon-info"/> 此项为必填
      </div>
    </div>

    <!-- 最后一步确认信息 -->
    <div v-if="isLastStep" class="wizard-confirm-area">
      <div class="confirm-card">
        <div class="confirm-icon">
          <i class="iconfont icon-item"/>
        </div>
        <div class="confirm-info">
          <div class="confirm-name">{{ data.formData.itemName || '未命名物品' }}</div>
          <div class="confirm-meta">
            <span v-if="categoryName" class="confirm-meta-item">{{ categoryName }}</span>
            <span v-if="data.formData.itemNum != null && data.formData.itemNum !== ''"
                  class="confirm-meta-item">{{ data.formData.itemNum }} {{ unitName }}</span>
          </div>
          <div v-if="data.formData.productionDate" class="confirm-address">
            <i class="iconfont icon-calendar"/> 生产日期：{{ data.formData.productionDate }}
          </div>
          <div v-if="data.formData.shelfLifeDays" class="confirm-address">
            <i class="iconfont icon-calendar-alert"/> 保质期：{{ data.formData.shelfLifeDays }} 天
          </div>
          <div v-if="data.formData.remark" class="confirm-desc">{{ data.formData.remark }}</div>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="wizard-actions">
      <CustomButton class="dialog-btn dialog-btn-cancel" @click="handleCancel">
        取消
      </CustomButton>
      <CustomButton
          v-if="canSkip"
          class="dialog-btn dialog-btn-cancel"
          :disabled="stepSubmitting"
          @click="handleSkip"
      >
        跳过
      </CustomButton>
      <CustomButton
          v-if="!isLastStep"
          type="primary"
          class="dialog-btn dialog-btn-confirm"
          :loading="stepSubmitting"
          :disabled="!canGoNext"
          @click="handleNext"
      >
        {{ stepSubmitting ? '处理中...' : '下一步' }}
      </CustomButton>
      <CustomButton
          v-else
          type="primary"
          class="dialog-btn dialog-btn-confirm"
          :loading="creating"
          @click="handleConfirm"
      >
        确认添加
      </CustomButton>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, watch, onMounted} from 'vue'
import CustomButton from '@/components/CustomButton.vue'
import CustomSelect from '@/components/CustomSelect.vue'
import EnhancedInput from '@/components/EnhancedInput.vue'
import {listItemCategories, listItemUnits, listUnitTypes} from '@/api/item'

const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({
      currentStep: 0,
      totalSteps: 5,
      steps: [],
      formData: {},
      currentInput: null
    })
  }
})

const emit = defineEmits(['step-submit', 'confirm', 'cancel', 'skip'])

// ==================== 基础数据（自治加载）====================
const categoryList = ref([])
const unitList = ref([])
const unitTypeList = ref([])
const dataLoading = ref(false)

async function loadBaseData() {
  dataLoading.value = true
  try {
    const [catRes, unitRes, typeRes] = await Promise.all([
      listItemCategories().catch(() => ({code: -1, data: []})),
      listItemUnits().catch(() => ({code: -1, data: []})),
      listUnitTypes().catch(() => ({code: -1, data: []}))
    ])
    if (catRes.code === 200 && Array.isArray(catRes.data)) {
      categoryList.value = catRes.data
    }
    if (unitRes.code === 200 && Array.isArray(unitRes.data)) {
      unitList.value = unitRes.data
    }
    if (typeRes.code === 200 && Array.isArray(typeRes.data)) {
      unitTypeList.value = typeRes.data
    }
  } catch (err) {
    console.error('加载基础数据失败:', err)
  } finally {
    dataLoading.value = false
  }
}

onMounted(() => {
  loadBaseData()
})

// ==================== 输入状态 ====================
const inputValue = ref('')
const inputValueNum = ref(null)
const dateValue = ref('')
const shelfLifeDays = ref(null)
const unitTypeId = ref('')
const unitId = ref('')
const creating = ref(false)
const stepSubmitting = ref(false)

const isLastStep = computed(() => {
  return props.data.currentStep >= (props.data.totalSteps || props.data.steps?.length || 1) - 1
})

const currentStepDescription = computed(() => {
  const step = props.data.steps?.[props.data.currentStep]
  return step?.description || ''
})

const currentInputConfig = computed(() => {
  return props.data.currentInput || null
})

const inputComponentType = computed(() => {
  return currentInputConfig.value?.type || 'text'
})

const selectOptions = computed(() => {
  if (inputComponentType.value !== 'select') return []
  const field = currentInputConfig.value?.field
  if (field === 'categoryId') {
    return categoryList.value.map(c => ({label: c.categoryName, value: c.id}))
  }
  return currentInputConfig.value?.options || []
})

const unitTypeOptions = computed(() => {
  return unitTypeList.value.map(t => ({label: t.unitTypeName, value: t.id}))
})

const filteredUnitOptions = computed(() => {
  if (!unitTypeId.value) return []
  return unitList.value
      .filter(u => u.unitTypeId === unitTypeId.value)
      .map(u => ({label: u.unitName, value: u.id}))
})

const categoryName = computed(() => {
  const cid = props.data.formData?.categoryId
  if (!cid) return ''
  const cat = categoryList.value.find(c => c.id === Number(cid))
  return cat?.categoryName || ''
})

const unitName = computed(() => {
  const uid = props.data.formData?.itemUnitId || unitId.value
  if (!uid) return ''
  const unit = unitList.value.find(u => u.id === Number(uid))
  return unit?.unitName || ''
})

const isInputValid = computed(() => {
  const type = inputComponentType.value
  if (type === 'combined_unit') {
    return inputValueNum.value !== null && inputValueNum.value !== undefined && inputValueNum.value !== '' && unitId.value
  }
  if (type === 'combined_date_number') {
    return true // 均为选填
  }
  if (type === 'number') {
    return inputValueNum.value !== null && inputValueNum.value !== undefined && inputValueNum.value !== ''
  }
  if (type === 'select') {
    return inputValue.value !== null && inputValue.value !== undefined && inputValue.value !== ''
  }
  if (type === 'date') {
    return !!inputValue.value
  }
  return String(inputValue.value || '').trim().length > 0
})

const canGoNext = computed(() => {
  if (isLastStep.value) return true
  if (!currentInputConfig.value) return true
  if (!currentInputConfig.value.required) return true
  return isInputValid.value
})

const canSkip = computed(() => {
  if (isLastStep.value) return false
  return currentInputConfig.value && !currentInputConfig.value.required
})

const hasFormData = computed(() => {
  const fd = props.data.formData
  if (!fd) return false
  // 只检查 summary 中实际会显示的字段，避免 formData 中存在
  // summary 未渲染的字段（如 unitTypeId、itemUnitId 或 itemNum: 0）
  // 导致出现空的蓝色摘要块
  return !!(
      fd.itemName ||
      fd.categoryId ||
      (fd.itemNum != null && fd.itemNum !== '') ||
      fd.productionDate ||
      fd.shelfLifeDays ||
      fd.remark
  )
})

// ==================== 输入管理 ====================
function resetInput() {
  inputValue.value = ''
  inputValueNum.value = null
  dateValue.value = ''
  shelfLifeDays.value = null
  unitTypeId.value = ''
  unitId.value = ''
}

function handleUnitTypeChange() {
  unitId.value = ''
}

function initInputFromFormData() {
  const config = currentInputConfig.value
  if (!config) {
    resetInput()
    return
  }

  const field = config.field
  const existing = props.data.formData?.[field]

  if (inputComponentType.value === 'number') {
    inputValueNum.value = existing !== undefined && existing !== null && existing !== '' ? Number(existing) : null
  } else if (inputComponentType.value === 'select') {
    inputValue.value = existing !== undefined && existing !== null ? existing : ''
  } else if (inputComponentType.value === 'date') {
    inputValue.value = existing !== undefined && existing !== null ? String(existing) : ''
  } else if (inputComponentType.value === 'combined_unit') {
    inputValueNum.value = props.data.formData?.itemNum !== undefined && props.data.formData?.itemNum !== null ? Number(props.data.formData.itemNum) : null
    unitTypeId.value = props.data.formData?.unitTypeId || ''
    unitId.value = props.data.formData?.itemUnitId || ''
  } else if (inputComponentType.value === 'combined_date_number') {
    dateValue.value = props.data.formData?.productionDate || ''
    shelfLifeDays.value = props.data.formData?.shelfLifeDays || null
  } else {
    inputValue.value = existing !== undefined && existing !== null ? String(existing) : ''
  }
}

watch(
    () => [props.data.currentStep, props.data.currentInput?.field],
    () => {
      stepSubmitting.value = false
      resetInput()
      initInputFromFormData()
    },
    {immediate: true}
)

// ==================== 事件处理 ====================
function handleNext() {
  if (!canGoNext.value || stepSubmitting.value) return
  stepSubmitting.value = true
  const config = currentInputConfig.value
  const field = config?.field
  let value
  const formData = {...props.data.formData}

  const type = inputComponentType.value
  if (type === 'number') {
    value = inputValueNum.value
  } else if (type === 'select') {
    value = inputValue.value
  } else if (type === 'date') {
    value = inputValue.value
  } else if (type === 'combined_unit') {
    value = inputValueNum.value
    formData.itemNum = inputValueNum.value
    formData.unitTypeId = unitTypeId.value
    formData.itemUnitId = unitId.value
  } else if (type === 'combined_date_number') {
    formData.productionDate = dateValue.value || undefined
    formData.shelfLifeDays = shelfLifeDays.value || undefined
  } else {
    value = String(inputValue.value || '').trim()
  }

  if (type !== 'combined_unit' && type !== 'combined_date_number') {
    formData[field] = value
  }

  emit('step-submit', {field, value, formData})
}

function handleSkip() {
  if (stepSubmitting.value) return
  stepSubmitting.value = true
  const field = currentInputConfig.value?.field
  let messageText
  if (field === 'productionDate') {
    messageText = '跳过生产日期和保质期'
  } else if (field === 'remark') {
    messageText = '跳过备注'
  } else {
    messageText = `跳过${currentInputConfig.value?.label || field}`
  }
  emit('skip', {field, formData: props.data.formData, messageText})
}

function handleConfirm() {
  if (creating.value) return
  if (!props.data.formData.itemName || !String(props.data.formData.itemName).trim()) {
    emit('cancel')
    return
  }
  creating.value = true
  emit('confirm', props.data.formData)
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.item-creation-wizard {
  background: var(--main-content-bg);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-md);
  padding: var(--space-3) var(--space-4);
}

.wizard-steps {
  :deep(.el-steps--simple) {
    background: transparent;
    padding: 8px 0;
  }

  :deep(.el-step__title) {
    font-size: 13px;
    font-weight: 500;
  }

  :deep(.el-step__icon) {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
}

.wizard-description {
  margin: var(--space-3) 0;
  text-align: center;

  p {
    margin: 0;
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.6;
  }
}

.wizard-summary {
  background: var(--primary-10);
  border-radius: var(--radius-sm);
  padding: var(--space-3) var(--space-4);
  margin-bottom: var(--space-3);
  display: flex;
  flex-direction: column;
  gap: 4px;

  .summary-item {
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .summary-label {
    color: var(--text-tertiary);
    flex-shrink: 0;
  }

  .summary-value {
    color: var(--text-primary);
    font-weight: 500;
  }
}

.wizard-input-area {
  margin-bottom: var(--space-3);

  .enhanced-input.is-error :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 1px #F56C6C inset;
    border-color: var(--el-color-danger);
  }

  .enhanced-input.is-error :deep(.el-textarea__inner.is-focus) {
    box-shadow: 0 0 0 1px #F56C6C inset;
    border-color: var(--el-color-danger);
  }

  .wizard-input-hint {
    margin-top: 6px;
    font-size: 12px;
    color: var(--text-tertiary);
    display: flex;
    align-items: center;
    gap: 4px;
    transition: color 0.2s ease;

    i {
      font-size: 12px;
    }

    &.is-error {
      color: #F56C6C;
      font-weight: 500;
    }
  }
}

/* 组合单位输入 */
.combined-unit-row {
  display: flex;
  gap: var(--space-2);
  align-items: center;

  .unit-num-input {
    flex: 0 0 33%;
  }

  .unit-type-select,
  .unit-select {
    flex: 1;
    min-width: 0;
  }
}

/* 保质期组合输入 */
.shelf-life-row {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.shelf-life-hint {
  font-size: 14px;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.wizard-confirm-area {
  margin-bottom: var(--space-3);

  .confirm-card {
    display: flex;
    align-items: center;
    gap: var(--space-3);
    background: var(--primary-10);
    border: 1px solid var(--primary-light);
    border-radius: var(--radius-md);
    padding: var(--space-4);
  }

  .confirm-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    background: var(--card-bg);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    i {
      font-size: 24px;
      color: var(--primary-dark);
    }
  }

  .confirm-info {
    flex: 1;
    min-width: 0;
  }

  .confirm-name {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
  }

  .confirm-meta {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: var(--space-2);
    margin-bottom: 4px;
  }

  .confirm-meta-item {
    font-size: 12px;
    color: var(--text-secondary);
    background: var(--glass-bg);
    padding: 2px 8px;
    border-radius: var(--radius-sm);
    border: 1px solid var(--border-light);
  }

  .confirm-address {
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 4px;
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      font-size: 12px;
      color: var(--text-tertiary);
    }
  }

  .confirm-desc {
    font-size: 13px;
    color: var(--text-secondary);
    word-break: break-word;
  }
}

.wizard-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
}

.wizard-custom-select {
  :deep(.custom-select-trigger) {
    height: 40px;
  }
}

/* 响应式 */
@media (max-width: 768px) {
  .combined-unit-row {
    flex-direction: column;
    align-items: stretch;

    .unit-num-input {
      flex: 1;
      width: 100%;
    }
  }
}
</style>
