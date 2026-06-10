<template>
  <div class="fridge-creation-wizard">
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
      <div v-if="data.formData.name" class="summary-item">
        <span class="summary-label">名称：</span>
        <span class="summary-value">{{ data.formData.name }}</span>
      </div>
      <div v-if="data.formData.fridgeTypeId" class="summary-item">
        <span class="summary-label">类型：</span>
        <span class="summary-value">{{ fridgeTypeName }}</span>
      </div>
      <div v-if="data.formData.totalCapacity != null && data.formData.totalCapacity !== ''" class="summary-item">
        <span class="summary-label">容量：</span>
        <span class="summary-value">{{ data.formData.totalCapacity }} L</span>
      </div>
      <div v-if="data.formData.isDefault" class="summary-item">
        <span class="summary-label">默认冰箱：</span>
        <span class="summary-value">是</span>
      </div>
      <div v-if="data.formData.address" class="summary-item">
        <span class="summary-label">地址：</span>
        <span class="summary-value">{{ data.formData.address }}</span>
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
        <CustomInput
            v-model="inputValue"
            :placeholder="currentInputConfig.placeholder || `请输入${currentInputConfig.label}`"
            type="text"
            clearable
            :maxlength="50"
            showWordLimit
            :class="{ 'is-error': currentInputConfig.required && !isInputValid }"
        />
      </template>

      <!-- 文本域 -->
      <template v-else-if="inputComponentType === 'textarea'">
        <CustomInput
            v-model="inputValue"
            :placeholder="currentInputConfig.placeholder || `请输入${currentInputConfig.label}`"
            type="textarea"
            :rows="2"
            :maxlength="200"
            showWordLimit
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
        >
          <template #prefix="{ selected }">
            <img v-if="selected?.icon" :src="selected.icon" class="fridge-type-icon-trigger" alt=""/>
          </template>
          <template #option="{ option }">
            <img :src="option.icon" class="fridge-type-icon-option" alt=""/>
            <span class="option-label">{{ option.label }}</span>
          </template>
        </CustomSelect>
      </template>

      <!-- 数字输入 -->
      <template v-else-if="inputComponentType === 'number'">
        <CustomInputNumber
            v-model="inputValueNum"
            :min="50"
            :max="1000"
            placeholder="请输入总容量（L）"
            size="large"
            width="100%"
        />
      </template>

      <!-- 开关 -->
      <template v-else-if="inputComponentType === 'switch'">
        <div class="wizard-switch-row">
          <span class="wizard-switch-label">{{ currentInputConfig.label }}</span>
          <CustomSwitch v-model="inputValueBool" size="large"/>
        </div>
      </template>

      <!-- 组合输入（地址+备注） -->
      <template v-else-if="inputComponentType === 'combined'">
        <CustomInput
            v-model="inputValue"
            :placeholder="currentInputConfig.placeholder || '请输入地址（选填）'"
            clearable
            :maxlength="100"
            showWordLimit
            class="combined-input-first"
        />
        <CustomInput
            v-model="remarkValue"
            placeholder="请输入备注（选填）"
            type="textarea"
            :rows="2"
            :maxlength="200"
            showWordLimit
            class="combined-input-second"
        />
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
          <img v-if="fridgeTypeIcon" :src="fridgeTypeIcon" alt=""/>
          <i v-else class="iconfont icon-fridge-line"/>
        </div>
        <div class="confirm-info">
          <div class="confirm-name">{{ data.formData.name || '未命名冰箱' }}</div>
          <div class="confirm-meta">
            <span v-if="fridgeTypeName" class="confirm-meta-item">{{ fridgeTypeName }}</span>
            <span v-if="data.formData.totalCapacity != null && data.formData.totalCapacity !== ''"
                  class="confirm-meta-item">{{ data.formData.totalCapacity }}L</span>
            <span v-if="data.formData.isDefault" class="confirm-meta-item confirm-meta-default">默认</span>
          </div>
          <div v-if="data.formData.address" class="confirm-address">
            <i class="iconfont icon-building-community"/> {{ data.formData.address }}
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
        确认创建
      </CustomButton>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, watch} from 'vue'
import {getFridgeTypeById, FRIDGE_TYPE_LIST} from '@/utils/fridgeTypeMap.js'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import CustomSwitch from '@/components/ui/CustomSwitch.vue'

const props = defineProps({
  data: {
    type: Object,
    required: true,
    default: () => ({
      currentStep: 0,
      totalSteps: 6,
      steps: [],
      formData: {},
      currentInput: null
    })
  }
})

