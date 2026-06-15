<template>
  <teleport to="body">
    <transition name="dialog-fade">
      <div v-if="visible" class="bind-email-dialog-overlay" @click.self="handleClose">
        <div class="bind-email-dialog">
          <div class="dialog-header">
            <div class="dialog-title-container">
              <i class="iconfont icon-mail dialog-icon"/>
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
                class="email-form"
            >
              <el-form-item label="新邮箱" prop="email">
                <EmailVerifyInput
                    v-model="form.email"
                    placeholder="请输入新邮箱地址"
                    captcha-type="BIND"
                    api-url="/user/email/captcha"
                />
              </el-form-item>
              <el-form-item label="验证码" prop="captcha">
                <CustomInput
                    v-model="form.captcha"
                    placeholder="请输入6位验证码"
                    icon="icon-captcha"
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
              确认
            </CustomButton>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {useUserStore} from '@/stores/user'
import {useNotificationStore} from '@/stores/notification'
import showMessage from '@/utils/message'
import notifyGamificationResult from '@/utils/gamificationNotify'
import CustomButton from '@/components/ui/CustomButton.vue'
import CustomInput from '@/components/ui/CustomInput.vue'
import EmailVerifyInput from '@/components/form/EmailVerifyInput.vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  currentEmail: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:visible', 'success'])

const userStore = useUserStore()
const notificationStore = useNotificationStore()
const {updateUserEmail} = userStore

const formRef = ref(null)
const loading = ref(false)

const title = computed(() => props.currentEmail ? '修改邮箱' : '绑定邮箱')

const form = reactive({
  email: '',
  captcha: ''
})

const validateEmail = (rule, value, callback) => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!emailRegex.test(value)) {
    callback(new Error('邮箱格式不正确'))
  } else if (value === props.currentEmail) {
    callback(new Error('新邮箱不能与当前邮箱相同'))
  } else {
    callback()
  }
}

const validateEmailCaptcha = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (!/^\d{6}$/.test(value)) {
    callback(new Error('验证码为6位数字'))
  } else {
    callback()
  }
}

const rules = {
  email: [
    {required: true, validator: validateEmail, trigger: 'blur'}
  ],
  captcha: [
    {required: true, validator: validateEmailCaptcha, trigger: 'blur'}
  ]
}

const resetForm = () => {
  form.email = ''
  form.captcha = ''
  formRef.value?.resetFields()
}

watch(() => props.visible, (val) => {
  if (val) {
    resetForm()
  }
})

const handleClose = () => {
  emit('update:visible', false)
}

const handleSubmit = async () => {
  if (loading.value) return

  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    loading.value = true
    const res = await updateUserEmail({
      email: form.email,
      captcha: form.captcha
    })

    if (res.code === 200) {
      showMessage.success(res.message || '邮箱绑定成功')
      notifyGamificationResult(res, '绑定邮箱')
      emit('success', form.email)
      handleClose()
      // 刷新通知状态（后端会自动标记绑定邮箱提醒为已读）
      await notificationStore.fetchUnreadCount()
      await notificationStore.fetchSummary()
    } else {
      showMessage.error(res.message || '邮箱绑定失败')
    }
  } catch (error) {
    console.error('绑定邮箱失败:', error)
    if (!error.response) {
      showMessage.error(error.message || '绑定邮箱失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.bind-email-dialog-overlay {
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

.bind-email-dialog {
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

.email-form :deep(.el-form-item__label) {
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 14px;
  padding-bottom: 4px;
}

.email-form :deep(.el-form-item) {
  margin-bottom: var(--space-4);
}

.email-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

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
  .bind-email-dialog {
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
