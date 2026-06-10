<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="visible" class="change-password-dialog-overlay" @click.self="handleOverlayClick">
        <div class="change-password-dialog">
        <div class="dialog-header">
          <div class="dialog-title-container">
            <i class="iconfont icon-lock dialog-icon"/>
            <h3 class="dialog-title">修改密码</h3>
          </div>
          <i class="iconfont icon-close dialog-close" @click="handleClose"/>
        </div>
        <div class="dialog-content">
          <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="password-form"
          >
            <el-form-item label="原密码" prop="originalPassword">
              <CustomInput
                  v-model="form.originalPassword"
                  type="password"
                  placeholder="请输入原密码"
                  icon="icon-lock"
                  size="large"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <CustomInput
                  v-model="form.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  icon="icon-lock"
                  size="large"
              />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmNewPassword">
              <CustomInput
                  v-model="form.confirmNewPassword"
                  type="password"
                  placeholder="请确认新密码"
                  icon="icon-lock"
                  size="large"
              />
            </el-form-item>
            <el-form-item label="验证码" prop="captcha">
              <CaptchaInput
                  v-model="form.captcha"
                  ref="captchaInputRef"
                  :height="40"
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
            确认修改
          </CustomButton>
        </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import {reactive, ref, watch} from 'vue'
import {useUserStore} from '@/stores/user'
import showMessage from '@/utils/message'
import CustomButton from './CustomButton.vue'
import CustomInput from './CustomInput.vue'
import CaptchaInput from '@/components/form/CaptchaInput.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const {changePassword} = userStore

const formRef = ref(null)
const captchaInputRef = ref(null)
const loading = ref(false)

const form = reactive({
  originalPassword: '',
  newPassword: '',
  confirmNewPassword: '',
  captcha: ''
})

const rules = {
  originalPassword: [
    {required: true, message: '原密码不能为空', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '新密码不能为空', trigger: 'blur'},
    {min: 6, message: '新密码长度至少为6位', trigger: 'blur'}
  ],
  confirmNewPassword: [
    {required: true, message: '确认新密码不能为空', trigger: 'blur'},
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
  ],
  captcha: [
    {required: true, message: '验证码不能为空', trigger: 'blur'},
    {min: 4, max: 4, message: '验证码长度为4位', trigger: 'blur'}
  ]
}

const resetForm = () => {
  form.originalPassword = ''
  form.newPassword = ''
  form.confirmNewPassword = ''
  form.captcha = ''
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

watch(() => props.visible, async (val) => {
  if (val) {
    await captchaInputRef.value?.refreshCaptcha()
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
  if (loading.value) return

  // 先验证表单
  try {
    await formRef.value.validate()
  } catch {
    return // 表单验证失败，直接返回
  }

  // 执行修改密码请求
  try {
    loading.value = true

    const changePasswordData = {
      originalPassword: form.originalPassword,
      newPassword: form.newPassword,
      confirmNewPassword: form.confirmNewPassword,
      captcha: form.captcha,
      captchaId: captchaInputRef.value?.captchaId || ''
    }

    const res = await changePassword(changePasswordData)

    if (res.code === 200) {
      showMessage.success('密码修改成功')
      emit('success')
      handleClose()
    } else {
      showMessage.error(res.message || '密码修改失败')
      await captchaInputRef.value?.refreshCaptcha()
    }
  } catch (error) {
    // 网络错误、请求超时、拦截器reject等异常情况
    console.error('修改密码失败:', error)
    if (!error.response) {
      // 只有真正的网络/超时错误才兜底提示；业务错误已由响应拦截器处理
      showMessage.error(error.message || '修改密码失败')
    }
    await captchaInputRef.value?.refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.change-password-dialog-overlay {
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

.change-password-dialog {
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
  .change-password-dialog {
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
