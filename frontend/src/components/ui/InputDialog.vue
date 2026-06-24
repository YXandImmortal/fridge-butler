<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="input-dialog-overlay" @click.self="handleOverlayClick">
      <div class="input-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-edit-box dialog-icon"/>
            <h3 class="dialog-title">{{ title }}</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>
        <div class="dialog-content">
          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="edit-form"
          >
            <el-form-item :label="label" prop="value">
              <CustomInput
                  v-model="form.value"
                  :placeholder="placeholder"
                  :maxlength="maxlength"
                  showWordLimit
                  :icon="icon"
                  size="large"
              />
            </el-form-item>
            <el-form-item
                v-if="showSecondField"
                style="margin-top: 12px"
                :label="secondLabel"
                prop="secondValue"
            >
              <CustomInput
                  v-model="form.secondValue"
                  type="textarea"
                  :placeholder="secondPlaceholder"
                  :maxlength="secondMaxlength"
                  :rows="secondRows"
                  showWordLimit
                  :icon="secondIcon"
                  size="large"
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="dialog-footer">
          <CustomButton @click="handleClose">
            取消
          </CustomButton>
          <CustomButton
              type="primary"
              :loading="loading"
              @click="handleSubmit"
          >
            {{ confirmText }}
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '编辑'
  },
  label: {
    type: String,
    default: '名称'
  },
  placeholder: {
    type: String,
    default: '请输入名称'
  },
  icon: {
    type: String,
    default: 'icon-edit'
  },
  data: {
    type: Object,
    default: () => null
  },
  valueProp: {
    type: String,
    default: 'name'
  },
  confirmText: {
    type: String,
    default: '确认'
  },
  loading: {
    type: Boolean,
    default: false
  },
  maxlength: {
    type: Number,
    default: 20
  },
  showSecondField: {
    type: Boolean,
    default: false
  },
  secondLabel: {
    type: String,
    default: '描述'
  },
  secondPlaceholder: {
    type: String,
    default: '请输入描述'
  },
  secondIcon: {
    type: String,
    default: 'icon-edit'
  },
  secondMaxlength: {
    type: Number,
    default: 200
  },
  secondRows: {
    type: Number,
    default: 4
  },
  secondValueProp: {
    type: String,
    default: 'desc'
  }
})

const emit = defineEmits(['update:visible', 'submit'])

const formRef = ref(null)

const form = reactive({
  value: '',
  secondValue: ''
})

const rules = computed(() => {
  const baseRules = {
    value: [
      {required: true, message: props.placeholder, trigger: 'blur'},
      {min: 1, max: props.maxlength, message: `长度为1-${props.maxlength}个字符`, trigger: 'blur'}
    ]
  }
  if (props.showSecondField) {
    baseRules.secondValue = [
      {required: true, message: props.secondPlaceholder, trigger: 'blur'},
      {min: 1, max: props.secondMaxlength, message: `长度为1-${props.secondMaxlength}个字符`, trigger: 'blur'}
    ]
  }
  return baseRules
})

const resetForm = () => {
  form.value = ''
  form.secondValue = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const initForm = () => {
  if (props.data) {
    form.value = props.data[props.valueProp] || ''
    if (props.showSecondField) {
      form.secondValue = props.data[props.secondValueProp] || ''
    }
  } else {
    form.value = ''
    form.secondValue = ''
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    initForm()
  } else {
    resetForm()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleOverlayClick = () => {
  handleClose()
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  emit('submit', {
    id: props.data?.id,
    value: form.value.trim(),
    secondValue: props.showSecondField ? form.secondValue.trim() : undefined
  })
}
</script>

<style lang="scss" scoped>
.input-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: var(--overlay-bg);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.input-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 440px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6) 0;
}

.dialog-title-container {
  display: flex;
  align-items: center;
  gap: var(--space-2) var(--space-3);
}

.dialog-icon {
  font-size: 24px;
  color: var(--primary-color);
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-close {
  font-size: var(--space-5);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.dialog-close:hover {
  color: var(--primary-color);
  transform: scale(1.1);
}

.dialog-content {
  padding: var(--space-5) var(--space-6);
}

.edit-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding-bottom: 4px;
}

.edit-form :deep(.el-form-item) {
  margin-bottom: 0;
}

/* 验证错误时的样式 */
:deep(.el-form-item.is-error) .custom-input {
  border-color: var(--el-color-danger);
}

:deep(.el-form-item.is-error) .custom-input.is-focused {
  box-shadow: 0 4px 16px rgba(245, 108, 108, 0.3), 0 0 0 3px rgba(245, 108, 108, 0.15);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
}

@keyframes dialog-slide-in {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: all 0.3s ease;
}

.dialog-fade-enter-from {
  opacity: 0;
}

.dialog-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .input-dialog {
    min-width: 280px;
    width: 85%;
  }

  .dialog-header {
    padding: var(--space-4) var(--space-5) 0;
  }

  .dialog-icon {
    font-size: 20px;
  }

  .dialog-title {
    font-size: 18px;
  }

  .dialog-content {
    padding: var(--space-4) var(--space-5);
  }

  .dialog-footer {
    padding: 0 var(--space-5) var(--space-5);
  }
}
</style>