const emit = defineEmits(['step-submit', 'confirm', 'cancel', 'skip'])

const inputValue = ref('')
const inputValueNum = ref(null)
const inputValueBool = ref(false)
const remarkValue = ref('')
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
  const type = currentInputConfig.value?.type || 'text'
  // 特殊处理：如果 field 是 address，渲染组合输入（地址+备注）
  if (currentInputConfig.value?.field === 'address') {
    return 'combined'
  }
  return type
})

const selectOptions = computed(() => {
  if (inputComponentType.value !== 'select') return []
  // 使用前端内置的冰箱类型数据，带图标
  return FRIDGE_TYPE_LIST.map(item => ({
    label: item.name,
    value: item.id,
    icon: item.icon
  }))
})

const fridgeTypeName = computed(() => {
  const typeId = props.data.formData?.fridgeTypeId
  if (!typeId) return ''
  return getFridgeTypeById(Number(typeId))?.name || ''
})

const fridgeTypeIcon = computed(() => {
  const typeId = props.data.formData?.fridgeTypeId
  if (!typeId) return ''
  return getFridgeTypeById(Number(typeId))?.icon || ''
})

const isInputValid = computed(() => {
  const type = inputComponentType.value
  if (type === 'switch') return true
  if (type === 'number') {
    return inputValueNum.value !== null && inputValueNum.value !== undefined && inputValueNum.value !== ''
  }
  if (type === 'combined') return true // 组合输入均为选填
  if (type === 'select') {
    return inputValue.value !== null && inputValue.value !== undefined && inputValue.value !== ''
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
  return Object.entries(fd).some(([key, v]) => {
    if (key === 'isDefault') return v === true
    return v !== null && v !== undefined && v !== ''
  })
})

// 初始化输入值
function resetInput() {
  inputValue.value = ''
  inputValueNum.value = null
  inputValueBool.value = false
  remarkValue.value = ''
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
  } else if (inputComponentType.value === 'switch') {
    inputValueBool.value = existing === true
  } else if (inputComponentType.value === 'combined') {
    inputValue.value = existing !== undefined && existing !== null ? String(existing) : ''
    remarkValue.value = props.data.formData?.remark !== undefined && props.data.formData?.remark !== null ? String(props.data.formData.remark) : ''
  } else if (inputComponentType.value === 'select') {
    inputValue.value = existing !== undefined && existing !== null ? existing : ''
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

function handleNext() {
  if (!canGoNext.value || stepSubmitting.value) return
  stepSubmitting.value = true
  const config = currentInputConfig.value
  const field = config?.field
  let value
  const formData = {...props.data.formData}

  if (inputComponentType.value === 'number') {
    value = inputValueNum.value
  } else if (inputComponentType.value === 'switch') {
    value = inputValueBool.value
  } else if (inputComponentType.value === 'combined') {
    value = String(inputValue.value || '').trim()
    formData.address = value
    formData.remark = remarkValue.value.trim()
  } else if (inputComponentType.value === 'select') {
    value = inputValue.value
  } else {
    value = String(inputValue.value || '').trim()
  }

  if (inputComponentType.value !== 'combined') {
    formData[field] = value
  }

  emit('step-submit', {field, value, formData})
}

function handleSkip() {
  if (stepSubmitting.value) return
  stepSubmitting.value = true
  const field = currentInputConfig.value?.field
  emit('skip', {field, formData: props.data.formData})
}

function handleConfirm() {
  if (creating.value) return
  creating.value = true
  emit('confirm', props.data.formData)
}

function handleCancel() {
  emit('cancel')
}
</script>

<style scoped lang="scss">
.fridge-creation-wizard {
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

  .custom-input.is-error {
    border-color: var(--el-color-danger);
  }

  .custom-input.is-error.is-focused {
    box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
  }

  .combined-input-first {
    margin-bottom: var(--space-2);
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

.wizard-switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-3) var(--space-4);
  background: var(--card-bg);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
}

.wizard-switch-label {
  font-size: 14px;
  color: var(--text-primary);
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
    overflow: hidden;

    img {
      width: 32px;
      height: 32px;
      object-fit: contain;
    }

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

  .confirm-meta-default {
    color: var(--primary-dark);
    background: var(--primary-light);
    border-color: var(--primary-color);
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

.fridge-type-icon-trigger {
  width: 20px;
  height: 20px;
  margin-right: 8px;
  flex-shrink: 0;
}

.fridge-type-icon-option {
  width: 32px;
  height: 32px;
  margin-bottom: 4px;
}

.option-label {
  font-size: 13px;
}
</style>
