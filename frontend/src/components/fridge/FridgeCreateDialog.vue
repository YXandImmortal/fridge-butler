<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="fridge-create-dialog-overlay" @click.self="handleOverlayClick">
      <div class="fridge-create-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-fridge-line dialog-icon"/>
            <h3 class="dialog-title">创建冰箱</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>
        <div class="dialog-content">
          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="create-form"
          >
            <el-form-item label="冰箱名称" prop="name">
              <EnhancedInput
                  v-model="form.name"
                  placeholder="请输入冰箱名称，如：家用冰箱、办公室冰箱"
                  maxlength="50"
                  show-word-limit
                  icon="icon-notes"
              />
            </el-form-item>

            <el-form-item label="冰箱类型" prop="fridgeTypeId">
              <CustomSelect
                  v-model="form.fridgeTypeId"
                  :options="fridgeTypeOptions"
                  placeholder="请选择冰箱类型"
                  :grid="true"
                  :full-width="true"
                  clearable
              >
                <template #prefix="{ selected }">
                  <img v-if="selected?.icon" :src="selected.icon" class="fridge-type-icon-trigger" alt=""/>
                </template>
                <template #option="{ option }">
                  <img :src="option.icon" class="fridge-type-icon-option" alt=""/>
                  <span class="option-label">{{ option.label }}</span>
                </template>
              </CustomSelect>
            </el-form-item>

            <el-form-item label="地址" prop="address">
              <EnhancedInput
                  v-model="form.address"
                  placeholder="请输入冰箱地址（选填）"
                  maxlength="100"
                  show-word-limit
                  icon="icon-building-community"
              />
            </el-form-item>

            <el-form-item label="冰箱描述" prop="description">
              <EnhancedInput
                  v-model="form.description"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入冰箱描述（选填）"
                  maxlength="200"
                  show-word-limit
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
            {{ loading ? '创建中...' : '创建冰箱' }}
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import {reactive, ref, watch} from 'vue'
import CustomButton from '@/components/ui/CustomButton.vue'
import EnhancedInput from '@/components/ui/EnhancedInput.vue'
import CustomSelect from '@/components/ui/CustomSelect.vue'
import {FRIDGE_TYPE_LIST} from '@/utils/fridgeTypeMap.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'submit'])

const formRef = ref(null)

const form = reactive({
  name: '',
  description: '',
  address: '',
  fridgeTypeId: null
})

const fridgeTypeOptions = FRIDGE_TYPE_LIST.map(item => ({
  label: item.name,
  value: item.id,
  icon: item.icon
}))

const rules = {
  name: [
    {required: true, message: '请输入冰箱名称', trigger: 'blur'},
    {min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur'}
  ],
  description: [
    {max: 200, message: '描述最多 200 个字符', trigger: 'blur'}
  ],
  address: [
    {max: 100, message: '地址最多 100 个字符', trigger: 'blur'}
  ]
}

const resetForm = () => {
  form.name = ''
  form.description = ''
  form.address = ''
  form.fridgeTypeId = null
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

watch(() => props.visible, (val) => {
  if (!val) {
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
    fridgeName: form.name.trim(),
    remark: form.description.trim() || undefined,
    fridgeAddress: form.address.trim() || undefined,
    fridgeTypeId: form.fridgeTypeId || undefined
  })
}
</script>

<style lang="scss" scoped>
.fridge-create-dialog-overlay {
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

.fridge-create-dialog {
  background: var(--card-bg);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  max-width: 480px;
  width: 90%;
  animation: dialog-slide-in 0.3s ease-out;
  transition: background-color 0.3s ease, color 0.3s ease,
  border-color 0.3s ease, box-shadow 0.3s ease;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-5) var(--space-6);
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

.create-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding-bottom: 4px;
}

.create-form :deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

.create-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
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

@media (max-width: 768px) {
  .fridge-create-dialog {
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
