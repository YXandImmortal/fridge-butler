<template>
  <transition name="dialog-fade">
    <div v-if="visible" class="unit-edit-dialog-overlay" @click.self="handleOverlayClick">
      <div class="unit-edit-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-edit dialog-icon" />
            <h3 class="dialog-title">编辑物品单位</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose" />
        </div>
        <div class="dialog-content">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            class="edit-form"
          >
            <el-form-item label="单位名称" prop="unitName">
              <EnhancedInput
                v-model="form.unitName"
                placeholder="请输入物品单位名称"
                maxlength="20"
                show-word-limit
                icon="icon-item"
              />
            </el-form-item>
            <el-form-item label="所属分类">
              <EnhancedInput
                v-model="displayTypeName"
                disabled
                icon="icon-label-alt"
              />
            </el-form-item>
          </el-form>
        </div>
        <div class="dialog-footer">
          <CustomButton class="dialog-btn dialog-btn-cancel" @click="handleClose">
            取消
          </CustomButton>
          <CustomButton
            type="primary"
            class="dialog-btn dialog-btn-confirm"
            :loading="submitting"
            @click="handleSubmit"
          >
            确认修改
          </CustomButton>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { reactive, ref, watch, computed } from 'vue'
import CustomButton from '@/components/CustomButton.vue'
import EnhancedInput from '@/components/EnhancedInput.vue'
import { updateItemUnit } from '@/api/item'
import showMessage from '@/utils/message'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  unitData: {
    type: Object,
    default: () => null
  },
  unitTypeName: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  unitName: ''
})

const displayTypeName = computed(() => props.unitTypeName || '')

const rules = {
  unitName: [
    { required: true, message: '请输入物品单位名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ]
}

const resetForm = () => {
  form.unitName = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const initForm = () => {
  if (props.unitData) {
    form.unitName = props.unitData.unitName || ''
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

  if (!props.unitData) {
    showMessage.error('物品单位数据异常')
    return
  }

  submitting.value = true
  try {
    const res = await updateItemUnit({
      id: props.unitData.id,
      unitName: form.unitName.trim(),
      unitTypeId: props.unitData.unitTypeId
    })
    if (res.code === 200) {
      showMessage.success('修改成功')
      emit('success')
      handleClose()
    } else {
      showMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改物品单位失败:', error)
    showMessage.error('修改失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.unit-edit-dialog-overlay {
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

.unit-edit-dialog {
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
  margin-bottom: var(--space-4);
}

.edit-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding: 0 var(--space-6) var(--space-6);
}

.dialog-btn {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid transparent;
}

.dialog-btn-cancel {
  background: var(--gray-20);
  color: var(--text-secondary);
}

.dialog-btn-cancel:hover {
  background: var(--gray-30);
  transform: translateY(-2px);
}

.dialog-btn-confirm {
  background: var(--primary-color);
  color: var(--text-inverse);
}

.dialog-btn-confirm:hover {
  box-shadow: 0 6px 20px var(--primary-40);
  transform: translateY(-2px);
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
  .unit-edit-dialog {
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
