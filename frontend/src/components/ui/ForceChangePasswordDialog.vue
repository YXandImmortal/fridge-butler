<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="visible" class="force-change-password-dialog-overlay">
        <div class="force-change-password-dialog">
          <div class="dialog-header">
            <div class="dialog-title-container">
              <i class="iconfont icon-alert dialog-icon"/>
              <h3 class="dialog-title">安全提醒</h3>
            </div>
          </div>
          <div class="dialog-content">
            <p class="security-notice">
              您的账号当前使用的是系统初始密码，请立即设置新密码以保障账号安全。
            </p>
            <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-position="top"
                class="password-form"
            >
              <el-form-item label="新密码" prop="newPassword">
                <EnhancedInput
                    v-model="form.newPassword"
                    type="password"
                    placeholder="请输入新密码"
                    icon="icon-lock"
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmNewPassword">
                <EnhancedInput
                    v-model="form.confirmNewPassword"
                    type="password"
                    placeholder="请确认新密码"
                    icon="icon-lock"
                />
              </el-form-item>
            </el-form>
          </div>
          <div class="dialog-footer">
            <CustomButton
                type="primary"
                :loading="loading"
                @click="handleSubmit"
            >
              确认设置
            </CustomButton>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import {reactive, ref, watch} from 'vue'
import showMessage from '@/utils/message'
import {initPassword} from '@/api/user'
import CustomButton from './CustomButton.vue'
import EnhancedInput from './EnhancedInput.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  newPassword: '',
  confirmNewPassword: ''
})

const validatePasswordStrength = (rule, value, callback) => {
  if (!value) {
    callback(new Error('新密码不能为空'))
    return
  }
  if (value.length < 8) {
    callback(new Error('新密码长度至少为8位'))
    return
  }
  if (!/\d/.test(value)) {
    callback(new Error('新密码必须包含数字'))
    return
  }
  if (!/[a-z]/.test(value)) {
    callback(new Error('新密码必须包含小写字母'))
    return
  }
  if (!/[A-Z]/.test(value)) {
    callback(new Error('新密码必须包含大写字母'))
    return
  }
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(value)) {
    callback(new Error('新密码必须包含特殊符号'))
    return
  }
  callback()
}

const rules = {
  newPassword: [
    {required: true,validator: validatePasswordStrength, trigger: 'blur'}
  ],
  confirmNewPassword: [
    {required: true, message: '确认密码不能为空', trigger: 'blur'},
    {
      validator: (rule, value, callback) => {
        if (value !== form.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const resetForm = () => {
  form.newPassword = ''
  form.confirmNewPassword = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

watch(() => props.visible, (val) => {
  if (!val) {
    resetForm()
  }
})

const handleSubmit = async () => {
  if (loading.value) return

  try {
    const valid = await formRef.value.validate().catch(() => false)
    if (!valid) return

    loading.value = true

    const res = await initPassword({
      newPassword: form.newPassword,
      confirmNewPassword: form.confirmNewPassword
    })

    if (res.code === 200) {
      showMessage.success('密码设置成功')
      emit('success')
      emit('update:visible', false)
    } else {
      showMessage.error(res.message || '密码设置失败')
    }
  } catch (error) {
    if (error?.message) {
      console.log('表单验证失败')
    } else {
      console.error('设置密码失败:', error)
      showMessage.error('设置密码失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.force-change-password-dialog-overlay {
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
  z-index: 2000;
}

.force-change-password-dialog {
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
  color: var(--warn-color);
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.dialog-content {
  padding: var(--space-5) var(--space-6);
}

.security-notice {
  margin: 0 0 var(--space-5);
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  padding: var(--space-3) var(--space-4);
  background: var(--warn-color);
  background: rgba(255, 183, 77, 0.12);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--warn-color);
}

.password-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding-bottom: 4px;
}

.password-form :deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

.password-form :deep(.el-form-item:last-child) {
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

@media (max-width: 768px) {
  .force-change-password-dialog {
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
