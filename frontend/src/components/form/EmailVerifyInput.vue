<template>
  <div class="email-verify-input-wrapper">
    <EnhancedInput
        :model-value="email"
        @update:model-value="handleEmailChange"
        :placeholder="placeholder"
        icon="icon-mail"
        :clearable="true"
        :disabled="sending"
        class="email-input"
    />
    <CustomButton
        type="primary"
        class="verify-btn"
        :disabled="!isEmailValid || countdown > 0"
        :loading="sending"
        @click="handleSendCaptcha"
    >
      {{ countdown > 0 ? `${countdown}秒` : '验证' }}
    </CustomButton>
  </div>
</template>

<script setup>
import {ref, computed, watch, onUnmounted} from 'vue'
import request from '@/utils/request.js'
import showMessage from '@/utils/message.js'
import EnhancedInput from '../ui/EnhancedInput.vue'
import CustomButton from '../ui/CustomButton.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入邮箱（选填,用于找回密码）'
  },
  captchaType: {
    type: String,
    default: 'REGISTER',
    validator: (val) => ['REGISTER', 'RESET', 'BIND'].includes(val)
  },
  apiUrl: {
    type: String,
    default: '/auth/email/captcha'
  }
})

const emit = defineEmits(['update:modelValue', 'send'])

const email = ref(props.modelValue || '')
const sending = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 邮箱格式校验
const isEmailValid = computed(() => {
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
  return emailRegex.test(email.value)
})

// 同步父组件的 v-model
watch(() => props.modelValue, (newVal) => {
  if (newVal !== email.value) {
    email.value = newVal || ''
  }
})

const handleEmailChange = (val) => {
  email.value = val || ''
  emit('update:modelValue', val)
}

// 发送验证码
const handleSendCaptcha = async () => {
  if (!isEmailValid.value || countdown.value > 0 || sending.value) return

  try {
    sending.value = true
    const payload = props.apiUrl === '/auth/email/captcha'
      ? { email: email.value, type: props.captchaType }
      : { email: email.value }

    const res = await request({
      url: props.apiUrl,
      method: 'post',
      data: payload
    })

    if (res.code === 200) {
      showMessage.success(res.message || '验证码已发送，请查收邮件')
      startCountdown()
      emit('send', email.value)
    } else {
      showMessage.error(res.message || '验证码发送失败')
    }
  } catch (error) {
    // 请求拦截器已统一处理错误提示，此处无需额外处理
    console.error('发送邮箱验证码失败:', error)
  } finally {
    sending.value = false
  }
}

// 启动倒计时
const startCountdown = () => {
  countdown.value = 60
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped lang="scss">
/* 外层容器统一管理整个组合控件的边框、阴影和圆角 */
.email-verify-input-wrapper {
  display: flex;
  align-items: center;
  width: 100%;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-input);
  background-color: var(--card-bg);
  overflow: hidden;
  transition: all 0.3s ease;
}

/* Focus 时整个组合控件的边框和阴影同步高亮，无断层 */
.email-verify-input-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-input-focus);
}

/* 输入框去掉自身的 border、shadow、圆角和背景，完全融入外层容器 */
.email-verify-input-wrapper :deep(.enhanced-default .el-input__wrapper) {
  border-radius: 0;
  border: none !important;
  box-shadow: none !important;
  background-color: transparent;
}

.email-verify-input-wrapper :deep(.enhanced-default .el-input__wrapper:hover),
.email-verify-input-wrapper :deep(.enhanced-default .el-input__wrapper.is-focus) {
  box-shadow: none !important;
  border: none !important;
}

/* 按钮去掉 border 和圆角，融入外层容器 */
.verify-btn {
  flex-shrink: 0;
  width: 64px;
  height: 44px;
  padding: 0;
  font-size: 13px;
  border-radius: 0;
  border: none;
}

/* 取消 hover 上浮效果 */
.verify-btn:hover:not(:disabled) {
  transform: none;
  box-shadow: none;
}

.verify-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
